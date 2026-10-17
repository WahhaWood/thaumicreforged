package thaumicenergistics.block.gearbox.entity;

import appeng.api.implementations.blockentities.ICrankable;
import thaumicenergistics.block.gearbox.GolemGearBoxBlock;
import thaumicenergistics.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GearBoxBlockEntity extends BlockEntity {
   private static final int BASE_POWER = 6;
   private static final int REQUIRED_POWER = 18;
   private static final int SIDE_COUNT = Direction.values().length;
   private static final int MIN_TICKS_PER_SYNC = 6;
   private static final float FULL_ROTATION = (float) Math.PI;
   private static final String NBT_KEY_CRANKABLES = "crankables";
   private static final String NBT_KEY_ROTATION = "shaftrotation";
   private static final String NBT_KEY_IS_THAUM_BOX = "isthaumbox";
   private final int[] shafts = new int[SIDE_COUNT];
   private final ICrankable[] crankables = new ICrankable[SIDE_COUNT];
   private final boolean[] canTurn = new boolean[SIDE_COUNT];
   private int crankableCount = -1;
   private int syncTickCount = 0;
   private boolean isReady = false;
   private boolean hasNewCranks = false;
   private boolean isThaumiumGearbox = false;
   public final boolean[] sideIsFacingCrankable = new boolean[SIDE_COUNT];
   public float shaftRotation = 0.0F;

   public GearBoxBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.GEAR_BOX.get(), pos, state);
      this.isThaumiumGearbox = state.getBlock() instanceof GolemGearBoxBlock;
   }

   public boolean isThaumiumGearbox() {
      return this.isThaumiumGearbox;
   }

   public void updateCrankables() {
      if (this.level != null) {
         this.crankableCount = 0;

         for (Direction side : Direction.values()) {
            int sideIndex = side.ordinal();
            this.crankables[sideIndex] = null;
            this.sideIsFacingCrankable[sideIndex] = false;
            this.canTurn[sideIndex] = false;
            ICrankable crank = ICrankable.get(this.level, this.worldPosition.relative(side), side.getOpposite());
            if (crank != null) {
               this.crankableCount++;
               this.crankables[sideIndex] = crank;
               this.sideIsFacingCrankable[sideIndex] = true;
            }
         }
      }
   }

   public void requestSync() {
      if (this.level != null && !this.level.isClientSide) {
         BlockState state = this.getBlockState();
         this.level.sendBlockUpdated(this.worldPosition, state, state, 2);
      }
   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, GearBoxBlockEntity gearBox) {
      if (level.isClientSide()) {
         return;
      }

      if (gearBox.crankableCount < 0) {
         gearBox.updateCrankables();
      }

      boolean turned = false;
      for (int i = 0; i < SIDE_COUNT; i++) {
         ICrankable crank = gearBox.crankables[i];
         if (crank != null) {
            try {
               if (crank.canTurn()) {
                  crank.applyTurn();
                  turned = true;
               }
            } catch (Exception ignored) {
            }
         }
      }

      if (turned) {
         gearBox.hasNewCranks = true;
         gearBox.syncTickCount++;
         if (gearBox.syncTickCount >= MIN_TICKS_PER_SYNC) {
            gearBox.syncTickCount = 0;
            gearBox.requestSync();
         }

         gearBox.setChanged();
      }
   }

   public static void clientTick(Level level, BlockPos pos, BlockState state, GearBoxBlockEntity gearBox) {
      if (gearBox.shaftRotation != 0.0F) {
         gearBox.shaftRotation -= 0.2F;
         if (gearBox.shaftRotation < 0.001F) {
            gearBox.shaftRotation = 0.0F;
         }
      }
   }

   protected void saveAdditional(@NotNull CompoundTag tag, Provider registries) {
      super.saveAdditional(tag, registries);
      this.writeSyncData(tag);
   }

   protected void loadAdditional(@NotNull CompoundTag tag, Provider registries) {
      super.loadAdditional(tag, registries);
      this.readSyncData(tag);
   }

   @NotNull
   public CompoundTag getUpdateTag(Provider registries) {
      CompoundTag tag = super.getUpdateTag(registries);
      this.writeSyncData(tag);
      return tag;
   }

   public void handleUpdateTag(@NotNull CompoundTag tag, Provider registries) {
      super.handleUpdateTag(tag, registries);
      this.readSyncData(tag);
   }

   @Nullable
   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      CompoundTag tag = new CompoundTag();
      this.writeSyncData(tag);
      return ClientboundBlockEntityDataPacket.create(this, (be, registries) -> tag);
   }

   public void onDataPacket(@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt, Provider registries) {
      super.onDataPacket(net, pkt, registries);
      this.readSyncData(pkt.getTag());
   }

   private void onReady() {
      if (this.level != null) {
         this.updateCrankables();
         this.isThaumiumGearbox = this.getBlockState().getBlock() instanceof GolemGearBoxBlock;
         this.isReady = true;
      }
   }

   private void readSyncData(CompoundTag data) {
      if (data.contains("crankables")) {
         byte flags = data.getByte("crankables");

         for (Direction side : Direction.values()) {
            int mask = 1 << side.ordinal();
            this.sideIsFacingCrankable[side.ordinal()] = (flags & mask) != 0;
         }
      }

      if (data.contains("shaftrotation") && this.shaftRotation < 1.0F) {
         this.shaftRotation += (float) (Math.PI * 2);
      }

      if (data.contains("isthaumbox")) {
         this.isThaumiumGearbox = data.getBoolean("isthaumbox");
      }
   }

   private void writeSyncData(CompoundTag data) {
      if (!this.isReady) {
         this.onReady();
      }

      byte crankableFlags = 0;

      for (Direction side : Direction.values()) {
         if (this.sideIsFacingCrankable[side.ordinal()]) {
            crankableFlags |= (byte)(1 << side.ordinal());
         }
      }

      data.putByte("crankables", crankableFlags);
      if (this.hasNewCranks) {
         data.putBoolean("shaftrotation", true);
         this.hasNewCranks = false;
      }

      data.putBoolean("isthaumbox", this.isThaumiumGearbox);
   }
}
