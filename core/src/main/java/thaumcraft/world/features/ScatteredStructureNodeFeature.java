package thaumcraft.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Одиночный узел ауры у поверхности (без привязки к структуре). */
public class ScatteredStructureNodeFeature extends Feature<NoneFeatureConfiguration> {
   public ScatteredStructureNodeFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      WorldGenLevel level = context.level();
      RandomSource random = context.random();
      int x = context.origin().getX();
      int z = context.origin().getZ();
      int surface = StructureGen.surfaceY(level, x, z);
      BlockPos pos = new BlockPos(x, surface + 1, z);
      return ThaumcraftWorldgen.placeNode(level, pos, random);
   }
}
