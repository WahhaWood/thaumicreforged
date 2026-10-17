package thaumcraft.item.armor.ichor;

import java.util.EnumMap;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial.Layer;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.material.ArmorMaterialImpl;
import thaumcraft.init.registry.ModItems;

public class ItemIchorArmor extends ArmorItem implements IVisDiscountGear {
   public static final Holder<ArmorMaterial> ARMOR_MATERIAL_ICHOR = Holder.direct(
      ArmorMaterialImpl.create("ICHOR", 0, (EnumMap<Type, Integer>)Util.make(new EnumMap(Type.class), map -> {
         map.put(Type.BOOTS, 4);
         map.put(Type.LEGGINGS, 7);
         map.put(Type.CHESTPLATE, 10);
         map.put(Type.HELMET, 5);
      }), 30, 5.0F, 0.3F, () -> Ingredient.of(new ItemLike[]{(ItemLike)ModItems.ICHOR_CLOTH.get()}))
   );

   public ItemIchorArmor(Type type) {
      super(ARMOR_MATERIAL_ICHOR, type, new Properties().rarity(Rarity.EPIC).fireResistant());
   }

   public boolean isDamageable(ItemStack stack) {
      return false;
   }

   public boolean isEnchantable(ItemStack stack) {
      return true;
   }

   @Override
   public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
      return this.type == Type.BOOTS ? 3 : 4;
   }

   @Nullable
   public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, Layer layer, boolean innerModel) {
      return slot == EquipmentSlot.LEGS
         ? ResourceLocation.parse("thaumcraft:textures/item/model/ichor_boots.png")
         : ResourceLocation.parse("thaumcraft:textures/item/model/ichor_armor.png");
   }

   public void appendHoverText(ItemStack stack, @Nullable TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      Level level = context.level();
      tooltip.add(Component.translatable("tc.visdiscount").append(": ").append(Component.literal(this.getVisDiscount(stack, null, null) + "%")));
   }
}
