package thaumcraft.item.ichor_pouch;

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
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.item.container.ItemContainer;
import thaumcraft.item.wands.ItemFocusPouch;
import thaumcraft.util.ItemStackData;

public class IchorPouchMenu extends AbstractContainerMenu {
   public static final int COLS = 13;
   public static final int ROWS = 9;
   public static final int SLOT_COUNT = 117;
   private final ItemContainer input = new ItemContainer(this, 117, 64)
      .setFilter(stack -> !(stack.getItem() instanceof ItemFocusPouch) && !(stack.getItem() instanceof ItemIchorPouch));
   private final Player player;
   private final InteractionHand hand;
   private ItemStack pouch;
   private final int blockedHotbarIndex;
   private int blockedMenuSlot = -1;
   private boolean dirty;

   public IchorPouchMenu(int windowId, Inventory inventory, FriendlyByteBuf buf) {
      this(windowId, inventory, buf != null ? (InteractionHand)buf.readEnum(InteractionHand.class) : InteractionHand.MAIN_HAND);
   }

   public IchorPouchMenu(int windowId, Inventory inventory, InteractionHand hand) {
      super((MenuType)ModMenuTypes.ICHOR_POUCH.get(), windowId);
      this.player = inventory.player;
      this.hand = hand;
      this.pouch = this.player.getItemInHand(hand);
      this.blockedHotbarIndex = inventory.selected;
      if (this.pouch.getItem() instanceof ItemIchorPouch) {
         ItemIchorPouch.ensureUid(this.pouch);
         int startX = 12;
         int startY = 8;

         for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 13; col++) {
               int index = row * 13 + col;
               this.addSlot(new Slot(this.input, index, startX + col * 18, startY + row * 18));
            }
         }

         this.bindPlayerInventory(inventory);
         if (!this.player.level().isClientSide) {
            this.input.fill(((ItemIchorPouch)this.pouch.getItem()).getInventory(this.pouch));
         }

         this.dirty = false;
      }
   }

   protected void bindPlayerInventory(Inventory inv) {
      int offsetX = 48;
      int offsetY = 177;

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(inv, j + i * 9 + 9, offsetX + j * 18, offsetY + i * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         int menuIndex = this.slots.size();
         this.addSlot(new Slot(inv, i, offsetX + i * 18, offsetY + 58));
         if (i == this.blockedHotbarIndex) {
            this.blockedMenuSlot = menuIndex;
         }
      }
   }

   public void slotsChanged(@NotNull Container container) {
      this.dirty = true;
      super.slotsChanged(container);
   }

   public void broadcastChanges() {
      super.broadcastChanges();
      if (!this.player.level().isClientSide && this.dirty) {
         this.dirty = false;
         ItemStack current = this.player.getItemInHand(this.hand);
         if (isSamePouch(this.pouch, current)) {
            ((ItemIchorPouch)this.pouch.getItem()).setInventory(this.pouch, this.input.getItems());
            this.player.setItemInHand(this.hand, this.pouch);
            this.player.getInventory().setChanged();
         }
      }
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
      if (slotIndex == this.blockedMenuSlot) {
         return ItemStack.EMPTY;
      } else if (!this.stillValid(player)) {
         return ItemStack.EMPTY;
      } else {
         Slot slot = this.getSlot(slotIndex);
         if (!slot.hasItem()) {
            return ItemStack.EMPTY;
         } else {
            ItemStack inSlot = slot.getItem();
            ItemStack copy = inSlot.copy();
            if (slotIndex < 117) {
               if (!this.moveItemStackTo(inSlot, 117, this.slots.size(), true)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.input.canPlaceItem(0, inSlot) || !this.moveItemStackTo(inSlot, 0, 117, false)) {
               return ItemStack.EMPTY;
            }

            if (inSlot.isEmpty()) {
               slot.set(ItemStack.EMPTY);
            } else {
               slot.setChanged();
            }

            return copy;
         }
      }
   }

   public boolean stillValid(@NotNull Player player) {
      if (!player.isAlive()) {
         return false;
      } else {
         ItemStack current = player.getItemInHand(this.hand);
         return !player.level().isClientSide ? isSamePouch(this.pouch, current) : !current.isEmpty() && current.getItem() instanceof ItemIchorPouch;
      }
   }

   private static boolean isSamePouch(ItemStack a, ItemStack b) {
      if (!a.isEmpty() && !b.isEmpty()) {
         if (a.getItem() != b.getItem()) {
            return false;
         } else {
            CompoundTag ta = ItemStackData.getTag(a);
            CompoundTag tb = ItemStackData.getTag(b);
            if (ta != null && tb != null) {
               String ua = ta.getString("UID");
               String ub = tb.getString("UID");
               return !ua.isEmpty() && ua.equals(ub);
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   public void clicked(int slotIndex, int button, @NotNull ClickType clickType, @NotNull Player player) {
      if (this.stillValid(player)) {
         if (slotIndex != this.blockedMenuSlot) {
            if (clickType != ClickType.SWAP || button != this.blockedHotbarIndex) {
               super.clicked(slotIndex, button, clickType, player);
            }
         }
      }
   }

   public void removed(@NotNull Player player) {
      super.removed(player);
      if (!player.level().isClientSide && this.pouch.getItem() instanceof ItemIchorPouch) {
         ItemStack current = player.getItemInHand(this.hand);
         if (isSamePouch(this.pouch, current)) {
            ((ItemIchorPouch)this.pouch.getItem()).setInventory(this.pouch, this.input.getItems());
            player.setItemInHand(this.hand, this.pouch);
            player.getInventory().setChanged();
         }
      }
   }

   public int getBlockedHotbarIndex() {
      return this.blockedHotbarIndex;
   }
}
