package thaumcraft.item.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

public class ItemContainer extends BaseContainer {
   private final AbstractContainerMenu menu;

   public ItemContainer(AbstractContainerMenu menu, int containerSize) {
      super(containerSize);
      this.menu = menu;
   }

   public ItemContainer(AbstractContainerMenu menu, int containerSize, int maxStackSize) {
      super(containerSize, maxStackSize);
      this.menu = menu;
   }

   @Override
   public void setChanged() {
      this.menu.slotsChanged(this);
   }

   @Override
   public boolean stillValid(@NotNull Player player) {
      return true;
   }
}
