package thaumcraft.world.features;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.tags.BlockTags;

/** Общие помощники генерации структур (порт идей 1.7.10). */
public final class StructureGen {
   public static boolean isSoil(BlockState state) {
      return state.is(BlockTags.DIRT)
         || state.is(Blocks.GRASS_BLOCK)
         || state.is(Blocks.STONE)
         || state.is(Blocks.GRAVEL)
         || state.is(Blocks.SAND)
         || state.is(Blocks.RED_SAND)
         || state.is(Blocks.SANDSTONE)
         || state.is(Blocks.NETHERRACK)
         || state.is(Blocks.END_STONE);
   }

   /** Первый твёрдый блок начиная со startY и ниже. */
   public static int groundY(WorldGenLevel level, int x, int z, int startY) {
      int top = Math.min(startY, level.getMaxBuildHeight() - 1);

      for (int y = top; y > level.getMinBuildHeight(); y--) {
         BlockPos pos = new BlockPos(x, y, z);
         if (level.getBlockState(pos).isSolidRender(level, pos)) {
            return y;
         }
      }

      return Integer.MIN_VALUE;
   }

   /** Верхний твёрдый блок по карте высот. */
   public static int surfaceY(WorldGenLevel level, int x, int z) {
      return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
   }

   public static boolean placeChest(LevelAccessor level, BlockPos pos, RandomSource random, ResourceKey<LootTable> table) {
      return placeChest(level, pos, random, table, net.minecraft.world.item.ItemStack.EMPTY);
   }

   public static boolean placeChest(LevelAccessor level, BlockPos pos, RandomSource random, ResourceKey<LootTable> table, net.minecraft.world.item.ItemStack extra) {
      level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
      if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
         chest.setLootTable(table, random.nextLong());
         if (extra != null && !extra.isEmpty()) {
            chest.setItem(13, extra);
         }
         return true;
      }

      return false;
   }

   public static void box(WorldGenLevel level, BlockPos from, BlockPos to, BlockState state) {
      for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
         level.setBlock(pos, state, 3);
      }
   }

   public static ResourceKey<LootTable> dungeonLoot() {
      return ResourceKey.create(Registries.LOOT_TABLE, net.minecraft.resources.ResourceLocation.withDefaultNamespace("chests/simple_dungeon"));
   }

   public static ResourceKey<LootTable> villageLoot() {
      return ResourceKey.create(Registries.LOOT_TABLE, net.minecraft.resources.ResourceLocation.withDefaultNamespace("chests/village/village_weaponsmith"));
   }

   private StructureGen() {
   }
}
