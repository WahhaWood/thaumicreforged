package thaumcraft.api;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface IChampionModifierEffect {
   default float performEffect(LivingEntity target, LivingEntity attacker, DamageSource source, float amount) {
      return 0.0F;
   }

   @OnlyIn(Dist.CLIENT)
   default void showFX(LivingEntity paramEntityLivingBase) {
   }
}
