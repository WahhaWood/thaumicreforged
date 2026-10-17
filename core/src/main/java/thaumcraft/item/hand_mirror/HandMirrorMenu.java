package thaumcraft.item.hand_mirror;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.item.container.ItemContainer;
import thaumcraft.util.ItemStackData;

public class HandMirrorMenu extends AbstractContainerMenu {
   public ItemContainer input = new ItemContainer(this, 1, 1);
   ItemStack mirror = null;
   Player player = null;
   private final int blockSlot;

   public HandMirrorMenu(int windowsId, Inventory inventory, FriendlyByteBuf buf) {
      this(windowsId, inventory);
   }

   public HandMirrorMenu(int windowsId, Inventory inventory) {
      super((MenuType)ModMenuTypes.HAND_MIRROR.get(), windowsId);
      this.player = inventory.player;
      this.mirror = inventory.getSelected();
      this.blockSlot = inventory.selected;
      this.addSlot(new HandMirrorMenu.MirrorSlot(this.mirror, this.input, 0, 80, 24));
      this.bindPlayerInventory(inventory);
      this.slotsChanged(this.input);
   }

   protected void bindPlayerInventory(Inventory inventoryPlayer) {
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.addSlot(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
      }
   }

   public void slotsChanged(@NotNull Container container) {
      if (ItemStackData.sameItemSameTags(this.input.getItem(0), this.mirror)) {
         this.player.containerMenu = this.player.inventoryMenu;
      } else if (!this.player.level().isClientSide
         && !this.input.getItem(0).isEmpty()
         && this.player != null
         && ItemHandMirror.transport(this.mirror, this.input.getItem(0), this.player, this.player.level())) {
         this.input.setItem(0, ItemStack.EMPTY);
      }

      super.slotsChanged(container);
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int slot) {
      ItemStack stack = ItemStack.EMPTY;
      Slot slotObject = this.getSlot(slot);
      if (slotObject.hasItem() && !(slotObject.getItem().getItem() instanceof ItemHandMirror)) {
         ItemStack stackInSlot = slotObject.getItem();
         stack = stackInSlot.copy();
         if (slot == 0) {
            if (!this.moveItemStackTo(stackInSlot, 1, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
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

   public boolean stillValid(Player player) {
      return player.isAlive() && this.blockSlot == player.getInventory().selected && ItemStackData.sameItemSameTags(player.getMainHandItem(), this.mirror);
   }

   public static class MirrorSlot extends Slot {
      private final ItemStack hand;

      public MirrorSlot(ItemStack hand, Container inv, int id, int x, int y) {
         super(inv, id, x, y);
         this.hand = hand;
      }

      public boolean mayPlace(@NotNull ItemStack stack) {
         return !ItemStackData.sameItemSameTags(this.hand, stack);
      }
   }
}
