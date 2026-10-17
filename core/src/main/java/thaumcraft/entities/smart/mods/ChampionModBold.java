package thaumcraft.entities.smart.mods;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.ClientSetup;
import thaumcraft.api.IChampionModifierEffect;
import thaumcraft.particles.fx.particles.FXSpark;

public class ChampionModBold implements IChampionModifierEffect {
   @OnlyIn(Dist.CLIENT)
   @Override
   public void showFX(LivingEntity boss) {
      RandomSource random = boss.getRandom();
      if (!random.nextBoolean()) {
         float w = random.nextFloat() * boss.getBbWidth();
         float d = random.nextFloat() * boss.getBbWidth();
         float h = random.nextFloat() * boss.getBbHeight() / 3.0F;
         FXSpark ef = new FXSpark(
            (ClientLevel)boss.level(), boss.getBoundingBox().minX + w, boss.getBoundingBox().minY + h, boss.getBoundingBox().minZ + d, 0.2F
         );
         ef.setColor(0.3F - random.nextFloat() * 0.1F, 0.0F, 0.8F + random.nextFloat() * 0.2F);
         ClientSetup.add(ef);
      }
   }
}
