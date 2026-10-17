package thaumcraft.item.spell_cloth;

import java.awt.Color;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;

public class ItemSpellCloth extends Item {
   public static final int MAX_USES = 40;

   public ItemSpellCloth() {
      super(new Properties().durability(40).setNoRepair().rarity(Rarity.UNCOMMON));
   }

   public static int colorFromStack(ItemStack stack, int tint) {
      if (tint != 0) {
         return 16777215;
      } else {
         int dmg = stack.getDamageValue();
         float max = stack.getMaxDamage();
         float saturation = (max - dmg) / max * 0.5F;
         return Color.HSBtoRGB(0.75F, saturation, 1.0F);
      }
   }

   public boolean hasCraftingRemainingItem(ItemStack stack) {
      return true;
   }

   public ItemStack getCraftingRemainingItem(ItemStack stack) {
      ItemStack copy = stack.copy();
      copy.setDamageValue(copy.getDamageValue() + 1);
      return copy.getDamageValue() >= copy.getMaxDamage() ? ItemStack.EMPTY : copy;
   }
}
