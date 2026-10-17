package thaumcraft.item.xp_talisman;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.util.CuriosSlotUtils;
import thaumcraft.util.ItemStackData;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemXPTalisman extends Item implements ICurioItem {
   private static final String TAG_XP = "xp";
   private static final String TAG_ACTIVE = "active";
   private static final int MAX_XP = 1395;
   private static final int ENCHANTING_BOTTLE_COST = 11;
   private static final int RANGE = 6;

   public ItemXPTalisman() {
      super(new Properties().stacksTo(1).setNoRepair().rarity(Rarity.UNCOMMON));
   }

   public boolean canEquip(SlotContext slotContext, ItemStack stack) {
      return stack.getItem() == this && CuriosSlotUtils.isAmuletSlot(slotContext.identifier());
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (player.isShiftKeyDown()) {
         if (getXP(stack) < 1395) {
            setActive(stack, !isActive(stack));
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, isActive(stack) ? 1.1F : 0.85F);
         }

         return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
      } else {
         if (getXP(stack) >= 11) {
            int bottleSlot = player.getInventory().findSlotMatchingItem(new ItemStack(Items.GLASS_BOTTLE));
            if (bottleSlot >= 0) {
               player.getInventory().removeItem(bottleSlot, 1);
               ItemStack xpBottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
               if (!player.getInventory().add(xpBottle) && !level.isClientSide) {
                  player.drop(xpBottle, false);
               }

               setXP(stack, getXP(stack) - 11);
               level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.2F, 1.35F);
               return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
         }

         return InteractionResultHolder.pass(stack);
      }
   }

   public float getXpRepairRatio(ItemStack stack) {
      return getXP(stack) > 0 ? 1.0F : 0.0F;
   }

   @Nullable
   public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
      return itemStack.copy();
   }

   public boolean hasCraftingRemainingItem() {
      return true;
   }

   public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      Level level = context.level();
      tooltip.add(Component.literal("XP: " + getXP(stack)).withStyle(ChatFormatting.GOLD));
      if (getXP(stack) >= 1395) {
         tooltip.add(Component.translatable("item.thaumcraft.xp_talisman.full").withStyle(ChatFormatting.YELLOW));
      } else {
         tooltip.add(
            Component.translatable(isActive(stack) ? "item.thaumcraft.xp_talisman.absorbing" : "item.thaumcraft.xp_talisman.not_absorbing")
               .withStyle(isActive(stack) ? ChatFormatting.GREEN : ChatFormatting.GRAY)
         );
      }
   }

   public static int getXP(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      return tag == null ? 0 : tag.getInt("xp");
   }

   public static void setXP(ItemStack stack, int xp) {
      CompoundTag tag = ItemStackData.getOrCreateTag(stack);
      tag.putInt("xp", Math.max(0, xp));
      ItemStackData.setTag(stack, tag);
   }

   public static boolean isActive(ItemStack stack) {
      CompoundTag tag = ItemStackData.getTag(stack);
      return tag != null && tag.getBoolean("active");
   }

   public static void setActive(ItemStack stack, boolean active) {
      CompoundTag tag = ItemStackData.getOrCreateTag(stack);
      tag.putBoolean("active", active);
      ItemStackData.setTag(stack, tag);
   }
}
