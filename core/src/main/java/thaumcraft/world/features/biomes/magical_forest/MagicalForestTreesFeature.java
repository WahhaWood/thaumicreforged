package thaumcraft.world.features.biomes.magical_forest;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Реализация worldgen-фичи (порт по идеям 1.7.10).
 */
public class MagicalForestTreesFeature extends Feature<NoneFeatureConfiguration> {
   public MagicalForestTreesFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      // Декоратор магического леса (аналог BiomeGenMagicalForest): в TC4 здесь
      // 1/14 silverwood и 1/10 greatwood на чанк; в placed_feature уже стоит
      // count 2, поэтому внутри делим только между видами ~40/60.
      if (context.random().nextFloat() < 0.4F) {
         return thaumcraft.world.features.SilverwoodTreeFeature.grow(
            context.level(), context.origin(), context.random(), 8, 5, false);
      }

      return thaumcraft.world.features.GreatwoodTreeFeature.grow(
         context.level(), context.origin(), context.random(), false);
   }
}
