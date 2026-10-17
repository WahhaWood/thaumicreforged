package thaumcraft.api.container;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.item.wands.ItemWandCasting;

public class SlotLimitedByWand extends Slot {
   public SlotLimitedByWand(Container container, int index, int x, int y) {
      super(container, index, x, y);
   }

   public boolean mayPlace(@NotNull ItemStack stack) {
      return stack.getItem() instanceof ItemWandCasting wand && !wand.isStaff(stack);
   }

   public int getMaxStackSize() {
      return 1;
   }
}
