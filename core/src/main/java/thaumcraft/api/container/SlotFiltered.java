package thaumcraft.api.container;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotFiltered extends Slot {
   public SlotFiltered(Container inventory, int index, int x, int y) {
      super(inventory, index, x, y);
   }

   public boolean mayPlace(@NotNull ItemStack stack) {
      return this.container.canPlaceItem(this.getSlotIndex(), stack);
   }
}
