package thaumcraft.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class PotionUnnaturalHunger extends MobEffect {
   public PotionUnnaturalHunger() {
      super(MobEffectCategory.HARMFUL, 4482611);
   }

   public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
      return true;
   }

   public boolean shouldApplyEffectTickThisTick(int tick, int amplifier) {
      return true;
   }
}
