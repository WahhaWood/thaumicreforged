package thaumcraft.item.bath_salts;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class ItemBathSalts extends Item {
   public ItemBathSalts() {
      super(new Properties());
   }

   public int getEntityLifespan(ItemStack itemStack, Level level) {
      return 200;
   }
}
