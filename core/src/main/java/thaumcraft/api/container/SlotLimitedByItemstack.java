package thaumcraft.api.container;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotLimitedByItemstack extends Slot {
   private final ItemStack filter;

   public SlotLimitedByItemstack(ItemStack filter, Container container, int index, int x, int y) {
      super(container, index, x, y);
      this.filter = filter;
   }

   public boolean mayPlace(@NotNull ItemStack stack) {
      return ItemStack.isSameItem(this.filter, stack);
   }
}
