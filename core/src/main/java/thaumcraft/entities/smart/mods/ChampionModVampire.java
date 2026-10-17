package thaumcraft.entities.smart.mods;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.Thaumcraft;
import thaumcraft.api.IChampionModifierEffect;

public class ChampionModVampire implements IChampionModifierEffect {
   @OnlyIn(Dist.CLIENT)
   @Override
   public void showFX(LivingEntity boss) {
      RandomSource random = boss.getRandom();
      if (!(random.nextFloat() > 0.2F)) {
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
               0.0,
               0.0,
               0.9F + random.nextFloat() * 0.1F,
               0.0F,
               0.0F,
               0.9F,
               false,
               147,
               4,
               1,
               8 + random.nextInt(4),
               0,
               0.5F + random.nextFloat() * 0.2F
            );
      }
   }
}
