package thaumcraft.dimension.outer_lands.maze;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.init.registry.ModBiomes;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModEntities;
import thaumcraft.util.BlockUtils;
import thaumcraft.util.Utils;

/**
 * Генератор обычного прохода лабиринта — порт {@code GenPassage} (1.7.10).
 *
 * <p>Один проход занимает чанк 16×16 по высоте {@code y..y+10}:
 * пол и потолок из BEDROCK, стены из VOID с зазорами для дверей,
 * внутренний объём 7×7 из STONE (или STONE_TRAPPED для feature 11) и
 * лестничные «карнизы» по углам.
 *
 * <p>Открытая сторона ячейки получает воронку {@link GenCommon#generateConnections}
 * плюс площадку PAT_CONNECT; закрытая сторона получает глухую стену с
 * декоративными уступами.
 */
public final class GenPassage {
   private GenPassage() {
   }

   /** Порт {@code generateDefaultPassage}. */
   public static void generateDefaultPassage(Level level, RandomSource random, int cx, int cz, int y, Cell cell) {
      int x = cx * 16;
      int z = cz * 16;
      GenCommon.generateConnections(level, random, cx, cz, y, cell, 4, false);

      // mod == 1 — проход насквозь (крест) с колоннами по углам
      int mod = 0;
      if (cell.north && cell.south && cell.west && cell.east && random.nextBoolean()) {
         mod = 1;
      }

      // Основной объём 7×7: пол, потолок, две «юбки» и стены-заглушки
      for (int w = 1; w < 8; w++) {
         for (int h = 1; h < 8; h++) {
            if (w == 4 && h == 4 && mod == 1) {
               GenCommon.placeBlock(level, x + 4 + w, y + 2, z + 4 + h, 7, cell);
               GenCommon.placeBlock(level, x + 4 + w, y + 8, z + 4 + h, 7, cell);
            } else {
               int fill = cell.feature == 11 && random.nextInt(3) == 0 ? GenCommon.STONE_TRAPPED : GenCommon.STONE;
               GenCommon.placeBlock(level, x + 4 + w, y + 2, z + 4 + h, fill, cell);
               GenCommon.placeBlock(level, x + 4 + w, y + 8, z + 4 + h, fill, cell);
            }

            GenCommon.placeBlock(level, x + 4 + w, y, z + 4 + h, GenCommon.BEDROCK, cell);
            GenCommon.placeBlock(level, x + 4 + w, y + 10, z + 4 + h, GenCommon.BEDROCK, cell);
            GenCommon.placeBlock(level, x + 4 + w, y + 1, z + 4 + h, GenCommon.VOID, cell);
            GenCommon.placeBlock(level, x + 4 + w, y + 9, z + 4 + h, GenCommon.VOID, cell);
         }
      }

      northSide(level, random, cell, x, y, z, mod);
      southSide(level, random, cell, x, y, z, mod);
      eastSide(level, random, cell, x, y, z, mod);
      westSide(level, random, cell, x, y, z, mod);

      if (mod == 1) {
         // Угловые колонны сквозного прохода
         GenCommon.placeBlock(level, x + 5, y + 3, z + 5, 3, Direction.EAST, cell);
         GenCommon.placeBlock(level, x + 5, y + 7, z + 5, 5, Direction.EAST, cell);
         GenCommon.placeBlock(level, x + 5, y + 3, z + 6, 3, Direction.NORTH, cell);
         GenCommon.placeBlock(level, x + 5, y + 7, z + 6, 5, Direction.NORTH, cell);
         GenCommon.placeBlock(level, x + 11, y + 3, z + 5, 3, Direction.EAST, cell);
         GenCommon.placeBlock(level, x + 11, y + 7, z + 5, 5, Direction.EAST, cell);
         GenCommon.placeBlock(level, x + 11, y + 3, z + 6, 4, Direction.NORTH, cell);
         GenCommon.placeBlock(level, x + 11, y + 7, z + 6, 6, Direction.NORTH, cell);
         GenCommon.placeBlock(level, x + 5, y + 3, z + 11, 3, Direction.NORTH, cell);
         GenCommon.placeBlock(level, x + 5, y + 7, z + 11, 5, Direction.NORTH, cell);
         GenCommon.placeBlock(level, x + 6, y + 3, z + 11, 4, Direction.EAST, cell);
         GenCommon.placeBlock(level, x + 6, y + 7, z + 11, 6, Direction.EAST, cell);
         GenCommon.placeBlock(level, x + 11, y + 3, z + 11, 4, Direction.NORTH, cell);
         GenCommon.placeBlock(level, x + 11, y + 7, z + 11, 6, Direction.NORTH, cell);
         GenCommon.placeBlock(level, x + 10, y + 3, z + 11, 4, Direction.EAST, cell);
         GenCommon.placeBlock(level, x + 10, y + 7, z + 11, 6, Direction.EAST, cell);
      }

      feature12Crust(level, random, cell, x, y, z);
      feature13Taint(level, random, cell, x, y, z);
      feature14MindSpider(level, random, cell, x, y, z);
   }

   private static void northSide(Level level, RandomSource random, Cell cell, int x, int y, int z, int mod) {
      if (cell.north) {
         for (int w = 2 + mod; w < 9 - mod; w++) {
            for (int h = 2 + mod; h < 9 - mod; h++) {
               GenCommon.placeBlock(
                  level, x + 3 + w, y + 10 - h, z + 5, GenCommon.PAT_CONNECT[h][w], Direction.NORTH, cell
               );
            }
         }

         if (mod == 0) {
            if (cell.west) {
               GenCommon.placeBlock(level, x + 6, y + 3, z + 6, 3, Direction.EAST, cell);
               GenCommon.placeBlock(level, x + 6, y + 7, z + 6, 5, Direction.EAST, cell);
            }

            if (cell.east) {
               GenCommon.placeBlock(level, x + 10, y + 3, z + 6, 3, Direction.EAST, cell);
               GenCommon.placeBlock(level, x + 10, y + 7, z + 6, 5, Direction.EAST, cell);
            }
         }
      } else {
         for (int w = 1; w < 8; w++) {
            for (int h = 1; h < 8; h++) {
               int fill = cell.feature == 11 && random.nextInt(3) == 0 ? GenCommon.STONE_TRAPPED : GenCommon.STONE;
               GenCommon.placeBlock(level, x + 4 + w, y + 9 - h, z + 5, fill, cell);
               GenCommon.placeBlock(level, x + 4 + w, y + 9 - h, z + 4, GenCommon.VOID, cell);
               GenCommon.placeBlock(level, x + 4 + w, y + 9 - h, z + 3, GenCommon.BEDROCK, cell);
               if (h == 7) {
                  GenCommon.placeBlock(level, x + 4 + w, y + 1, z + 4, GenCommon.BEDROCK, cell);
                  GenCommon.placeBlock(level, x + 4 + w, y + 9, z + 4, GenCommon.BEDROCK, cell);
               }

               if (w == 7) {
                  GenCommon.placeBlock(level, x + 4, y + 9 - h, z + 4, GenCommon.BEDROCK, cell);
                  GenCommon.placeBlock(level, x + 12, y + 9 - h, z + 4, GenCommon.BEDROCK, cell);
               }
            }
         }

         for (int w = 2; w < 7; w++) {
            GenCommon.placeBlock(level, x + 4 + w, y + 3, z + 6, 3, Direction.EAST, cell);
            GenCommon.placeBlock(level, x + 4 + w, y + 7, z + 6, 5, Direction.EAST, cell);
         }
      }
   }

   private static void southSide(Level level, RandomSource random, Cell cell, int x, int y, int z, int mod) {
      if (cell.south) {
         for (int w = 2 + mod; w < 9 - mod; w++) {
            for (int h = 2 + mod; h < 9 - mod; h++) {
               GenCommon.placeBlock(
                  level, x + 3 + w, y + 10 - h, z + 11, GenCommon.PAT_CONNECT[h][w], Direction.SOUTH, cell
               );
            }
         }

         if (mod == 0) {
            if (cell.west) {
               GenCommon.placeBlock(level, x + 6, y + 3, z + 10, 4, Direction.EAST, cell);
               GenCommon.placeBlock(level, x + 6, y + 7, z + 10, 6, Direction.EAST, cell);
            }

            if (cell.east) {
               GenCommon.placeBlock(level, x + 10, y + 3, z + 10, 4, Direction.EAST, cell);
               GenCommon.placeBlock(level, x + 10, y + 7, z + 10, 6, Direction.EAST, cell);
            }
         }
      } else {
         for (int w = 1; w < 8; w++) {
            for (int h = 1; h < 8; h++) {
               int fill = cell.feature == 11 && random.nextInt(3) == 0 ? GenCommon.STONE_TRAPPED : GenCommon.STONE;
               GenCommon.placeBlock(level, x + 4 + w, y + 9 - h, z + 11, fill, cell);
               GenCommon.placeBlock(level, x + 4 + w, y + 9 - h, z + 12, GenCommon.VOID, cell);
               GenCommon.placeBlock(level, x + 4 + w, y + 9 - h, z + 13, GenCommon.BEDROCK, cell);
               if (h == 7) {
                  GenCommon.placeBlock(level, x + 4 + w, y + 1, z + 12, GenCommon.BEDROCK, cell);
                  GenCommon.placeBlock(level, x + 4 + w, y + 9, z + 12, GenCommon.BEDROCK, cell);
               }

               if (w == 7) {
                  GenCommon.placeBlock(level, x + 4, y + 9 - h, z + 12, GenCommon.BEDROCK, cell);
                  GenCommon.placeBlock(level, x + 12, y + 9 - h, z + 12, GenCommon.BEDROCK, cell);
               }
            }
         }

         for (int w = 2; w < 7; w++) {
            GenCommon.placeBlock(level, x + 4 + w, y + 3, z + 10, 4, Direction.EAST, cell);
            GenCommon.placeBlock(level, x + 4 + w, y + 7, z + 10, 6, Direction.EAST, cell);
         }
      }
   }

   private static void eastSide(Level level, RandomSource random, Cell cell, int x, int y, int z, int mod) {
      if (cell.east) {
         for (int w = 2 + mod; w < 9 - mod; w++) {
            for (int h = 2 + mod; h < 9 - mod; h++) {
               GenCommon.placeBlock(
                  level, x + 11, y + 10 - h, z + 3 + w, GenCommon.PAT_CONNECT[h][w], Direction.EAST, cell
               );
            }
         }

         if (mod == 0) {
            if (cell.north) {
               GenCommon.placeBlock(level, x + 10, y + 3, z + 6, 4, Direction.NORTH, cell);
               GenCommon.placeBlock(level, x + 10, y + 7, z + 6, 6, Direction.NORTH, cell);
            }

            if (cell.south) {
               GenCommon.placeBlock(level, x + 10, y + 3, z + 10, 4, Direction.NORTH, cell);
               GenCommon.placeBlock(level, x + 10, y + 7, z + 10, 6, Direction.NORTH, cell);
            }
         }
      } else {
         for (int w = 1; w < 8; w++) {
            for (int h = 1; h < 8; h++) {
               int fill = cell.feature == 11 && random.nextInt(3) == 0 ? GenCommon.STONE_TRAPPED : GenCommon.STONE;
               GenCommon.placeBlock(level, x + 11, y + 9 - h, z + 4 + w, fill, cell);
               GenCommon.placeBlock(level, x + 12, y + 9 - h, z + 4 + w, GenCommon.VOID, cell);
               GenCommon.placeBlock(level, x + 13, y + 9 - h, z + 4 + w, GenCommon.BEDROCK, cell);
               if (h == 7) {
                  GenCommon.placeBlock(level, x + 12, y + 1, z + 4 + w, GenCommon.BEDROCK, cell);
                  GenCommon.placeBlock(level, x + 12, y + 9, z + 4 + w, GenCommon.BEDROCK, cell);
               }

               if (w == 7) {
                  GenCommon.placeBlock(level, x + 12, y + 9 - h, z + 4, GenCommon.BEDROCK, cell);
                  GenCommon.placeBlock(level, x + 12, y + 9 - h, z + 12, GenCommon.BEDROCK, cell);
               }
            }
         }

         for (int w = 2; w < 7; w++) {
            GenCommon.placeBlock(level, x + 10, y + 3, z + 4 + w, 4, Direction.NORTH, cell);
            GenCommon.placeBlock(level, x + 10, y + 7, z + 4 + w, 6, Direction.NORTH, cell);
         }
      }
   }

   private static void westSide(Level level, RandomSource random, Cell cell, int x, int y, int z, int mod) {
      if (cell.west) {
         for (int w = 2 + mod; w < 9 - mod; w++) {
            for (int h = 2 + mod; h < 9 - mod; h++) {
               GenCommon.placeBlock(
                  level, x + 5, y + 10 - h, z + 3 + w, GenCommon.PAT_CONNECT[h][w], Direction.WEST, cell
               );
            }
         }

         if (mod == 0) {
            if (cell.north) {
               GenCommon.placeBlock(level, x + 6, y + 3, z + 6, 3, Direction.NORTH, cell);
               GenCommon.placeBlock(level, x + 6, y + 7, z + 6, 5, Direction.NORTH, cell);
            }

            if (cell.south) {
               GenCommon.placeBlock(level, x + 6, y + 3, z + 10, 3, Direction.NORTH, cell);
               GenCommon.placeBlock(level, x + 6, y + 7, z + 10, 5, Direction.NORTH, cell);
            }
         }
      } else {
         for (int w = 1; w < 8; w++) {
            for (int h = 1; h < 8; h++) {
               int fill = cell.feature == 11 && random.nextInt(3) == 0 ? GenCommon.STONE_TRAPPED : GenCommon.STONE;
               GenCommon.placeBlock(level, x + 5, y + 9 - h, z + 4 + w, fill, cell);
               GenCommon.placeBlock(level, x + 4, y + 9 - h, z + 4 + w, GenCommon.VOID, cell);
               GenCommon.placeBlock(level, x + 3, y + 9 - h, z + 4 + w, GenCommon.BEDROCK, cell);
               if (h == 7) {
                  GenCommon.placeBlock(level, x + 4, y + 1, z + 4 + w, GenCommon.BEDROCK, cell);
                  GenCommon.placeBlock(level, x + 4, y + 9, z + 4 + w, GenCommon.BEDROCK, cell);
               }

               if (w == 7) {
                  GenCommon.placeBlock(level, x + 4, y + 9 - h, z + 4, GenCommon.BEDROCK, cell);
                  GenCommon.placeBlock(level, x + 4, y + 9 - h, z + 12, GenCommon.BEDROCK, cell);
               }
            }
         }

         for (int w = 2; w < 7; w++) {
            GenCommon.placeBlock(level, x + 6, y + 3, z + 4 + w, 3, Direction.NORTH, cell);
            GenCommon.placeBlock(level, x + 6, y + 7, z + 4 + w, 5, Direction.NORTH, cell);
         }
      }
   }

   /**
 * feature 12 — заросли Crusted Stone объёмом 9×9×9 вокруг центра ячейки.
 *
 * <p>Внимание к условию оригинала: там проверяется {@code isBlockLoaded},
 * а не «блок воздушный», поэтому корка может лечь поверх любого блока в
 * загруженном чанке. Порядок обращений к {@code random} сохранён — иначе
 * последовательность расстановки разойдётся с оригиналом.
 */
   private static void feature12Crust(Level level, RandomSource random, Cell cell, int x, int y, int z) {
   if (cell.feature != 12) {
      return;
   }

   for (int w = -4; w <= 4; w++) {
      for (int h = -4; h < 5; h++) {
         for (int j = -4; j <= 4; j++) {
            BlockPos p = new BlockPos(x + 8 + w, y + 4 + h, z + 8 + j);
            BlockState here = level.getBlockState(p);
            if ((level.isLoaded(p)
               || here.is(ModBlocks.ANCIENT_STONE.get())
               || here.is(ModBlocks.ELDRITCH_STONE_STAIRS.get()))
               && random.nextBoolean()) {
               GenCommon.placeBlock(level, p.getX(), p.getY(), p.getZ(), GenCommon.CRUST, cell);
            }
         }
      }
   }
   }

   /**
 * feature 13 — заражение: нити фумыи (meta 0 или 1) и биом Taint.
 *
 * <p>{@code blockTaintFibres} в 1.7.10 — это один блок с meta 0/1, у нас он
 * разнесён на два: {@code SPORE_STALK} (meta 0) и {@code MATURE_SPORE_STALK}
 * (meta 1); оригинал выбирает meta 1 с вероятностью 1/4. Ставится только
 * рядом с твёрдым блоком ({@code isAdjacentToSolidBlock}).
 */
   private static void feature13Taint(Level level, RandomSource random, Cell cell, int x, int y, int z) {
   if (cell.feature != 13) {
      return;
   }

   for (int w = -4; w <= 4; w++) {
      for (int h = -3; h <= 3; h++) {
         for (int j = -4; j <= 4; j++) {
            BlockPos p = new BlockPos(x + 8 + w, y + 4 + h, z + 8 + j);
            if (level.isLoaded(p) && BlockUtils.isAdjacentToSolidBlock(level, p)) {
               if (random.nextInt(3) != 0) {
                  BlockState fibres = random.nextInt(4) == 0
                     ? ModBlocks.MATURE_SPORE_STALK.get().defaultBlockState()
                     : ModBlocks.SPORE_STALK.get().defaultBlockState();
                  level.setBlock(p, fibres, 3);
               }

               Utils.setBiomeAt(level, p, ModBiomes.TAINT);
            }
         }
      }
   }
   }

   /**
 * feature 14 — логово Разума-паука: паутина с вероятностью 0.35 в кубе 7×7×7
 * вокруг центра и ванильный спавнер с Mind Spider в самом центре.
 *
 * <p>В оригинале спавнер ставится внутри самого внутреннего цикла, то есть
 * многократно — структура сохранена как есть, чтобы не разойтись по числу
 * вызовов {@code setEntityId}.
 */
   private static void feature14MindSpider(Level level, RandomSource random, Cell cell, int x, int y, int z) {
   if (cell.feature != 14) {
      return;
   }

   for (int w = -3; w <= 3; w++) {
      for (int h = -3; h <= 3; h++) {
         for (int j = -3; j <= 3; j++) {
            BlockPos p = new BlockPos(x + 8 + w, y + 4 + h, z + 8 + j);
            // В оригинале паутина ставится ТОЛЬКО в воздух (func_147437_c),
            // иначе она затирала блоки коридора.
            if (level.isLoaded(p) && level.getBlockState(p).isAir() && random.nextFloat() < 0.35F) {
               // field_150321_G = web: подтверждено аспектом TRAP+CLOTH в ConfigAspects
               level.setBlock(p, Blocks.COBWEB.defaultBlockState(), 3);
            }

            BlockPos center = new BlockPos(x + 8, y + 4, z + 8);
            // field_150474_ac = mob_spawner: подтверждено приведением к TileEntityMobSpawner
            level.setBlock(center, Blocks.SPAWNER.defaultBlockState(), 3);
            if (level.getBlockEntity(center) instanceof SpawnerBlockEntity spawner) {
               spawner.setEntityId(ModEntities.MIND_SPIDER.get(), random);
            }
         }
      }
   }
   }
   }
