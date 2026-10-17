package thaumcraft.item.equipment;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IWarpingGear;
import thaumcraft.api.ThaumcraftApi;

public class ItemVoidPickaxe extends PickaxeItem implements IRepairable, IWarpingGear {
   public ItemVoidPickaxe() {
      super(
         ThaumcraftApi.TOOL_MATERIAL_VOID,
         new Properties().rarity(Rarity.UNCOMMON).attributes(DiggerItem.createAttributes(ThaumcraftApi.TOOL_MATERIAL_VOID, 1.0F, -2.8F))
      );
   }

   @Override
   public int getWarp(ItemStack itemstack, Player player) {
      return 1;
   }
}
