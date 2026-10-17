package thaumcraft.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModMobEffect;

public class PotionBlurredVision extends MobEffect {
   public PotionBlurredVision() {
      super(MobEffectCategory.HARMFUL, 8421504);
   }

   public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
      if (!Config.enableBlurredVision) {
         entity.removeEffect(ModMobEffect.BLURRED);
      }

      return true;
   }
}
