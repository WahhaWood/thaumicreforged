package thaumcraft.util;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BlockUtils {
   private static final ThreadLocal<Boolean> INTERNAL_BREAK = ThreadLocal.withInitial(() -> Boolean.FALSE);

   public static boolean isInternalBreak() {
      return INTERNAL_BREAK.get();
   }

   private static boolean withInternalBreak(BooleanSupplier action) {
      boolean prev = INTERNAL_BREAK.get();
      INTERNAL_BREAK.set(true);

      boolean var2;
      try {
         var2 = action.getAsBoolean();
      } finally {
         INTERNAL_BREAK.set(prev);
      }

      return var2;
   }

   public static BlockPos findFurthestConnectedLog(Level level, BlockPos start, BlockState startState, int maxVisited) {
      if (!startState.is(BlockTags.LOGS)) {
         return start;
      } else {
         Block logBlock = startState.getBlock();
         int LIMIT_XZ = 24;
         int LIMIT_Y = 48;
         LongOpenHashSet visited = new LongOpenHashSet();
         Deque<BlockPos> queue = new ArrayDeque<>();
         visited.add(start.asLong());
         queue.add(start);
         BlockPos furthest = start;
         double bestDist = 0.0;
         MutableBlockPos mut = new MutableBlockPos();

         while (!queue.isEmpty() && visited.size() < maxVisited) {
            BlockPos cur = queue.pollFirst();
            double dist = cur.distSqr(start);
            if (dist > bestDist) {
               bestDist = dist;
               furthest = cur;
            }

            for (int ox = -1; ox <= 1; ox++) {
               for (int oy = -1; oy <= 1; oy++) {
                  for (int oz = -1; oz <= 1; oz++) {
                     if (ox != 0 || oy != 0 || oz != 0) {
                        mut.set(cur.getX() + ox, cur.getY() + oy, cur.getZ() + oz);
                        int dx = Math.abs(mut.getX() - start.getX());
                        int dy = Math.abs(mut.getY() - start.getY());
                        int dz = Math.abs(mut.getZ() - start.getZ());
                        if (dx <= 24 && dz <= 24 && dy <= 48) {
                           long key = mut.asLong();
                           if (!visited.contains(key)) {
                              BlockState st = level.getBlockState(mut);
                              if (st.is(logBlock) && st.is(BlockTags.LOGS) && st.getDestroySpeed(level, mut) >= 0.0F) {
                                 visited.add(key);
                                 queue.add(mut.immutable());
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return furthest;
      }
   }

   @Nullable
   public static BlockPos breakFurthestBlockPos(
      Level level, BlockPos hitPos, BlockState hitState, Player player, boolean followItem, int followType, int maxVisited
   ) {
      if (level.isClientSide) {
         return null;
      } else if (!(level instanceof ServerLevel)) {
         return null;
      } else if (player instanceof ServerPlayer sp) {
         if (!hitState.is(BlockTags.LOGS)) {
            return null;
         } else {
            BlockPos target = findFurthestConnectedLog(level, hitPos, hitState, maxVisited);
            return (BlockPos)null;
         }
      } else {
         return null;
      }
   }

   public static boolean breakFurthestBlock(Level level, BlockPos hitPos, BlockState hitState, Player player, boolean followItem, int followType) {
      return breakFurthestBlockPos(level, hitPos, hitState, player, followItem, followType, 4096) != null;
   }

   private static boolean destroyBlockNoHooks(ServerLevel level, ServerPlayer player, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      if (state.isAir()) {
         return false;
      } else if (state.getDestroySpeed(level, pos) < 0.0F) {
         return false;
      } else if (!level.mayInteract(player, pos)) {
         return false;
      } else {
         BlockEntity be = level.getBlockEntity(pos);
         ItemStack tool = player.getInventory().getSelected();
         if (tool.isEmpty()) {
            tool = ItemStack.EMPTY;
         }

         state.getBlock().playerWillDestroy(level, pos, state, player);
         boolean removed = level.removeBlock(pos, false);
         if (!removed) {
            return false;
         } else {
            level.levelEvent(2001, pos, Block.getId(state));
            state.getBlock().playerDestroy(level, player, pos, state, be, tool);
            level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
            return true;
         }
      }
   }

   public static BlockHitResult getTargetBlock(Level level, Entity entity, double range) {
      return getTargetBlock(level, entity, range, false);
   }

   public static BlockHitResult getTargetBlock(Level level, Entity entity, double range, boolean stopOnLiquid) {
      Vec3 start = entity.getEyePosition();
      Vec3 look = entity.getViewVector(1.0F);
      Vec3 end = start.add(look.x * range, look.y * range, look.z * range);
      return level.clip(new ClipContext(start, end, net.minecraft.world.level.ClipContext.Block.OUTLINE, stopOnLiquid ? Fluid.ANY : Fluid.NONE, entity));
   }

   public static void destroyBlockPartially(Level level, int breakerId, BlockPos pos, int progress) {
      if (level instanceof ServerLevel serverLevel) {
         serverLevel.destroyBlockProgress(breakerId, pos, progress);
      }
   }

   public static boolean isBlockAdjacentToAtleast(LevelAccessor level, BlockPos pos, Block block, int amount, int range) {
      int count = 0;

      for (int x = -range; x <= range; x++) {
         for (int y = -range; y <= range; y++) {
            for (int z = -range; z <= range; z++) {
               if (x != 0 || y != 0 || z != 0) {
                  BlockPos checkPos = pos.offset(x, y, z);
                  if (level.getBlockState(checkPos).is(block)) {
                     count++;
                  }

                  if (count >= amount) {
                     return true;
                  }
               }
            }
         }
      }

      return count >= amount;
   }

   public static List<ItemEntity> getContentsOfBlock(Level level, BlockPos pos) {
      AABB aabb = new AABB(pos);
      return level.getEntitiesOfClass(ItemEntity.class, aabb);
   }

   public static int countExposedSides(LevelAccessor level, BlockPos pos) {
      int count = 0;

      for (Direction dir : Direction.values()) {
         if (level.isEmptyBlock(pos.relative(dir))) {
            count++;
         }
      }

      return count;
   }

   public static boolean isBlockExposed(Level level, BlockPos pos) {
      for (Direction dir : Direction.values()) {
         BlockPos neighbor = pos.relative(dir);
         BlockState state = level.getBlockState(neighbor);
         if (!state.canOcclude() || !state.isSolidRender(level, neighbor)) {
            return true;
         }
      }

      return false;
   }

   public static boolean isAdjacentToSolidBlock(Level level, BlockPos pos) {
      for (Direction dir : Direction.values()) {
         if (level.getBlockState(pos.relative(dir)).isSolidRender(level, pos.relative(dir))) {
            return true;
         }
      }

      return false;
   }

   public static boolean isBlockTouching(LevelAccessor level, BlockPos pos, Block block) {
      for (Direction dir : Direction.values()) {
         if (level.getBlockState(pos.relative(dir)).is(block)) {
            return true;
         }
      }

      return false;
   }

   public static void dropBlockAsItem(Level level, BlockPos pos, BlockState state) {
      Block.dropResources(state, level, pos);
   }

   public static void dropBlockAsItemWithChance(Level level, BlockPos pos, BlockState state, float chance, int fortune) {
      if (!level.isClientSide && level.random.nextFloat() <= chance) {
         Block.dropResources(state, level, pos, null, null, ItemStack.EMPTY);
      }
   }
}
