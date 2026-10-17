package thaumcraft.world.features;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.block.airy.aura_node.AuraNodeBlockEntity;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModBiomes;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.world.biomes.BiomeHandler;

/** Общие помощники мировой генерации (порт идей 1.7.10). */
public final class ThaumcraftWorldgen {
   private ThaumcraftWorldgen() {
   }

   public static boolean isSoil(BlockState state) {
      return state.is(BlockTags.DIRT) || state.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK);
   }

   /** Ставит простое дерево: ствол (1x1 или 2x2) и округлая крона. */
   public static boolean placeTree(
      WorldGenLevel level, BlockPos pos, RandomSource random,
      Block log, Block leaves, int minHeight, int maxHeight, int trunkRadius, int canopyRadius
   ) {
      if (!isSoil(level.getBlockState(pos.below()))) {
         return false;
      }

      for (int i = 0; i < 8; i++) {
         if (!level.isEmptyBlock(pos.above(i))) {
            return false;
         }
      }

      int height = minHeight + random.nextInt(Math.max(1, maxHeight - minHeight + 1));
      for (int y = 0; y < height; y++) {
         for (int dx = 0; dx <= trunkRadius; dx++) {
            for (int dz = 0; dz <= trunkRadius; dz++) {
               if (dx * dx + dz * dz <= trunkRadius * trunkRadius) {
                  level.setBlock(pos.offset(dx, y, dz), log.defaultBlockState(), 3);
               }
            }
         }
      }

      int canopyY = height - 2;
      int radius = canopyRadius;
      for (int dy = -radius; dy <= radius; dy++) {
         for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
               double distance = Math.sqrt(dx * dx + dz * dz + dy * dy * 1.6);
               if (distance > radius + 0.4) {
                  continue;
               }

               BlockPos leafPos = pos.offset(dx, canopyY + dy, dz);
               if (level.isEmptyBlock(leafPos) && random.nextInt(4) != 0) {
                  level.setBlock(leafPos, leaves.defaultBlockState(), 3);
               }
            }
         }
      }

      return true;
   }

   /** Узел ауры со случайными аспектами/типом/модификатором. */
   public static boolean placeNode(WorldGenLevel level, BlockPos pos, RandomSource random) {
      if (!level.isEmptyBlock(pos)) {
         return false;
      }

      level.setBlock(pos, ModBlocks.AURA_NODE.get().defaultBlockState(), 3);
      initNode(level, pos, random, false, false);
      return true;
   }

   /**
    * Полный порт {@code ThaumcraftWorldGenerator.createRandomNodeAt} (1.7.10, строки
    * 219-402). Определяет, что именно лежит внутри узла ауры.
    *
    * <p>Отличия от прежнего {@code initNode} (27 строк, придумано): запас по биому,
    * аспект самого биома, составные аспекты, добавки по типу узла, замер окружения
    * 11×11×11, взвешенное распределение, бросок типа 1/{@code specialNodeRarity} с
    * HUNGRY/PURE/TAINTED, бросок модификатора 1/{@code specialNodeRarity/2} с FADING,
    * ветка тайнового биома.
    *
    * <p>{@code silverwood} — узел серебряного дерева (всегда PURE, вис /4).
     * {@code eerie} — узел элдрич-кольца (всегда DARK, вис НЕ уменьшается).
     *
     * <p>В оригинале есть ещё параметр {@code small} («маленький узел», вис /4),
     * но ни один его вызывающий в 1.7.10 не передаёт — здесь его нет.
     */
   public static void initNode(
      WorldGenLevel level, BlockPos pos, RandomSource random, boolean silverwood, boolean eerie
   ) {
      if (!(level.getBlockEntity(pos) instanceof AuraNodeBlockEntity node)) {
         return;
      }

      // Кэш primals/compound нужен, чтобы не пересобирать списки на каждый узел.
      List<Aspect> basic = Aspect.getPrimalAspects();
      List<Aspect> compound = Aspect.getCompoundAspects();

      if (basic.isEmpty() || compound.isEmpty()) {
         // Аспекты ещё не зарегистрированы (мир генерируется в ранней загрузке) —
         // узел останется пустым, как и в оригинале при пустом реестре.
         return;
      }

      // --- тип узла (1.7.10:230-255) ---
      NodeType type = NodeType.NORMAL;

      if (silverwood) {
         type = NodeType.PURE;
      } else if (eerie) {
         type = NodeType.DARK;
      } else if (random.nextInt(Config.specialNodeRarity) == 0) {
         type = switch (random.nextInt(10)) {
            case 0, 1, 2 -> NodeType.DARK;
            case 3, 4, 5 -> NodeType.UNSTABLE;
            case 6, 7, 8 -> NodeType.PURE;
            default -> NodeType.HUNGRY;
         };
      }

      // --- модификатор (1.7.10:257-269) ---
      NodeModifier modifier = null;

      if (random.nextInt(Math.max(1, Config.specialNodeRarity / 2)) == 0) {
         modifier = switch (random.nextInt(3)) {
            case 0 -> NodeModifier.BRIGHT;
            case 1 -> NodeModifier.PALE;
            default -> NodeModifier.FADING;
         };
      }

      // --- запас по биому (1.7.10:271-285) ---
      int baura = BiomeHandler.getBiomeAura(level.getBiome(pos), pos);
      boolean taintBiome = level.getBiome(pos).is(ModBiomes.TAINT);

      if (type != NodeType.PURE && taintBiome) {
         baura = (int)(baura * 1.5F);

         if (random.nextBoolean()) {
            type = NodeType.TAINTED;
            baura = (int)(baura * 1.5F);
         }
      }

      // 1.7.10:281 — делит только серебряное дерево и «маленький» узел.
      // eerie (элдрич-кольцо) запас НЕ уменьшает.
      if (silverwood) {
         baura /= 4;
      }

      // value = rand(baura/2) + baura/2 — итоговый вис, распределяемый по аспектам.
      int half = Math.max(1, baura / 2);
      int value = random.nextInt(half) + half;

      // --- стартовые аспекты (1.7.10:286-307) ---
      AspectList al = new AspectList();
      Aspect biomeTag = BiomeHandler.getRandomBiomeTag(level.getBiome(pos), pos, random);

      if (biomeTag != null) {
         al.add(biomeTag, 2);
      } else {
         al.add(compound.get(random.nextInt(compound.size())), 1);
         al.add(basic.get(random.nextInt(basic.size())), 1);
      }

      for (int i = 0; i < 3; i++) {
         if (random.nextBoolean()) {
            if (random.nextInt(Config.specialNodeRarity) == 0) {
               al.merge(compound.get(random.nextInt(compound.size())), 1);
            } else {
               al.merge(basic.get(random.nextInt(basic.size())), 1);
            }
         }
      }

      // --- добавки по типу (1.7.10:309-336) ---
      if (type == NodeType.HUNGRY) {
         al.merge(Aspect.HUNGER, 2);

         if (random.nextBoolean()) {
            al.merge(Aspect.GREED, 1);
         }
      } else if (type == NodeType.PURE) {
         al.merge(random.nextBoolean() ? Aspect.LIFE : Aspect.ORDER, 2);
      } else if (type == NodeType.DARK) {
         if (random.nextBoolean()) {
            al.merge(Aspect.DEATH, 1);
         }

         if (random.nextBoolean()) {
            al.merge(Aspect.UNDEAD, 1);
         }

         if (random.nextBoolean()) {
            al.merge(Aspect.ENTROPY, 1);
         }

         if (random.nextBoolean()) {
            al.merge(Aspect.DARKNESS, 1);
         }
      }

      // --- замер окружения 11×11×11 (1.7.10:338-383) ---
      sampleEnvironment(level, pos, al);

      // --- взвешенное распределение (1.7.10:385-400) ---
      Aspect[] sorted = al.getAspectsSorted();
      int[] spread = new int[sorted.length];
      float total = 0.0F;

      for (int i = 0; i < spread.length; i++) {
         // Аспекты с amount == 2 (аспект биома, добавки по типу) весят больше.
         spread[i] = al.getAmount(sorted[i]) == 2 ? 50 + random.nextInt(25) : 25 + random.nextInt(50);
         total += spread[i];
      }

      for (int i = 0; i < spread.length; i++) {
         al.merge(sorted[i], (int)((float)spread[i] / total * (float)value));
      }

      node.setAspects(al);
      node.setNodeType(type);
      node.setNodeModifier(modifier);
      node.setChanged();
   }

   /**
    * Обход куба 11×11×11 вокруг узла (1.7.10:343-383). В оригинале обход обёрнут в
    * {@code try/catch} и глушит исключения — здесь ошибок быть неоткуда, но пустой
    * бокс мира (y вне [-64, 320]) мы пропускаем так же, как {@code IndexOutOfBounds}
    * в 1.7.10.
    */
   private static void sampleEnvironment(WorldGenLevel level, BlockPos pos, AspectList al) {
      int water = 0;
      int lava = 0;
      int stone = 0;
      int foliage = 0;

      for (int x = -5; x <= 5; x++) {
         for (int y = -5; y <= 5; y++) {
            for (int z = -5; z <= 5; z++) {
               BlockPos p = pos.offset(x, y, z);

               // В 1.7.10 несостоявшийся getBlock бросал исключение и его глушили, поэтому
               // за пределами мира счётчики не растут. Здесь проверяем явно.
               if (level.isOutsideBuildHeight(p) || !level.hasChunkAt(p)) {
                  continue;
               }

               BlockState bs = level.getBlockState(p);

               if (bs.getFluidState().is(net.minecraft.tags.FluidTags.WATER)) {
                  water++;
               } else if (bs.getFluidState().is(net.minecraft.tags.FluidTags.LAVA)) {
                  lava++;
               } else if (bs.is(net.minecraft.world.level.block.Blocks.STONE)) {
                  stone++;
               }

               if (bs.is(BlockTags.LEAVES)) {
                  foliage++;
               }
            }
         }
      }

      if (water > 100) {
         al.merge(Aspect.WATER, 1);
      }

      if (lava > 100) {
         al.merge(Aspect.FIRE, 1);
         al.merge(Aspect.EARTH, 1);
      }

      if (stone > 500) {
         al.merge(Aspect.EARTH, 1);
      }

      if (foliage > 100) {
         al.merge(Aspect.PLANT, 1);
      }
   }

   /** Старый вызов без флагов — обычный дикий узел. */
   public static void initNode(WorldGenLevel level, BlockPos pos, RandomSource random) {
      initNode(level, pos, random, false, false);
   }

   /**
    * Узел серебряного дерева: всегда чистый (PURE), как в
    * {@code createRandomNodeAt(..., silverwood=true)}.
    */
   public static void initSilverwoodNode(WorldGenLevel level, BlockPos pos, RandomSource random) {
      initNode(level, pos, random, true, false);
   }

   /**
    * Узел элдрич-кольца: всегда тёмный, как в
    * {@code createRandomNodeAt(..., eerie=true)}. Вызывающий код всё равно может
    * принудительно поставить {@code NodeType.DARK} — это перекрывает бросок типа.
    */
   public static void initEerieNode(WorldGenLevel level, BlockPos pos, RandomSource random) {
      initNode(level, pos, random, false, true);
   }

   /** Шиммерлиф под деревом при мирогене (аналог {@code WorldGenCustomFlowers} в оригинале). */
   public static void scatterShimmerleaf(WorldGenLevel level, BlockPos base, RandomSource random) {
      for (int i = 0; i < 4; i++) {
         BlockPos p = base.offset(random.nextInt(7) - 3, 0, random.nextInt(7) - 3);
         BlockPos ground = p;
         // Ищем землю вниз не дальше пары блоков.
         for (int d = 0; d < 3 && level.isEmptyBlock(ground); d++) {
            ground = ground.below();
         }

         if (level.isEmptyBlock(ground.above()) && level.getBlockState(ground).is(BlockTags.DIRT)) {
            level.setBlock(ground.above(), ModBlocks.SHIMMER_LEAF.get().defaultBlockState(), 3);
         }
      }
   }
}
