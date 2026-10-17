package thaumcraft.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import thaumcraft.init.registry.ModBlocks;

/** Круг камней на холме с тайником (порт WorldGenHilltopStones из 1.7.10). */
public class HilltopStonesStructureFeature extends Feature<NoneFeatureConfiguration> {
   public HilltopStonesStructureFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      WorldGenLevel level = context.level();
      RandomSource random = context.random();
      BlockPos center = context.origin().below();
      if (!StructureGen.isSoil(level.getBlockState(center))) {
         return false;
      }

      BlockState ancient = ModBlocks.ANCIENT_STONE.get().defaultBlockState();
      BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();

      for (int dx = -3; dx <= 3; dx++) {
         for (int dz = -3; dz <= 3; dz++) {
            boolean corner = Math.abs(dx) == 3 && Math.abs(dz) == 3;
            if (corner || dx == 0 && dz == 0) {
               continue;
            }

            BlockPos pos = center.offset(dx, 0, dz);
            level.setBlock(pos, random.nextBoolean() ? ancient : obsidian, 3);

            for (int dy = 1; dy <= 4; dy++) {
               BlockPos under = pos.below(dy);
               BlockState state = level.getBlockState(under);
               if (state.isAir() || state.is(BlockTags.LEAVES)) {
                  level.setBlock(under, Blocks.DIRT.defaultBlockState(), 3);
               } else {
                  break;
               }
            }
         }
      }

      level.setBlock(center, ancient, 3);
      level.setBlock(center.above(), ancient, 3);
      StructureGen.placeChest(level, center.above(2), random, StructureGen.dungeonLoot());
      return true;
   }
}
