package thaumcraft.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class PotionDeathGaze extends MobEffect {
   public PotionDeathGaze() {
      super(MobEffectCategory.HARMFUL, 6702131);
   }

   public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
      return true;
   }
}
