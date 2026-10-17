package thaumcraft.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import thaumcraft.init.registry.ModBlocks;

/**
 * Реализация worldgen-фичи (порт по идеям 1.7.10).
 */
public class CinderPearlFeature extends Feature<NoneFeatureConfiguration> {
   public CinderPearlFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      WorldGenLevel level = context.level();
      RandomSource random = context.random();
      BlockPos pos = context.origin();
      if (!ThaumcraftWorldgen.isSoil(level.getBlockState(pos.below()))) {
         return false;
      }

      level.setBlock(pos, ModBlocks.CINDER_PEARL.get().defaultBlockState(), 3);
      return true;
   }
}
