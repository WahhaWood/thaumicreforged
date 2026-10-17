package thaumcraft.block.jar.node;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.api.wands.IWandable;
import thaumcraft.block.airy.aura_node.AuraNodeBlockEntity;
import thaumcraft.block.jar.JarBaseBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;

public class NodeJarBlockEntity extends JarBaseBlockEntity implements INode, IWandable, IAspectContainer {
   public static final int MAX_ASPECT_AMOUNT = 256;
   private final AspectList aspects = new AspectList(256);
   private final AspectList aspectsBase = new AspectList(256);
   @NotNull
   private NodeType nodeType = NodeType.NORMAL;
   /** Preset nodes: their energized output, kept through the jar. */
   private int awakenPrimalVisAmount = 0;
   @Nullable
   private NodeModifier nodeModifier = null;
   @NotNull
   private String id = "";
   public boolean drop = true;
   public long animateUntilMs = 0L;

   public NodeJarBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.NODE_JAR.get(), pos, state);
   }

   @Override
   public AspectList getAspects() {
      return this.aspects;
   }

   @Override
   public void setAspects(AspectList aspectList) {
      this.aspects.clear();
      this.aspectsBase.clear();
      if (aspectList != null) {
         AspectList normalized = aspectList.copy();
         normalized.setMaxAmount(256);
         this.aspects.add(normalized);
         this.aspectsBase.add(normalized);
      }

      this.markForUpdate();
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return true;
   }

   @Override
   public int addToContainer(Aspect aspect, int amount) {
      int out = 0;
      int current = this.aspects.getAmount(aspect);
      int base = this.aspectsBase.getAmount(aspect);
      if (current + amount > base) {
         out = current + amount - base;
      }

      this.aspects.add(aspect, amount - out);
      this.markForUpdate();
      return out;
   }

   @Override
   public boolean takeFromContainer(Aspect aspect, int amount) {
      if (this.aspects.getAmount(aspect) >= amount) {
         this.aspects.remove(aspect, amount);
         this.markForUpdate();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      return false;
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      return this.aspects.getAmount(aspect) >= amount;
   }

   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      for (Aspect a : aspectList.getAspects()) {
         if (this.aspects.getAmount(a) < aspectList.getAmount(a)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public int containerContains(Aspect aspect) {
      return this.aspects.getAmount(aspect);
   }

   @Override
   public String getId() {
      if (this.id == null || this.id.isEmpty()) {
         this.generateId();
      }

      return this.id;
   }

   public void generateId() {
      if (this.level != null) {
         this.id = this.level.dimension().location() + ":" + this.worldPosition.getX() + ":" + this.worldPosition.getY() + ":" + this.worldPosition.getZ();
      } else {
         this.id = "generated:" + System.currentTimeMillis();
      }
   }

   public void setId(String id) {
      this.id = id;
   }

   public int getAwakenPrimalVisAmount() {
      return this.awakenPrimalVisAmount;
   }

   public void setAwakenPrimalVisAmount(int awakenPrimalVisAmount) {
      this.awakenPrimalVisAmount = Math.max(0, awakenPrimalVisAmount);
   }

   @Override
   public AspectList getAspectsBase() {
      return this.aspectsBase;
   }

   @Override
   public NodeType getNodeType() {
      return this.nodeType;
   }

   @Override
   public void setNodeType(NodeType nodeType) {
      this.nodeType = nodeType;
      this.markForUpdate();
   }

   @Override
   public NodeModifier getNodeModifier() {
      return this.nodeModifier;
   }

   @Override
   public void setNodeModifier(NodeModifier nodeModifier) {
      this.nodeModifier = nodeModifier;
      this.markForUpdate();
   }

   @Override
   public int getNodeVisBase(Aspect aspect) {
      return this.aspectsBase.getAmount(aspect);
   }

   @Override
   public void setNodeVisBase(Aspect aspect, short amount) {
      int current = this.aspectsBase.getAmount(aspect);
      if (current < amount) {
         this.aspectsBase.merge(aspect, amount);
      } else {
         int diff = current - amount;
         if (diff > 0) {
            this.aspectsBase.remove(aspect, diff);
         }
      }

      this.markForUpdate();
   }

   /** TC4 TileJarNode.onWandRightClick: breaks the jar and releases the node into the world. */
   @Override
   public int onWandRightClick(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockPos pos, @NotNull Direction direction) {
      if (!level.isClientSide) {
         this.drop = false;
         BlockState jarState = this.getBlockState();
         AspectList nodeAspects = this.getAspects().copy();
         NodeType type = this.getNodeType();
         NodeModifier modifier = this.getNodeModifier();
         String nodeId = this.getId();
         int awaken = this.awakenPrimalVisAmount;
         level.setBlock(pos, ModBlocks.AURA_NODE.get().defaultBlockState(), 3);
         if (level.getBlockEntity(pos) instanceof AuraNodeBlockEntity node) {
            node.setAspects(nodeAspects);
            node.setNodeModifier(modifier);
            node.setNodeType(type);
            node.setId(nodeId);
            node.setAwakenPrimalVisAmount(awaken);
            node.markForUpdate();
         }

         level.levelEvent(2001, pos, Block.getId(jarState));
         level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 0.9F + level.random.nextFloat() * 0.2F);
      }

      return 1;
   }

   @NotNull
   @Override
   public ItemStack onWandRightClick(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      return ItemStack.EMPTY;
   }

   @Override
   public void onUsingWandTick(ItemStack wandstack, Player player, int count) {
   }

   @Override
   public void onWandStoppedUsing(ItemStack wandstack, Level world, Player player, int count) {
   }

   @Override
   protected void loadCustom(@NotNull CompoundTag tag) {
      this.drop = !tag.contains("drop") || tag.getBoolean("drop");
      this.id = tag.getString("nodeId");
      this.aspects.load(tag, "Aspects");
      this.aspectsBase.clear();
      if (tag.contains("AspectsBase", 9)) {
         ListTag list = tag.getList("AspectsBase", 10);

         for (int i = 0; i < list.size(); i++) {
            CompoundTag rs = list.getCompound(i);
            if (rs.contains("key")) {
               Aspect a = Aspect.getAspect(rs.getString("key"));
               if (a != null) {
                  this.aspectsBase.add(a, rs.getInt("amount"));
               }
            }
         }
      }

      if (this.aspectsBase.size() == 0 && tag.contains("nodeVisBase")) {
         short oldBase = tag.getShort("nodeVisBase");
         if (oldBase > 0) {
            for (Aspect a : this.aspects.getAspects()) {
               this.aspectsBase.merge(a, oldBase);
            }
         } else {
            this.aspectsBase.add(this.aspects);
         }
      }

      this.awakenPrimalVisAmount = tag.getInt("awakenPrimalVisAmount");
      int t = tag.getByte("type");
      NodeType[] types = NodeType.values();
      this.nodeType = t >= 0 && t < types.length ? types[t] : NodeType.NORMAL;
      byte mod = tag.getByte("modifier");
      if (mod >= 0) {
         NodeModifier[] mods = NodeModifier.values();
         this.nodeModifier = mod < mods.length ? mods[mod] : null;
      } else {
         this.nodeModifier = null;
      }
   }

   @Override
   protected void saveCustom(@NotNull CompoundTag tag) {
      tag.putBoolean("drop", this.drop);
      if (this.id == null || this.id.isEmpty()) {
         this.generateId();
      }

      tag.putString("nodeId", this.id);
      this.aspects.save(tag, "Aspects");
      ListTag list = new ListTag();

      for (Aspect aspect : this.aspectsBase.getAspects()) {
         if (aspect != null) {
            CompoundTag f = new CompoundTag();
            f.putString("key", aspect.getTag());
            f.putInt("amount", this.aspectsBase.getAmount(aspect));
            list.add(f);
         }
      }

      tag.put("AspectsBase", list);
      tag.putByte("type", (byte)this.nodeType.ordinal());
      tag.putByte("modifier", (byte)(this.nodeModifier == null ? -1 : this.nodeModifier.ordinal()));
      tag.putInt("awakenPrimalVisAmount", this.awakenPrimalVisAmount);
   }

   /** Event 9 returns true on the server too, or the server never sends it to clients. */
   @Override
   public boolean triggerEvent(int id, int value) {
      if (id == 9) {
         if (this.level == null || !this.level.isClientSide) {
            return true;
         }

         for (int yy = -1; yy < 3; yy++) {
            for (int xx = -1; xx < 2; xx++) {
               for (int zz = -1; zz < 2; zz++) {
                  this.level
                     .addParticle(
                        ParticleTypes.ENCHANT,
                        this.worldPosition.getX() + xx + 0.5,
                        this.worldPosition.getY() + yy + 0.5,
                        this.worldPosition.getZ() + zz + 0.5,
                        0.0,
                        0.0,
                        0.0
                     );
               }
            }
         }

         this.animateUntilMs = System.currentTimeMillis() + 1000L;
         return true;
      } else {
         return super.triggerEvent(id, value);
      }
   }

   public boolean isAnimating() {
      return System.currentTimeMillis() < this.animateUntilMs;
   }
}
