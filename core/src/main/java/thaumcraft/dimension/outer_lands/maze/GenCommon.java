package thaumcraft.dimension.outer_lands.maze;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.util.BlockUtils;

/**
 * Общая раскладка блоков лабиринта — порт {@code GenCommon} (1.7.10).
 *
 * <p>{@code placeBlock} повторяет оригинал из Thaumcraft 4.2.3.5 (1.7.10),
 * включая switch из 19 веток. Метаданные лестниц
 * (0..3 — поворот, 4..7 — верхняя половина) и все пороги случайности
 * перенесены как есть.
 *
 * <p>Соответствие палитровых ID блокам TC4 сверено с notes/wiki:
 * <ul>
 *   <li>{@code blockCosmeticSolid:11} → Ancient Stone ({@code ANCIENT_STONE})</li>
 *   <li>{@code blockCosmeticSolid:12} → Ancient Rock ({@code ANCIENT_ROCK})</li>
 *   <li>{@code blockCosmeticSolid:14} → Crusted Stone ({@code CRUSTED_STONE})</li>
 *   <li>{@code blockEldritch:4} → Glowing Crusted Stone ({@code GLOWING_CRUSTED_STONE})</li>
 *   <li>{@code blockEldritch:7} → Ancient Doorway ({@code ANCIENT_DOORWAY})</li>
 *   <li>{@code blockEldritchNothing} → Portal to Nothing ({@code ELDRITCH_NOTHING})</li>
 *   <li>{@code blockStairsEldritch} → Ancient Stone Stairs ({@code ELDRITCH_STONE_STAIRS})</li>
 *   <li>{@code blockAiry} → Aura Node ({@code AURA_NODE})</li>
 * </ul>
 *
 * <p>Имена констант оригинала подтверждают сводку: {@code STONE} ставит
 * meta 11 (Ancient Stone), {@code ROCK} — meta 12 (Ancient Rock),
 * {@code CRUST} — meta 14 (Crusted Stone).
 */
public final class GenCommon {
   /** Отметки позиций, ожидающих декорации; обрабатываются в {@link #processDecorations}. */
   public static final List<BlockPos> DECO_COMMON = new ArrayList<>();
   public static final List<BlockPos> CRAB_SPAWNER = new ArrayList<>();
   public static final List<BlockPos> DECO_URN = new ArrayList<>();

   /** Константы палитры — как в оригинале (GenCommon). */
   public static final int BEDROCK = 1;
   public static final int STONE = 2;
   public static final int VOID = 8;
   public static final int AIR_REPL = 9;
   public static final int STAIR_DIRECTIONAL = 10;
   public static final int STAIR_DIRECTIONAL_INV = 11;
   public static final int SLAB = 12;
   public static final int DOOR_BLOCK = 15;
   public static final int DOOR_LOCK = 16;
   public static final int VOID_DOOR = 17;
   public static final int ROCK = 18;
   public static final int STONE_NOSPAWN = 19;
   public static final int STONE_TRAPPED = 20;
   public static final int CRUST = 21;

   /**
    * Шаблон прохода между чанками (11×11), порт {@code PAT_CONNECT}.
    * Значения — те же ID палитры.
    */
   static final int[][] PAT_CONNECT = new int[][]{
      {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
      {1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1},
      {1, 8, 8, 2, 2, 2, 2, 2, 8, 8, 1},
      {1, 8, 2, 5, 9, 9, 9, 6, 2, 8, 1},
      {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1},
      {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1},
      {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1},
      {1, 8, 2, 3, 9, 9, 9, 4, 2, 8, 1},
      {1, 8, 8, 2, 2, 2, 2, 2, 8, 8, 1},
      {1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1},
      {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}
   };

   private GenCommon() {
   }

   public static void placeBlock(Level level, int x, int y, int z, int palette, Cell cell) {
      placeBlock(level, x, y, z, palette, Direction.NORTH, cell);
   }

   /**
    * Ставит блок по ID из палитры. Флаг обновления соседей в оригинале:
    * 0 для «невидимых» блоков (nothing/air/stone) и 3 для остальных.
    */
   public static void placeBlock(Level level, int x, int y, int z, int palette, Direction dir, Cell cell) {
      BlockState state = null;

      switch (palette) {
         case BEDROCK -> {
            // func_147437_c — только если чанк загружен, иначе блок не ставится вовсе.
            // NB: field_150357_h из 1.7.10 — это BEDROCK, не воздух: в ConfigAspects
            // ему назначен аспект VOID, а portable hole не ест его вместе с
            // blockEldritchNothing.
            if (level.isLoaded(new BlockPos(x, y, z))) {
               state = Blocks.BEDROCK.defaultBlockState();
            }
         }
         case STONE -> {
            RandomSource random = level.getRandom();
            // 1/3 гнезд (feature 7) уходят в ветку CRUST
            if (cell.feature != 7 || random.nextInt(3) != 0) {
               if (!level.getBlockState(new BlockPos(x, y, z)).is(ModBlocks.ELDRITCH_NOTHING.get())) {
                  if (random.nextInt(25) == 0) {
                     boolean wantSpawner = cell.feature == 7 || random.nextInt(50) == 0;
                     if (wantSpawner && (cell.feature == 0 || cell.feature == 7)) {
                        CRAB_SPAWNER.add(new BlockPos(x, y, z));
                     } else {
                        DECO_COMMON.add(new BlockPos(x, y, z));
                     }
                  }

                  state = ModBlocks.ANCIENT_STONE.get().defaultBlockState();
               }
            } else {
               state = crustPlacement(level, x, y, z, cell);
            }
         }
         case 3 -> {
            if (level.getRandom().nextFloat() < 0.005F) {
               DECO_URN.add(new BlockPos(x, y, z));
            }

            state = stairs(dir, metaCorner3(dir));
         }
         case 4 -> {
            if (level.getRandom().nextFloat() < 0.005F) {
               DECO_URN.add(new BlockPos(x, y, z));
            }

            state = stairs(dir, metaCorner4(dir));
         }
         case 5 -> state = stairs(dir, metaTopFacingAcross(dir));
         case 6 -> state = stairs(dir, metaTopFacingAlong(dir));
         case 7 -> state = ModBlocks.GLOWING_CRUSTED_STONE.get().defaultBlockState();
         case VOID -> state = ModBlocks.ELDRITCH_NOTHING.get().defaultBlockState();
         case AIR_REPL -> {
            // В оригинале эта палитра ставит ВОЗДУХ (проверено по байткоду:
            // case 9 -> Blocks.field_150350_a). У нас здесь стоял камень, и
            // все проходы лабиринта оказывались замурованы в камне — игрок
            // появлялся внутри блоков, ходить было негде.
            state = Blocks.AIR.defaultBlockState();
            removeFromDecoLists(x, y, z);
         }
         case STAIR_DIRECTIONAL -> state = stairs(dir, metaFacing(dir));
         case STAIR_DIRECTIONAL_INV -> state = stairs(dir, metaFacing(dir) | 4);
         case DOOR_BLOCK -> {
            state = ModBlocks.ANCIENT_DOORWAY.get().defaultBlockState();
            removeFromDecoLists(x, y, z);
         }
         case DOOR_LOCK -> {
            state = ModBlocks.ANCIENT_LOCKING_MECHANISM.get().defaultBlockState();
            removeFromDecoLists(x, y, z);
         }
         case VOID_DOOR -> state = ModBlocks.AURA_NODE.get().defaultBlockState();
         case ROCK -> {
            if (!level.getBlockState(new BlockPos(x, y, z)).is(ModBlocks.ELDRITCH_NOTHING.get())) {
               state = ModBlocks.ANCIENT_ROCK.get().defaultBlockState();
            }
         }
         case STONE_NOSPAWN -> {
            // meta 13 оригинала не описан в wiki; ближайший по смыслу — Ancient Stone
            if (!level.getBlockState(new BlockPos(x, y, z)).is(ModBlocks.ELDRITCH_NOTHING.get())) {
               state = ModBlocks.ANCIENT_STONE.get().defaultBlockState();
            }
         }
         case STONE_TRAPPED -> {
            if (!level.getBlockState(new BlockPos(x, y, z)).is(ModBlocks.ELDRITCH_NOTHING.get())) {
               state = ModBlocks.ELDRITCH_TRAP.get().defaultBlockState();
            }
         }
         case CRUST -> state = crustPlacement(level, x, y, z, cell);
         case 99 -> state = Blocks.BEDROCK.defaultBlockState();
         // default: в оригинале lookupswitch уходит сразу на хвост с block == null,
         // то есть блок просто не ставится.
         default -> {
         }
      }

      if (state != null) {
         int flags = 3;
         if (state.is(ModBlocks.ELDRITCH_NOTHING.get())
            || state.is(Blocks.BEDROCK) || state.is(Blocks.STONE)) {
            flags = 0;
         }

         level.setBlock(new BlockPos(x, y, z), state, flags);
      }
   }

   /** Ветка CRUST (палитра 2 при feature==7 и палитра 21) — порт смеси из оригинала. */
   private static BlockState crustPlacement(Level level, int x, int y, int z, Cell cell) {
      BlockPos pos = new BlockPos(x, y, z);
      if (level.getBlockState(pos).is(ModBlocks.ELDRITCH_NOTHING.get())) {
         return null;
      }

      RandomSource random = level.getRandom();
      BlockState state = ModBlocks.CRUSTED_STONE.get().defaultBlockState();
      if (random.nextInt(25) == 0) {
         // 1/25 — светящаяся корка
         return ModBlocks.GLOWING_CRUSTED_STONE.get().defaultBlockState();
      }

      if (random.nextInt(25) == 0) {
         boolean wantSpawner = cell.feature == 7
            || (cell.feature == 12 && random.nextBoolean())
            || random.nextInt(25) == 0;
         if (wantSpawner && (cell.feature == 0 || cell.feature == 7 || cell.feature == 12)) {
            CRAB_SPAWNER.add(pos);
         }
      }

      return state;
   }

   private static void removeFromDecoLists(int x, int y, int z) {
      BlockPos pos = new BlockPos(x, y, z);
      DECO_COMMON.remove(pos);
      CRAB_SPAWNER.remove(pos);
      DECO_URN.remove(pos);
   }

   // ---- метаданные лестниц TC4: 0..3 поворот (east/west/south/north), бит 2 — верхняя половина

   private static Direction facingOf(int meta) {
      return switch (meta & 3) {
         case 0 -> Direction.EAST;
         case 1 -> Direction.WEST;
         case 2 -> Direction.SOUTH;
         default -> Direction.NORTH;
      };
   }

   private static BlockState stairs(Direction dir, int meta) {
      return ModBlocks.ELDRITCH_STONE_STAIRS.get().defaultBlockState()
         .setValue(net.minecraft.world.level.block.StairBlock.FACING, facingOf(meta))
         .setValue(net.minecraft.world.level.block.StairBlock.HALF, (meta & 4) != 0 ? Half.TOP : Half.BOTTOM);
   }

   /** STAIR_DIRECTIONAL: N→3, S→2, E→0, W→1 (поворот «наружу»). */
   private static int metaFacing(Direction dir) {
      return switch (dir) {
         case NORTH -> 3;
         case SOUTH -> 2;
         case EAST -> 0;
         case WEST -> 1;
         default -> 0;
      };
   }

   /** Палитра 3: север/юг→1, запад/восток→3. */
   private static int metaCorner3(Direction dir) {
      return switch (dir) {
         case NORTH, SOUTH -> 1;
         case WEST, EAST -> 3;
         default -> 0;
      };
   }

   /** Палитра 4: север/юг→0, запад/восток→2. */
   private static int metaCorner4(Direction dir) {
      return switch (dir) {
         case NORTH, SOUTH -> 0;
         case WEST, EAST -> 2;
         default -> 0;
      };
   }

   /** Палитра 5: север/юг→5, запад/восток→7. */
   private static int metaTopFacingAcross(Direction dir) {
      return switch (dir) {
         case NORTH, SOUTH -> 5;
         case WEST, EAST -> 7;
         default -> 0;
      };
   }

   /** Палитра 6: север/юг→4, запад/восток→6. */
   private static int metaTopFacingAlong(Direction dir) {
      return switch (dir) {
         case NORTH, SOUTH -> 4;
         case WEST, EAST -> 6;
         default -> 0;
      };
   }

   /**
    * Обелиск — порт {@code genObelisk} (1.7.10, строки 518-524).
    *
    * <p>В оригинале это один блок {@code blockEldritch} с метаданными 1 и
    * четыре блока с метаданными 2: рендер обелиска рисует всю
    * пятиметровую конструкцию от нижнего блока (у тайла
    * {@code TileEldritchObelisk} габарит {@code y..y+5}), а блоки с
    * метаданными 2 — тело для света и разрушения.
    *
    * <p>Раньше здесь ставилось пять блоков {@code ELDRITCH_OBELISK}, и каждый
    * из них рендерил всю конструкцию целиком: в боссовых залах голема и
    * Смотрителя обелиски накладывались друг на друга в пять слоёв, и пять
    * тайлов вместо одного. Те же пять блоков ставит {@code EldritchRingFeature},
    * там уже было сделано правильно — 1 обелиск и 4 части.
    */
   public static void genObelisk(Level level, int x, int y, int z) {
      level.setBlock(new BlockPos(x, y, z), ModBlocks.ELDRITCH_OBELISK.get().defaultBlockState(), 3);
      for (int i = 1; i <= 4; i++) {
         level.setBlock(new BlockPos(x, y + i, z),
            ModBlocks.ELDRITCH_OBELISK_PART.get().defaultBlockState(), 3);
      }
   }

   /**
    * Стыковка прохода между соседними чанками — порт {@code generateConnections}.
    * Кладёт «воронку» шириной {@code depth} по направлению, открытому в {@code cell}.
    *
    * @param justthetip сузить воронку до узкого горлышка (используется боссовыми залами)
    */
   public static void generateConnections(
      Level level, RandomSource random, int cx, int cz, int y, Cell cell, int depth, boolean justthetip
   ) {
      int x = cx * 16;
      int z = cz * 16;
      // Ширина воронки: у самого края (d == depth) берётся только горлышко 2..9
      // либо 1..10 на предпоследнем слое, иначе — полный пролёт 0..11.
      for (int d = 0; d <= depth; d++) {
         boolean tip = d == depth && justthetip;
         boolean nearTip = d == depth - 1 && justthetip;
         int wFrom = tip ? 2 : (nearTip ? 1 : 0);
         int wTo = tip ? 9 : (nearTip ? 10 : 11);
         int hFrom = tip ? 2 : (nearTip ? 1 : 0);
         int hTo = tip ? 9 : (nearTip ? 10 : 11);

         for (int w = wFrom; w < wTo; w++) {
            for (int h = hFrom; h < hTo; h++) {
               if (d != depth || !justthetip || PAT_CONNECT[h][w] != VOID) {
                  if (cell.north) {
                     placeBlock(level, x + 3 + w, y + 10 - h, z + d, PAT_CONNECT[h][w], Direction.NORTH, cell);
                  }

                  if (cell.south) {
                     placeBlock(level, x + 3 + w, y + 10 - h, z + 16 - d, PAT_CONNECT[h][w], Direction.SOUTH, cell);
                  }

                  if (cell.east) {
                     placeBlock(level, x + 16 - d, y + 10 - h, z + 3 + w, PAT_CONNECT[h][w], Direction.EAST, cell);
                  }

                  if (cell.west) {
                     placeBlock(level, x + d, y + 10 - h, z + 3 + w, PAT_CONNECT[h][w], Direction.WEST, cell);
                  }
               }
            }
         }
      }
   }
   /**
    * Порт {@code isBedrockShowing}: видно ли «обнажённый» бедрок — блок, у которого
    * хотя бы с одной стороны нет непрозрачного соседа, а напротив стоит бедрок
    * либо «ничего». Такие места TC2 скрывает заглушкой {@code blockEldritchNothing}.
    */
   public static boolean isBedrockShowing(Level level, int x, int y, int z) {
      for (Direction dir : Direction.values()) {
         BlockPos near = new BlockPos(x, y, z).relative(dir);
         BlockPos far = new BlockPos(x, y, z).relative(dir.getOpposite());
         if (!level.getBlockState(near).isSolidRender(level, near)
            && (level.getBlockState(far).is(Blocks.BEDROCK)
            || level.getBlockState(far).is(ModBlocks.ELDRITCH_NOTHING.get()))) {
            return true;
         }
      }

      return false;
   }

   /** Принадлежит ли блок семейству {@code blockEldritch} оригинала. */
   private static boolean isEldritchFamily(BlockState state) {
      return state.is(ModBlocks.GLOWING_CRUSTED_STONE.get())
         || state.is(ModBlocks.GLYPHED_STONE.get())
         || state.is(ModBlocks.ANCIENT_DOORWAY.get())
         || state.is(ModBlocks.ANCIENT_LOCKING_MECHANISM.get())
         || state.is(ModBlocks.ELDRITCH_CRAB_SPAWNER.get())
         || state.is(ModBlocks.ELDRITCH_TRAP.get());
   }

   /**
    * Есть ли рядом (в радиусе 1) хотя бы один блок семейства {@code blockEldritch}.
    * В оригинале это {@code isBlockAdjacentToAtleast(world, x, y, z,
    * blockEldritch, 32767, 1)}, где 32767 — любая meta.
    */
   private static boolean isAdjacentToEldritch(Level level, BlockPos pos) {
      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (dx != 0 || dy != 0 || dz != 0) {
                  BlockPos check = pos.offset(dx, dy, dz);
                  if (isEldritchFamily(level.getBlockState(check))) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   /**
    * Расстановка декораций после генерации ячейки — порт {@code processDecorations}.
    * Работает по трём спискам отметок, которые накапливает {@link #placeBlock},
    * и в конце очищает их.
    *
    * <p>Соответствия палитр:
    * <ul>
    *   <li>пьедестал — {@code blockCosmeticSolid:15}</li>
    *   <li>сундук добычи — {@code blockLootUrn} meta 0/1/2</li>
    *   <li>{@code blockEldritch} meta 4/5/10 — светящаяся корка, глифный камень, ловушка</li>
    *   <li>{@code blockEldritch} meta 9 — спавнер крабов (у нас направление задаётся
    *       сразу свойством состояния FACING, а не полем у блок-энтити)</li>
    *   <li>{@code blockCrystal:7} — Strange Crystals с ориентацией по направлению</li>
    * </ul>
    */
   public static void processDecorations(Level level) {
      RandomSource random = level.getRandom();

      for (BlockPos pos : DECO_URN) {
         BlockPos above = pos.above();
         if (level.isLoaded(above)) {
            level.setBlock(pos, ModBlocks.ANCIENT_STONE_PEDESTAL.get().defaultBlockState(), 3);
            float rr = random.nextFloat();
            int meta = rr < 0.025F ? 2 : (rr < 0.1F ? 1 : 0);
            level.setBlock(above, lootUrnState(meta), 3);
         }
      }

      for (BlockPos pos : DECO_COMMON) {
         int exp = BlockUtils.countExposedSides(level, pos);
         if (exp > 0 && (exp == 1 || !isBedrockShowing(level, pos.getX(), pos.getY(), pos.getZ()))
            && !isAdjacentToEldritch(level, pos)) {
            int meta = random.nextInt(3) != 0 ? 4 : (random.nextInt(8) != 0 ? 5 : 10);
            level.setBlock(pos, eldritchState(meta), 3);
            if (meta == 4 && random.nextInt(12) == 0) {
               for (Direction dir : Direction.values()) {
                  BlockPos near = pos.relative(dir);
                  // В оригинале проверяется именно ВОЗДУХ (func_147437_c):
                  // кристалл крепится к блоку украшения и растёт наружу. У нас
                  // стояла проверка «чанк загружен», и кристалл мог оказаться
                  // внутри стены или потолка — выглядело как висящий в воздухе.
                  if (level.isLoaded(near) && level.getBlockState(near).isAir()) {
                     level.setBlock(near, strangeCrystal(dir), 3);
                     break;
                  }
               }
            }
         }
      }

      for (BlockPos pos : CRAB_SPAWNER) {
         int exp = BlockUtils.countExposedSides(level, pos);
         if (exp == 1 && !isAdjacentToEldritch(level, pos)) {
            // В оригинале сначала ставится блок без ориентации, а затем у уже
            // созданного TileEldritchCrabSpawner выставляется setFacing по первой
            // же загруженной соседней клетке. У нас ориентация — свойство
            // состояния, поэтому направление ищется до установки блока.
            for (Direction dir : Direction.values()) {
               BlockPos near = pos.relative(dir);
               if (level.isLoaded(near)) {
                  level.setBlock(
                     pos, ModBlocks.ELDRITCH_CRAB_SPAWNER.get().defaultBlockState()
                        .setValue(
                           thaumcraft.block.eldritch.crab_spawner.BlockEldritchCrabSpawner.FACING, dir
                        ),
                     3
                  );
                  break;
               }
            }
         }
      }

      DECO_COMMON.clear();
      CRAB_SPAWNER.clear();
      DECO_URN.clear();
   }

   /**
    * Урна добычи по meta оригинала (0 — обычная, 1 — необычная, 2 — редкая).
    * Публичный: тем же набором пользуются генераторы боссовых залов.
    */
   public static Block lootUrn(int meta) {
      return lootUrnState(meta).getBlock();
   }

   /** Сундук добычи по meta оригинала (0 — обычный, 1 — необычный, 2 — редкий). */
   private static BlockState lootUrnState(int meta) {
      return switch (meta) {
         case 2 -> ModBlocks.LOOT_URN_RARE.get().defaultBlockState();
         case 1 -> ModBlocks.LOOT_URN_UNCOMMON.get().defaultBlockState();
         default -> ModBlocks.LOOT_URN.get().defaultBlockState();
      };
   }

   /** Блок семейства {@code blockEldritch} по meta оригинала. */
   private static BlockState eldritchState(int meta) {
      return switch (meta) {
         case 4 -> ModBlocks.GLOWING_CRUSTED_STONE.get().defaultBlockState();
         case 5 -> ModBlocks.GLYPHED_STONE.get().defaultBlockState();
         case 10 -> ModBlocks.ELDRITCH_TRAP.get().defaultBlockState();
         default -> ModBlocks.GLOWING_CRUSTED_STONE.get().defaultBlockState();
      };
   }

   /** Strange Crystals с ориентацией в сторону {@code dir}. */
   private static BlockState strangeCrystal(Direction dir) {
      return ModBlocks.STRANGE_CRYSTALS.get().defaultBlockState()
         .setValue(thaumcraft.block.crystal.CrystalClusterBlock.FACING, dir);
   }
}
