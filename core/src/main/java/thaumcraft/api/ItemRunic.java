package thaumcraft.api;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class ItemRunic extends Item implements IRunicArmor {
   int charge;

   public ItemRunic(int charge, Properties properties) {
      super(properties);
      this.charge = charge;
   }

   @Override
   public int getRunicCharge(ItemStack stack) {
      return this.charge;
   }
}
