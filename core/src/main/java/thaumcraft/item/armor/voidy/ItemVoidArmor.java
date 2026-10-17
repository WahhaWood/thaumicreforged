package thaumcraft.item.armor.voidy;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial.Layer;
import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.IWarpingGear;
import thaumcraft.api.ThaumcraftApi;

public class ItemVoidArmor extends ArmorItem implements IRepairable, IRunicArmor, IWarpingGear {
   public ItemVoidArmor(Type type) {
      super(ThaumcraftApi.ARMOR_MATERIAL_VOID, type, new Properties().rarity(Rarity.UNCOMMON));
   }

   @Nullable
   public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, Layer layer, boolean innerModel) {
      return this.type == Type.LEGGINGS
         ? ResourceLocation.parse("thaumcraft:textures/model/void_2.png")
         : ResourceLocation.parse("thaumcraft:textures/model/void_1.png");
   }

   @Override
   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   @Override
   public int getWarp(ItemStack itemstack, Player player) {
      return 1;
   }
}
