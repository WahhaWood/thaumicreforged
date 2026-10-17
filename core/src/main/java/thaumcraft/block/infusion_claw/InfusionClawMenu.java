package thaumcraft.block.infusion_claw;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.init.registry.ModMenuTypes;

public class InfusionClawMenu extends AbstractContainerMenu {
   public final Inventory playerInv;
   public final InfusionClawBlockEntity clawInv;

   public InfusionClawMenu(int windowsId, Inventory inventory, FriendlyByteBuf buf) {
      this(windowsId, inventory, (InfusionClawBlockEntity)inventory.player.level().getBlockEntity(buf.readBlockPos()));
   }

   public InfusionClawMenu(int id, Inventory playerInv, InfusionClawBlockEntity clawInv) {
      super((MenuType)ModMenuTypes.INFUSION_CLAW.get(), id);
      this.playerInv = playerInv;
      this.clawInv = clawInv;
      this.addSlot(new InfusionClawMenu.SlotFiltered(clawInv, 0, 80, 32));
      this.bindPlayerInventory(playerInv);
   }

   public InfusionClawBlockEntity getClaw() {
      return this.clawInv;
   }

   private void bindPlayerInventory(Inventory inv) {
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
      }
   }

   /** The screen sends the wanted lock state: 1 locks the claw, 0 unlocks it. */
   @Override
   public boolean clickMenuButton(Player player, int id) {
      if (id == 0 || id == 1) {
         this.clawInv.setIsLocked(id == 1);
         return true;
      } else {
         return false;
      }
   }

   public ItemStack quickMoveStack(Player player, int index) {
      if (this.clawInv.isRunning()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack result = ItemStack.EMPTY;
         Slot slot = (Slot)this.slots.get(index);
         if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == 0) {
               if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) {
                  return ItemStack.EMPTY;
               }
            } else {
               Slot clawSlot = (Slot)this.slots.get(0);
               if (!clawSlot.mayPlace(stack)) {
                  return ItemStack.EMPTY;
               }

               if (!this.moveItemStackTo(stack, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            }

            if (stack.isEmpty()) {
               slot.set(ItemStack.EMPTY);
            } else {
               slot.setChanged();
            }
         }

         return result;
      }
   }

   public boolean stillValid(Player player) {
      return this.clawInv.stillValid(player);
   }

   public static class SlotFiltered extends Slot {
      private final InfusionClawBlockEntity claw;

      public SlotFiltered(InfusionClawBlockEntity claw, int index, int x, int y) {
         super(claw, index, x, y);
         this.claw = claw;
      }

      public boolean mayPlace(ItemStack stack) {
         return this.claw.isItemValidForSlot(this.getSlotIndex(), stack);
      }

      public boolean mayPickup(Player player) {
         return !this.claw.isRunning() && super.mayPickup(player);
      }
   }
}
