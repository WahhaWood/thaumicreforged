package thaumcraft.entities.smart.mods;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.Thaumcraft;
import thaumcraft.api.IChampionModifierEffect;

public class ChampionModInfested implements IChampionModifierEffect {
   @OnlyIn(Dist.CLIENT)
   @Override
   public void showFX(LivingEntity boss) {
      if (boss.getRandom().nextBoolean()) {
         Thaumcraft.setup.slimeJumpFX(boss, 0);
      }
   }
}
