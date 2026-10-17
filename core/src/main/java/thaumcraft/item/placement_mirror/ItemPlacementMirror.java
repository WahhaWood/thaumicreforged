package thaumcraft.item.placement_mirror;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.item.block_talisman.ItemBlockTalisman;
import thaumcraft.util.ItemStackData;

public class ItemPlacementMirror extends Item {
   private static final String TAG_PROTOTYPE = "prototype";
   private static final String TAG_SIZE = "size";
   private static final double REACH_DISTANCE = 64.0;

   public ItemPlacementMirror() {
      super(new Properties().stacksTo(1).rarity(Rarity.RARE));
   }

   @NotNull
   public InteractionResult useOn(UseOnContext context) {
      Player player = context.getPlayer();
      if (player == null) {
         return InteractionResult.PASS;
      } else {
         ItemStack mirrorStack = context.getItemInHand();
         Level level = context.getLevel();
         BlockPos pos = context.getClickedPos();
         if (player.isShiftKeyDown()) {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()) {
               BlockHitResult hitResult = hitResult(context);
               ItemStack clone = state.getCloneItemStack(hitResult, level, pos, player);
               if (!clone.isEmpty()) {
                  setPrototype(mirrorStack, clone);
                  level.playSound(player, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, 0.5F);
                  if (level.isClientSide) {
                     player.displayClientMessage(Component.translatable("thaumcraft.tooltip.mirror_bound", new Object[]{clone.getHoverName()}), true);
                  }

                  return InteractionResult.sidedSuccess(level.isClientSide);
               }
            }

            return InteractionResult.FAIL;
         } else if (this.placeAllBlocks(mirrorStack, player, hitResult(context))) {
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(level.isClientSide);
         } else {
            return InteractionResult.FAIL;
         }
      }
   }

   private static BlockHitResult hitResult(UseOnContext context) {
      return new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (player.isShiftKeyDown()) {
         int size = getSize(stack);
         int newSize = size >= 9 ? 1 : size + 2;
         setSize(stack, newSize);
         if (level.isClientSide) {
            player.displayClientMessage(Component.literal("Grid: " + newSize + "x" + newSize), true);
         }

         level.playSound(null, player.blockPosition(), (SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.3F, 1.0F);
         return InteractionResultHolder.success(stack);
      } else {
         BlockHitResult rayTrace = getPlayerPOVHitResult(level, player, Fluid.NONE, 64.0);
         if (rayTrace.getType() == Type.BLOCK && this.placeAllBlocks(stack, player, rayTrace)) {
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.swing(hand);
            return InteractionResultHolder.success(stack);
         } else {
            return InteractionResultHolder.pass(stack);
         }
      }
   }

   private boolean placeAllBlocks(ItemStack mirror, Player player, BlockHitResult originHit) {
      ItemStack prototype = getPrototype(mirror);
      if (!prototype.isEmpty() && prototype.getItem() instanceof BlockItem) {
         List<BlockPos> targets = getTargetPositions(mirror, player, originHit);
         if (targets.isEmpty()) {
            return false;
         } else {
            boolean anyPlaced = false;

            for (BlockPos targetPos : targets) {
               if (!hasMaterial(player, prototype)) {
                  break;
               }

               final ItemStack placeStack = prototype.copy();
               placeStack.setCount(1);
               BlockHitResult targetHit = new BlockHitResult(originHit.getLocation(), originHit.getDirection(), targetPos, false);
               UseOnContext placeContext = new UseOnContext(player, InteractionHand.MAIN_HAND, targetHit) {
                  @NotNull
                  public ItemStack getItemInHand() {
                     return placeStack;
                  }
               };
               InteractionResult result = ((BlockItem)prototype.getItem()).useOn(placeContext);
               if (result.consumesAction()) {
                  anyPlaced = true;
                  if (!player.getAbilities().instabuild) {
                     this.consumeMaterial(player, prototype);
                  }
               }
            }

            return anyPlaced;
         }
      } else {
         return false;
      }
   }

   public static List<BlockPos> getTargetPositions(ItemStack stack, Player player, BlockHitResult hit) {
      List<BlockPos> out = new ArrayList<>();
      Level level = player.level();
      BlockPos center = hit.getBlockPos();
      Direction face = hit.getDirection();
      BlockState centerState = level.getBlockState(center);
      if (!centerState.canBeReplaced()) {
         center = center.relative(face);
      }

      int size = getSize(stack);
      int range = (size - 1) / 2;
      int xRange = 0;
      int yRange = 0;
      int zRange = 0;
      if (face.getAxis() != Axis.Y) {
         if (face.getAxis() == Axis.X) {
            yRange = range;
            zRange = range;
         } else {
            xRange = range;
            yRange = range;
         }
      } else {
         boolean isLookingVertical = Math.abs(player.getXRot()) > 75.0F;
         if (isLookingVertical) {
            xRange = range;
            zRange = range;
         } else {
            yRange = range;
            Direction playerFacing = player.getDirection();
            if (playerFacing.getAxis() == Axis.X) {
               zRange = range;
            } else {
               xRange = range;
            }
         }
      }

      for (int x = -xRange; x <= xRange; x++) {
         for (int y = -yRange; y <= yRange; y++) {
            for (int z = -zRange; z <= zRange; z++) {
               BlockPos p = center.offset(x, y, z);
               BlockState bs = level.getBlockState(p);
               if (bs.canBeReplaced() || bs.isAir()) {
                  out.add(p);
               }
            }
         }
      }

      return out;
   }

   protected static BlockHitResult getPlayerPOVHitResult(Level level, Player player, Fluid fluidMode, double range) {
      float xRot = player.getXRot();
      float yRot = player.getYRot();
      Vec3 startVec = player.getEyePosition();
      Vec3 lookVec = player.getLookAngle();
      Vec3 endVec = startVec.add(lookVec.x * range, lookVec.y * range, lookVec.z * range);
      return level.clip(new ClipContext(startVec, endVec, Block.OUTLINE, fluidMode, player));
   }

   public static boolean hasMaterial(Player player, ItemStack prototype) {
      if (player.getAbilities().instabuild) {
         return true;
      } else {
         for (ItemStack s : player.getInventory().items) {
            if (!s.isEmpty() && ItemStackData.sameItemSameTags(s, prototype)) {
               return true;
            }
         }

         for (ItemStack sx : player.getInventory().items) {
            if (!sx.isEmpty() && sx.getItem() instanceof ItemBlockTalisman) {
               ItemStack stored = ItemBlockTalisman.getStoredItem(sx);
               if (ItemStackData.sameItemSameTags(stored, prototype) && ItemBlockTalisman.getStoredCount(sx) > 0) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private void consumeMaterial(Player player, ItemStack prototype) {
      for (int i = 0; i < player.getInventory().items.size(); i++) {
         ItemStack s = (ItemStack)player.getInventory().items.get(i);
         if (!s.isEmpty() && ItemStackData.sameItemSameTags(s, prototype)) {
            s.shrink(1);
            if (s.isEmpty()) {
               player.getInventory().items.set(i, ItemStack.EMPTY);
            }

            return;
         }
      }

      for (ItemStack s : player.getInventory().items) {
         if (!s.isEmpty() && s.getItem() instanceof ItemBlockTalisman) {
            ItemStack stored = ItemBlockTalisman.getStoredItem(s);
            if (ItemStackData.sameItemSameTags(stored, prototype)) {
               int count = ItemBlockTalisman.getStoredCount(s);
               if (count > 0) {
                  ItemBlockTalisman.setStoredCount(s, count - 1);
                  return;
               }
            }
         }
      }
   }

   private static void setSize(ItemStack stack, int size) {
      CompoundTag tag = ItemStackData.getOrCreateTag(stack);
      tag.putInt("size", size);
      ItemStackData.setTag(stack, tag);
   }

   public static int getSize(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      int s = tag == null ? 0 : tag.getInt("size");
      return s == 0 ? 3 : s;
   }

   private static void setPrototype(ItemStack stack, ItemStack prototype) {
      CompoundTag tag = ItemStackData.getOrCreateTag(stack);
      tag.put("prototype", ItemStackData.saveStack(prototype));
      ItemStackData.setTag(stack, tag);
   }

   public static ItemStack getPrototype(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      return tag != null && tag.contains("prototype") ? ItemStackData.loadStack(tag.getCompound("prototype")) : ItemStack.EMPTY;
   }

   public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      Level level = context.level();
      int size = getSize(stack);
      tooltip.add(Component.literal("Area: " + size + " x " + size).withStyle(ChatFormatting.AQUA));
      ItemStack proto = getPrototype(stack);
      if (!proto.isEmpty()) {
         tooltip.add(Component.literal("Bound: ").append(proto.getHoverName()).withStyle(ChatFormatting.GOLD));
      } else {
         tooltip.add(Component.literal("Shift+Right Click block to bind").withStyle(ChatFormatting.GRAY));
      }
   }
}
