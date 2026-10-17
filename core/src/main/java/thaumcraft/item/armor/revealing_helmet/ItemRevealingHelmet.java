package thaumcraft.item.armor.revealing_helmet;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial.Layer;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IGoggles;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.nodes.IRevealer;
import thaumcraft.init.registry.ModItems;
import thaumcraft.util.ItemStackData;

public class ItemRevealingHelmet extends ArmorItem implements IRepairable, IRevealer, IGoggles, IVisDiscountGear {
   public ItemRevealingHelmet() {
      super(ThaumcraftApi.ARMOR_MATERIAL_THAUMIUM, Type.HELMET, new Properties().durability(500).rarity(Rarity.UNCOMMON));
   }

   @Override
   public boolean showNodes(ItemStack stack, LivingEntity living) {
      return true;
   }

   @Override
   public boolean showIngamePopups(ItemStack stack, LivingEntity living) {
      return true;
   }

   public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      Level level = context.level();
      tooltip.add(Component.translatable("tc.visdiscount").append(": 5%"));
   }

   @Nullable
   public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, Layer layer, boolean innerModel) {
      return ResourceLocation.parse("thaumcraft:textures/item/model/revealing_helmet.png");
   }

   public boolean isValidRepairItem(@NotNull ItemStack toRepair, @NotNull ItemStack repair) {
      ItemStack thaumium = new ItemStack((ItemLike)ModItems.THAUMIUM_INGOT.get());
      return ItemStackData.sameItemSameTags(repair, thaumium) || super.isValidRepairItem(toRepair, repair);
   }

   @Override
   public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
      return 5;
   }
}
