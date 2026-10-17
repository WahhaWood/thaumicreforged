package thaumcraft.block.infernal_furnace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.init.registry.ModBlockEntities;

public class InfernalFurnaceNozzleBlockEntity extends BlockEntity implements IEssenceTransport {
   private static final int DRAW_DELAY_INTERVAL = 5;
   private static final int SPEEDY_THRESHOLD_LOW = 40;
   private static final int SPEEDY_THRESHOLD_HIGH = 60;
   private static final int SPEEDY_BOOST = 600;
   private static final int MAX_SUCTION = 128;
   @Nullable
   private BlockPos furnacePos = null;
   @Nullable
   private Direction facing = null;
   private int drawDelay = 0;
   private boolean initialized = false;

   public InfernalFurnaceNozzleBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.INFERNAL_FURNACE_NOZZLE.get(), pos, state);
   }

   protected void loadAdditional(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.loadAdditional(tag, provider);
      if (tag.contains("FurnaceX") && tag.contains("FurnaceY") && tag.contains("FurnaceZ")) {
         this.furnacePos = new BlockPos(tag.getInt("FurnaceX"), tag.getInt("FurnaceY"), tag.getInt("FurnaceZ"));
      }

      if (tag.contains("FacingDir")) {
         this.facing = Direction.from3DDataValue(tag.getInt("FacingDir"));
      }

      this.initialized = tag.getBoolean("Initialized");
   }

   protected void saveAdditional(@NotNull CompoundTag tag, @NotNull Provider provider) {
      super.saveAdditional(tag, provider);
      if (this.furnacePos != null) {
         tag.putInt("FurnaceX", this.furnacePos.getX());
         tag.putInt("FurnaceY", this.furnacePos.getY());
         tag.putInt("FurnaceZ", this.furnacePos.getZ());
      }

      if (this.facing != null) {
         tag.putInt("FacingDir", this.facing.get3DDataValue());
      }

      tag.putBoolean("Initialized", this.initialized);
   }

   @Nullable
   private Direction calculateFacing(BlockPos diff) {
      int dx = diff.getX();
      int dz = diff.getZ();
      if (dx != 0 && dz == 0) {
         return dx > 0 ? Direction.EAST : Direction.WEST;
      } else if (dz != 0 && dx == 0) {
         return dz > 0 ? Direction.SOUTH : Direction.NORTH;
      } else if (Math.abs(dx) >= Math.abs(dz) && dx != 0) {
         return dx > 0 ? Direction.EAST : Direction.WEST;
      } else if (dz != 0) {
         return dz > 0 ? Direction.SOUTH : Direction.NORTH;
      } else {
         return null;
      }
   }

   @Override
   public boolean isConnectable(Direction face) {
      return this.facing != null && face == this.facing;
   }

   @Override
   public boolean canInputFrom(Direction face) {
      return this.facing != null && face == this.facing;
   }

   @Override
   public boolean canOutputTo(Direction face) {
      return false;
   }

   @Override
   public void setSuction(Aspect aspect, int amount) {
   }

   @Override
   public boolean renderExtendedTube() {
      return false;
   }

   @Override
   public int getMinimumSuction() {
      return 0;
   }

   @Override
   public Aspect getSuctionType(Direction face) {
      return Aspect.FIRE;
   }

   @Override
   public int extractEssence(Aspect aspect, int amount, Direction direction) {
      return 0;
   }

   @Override
   public int injectEssence(Aspect aspect, int amount, Direction direction) {
      return 0;
   }

   @Override
   public Aspect getEssenceType(Direction face) {
      return null;
   }

   @Override
   public int getEssenceAmount(Direction face) {
      return 0;
   }

   @Override
   public int getSuctionAmount(net.minecraft.core.Direction face) {
      if (this.level != null && this.furnacePos != null
         && this.level.getBlockEntity(this.furnacePos) instanceof InfernalFurnaceBlockEntity furnace
         && furnace.speedyTime < SPEEDY_THRESHOLD_LOW) {
         return MAX_SUCTION;
      }

      return 0;
   }

   /** Порт TileArcaneFurnaceNozzle: тянет ignis из соседней трубы и даёт печи разгон. */
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (!this.initialized || this.furnacePos == null || this.facing == null) {
         this.initNozzle(level, pos);
         this.initialized = true;
      }

      if (this.furnacePos == null || this.facing == null) {
         return;
      }

      if (!(level.getBlockEntity(this.furnacePos) instanceof InfernalFurnaceBlockEntity furnace)) {
         this.furnacePos = null;
         this.facing = null;
         return;
      }

      if (furnace.speedyTime < SPEEDY_THRESHOLD_HIGH && this.drawEssence(level, pos)) {
         furnace.speedyTime += SPEEDY_BOOST;
      }
   }

   private void initNozzle(Level level, BlockPos pos) {
      for (Direction dir : Direction.values()) {
         if (level.getBlockEntity(pos.relative(dir)) instanceof InfernalFurnaceBlockEntity furnace) {
            this.furnacePos = furnace.getBlockPos();
            this.facing = dir.getOpposite();
            return;
         }
      }
   }

   private boolean drawEssence(Level level, BlockPos pos) {
      if (++this.drawDelay % DRAW_DELAY_INTERVAL != 0) {
         return false;
      }

      BlockEntity tile = thaumcraft.api.ThaumcraftApiHelper.getConnectableTile(level, pos, this.facing);
      if (tile instanceof IEssenceTransport transport) {
         if (!transport.canOutputTo(this.facing.getOpposite())) {
            return false;
         }

         if (transport.getSuctionAmount(this.facing.getOpposite()) < this.getSuctionAmount(this.facing)
            && transport.extractEssence(Aspect.FIRE, 1, this.facing.getOpposite()) == 1) {
            return true;
         }
      }

      return false;
   }

}
