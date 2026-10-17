package thaumcraft.world.features.biomes.magical_forest;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import thaumcraft.init.registry.ModBiomes;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.world.features.StructureGen;
import thaumcraft.world.features.ThaumcraftWorldgen;

/** Большое магическое дерево: ствол 2x2, огромная крона, узел в стволе, шиммерлиф и стручки у корней. */
public class BigMagicTreeFeature extends Feature<NoneFeatureConfiguration> {
   public BigMagicTreeFeature(Codec<NoneFeatureConfiguration> codec) {
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

      BlockState log = ModBlocks.GREATWOOD_LOG.get().defaultBlockState();
      BlockState leaves = ModBlocks.GREATWOOD_LEAVES.get().defaultBlockState();
      int height = 18 + random.nextInt(7);

      for (int dy = 0; dy < height; dy++) {
         for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
               level.setBlock(ground.offset(dx, dy, dz), log, 3);
            }
         }
      }

      // ветви
      for (int branch = 0; branch < 4; branch++) {
         int by = 6 + random.nextInt(Math.max(1, height - 8));
         int dirX = random.nextInt(3) - 1;
         int dirZ = dirX == 0 ? (random.nextBoolean() ? 1 : -1) : 0;
         int length = 2 + random.nextInt(3);
         for (int i = 1; i <= length; i++) {
            BlockPos pos = ground.offset(1, by + i / 2, 1).offset(dirX * i, 0, dirZ * i);
            level.setBlock(pos, log, 3);
         }
      }

      // крона
      int topY = ground.getY() + height - 2;
      int radius = 6;
      BlockPos crownCenter = new BlockPos(ground.getX(), topY, ground.getZ());
      for (int dx = -radius; dx <= radius; dx++) {
         for (int dz = -radius; dz <= radius; dz++) {
            for (int dy = -radius; dy <= radius; dy++) {
               double dist = Math.sqrt(dx * dx + dz * dz + dy * dy * 1.5);
               if (dist <= radius + 0.4) {
                  BlockPos pos = crownCenter.offset(dx, dy, dz);
                  if (level.isEmptyBlock(pos) && random.nextInt(4) != 0) {
                     level.setBlock(pos, leaves, 3);
                  }
               }
            }
         }
      }

      // узел в стволе (внутри 2x2 — ставим вместо блока ствола)
      BlockPos nodePos = ground.offset(random.nextInt(2), height / 2 + random.nextInt(4), random.nextInt(2));
      level.setBlock(nodePos, ModBlocks.SILVERWOOD_LOG_NODE.get().defaultBlockState(), 3);
      ThaumcraftWorldgen.initNode(level, nodePos, random);

      // шиммерлиф и мана-стручки у корней
      for (int attempt = 0; attempt < 16; attempt++) {
         BlockPos pos = ground.offset(random.nextInt(9) - 4, random.nextInt(3), random.nextInt(9) - 4);
         BlockState below = level.getBlockState(pos.below());
         if (level.isEmptyBlock(pos) && (below.is(Blocks.GRASS_BLOCK) || below.is(ModBlocks.GREATWOOD_LEAVES.get()))) {
            if (random.nextBoolean()) {
               level.setBlock(pos, ModBlocks.SHIMMER_LEAF.get().defaultBlockState(), 3);
            } else if (level.getBiome(pos).is(ModBiomes.MAGICAL_FOREST)) {
               level.setBlock(pos, ModBlocks.MANA_BEAN.get().defaultBlockState(), 3);
            }
         }
      }

      return true;
   }
}
