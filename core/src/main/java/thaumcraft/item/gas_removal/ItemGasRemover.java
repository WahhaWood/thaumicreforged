package thaumcraft.item.gas_removal;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;

public class ItemGasRemover extends Item {
   public ItemGasRemover() {
      super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
   }
}
