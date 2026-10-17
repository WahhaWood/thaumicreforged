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
public class ManaPodsFeature extends Feature<NoneFeatureConfiguration> {
   public ManaPodsFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      WorldGenLevel level = context.level();
      RandomSource random = context.random();
      BlockPos pos = context.origin();
      int placed = 0;
      for (int attempt = 0; attempt < 40 && placed < 6; attempt++) {
         BlockPos candidate = pos.offset(
            random.nextInt(13) - 6, random.nextInt(21) - 10, random.nextInt(13) - 6
         );
         net.minecraft.world.level.block.state.BlockState state = level.getBlockState(candidate);
         if (state.is(ModBlocks.GREATWOOD_LEAVES.get())
            || state.is(ModBlocks.SILVERWOOD_LEAVES.get())
            || state.is(net.minecraft.tags.BlockTags.LEAVES)) {
            BlockPos below = candidate.below();
            if (level.isEmptyBlock(below)) {
               level.setBlock(below, ModBlocks.MANA_BEAN.get().defaultBlockState(), 3);
               placed++;
            }
         }
      }

      return placed > 0;
   }
}
