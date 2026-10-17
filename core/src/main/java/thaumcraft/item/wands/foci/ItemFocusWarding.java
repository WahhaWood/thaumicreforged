package thaumcraft.item.wands.foci;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.TintedGlassBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.IArchitect;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.block.opaque.OwnedBlockEntity;
import thaumcraft.block.opaque.WardedGlassBlock;
import thaumcraft.block.warded.WardedBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.WandManager;

public class ItemFocusWarding extends ItemFocusBasic implements IArchitect {
   private static final AspectList COST = new AspectList().add(Aspect.EARTH, 25).add(Aspect.ORDER, 25).add(Aspect.WATER, 10);
   public static final Map<String, Long> DELAY_MAP = new HashMap<>();
   /** Счётчик кликов между чистками карты (см. {@link ItemFocusExcavation#purgeCounter}). */
   private static int purgeCounter = 0;
   /** Записи старше этого срока считаются протухшими и удаляются целиком. */
   private static final long STALE_TIMEOUT_MS = 5000L;

   public ItemFocusWarding() {
      super("warding");
   }

   /**
    * Портирование {@code ItemFocusWarding#onFocusRightClick} (1.7.10): ставит/снимает
    * защитные блоки по области архитектора. Повторный клик по своей защите снимает её.
    *
    * <p>В 1.7.10 метод получал {@code MovingObjectPosition}, то есть срабатывал именно
    * по клику на блок; в 1.21 это {@code onFocusBlockClick}, а не right-click по воздуху.
    */
   @Override
   public ItemStack onFocusBlockClick(ItemStack wandStack, Level level, Player player, InteractionHand hand, BlockPos pos, Direction direction) {
      ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
      ItemStack focusStack = wand.getFocusItem(wandStack);
      player.swing(hand);
      if (level.isClientSide) {
         return wandStack;
      }

      long now = System.currentTimeMillis();
      // Чистим не каждый клик: полный removeIf по всей карте на горячем пути стоит
      // O(n) на клик, а карта растёт с каждым заблокеанным блоком за всю жизнь сервера
      // и больше ничем не чистится. Раз в 100 кликов достаточно — протухшая запись
      // всё равно перестаёт влиять на результат раньше, чем будет удалена.
      if (++purgeCounter >= 100) {
         purgeCounter = 0;
         DELAY_MAP.entrySet().removeIf(entry -> now - entry.getValue() > STALE_TIMEOUT_MS);
      }

      String key = pos.getX() + ":" + pos.getY() + ":" + pos.getZ() + ":" + level.dimension().location();
      Long lockedUntil = DELAY_MAP.get(key);
      if (lockedUntil != null && lockedUntil > now) {
         return wandStack;
      }

      DELAY_MAP.put(key, now + 500L);
      BlockEntity te = level.getBlockEntity(pos);
      boolean solid = level.getBlockState(pos).isCollisionShapeFullBlock(level, pos);
      if (te == null && solid) {
         for (BlockPos c : this.getArchitectBlocks(wandStack, level, pos, direction, player)) {
            if (!wand.consumeAllVis(wandStack, player, this.getVisCost(focusStack), true, false)) {
               break;
            }

            if (level.getBlockEntity(c) == null && level.getBlockState(c).isCollisionShapeFullBlock(level, c)) {
               BlockState oldState = level.getBlockState(c);
               int light = oldState.getLightEmission(level, c);
               level.setBlock(c, ModBlocks.WARDED.get().defaultBlockState(), 3);
               if (level.getBlockEntity(c) instanceof WardedBlockEntity tw) {
                  tw.setWardedData(
                     oldState, light, player.getUUID().hashCode(), player.getUUID(), player.getScoreboardName()
                  );
                  if (level instanceof net.minecraft.server.level.ServerLevel sl) {
                     WardedBlockEntity.spawnSparkle(sl, c, 16556032);
                  }
               }
            }
         }

         level.playSound(
            null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            thaumcraft.init.registry.ModSounds.ZAP.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.25F, 1.0F
         );
      } else if (te instanceof WardedBlockEntity tw && this.canInteractWithWard(player, tw)) {
         // Снятие защиты по области (только по своим блокам): за каждый блок списываем вис,
         // без виса блок не снимается. Проверка доступа та же, что и при построении области.
         boolean doit = !level.isClientSide;
         boolean removedAny = false;
         for (BlockPos c : this.getArchitectBlocks(wandStack, level, pos, direction, player)) {
            if (level.getBlockEntity(c) instanceof WardedBlockEntity tw2 && this.canInteractWithWard(player, tw2)) {
               if (!wand.consumeAllVis(wandStack, player, this.getVisCost(focusStack), doit, false)) {
                  break;
               }

               BlockState restored = tw2.getMimic();
               level.setBlock(c, restored, 3);
               if (level instanceof net.minecraft.server.level.ServerLevel sl) {
                  WardedBlockEntity.spawnSparkle(sl, c, 16556032);
               }

               removedAny = true;
            }
         }

         if (removedAny) {
            level.playSound(
               null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
               thaumcraft.init.registry.ModSounds.ZAP.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.25F, 1.0F
            );
         }
      }

      return wandStack;
   }

   @Override
   public String getSortingHelper(ItemStack itemstack) {
      return "BWA" + super.getSortingHelper(itemstack);
   }

   @Override
   public int getFocusColor(ItemStack itemstack) {
      return 16771535;
   }

   @Override
   public AspectList getVisCost(ItemStack itemstack) {
      return COST.copy();
   }

   private static boolean isGlassLike(BlockState state) {
      Block block = state.getBlock();
      return block == Blocks.GLASS
         || block == Blocks.TINTED_GLASS
         || block instanceof StainedGlassBlock
         || block instanceof TintedGlassBlock
         || block instanceof HalfTransparentBlock
         || block instanceof IronBarsBlock
         || block instanceof StainedGlassPaneBlock;
   }

   private boolean canInteractWithWard(Player player, WardedBlockEntity tile) {
      UUID ownerUUID = tile.getOwnerUUID();
      return ownerUUID == null ? tile.getOwnerHash() == player.getScoreboardName().hashCode() : ownerUUID.equals(player.getUUID());
   }

   private boolean canInteractWithOwned(Player player, OwnedBlockEntity tile) {
      return tile.owner != null && tile.owner.equals(player.getScoreboardName());
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemstack, int rank) {
      return switch (rank) {
         case 1 -> new FocusUpgradeType[]{FocusUpgradeType.frugal};
         case 2 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.architect};
         case 3, 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge};
         default -> null;
      };
   }

   @Override
   public boolean canApplyUpgrade(ItemStack focusstack, Player player, FocusUpgradeType type, int rank) {
      return !type.equals(FocusUpgradeType.enlarge) || this.isUpgradedWith(focusstack, FocusUpgradeType.architect);
   }

   @Override
   public int getMaxAreaSize(ItemStack focusstack) {
      return 3 + this.getUpgradeLevel(focusstack, FocusUpgradeType.enlarge);
   }

   @Override
   public ArrayList<BlockPos> getArchitectBlocks(ItemStack stack, Level level, BlockPos startPos, Direction side, Player player) {
      ArrayList<BlockPos> out = new ArrayList<>();
      ItemWandCasting wand = (ItemWandCasting)stack.getItem();
      wand.getFocus(stack);
      BlockEntity startBe = level.getBlockEntity(startPos);
      BlockState startState = level.getBlockState(startPos);
      boolean targetIsWarded = startBe instanceof WardedBlockEntity;
      boolean targetIsWardedGlass = !targetIsWarded && startBe instanceof OwnedBlockEntity && startState.getBlock() instanceof WardedGlassBlock;
      boolean targetIsGlass = !targetIsWarded && !targetIsWardedGlass && isGlassLike(startState);
      int rangeX = 0;
      int rangeY = 0;
      int rangeZ = 0;
      if (this.isUpgradedWith(wand.getFocusItem(stack), FocusUpgradeType.architect)) {
         int areaX = WandManager.getAreaX(stack);
         int areaY = WandManager.getAreaY(stack);
         int areaZ = WandManager.getAreaZ(stack);
         switch (side.getAxis()) {
            case Y:
               rangeX = areaX;
               rangeY = areaY;
               rangeZ = areaZ;
               break;
            case Z:
               rangeX = areaX;
               rangeY = areaZ;
               rangeZ = areaY;
               break;
            case X:
               rangeY = areaX;
               rangeZ = areaZ;
               rangeX = areaY;
         }
      }

      Queue<BlockPos> queue = new ArrayDeque<>();
      Set<BlockPos> visited = new HashSet<>();
      queue.add(startPos);
      visited.add(startPos);

      while (!queue.isEmpty()) {
         BlockPos current = queue.poll();
         if (Math.abs(current.getX() - startPos.getX()) <= rangeX
            && Math.abs(current.getY() - startPos.getY()) <= rangeY
            && Math.abs(current.getZ() - startPos.getZ()) <= rangeZ) {
            BlockEntity currentBe = level.getBlockEntity(current);
            BlockState currentState = level.getBlockState(current);
            boolean currentSolid = currentState.isRedstoneConductor(level, current);
            boolean currentGlass = isGlassLike(currentState);
            boolean isValid = false;
            if (targetIsWarded) {
               if (currentBe instanceof WardedBlockEntity tw && this.canInteractWithWard(player, tw)) {
                  isValid = true;
               }
            } else if (targetIsWardedGlass) {
               if (currentBe instanceof OwnedBlockEntity owned
                  && currentState.getBlock() instanceof WardedGlassBlock
                  && this.canInteractWithOwned(player, owned)) {
                  isValid = true;
               }
            } else if (targetIsGlass) {
               if (currentBe == null && currentGlass && !currentState.isAir()) {
                  isValid = true;
               }
            } else if (currentBe == null && currentSolid && !currentState.isAir()) {
               isValid = true;
            }

            if (isValid) {
               out.add(new BlockPos(current.getX(), current.getY(), current.getZ()));

               for (Direction dir : Direction.values()) {
                  BlockPos neighbor = current.relative(dir);
                  if (visited.add(neighbor) && !level.isEmptyBlock(neighbor)) {
                     queue.add(neighbor);
                  }
               }
            }
         }
      }

      return out;
   }

   @Override
   public boolean showAxis(ItemStack stack, Level level, Player player, Direction side, IArchitect.EnumAxis axis) {
      int dim = WandManager.getAreaDim(stack);
      if (dim == 0) {
         return true;
      } else {
         return switch (side.getAxis()) {
            case Y -> axis == IArchitect.EnumAxis.X && dim == 1 || axis == IArchitect.EnumAxis.Z && dim == 2 || axis == IArchitect.EnumAxis.Y && dim == 3;
            case Z -> axis == IArchitect.EnumAxis.Y && dim == 1 || axis == IArchitect.EnumAxis.X && dim == 2 || axis == IArchitect.EnumAxis.Z && dim == 3;
            case X -> axis == IArchitect.EnumAxis.Y && dim == 1 || axis == IArchitect.EnumAxis.Z && dim == 2 || axis == IArchitect.EnumAxis.X && dim == 3;
            default -> throw new MatchException(null, null);
         };
      }
   }
}
