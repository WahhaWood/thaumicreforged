package thaumcraft.item.focus_pouch;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.container.SlotLimitedByClass;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.item.container.ItemContainer;
import thaumcraft.item.wands.ItemFocusPouch;
import thaumcraft.util.ItemStackData;

public class FocusPouchMenu extends AbstractContainerMenu {
   private final int blockSlot;
   private final ItemContainer input = new ItemContainer(this, 1, 1).setFilter(stack -> stack.getItem() instanceof ItemFocusBasic);
   private final ItemStack pouch;
   private final Player player;
   private static final String NBT_KEY_UID = "UID";
   private int blockedSlotNumber;
   private final int blockedSlotIndex;

   public void slotsChanged(@NotNull Container container) {
      ((ItemFocusPouch)this.pouch.getItem()).setInventory(this.pouch, this.input.getItems());
      if (this.player != null && this.stillValid(this.player) && ItemStack.isSameItem(this.player.getMainHandItem(), this.pouch)) {
         this.player.setItemInHand(InteractionHand.MAIN_HAND, this.pouch);
      }

      super.slotsChanged(container);
   }

   private static boolean isSameItemInventory(ItemStack base, ItemStack comparison) {
      if (base != null && comparison != null) {
         if (base.getItem() != comparison.getItem()) {
            return false;
         } else if (ItemStackData.hasTag(base) && ItemStackData.hasTag(comparison)) {
            String baseUID = ItemStackData.getTag(base).getString("UID");
            String comparisonUID = ItemStackData.getTag(comparison).getString("UID");
            return baseUID != null && baseUID.equals(comparisonUID);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public FocusPouchMenu(int windowsId, Inventory inventory, FriendlyByteBuf buf) {
      super((MenuType)ModMenuTypes.FOCUS_POUCH.get(), windowsId);
      this.player = inventory.player;
      this.pouch = inventory.getSelected();
      this.blockSlot = inventory.selected + 45;
      this.blockedSlotIndex = inventory.selected;
      if (this.pouch.getItem() instanceof ItemFocusPouch) {
         CompoundTag nbt = ItemStackData.getOrCreateTag(this.pouch);
         if (!nbt.contains("UID")) {
            nbt.putString("UID", UUID.randomUUID().toString());
         }

         for (int a = 0; a < 18; a++) {
            this.addSlot(new SlotLimitedByClass(ItemFocusBasic.class, this.input, a, 37 + a % 6 * 18, 51 + a / 6 * 18));
         }

         this.bindPlayerInventory(inventory);
         if (!inventory.player.level().isClientSide) {
            this.input.fill(((ItemFocusPouch)this.pouch.getItem()).getInventory(this.pouch));
         }

         this.slotsChanged(this.input);
      }
   }

   protected void bindPlayerInventory(Inventory inventoryPlayer) {
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 151 + i * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         Slot slot = this.addSlot(new Slot(inventoryPlayer, i, 8 + i * 18, 209));
         if (slot.getSlotIndex() == this.blockedSlotIndex) {
            this.blockedSlotNumber = slot.getContainerSlot();
         }
      }
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int slot) {
      if (slot == this.blockSlot) {
         return ItemStack.EMPTY;
      } else if (slot == this.blockedSlotNumber) {
         return ItemStack.EMPTY;
      } else if (!this.stillValid(player)) {
         return ItemStack.EMPTY;
      } else {
         ItemStack stack = ItemStack.EMPTY;
         Slot slotObject = this.getSlot(slot);
         if (slotObject.hasItem()) {
            ItemStack stackInSlot = slotObject.getItem();
            stack = stackInSlot.copy();
            if (slot < 18) {
               if (!this.input.canPlaceItem(slot, stackInSlot) || !this.moveItemStackTo(stackInSlot, 18, this.slots.size(), true)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.input.canPlaceItem(slot, stackInSlot) || !this.moveItemStackTo(stackInSlot, 0, 18, false)) {
               return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
               slotObject.set(ItemStack.EMPTY);
            } else {
               slotObject.setChanged();
            }
         }

         return stack;
      }
   }

   public boolean stillValid(Player player) {
      return player.isAlive() && this.blockedSlotIndex == player.getInventory().selected && isSameItemInventory(this.pouch, player.getMainHandItem());
   }

   public void clicked(int slot, int button, @NotNull ClickType clickType, @NotNull Player player) {
      if (slot != this.blockSlot) {
         if (slot != this.blockedSlotNumber) {
            if (clickType != ClickType.SWAP || button != this.blockedSlotNumber) {
               if (this.stillValid(player)) {
                  super.clicked(slot, button, clickType, player);
               }
            }
         }
      }
   }

   public void removed(@NotNull Player player) {
      super.removed(player);
      if (!player.level().isClientSide) {
         ((ItemFocusPouch)this.pouch.getItem()).setInventory(this.pouch, this.input.getItems());
         if (this.player == null || !this.stillValid(this.player)) {
            return;
         }

         if (ItemStack.isSameItem(this.player.getMainHandItem(), this.pouch)) {
            this.player.setItemInHand(InteractionHand.MAIN_HAND, this.pouch);
         }

         this.player.getInventory().setChanged();
      }
   }
}
