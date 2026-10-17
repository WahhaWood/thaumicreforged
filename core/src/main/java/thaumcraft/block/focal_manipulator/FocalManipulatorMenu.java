package thaumcraft.block.focal_manipulator;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.container.SlotLimitedByClass;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.init.registry.ModMenuTypes;

public class FocalManipulatorMenu extends AbstractContainerMenu {
   public final FocalManipulatorBlockEntity manipulator;

   public FocalManipulatorMenu(int windowsId, Inventory inventory, FriendlyByteBuf buf) {
      this(windowsId, inventory, (FocalManipulatorBlockEntity)inventory.player.level().getBlockEntity(buf.readBlockPos()));
   }

   public FocalManipulatorMenu(int windowsId, Inventory inventory, FocalManipulatorBlockEntity manipulator) {
      super((MenuType)ModMenuTypes.FOCAL_MANIPULATOR.get(), windowsId);
      this.manipulator = manipulator;
      this.addSlot(new SlotLimitedByClass(ItemFocusBasic.class, manipulator, 0, 88, 60));

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(inventory, j + i * 9 + 9, 16 + j * 18, 151 + i * 18));
         }
      }

      for (int k = 0; k < 9; k++) {
         this.addSlot(new Slot(inventory, k, 16 + k * 18, 209));
      }
   }

   public boolean stillValid(@NotNull Player player) {
      return this.manipulator.stillValid(player);
   }

   @Override
   public boolean clickMenuButton(@NotNull Player player, int id) {
      if (this.manipulator != null) {
         return this.manipulator.startCraft(id, player);
      }
      return false;
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int index) {
      ItemStack copy = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot.hasItem()) {
         ItemStack stack = slot.getItem();
         copy = stack.copy();
         if (index == 0) {
            if (!this.moveItemStackTo(stack, 1, 37, false)) {
               return ItemStack.EMPTY;
            }
         } else if (stack.getItem() instanceof ItemFocusBasic) {
            if (!this.moveItemStackTo(stack, 0, 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (index < 28) {
            if (!this.moveItemStackTo(stack, 28, 37, false)) {
               return ItemStack.EMPTY;
            }
         } else if (index < 37 && !this.moveItemStackTo(stack, 1, 28, false)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, stack);
      }

      return copy;
   }
}
