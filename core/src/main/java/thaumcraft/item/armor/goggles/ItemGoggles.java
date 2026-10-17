package thaumcraft.item.armor.goggles;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial.Layer;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IGoggles;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.nodes.IRevealer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ItemGoggles extends ArmorItem implements IRepairable, IVisDiscountGear, IRevealer, IGoggles, IRunicArmor {
   public ItemGoggles() {
      super(ThaumcraftApi.ARMOR_MATERIAL_SPECIAL, Type.HELMET, new Properties().durability(350).rarity(Rarity.RARE));
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(
      @NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag
   ) {
      Level level = context.level();
      components.add(
         Component.literal(
            ChatFormatting.DARK_PURPLE
               + I18n.get("tc.visdiscount", new Object[0])
               + ": "
               + this.getVisDiscount(stack, Minecraft.getInstance().player, null)
               + "%"
         )
      );
   }

   @Nullable
   public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, Layer layer, boolean innerModel) {
      return ResourceLocation.parse("thaumcraft:textures/model/goggles.png");
   }

   public boolean isValidRepairItem(@NotNull ItemStack stack, @NotNull ItemStack itemStack) {
      return itemStack.is(Items.GOLD_INGOT) || super.isValidRepairItem(stack, itemStack);
   }

   @Override
   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   @Override
   public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
      return 5;
   }

   @Override
   public boolean showNodes(ItemStack stack, LivingEntity entity) {
      return true;
   }

   @Override
   public boolean showIngamePopups(ItemStack itemstack, LivingEntity player) {
      return true;
   }
}
