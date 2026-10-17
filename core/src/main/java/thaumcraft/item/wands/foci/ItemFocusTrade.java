package thaumcraft.item.wands.foci;

import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import thaumcraft.api.IArchitect;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.event.common.ServerTickEventsFML;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModBlockTags;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.WandManager;
import thaumcraft.util.BlockUtils;
import thaumcraft.util.ItemStackData;

public class ItemFocusTrade extends ItemFocusBasic implements IArchitect {
   private static final AspectList cost = new AspectList().add(Aspect.ENTROPY, 5).add(Aspect.EARTH, 5).add(Aspect.ORDER, 5);
   private static AspectList cost2 = null;
   private final ArrayList<BlockPos> checked = new ArrayList<>();

   public ItemFocusTrade() {
      super("trade");
   }

   @Override
   public String getSortingHelper(ItemStack itemstack) {
      return "BT" + super.getSortingHelper(itemstack);
   }

   private static BlockHitResult rayTrace(Level level, Player player) {
      double reach = player.blockInteractionRange();
      Vec3 eye = player.getEyePosition(1.0F);
      Vec3 look = player.getViewVector(1.0F);
      Vec3 end = eye.add(look.x * reach, look.y * reach, look.z * reach);
      return level.clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, player));
   }

   private static ItemStack stackFromState(BlockState state) {
      if (state.isAir()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack out = new ItemStack(state.getBlock());
         CompoundTag props = NbtUtils.writeBlockState(state).getCompound("Properties");
         if (!props.isEmpty()) {
            ItemStackData.getOrCreateTag(out).put("BlockStateTag", props.copy());
         }

         return out;
      }
   }

   @Override
   public ItemStack onFocusRightClick(ItemStack wandStack, Level level, Player player, InteractionHand ignored) {
      if (!Config.enableFocusTrade) {
         return wandStack;
      } else {
         BlockHitResult hit = rayTrace(level, player);
         if (hit.getType() != Type.BLOCK) {
            return wandStack;
         } else {
            BlockPos pos = hit.getBlockPos();
            BlockState state = level.getBlockState(pos);
            net.minecraft.world.level.block.Block block = state.getBlock();
            if (player.isShiftKeyDown()) {
               if (!level.isClientSide) {
                  if (level.getBlockEntity(pos) != null) {
                     return wandStack;
                  }

                  if (Config.focusTradePickBlackList.contains(block, 0)) {
                     return wandStack;
                  }

                  ItemStack picked = stackFromState(state);
                  if (!picked.isEmpty() && block != Blocks.AIR) {
                     this.storePickedBlock(wandStack, picked);
                  }
               } else {
                  player.swing(InteractionHand.MAIN_HAND);
               }

               return wandStack;
            } else {
               ItemStack pb = this.getPickedBlock(wandStack);
               if (pb == null || pb.isEmpty()) {
                  return wandStack;
               } else if (level.isClientSide) {
                  player.swing(InteractionHand.MAIN_HAND);
                  return wandStack;
               } else if (level.getBlockEntity(pos) != null) {
                  return wandStack;
               } else if (state.is(ModBlockTags.TAINT_MATERIAL)) {
                  return wandStack;
               } else {
                  ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
                  int selected = player.getInventory().selected;
                  // Цену читаем из стака фокуса: там лежат апгрейды.
                  AspectList price = this.getVisCost(wand.getFocusItem(wandStack));
                  if (this.isUpgradedWith(wand.getFocusItem(wandStack), FocusUpgradeType.architect)) {
                     Direction side = hit.getDirection();
                     // Собираем позиции заранее, чтобы посчитать суммарную цену.
                     ArrayList<BlockPos> targets = new ArrayList<>();
                     for (BlockPos p2 : this.getArchitectBlocks(wandStack, level, pos, side, player)) {
                        BlockState s2 = level.getBlockState(p2);
                        if (level.getBlockEntity(p2) == null
                           && !s2.is(ModBlockTags.TAINT_MATERIAL)
                           && !Config.focusTradeReplaceBlackList.contains(s2.getBlock(), 0)) {
                           targets.add(p2);
                        }
                     }

                     if (!targets.isEmpty()) {
                        AspectList total = new AspectList();
                        for (int i = 0; i < targets.size(); i++) {
                           total.add(price);
                        }

                        // Сначала проверяем, хватает ли виса на всё, потом списываем.
                        if (wand.consumeAllVis(wandStack, player, total, false, false)) {
                           wand.consumeAllVis(wandStack, player, total, !level.isClientSide, false);

                           for (BlockPos p2 : targets) {
                              ServerTickEventsFML.addSwapper(level, p2, level.getBlockState(p2), pb, 0, player, selected);
                           }
                        }
                     }
                  } else if (!Config.focusTradeReplaceBlackList.contains(block, 0)) {
                     // Без виса обмен не происходит.
                     if (wand.consumeAllVis(wandStack, player, price, !level.isClientSide, false)) {
                        int life = 3 + wand.getFocusEnlarge(wandStack);
                        ServerTickEventsFML.addSwapper(level, pos, state, pb, life, player, selected);
                     }
                  }

                  return wandStack;
               }
            }
         }
      }
   }

   public void storePickedBlock(ItemStack wandStack, ItemStack stackout) {
      ItemStackData.getOrCreateTag(wandStack).put("picked", ItemStackData.saveStack(stackout));
   }

   public ItemStack getPickedBlock(ItemStack wandStack) {
      CompoundTag tag = ItemStackData.getTag(wandStack);
      return tag != null && tag.contains("picked", 10) ? ItemStackData.loadStack(tag.getCompound("picked")) : null;
   }

   @Override
   public int getFocusColor(ItemStack itemstack) {
      return 8747923;
   }

   @Override
   public AspectList getVisCost(ItemStack focusstack) {
      if (this.isUpgradedWith(focusstack, FocusUpgradeType.silktouch)) {
         if (cost2 == null) {
            cost2 = new AspectList()
               .add(Aspect.AIR, 1)
               .add(Aspect.FIRE, 1)
               .add(Aspect.EARTH, 1)
               .add(Aspect.WATER, 1)
               .add(Aspect.ORDER, 1)
               .add(Aspect.ENTROPY, 1);
            cost2.add(cost);
         }

         return cost2;
      } else {
         return cost;
      }
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemstack, int rank) {
      return switch (rank) {
         case 1, 2, 4 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge};
         case 3 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge, FocusUpgradeType.treasure, FocusUpgradeType.architect};
         case 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge, FocusUpgradeType.silktouch};
         default -> null;
      };
   }

   @Override
   public int getMaxAreaSize(ItemStack focusstack) {
      return 3 + this.getUpgradeLevel(focusstack, FocusUpgradeType.enlarge) * 2;
   }

   @Override
   public ArrayList<BlockPos> getArchitectBlocks(ItemStack wandStack, Level level, BlockPos origin, Direction side, Player player) {
      ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
      wand.getFocus(wandStack);
      BlockState sourceState = level.getBlockState(origin);
      ArrayList<BlockPos> out = new ArrayList<>();
      this.checked.clear();
      int areaX = WandManager.getAreaX(wandStack);
      int areaY = WandManager.getAreaY(wandStack);
      int areaZ = WandManager.getAreaZ(wandStack);
      if (side != Direction.NORTH && side != Direction.SOUTH) {
         this.checkNeighbours(level, origin, sourceState, origin, side, areaX, areaY, areaZ, out, player);
      } else {
         this.checkNeighbours(level, origin, sourceState, origin, side, areaZ, areaY, areaX, out, player);
      }

      return out;
   }

   public void checkNeighbours(
      Level level,
      BlockPos origin,
      BlockState sourceState,
      BlockPos currentPos,
      Direction side,
      int sizeX,
      int sizeY,
      int sizeZ,
      ArrayList<BlockPos> list,
      Player player
   ) {
      if (!this.checked.contains(currentPos)) {
         this.checked.add(currentPos);
         switch (side) {
            case DOWN:
            case UP:
               if (Math.abs(currentPos.getX() - origin.getX()) > sizeX) {
                  return;
               }

               if (Math.abs(currentPos.getZ() - origin.getZ()) > sizeZ) {
                  return;
               }
               break;
            case NORTH:
            case SOUTH:
               if (Math.abs(currentPos.getX() - origin.getX()) > sizeX) {
                  return;
               }

               if (Math.abs(currentPos.getY() - origin.getY()) > sizeZ) {
                  return;
               }
               break;
            case WEST:
            case EAST:
               if (Math.abs(currentPos.getY() - origin.getY()) > sizeX) {
                  return;
               }

               if (Math.abs(currentPos.getZ() - origin.getZ()) > sizeZ) {
                  return;
               }
         }

         BlockState state = level.getBlockState(currentPos);
         if (state.equals(sourceState)
            && BlockUtils.isBlockExposed(level, currentPos)
            && !state.isAir()
            && state.getDestroySpeed(level, currentPos) >= 0.0F
            && level.mayInteract(player, currentPos)) {
            list.add(currentPos);

            for (Direction dir : Direction.values()) {
               if (dir != side && dir != side.getOpposite()) {
                  BlockPos nextPos = currentPos.relative(dir);
                  this.checkNeighbours(level, origin, sourceState, nextPos, side, sizeX, sizeY, sizeZ, list, player);
               }
            }
         }
      }
   }

   @Override
   public boolean showAxis(ItemStack stack, Level world, Player player, Direction side, IArchitect.EnumAxis axis) {
      int dim = WandManager.getAreaDim(stack);
      switch (side) {
         case DOWN:
         case UP:
            if (axis == IArchitect.EnumAxis.X && (dim == 0 || dim == 1) || axis == IArchitect.EnumAxis.Z && (dim == 0 || dim == 2)) {
               return true;
            }
            break;
         case NORTH:
         case SOUTH:
            if (axis == IArchitect.EnumAxis.Y && (dim == 0 || dim == 1) || axis == IArchitect.EnumAxis.X && (dim == 0 || dim == 2)) {
               return true;
            }
            break;
         case WEST:
         case EAST:
            if (axis == IArchitect.EnumAxis.Y && (dim == 0 || dim == 1) || axis == IArchitect.EnumAxis.Z && (dim == 0 || dim == 2)) {
               return true;
            }
      }

      return false;
   }
}
