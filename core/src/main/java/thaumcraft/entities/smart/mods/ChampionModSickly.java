package thaumcraft.entities.smart.mods;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.Thaumcraft;
import thaumcraft.api.IChampionModifierEffect;

public class ChampionModSickly implements IChampionModifierEffect {
   @OnlyIn(Dist.CLIENT)
   @Override
   public void showFX(LivingEntity boss) {
      RandomSource random = boss.getRandom();
      if (!random.nextBoolean()) {
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
               -0.02,
               0.0,
               0.2F,
               0.6F + random.nextFloat() * 0.1F,
               0.2F + random.nextFloat() * 0.1F,
               0.5F,
               false,
               1,
               4,
               2,
               5 + random.nextInt(4),
               0,
               0.9F + random.nextFloat() * 0.3F
            );
      }
   }
}
