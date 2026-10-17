package thaumcraft.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class PotionInfectiousVisExhaust extends MobEffect {
   public PotionInfectiousVisExhaust() {
      super(MobEffectCategory.HARMFUL, 6706551);
   }

   public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
      return true;
   }

   public boolean shouldApplyEffectTickThisTick(int tick, int amplifier) {
      return tick % 40 == 0;
   }
}
