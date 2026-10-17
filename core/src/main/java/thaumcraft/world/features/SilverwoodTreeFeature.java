package thaumcraft.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import thaumcraft.init.registry.ModBlocks;

/**
 * Серебряное дерево 1:1 с {@code WorldGenSilverwoodTrees} (1.7.10).
 *
 * <p>Что было не так: ствол 1x1 высотой 8–12 с кроной-сферой r=2 — на оригинал
 * не похоже вообще. В TC4 ствол — ромб 5x5 (центральная колонна + 4 боковых),
 * высота 7–10, крона — сферическое облако листвы радиусом ~5, корни-уголки и
 * боковые выносы, узел ауры — внутри центральной колонны (чистый, small).
 */
public class SilverwoodTreeFeature extends Feature<NoneFeatureConfiguration> {
   public SilverwoodTreeFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      // Мироген в TC4: (false, 7, 4) — высота 7–10. Саженцы растят тем же.
      return grow(context.level(), context.origin(), context.random(), 7, 4, true);
   }

   /**
    * Вырастить серебряное дерево.
    *
    * @param worldgen {@code true} — мироген (цветы шиммерлифа под деревом),
    *                 {@code false} — саженец
    */
   public static boolean grow(WorldGenLevel level, BlockPos pos, RandomSource random,
                              int minHeight, int randHeight, boolean worldgen) {
      int x = pos.getX();
      int y = pos.getY();
      int z = pos.getZ();
      int height = random.nextInt(randHeight) + minHeight;
      if (y < 1 || y + height + 1 > level.getMaxBuildHeight()) {
         return false;
      }

      // Проверка свободного места: разлёт 0 у земли, 1 в середине, 3 у верхушки.
      for (int i1 = y; i1 <= y + 1 + height; i1++) {
         int spread = 1;
         if (i1 == y) {
            spread = 0;
         }

         if (i1 >= y + 1 + height - 2) {
            spread = 3;
         }

         for (int j1 = x - spread; j1 <= x + spread; j1++) {
            for (int k1 = z - spread; k1 <= z + spread; k1++) {
               if (i1 < level.getMinBuildHeight() || i1 >= level.getMaxBuildHeight()) {
                  return false;
               }

               BlockState state = level.getBlockState(new BlockPos(j1, i1, k1));
               if (!state.isAir() && !(state.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock)
                  && i1 > y) {
                  return false;
               }
            }
         }
      }

      if (!level.getBlockState(new BlockPos(x, y - 1, z)).is(BlockTags.DIRT)) {
         return false;
      }

      BlockState log = ModBlocks.SILVERWOOD_LOG.get().defaultBlockState();
      BlockState leaves = ModBlocks.SILVERWOOD_LEAVES.get().defaultBlockState();

      // Крона: облако в кубе x±5, центр по Y прижат к [y+h-3, y+h].
      int start = y + height - 5;
      int end = y + height + 3 + random.nextInt(3);
      for (int k2 = start; k2 <= end; k2++) {
         int cty = Math.max(y + height - 3, Math.min(k2, y + height));
         for (int xx = x - 5; xx <= x + 5; xx++) {
            for (int zz = z - 5; zz <= z + 5; zz++) {
               double dist = (xx - x) * (xx - x) + (k2 - cty) * (k2 - cty) + (zz - z) * (zz - z);
               if (dist < 10 + random.nextInt(8)) {
                  BlockPos p = new BlockPos(xx, k2, zz);
                  if (level.getBlockState(p).isAir()) {
                     level.setBlock(p, leaves, 3);
                  }
               }
            }
         }
      }

      // Ствол: центральная колонна + 4 боковых. В центре с шансом узел.
      int chance = (int)(height * 1.5);
      boolean lastblock = false;
      int var29;
      for (var29 = 0; var29 < height; var29++) {
         BlockPos center = new BlockPos(x, y + var29, z);
         BlockState cur = level.getBlockState(center);
         if (cur.isAir() || cur.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock) {
            if (var29 > 0 && !lastblock && random.nextInt(chance) == 0) {
               level.setBlock(center, ModBlocks.SILVERWOOD_LOG_NODE.get().defaultBlockState(), 3);
               ThaumcraftWorldgen.initSilverwoodNode(level, center, random);
               chance += height;
               lastblock = true;
            } else {
               level.setBlock(center, log, 3);
               lastblock = false;
            }

            setIfFree(level, new BlockPos(x - 1, y + var29, z), log);
            setIfFree(level, new BlockPos(x + 1, y + var29, z), log);
            setIfFree(level, new BlockPos(x, y + var29, z - 1), log);
            setIfFree(level, new BlockPos(x, y + var29, z + 1), log);
         }
      }

      // Верхушка и корни.
      level.setBlock(new BlockPos(x, y + var29, z), log, 3);
      setLog(level, x - 1, y, z - 1, log);
      setLog(level, x + 1, y, z + 1, log);
      setLog(level, x - 1, y, z + 1, log);
      setLog(level, x + 1, y, z - 1, log);
      if (random.nextInt(3) != 0) {
         setLog(level, x - 1, y + 1, z - 1, log);
      }

      if (random.nextInt(3) != 0) {
         setLog(level, x + 1, y + 1, z + 1, log);
      }

      if (random.nextInt(3) != 0) {
         setLog(level, x - 1, y + 1, z + 1, log);
      }

      if (random.nextInt(3) != 0) {
         setLog(level, x + 1, y + 1, z - 1, log);
      }

      // Боковые выносы (кора вдоль оси: meta 5/9 → AXIS_X/AXIS_Z).
      setLog(level, x - 2, y, z, barkX());
      setLog(level, x + 2, y, z, barkX());
      setLog(level, x, y, z - 2, barkZ());
      setLog(level, x, y, z + 2, barkZ());
      setLog(level, x - 2, y - 1, z, log);
      setLog(level, x + 2, y - 1, z, log);
      setLog(level, x, y - 1, z - 2, log);
      setLog(level, x, y - 1, z + 2, log);
      setLog(level, x - 1, y + height - 4, z - 1, log);
      setLog(level, x + 1, y + height - 4, z + 1, log);
      setLog(level, x - 1, y + height - 4, z + 1, log);
      setLog(level, x + 1, y + height - 4, z - 1, log);
      if (random.nextInt(3) == 0) {
         setLog(level, x - 1, y + height - 5, z - 1, log);
      }

      if (random.nextInt(3) == 0) {
         setLog(level, x + 1, y + height - 5, z + 1, log);
      }

      if (random.nextInt(3) == 0) {
         setLog(level, x - 1, y + height - 5, z + 1, log);
      }

      if (random.nextInt(3) == 0) {
         setLog(level, x + 1, y + height - 5, z - 1, log);
      }

      setLog(level, x - 2, y + height - 4, z, barkX());
      setLog(level, x + 2, y + height - 4, z, barkX());
      setLog(level, x, y + height - 4, z - 2, barkZ());
      setLog(level, x, y + height - 4, z + 2, barkZ());

      if (worldgen) {
         ThaumcraftWorldgen.scatterShimmerleaf(level, new BlockPos(x, y, z), random);
      }

      return true;
   }

   private static BlockState barkX() {
      return ModBlocks.SILVERWOOD_LOG.get().defaultBlockState()
         .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
            net.minecraft.core.Direction.Axis.X);
   }

   private static BlockState barkZ() {
      return ModBlocks.SILVERWOOD_LOG.get().defaultBlockState()
         .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
            net.minecraft.core.Direction.Axis.Z);
   }

   private static void setLog(WorldGenLevel level, int x, int y, int z, BlockState log) {
      level.setBlock(new BlockPos(x, y, z), log, 3);
   }

   private static void setIfFree(WorldGenLevel level, BlockPos pos, BlockState log) {
      BlockState cur = level.getBlockState(pos);
      if (cur.isAir() || cur.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock) {
         level.setBlock(pos, log, 3);
      }
   }
}
