package thaumcraft.dimension.outer_lands.maze;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import thaumcraft.entities.brainless.permanent.PermanentItemEntity;
import thaumcraft.entities.smart.eldrich_guardian.EntityEldritchGuardian;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModEntities;
import thaumcraft.init.registry.ModItems;
import thaumcraft.util.EntityUtils;

/**
 * Комната ключа — порт {@code GenKeyRoom} (1.7.10).
 *
 * <p>Единственная ячейка с настоящей наградой: в центре на постаменте
 * лежит «Эльдрихов объект», вокруг стоят хранители (от двух на нормальной
 * сложности до четырёх на сложной, первый из них чемпион), а по углам и
 * краям — ступенчатые уступы, сужающиеся к центру.
 *
 * <p>В 1.7.10 на постаменте лежит {@code itemEldritchObject} с meta 2 —
 * Runed Tablet (см. lang: {@code item.ItemEldritchObject.2.name}). В порте
 * кладётся {@code RUNE_TABLET} ({@code RunedTabletItem}); глаз древних
 * ({@code ELDRITCH_EYE}, meta 0) здесь был ошибкой переноса.
 */
public final class GenKeyRoom {
   private GenKeyRoom() {
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

      // Стены из камня; в середине каждой стены — выемка под лестницу (палитра 19).
      for (int a = 3; a <= 13; a++) {
         for (int b = 3; b <= 13; b++) {
            for (int cxxx = 2; cxxx < 11; cxxx++) {
               if (a == 3 || a == 13 || b == 3 || b == 13) {
                  if (cxxx > 3 && cxxx < 9 && (a == 8 || b == 8)
                     || cxxx > 4 && cxxx < 8 && (a == 7 || b == 7 || a == 9 || b == 9)) {
                     if (a != 8 && b != 8 || cxxx != 6) {
                        GenCommon.placeBlock(level, x + a, y + cxxx, z + b, GenCommon.STONE_NOSPAWN, cell);
                     }
                  } else {
                     GenCommon.placeBlock(level, x + a, y + cxxx, z + b, GenCommon.ROCK, cell);
                  }
               }
            }
         }
      }

      for (int a = 2; a <= 14; a++) {
         for (int b = 2; b <= 14; b++) {
            GenCommon.placeBlock(level, x + a, y - 1, z + b, GenCommon.BEDROCK, cell);
            GenCommon.placeBlock(level, x + a, y, z + b, GenCommon.VOID, cell);
            GenCommon.placeBlock(level, x + a, y + 1, z + b, GenCommon.STONE, cell);
            GenCommon.placeBlock(level, x + a, y + 13, z + b, GenCommon.BEDROCK, cell);
            GenCommon.placeBlock(level, x + a, y + 12, z + b, GenCommon.VOID, cell);
            GenCommon.placeBlock(level, x + a, y + 11, z + b, GenCommon.STONE, cell);

            // Уступы, сходящиеся к центру: чем ближе к середине, тем выше колонна
            if (a > 1 && a < 15 && b > 1 && b < 15) {
               int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
               for (int g = 0; g < q - 1; g++) {
                  GenCommon.placeBlock(level, x + a, y + 1 + g, z + b, GenCommon.STONE, cell);
               }
            }

            if (a > 3 && a < 13 && b > 3 && b < 13) {
               int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
               for (int g = 0; g < q; g++) {
                  GenCommon.placeBlock(level, x + a, y + 11 - g, z + b, GenCommon.STONE, cell);
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

      // Постамент: blockEldritch:3 — в 1.7.10 его иконка это thaumcraft:obsidiantile,
      // тот же текстура, что у blockCosmeticSolid:1 (Obsidian Tile), поэтому в порте
      // используется единственный блок OBSIDIAN_TILE на оба случая.
      level.setBlock(new BlockPos(x + 8, y + 2, z + 8), ModBlocks.OBSIDIAN_TILE.get().defaultBlockState(), 3);

      // Ключ: в 1.7.10 EntityPermanentItem с обнулённой скоростью и поворотом
      PermanentItemEntity item = new PermanentItemEntity(
         level, x + 8.5, y + 3.5, z + 8.5, new ItemStack(ModItems.RUNE_TABLET.get())
      );
      item.setDeltaMovement(0.0, 0.0, 0.0);
      item.setYRot(0.0F);
      level.addFreshEntity(item);

      int guardians = 2 + (level.getDifficulty() == Difficulty.HARD ? 2
         : (level.getDifficulty() == Difficulty.NORMAL ? 1 : 0));
      for (int q = 0; q < guardians; q++) {
         EntityEldritchGuardian guardian = new EntityEldritchGuardian(ModEntities.ELDRITCH_GUARDIAN.get(), level);
         double i1 = x + 8.5 + (double)(1 + random.nextInt(3)) * (random.nextInt(3) - 1);
         double j1 = y + 2;
         double k1 = z + 8.5 + (double)(1 + random.nextInt(3)) * (random.nextInt(3) - 1);
         // В 1.7.10 yaw не задавался (func_70107_b = setLocationAndAngles с yaw=pitch=0),
         // поэтому оставляем нули, а finalizeSpawn повторяет func_110161_a(null).
         guardian.moveTo(i1, j1, k1, 0.0F, 0.0F);
         guardian.finalizeSpawn((ServerLevelAccessor)level, level.getCurrentDifficultyAt(guardian.blockPosition()),
            MobSpawnType.EVENT, null);
         // func_110171_b(x + 8, y + 2, z + 8, 16) — «дом» хранителя
         guardian.setHomePosition(new BlockPos(x + 8, y + 2, z + 8), 16);
         if (q == 0 && guardians >= 4) {
            EntityUtils.makeChampion(guardian, true);
         }

         level.addFreshEntity(guardian);
      }
   }
}