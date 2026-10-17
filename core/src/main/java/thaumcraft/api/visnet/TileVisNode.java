package thaumcraft.api.visnet;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.ITickable;
import thaumcraft.api.LevelBlockPos;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;

public abstract class TileVisNode extends ThaumcraftBlockEntity implements ITickable {
   private static int nextNodeId;
   private final int nodeId;
   private static final ThreadLocal<IntSet> CONSUMING_NODES = ThreadLocal.withInitial(IntOpenHashSet::new);
   private static final ThreadLocal<IntSet> REMOVING_NODES = ThreadLocal.withInitial(IntOpenHashSet::new);
   WeakReference<TileVisNode> parent;
   ArrayList<WeakReference<TileVisNode>> children;
   protected int nodeCounter;
   private boolean nodeRegged;
   public boolean nodeRefresh;

   public TileVisNode(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState) {
      super(type, blockPos, blockState);
      this.nodeId = nextNodeId++;
      this.parent = null;
      this.children = new ArrayList<>();
      this.nodeCounter = 0;
      this.nodeRegged = false;
      this.nodeRefresh = false;
   }

   public LevelBlockPos getLocation() {
      return new LevelBlockPos(this);
   }

   /**
    * Квадрат расстояния от блока до точки — порт {@code
    * TileEntity.func_145835_a} (1.7.10). В 1.21.1 у {@code BlockEntity} такого
    * метода нет, поэтому пересчёт здесь, а копия в 1.7.10 брала именно квадрат
    * расстояния — сравнение в {@code TileVisRelay} идёт по нему.
    */
   public double distanceToSqr(double x, double y, double z) {
      double dx = this.worldPosition.getX() + 0.5D - x;
      double dy = this.worldPosition.getY() + 0.5D - y;
      double dz = this.worldPosition.getZ() + 0.5D - z;
      return dx * dx + dy * dy + dz * dz;
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (this.nodeCounter++ % 40 == 0 || this.nodeRefresh) {
         if (!this.nodeRefresh && !this.children.isEmpty()) {
            for (WeakReference<TileVisNode> n : this.children) {
               TileVisNode nn = n == null ? null : n.get();
               if (nn == null || !VisNetHandler.canNodeBeSeen(this, nn)) {
                  this.nodeRefresh = true;
                  break;
               }
            }
         }

         if (this.nodeRefresh) {
            for (WeakReference<TileVisNode> nx : this.children) {
               TileVisNode nn = nx == null ? null : nx.get();
               if (nn != null) {
                  nn.nodeRefresh = true;
               }
            }

            this.children.clear();
            this.parent = null;
         }

         if (this.isSource() && !this.nodeRegged) {
            VisNetHandler.addSource(level, this);
            this.nodeRegged = true;
         } else if (!this.isSource() && !VisNetHandler.isNodeValid(this.getParent())) {
            this.setParent(VisNetHandler.addNode(level, this));
            this.nodeRefresh = true;
         }

         if (this.nodeRefresh) {
            level.sendBlockUpdated(pos, state, state, 3);
            this.setChanged();
            this.parentChanged();
         }

         this.nodeRefresh = false;
      }
   }

   public abstract int getRange();

   public abstract boolean isSource();

   public int consumeVis(Aspect aspect, int vis) {
      int out = 0;
      IntSet consumingNodes = CONSUMING_NODES.get();
      if (!consumingNodes.add(this.nodeId)) {
         return 0;
      } else {
         IntArrayList visited = new IntArrayList();
         boolean var13 = false /* VF: Semaphore variable */;

         try {
            var13 = true;
            Deque<TileVisNode> i = new ArrayDeque();
            i.push(this);

            while (true) {
               TileVisNode node = (TileVisNode)i.peek();
               WeakReference<TileVisNode> pref = node.getParent();
               if (!VisNetHandler.isNodeValid(pref)) {
                  break;
               }

               TileVisNode p = pref.get();
               if (p == null) {
                  break;
               }

               if (p.isSource()) {
                  out = p.consumeVis(aspect, vis);
                  break;
               }

               if (!consumingNodes.add(p.nodeId)) {
                  break;
               }

               visited.add(p.nodeId);
               i.push(p);
            }

            if (out <= 0) {
               var13 = false;
            } else {
               while (!i.isEmpty()) {
                  TileVisNode n = (TileVisNode)i.pop();
                  n.triggerConsumeEffect(aspect);
               }

               var13 = false;
            }
         } finally {
            if (var13) {
               for (int i = 0; i < visited.size(); i++) {
                  consumingNodes.remove(visited.getInt(i));
               }

               consumingNodes.remove(this.nodeId);
            }
         }

         for (int i = 0; i < visited.size(); i++) {
            consumingNodes.remove(visited.getInt(i));
         }

         consumingNodes.remove(this.nodeId);
         return out;
      }
   }

   public void removeThisNode() {
      IntSet removingNodes = REMOVING_NODES.get();
      if (removingNodes.add(this.nodeId)) {
         try {
            for (WeakReference<TileVisNode> n : this.getChildren()) {
               if (n != null) {
                  TileVisNode nn = n.get();
                  if (nn != null) {
                     nn.removeThisNode();
                  }
               }
            }

            this.children = new ArrayList<>();
            if (VisNetHandler.isNodeValid(this.getParent())) {
               this.getParent().get().nodeRefresh = true;
            }

            this.setParent(null);
            this.parentChanged();
            Level level = this.getLevel();
            if (this.isSource() && level != null) {
               HashMap<LevelBlockPos, WeakReference<TileVisNode>> sourcelist = VisNetHandler.sources.get(level.dimension());
               if (sourcelist != null) {
                  sourcelist.remove(this.getLocation());
               }
            }

            if (level != null) {
               level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
               this.setChanged();
            }
         } finally {
            removingNodes.remove(this.nodeId);
         }
      }
   }

   public void setRemoved() {
      this.removeThisNode();
      super.setRemoved();
   }

   public void triggerConsumeEffect(Aspect aspect) {
   }

   public WeakReference<TileVisNode> getParent() {
      return this.parent;
   }

   public WeakReference<TileVisNode> getRootSource() {
      return VisNetHandler.isNodeValid(this.getParent()) ? this.getParent().get().getRootSource() : (this.isSource() ? new WeakReference<>(this) : null);
   }

   public void setParent(WeakReference<TileVisNode> parent) {
      this.parent = parent;
   }

   public ArrayList<WeakReference<TileVisNode>> getChildren() {
      return this.children;
   }

   public void parentChanged() {
   }

   public byte getAttunement() {
      return -1;
   }
}
