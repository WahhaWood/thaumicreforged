package thaumcraft.item.armor.thaumium;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial.Layer;
import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.ThaumcraftApi;

public class ItemThaumiumArmor extends ArmorItem implements IRepairable, IRunicArmor {
   public ItemThaumiumArmor(Type type) {
      super(ThaumcraftApi.ARMOR_MATERIAL_THAUMIUM, type, new Properties().rarity(Rarity.UNCOMMON));
   }

   @Override
   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   @Nullable
   public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, Layer layer, boolean innerModel) {
      return slot == EquipmentSlot.LEGS
         ? ResourceLocation.parse("thaumcraft:textures/model/thaumium_2.png")
         : ResourceLocation.parse("thaumcraft:textures/model/thaumium_1.png");
   }
}
