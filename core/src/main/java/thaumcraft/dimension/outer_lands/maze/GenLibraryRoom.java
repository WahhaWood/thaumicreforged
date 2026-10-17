package thaumcraft.dimension.outer_lands.maze;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import thaumcraft.init.registry.ModBlocks;

/**
 * Библиотека — порт {@code GenLibraryRoom} (1.7.10).
 *
 * <p>Комната читается как библиотека из-за восьми пьедесталов по углам и
 * дополнительного пьедестала в центре: {@code blockCosmeticSolid:15}
 * (Ancient Stone Pedestal) с «книгой» — {@code blockEldritch:5}
 * (Glyphed Stone) и плитой сверху или снизу ({@code blockSlabStone} с meta 1
 * для нижней половины и meta 9 = 8|1 для верхней, то есть перевёрнутой).
 *
 * <p>Плиты 1.7.10 кодируются битом 3 состояния; в порте это свойство
 * {@link SlabBlock#TYPE}, поэтому meta 1 → BOTTOM, meta 9 → TOP.
 */
public final class GenLibraryRoom {
   private GenLibraryRoom() {
   }

   /** Порт {@code generateRoom}. */
   public static void generateRoom(Level level, RandomSource random, int cx, int cz, int y, Cell cell) {
      int x = cx * 16;
      int z = cz * 16;

      for (int a = 1; a <= 15; a++) {
         for (int b = 1; b <= 15; b++) {
            for (int c = 0; c < 13; c++) {
               if (a == 1 || a == 15 || b == 1 || b == 15) {
                  GenCommon.placeBlock(level, x + a, y + c, z + b, GenCommon.BEDROCK, cell);
               }
            }
         }
      }

      for (int a = 2; a <= 14; a++) {
         for (int b = 2; b <= 14; b++) {
            for (int cxx = 1; cxx < 12; cxx++) {
               if ((a == 2 || a == 14 || b == 2 || b == 14)
                  && (a != 2 || b <= 3 || b >= 12 || !cell.west || cxx >= 10)
                  && (a != 14 || b <= 3 || b >= 12 || !cell.east || cxx >= 10)
                  && (b != 2 || a <= 3 || a >= 12 || !cell.north || cxx >= 10)
                  && (b != 14 || a <= 3 || a >= 12 || !cell.south || cxx >= 10)) {
                  GenCommon.placeBlock(level, x + a, y + cxx, z + b, GenCommon.VOID, cell);
               }
            }
         }
      }

      for (int a = 3; a <= 13; a++) {
         for (int b = 3; b <= 13; b++) {
            for (int cxxx = 2; cxxx < 11; cxxx++) {
               if (a == 3 || a == 13 || b == 3 || b == 13) {
                  GenCommon.placeBlock(level, x + a, y + cxxx, z + b, GenCommon.STONE, cell);
               }
            }
         }
      }

      for (int a = 2; a <= 14; a++) {
         for (int b = 2; b <= 14; b++) {
            GenCommon.placeBlock(level, x + a, y - 1, z + b, GenCommon.BEDROCK, cell);
            GenCommon.placeBlock(level, x + a, y, z + b, GenCommon.VOID, cell);
            GenCommon.placeBlock(level, x + a, y + 1, z + b, GenCommon.STONE, cell);
            GenCommon.placeBlock(level, x + a, y + 12, z + b, GenCommon.BEDROCK, cell);
            GenCommon.placeBlock(level, x + a, y + 11, z + b, GenCommon.VOID, cell);
            GenCommon.placeBlock(level, x + a, y + 10, z + b, GenCommon.STONE, cell);

            if (a > 3 && a < 13 && b > 3 && b < 13) {
               // Полки вдоль стен
               if (a <= 5 && b <= 5 || a <= 5 && b >= 11 || a >= 11 && b <= 5 || a >= 11 && b >= 11) {
                  GenCommon.placeBlock(level, x + a, y + 2, z + b, GenCommon.STONE, cell);
                  GenCommon.placeBlock(level, x + a, y + 9, z + b, GenCommon.STONE, cell);
               }

               // Пьедесталы по четырём углам комнаты
               if (a == 5 && b == 5 || a == 5 && b == 11 || a == 11 && b == 5 || a == 11 && b == 11) {
                  pedestal(level, x + a, y + 3, z + b);
                  pedestal(level, x + a, y + 8, z + b);
               }
            }
         }
      }

      for (int g = 0; g < 5; g++) {
         GenCommon.placeBlock(level, x + 6 + g, y + 2, z + 4, GenCommon.STAIR_DIRECTIONAL, Direction.NORTH, cell);
         GenCommon.placeBlock(level, x + 6 + g, y + 2, z + 12, GenCommon.STAIR_DIRECTIONAL, Direction.SOUTH, cell);
         GenCommon.placeBlock(level, x + 12, y + 2, z + 6 + g, GenCommon.STAIR_DIRECTIONAL, Direction.EAST, cell);
         GenCommon.placeBlock(level, x + 4, y + 2, z + 6 + g, GenCommon.STAIR_DIRECTIONAL, Direction.WEST, cell);
         GenCommon.placeBlock(level, x + 6 + g, y + 9, z + 4, GenCommon.STAIR_DIRECTIONAL_INV, Direction.NORTH, cell);
         GenCommon.placeBlock(level, x + 6 + g, y + 9, z + 12, GenCommon.STAIR_DIRECTIONAL_INV, Direction.SOUTH, cell);
         GenCommon.placeBlock(level, x + 12, y + 9, z + 6 + g, GenCommon.STAIR_DIRECTIONAL_INV, Direction.EAST, cell);
         GenCommon.placeBlock(level, x + 4, y + 9, z + 6 + g, GenCommon.STAIR_DIRECTIONAL_INV, Direction.WEST, cell);
      }

      // Нижние пьедесталы: пьедестал, «книга», плита сверху
      lowerPedestal(level, x + 5, y, z + 5);
      lowerPedestal(level, x + 5, y, z + 11);
      lowerPedestal(level, x + 11, y, z + 5);
      lowerPedestal(level, x + 11, y, z + 11);
      // Верхние пьедесталы: пьедестал, «книга», плита снизу
      upperPedestal(level, x + 5, y, z + 5);
      upperPedestal(level, x + 5, y, z + 11);
      upperPedestal(level, x + 11, y, z + 5);
      upperPedestal(level, x + 11, y, z + 11);
      // Центральный пьедестал
      level.setBlock(new BlockPos(x + 8, y + 2, z + 8), ModBlocks.ANCIENT_STONE_PEDESTAL.get().defaultBlockState(), 3);
      glyphed(level, x + 8, y + 3, z + 8);
      slab(level, x + 8, y + 4, z + 8, false);
      level.setBlock(new BlockPos(x + 8, y + 9, z + 8), ModBlocks.ANCIENT_STONE_PEDESTAL.get().defaultBlockState(), 3);
      glyphed(level, x + 8, y + 8, z + 8);
      slab(level, x + 8, y + 7, z + 8, true);

      GenCommon.generateConnections(level, random, cx, cz, y, cell, 3, true);
   }

   /** Пьедестал без «книги»: в оригинале два одиночных вызова blockCosmeticSolid:15. */
   private static void pedestal(Level level, int x, int y, int z) {
      level.setBlock(new BlockPos(x, y, z), ModBlocks.ANCIENT_STONE_PEDESTAL.get().defaultBlockState(), 3);
   }

   /** «Книга» — Glyphed Stone (blockEldritch:5). */
   private static void glyphed(Level level, int x, int y, int z) {
      level.setBlock(new BlockPos(x, y, z), ModBlocks.GLYPHED_STONE.get().defaultBlockState(), 3);
   }

   /** Плита: {@code top = true} соответствует meta 9 (бит 3), иначе meta 1. */
   private static void slab(Level level, int x, int y, int z, boolean top) {
      level.setBlock(
         new BlockPos(x, y, z),
         ModBlocks.ELDRITCH_STONE_SLAB.get().defaultBlockState()
            .setValue(SlabBlock.TYPE, top ? SlabType.TOP : SlabType.BOTTOM),
         3
      );
   }

   /** Нижний ряд: пьедестал на y+3, «книга» на y+4, плита сверху на y+5. */
   private static void lowerPedestal(Level level, int x, int y, int z) {
      glyphed(level, x, y + 4, z);
      slab(level, x, y + 5, z, false);
   }

   /** Верхний ряд: пьедестал на y+7, «книга» на y+6, плита снизу. */
   private static void upperPedestal(Level level, int x, int y, int z) {
      glyphed(level, x, y + 7, z);
      slab(level, x, y + 6, z, true);
   }
}