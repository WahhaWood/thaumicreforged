package thaumcraft.block.cosmetic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;

public final class TravelStoneEffects {
   private TravelStoneEffects() {
   }

   public static void apply(Level level, BlockPos pos, LivingEntity living) {
      if (level.isClientSide) {
         Thaumcraft.setup.blockSparkle(level, pos.getX(), pos.getY(), pos.getZ(), 32768, 5);
      }
   }
}
