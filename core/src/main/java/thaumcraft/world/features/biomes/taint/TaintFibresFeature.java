package thaumcraft.world.features.biomes.taint;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.world.features.ThaumcraftWorldgen;

/**
 * Реализация worldgen-фичи (порт по идеям 1.7.10).
 */
public class TaintFibresFeature extends Feature<NoneFeatureConfiguration> {
   public TaintFibresFeature(Codec<NoneFeatureConfiguration> codec) {
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

      int placed = 0;
      for (int attempt = 0; attempt < 6; attempt++) {
         BlockPos candidate = pos.offset(random.nextInt(5) - 2, 0, random.nextInt(5) - 2);
         if (level.isEmptyBlock(candidate) && ThaumcraftWorldgen.isSoil(level.getBlockState(candidate.below()))) {
            level.setBlock(candidate, ModBlocks.FIBROUS_TAINT.get().defaultBlockState(), 3);
            placed++;
         }
      }

      return placed > 0;
   }
}
