package thaumcraft.block.spa;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.container.SlotLimitedByClass;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.item.bath_salts.ItemBathSalts;

public class SpaMenu extends AbstractContainerMenu {
   public final SpaBlockEntity spa;

   public SpaMenu(int windowsId, Inventory inventory, FriendlyByteBuf buf) {
      this(windowsId, inventory, (SpaBlockEntity)inventory.player.level().getBlockEntity(buf.readBlockPos()));
   }

   public SpaMenu(int windowsId, Inventory inventory, SpaBlockEntity spa) {
      super((MenuType)ModMenuTypes.SPA.get(), windowsId);
      this.spa = spa;
      this.addSlot(new SlotLimitedByClass(ItemBathSalts.class, spa, 0, 65, 31));

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int k = 0; k < 9; k++) {
         this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
      }
   }

   public boolean clickMenuButton(@NotNull Player player, int id) {
      if (id == 1) {
         this.spa.toggleMix();
      }

      return super.clickMenuButton(player, id);
   }

   public boolean stillValid(@NotNull Player player) {
      return this.spa.stillValid(player);
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int index) {
      ItemStack copy = ItemStack.EMPTY;
      Slot slot = this.getSlot(index);
      if (slot.hasItem()) {
         ItemStack stack = slot.getItem();
         copy = stack.copy();
         if (index == 0) {
            if (!this.spa.canPlaceItem(index, stack) || !this.moveItemStackTo(stack, 1, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.spa.canPlaceItem(index, stack) || !this.moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }
      }

      return copy;
   }
}
