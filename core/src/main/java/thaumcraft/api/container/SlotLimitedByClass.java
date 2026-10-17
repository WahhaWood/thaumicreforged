package thaumcraft.api.container;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotLimitedByClass extends Slot {
   Class<?> clazz;
   int limit;

   public SlotLimitedByClass(Class<?> clazz, Container inventory, int index, int x, int y) {
      super(inventory, index, x, y);
      this.clazz = clazz;
      this.limit = 64;
   }

   public SlotLimitedByClass(Class<?> clazz, int limit, Container inventory, int index, int x, int y) {
      super(inventory, index, x, y);
      this.clazz = clazz;
      this.limit = limit;
   }

   public boolean mayPlace(ItemStack stack) {
      return !stack.isEmpty() && this.clazz.isInstance(stack.getItem());
   }

   public int getMaxStackSize() {
      return this.limit;
   }
}
