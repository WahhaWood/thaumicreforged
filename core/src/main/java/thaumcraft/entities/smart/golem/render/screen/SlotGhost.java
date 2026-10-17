package thaumcraft.entities.smart.golem.render.screen;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotGhost extends Slot {
   private final int limit;

   public SlotGhost(Container container, int index, int x, int y) {
      this(container, index, x, y, 256);
   }

   public SlotGhost(Container container, int index, int x, int y, int limit) {
      super(container, index, x, y);
      this.limit = limit;
   }

   public boolean accept(ItemStack stack) {
      return true;
   }

   public final void set(ItemStack stack) {
      if (!stack.isEmpty()) {
         stack = stack.copy();
         stack.setCount(Math.min(stack.getCount(), this.getMaxStackSize()));
      }

      super.set(stack);
   }

   public int getMaxStackSize() {
      return this.limit;
   }

   public final boolean mayPlace(@NotNull ItemStack stack) {
      return false;
   }

   public final boolean mayPickup(@NotNull Player player) {
      return false;
   }
}
