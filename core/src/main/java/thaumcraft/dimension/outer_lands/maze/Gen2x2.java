package thaumcraft.dimension.outer_lands.maze;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/**
 * Боссовая комната 2×2 — порт {@code Gen2x2} (1.7.10).
 *
 * <p>Комната занимает четыре чанка и собирается из четырёх «углов», каждый из
 * которых перекрывается со своими соседями по границе чанка: {@code a == 1/15}
 * и {@code b == 1/15} — бедрок, {@code 2/14} — «ничего», {@code 3/13} — камень.
 * Каждая четверть ставит половину пола/потолка и свой угол лестниц.
 *
 * <p>Методы намеренно оставлены четырьмя почти одинаковыми функциями, ровно
 * как в оригинале: общая параметризация сбила бы нумерацию циклов и, главное,
 * последовательность вызовов {@code Random}, от которой зависит результат.
 * Сверка велась построчно с оригинальным
 * {@code thaumcraft/common/lib/world/dim/Gen2x2}.
 */
public final class Gen2x2 {
   private Gen2x2() {
   }

   /** Левая верхняя четверть: полная кромка {@code a == 1}, открыта на запад и север. */
   public static void generateUpperLeft(Level level, RandomSource random, int cx, int cz, int y, Cell cell) {
      int x = cx * 16;
      int z = cz * 16;

      for (int a = 1; a <= 15; a++) {
         for (int b = 1; b <= 15; b++) {
            for (int c = 0; c < 13; c++) {
               if (a == 1 || b == 1) {
                  GenCommon.placeBlock(level, x + a, y + c, z + b, GenCommon.BEDROCK, cell);
               }
            }
         }
      }

      for (int a = 2; a <= 15; a++) {
         for (int b = 2; b <= 15; b++) {
            for (int cxx = 1; cxx < 12; cxx++) {
               if ((a == 2 || b == 2) && (a != 2 || b <= 4 || b >= 12 || !cell.west || cxx >= 10)
                  && (b != 2 || a <= 4 || a >= 12 || !cell.north || cxx >= 10)) {
                  GenCommon.placeBlock(level, x + a, y + cxx, z + b, GenCommon.VOID, cell);
               }
            }
         }
      }

      for (int a = 3; a <= 15; a++) {
         for (int b = 3; b <= 15; b++) {
            for (int cxxx = 2; cxxx < 11; cxxx++) {
               if (a == 3 || b == 3) {
                  GenCommon.placeBlock(level, x + a, y + cxxx, z + b, GenCommon.ROCK, cell);
               }
            }
         }
      }

      for (int a = 2; a <= 15; a++) {
         for (int b = 2; b <= 15; b++) {
            GenCommon.placeBlock(level, x + a, y - 1, z + b, GenCommon.BEDROCK, cell);
            GenCommon.placeBlock(level, x + a, y, z + b, GenCommon.VOID, cell);
            GenCommon.placeBlock(level, x + a, y + 1, z + b, GenCommon.STONE_NOSPAWN, cell);
            GenCommon.placeBlock(level, x + a, y + 13, z + b, GenCommon.BEDROCK, cell);
            GenCommon.placeBlock(level, x + a, y + 12, z + b, GenCommon.VOID, cell);
            GenCommon.placeBlock(level, x + a, y + 11, z + b, GenCommon.STONE, cell);
         }
      }

      for (int g = 4; g <= 15; g++) {
         GenCommon.placeBlock(level, x + g, y + 2, z + 4, GenCommon.STAIR_DIRECTIONAL, Direction.NORTH, cell);
         GenCommon.placeBlock(level, x + g, y + 10, z + 4, GenCommon.STAIR_DIRECTIONAL_INV, Direction.NORTH, cell);
      }

      for (int g = 4; g <= 15; g++) {
         GenCommon.placeBlock(level, x + 4, y + 2, z + g, GenCommon.STAIR_DIRECTIONAL, Direction.WEST, cell);
         GenCommon.placeBlock(level, x + 4, y + 10, z + g, GenCommon.STAIR_DIRECTIONAL_INV, Direction.WEST, cell);
      }

      GenCommon.generateConnections(level, random, cx, cz, y, cell, 3, true);
   }

   /** Правая верхняя четверть: полная кромка {@code a == 15}, открыта на восток и север. */
   public static void generateUpperRight(Level level, RandomSource random, int cx, int cz, int y, Cell cell) {
      int x = cx * 16;
      int z = cz * 16;

      for (int a = 0; a <= 15; a++) {
         for (int b = 1; b <= 15; b++) {
            for (int c = 0; c < 13; c++) {
               if (a == 15 || b == 1) {
                  GenCommon.placeBlock(level, x + a, y + c, z + b, GenCommon.BEDROCK, cell);
               }
            }
         }
      }

      for (int a = 0; a <= 14; a++) {
         for (int b = 2; b <= 15; b++) {
            for (int cxx = 1; cxx < 12; cxx++) {
               if ((a == 14 || b == 2) && (a != 14 || b <= 4 || b >= 12 || !cell.east || cxx >= 10)
                  && (b != 2 || a <= 4 || a >= 12 || !cell.north || cxx >= 10)) {
                  GenCommon.placeBlock(level, x + a, y + cxx, z + b, GenCommon.VOID, cell);
               }
            }
         }
      }

      for (int a = 0; a <= 13; a++) {
         for (int b = 3; b <= 15; b++) {
            for (int cxxx = 2; cxxx < 11; cxxx++) {
               if (a == 13 || b == 3) {
                  GenCommon.placeBlock(level, x + a, y + cxxx, z + b, GenCommon.ROCK, cell);
               }
            }
         }
      }

      for (int a = 0; a <= 14; a++) {
         for (int b = 2; b <= 15; b++) {
            GenCommon.placeBlock(level, x + a, y - 1, z + b, GenCommon.BEDROCK, cell);
            GenCommon.placeBlock(level, x + a, y, z + b, GenCommon.VOID, cell);
            GenCommon.placeBlock(level, x + a, y + 1, z + b, GenCommon.STONE_NOSPAWN, cell);
            GenCommon.placeBlock(level, x + a, y + 13, z + b, GenCommon.BEDROCK, cell);
            GenCommon.placeBlock(level, x + a, y + 12, z + b, GenCommon.VOID, cell);
            GenCommon.placeBlock(level, x + a, y + 11, z + b, GenCommon.STONE, cell);
         }
      }

      for (int g = 0; g <= 11; g++) {
         GenCommon.placeBlock(level, x + g, y + 2, z + 4, GenCommon.STAIR_DIRECTIONAL, Direction.NORTH, cell);
         GenCommon.placeBlock(level, x + g, y + 10, z + 4, GenCommon.STAIR_DIRECTIONAL_INV, Direction.NORTH, cell);
      }

      for (int g = 4; g <= 15; g++) {
         GenCommon.placeBlock(level, x + 12, y + 2, z + g, GenCommon.STAIR_DIRECTIONAL, Direction.EAST, cell);
         GenCommon.placeBlock(level, x + 12, y + 10, z + g, GenCommon.STAIR_DIRECTIONAL_INV, Direction.EAST, cell);
      }

      GenCommon.generateConnections(level, random, cx, cz, y, cell, 3, true);
   }

   /** Левая нижняя четверть: полная кромка {@code b == 15}, открыта на запад и юг. */
   public static void generateLowerLeft(Level level, RandomSource random, int cx, int cz, int y, Cell cell) {
      int x = cx * 16;
      int z = cz * 16;

      for (int a = 1; a <= 15; a++) {
         for (int b = 0; b <= 15; b++) {
            for (int c = 0; c < 13; c++) {
               if (a == 1 || b == 15) {
                  GenCommon.placeBlock(level, x + a, y + c, z + b, GenCommon.BEDROCK, cell);
               }
            }
         }
      }

      for (int a = 2; a <= 15; a++) {
         for (int b = 0; b <= 14; b++) {
            for (int cxx = 1; cxx < 12; cxx++) {
               if ((a == 2 || b == 14) && (a != 2 || b <= 4 || b >= 12 || !cell.west || cxx >= 10)
                  && (b != 14 || a <= 4 || a >= 12 || !cell.south || cxx >= 10)) {
                  GenCommon.placeBlock(level, x + a, y + cxx, z + b, GenCommon.VOID, cell);
               }
            }
         }
      }

      for (int a = 3; a <= 15; a++) {
         for (int b = 0; b <= 13; b++) {
            for (int cxxx = 2; cxxx < 11; cxxx++) {
               if (a == 3 || b == 13) {
                  GenCommon.placeBlock(level, x + a, y + cxxx, z + b, GenCommon.ROCK, cell);
               }
            }
         }
      }

      for (int a = 2; a <= 15; a++) {
         for (int b = 0; b <= 14; b++) {
            GenCommon.placeBlock(level, x + a, y - 1, z + b, GenCommon.BEDROCK, cell);
            GenCommon.placeBlock(level, x + a, y, z + b, GenCommon.VOID, cell);
            GenCommon.placeBlock(level, x + a, y + 1, z + b, GenCommon.STONE_NOSPAWN, cell);
            GenCommon.placeBlock(level, x + a, y + 13, z + b, GenCommon.BEDROCK, cell);
            GenCommon.placeBlock(level, x + a, y + 12, z + b, GenCommon.VOID, cell);
            GenCommon.placeBlock(level, x + a, y + 11, z + b, GenCommon.STONE, cell);
         }
      }

      for (int g = 4; g <= 15; g++) {
         GenCommon.placeBlock(level, x + g, y + 2, z + 12, GenCommon.STAIR_DIRECTIONAL, Direction.SOUTH, cell);
         GenCommon.placeBlock(level, x + g, y + 10, z + 12, GenCommon.STAIR_DIRECTIONAL_INV, Direction.SOUTH, cell);
      }

      for (int g = 0; g <= 11; g++) {
         GenCommon.placeBlock(level, x + 4, y + 2, z + g, GenCommon.STAIR_DIRECTIONAL, Direction.WEST, cell);
         GenCommon.placeBlock(level, x + 4, y + 10, z + g, GenCommon.STAIR_DIRECTIONAL_INV, Direction.WEST, cell);
      }

      GenCommon.generateConnections(level, random, cx, cz, y, cell, 3, true);
   }

   /** Правая нижняя четверть: обе кромки, открыта на восток и юг. */
   public static void generateLowerRight(Level level, RandomSource random, int cx, int cz, int y, Cell cell) {
      int x = cx * 16;
      int z = cz * 16;

      for (int a = 0; a <= 15; a++) {
         for (int b = 0; b <= 15; b++) {
            for (int c = 0; c < 13; c++) {
               if (a == 15 || b == 15) {
                  GenCommon.placeBlock(level, x + a, y + c, z + b, GenCommon.BEDROCK, cell);
               }
            }
         }
      }

      for (int a = 0; a <= 14; a++) {
         for (int b = 0; b <= 14; b++) {
            for (int cxx = 1; cxx < 12; cxx++) {
               if ((a == 14 || b == 14) && (a != 14 || b <= 4 || b >= 12 || !cell.east || cxx >= 10)
                  && (b != 14 || a <= 4 || a >= 12 || !cell.south || cxx >= 10)) {
                  GenCommon.placeBlock(level, x + a, y + cxx, z + b, GenCommon.VOID, cell);
               }
            }
         }
      }

      for (int a = 0; a <= 13; a++) {
         for (int b = 0; b <= 13; b++) {
            for (int cxxx = 2; cxxx < 11; cxxx++) {
               if (a == 13 || b == 13) {
                  GenCommon.placeBlock(level, x + a, y + cxxx, z + b, GenCommon.ROCK, cell);
               }
            }
         }
      }

      for (int a = 0; a <= 14; a++) {
         for (int b = 0; b <= 14; b++) {
            GenCommon.placeBlock(level, x + a, y - 1, z + b, GenCommon.BEDROCK, cell);
            GenCommon.placeBlock(level, x + a, y, z + b, GenCommon.VOID, cell);
            GenCommon.placeBlock(level, x + a, y + 1, z + b, GenCommon.STONE_NOSPAWN, cell);
            GenCommon.placeBlock(level, x + a, y + 13, z + b, GenCommon.BEDROCK, cell);
            GenCommon.placeBlock(level, x + a, y + 12, z + b, GenCommon.VOID, cell);
            GenCommon.placeBlock(level, x + a, y + 11, z + b, GenCommon.STONE, cell);
         }
      }

      for (int g = 0; g <= 11; g++) {
         GenCommon.placeBlock(level, x + g, y + 2, z + 12, GenCommon.STAIR_DIRECTIONAL, Direction.SOUTH, cell);
         GenCommon.placeBlock(level, x + g, y + 10, z + 12, GenCommon.STAIR_DIRECTIONAL_INV, Direction.SOUTH, cell);
      }

      for (int g = 0; g <= 12; g++) {
         GenCommon.placeBlock(level, x + 12, y + 2, z + g, GenCommon.STAIR_DIRECTIONAL, Direction.EAST, cell);
         GenCommon.placeBlock(level, x + 12, y + 10, z + g, GenCommon.STAIR_DIRECTIONAL_INV, Direction.EAST, cell);
      }

      GenCommon.generateConnections(level, random, cx, cz, y, cell, 3, true);
   }
}