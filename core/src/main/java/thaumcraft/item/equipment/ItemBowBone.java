package thaumcraft.item.equipment;

import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item.Properties;
import thaumcraft.api.IRepairable;

public class ItemBowBone extends BowItem implements IRepairable {
   public ItemBowBone() {
      super(new Properties().stacksTo(1).durability(512));
   }

   public int getEnchantmentValue() {
      return 3;
   }
}
