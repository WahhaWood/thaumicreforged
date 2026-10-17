package thaumcraft.item.curio;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.NotNull;

public class CurioItem extends Item {
   public CurioItem() {
      super(new Properties());
   }

   public void verifyComponentsAfterLoad(@NotNull ItemStack stack) {
      super.verifyComponentsAfterLoad(stack);
      stripInvalidDamage(stack);
   }

   public static void stripInvalidDamage(ItemStack stack) {
      if (stack.has(DataComponents.DAMAGE) && !stack.has(DataComponents.MAX_DAMAGE)) {
         stack.remove(DataComponents.DAMAGE);
      }
   }
}
