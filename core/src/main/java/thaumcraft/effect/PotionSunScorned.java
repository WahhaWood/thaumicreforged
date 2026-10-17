package thaumcraft.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class PotionSunScorned extends MobEffect {
   public PotionSunScorned() {
      super(MobEffectCategory.HARMFUL, 16308330);
   }

   @Override
   public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
      if (!entity.level().isClientSide && entity.level().isDay() && !entity.level().isRaining()) {
         float brightness = entity.getLightLevelDependentMagicValue();
         net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
         if (brightness > 0.5F && entity.level().canSeeSky(pos)) {
            entity.igniteForSeconds(4);
         }
      }
      return true;
   }

   @Override
   public boolean shouldApplyEffectTickThisTick(int tick, int amplifier) {
      return tick % 40 == 0;
   }
}
