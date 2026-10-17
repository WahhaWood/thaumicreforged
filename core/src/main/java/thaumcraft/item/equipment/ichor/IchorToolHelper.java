package thaumcraft.item.equipment.ichor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.util.ItemStackData;

/**
 * Общие механики пробуждённых ихориевых инструментов (порт {@code ToolHandler} из Thaumic Tinkerer 2).
 *
 * <p>Режимы (как в TT2): 0 — Block, 1 — Square (5×5 в плоскости удара), 2 — Line/Column/Tree/Soul
 * (зависит от инструмента). Смена режима — Shift+ПКМ, режим хранится в NBT {@code ichor_mode}.
 */
public final class IchorToolHelper {
   private static final String TAG_MODE = "ichor_mode";
   public static final int MAX_MODE = 2;

   /** Материалы, допустимые для режимов кирки/лопаты/топора (аналог materialsPick/materialsShovel/materialsAxe). */
   public static final Predicate<BlockState> PICK_MATERIALS = state -> state.is(BlockTags.MINEABLE_WITH_PICKAXE);
   public static final Predicate<BlockState> SHOVEL_MATERIALS = state -> state.is(BlockTags.MINEABLE_WITH_SHOVEL);
   public static final Predicate<BlockState> AXE_MATERIALS = state -> state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.LEAVES);

   private IchorToolHelper() {
   }

   public static int getMode(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      return tag == null ? 0 : tag.getInt("ichor_mode");
   }

   public static void cycleMode(ItemStack stack) {
      int current = getMode(stack);
      int next = current >= MAX_MODE ? 0 : current + 1;
      CompoundTag tag = ItemStackData.getOrCreateTag(stack);
      tag.putInt(TAG_MODE, next);
      ItemStackData.setTag(stack, tag);
   }

   public static Component getModeText(String toolType, int mode) {
      return Component.translatable("tc.tool.mode." + toolType + "." + mode).withStyle(ChatFormatting.GOLD);
   }

   /**
    * Ориентир удара: направление грани блока, по которой пришёлся взгляд игрока.
    * Аналог {@code ToolHandler.raytraceFromEntity(world, player, true, 4.5)} из TT2.
    */
   public static Direction rayTraceFace(Player player, BlockPos target) {
      HitResult hit = player.pick(4.5, 1.0F, false);
      if (hit.getType() == HitResult.Type.BLOCK) {
         BlockHitResult blockHit = (BlockHitResult)hit;
         if (blockHit.getBlockPos().equals(target)) {
            return blockHit.getDirection();
         }
      }

      return estimateHitFace(player, target);
   }

   /**
    * Грубая оценка грани по положению глаз (фолбэк, если рейтрейс не совпал с целевым блоком).
    */
   public static Direction estimateHitFace(Player player, BlockPos blockPos) {
      Vec3 eye = player.getEyePosition(1.0F);
      Vec3 center = Vec3.atCenterOf(blockPos);
      double dx = center.x - eye.x;
      double dy = center.y - eye.y;
      double dz = center.z - eye.z;
      double adx = Math.abs(dx);
      double ady = Math.abs(dy);
      double adz = Math.abs(dz);
      if (ady >= adx && ady >= adz) {
         return dy > 0.0 ? Direction.DOWN : Direction.UP;
      } else if (adx >= adz) {
         return dx > 0.0 ? Direction.WEST : Direction.EAST;
      } else {
         return dz > 0.0 ? Direction.NORTH : Direction.SOUTH;
      }
   }

   /** Диапазоны «квадратного» режима TT2: 5×5 в плоскости, перпендикулярной грани (Y смещён на 1 вверх). */
   public static int[] squareRanges(Direction face) {
      Axis axis = face.getAxis();
      int xs = axis == Axis.X ? 0 : -2;
      int ys = axis == Axis.Y ? 0 : -1;
      int zs = axis == Axis.Z ? 0 : -2;
      int xe = axis == Axis.X ? 1 : 3;
      int ye = axis == Axis.Y ? 1 : 4;
      int ze = axis == Axis.Z ? 1 : 3;
      return new int[]{xs, ys, zs, xe, ye, ze};
   }

   /** Диапазоны «линейного» режима TT2: 10 блоков от удара «в глубину» по нормали грани. */
   public static int[] lineRanges(Direction face) {
      int xo = -face.getStepX();
      int yo = -face.getStepY();
      int zo = -face.getStepZ();
      return new int[]{
         xo >= 0 ? 0 : -10,
         yo >= 0 ? 0 : -10,
         zo >= 0 ? 0 : -10,
         xo > 0 ? 10 : 1,
         yo > 0 ? 10 : 1,
         zo > 0 ? 10 : 1
      };
   }

   /**
    * Ломает блоки в заданном диапазоне вокруг центра (порт {@code ToolHandler.removeBlocksInIteration}).
    * Дроп подтягивается к игроку (канонический эффект ихориевых инструментов).
    *
    * @param requiredBlock если не null — ломаем только этот блок (режим колонны у лопаты)
    */
   public static void removeBlocksInIteration(
      Level level,
      Player player,
      ItemStack tool,
      BlockPos center,
      int xs,
      int ys,
      int zs,
      int xe,
      int ye,
      int ze,
      Block requiredBlock,
      Predicate<BlockState> materials
   ) {
      if (level.isClientSide) {
         return;
      }

      int broken = 0;
      for (int x1 = xs; x1 < xe; x1++) {
         for (int y1 = ys; y1 < ye; y1++) {
            for (int z1 = zs; z1 < ze; z1++) {
               if (x1 == 0 && y1 == 0 && z1 == 0) {
                  continue;
               }

               BlockPos pos = center.offset(x1, y1, z1);
               if (!level.isLoaded(pos)) {
                  continue;
               }

               BlockState state = level.getBlockState(pos);
               if (state.isAir()) {
                  continue;
               }

               if (requiredBlock != null && !state.is(requiredBlock)) {
                  continue;
               }

               if (!materials.test(state) || !tool.isCorrectToolForDrops(state)) {
                  continue;
               }

               if (level.destroyBlock(pos, true, player)) {
                  broken++;
               }
            }
         }
      }

      if (broken > 0) {
         pullDropsToPlayer(level, player, iterationArea(center, xs, ys, zs, xe, ye, ze));
      }
   }

   /** Построение AABB по диапазону итерации. */
   private static AABB iterationArea(BlockPos center, int xs, int ys, int zs, int xe, int ye, int ze) {
      int minX = Math.min(xs, xe - 1);
      int minY = Math.min(ys, ye - 1);
      int minZ = Math.min(zs, ze - 1);
      int maxX = Math.max(xs, xe - 1) + 1;
      int maxY = Math.max(ys, ye - 1) + 1;
      int maxZ = Math.max(zs, ze - 1) + 1;
      return new AABB(
         center.getX() + minX, center.getY() + minY, center.getZ() + minZ,
         center.getX() + maxX, center.getY() + maxY, center.getZ() + maxZ
      );
   }

   /**
    * Притягивание выпавших предметов к игроку (в оригинале — телепорт в позицию игрока).
    *
    * <p>Раньше все сущности дропа складывались в одну точку перед игроком, и при
    * добыче 3x3 весь экран закрывала стена предметов. Теперь дроп уходит сразу в
    * инвентарь, а у игрока появляется только переполнение (которое не влезло) —
    * с небольшим разбросом и без задержки подбора.
    */
   public static void pullDropsToPlayer(Level level, Player player, AABB area) {
      if (level.isClientSide) {
         return;
      }

      for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area.inflate(1.0))) {
         ItemStack stack = item.getItem();
         if (stack.isEmpty()) {
            continue;
         }

         ItemStack rest = stack.copy();
         // Inventory.add складывает в себя всё, что влезло, остаток оставляет в стеке.
         if (player.getInventory().add(rest) || rest.isEmpty()) {
            item.discard();
         } else {
            item.setItem(rest);
            item.setPos(
               player.getX() + (level.random.nextDouble() - 0.5) * 1.5,
               player.getY() + 1.0,
               player.getZ() + (level.random.nextDouble() - 0.5) * 1.5
            );
            item.setDeltaMovement(Vec3.ZERO);
            item.setPickUpDelay(0);
         }
      }
   }

   /**
    * Режим Tree у пробуждённого топора: свалить всё дерево (порт идеи {@code BlockUtils.breakFurthestBlock}).
    * Начинаем с BFS по соединённым брёвнам, затем ломаем от самых дальних к ближним.
    */
   public static void fellTree(Level level, Player player, ItemStack tool, BlockPos start, BlockState startState) {
      if (level.isClientSide) {
         return;
      }

      if (!startState.is(BlockTags.LOGS)) {
         return;
      }

      List<BlockPos> logs = new ArrayList<>();
      HashSet<BlockPos> visited = new HashSet<>();
      ArrayDeque<BlockPos> queue = new ArrayDeque<>();
      visited.add(start);

      for (Direction direction : Direction.values()) {
         BlockPos next = start.relative(direction);
         if (level.isLoaded(next) && level.getBlockState(next).is(BlockTags.LOGS)) {
            queue.add(next);
            visited.add(next);
         }
      }

      int limit = 512;
      while (!queue.isEmpty() && logs.size() < limit) {
         BlockPos pos = queue.poll();
         logs.add(pos);

         for (Direction direction : Direction.values()) {
            BlockPos next = pos.relative(direction);
            if (visited.add(next) && level.isLoaded(next) && level.getBlockState(next).is(BlockTags.LOGS)) {
               queue.add(next);
            }
         }
      }

      // От дальних к ближним — «сверху вниз» без плавающих обрубков.
      logs.sort(Comparator.comparingDouble(pos -> -pos.distSqr(start)));

      double minX = start.getX();
      double minY = start.getY();
      double minZ = start.getZ();
      double maxX = start.getX();
      double maxY = start.getY();
      double maxZ = start.getZ();
      for (BlockPos pos : logs) {
         if (level.getBlockState(pos).is(BlockTags.LOGS) && tool.isCorrectToolForDrops(level.getBlockState(pos))) {
            level.destroyBlock(pos, true, player);
         }
         minX = Math.min(minX, pos.getX());
         minY = Math.min(minY, pos.getY());
         minZ = Math.min(minZ, pos.getZ());
         maxX = Math.max(maxX, pos.getX());
         maxY = Math.max(maxY, pos.getY());
         maxZ = Math.max(maxZ, pos.getZ());
      }

      pullDropsToPlayer(
         level, player, new AABB(minX - 1.0, minY - 1.0, minZ - 1.0, maxX + 2.0, maxY + 2.0, maxZ + 2.0)
      );
   }

   /** Столб вниз/вверх из одинаковых блоков (режим Column у лопаты TT2). */
   public static void breakColumn(Level level, Player player, ItemStack tool, BlockPos center, BlockState centerState) {
      if (level.isClientSide || centerState.isAir()) {
         return;
      }

      Block block = centerState.getBlock();
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

      for (int dy = -8; dy < 8; dy++) {
         if (dy == 0) {
            continue;
         }

         pos.set(center.getX(), center.getY() + dy, center.getZ());
         if (!level.isLoaded(pos)) {
            continue;
         }

         BlockState state = level.getBlockState(pos);
         if (state.is(block) && tool.isCorrectToolForDrops(state)) {
            level.destroyBlock(pos.immutable(), true, player);
         }
      }

      pullDropsToPlayer(level, player, new AABB(center).inflate(1.0, 9.0, 1.0));
   }
}
