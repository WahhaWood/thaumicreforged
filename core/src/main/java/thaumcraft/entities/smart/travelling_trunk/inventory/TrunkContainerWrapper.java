package thaumcraft.entities.smart.travelling_trunk.inventory;

import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import thaumcraft.entities.smart.travelling_trunk.EntityTravelingTrunk;

public class TrunkContainerWrapper implements Container {
   private final IItemHandlerModifiable handler;
   @Nullable
   private final EntityTravelingTrunk trunk;

   public TrunkContainerWrapper(IItemHandlerModifiable handler, @Nullable EntityTravelingTrunk trunk) {
      this.handler = handler;
      this.trunk = trunk;
   }

   public int getContainerSize() {
      return this.handler.getSlots();
   }

   public boolean isEmpty() {
      for (int i = 0; i < this.handler.getSlots(); i++) {
         if (!this.handler.getStackInSlot(i).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack getItem(int slot) {
      return this.handler.getStackInSlot(slot);
   }

   public ItemStack removeItem(int slot, int amount) {
      return this.handler.extractItem(slot, amount, false);
   }

   public ItemStack removeItemNoUpdate(int slot) {
      ItemStack stack = this.handler.getStackInSlot(slot);
      if (stack.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         this.handler.setStackInSlot(slot, ItemStack.EMPTY);
         return stack;
      }
   }

   public void setItem(int slot, ItemStack stack) {
      this.handler.setStackInSlot(slot, stack);
   }

   public void setChanged() {
   }

   public boolean stillValid(Player player) {
      if (this.trunk == null) {
         return true;
      } else {
         return !this.trunk.isAlive() ? false : player.distanceToSqr(this.trunk) <= 64.0;
      }
   }

   public void clearContent() {
      for (int i = 0; i < this.handler.getSlots(); i++) {
         this.handler.setStackInSlot(i, ItemStack.EMPTY);
      }
   }

   public int getMaxStackSize() {
      return this.handler.getSlotLimit(0);
   }

   public void startOpen(Player player) {
      if (this.trunk != null) {
         this.trunk.setOpen(true);
      }
   }

   public void stopOpen(Player player) {
      if (this.trunk != null) {
         this.trunk.setOpen(false);
      }
   }

   public IItemHandlerModifiable getHandler() {
      return this.handler;
   }
}
