package thaumcraft.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class PotionWarpWard extends MobEffect {
   public PotionWarpWard() {
      super(MobEffectCategory.NEUTRAL, 14742263);
   }

   public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
      return true;
   }
}
