package thaumcraft.block.transvector;

import java.util.LinkedHashSet;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;

public abstract class TransvectorBlockEntity extends ThaumcraftBlockEntity {
   private static final String TAG_HAS_TARGET = "hasTarget";
   private static final String TAG_X_TARGET = "xt";
   private static final String TAG_Y_TARGET = "yt";
   private static final String TAG_Z_TARGET = "zt";
   private static final String TAG_CHEATY_MODE = "cheatyMode";
   protected static final int MAX_LINK_DEPTH = 32;
   @Nullable
   protected BlockPos targetPos;
   protected boolean cheatyMode;

   protected TransvectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   public void setTargetPos(@Nullable BlockPos pos) {
      if (!this.canLinkTo(pos)) {
         pos = null;
      }

      if (!Objects.equals(this.targetPos, pos)) {
         this.targetPos = pos;
         this.setChanged();
         this.markForUpdate();
      }
   }

   public final boolean canLinkTo(@Nullable BlockPos pos) {
      return pos == null || !pos.equals(this.worldPosition) && this.isWithinDistanceLimit(pos);
   }

   @Nullable
   public BlockPos getTargetPos() {
      return this.targetPos;
   }

   @Nullable
   public BlockEntity getLinkedBlockEntity() {
      LinkTargetResult result = this.lookupDirectTarget();
      if (result.state().shouldClearTarget()) {
         this.clearTarget();
      }

      return result.state() == LinkTargetState.RESOLVED ? result.blockEntity() : null;
   }

   protected void clearTarget() {
      if (this.targetPos != null) {
         this.targetPos = null;
         super.setChanged();
         this.markForUpdate();
      }
   }

   @Nullable
   protected final BlockPos getValidatedTargetPos() {
      LinkTargetResult result = this.lookupDirectTarget();
      if (result.state().shouldClearTarget()) {
         this.clearTarget();
         return null;
      } else {
         return result.state().isLoadedTarget() ? result.targetPos() : null;
      }
   }

   protected final void clearTargetChain(Iterable<? extends TransvectorBlockEntity> chain) {
      LinkedHashSet<TransvectorBlockEntity> unique = new LinkedHashSet<>();

      for (TransvectorBlockEntity transvector : chain) {
         if (transvector != null) {
            unique.add(transvector);
         }
      }

      unique.forEach(TransvectorBlockEntity::clearTarget);
   }

   protected final LinkTargetResult lookupDirectTarget() {
      if (this.level == null || this.targetPos == null) {
         return LinkTargetResult.noTarget();
      } else if (this.targetPos.equals(this.worldPosition)) {
         return LinkTargetResult.invalid(this.targetPos, LinkTargetState.INVALID_SELF);
      } else if (!this.isWithinDistanceLimit(this.targetPos)) {
         return LinkTargetResult.invalid(this.targetPos, LinkTargetState.INVALID_OUT_OF_RANGE);
      } else if (!this.level.isLoaded(this.targetPos)) {
         return LinkTargetResult.unloaded(this.targetPos);
      } else {
         BlockEntity linked = this.level.getBlockEntity(this.targetPos);
         if (linked != null && !linked.isRemoved()) {
            return LinkTargetResult.resolved(this.targetPos, linked);
         } else {
            return this.tileRequiredAtLink()
               ? LinkTargetResult.invalid(this.targetPos, LinkTargetState.INVALID_MISSING_BLOCK_ENTITY)
               : LinkTargetResult.loadedTarget(this.targetPos);
         }
      }
   }

   private boolean isWithinDistanceLimit(BlockPos pos) {
      if (this.cheatyMode) {
         return true;
      } else {
         int maxDistance = this.getMaxDistance();
         return Math.abs(pos.getX() - this.worldPosition.getX()) <= maxDistance
            && Math.abs(pos.getY() - this.worldPosition.getY()) <= maxDistance
            && Math.abs(pos.getZ() - this.worldPosition.getZ()) <= maxDistance;
      }
   }

   protected abstract int getMaxDistance();

   protected boolean tileRequiredAtLink() {
      return !this.cheatyMode;
   }

   @Override
   public void loadCustom(CompoundTag compound) {
      super.loadCustom(compound);
      if (compound.contains("hasTarget")) {
         boolean hasTarget = compound.getBoolean("hasTarget");
         this.targetPos = hasTarget ? new BlockPos(compound.getInt("xt"), compound.getInt("yt"), compound.getInt("zt")) : null;
      } else if (compound.contains("yt") && compound.getInt("yt") >= 0) {
         this.targetPos = new BlockPos(compound.getInt("xt"), compound.getInt("yt"), compound.getInt("zt"));
      } else {
         this.targetPos = null;
      }

      this.cheatyMode = compound.getBoolean("cheatyMode");
   }

   @Override
   public void saveCustom(CompoundTag compound) {
      super.saveCustom(compound);
      boolean hasTarget = this.targetPos != null;
      compound.putBoolean("hasTarget", hasTarget);
      if (hasTarget) {
         compound.putInt("xt", this.targetPos.getX());
         compound.putInt("yt", this.targetPos.getY());
         compound.putInt("zt", this.targetPos.getZ());
      }

      compound.putBoolean("cheatyMode", this.cheatyMode);
   }
}
