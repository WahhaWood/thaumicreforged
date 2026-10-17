package thaumcraft.item.armor.ichor_gem;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial.Layer;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import thaumcraft.item.armor.ichor.ItemIchorArmor;
import thaumcraft.util.ItemStackData;

public class ItemIchorArmorGem extends ItemIchorArmor {
   private static final String TAG_DISABLED = "gem_disabled";

   public ItemIchorArmorGem(Type type) {
      super(type);
   }

   public static boolean isEnabled(ItemStack stack) {
      return stack.isEmpty() ? false : !ItemStackData.getBoolean(stack, "gem_disabled");
   }

   public static void toggleEnabled(ItemStack stack) {
      boolean current = ItemStackData.getBoolean(stack, "gem_disabled");
      ItemStackData.getOrCreateTag(stack).putBoolean("gem_disabled", !current);
   }

   /**
    * Полный комплект пробуждённой брони на игроке: все четыре части надеты и у
    * каждой способности включены (Shift+ПКМ по вещи их выключает).
    *
    * <p>Пока комплект собран, владельца нельзя убить — см.
    * {@code GemArmorEvents#onLivingDeath}.
    */
   public static boolean isFullSetWorn(Player player) {
      return isPartEquipped(player, EquipmentSlot.HEAD)
         && isPartEquipped(player, EquipmentSlot.CHEST)
         && isPartEquipped(player, EquipmentSlot.LEGS)
         && isPartEquipped(player, EquipmentSlot.FEET);
   }

   private static boolean isPartEquipped(Player player, EquipmentSlot slot) {
      ItemStack stack = player.getItemBySlot(slot);
      return stack.getItem() instanceof ItemIchorArmorGem && isEnabled(stack);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (player.isShiftKeyDown()) {
         toggleEnabled(stack);
         if (!level.isClientSide) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, 0.1F);
         }

         player.displayClientMessage(Component.translatable("tc.awakened_armor." + (isEnabled(stack) ? "enabled" : "disabled")), true);
         return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
      } else {
         return super.use(level, player, hand);
      }
   }

   @Override
   public void appendHoverText(ItemStack stack, @Nullable TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      if (!isEnabled(stack)) {
         tooltip.add(Component.translatable("tc.awakened_armor.disabled"));
      } else {
         // Собран ли комплект на игроке, из тултипа не узнать: Item.TooltipContext
         // в 1.21.1 даёт только level(), а не владельца. Поэтому строка — описание
         // бонуса сета, а не индикатор его активности.
         tooltip.add(Component.translatable("tc.awakened_armor.immortal").withStyle(ChatFormatting.DARK_RED));
      }
   }

   @Nullable
   @Override
   public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, Layer layer, boolean innerModel) {
      return slot == EquipmentSlot.LEGS
         ? ResourceLocation.parse("thaumcraft:textures/item/model/ichor_gem_leggins.png")
         : ResourceLocation.parse("thaumcraft:textures/item/model/ichor_gem_armor.png");
   }
}
