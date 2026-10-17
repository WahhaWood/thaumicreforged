package thaumcraft.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class PotionThaumarhia extends MobEffect {
   public PotionThaumarhia() {
      super(MobEffectCategory.HARMFUL, 6702199);
   }

   public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
      return true;
   }

   public boolean shouldApplyEffectTickThisTick(int tick, int amplifier) {
      return tick % 40 == 0;
   }
}
