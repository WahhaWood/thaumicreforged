package thaumcraft.world.features.biomes.magical_forest;

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
public class ShimmerleafNearWoodFeature extends Feature<NoneFeatureConfiguration> {
   public ShimmerleafNearWoodFeature(Codec<NoneFeatureConfiguration> codec) {
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

      boolean nearWood = false;
      for (BlockPos check : BlockPos.betweenClosed(pos.offset(-5, -3, -5), pos.offset(5, 3, 5))) {
         if (level.getBlockState(check).is(net.minecraft.tags.BlockTags.LOGS)) {
            nearWood = true;
            break;
         }
      }

      if (!nearWood) {
         return false;
      }

      level.setBlock(pos, ModBlocks.SHIMMER_LEAF.get().defaultBlockState(), 3);
      return true;
   }
}
