package thaumcraft.api.potions;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import thaumcraft.init.registry.Config;

public class VisExhaustMobEffect extends MobEffect {
   public VisExhaustMobEffect() {
      super(MobEffectCategory.NEUTRAL, 6697847);
   }

   public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
      if (!Config.enableVisExhaust) {
         entity.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this));
      }

      return true;
   }
}
