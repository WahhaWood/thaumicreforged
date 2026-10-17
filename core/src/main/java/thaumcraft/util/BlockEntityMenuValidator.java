package thaumcraft.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class BlockEntityMenuValidator {
   private static final double MAX_INTERACTION_DISTANCE_SQUARED = 64.0;

   private BlockEntityMenuValidator() {
   }

   public static boolean isValid(Player player, @Nullable BlockEntity blockEntity) {
      if (blockEntity != null && !blockEntity.isRemoved()) {
         Level level = blockEntity.getLevel();
         if (level != null && player.level() == level) {
            BlockPos pos = blockEntity.getBlockPos();
            return level.getBlockEntity(pos) == blockEntity && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
