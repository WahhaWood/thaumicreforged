package thaumcraft.dimension.outer_lands.maze;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import thaumcraft.block.eldritch.lock.EldritchLockBlockEntity;

/**
 * Портал боссовой комнаты — порт {@code GenBossRoom} (1.7.10).
 *
 * <p>Ячейка с feature 2/3/4/5 — это один из углов боссовой комнаты 2×2, и
 * генератор достраивает нужный угол через {@link Gen2x2}. После этого на
 * каждой открытой стороне ставится дверной проём 7×7 по шаблону
 * {@code PAT_DOORWAY}: Ancient Doorway по краям, «ничего» (aura node) внутри и
 * один механизм замка в центре (значение 1).
 *
 * <p>Ориентация механизма берётся из последней подошедшей открытой стороны:
 * в оригинале это цепочка независимых {@code if}, а не {@code else if},
 * поэтому при нескольких открытых сторонах побеждает WEST, затем EAST,
 * SOUTH и только потом NORTH. Это сохранено намеренно.
 */
public final class GenBossRoom {
   /** Шаблон дверного проёма 7×7: 0 — пусто, 1 — замок, 2 — Ancient Doorway, 9 — aura node. */
   static final int[][] PAT_DOORWAY = new int[][]{
      {0, 2, 2, 2, 2, 2, 0},
      {2, 2, 9, 9, 9, 2, 2},
      {2, 9, 9, 9, 9, 9, 2},
      {2, 9, 9, 1, 9, 9, 2},
      {2, 9, 9, 9, 9, 9, 2},
      {2, 2, 9, 9, 9, 2, 2},
      {0, 2, 2, 2, 2, 2, 0}
   };

   private GenBossRoom() {
   }

   /** Порт {@code generateRoom}. */
   public static void generateRoom(Level level, RandomSource random, int cx, int cz, int y, Cell cell) {
      int x = cx * 16;
      int z = cz * 16;

      switch (cell.feature) {
         case 2 -> Gen2x2.generateUpperLeft(level, random, cx, cz, y, cell);
         case 3 -> Gen2x2.generateUpperRight(level, random, cx, cz, y, cell);
         case 4 -> Gen2x2.generateLowerLeft(level, random, cx, cz, y, cell);
         case 5 -> Gen2x2.generateLowerRight(level, random, cx, cz, y, cell);
         default -> {
         }
      }

      for (int a = 0; a < 7; a++) {
         for (int b = 0; b < 7; b++) {
            int xx = 0;
            int zz = 0;
            Direction dir = null;
            // ВНИМАНИЕ: в оригинале цепочка независимых if, а не else if —
            // при нескольких открытых сторонах выигрывает последняя (WEST).
            if (cell.north) {
               xx = x + 5 + a;
               zz = z + 3;
               dir = Direction.NORTH;
            }

            if (cell.south) {
               xx = x + 5 + a;
               zz = z + 13;
               dir = Direction.SOUTH;
            }

            if (cell.east) {
               xx = x + 13;
               zz = z + 5 + a;
               dir = Direction.EAST;
            }

            if (cell.west) {
               xx = x + 3;
               zz = z + 5 + a;
               dir = Direction.WEST;
            }

            switch (PAT_DOORWAY[a][b]) {
               case 1 -> {
                  GenCommon.placeBlock(level, xx, y + 2 + b, zz, GenCommon.DOOR_LOCK, cell);
                  if (level.getBlockEntity(new net.minecraft.core.BlockPos(xx, y + 2 + b, zz))
                     instanceof EldritchLockBlockEntity lock && dir != null) {
                     // В 1.7.10 сюда писался порядковый номер ForgeDirection (UNKNOWN = 6)
                     lock.setFacing((byte)dir.ordinal());
                  }
               }
               case 2 -> GenCommon.placeBlock(level, xx, y + 2 + b, zz, GenCommon.DOOR_BLOCK, cell);
               case 9 -> GenCommon.placeBlock(level, xx, y + 2 + b, zz, GenCommon.VOID_DOOR, cell);
               default -> {
               }
            }
         }
      }
   }
}