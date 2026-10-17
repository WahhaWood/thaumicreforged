package thaumcraft.item.relics;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;

public class ItemSanityChecker extends Item {
   public ItemSanityChecker() {
      super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
   }
}
