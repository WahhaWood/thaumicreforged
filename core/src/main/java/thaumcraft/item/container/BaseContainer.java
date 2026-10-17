package thaumcraft.item.container;

import java.util.Iterator;
import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.util.ItemStackData;

public class BaseContainer implements Container, Iterable<ItemStack> {
   private final NonNullList<ItemStack> stacks;
   private final int containerSize;
   private final int maxStackSize;
   private Predicate<ItemStack> filter;

   public BaseContainer(int containerSize) {
      this(containerSize, 64);
   }

   public BaseContainer(int containerSize, int maxStackSize) {
      this.containerSize = containerSize;
      this.maxStackSize = maxStackSize;
      this.stacks = NonNullList.withSize(containerSize, ItemStack.EMPTY);
   }

   public BaseContainer(ItemStack... stacks) {
      this(64, stacks);
   }

   public BaseContainer(int maxStackSize, ItemStack... stacks) {
      this.containerSize = stacks.length;
      this.maxStackSize = maxStackSize;
      this.stacks = NonNullList.of(ItemStack.EMPTY, stacks);
   }

   public <T extends BaseContainer> T setFilter(Predicate<ItemStack> value) {
      this.filter = value;
      return (T)this;
   }

   public NonNullList<ItemStack> getItems() {
      return this.stacks;
   }

   public void fill(NonNullList<ItemStack> stacks) {
      this.stacks.clear();
      int min = Math.min(this.containerSize, stacks.size());

      for (int i = 0; i < min; i++) {
         this.stacks.set(i, (ItemStack)stacks.get(i));
      }
   }

   public boolean isEmpty(int index) {
      return ((ItemStack)this.stacks.get(index)).isEmpty();
   }

   public int getContainerSize() {
      return this.containerSize;
   }

   public int getMaxStackSize() {
      return this.maxStackSize;
   }

   public boolean isEmpty() {
      return this.stacks.isEmpty();
   }

   @NotNull
   public ItemStack getItem(int index) {
      return (ItemStack)this.stacks.get(index);
   }

   @NotNull
   public ItemStack removeItem(int index, int amount) {
      ItemStack stack = (ItemStack)this.stacks.get(index);
      if (!stack.isEmpty()) {
         ItemStack split = stack.split(amount);
         this.setChanged();
         return split;
      } else {
         return ItemStack.EMPTY;
      }
   }

   @NotNull
   public ItemStack removeItemNoUpdate(int index) {
      return (ItemStack)this.stacks.remove(index);
   }

   public void setItem(int index, @NotNull ItemStack stack) {
      this.stacks.set(index, stack);
      this.setChanged();
   }

   public void setChanged() {
   }

   public boolean stillValid(@NotNull Player player) {
      return false;
   }

   public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
      return this.filter == null || this.filter.test(stack);
   }

   public void clearContent() {
      this.stacks.clear();
   }

   @NotNull
   @Override
   public Iterator<ItemStack> iterator() {
      return this.stacks.iterator();
   }

   public ListTag save(ListTag listTag) {
      for (int i = 0; i < this.containerSize; i++) {
         ItemStack stack = (ItemStack)this.stacks.get(i);
         if (!stack.isEmpty()) {
            CompoundTag tag = ItemStackData.saveStack(stack);
            tag.putByte("Slot", (byte)i);
            listTag.add(tag);
         }
      }

      return listTag;
   }

   public void load(ListTag listTag) {
      for (int i = 0; i < listTag.size(); i++) {
         CompoundTag tag = listTag.getCompound(i);
         int index = tag.getByte("Slot") & 255;
         if (index < this.containerSize) {
            this.stacks.set(index, ItemStackData.loadStack(tag));
         }
      }
   }
}
