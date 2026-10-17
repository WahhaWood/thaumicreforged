package thaumcraft.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.block.airy.aura_node.AuraNodeBlockEntity;
import thaumcraft.block.eldritch.altar.EldritchAltarBlockEntity;
import thaumcraft.init.registry.ModBlocks;

/**
 * Кольцо элдричей 1:1 с {@code WorldGenEldritchRing} (1.7.10) — переход в мир с боссами.
 *
 * <p>Этой структуры не было в порту вообще: платформа 7x7 без углов из
 * обсидиановой плитки и обсидиана, в центре алтарь элдричей, над ним колонна
 * обелиска, по кольцу r=3 восемь капителей, тёмный узел в центре. Шанс в TC4 —
 * 1/66 на чанк (в цепочке после кургана 1/150).
 *
 * <p>Маппинг: {@code blockCosmeticSolid:1} → обсидиановая плитка
 * ({@code OBSIDIAN_TILE}), {@code blockEldritch:0} → алтарь,
 * {@code :1} → обелиск, {@code :2} → сегменты, {@code :3} → капители,
 * {@code blockWoodenDevice:8} → знамя культа.
 */
public class EldritchRingFeature extends Feature<NoneFeatureConfiguration> {
   public EldritchRingFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      return generate(context.level(), context.origin(), context.random());
   }

   /** Построить кольцо; base — позиция центра на уровне платформы. */
   public static boolean generate(WorldGenLevel level, BlockPos base, RandomSource random) {
      int i = base.getX();
      int j = base.getY();
      int k = base.getZ();
      // Проверка пяти точек опоры, как в оригинале.
      if (!validSpawn(level, i - 3, j, k - 3) || !validSpawn(level, i, j, k)
         || !validSpawn(level, i + 3, j, k) || !validSpawn(level, i + 3, j, k + 3)
         || !validSpawn(level, i, j, k + 3)) {
         return false;
      }

      BlockState tile = ModBlocks.OBSIDIAN_TILE.get().defaultBlockState();
      BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
      for (int x = i - 3; x <= i + 3; x++) {
         for (int z = k - 3; z <= k + 3; z++) {
            boolean corner = (x == i - 3 || x == i + 3) && (z == k - 3 || z == k + 3);
            if (corner) {
               continue;
            }

            for (int q = -4; q < 5; q++) {
               BlockPos p = new BlockPos(x, j + q, z);
               BlockState cur = level.getBlockState(p);
               if (q <= 0 || cur.isAir() || !cur.isSolid()) {
                  level.setBlock(p, random.nextInt(4) == 0 ? obsidian : tile, 3);
               }

               if (q > 0) {
                  level.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
               }
            }

            if (x == i && z == k) {
               level.setBlock(new BlockPos(x, j + 1, z),
                  ModBlocks.ELDRITCH_ALTAR.get().defaultBlockState(), 3);
               level.setBlock(new BlockPos(x, j, z), tile, 3);
               int r = random.nextInt(10);
               if (level.getBlockEntity(new BlockPos(x, j + 1, z)) instanceof EldritchAltarBlockEntity altar) {
                  if (r >= 1 && r <= 4) {
                     altar.setSpawner(true);
                     altar.setSpawnType((byte)0);
                     // Знамёна культа по сторонам на r=3.
                     placeBanner(level, new BlockPos(x - 3, j + 1, z));
                     placeBanner(level, new BlockPos(x + 3, j + 1, z));
                     placeBanner(level, new BlockPos(x, j + 1, z - 3));
                     placeBanner(level, new BlockPos(x, j + 1, z + 3));
                  } else if (r == 6 || r == 7) {
                     altar.setSpawner(true);
                     altar.setSpawnType((byte)1);
                  }
               }

               level.setBlock(new BlockPos(x, j + 3, z),
                  ModBlocks.ELDRITCH_OBELISK.get().defaultBlockState(), 3);
               for (int q = 4; q <= 7; q++) {
                  level.setBlock(new BlockPos(x, j + q, z),
                     ModBlocks.ELDRITCH_OBELISK_PART.get().defaultBlockState(), 3);
               }

               // Тёмный узел в центре (в TC4 — createRandomNodeAt eerie=true,
               // ThaumcraftWorldGenerator:233-234, тип принудительно DARK).
               BlockPos nodePos = new BlockPos(x, j + 2, z);
               level.setBlock(nodePos, ModBlocks.AURA_NODE.get().defaultBlockState(), 3);
               ThaumcraftWorldgen.initEerieNode(level, nodePos, random);
               if (level.getBlockEntity(nodePos) instanceof AuraNodeBlockEntity node) {
                  node.setNodeType(NodeType.DARK);
                  node.setChanged();
               }
            } else if ((((x == i - 3 || x == i + 3) && Math.abs((z - k) % 2) == 1)
               || ((z == k - 3 || z == k + 3) && Math.abs((x - i) % 2) == 1))
               && Math.abs(x - i) != Math.abs(z - k)) {
               level.setBlock(new BlockPos(x, j, z), tile, 3);
               // Капитель (blockEldritch:3 / TileEldritchCap). Раньше здесь стоял
               // ELDRITCH_OBELISK_PART с RenderShape.INVISIBLE — восемь блоков
               // вокруг алтаря выглядели прозрачными.
               level.setBlock(new BlockPos(x, j + 1, z),
                  ModBlocks.ELDRITCH_CAPSTONE.get().defaultBlockState(), 3);
            }
         }
      }

      return true;
   }

   private static void placeBanner(WorldGenLevel level, BlockPos pos) {
      BlockState cur = level.getBlockState(pos);
      if (cur.isAir() || !cur.isSolid()) {
         level.setBlock(pos, ModBlocks.CRIMSON_CULT_BANNER.get().defaultBlockState(), 3);
      }
   }

   /** Точка опоры: не выше 2 блоков над землёй, земля подходит, сверху воздух. */
   private static boolean validSpawn(WorldGenLevel level, int x, int y, int z) {
      // Дословно из LocationIsValidSpawn: считаем НЕвоздушные блоки вверх от точки.
      int solid = 0;
      while (!level.getBlockState(new BlockPos(x, y + solid, z)).isAir()) {
         solid++;
         if (y + solid > level.getMaxBuildHeight() - 1) {
            return false;
         }
      }

      if (solid > 2) {
         return false;
      }

      BlockPos ground = new BlockPos(x, y + solid - 1, z);
      BlockState below = level.getBlockState(ground);
      // Раньше здесь была своя проверка через BlockTags.DIRT, и в неё НЕ входил
      // grass_block — а трава самый частый грунт. Кольцо не ставилось почти
      // никогда: /locate находил точку, но постройка молча отменялась.
      // В TC4 GetValidSpawnBlocks включает grass, поэтому берём общий isSoil.
      boolean soil = StructureGen.isSoil(below);
      return soil && level.getBlockState(ground.above()).isAir();
   }
}
