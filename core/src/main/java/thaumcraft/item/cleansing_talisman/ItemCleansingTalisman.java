package thaumcraft.item.cleansing_talisman;

import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.init.registry.ModMobEffect;
import thaumcraft.util.CuriosSlotUtils;
import thaumcraft.util.ItemStackData;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemCleansingTalisman extends Item implements ICurioItem {
   private static final String TAG_ACTIVE = "active";
   private static final Set<Holder<MobEffect>> HIGH_COST_WARP_EFFECTS = Set.of(
      ModMobEffect.BLURRED, ModMobEffect.DEATH_GAZE, ModMobEffect.INFECTIOUS_VIS_EXHAUST, ModMobEffect.SUN_SCORNED, ModMobEffect.UNNATURAL_HUNGER
   );

   public ItemCleansingTalisman() {
      super(new Properties().stacksTo(1).durability(100).setNoRepair().rarity(Rarity.UNCOMMON));
   }

   public boolean canEquip(SlotContext slotContext, ItemStack stack) {
      return stack.getItem() == this && CuriosSlotUtils.isAmuletSlot(slotContext.identifier());
   }

   public void curioTick(SlotContext slotContext, ItemStack stack) {
      if (slotContext.entity() instanceof net.minecraft.world.entity.LivingEntity living) {
         this.tickCleansing(stack, living.level(), living);
      }
   }

   @Override
   public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
      if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
         this.tickCleansing(stack, level, living);
      }
   }

   private void tickCleansing(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity living) {
      if (!level.isClientSide && living.tickCount % 20 == 0 && isActive(stack) && !isBroken(stack)) {
         for (net.minecraft.world.effect.MobEffectInstance inst : living.getActiveEffects()) {
            Holder<MobEffect> effect = inst.getEffect();
            boolean isHighCost = HIGH_COST_WARP_EFFECTS.contains(effect);
            boolean isHarmful = effect.value().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL;
            if (isHighCost || isHarmful) {
               int cost = isHighCost ? 5 : 1;
               living.removeEffect(effect);
               int newDamage = stack.getDamageValue() + cost;
               stack.setDamageValue(newDamage);
               if (isBroken(stack)) {
                  setActive(stack, false);
               }
               level.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 1.2F);
               break;
            }
         }
      }
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (player.isShiftKeyDown() && !isBroken(stack)) {
         boolean newState = !isActive(stack);
         setActive(stack, newState);
         level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, newState ? 1.1F : 0.85F);
         return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
      } else {
         return InteractionResultHolder.pass(stack);
      }
   }

   public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      Level level = context.level();
      tooltip.add(
         Component.translatable(isActive(stack) ? "item.thaumcraft.cleansing_talisman.active" : "item.thaumcraft.cleansing_talisman.inactive")
            .withStyle(isActive(stack) ? ChatFormatting.GREEN : ChatFormatting.GRAY)
      );
      if (isBroken(stack)) {
         tooltip.add(Component.translatable("item.thaumcraft.cleansing_talisman.broken").withStyle(ChatFormatting.RED));
      }
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

   public static boolean isBroken(ItemStack stack) {
      return stack.getDamageValue() >= stack.getMaxDamage();
   }
}
