package thaumcraft.dimension.outer_lands.maze;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.init.registry.ModBlocks;

/**
 * Гнездо — порт {@code GenNestRoom} (1.7.10).
 *
 * <p>Комната целиком зарастает Crusted Stone: стены, пол, потолок и два «сталактита»
 * из корки, свисающих с потолка. В среднем по высоте вместо корки с вероятностью
 * 1/2 ставится кристалл {@code blockCrystal:7} (Strange Crystals) с ориентацией
 * вниз — так визуально отличается зал от остальных.
 *
 * <p>В оригинале ориентация задаётся полем {@code TileCrystal.orientation}
 * (порядковый номер ForgeDirection). У нас {@code CrystalClusterBlock} — это
 * {@code DirectionalBlock}, поэтому то же самое выражается свойством состояния
 * {@code FACING} без отдельного блок-энтити.
 */
public final class GenNestRoom {
   private GenNestRoom() {
   }

   /** Порт {@code generateRoom}. */
   public static void generateRoom(Level level, RandomSource random, int cx, int cz, int y, Cell cell) {
      int x = cx * 16;
      int z = cz * 16;

      for (int a = 1; a <= 15; a++) {
         for (int b = 1; b <= 15; b++) {
            for (int c = 0; c < 11; c++) {
               if (a == 1 || a == 15 || b == 1 || b == 15) {
                  GenCommon.placeBlock(level, x + a, y + c, z + b, GenCommon.BEDROCK, cell);
               }
            }
         }
      }

      for (int a = 2; a <= 14; a++) {
         for (int b = 2; b <= 14; b++) {
            for (int cxx = 1; cxx < 10; cxx++) {
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
            for (int cxxx = 2; cxxx < 9; cxxx++) {
               if (a == 3 || a == 13 || b == 3 || b == 13) {
                  GenCommon.placeBlock(level, x + a, y + cxxx, z + b, GenCommon.CRUST, cell);
               }

               // Дополнительный нарост корки напротив открытой стороны
               if ((a == 4 && !cell.west || a == 12 && !cell.east
                  || b == 4 && !cell.north || b == 12 && !cell.south) && random.nextBoolean()) {
                  GenCommon.placeBlock(level, x + a, y + cxxx, z + b, GenCommon.CRUST, cell);
               }
            }
         }
      }

      for (int a = 2; a <= 14; a++) {
         for (int b = 2; b <= 14; b++) {
            GenCommon.placeBlock(level, x + a, y - 1, z + b, GenCommon.BEDROCK, cell);
            GenCommon.placeBlock(level, x + a, y, z + b, GenCommon.VOID, cell);
            GenCommon.placeBlock(level, x + a, y + 1, z + b, GenCommon.CRUST, cell);
            GenCommon.placeBlock(level, x + a, y + 11, z + b, GenCommon.BEDROCK, cell);
            GenCommon.placeBlock(level, x + a, y + 10, z + b, GenCommon.VOID, cell);
            GenCommon.placeBlock(level, x + a, y + 9, z + b, GenCommon.CRUST, cell);

            if (random.nextBoolean()) {
               GenCommon.placeBlock(level, x + a, y + 8, z + b, GenCommon.CRUST, cell);
            } else if (random.nextBoolean()) {
               BlockPos p = new BlockPos(x + a, y + 8, z + b);
               if (level.isLoaded(p)) {
                  // blockCrystal meta 7 = Strange Crystals, orientation = DOWN
                  BlockState crystal = ModBlocks.STRANGE_CRYSTALS.get().defaultBlockState()
                     .setValue(thaumcraft.block.crystal.CrystalClusterBlock.FACING, Direction.DOWN);
                  level.setBlock(p, crystal, 3);
               }
            }
         }
      }

      // Нижний сталактит у пола
      GenCommon.placeBlock(level, x + 8, y + 2, z + 8, GenCommon.CRUST, cell);
      GenCommon.placeBlock(level, x + 8, y + 3, z + 8, GenCommon.CRUST, cell);
      GenCommon.placeBlock(level, x + 8, y + 4, z + 8, GenCommon.CRUST, cell);
      GenCommon.placeBlock(level, x + 7, y + 2, z + 8, GenCommon.CRUST, cell);
      GenCommon.placeBlock(level, x + 8, y + 2, z + 7, GenCommon.CRUST, cell);
      GenCommon.placeBlock(level, x + 9, y + 2, z + 8, GenCommon.CRUST, cell);
      GenCommon.placeBlock(level, x + 8, y + 2, z + 9, GenCommon.CRUST, cell);
      if (random.nextBoolean()) {
         GenCommon.placeBlock(level, x + 7, y + 3, z + 8, GenCommon.CRUST, cell);
      }

      if (random.nextBoolean()) {
         GenCommon.placeBlock(level, x + 8, y + 3, z + 7, GenCommon.CRUST, cell);
      }

      if (random.nextBoolean()) {
         GenCommon.placeBlock(level, x + 9, y + 3, z + 8, GenCommon.CRUST, cell);
      }

      if (random.nextBoolean()) {
         GenCommon.placeBlock(level, x + 8, y + 3, z + 9, GenCommon.CRUST, cell);
      }

      if (random.nextBoolean()) {
         GenCommon.placeBlock(level, x + 8, y + 5, z + 8, 7, cell);
      }

      // Верхний сталактит у потолка
      GenCommon.placeBlock(level, x + 8, y + 8, z + 8, GenCommon.CRUST, cell);
      GenCommon.placeBlock(level, x + 8, y + 7, z + 8, GenCommon.CRUST, cell);
      GenCommon.placeBlock(level, x + 8, y + 6, z + 8, GenCommon.CRUST, cell);
      GenCommon.placeBlock(level, x + 7, y + 8, z + 8, GenCommon.CRUST, cell);
      GenCommon.placeBlock(level, x + 8, y + 8, z + 7, GenCommon.CRUST, cell);
      GenCommon.placeBlock(level, x + 9, y + 8, z + 8, GenCommon.CRUST, cell);
      GenCommon.placeBlock(level, x + 8, y + 8, z + 9, GenCommon.CRUST, cell);
      if (random.nextBoolean()) {
         GenCommon.placeBlock(level, x + 7, y + 7, z + 8, GenCommon.CRUST, cell);
      }

      if (random.nextBoolean()) {
         GenCommon.placeBlock(level, x + 8, y + 7, z + 7, GenCommon.CRUST, cell);
      }

      if (random.nextBoolean()) {
         GenCommon.placeBlock(level, x + 9, y + 7, z + 8, GenCommon.CRUST, cell);
      }

      if (random.nextBoolean()) {
         GenCommon.placeBlock(level, x + 8, y + 7, z + 9, GenCommon.CRUST, cell);
      }

      GenCommon.generateConnections(level, random, cx, cz, y, cell, 3, true);

      // Добыча: сундук или urn; мета 0 — обычный, 1 — необычный, 2 — редкий
      for (int a = -5; a <= 5; a++) {
         for (int bx = -5; bx <= 5; bx++) {
            // В оригинале лут ставится только в ВОЗДУХ (func_147437_c), иначе
            // горшки появлялись внутри стен и корки.
            if (random.nextFloat() < 0.15F) {
               BlockPos p = new BlockPos(x + 8 + a, y + 2, z + 8 + bx);
               if (level.isLoaded(p) && level.getBlockState(p).isAir()) {
                  float rr = random.nextFloat();
                  int md = rr < 0.15F ? 2 : (rr < 0.4F ? 1 : 0);
                  var loot = random.nextFloat() < 0.2F
                     ? ModBlocks.LOOT_CRATE.get().defaultBlockState()
                     : (md == 2 ? ModBlocks.LOOT_URN_RARE.get().defaultBlockState()
                        : (md == 1 ? ModBlocks.LOOT_URN_UNCOMMON.get().defaultBlockState()
                           : ModBlocks.LOOT_URN.get().defaultBlockState()));
                  level.setBlock(p, loot, 3);
               }
            }
         }
      }
   }
}