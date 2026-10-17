package thaumcraft.api.container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.entities.smart.golem.render.screen.SlotGhost;
import thaumcraft.init.registry.ModMenuTypes;

public class GhostSlotsMenu extends AbstractContainerMenu {
   public GhostSlotsMenu(int windowsId, Inventory inventory, FriendlyByteBuf buf) {
      super((MenuType)ModMenuTypes.GHOST_SLOTS.get(), windowsId);
   }

   public boolean stillValid(@NotNull Player player) {
      return false;
   }

   public void clicked(int index, int button, @NotNull ClickType clickType, @NotNull Player player) {
      if (index >= 0 && index < this.slots.size()) {
         Slot slot = this.getSlot(index);
         if (slot instanceof SlotGhost ghost) {
            ItemStack stack = this.getCarried();
            if (stack.isEmpty()) {
               slot.set(ItemStack.EMPTY);
            } else if (ghost.accept(stack)) {
               slot.set(stack);
            }

            return;
         }
      }

      super.clicked(index, button, clickType, player);
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int index) {
      if (index >= 0 && index < this.slots.size()) {
         Slot slot = this.getSlot(index);
         if (slot.container == player.getInventory()) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
               for (Slot s : this.slots) {
                  if (s instanceof SlotGhost ghost && !ghost.hasItem() && ghost.accept(stack)) {
                     s.set(stack);
                     break;
                  }
               }
            }
         } else if (slot instanceof SlotGhost) {
            slot.set(ItemStack.EMPTY);
         }
      }

      return ItemStack.EMPTY;
   }
}
