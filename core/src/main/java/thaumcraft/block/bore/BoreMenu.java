package thaumcraft.block.bore;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.container.SlotLimitedByClass;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.item.wands.foci.ItemFocusExcavation;

public class BoreMenu extends AbstractContainerMenu {
   public final BoreBlockEntity bore;

   public BoreMenu(int windowsId, Inventory inventory, FriendlyByteBuf buf) {
      this(windowsId, inventory, (BoreBlockEntity)inventory.player.level().getBlockEntity(buf.readBlockPos()));
   }

   public BoreMenu(int windowsId, Inventory inventory, BoreBlockEntity bore) {
      super((MenuType)ModMenuTypes.ARCANE_BORE.get(), windowsId);
      this.bore = bore;
      this.addSlot(new SlotLimitedByClass(ItemFocusExcavation.class, bore, 0, 26, 18));
      this.addSlot(new SlotLimitedByClass(PickaxeItem.class, bore, 1, 74, 18));

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 59 + i * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.addSlot(new Slot(inventory, i, 8 + i * 18, 117));
      }
   }

   public boolean stillValid(@NotNull Player player) {
      return this.bore.stillValid(player);
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int index) {
      ItemStack stack = ItemStack.EMPTY;
      Slot slot = this.getSlot(index);
      if (slot.hasItem()) {
         ItemStack stackInSlot = slot.getItem();
         stack = stackInSlot.copy();
         if (index <= 1) {
            if (!this.moveItemStackTo(stackInSlot, 2, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (stackInSlot.getItem() instanceof ItemFocusExcavation) {
            if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (stackInSlot.getItem() instanceof PickaxeItem && !this.moveItemStackTo(stackInSlot, 1, 2, false)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (stackInSlot.getCount() == stack.getCount()) {
            return ItemStack.EMPTY;
         }
      }

      return stack;
   }
}
