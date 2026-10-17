package thaumcraft.item.equipment;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IWarpingGear;
import thaumcraft.api.ThaumcraftApi;

public class ItemVoidHoe extends HoeItem implements IRepairable, IWarpingGear {
   public ItemVoidHoe() {
      super(
         ThaumcraftApi.TOOL_MATERIAL_VOID,
         new Properties().rarity(Rarity.UNCOMMON).attributes(DiggerItem.createAttributes(ThaumcraftApi.TOOL_MATERIAL_VOID, -3.0F, 0.0F))
      );
   }

   public int getEnchantmentValue() {
      return 5;
   }

   @Override
   public int getWarp(ItemStack itemstack, Player player) {
      return 1;
   }
}
