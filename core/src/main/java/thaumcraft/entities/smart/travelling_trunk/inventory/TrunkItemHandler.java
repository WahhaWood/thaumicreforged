package thaumcraft.entities.smart.travelling_trunk.inventory;

import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import thaumcraft.util.ItemStackData;

public class TrunkItemHandler implements IItemHandlerModifiable {
   private ItemStack[] stacks;
   private int slotLimit;
   private Runnable onChanged;

   public TrunkItemHandler(int slots) {
      this(slots, 64);
   }

   public TrunkItemHandler(int slots, int stackLimit) {
      this.stacks = new ItemStack[slots];
      this.slotLimit = stackLimit;

      for (int i = 0; i < slots; i++) {
         this.stacks[i] = ItemStack.EMPTY;
      }
   }

   public void setOnChanged(Runnable callback) {
      this.onChanged = callback;
   }

   public int getSlotCount() {
      return this.stacks.length;
   }

   public void resize(int newSize) {
      ItemStack[] newStacks = new ItemStack[newSize];

      for (int i = 0; i < newSize; i++) {
         if (i < this.stacks.length) {
            newStacks[i] = this.stacks[i];
         } else {
            newStacks[i] = ItemStack.EMPTY;
         }
      }

      this.stacks = newStacks;
   }

   public int getSlots() {
      return this.stacks.length;
   }

   @Nonnull
   public ItemStack getStackInSlot(int slot) {
      this.validateSlot(slot);
      return this.stacks[slot];
   }

   @Nonnull
   public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
      if (stack.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         this.validateSlot(slot);
         if (!this.isItemValid(slot, stack)) {
            return stack;
         } else {
            ItemStack existing = this.stacks[slot];
            int limit = this.getSlotLimit(slot);
            if (!existing.isEmpty()) {
               if (!ItemStackData.sameItemSameTags(existing, stack)) {
                  return stack;
               }

               limit -= existing.getCount();
            }

            if (limit <= 0) {
               return stack;
            } else {
               boolean reachedLimit = stack.getCount() > limit;
               if (!simulate) {
                  if (existing.isEmpty()) {
                     this.stacks[slot] = reachedLimit ? copyStackWithSize(stack, limit) : stack.copy();
                  } else {
                     existing.grow(reachedLimit ? limit : stack.getCount());
                  }

                  this.onContentsChanged();
               }

               return reachedLimit ? copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
            }
         }
      }
   }

   @Nonnull
   public ItemStack extractItem(int slot, int amount, boolean simulate) {
      if (amount == 0) {
         return ItemStack.EMPTY;
      } else {
         this.validateSlot(slot);
         ItemStack existing = this.stacks[slot];
         if (existing.isEmpty()) {
            return ItemStack.EMPTY;
         } else {
            int toExtract = Math.min(amount, existing.getCount());
            if (!simulate) {
               if (toExtract == existing.getCount()) {
                  this.stacks[slot] = ItemStack.EMPTY;
               } else {
                  existing.shrink(toExtract);
               }

               this.onContentsChanged();
            }

            return copyStackWithSize(existing, toExtract);
         }
      }
   }

   public int getSlotLimit(int slot) {
      return this.slotLimit;
   }

   public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
      return true;
   }

   public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
      this.validateSlot(slot);
      this.stacks[slot] = stack;
      this.onContentsChanged();
   }

   public int findItem(Item item) {
      for (int i = 0; i < this.stacks.length; i++) {
         if (!this.stacks[i].isEmpty() && this.stacks[i].is(item)) {
            return i;
         }
      }

      return -1;
   }

   public int getFirstEmptySlot() {
      for (int i = 0; i < this.stacks.length; i++) {
         if (this.stacks[i].isEmpty()) {
            return i;
         }
      }

      return -1;
   }

   public boolean hasItems() {
      for (ItemStack stack : this.stacks) {
         if (!stack.isEmpty()) {
            return true;
         }
      }

      return false;
   }

   public boolean isEmpty() {
      return !this.hasItems();
   }

   public void dropAllItems(Entity entity) {
      if (entity != null && !entity.level().isClientSide) {
         for (int i = 0; i < this.stacks.length; i++) {
            if (!this.stacks[i].isEmpty()) {
               entity.spawnAtLocation(this.stacks[i], 0.0F);
               this.stacks[i] = ItemStack.EMPTY;
            }
         }

         this.onContentsChanged();
      }
   }

   public void clear() {
      for (int i = 0; i < this.stacks.length; i++) {
         this.stacks[i] = ItemStack.EMPTY;
      }

      this.onContentsChanged();
   }

   public CompoundTag serializeNBT() {
      ListTag list = new ListTag();

      for (int i = 0; i < this.stacks.length; i++) {
         if (!this.stacks[i].isEmpty()) {
            CompoundTag tag = new CompoundTag();
            tag.putByte("Slot", (byte)i);
            tag.put("Item", ItemStackData.saveStack(this.stacks[i]));
            list.add(tag);
         }
      }

      CompoundTag nbt = new CompoundTag();
      nbt.put("Items", list);
      nbt.putInt("Size", this.stacks.length);
      return nbt;
   }

   public void deserializeNBT(CompoundTag nbt) {
      int size = nbt.getInt("Size");
      if (size > 0 && size != this.stacks.length) {
         this.resize(size);
      }

      for (int i = 0; i < this.stacks.length; i++) {
         this.stacks[i] = ItemStack.EMPTY;
      }

      ListTag list = nbt.getList("Items", 10);

      for (int i = 0; i < list.size(); i++) {
         CompoundTag tag = list.getCompound(i);
         int slot = tag.getByte("Slot") & 255;
         if (slot >= 0 && slot < this.stacks.length) {
            this.stacks[slot] = ItemStackData.loadStack(tag.getCompound("Item"));
         }
      }
   }

   public ListTag writeToNBT(ListTag list) {
      for (int i = 0; i < this.stacks.length; i++) {
         if (!this.stacks[i].isEmpty()) {
            CompoundTag tag = new CompoundTag();
            tag.putByte("Slot", (byte)i);
            tag.put("Item", ItemStackData.saveStack(this.stacks[i]));
            list.add(tag);
         }
      }

      return list;
   }

   public void readFromNBT(ListTag list) {
      for (int i = 0; i < this.stacks.length; i++) {
         this.stacks[i] = ItemStack.EMPTY;
      }

      for (int i = 0; i < list.size(); i++) {
         CompoundTag tag = list.getCompound(i);
         int slot = tag.getByte("Slot") & 255;
         if (slot >= 0 && slot < this.stacks.length) {
            this.stacks[slot] = ItemStackData.loadStack(tag.getCompound("Item"));
         }
      }
   }

   protected void validateSlot(int slot) {
      if (slot < 0 || slot >= this.stacks.length) {
         throw new IndexOutOfBoundsException("Slot " + slot + " out of range [0," + this.stacks.length + ")");
      }
   }

   protected void onContentsChanged() {
      if (this.onChanged != null) {
         this.onChanged.run();
      }
   }

   private static ItemStack copyStackWithSize(ItemStack stack, int size) {
      if (size <= 0) {
         return ItemStack.EMPTY;
      } else {
         ItemStack copy = stack.copy();
         copy.setCount(size);
         return copy;
      }
   }
}
