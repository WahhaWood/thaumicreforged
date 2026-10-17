package thaumcraft.dimension.outer_lands.maze;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import thaumcraft.init.registry.ModBlocks;

/**
 * Портал в лабиринт — порт {@code GenPortal} (1.7.10).
 *
 * <p>Это выход из Outer Lands: комната 13×13 с лестницами по всем четырём
 * сторонам, посреди которой стоит постамент (Obsidian Tile), сам портал и
 * за ним обелиск. По углам комнаты — четыре «кармана», в которые после
 * {@code generateConnections} досыпается камень (палитра AIR_REPL) с
 * уведомлением соседей: это задел под трубы и механизмы, реагирующие на
 * смену блока.
 *
 * <p>В 1.7.10 уведомление делается вызовом {@code func_147444_c} — это
 * {@code World.notifyBlocksOfNeighborChange}, его аналог в 1.21 —
 * {@code blockUpdated}.
 */
public final class GenPortal {
   private GenPortal() {
   }

   /** Порт {@code generatePortal}. */
   public static void generatePortal(Level level, RandomSource random, int cx, int cz, int y, Cell cell) {
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

      // Стены с вырезом под дверные проёмы: пропускается только внешний угол 4..11
      for (int a = 3; a <= 13; a++) {
         for (int b = 3; b <= 13; b++) {
            for (int cxxx = 2; cxxx < 11; cxxx++) {
               if ((a == 3 || a == 13 || b == 3 || b == 13) && (a > 4 || b > 4) && (a > 4 || b < 12)
                  && (a < 12 || b > 4) && (a < 12 || b < 12)) {
                  GenCommon.placeBlock(level, x + a, y + cxxx, z + b, GenCommon.STONE, cell);
               }
            }
         }
      }

      for (int a = 2; a <= 14; a++) {
         for (int b = 2; b <= 14; b++) {
            GenCommon.placeBlock(level, x + a, y - 1, z + b, GenCommon.BEDROCK, cell);
            GenCommon.placeBlock(level, x + a, y, z + b, GenCommon.VOID, cell);
            GenCommon.placeBlock(level, x + a, y + 1, z + b, GenCommon.STONE_NOSPAWN, cell);
            GenCommon.placeBlock(level, x + a, y + 13, z + b, GenCommon.BEDROCK, cell);
            GenCommon.placeBlock(level, x + a, y + 12, z + b, GenCommon.VOID, cell);
            GenCommon.placeBlock(level, x + a, y + 11, z + b, GenCommon.STONE, cell);

            if (a > 1 && a < 15 && b > 1 && b < 15) {
               int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
               for (int g = 0; g < q - 1; g++) {
                  GenCommon.placeBlock(level, x + a, y + 1 + g, z + b, GenCommon.STONE_NOSPAWN, cell);
               }
            }

            if (a > 3 && a < 13 && b > 3 && b < 13) {
               int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
               for (int g = 0; g < q; g++) {
                  GenCommon.placeBlock(level, x + a, y + 11 - g, z + b, GenCommon.STONE_NOSPAWN, cell);
               }
            }
         }
      }

      for (int g = 0; g < 5; g++) {
         GenCommon.placeBlock(level, x + 6 + g, y + 2, z + 4, GenCommon.STAIR_DIRECTIONAL, Direction.NORTH, cell);
         GenCommon.placeBlock(level, x + 6 + g, y + 2, z + 12, GenCommon.STAIR_DIRECTIONAL, Direction.SOUTH, cell);
         GenCommon.placeBlock(level, x + 12, y + 2, z + 6 + g, GenCommon.STAIR_DIRECTIONAL, Direction.EAST, cell);
         GenCommon.placeBlock(level, x + 4, y + 2, z + 6 + g, GenCommon.STAIR_DIRECTIONAL, Direction.WEST, cell);
      }

      GenCommon.generateConnections(level, random, cx, cz, y, cell, 3, true);

      // Угловые карманы: камень с уведомлением соседей
      for (int a = 3; a <= 13; a++) {
         for (int b = 3; b <= 13; b++) {
            for (int cxxxx = 1; cxxxx < 12; cxxxx++) {
               if (a <= 4 && b <= 4 || a <= 4 && b >= 12 || a >= 12 && b <= 4 || a >= 12 && b >= 12) {
                  GenCommon.placeBlock(level, x + a, y + cxxxx, z + b, GenCommon.AIR_REPL, cell);
                  BlockPos p = new BlockPos(x + a, y + cxxxx, z + b);
                  if (level.getBlockState(p).is(Blocks.STONE)) {
                     // func_147444_c = notifyBlocksOfNeighborChange
                     level.blockUpdated(p, Blocks.STONE);
                  }
               }
            }
         }
      }

      // Лестницы в четырёх углах: нижняя смотрит вдоль стены, верхняя перевёрнута.
      // Порядок вызовов перенесён из оригинала построчно (24 вызова), потому что
      // от него зависит, в какие позиции попадут отметки декораций.
      GenCommon.placeBlock(level, x + 5, y + 3, z + 5, GenCommon.STAIR_DIRECTIONAL, Direction.NORTH, cell);
      GenCommon.placeBlock(level, x + 4, y + 3, z + 5, GenCommon.STAIR_DIRECTIONAL, Direction.NORTH, cell);
      GenCommon.placeBlock(level, x + 5, y + 3, z + 4, GenCommon.STAIR_DIRECTIONAL, Direction.WEST, cell);
      GenCommon.placeBlock(level, x + 5, y + 8, z + 5, GenCommon.STAIR_DIRECTIONAL_INV, Direction.NORTH, cell);
      GenCommon.placeBlock(level, x + 4, y + 8, z + 5, GenCommon.STAIR_DIRECTIONAL_INV, Direction.NORTH, cell);
      GenCommon.placeBlock(level, x + 5, y + 8, z + 4, GenCommon.STAIR_DIRECTIONAL_INV, Direction.WEST, cell);
      GenCommon.placeBlock(level, x + 12, y + 3, z + 5, GenCommon.STAIR_DIRECTIONAL, Direction.NORTH, cell);
      GenCommon.placeBlock(level, x + 11, y + 3, z + 5, GenCommon.STAIR_DIRECTIONAL, Direction.NORTH, cell);
      GenCommon.placeBlock(level, x + 11, y + 3, z + 4, GenCommon.STAIR_DIRECTIONAL, Direction.EAST, cell);
      GenCommon.placeBlock(level, x + 12, y + 8, z + 5, GenCommon.STAIR_DIRECTIONAL_INV, Direction.NORTH, cell);
      GenCommon.placeBlock(level, x + 11, y + 8, z + 5, GenCommon.STAIR_DIRECTIONAL_INV, Direction.NORTH, cell);
      GenCommon.placeBlock(level, x + 11, y + 8, z + 4, GenCommon.STAIR_DIRECTIONAL_INV, Direction.EAST, cell);
      GenCommon.placeBlock(level, x + 5, y + 3, z + 11, GenCommon.STAIR_DIRECTIONAL, Direction.SOUTH, cell);
      GenCommon.placeBlock(level, x + 4, y + 3, z + 11, GenCommon.STAIR_DIRECTIONAL, Direction.SOUTH, cell);
      GenCommon.placeBlock(level, x + 5, y + 3, z + 12, GenCommon.STAIR_DIRECTIONAL, Direction.WEST, cell);
      GenCommon.placeBlock(level, x + 5, y + 8, z + 11, GenCommon.STAIR_DIRECTIONAL_INV, Direction.SOUTH, cell);
      GenCommon.placeBlock(level, x + 4, y + 8, z + 11, GenCommon.STAIR_DIRECTIONAL_INV, Direction.SOUTH, cell);
      GenCommon.placeBlock(level, x + 5, y + 8, z + 12, GenCommon.STAIR_DIRECTIONAL_INV, Direction.WEST, cell);
      GenCommon.placeBlock(level, x + 12, y + 3, z + 11, GenCommon.STAIR_DIRECTIONAL, Direction.SOUTH, cell);
      GenCommon.placeBlock(level, x + 11, y + 3, z + 11, GenCommon.STAIR_DIRECTIONAL, Direction.SOUTH, cell);
      GenCommon.placeBlock(level, x + 11, y + 3, z + 12, GenCommon.STAIR_DIRECTIONAL, Direction.EAST, cell);
      GenCommon.placeBlock(level, x + 12, y + 8, z + 11, GenCommon.STAIR_DIRECTIONAL_INV, Direction.SOUTH, cell);
      GenCommon.placeBlock(level, x + 11, y + 8, z + 11, GenCommon.STAIR_DIRECTIONAL_INV, Direction.SOUTH, cell);
      GenCommon.placeBlock(level, x + 11, y + 8, z + 12, GenCommon.STAIR_DIRECTIONAL_INV, Direction.EAST, cell);

      // Постамент (blockEldritch:3 — та же текстура obsidiantile, что у Obsidian Tile),
      // сам портал и обелиск за ним
      level.setBlock(new BlockPos(x + 8, y + 2, z + 8), ModBlocks.OBSIDIAN_TILE.get().defaultBlockState(), 3);
      level.setBlock(new BlockPos(x + 8, y + 3, z + 8), ModBlocks.ELDRITCH_PORTAL.get().defaultBlockState(), 0);
      GenCommon.genObelisk(level, x + 8, y + 4, z + 8);
   }
}