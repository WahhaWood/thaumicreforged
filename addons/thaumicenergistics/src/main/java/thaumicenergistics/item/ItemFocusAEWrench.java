package thaumicenergistics.item;

import appeng.api.parts.IPartHost;
import appeng.core.definitions.AEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.block.arcane_pedestal.ArcanePedestalBlockEntity;
import thaumcraft.block.wand_recharger.WandPedestalBlockEntity;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.util.BlockUtils;

public class ItemFocusAEWrench extends ItemFocusBasic {
   private static final AspectList CAST_COST = new AspectList();
   private static ItemStack PSEUDO_WRENCH = ItemStack.EMPTY;

   public ItemFocusAEWrench() {
      super("aewrench");
   }

   private static ItemStack getWrench() {
      if (PSEUDO_WRENCH.isEmpty()) {
         PSEUDO_WRENCH = AEItems.CERTUS_QUARTZ_WRENCH.stack();
      }

      return PSEUDO_WRENCH;
   }

   private static ItemWandCasting wandIfValid(ItemStack stack, Player player) {
      if (stack.getItem() instanceof ItemWandCasting wand) {
         return !wand.consumeAllVis(stack, player, CAST_COST, false, false) ? null : wand;
      } else {
         return null;
      }
   }

   private static double getReach(Player player) {
      AttributeInstance attr = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
      return attr != null ? attr.getValue() : 5.0;
   }

   private static BlockHitResult getHitResult(Level level, Player player, BlockPos pos, Direction direction) {
      BlockHitResult hit = BlockUtils.getTargetBlock(level, player, getReach(player));
      if (hit.getType() == Type.BLOCK && hit.getBlockPos().equals(pos)) {
         return hit;
      } else {
         Vec3 center = Vec3.atCenterOf(pos);
         Direction face = direction != null ? direction : Direction.UP;
         return new BlockHitResult(center, face, pos, false);
      }
   }

   private static void consumeVisAndSpawnBeam(ItemWandCasting wand, ItemStack wandStack, Player player, Vec3 target) {
      if (player.level().isClientSide) {
         ThaumcraftApi.getSetup().beamCont(player.level(), player, target.x, target.y, target.z, 2, Aspect.ENERGY.getColor(), false, 0.0F, null, 0);
      } else {
         wand.consumeAllVis(wandStack, player, CAST_COST, true, false);
      }
   }

   private boolean useWrench(ServerPlayer player, BlockHitResult hit, boolean forceSneak) {
      ItemStack wrench = getWrench();
      if (wrench.isEmpty()) {
         return false;
      } else {
         ItemStack prevHolding = player.getMainHandItem();
         boolean prevSneak = player.isShiftKeyDown();

         boolean var8;
         try {
            player.setItemInHand(InteractionHand.MAIN_HAND, wrench.copy());
            if (prevSneak != forceSneak) {
               player.setShiftKeyDown(forceSneak);
            }

            InteractionResult result = player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
            var8 = result.consumesAction() || player.level().isEmptyBlock(hit.getBlockPos());
         } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, prevHolding);
            player.setShiftKeyDown(prevSneak);
         }

         return var8;
      }
   }

   private boolean onUse(Level level, Player player, BlockHitResult hit, ItemStack wandStack, boolean rightClick) {
      if (!player.isShiftKeyDown()) {
         return false;
      } else {
         ItemWandCasting wand = wandIfValid(wandStack, player);
         if (wand == null) {
            return false;
         } else {
            BlockPos pos = hit.getBlockPos();
            if (!level.mayInteract(player, pos)) {
               return false;
            } else {
               BlockEntity be = level.getBlockEntity(pos);
               if (be == null) {
                  return false;
               } else if (!(be instanceof ArcanePedestalBlockEntity) && !(be instanceof WandPedestalBlockEntity)) {
                  if (rightClick && be.getClass().getName().equals("cofh.thermalexpansion.block.cache.TileCache")) {
                     return false;
                  } else {
                     boolean isPartHost = be instanceof IPartHost;
                     if (isPartHost && rightClick) {
                        if (false) {
                           return false;
                        } else if (level.isClientSide) {
                           consumeVisAndSpawnBeam(wand, wandStack, player, hit.getLocation());
                           return true;
                        } else if (player instanceof ServerPlayer serverPlayer) {
                           this.useWrench(serverPlayer, hit, true);
                           consumeVisAndSpawnBeam(wand, wandStack, player, hit.getLocation());
                           return true;
                        } else {
                           return false;
                        }
                     } else if (!rightClick && !isPartHost) {
                        BlockState state = level.getBlockState(pos);
                        BlockState rotated = state.rotate(level, pos, Rotation.CLOCKWISE_90);
                        if (rotated != state) {
                           if (!level.isClientSide) {
                              level.setBlock(pos, rotated, 3);
                              level.updateNeighborsAt(pos, state.getBlock());
                           }

                           consumeVisAndSpawnBeam(wand, wandStack, player, hit.getLocation());
                        }

                        return true;
                     } else if (level.isClientSide) {
                        consumeVisAndSpawnBeam(wand, wandStack, player, hit.getLocation());
                        return true;
                     } else if (!(player instanceof ServerPlayer serverPlayer)) {
                        return false;
                     } else {
                        boolean forceSneak = !isPartHost || rightClick;
                        boolean handled = this.useWrench(serverPlayer, hit, forceSneak);
                        if (handled) {
                           consumeVisAndSpawnBeam(wand, wandStack, player, hit.getLocation());
                        }

                        return handled;
                     }
                  }
               } else {
                  return false;
               }
            }
         }
      }
   }

   public int getActivationCooldown(ItemStack focusstack) {
      return 250;
   }

   public int getFocusColor(ItemStack itemStack) {
      return Aspect.ENERGY.getColor();
   }

   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemStack, int rank) {
      return new FocusUpgradeType[0];
   }

   public AspectList getVisCost(ItemStack itemStack) {
      return CAST_COST;
   }

   public ItemStack onFocusBlockClick(ItemStack wandStack, Level level, Player player, InteractionHand hand, BlockPos blockPos, Direction direction) {
      BlockHitResult hit = getHitResult(level, player, blockPos, direction);
      return this.onUse(level, player, hit, wandStack, true) ? wandStack : ItemStack.EMPTY;
   }

   public boolean onFocusBlockStartBreak(ItemStack wandStack, BlockPos blockPos, Player player) {
      BlockHitResult hit = getHitResult(player.level(), player, blockPos, null);
      return this.onUse(player.level(), player, hit, wandStack, false);
   }

   static {
      CAST_COST.add(Aspect.FIRE, 10);
      CAST_COST.add(Aspect.AIR, 10);
   }
}
