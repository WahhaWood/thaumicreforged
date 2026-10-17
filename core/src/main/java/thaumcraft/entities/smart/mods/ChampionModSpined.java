package thaumcraft.entities.smart.mods;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.Thaumcraft;
import thaumcraft.api.IChampionModifierEffect;

public class ChampionModSpined implements IChampionModifierEffect {
   @OnlyIn(Dist.CLIENT)
   @Override
   public void showFX(LivingEntity boss) {
      RandomSource random = boss.getRandom();
      if (!random.nextBoolean()) {
         float w = random.nextFloat() * boss.getBbWidth();
         float d = random.nextFloat() * boss.getBbWidth();
         float h = random.nextFloat() * boss.getBbHeight();
         int p = 176 + random.nextInt(4) * 3;
         Thaumcraft.setup
            .drawGenericParticles(
               boss.level(),
               boss.getBoundingBox().minX + w,
               boss.getBoundingBox().minY + h,
               boss.getBoundingBox().minZ + d,
               0.0,
               0.0,
               0.0,
               0.5F + random.nextFloat() * 0.2F,
               0.1F + random.nextFloat() * 0.2F,
               0.1F + random.nextFloat() * 0.2F,
               0.7F,
               false,
               p,
               3,
               1,
               3,
               0,
               1.2F + random.nextFloat() * 0.3F
            );
      }
   }
}
