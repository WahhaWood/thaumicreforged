package thaumcraft.item.wands.foci;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.util.ItemStackData;

public class ItemFocusRecall extends ItemFocusBasic {
   private static final AspectList visUsage = new AspectList().add(Aspect.AIR, 4000).add(Aspect.EARTH, 4000).add(Aspect.ORDER, 4000);
   private static final String TAG_ATTUNED = "recall_attuned";
   private static final String TAG_X = "recall_x";
   private static final String TAG_Y = "recall_y";
   private static final String TAG_Z = "recall_z";
   private static final String TAG_DIM = "recall_dim";

   public ItemFocusRecall() {
      super("recall", Rarity.EPIC);
   }

   @Override
   public String getSortingHelper(ItemStack itemstack) {
      return "RECALL" + super.getSortingHelper(itemstack);
   }

   @Override
   public int getFocusColor(ItemStack stack) {
      return 10287359;
   }

   @Override
   public AspectList getVisCost(ItemStack stack) {
      return visUsage;
   }

   @Override
   public ItemFocusBasic.WandFocusAnimation getAnimation(ItemStack stack) {
      return ItemFocusBasic.WandFocusAnimation.CHARGE;
   }

   @Override
   public ItemStack onFocusRightClick(ItemStack wandStack, Level level, Player player, InteractionHand hand) {
      ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
      ItemStack focusItem = wand.getFocusItem(wandStack);
      if (player.isShiftKeyDown()) {
         if (!level.isClientSide) {
            attuneFocus(focusItem, player.blockPosition(), level.dimension());
            wand.setFocus(wandStack, focusItem);
            player.displayClientMessage(Component.translatable("ttmisc.recall.attuned").withStyle(ChatFormatting.GOLD), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5F, 1.5F);
         }

         return wandStack;
      } else {
         player.startUsingItem(hand);
         return wandStack;
      }
   }

   @Override
   public void onUsingFocusTick(ItemStack wandStack, Player player, int count) {
      // Стиль TC4: прошедшие тики из переданного остатка (getTicksUsingItem у фейков стоит).
      int ticksUsed = wandStack.getUseDuration(player) - count;
      if (ticksUsed >= 60) {
         ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();
         ItemStack focusItem = wand.getFocusItem(wandStack);
         if (!isAttuned(focusItem)) {
            player.stopUsingItem();
            if (!player.level().isClientSide) {
               player.displayClientMessage(Component.translatable("ttmisc.recall.not_attuned").withStyle(ChatFormatting.RED), true);
            }
         } else {
            ResourceLocation dimId = getDimension(focusItem);
            ResourceLocation currentDim = player.level().dimension().location();
            if (!dimId.equals(currentDim)) {
               player.stopUsingItem();
               if (!player.level().isClientSide) {
                  player.displayClientMessage(Component.translatable("ttmisc.recall.wrong_dimension").withStyle(ChatFormatting.RED), true);
               }
            } else {
               if (!player.level().isClientSide && player.level() instanceof net.minecraft.server.level.ServerLevel targetLevel) {
                  BlockPos target = getTarget(focusItem);
                  if (wand.consumeAllVis(wandStack, player, this.getVisCost(focusItem), true, false)) {
                     double x = target.getX() + 0.5;
                     double y = target.getY() + 0.5;
                     double z = target.getZ() + 0.5;
                     if (player instanceof net.neoforged.neoforge.common.util.FakePlayer) {
                        // FakePlayer переопределяет оба teleportTo в no-op — двигаем напрямую.
                        player.setPos(x, y, z);
                        player.fallDistance = 0.0F;
                     } else if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        serverPlayer.teleportTo(targetLevel, x, y, z, player.getYRot(), player.getXRot());
                     }

                     targetLevel.playSound(
                        null, x, y, z,
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5F, 1.5F
                     );
                  }
               }

               player.stopUsingItem();
            }
         }
      }
   }

   @Override
   public void addFocusInformation(ItemStack stack, Level level, List<Component> list, TooltipFlag flag) {
      super.addFocusInformation(stack, level, list, flag);
      if (isAttuned(stack)) {
         BlockPos pos = getTarget(stack);
         ResourceLocation dim = getDimension(stack);
         list.add(
            Component.literal(" ")
               .append(Component.translatable("ttmisc.recall.target").withStyle(ChatFormatting.GOLD))
               .append(Component.literal(" " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).withStyle(ChatFormatting.WHITE))
         );
         list.add(
            Component.literal(" ")
               .append(Component.translatable("ttmisc.recall.dimension").withStyle(ChatFormatting.GOLD))
               .append(Component.literal(" " + dim.getPath()).withStyle(ChatFormatting.WHITE))
         );
      } else {
         list.add(Component.translatable("ttmisc.recall.not_attuned").withStyle(ChatFormatting.GRAY));
      }
   }

   public static void attuneFocus(ItemStack focus, BlockPos pos, ResourceKey<Level> dimension) {
      CompoundTag tag = ItemStackData.getOrCreateTag(focus);
      tag.putBoolean("recall_attuned", true);
      tag.putInt("recall_x", pos.getX());
      tag.putInt("recall_y", pos.getY());
      tag.putInt("recall_z", pos.getZ());
      tag.putString("recall_dim", dimension.location().toString());
   }

   public static boolean isAttuned(ItemStack focus) {
      CompoundTag tag = ItemStackData.getTag(focus);
      return tag != null && tag.getBoolean("recall_attuned");
   }

   public static BlockPos getTarget(ItemStack focus) {
      CompoundTag tag = ItemStackData.getTag(focus);
      return tag == null ? BlockPos.ZERO : new BlockPos(tag.getInt("recall_x"), tag.getInt("recall_y"), tag.getInt("recall_z"));
   }

   public static ResourceLocation getDimension(ItemStack focus) {
      CompoundTag tag = ItemStackData.getTag(focus);
      return tag == null ? Level.OVERWORLD.location() : ResourceLocation.parse(tag.getString("recall_dim"));
   }

   @Override
   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemStack, int rank) {
      return switch (rank) {
         case 1, 2, 3, 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal};
         default -> null;
      };
   }
}
