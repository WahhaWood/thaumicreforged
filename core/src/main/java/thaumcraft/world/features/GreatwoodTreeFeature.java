package thaumcraft.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.world.biomes.BiomeHandler;

/**
 * Великое дерево 1:1 с {@code WorldGenGreatwoodTrees} (1.7.10) — алгоритм
 * ванильного большого дерева с параметрами TC4.
 *
 * <p>Что было не так: ствол 2x2 высотой 9–14 с кроной-сферой — куст, а не
 * дерево. В TC4 высота 11–21 и дерево строится в ДВА прохода (второй стартует с
 * {@code y + height}), итог ~30+ блоков, крона ~20 в ширину, ~500 брёвен.
 * Плюс паучья версия (1/8): спавнер пещерных пауков, сундук и паутина в кроне.
 */
public class GreatwoodTreeFeature extends Feature<NoneFeatureConfiguration> {
   // Параметры TC4 (поля WorldGenGreatwoodTrees).
   private static final double HEIGHT_ATTENUATION = 0.618;
   private static final double BRANCH_SLOPE = 0.38;
   private static final double LEAF_DENSITY = 0.9;
   private static final int HEIGHT_LIMIT_LIMIT = 11;
   private static final int LEAF_DISTANCE_LIMIT = 4;
   private static final byte[] OTHER_COORD_PAIRS = {2, 0, 0, 1, 2, 1};

   public GreatwoodTreeFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      // Гейт биома 1:1 с TC4: шанс 1/120 (пользователь просил очень редко;
      // в TC4 здесь 1/25) уже отсеян rarity-фильтром, затем таблица поддержки
      // BiomeHandler (лес/магический 1.0, пышный 0.5, хвойный/саванна/равнина/
      // болото 0.2, остальное 0).
      if (!(BiomeHandler.getBiomeSupportsGreatwood(context.level().getBiome(context.origin()), context.origin())
         > context.random().nextFloat())) {
         return false;
      }

      // Паучья версия — 1/8, как func_76484_a в оригинале.
      return grow(context.level(), context.origin(), context.random(),
         context.random().nextInt(8) == 0);
   }

   /** Вырастить великое дерево (саженцы зовут с {@code spiders=false}). */
   public static boolean grow(WorldGenLevel level, BlockPos origin, RandomSource random, boolean spiders) {
      return new Grower(level, origin, random, spiders).generate();
   }

   /** Состояние одного дерева — поля-близнецы оригинала. */
   private static final class Grower {
      private final WorldGenLevel level;
      private final RandomSource random;
      private final boolean spiders;
      private final BlockPos origin;
      private int heightLimit;
      private int height;
      private double scaleWidth = 1.2;
      private int baseX;
      private int baseY;
      private int baseZ;
      private int[][] leafNodes = new int[0][4];

      Grower(WorldGenLevel level, BlockPos origin, RandomSource random, boolean spiders) {
         this.level = level;
         this.origin = origin;
         this.random = random;
         this.spiders = spiders;
         this.heightLimit = HEIGHT_LIMIT_LIMIT + random.nextInt(HEIGHT_LIMIT_LIMIT);
         this.baseX = origin.getX();
         this.baseY = origin.getY();
         this.baseZ = origin.getZ();
      }

      boolean generate() {
         // Поиск опоры 2x2 среди соседних 3x3 (как в оригинале).
         boolean valid = false;
         for (int a = -1; a < 2 && !valid; a++) {
            for (int b = -1; b < 2; b++) {
               boolean ok = true;
               for (int dx = 0; dx < 2 && ok; dx++) {
                  for (int dz = 0; dz < 2 && ok; dz++) {
                     if (!this.validTreeLocation(dx + a, dz + b)) {
                        ok = false;
                     }
                  }
               }

               if (ok) {
                  valid = true;
                  this.baseX += a;
                  this.baseZ += b;
                  break;
               }
            }
         }

         if (!valid) {
            return false;
         }

         this.generateLeafNodeList();
         this.generateLeaves();
         this.generateLeafNodeBases();
         this.generateTrunk();

         // Второй проход: продолжение вверх от y + height, крона шире.
         this.scaleWidth = 1.66;
         this.baseX = this.origin.getX();
         this.baseY = this.origin.getY() + this.height;
         this.baseZ = this.origin.getZ();
         this.generateLeafNodeList();
         this.generateLeaves();
         this.generateLeafNodeBases();
         this.generateTrunk();

         if (this.spiders) {
            this.generateSpiderNest();
         }

         return true;
      }

      /** Паучье гнездо: спавнер, сундук и до 50 паутин в кроне. */
      private void generateSpiderNest() {
         int x = this.origin.getX();
         int y = this.origin.getY();
         int z = this.origin.getZ();
         this.level.setBlock(new BlockPos(x, y - 1, z), Blocks.SPAWNER.defaultBlockState(), 3);
         if (this.level.getBlockEntity(new BlockPos(x, y - 1, z)) instanceof SpawnerBlockEntity spawner) {
            // Именно setEntityId, а не load(NBT): у спавнера в 1.21.1 список
            // сущностей лежит под ключом SpawnData как вложенные записи, и плоский
            // рукописный тег не читается — спавнер оставался пустой клеткой.
            // Двухаргументная перегрузка на самом BE не требует Level, поэтому
            // WorldGenLevel подходит (тот же приём в GenPassage).
            spawner.setEntityId(net.minecraft.world.entity.EntityType.CAVE_SPIDER, this.random);
         }

         for (int a = 0; a < 50; a++) {
            BlockPos p = new BlockPos(x - 7 + this.random.nextInt(14),
               y + this.random.nextInt(10), z - 7 + this.random.nextInt(14));
            if (this.level.isEmptyBlock(p) && this.touchesWood(p)) {
               this.level.setBlock(p, Blocks.COBWEB.defaultBlockState(), 3);
            }
         }

         StructureGen.placeChest(this.level, new BlockPos(x, y - 2, z), this.random,
            StructureGen.dungeonLoot());
      }

      private boolean touchesWood(BlockPos p) {
         for (Direction dir : Direction.values()) {
            BlockState state = this.level.getBlockState(p.relative(dir));
            if (state.is(ModBlocks.GREATWOOD_LOG.get()) || state.is(ModBlocks.GREATWOOD_LEAVES.get())) {
               return true;
            }
         }

         return false;
      }

      private void generateLeafNodeList() {
         this.height = (int)(this.heightLimit * HEIGHT_ATTENUATION);
         if (this.height >= this.heightLimit) {
            this.height = this.heightLimit - 1;
         }

         int count = (int)(1.382 + Math.pow(LEAF_DENSITY * this.heightLimit / 13.0, 2.0));
         if (count < 1) {
            count = 1;
         }

         int[][] nodes = new int[count * this.heightLimit][4];
         int topY = this.baseY + this.heightLimit - LEAF_DISTANCE_LIMIT;
         int index = 1;
         int trunkTop = this.baseY + this.height;
         int dy = topY - this.baseY;
         nodes[0][0] = this.baseX;
         nodes[0][1] = topY;
         nodes[0][2] = this.baseZ;
         nodes[0][3] = trunkTop;
         topY--;

         while (dy >= 0) {
            float size = this.layerSize(dy);
            if (size < 0.0F) {
               topY--;
               dy--;
            } else {
               for (int i = 0; i < count; i++) {
                  double radius = 1.2 * size * (this.random.nextFloat() + 0.328);
                  double angle = this.random.nextFloat() * 2.0 * Math.PI;
                  int nx = (int)(radius * Math.sin(angle) + this.baseX + 0.5);
                  int nz = (int)(radius * Math.cos(angle) + this.baseZ + 0.5);
                  int[] from = {nx, topY, nz};
                  int[] to = {nx, topY + LEAF_DISTANCE_LIMIT, nz};
                  if (this.checkBlockLine(from, to) == -1) {
                     double dist = Math.sqrt(Math.pow(Math.abs(this.baseX - nx), 2.0)
                        + Math.pow(Math.abs(this.baseZ - nz), 2.0));
                     double branchBase = dist * BRANCH_SLOPE;
                     int base;
                     if (topY - branchBase > trunkTop) {
                        base = trunkTop;
                     } else {
                        base = (int)(topY - branchBase);
                     }

                     int[] trunkFrom = {this.baseX, base, this.baseZ};
                     if (this.checkBlockLine(trunkFrom, from) == -1) {
                        nodes[index][0] = nx;
                        nodes[index][1] = topY;
                        nodes[index][2] = nz;
                        nodes[index][3] = base;
                        index++;
                     }
                  }
               }

               topY--;
               dy--;
            }
         }

         this.leafNodes = new int[index][4];
         System.arraycopy(nodes, 0, this.leafNodes, 0, index);
      }

      private float layerSize(int dy) {
         if (dy < this.heightLimit * 0.3) {
            return -1.618F;
         }

         float half = this.heightLimit / 2.0F;
         float off = half - dy;
         float size;
         if (off == 0.0F) {
            size = half;
         } else if (Math.abs(off) >= half) {
            size = 0.0F;
         } else {
            size = (float)Math.sqrt(half * half - off * off);
         }

         return size * 0.5F;
      }

      private float leafSize(int dy) {
         return dy >= 0 && dy < LEAF_DISTANCE_LIMIT
            ? (dy != 0 && dy != LEAF_DISTANCE_LIMIT - 1 ? 3.0F : 2.0F) : -1.0F;
      }

      private void generateLeafNode(int x, int y, int z) {
         for (int yy = y; yy < y + LEAF_DISTANCE_LIMIT; yy++) {
            this.genTreeLayer(x, yy, z, this.leafSize(yy - y), (byte)1);
         }
      }

      private void genTreeLayer(int x, int y, int z, float size, byte axis) {
         int radius = (int)(size + 0.618);
         byte ax1 = OTHER_COORD_PAIRS[axis];
         byte ax2 = OTHER_COORD_PAIRS[axis + 3];
         int[] center = {x, y, z};
         int[] p = {0, 0, 0};
         p[axis] = center[axis];
         for (int d1 = -radius; d1 <= radius; d1++) {
            p[ax1] = center[ax1] + d1;
            for (int d2 = -radius; d2 <= radius; d2++) {
               double dist = Math.pow(Math.abs(d1) + 0.5, 2.0) + Math.pow(Math.abs(d2) + 0.5, 2.0);
               if (dist <= size * size) {
                  p[ax2] = center[ax2] + d2;
                  BlockState cur = this.level.getBlockState(new BlockPos(p[0], p[1], p[2]));
                  if (cur.isAir() || cur.is(ModBlocks.GREATWOOD_LEAVES.get())) {
                     this.level.setBlock(new BlockPos(p[0], p[1], p[2]),
                        ModBlocks.GREATWOOD_LEAVES.get().defaultBlockState(), 3);
                  }
               }
            }
         }
      }

      /** Линия блоков; мета направления 0/4/8 → ось Y/X/Z. */
      private void placeBlockLine(int[] from, int[] to) {
         int[] delta = {to[0] - from[0], to[1] - from[1], to[2] - from[2]};
         int main = 0;
         for (int i = 1; i < 3; i++) {
            if (Math.abs(delta[i]) > Math.abs(delta[main])) {
               main = i;
            }
         }

         if (delta[main] == 0) {
            return;
         }

         int ax1 = OTHER_COORD_PAIRS[main];
         int ax2 = OTHER_COORD_PAIRS[main + 3];
         int step = delta[main] > 0 ? 1 : -1;
         double f1 = (double)delta[ax1] / delta[main];
         double f2 = (double)delta[ax2] / delta[main];
         int[] p = {0, 0, 0};
         // ВНИМАНИЕ: цикл стартует с t = 0, а не с шага. В оригинале
         // (placeBlockLine) var15 = 0 и крутится до delta + step, поэтому
         // ставится И сам начальный блок линии. Со стартом с step нижний блок
         // ствола не рисовался — дерево висело в воздухе над землёй.
         int end = delta[main] + step;
         for (int t = 0, guard = 0; t != end && guard < 512; t += step, guard++) {
            p[main] = (int)(from[main] + t + 0.5);
            p[ax1] = (int)(from[ax1] + t * f1 + 0.5);
            p[ax2] = (int)(from[ax2] + t * f2 + 0.5);
            int dx = Math.abs(p[0] - from[0]);
            int dz = Math.abs(p[2] - from[2]);
            int spread = Math.max(dx, dz);
            Direction.Axis axis = Direction.Axis.Y;
            if (spread > 0) {
               axis = dx == spread ? Direction.Axis.X : Direction.Axis.Z;
            }

            this.level.setBlock(new BlockPos(p[0], p[1], p[2]),
               ModBlocks.GREATWOOD_LOG.get().defaultBlockState()
                  .setValue(RotatedPillarBlock.AXIS, axis), 3);
         }
      }

      private void generateLeaves() {
         for (int[] node : this.leafNodes) {
            this.generateLeafNode(node[0], node[1], node[2]);
         }
      }

      private void generateLeafNodeBases() {
         for (int[] node : this.leafNodes) {
            int base = node[3];
            if (base - this.baseY >= this.heightLimit * 0.2) {
               this.placeBlockLine(new int[]{this.baseX, base, this.baseZ},
                  new int[]{node[0], node[1], node[2]});
            }
         }
      }

      private void generateTrunk() {
         int top = this.baseY + this.height;
         this.placeBlockLine(new int[]{this.baseX, this.baseY, this.baseZ},
            new int[]{this.baseX, top, this.baseZ});
         this.placeBlockLine(new int[]{this.baseX + 1, this.baseY, this.baseZ},
            new int[]{this.baseX + 1, top, this.baseZ});
         this.placeBlockLine(new int[]{this.baseX, this.baseY, this.baseZ + 1},
            new int[]{this.baseX, top, this.baseZ + 1});
         this.placeBlockLine(new int[]{this.baseX + 1, this.baseY, this.baseZ + 1},
            new int[]{this.baseX + 1, top, this.baseZ + 1});
      }

      /** -1 если линия свободна (только воздух/листва), иначе длина до препятствия. */
      private int checkBlockLine(int[] from, int[] to) {
         int[] delta = {to[0] - from[0], to[1] - from[1], to[2] - from[2]};
         int main = 0;
         for (int i = 1; i < 3; i++) {
            if (Math.abs(delta[i]) > Math.abs(delta[main])) {
               main = i;
            }
         }

         if (delta[main] == 0) {
            return -1;
         }

         int ax1 = OTHER_COORD_PAIRS[main];
         int ax2 = OTHER_COORD_PAIRS[main + 3];
         int step = delta[main] > 0 ? 1 : -1;
         double f1 = (double)delta[ax1] / delta[main];
         double f2 = (double)delta[ax2] / delta[main];
         // Как в оригинале: проверка тоже начинается с t = 0 (checkBlockLine).
         int end = delta[main] + step;
         int t = 0;
         for (int guard = 0; t != end && guard < 512; t += step, guard++) {
            int[] p = {0, 0, 0};
            p[main] = from[main] + t;
            p[ax1] = (int)(from[ax1] + t * f1);
            p[ax2] = (int)(from[ax2] + t * f2);
            BlockState cur = this.level.getBlockState(new BlockPos(p[0], p[1], p[2]));
            if (!cur.isAir() && !cur.is(ModBlocks.GREATWOOD_LEAVES.get())) {
               break;
            }
         }

         return t == end ? -1 : Math.abs(t);
      }

      private boolean validTreeLocation(int dx, int dz) {
         BlockPos below = new BlockPos(this.baseX + dx, this.baseY - 1, this.baseZ + dz);
         if (!this.level.getBlockState(below).is(BlockTags.DIRT)) {
            return false;
         }

         int clear = this.checkBlockLine(
            new int[]{this.baseX + dx, this.baseY, this.baseZ + dz},
            new int[]{this.baseX + dx, this.baseY + this.heightLimit - 1, this.baseZ + dz});
         if (clear == -1) {
            return true;
         }

         if (clear < 6) {
            return false;
         }

         this.heightLimit = clear;
         return true;
      }
   }
}
