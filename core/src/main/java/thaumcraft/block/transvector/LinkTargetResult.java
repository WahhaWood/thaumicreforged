package thaumcraft.block.transvector;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public record LinkTargetResult(LinkTargetState state, @Nullable BlockPos targetPos, @Nullable BlockEntity blockEntity) {
   public static LinkTargetResult noTarget() {
      return new LinkTargetResult(LinkTargetState.NO_TARGET, null, null);
   }

   public static LinkTargetResult unloaded(BlockPos targetPos) {
      return new LinkTargetResult(LinkTargetState.UNLOADED, targetPos, null);
   }

   public static LinkTargetResult loadedTarget(BlockPos targetPos) {
      return new LinkTargetResult(LinkTargetState.LOADED_TARGET, targetPos, null);
   }

   public static LinkTargetResult resolved(BlockPos targetPos, BlockEntity blockEntity) {
      return new LinkTargetResult(LinkTargetState.RESOLVED, targetPos, blockEntity);
   }

   public static LinkTargetResult invalid(BlockPos targetPos, LinkTargetState state) {
      return new LinkTargetResult(state, targetPos, null);
   }
}
