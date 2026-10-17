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
public class VisCrystalFeature extends Feature<NoneFeatureConfiguration> {
   public VisCrystalFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      WorldGenLevel level = context.level();
      RandomSource random = context.random();
      BlockPos pos = context.origin();
      if (!ThaumcraftWorldgen.isSoil(level.getBlockState(pos.below()))
         && !level.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.STONE)) {
         return false;
      }

      net.minecraft.world.level.block.Block[] clusters = new net.minecraft.world.level.block.Block[]{
         ModBlocks.AIR_CRYSTAL_CLUSTER.get(),
         ModBlocks.FIRE_CRYSTAL_CLUSTER.get(),
         ModBlocks.WATER_CRYSTAL_CLUSTER.get(),
         ModBlocks.EARTH_CRYSTAL_CLUSTER.get(),
         ModBlocks.ORDER_CRYSTAL_CLUSTER.get(),
         ModBlocks.ENTROPY_CRYSTAL_CLUSTER.get(),
         ModBlocks.MIXED_CRYSTAL_CLUSTER.get()
      };
      level.setBlock(
         pos,
         clusters[random.nextInt(clusters.length)]
            .defaultBlockState()
            // Кластер на земле растёт ВВЕРХ: дефолт FACING=DOWN (рост вниз,
            // опора сверху) здесь осыпался бы по canSurvive в том же тике.
            .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, net.minecraft.core.Direction.UP),
         3
      );
      return true;
   }
}
