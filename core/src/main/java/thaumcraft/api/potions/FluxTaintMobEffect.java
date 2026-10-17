package thaumcraft.api.potions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.init.registry.ModDamageType;

public class FluxTaintMobEffect extends MobEffect {
   public FluxTaintMobEffect() {
      super(MobEffectCategory.NEUTRAL, 6702199);
   }

   public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
      if (entity instanceof ITaintedMob) {
         entity.heal(1.0F);
      } else if (!entity.isInvertedHealAndHarm()) {
         entity.hurt(entity.level().damageSources().source(ModDamageType.TAINT), 1.0F);
      }

      return true;
   }
}
