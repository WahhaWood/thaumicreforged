package thaumcraft.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import thaumcraft.init.registry.ModBlocks;

/** Могильный курган с камерой и сундуком (упрощённый порт WorldGenMound). */
public class MoundStructureFeature extends Feature<NoneFeatureConfiguration> {
   public MoundStructureFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      WorldGenLevel level = context.level();
      RandomSource random = context.random();
      BlockPos ground = context.origin().below();
      if (!StructureGen.isSoil(level.getBlockState(ground))) {
         return false;
      }

      BlockState fill = random.nextBoolean()
         ? Blocks.DIRT.defaultBlockState()
         : Blocks.COARSE_DIRT.defaultBlockState();

      for (int dx = -4; dx <= 4; dx++) {
         for (int dz = -4; dz <= 4; dz++) {
            for (int dy = 0; dy <= 4; dy++) {
               double dist = Math.sqrt(dx * dx + dz * dz + dy * dy * 1.6);
               if (dist <= 4.2) {
                  level.setBlock(ground.offset(dx, dy, dz), fill, 3);
               }
            }
         }
      }

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            for (int dy = 0; dy <= 1; dy++) {
               level.setBlock(ground.offset(dx, dy + 1, dz), Blocks.CAVE_AIR.defaultBlockState(), 3);
            }
         }
      }

      level.setBlock(ground.offset(0, 1, 0), ModBlocks.ANCIENT_STONE.get().defaultBlockState(), 3);
      StructureGen.placeChest(level, ground.offset(0, 2, 0), random, StructureGen.dungeonLoot());
      level.setBlock(ground.offset(1, 2, 0), Blocks.TORCH.defaultBlockState(), 3);
      level.setBlock(ground.offset(-1, 2, 0), Blocks.TORCH.defaultBlockState(), 3);
      return true;
   }
}
