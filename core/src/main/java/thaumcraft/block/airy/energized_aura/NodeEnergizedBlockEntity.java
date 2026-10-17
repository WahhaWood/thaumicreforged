package thaumcraft.block.airy.energized_aura;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.api.visnet.TileVisNode;
import thaumcraft.init.registry.ModBlockEntities;

public class NodeEnergizedBlockEntity extends TileVisNode implements IAspectContainer {
   private AspectList auraBase = new AspectList()
      .add(Aspect.AIR, 20)
      .add(Aspect.FIRE, 20)
      .add(Aspect.EARTH, 20)
      .add(Aspect.WATER, 20)
      .add(Aspect.ORDER, 20)
      .add(Aspect.ENTROPY, 20);
   private int forcedPrimalVisAmount = 0;
   public byte boosted;
   AspectList visBase = new AspectList();
   AspectList vis = new AspectList();
   private NodeType nodeType = NodeType.NORMAL;
   private NodeModifier nodeModifier = null;
   String id = "blank";

   public NodeEnergizedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   public NodeEnergizedBlockEntity(BlockPos pos, BlockState state) {
      this((BlockEntityType<?>)ModBlockEntities.ENERGIZED_AURA_NODE.get(), pos, state);
   }

   @Override
   public int getRange() {
      return 8;
   }

   @Override
   public boolean isSource() {
      return true;
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      super.serverTick(level, pos, state);
      if (this.getNodeType() == NodeType.UNSTABLE && level.random.nextInt(500) == 1) {
         this.visBase = new AspectList();
      }

      if (this.visBase.size() == 0 && this.getAuraBase().size() > 0) {
         this.setupNode();
      }

      this.vis = this.visBase.copy();
   }

   public void setupNode() {
      this.visBase = new AspectList();
      AspectList temp = thaumcraft.research.ResearchManager.reduceToPrimals(this.getAuraBase(), true);

      for (Aspect aspect : temp.getAspects()) {
         int amt = temp.getAmount(aspect);
         if (this.getNodeModifier() == NodeModifier.BRIGHT) {
            amt = (int)((float)amt * 1.2F);
         }

         if (this.getNodeModifier() == NodeModifier.PALE) {
            amt = (int)((float)amt * 0.8F);
         }

         if (this.getNodeModifier() == NodeModifier.FADING) {
            amt = (int)((float)amt * 0.5F);
         }

         amt = (int)Math.floor(Math.sqrt((double)amt));
         // Preset nodes promise a fixed output per primal once energized.
         if (this.forcedPrimalVisAmount > 0) {
            amt = this.forcedPrimalVisAmount;
         }

         if (this.getNodeType() == NodeType.UNSTABLE && this.level != null) {
            amt += this.level.random.nextInt(5) - 2;
         }

         if (amt >= 1) {
            this.visBase.merge(aspect, amt);
         }
      }

      this.setChanged();
      this.markForUpdate();
   }

   @Override
   public int consumeVis(Aspect aspect, int amount) {
      int drain = Math.min(this.vis.getAmount(aspect), amount);
      if (drain > 0) {
         this.vis.reduce(aspect, drain);
      }

      return drain;
   }

   /** Thaumometer phenomenon of an energized node (E18); TC4 has none, its energized node is not an INode. */
   public static final String SCAN_PREFIX = "ENODE";

   /** Position-based, the same on client and server: dim:x:y:z, like the aura node id. */
   public String getScanId() {
      return this.level == null
         ? ""
         : this.level.dimension().location() + ":" + this.worldPosition.getX() + ":" + this.worldPosition.getY() + ":" + this.worldPosition.getZ();
   }

   @Nullable
   public static BlockPos parseScanId(Level level, String scanId) {
      String[] parts = scanId.split(":");
      if (parts.length != 5 || !level.dimension().location().toString().equals(parts[0] + ":" + parts[1])) {
         return null;
      }

      try {
         return new BlockPos(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
      } catch (NumberFormatException e) {
         return null;
      }
   }

   public AspectList getAuraBase() {
      return this.auraBase;
   }

   @Override
   public AspectList getAspects() {
      return this.visBase;
   }

   @Override
   public void setAspects(AspectList aspectList) {
      aspectList.setMaxAmount(256);
      this.auraBase = aspectList;
   }

   public int getForcedPrimalVisAmount() {
      return this.forcedPrimalVisAmount;
   }

   public void setForcedPrimalVisAmount(int forcedPrimalVisAmount) {
      this.forcedPrimalVisAmount = Math.max(0, forcedPrimalVisAmount);
   }

   public NodeType getNodeType() {
      return this.nodeType;
   }

   public void setNodeType(NodeType nodeType) {
      this.nodeType = nodeType;
   }

   public NodeModifier getNodeModifier() {
      return this.nodeModifier;
   }

   public void setNodeModifier(NodeModifier nodeModifier) {
      this.nodeModifier = nodeModifier;
   }

   @Override
   public void loadCustom(CompoundTag tag) {
      super.loadCustom(tag);
      this.id = tag.getString("nodeId");
      this.setNodeType(NodeType.values()[tag.getByte("type")]);
      byte mod = tag.getByte("modifier");
      this.setNodeModifier(mod >= 0 ? NodeModifier.values()[mod] : null);
      this.visBase = new AspectList();
      ListTag tlist = tag.getList("AEB", 10);

      for (int j = 0; j < tlist.size(); j++) {
         CompoundTag rs = tlist.getCompound(j);
         if (rs.contains("key")) {
            Aspect a = Aspect.getAspect(rs.getString("key"));
            if (a != null) {
               this.visBase.add(a, rs.getInt("amount"));
            }
         }
      }

      this.getAuraBase().load(tag);
      this.forcedPrimalVisAmount = tag.getInt("forcedPrimalVisAmount");
   }

   @Override
   public void saveCustom(CompoundTag tag) {
      super.saveCustom(tag);
      tag.putString("nodeId", this.id);
      tag.putByte("type", (byte)this.getNodeType().ordinal());
      tag.putByte("modifier", this.getNodeModifier() == null ? -1 : (byte)this.getNodeModifier().ordinal());
      ListTag tlist = new ListTag();

      for (Aspect aspect : this.visBase.getAspects()) {
         if (aspect != null) {
            CompoundTag f = new CompoundTag();
            f.putString("key", aspect.getTag());
            f.putInt("amount", this.visBase.getAmount(aspect));
            tlist.add(f);
         }
      }

      tag.put("AEB", tlist);
      this.getAuraBase().save(tag);
      tag.putInt("forcedPrimalVisAmount", this.forcedPrimalVisAmount);
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return false;
   }

   @Override
   public int addToContainer(Aspect aspect, int amount) {
      return 0;
   }

   @Override
   public boolean takeFromContainer(Aspect aspect, int amount) {
      return false;
   }

   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      return false;
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      return false;
   }

   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      return false;
   }

   @Override
   public int containerContains(Aspect aspect) {
      return 0;
   }

   @Override
   public byte getAttunement() {
      return -1;
   }
}
