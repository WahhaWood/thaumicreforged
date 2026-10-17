package thaumcraft.world.features.dimension.nether;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import thaumcraft.world.features.ThaumcraftWorldgen;

/** Узел ауры в Нижнем мире. */
public class NetherAuraNodeFeature extends Feature<NoneFeatureConfiguration> {
   public NetherAuraNodeFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      WorldGenLevel level = context.level();
      RandomSource random = context.random();
      return ThaumcraftWorldgen.placeNode(level, context.origin(), random);
   }
}
