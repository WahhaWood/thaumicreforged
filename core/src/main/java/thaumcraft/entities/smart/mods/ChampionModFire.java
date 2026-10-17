package thaumcraft.entities.smart.mods;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.Thaumcraft;
import thaumcraft.api.IChampionModifierEffect;

public class ChampionModFire implements IChampionModifierEffect {
   @OnlyIn(Dist.CLIENT)
   @Override
   public void showFX(LivingEntity boss) {
      RandomSource random = boss.getRandom();
      float w = random.nextFloat() * boss.getBbWidth();
      float d = random.nextFloat() * boss.getBbWidth();
      float h = random.nextFloat() * boss.getBbHeight();
      Thaumcraft.setup
         .drawGenericParticles(
            boss.level(),
            boss.getBoundingBox().minX + w,
            boss.getBoundingBox().minY + h,
            boss.getBoundingBox().minZ + d,
            0.0,
            0.03,
            0.0,
            0.9F + random.nextFloat() * 0.1F,
            1.0F,
            1.0F,
            0.7F,
            false,
            160,
            10,
            1,
            8 + random.nextInt(4),
            0,
            0.7F + random.nextFloat() * 0.2F
         );
   }
}
