package thaumcraft.block.arcane_workbench;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class DummyMenu extends AbstractContainerMenu {
   public static final DummyMenu INSTANCE = new DummyMenu();

   private DummyMenu() {
      super(null, 0);
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int index) {
      return ItemStack.EMPTY;
   }

   public boolean stillValid(@NotNull Player player) {
      return false;
   }
}
