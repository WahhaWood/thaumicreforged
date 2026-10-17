package thaumcraft.entities.smart.travelling_trunk.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.entities.smart.travelling_trunk.EntityTravelingTrunk;
import thaumcraft.init.registry.ModMenuTypes;

public class MenuTravelingTrunk extends AbstractContainerMenu {
   private final EntityTravelingTrunk trunk;
   private final TrunkContainerWrapper container;
   private final int numRows;

   public MenuTravelingTrunk(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
      this(containerId, playerInv, getTrunk(playerInv.player.level(), buf.readInt()));
   }

   public MenuTravelingTrunk(int containerId, Inventory playerInv, EntityTravelingTrunk trunk) {
      super((MenuType)ModMenuTypes.TRAVELLING_TRUNK.get(), containerId);
      this.trunk = trunk;
      this.container = new TrunkContainerWrapper(trunk.getInventory(), trunk);
      this.numRows = trunk.getRows();
      this.addTrunkSlots();
      this.addPlayerSlots(playerInv);
      if (!playerInv.player.level().isClientSide) {
         this.trunk.setOpen(true);
         this.trunk.playChestSound(true);
      }
   }

   private void addTrunkSlots() {
      for (int row = 0; row < this.numRows; row++) {
         for (int col = 0; col < 9; col++) {
            int slotIndex = col + row * 9;
            int x = 8 + col * 18;
            int y = 15 + row * 23;
            this.addSlot(new MenuTravelingTrunk.TrunkSlot(this.container, slotIndex, x, y));
         }
      }
   }

   private void addPlayerSlots(Inventory playerInv) {
      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 118 + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         this.addSlot(new Slot(playerInv, col, 8 + col * 18, 176));
      }
   }

   private static EntityTravelingTrunk getTrunk(Level level, int id) {
      return (EntityTravelingTrunk)level.getEntity(id);
   }

   public EntityTravelingTrunk getTrunk() {
      return this.trunk;
   }

   public int getNumRows() {
      return this.numRows;
   }

   /** TC4 ContainerTravelingTrunk.enchantItem: button 1 toggles stay/follow. */
   @Override
   public boolean clickMenuButton(Player player, int id) {
      if (id == 1) {
         this.trunk.setStay(!this.trunk.getStay());
         return true;
      } else {
         return false;
      }
   }

   public ItemStack quickMoveStack(Player player, int index) {
      ItemStack result = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack slotStack = slot.getItem();
         result = slotStack.copy();
         int trunkSlots = this.numRows * 9;
         if (index < trunkSlots) {
            if (!this.moveItemStackTo(slotStack, trunkSlots, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(slotStack, 0, trunkSlots, false)) {
            return ItemStack.EMPTY;
         }

         if (slotStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         return result;
      } else {
         return result;
      }
   }

   public boolean stillValid(Player player) {
      return this.trunk.isAlive() && player.distanceToSqr(this.trunk) <= 64.0;
   }

   public void removed(Player player) {
      super.removed(player);
      if (!player.level().isClientSide) {
         this.trunk.setOpen(false);
         this.trunk.playChestSound(false);
      }
   }

   public static class TrunkSlot extends Slot {
      public TrunkSlot(TrunkContainerWrapper container, int slot, int x, int y) {
         super(container, slot, x, y);
      }

      public boolean mayPlace(ItemStack stack) {
         return true;
      }
   }
}
