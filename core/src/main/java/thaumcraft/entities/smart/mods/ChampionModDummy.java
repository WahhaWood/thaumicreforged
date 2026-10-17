package thaumcraft.entities.smart.mods;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.api.IChampionModifierEffect;

public class ChampionModDummy implements IChampionModifierEffect {
   @OnlyIn(Dist.CLIENT)
   @Override
   public void showFX(LivingEntity boss) {
   }
}
