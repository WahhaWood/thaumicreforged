package thaumcraft.entities.smart.golem.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import thaumcraft.util.ItemStackData;

public class GolemItemHandler implements IItemHandlerModifiable {
   protected ItemStack[] stacks;
   protected int slotLimit;
   protected Runnable onChanged;

   public GolemItemHandler(int slots) {
      this(slots, 64);
   }

   public GolemItemHandler(int slots, int stackLimit) {
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
               if (!canStacksStack(existing, stack)) {
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

   public boolean addItem(ItemStack stack) {
      if (stack.isEmpty()) {
         return true;
      } else {
         ItemStack remaining = stack.copy();

         for (int i = 0; i < this.stacks.length && !remaining.isEmpty(); i++) {
            remaining = this.insertItem(i, remaining, false);
         }

         if (remaining.isEmpty()) {
            stack.setCount(0);
            return true;
         } else {
            stack.setCount(remaining.getCount());
            return false;
         }
      }
   }

   public int findItem(Item item) {
      for (int i = 0; i < this.stacks.length; i++) {
         if (!this.stacks[i].isEmpty() && this.stacks[i].is(item)) {
            return i;
         }
      }

      return -1;
   }

   public int findItem(Predicate<ItemStack> predicate) {
      for (int i = 0; i < this.stacks.length; i++) {
         if (!this.stacks[i].isEmpty() && predicate.test(this.stacks[i])) {
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

   public boolean isFull() {
      for (ItemStack stack : this.stacks) {
         if (stack.isEmpty()) {
            return false;
         }

         if (stack.getCount() < Math.min(stack.getMaxStackSize(), this.slotLimit)) {
            return false;
         }
      }

      return true;
   }

   public int countItems(ItemStack target, boolean fuzzy) {
      if (target.isEmpty()) {
         return 0;
      } else {
         int count = 0;

         for (ItemStack stack : this.stacks) {
            if (!stack.isEmpty()) {
               if (fuzzy) {
                  if (this.matchesFuzzy(stack, target)) {
                     count += stack.getCount();
                  }
               } else if (ItemStackData.sameItemSameTags(stack, target)) {
                  count += stack.getCount();
               }
            }
         }

         return count;
      }
   }

   public int countItemsIgnoreMeta(ItemStack target, boolean ignoreNBT) {
      if (target.isEmpty()) {
         return 0;
      } else {
         int count = 0;

         for (ItemStack stack : this.stacks) {
            if (!stack.isEmpty() && stack.is(target.getItem()) && (ignoreNBT || Objects.equals(ItemStackData.getTag(stack), ItemStackData.getTag(target)))) {
               count += stack.getCount();
            }
         }

         return count;
      }
   }

   public List<ItemStack> getNeededItems(boolean fuzzy) {
      List<ItemStack> needed = new ArrayList<>();

      for (ItemStack stack : this.stacks) {
         if (!stack.isEmpty()) {
            if (fuzzy) {
               TagKey<Item> tag = this.getPrimaryTag(stack);
               if (tag != null) {
                  Named<Item> holders = (Named<Item>)BuiltInRegistries.ITEM.getTag(tag).orElse(null);
                  if (holders != null) {
                     for (Holder<Item> holder : holders) {
                        needed.add(new ItemStack((ItemLike)holder.value()));
                     }
                     continue;
                  }
               }
            }

            needed.add(stack.copy());
         }
      }

      return needed;
   }

   public boolean contains(ItemStack target, boolean fuzzy) {
      return this.countItems(target, fuzzy) > 0;
   }

   public List<ItemStack> clearAndGetItems() {
      List<ItemStack> dropped = new ArrayList<>();

      for (int i = 0; i < this.stacks.length; i++) {
         if (!this.stacks[i].isEmpty()) {
            dropped.add(this.stacks[i].copy());
            this.stacks[i] = ItemStack.EMPTY;
         }
      }

      if (!dropped.isEmpty()) {
         this.onContentsChanged();
      }

      return dropped;
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

   public CompoundTag serializeNBT() {
      ListTag list = new ListTag();

      for (int i = 0; i < this.stacks.length; i++) {
         if (!this.stacks[i].isEmpty()) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Slot", i);
            list.add(ItemStackData.saveStack(this.stacks[i]).merge(tag));
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

      Arrays.fill(this.stacks, ItemStack.EMPTY);
      ListTag list = nbt.getList("Items", 10);

      for (int i = 0; i < list.size(); i++) {
         CompoundTag tag = list.getCompound(i);
         int slot = tag.getInt("Slot");
         if (slot >= 0 && slot < this.stacks.length) {
            this.stacks[slot] = ItemStackData.loadStack(tag);
         }
      }
   }

   public ListTag writeToNBT(ListTag list) {
      for (int i = 0; i < this.stacks.length; i++) {
         if (!this.stacks[i].isEmpty()) {
            CompoundTag tag = new CompoundTag();
            tag.putByte("Slot", (byte)i);
            list.add(ItemStackData.saveStack(this.stacks[i]).merge(tag));
         }
      }

      return list;
   }

   public void readFromNBT(ListTag list) {
      Arrays.fill(this.stacks, ItemStack.EMPTY);

      for (int i = 0; i < list.size(); i++) {
         CompoundTag tag = list.getCompound(i);
         int slot = tag.getByte("Slot") & 255;
         if (slot >= 0 && slot < this.stacks.length) {
            this.stacks[slot] = ItemStackData.loadStack(tag);
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

   private static boolean canStacksStack(ItemStack left, ItemStack right) {
      return ItemStackData.sameItemSameTags(left, right) && left.getCount() <= left.getMaxStackSize();
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

   private boolean matchesFuzzy(ItemStack a, ItemStack b) {
      if (a.isEmpty() || b.isEmpty()) {
         return false;
      } else if (a.is(b.getItem())) {
         return true;
      } else {
         TagKey<Item> tagA = this.getPrimaryTag(a);
         if (tagA != null && b.is(tagA)) {
            return true;
         } else {
            TagKey<Item> tagB = this.getPrimaryTag(b);
            return tagB != null && a.is(tagB);
         }
      }
   }

   private TagKey<Item> getPrimaryTag(ItemStack stack) {
      Iterator<TagKey<Item>> it = stack.getTags().iterator();
      TagKey<Item> fallback = null;

      while (it.hasNext()) {
         TagKey<Item> tag = it.next();
         if (fallback == null) {
            fallback = tag;
         }

         String namespace = tag.location().getNamespace();
         if ("c".equals(namespace)) {
            return tag;
         }
      }

      return fallback;
   }
}
