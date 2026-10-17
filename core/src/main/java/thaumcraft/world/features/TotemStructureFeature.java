package thaumcraft.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import thaumcraft.init.registry.ModBlocks;

/** Обсидиановый тотем с узлом внутри (порт generateTotem из 1.7.10). */
public class TotemStructureFeature extends Feature<NoneFeatureConfiguration> {
   public TotemStructureFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      WorldGenLevel level = context.level();
      RandomSource random = context.random();
      BlockPos ground = context.origin().below();
      BlockState soil = level.getBlockState(ground);
      if (!StructureGen.isSoil(soil)) {
         return false;
      }

      level.setBlock(ground, ModBlocks.ANCIENT_STONE.get().defaultBlockState(), 3);
      int height = 2 + random.nextInt(4);
      boolean charged = false;

      for (int i = 1; i <= height; i++) {
         BlockPos pos = ground.above(i);
         if (!charged && i > 1 && random.nextInt(4) == 0) {
            charged = true;
            level.setBlock(pos, ModBlocks.CHARGED_OBSIDIAN_TOTEM.get().defaultBlockState(), 3);
            ThaumcraftWorldgen.initNode(level, pos, random);
         } else {
            level.setBlock(pos, ModBlocks.OBSIDIAN_TOTEM.get().defaultBlockState(), 3);
         }
      }

      if (!charged) {
         BlockPos top = ground.above(height);
         level.setBlock(top, ModBlocks.CHARGED_OBSIDIAN_TOTEM.get().defaultBlockState(), 3);
         ThaumcraftWorldgen.initNode(level, top, random);
      }

      for (int i = 1; i <= height + 1; i++) {
         level.setBlock(ground.above(i).offset(1, 0, 0), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
         level.setBlock(ground.above(i).offset(0, 0, 1), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
      }

      return true;
   }
}
