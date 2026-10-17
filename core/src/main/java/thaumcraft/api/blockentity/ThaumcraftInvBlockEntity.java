package thaumcraft.api.blockentity;

import java.util.Collections;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import thaumcraft.util.BlockEntityMenuValidator;
import thaumcraft.util.ItemStackData;

public class ThaumcraftInvBlockEntity extends ThaumcraftBlockEntity implements Container {
   public final NonNullList<ItemStack> contents;

   public ThaumcraftInvBlockEntity(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState, int containerSize) {
      super(type, blockPos, blockState);
      this.contents = NonNullList.withSize(containerSize, ItemStack.EMPTY);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      Collections.fill(this.contents, ItemStack.EMPTY);
      ContainerHelper.loadAllItems(compound, this.contents, provider);
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      ContainerHelper.saveAllItems(compound, this.contents, provider);
   }

   public int getContainerSize() {
      return this.contents.size();
   }

   public boolean isEmpty() {
      return this.contents.isEmpty();
   }

   @NotNull
   public ItemStack getItem(int index) {
      return (ItemStack)this.contents.get(index);
   }

   @NotNull
   public ItemStack removeItem(int index, int amount) {
      ItemStack stack = this.getItem(index);
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
      return (ItemStack)this.contents.remove(index);
   }

   public void setItem(int index, @NotNull ItemStack stack) {
      this.contents.set(index, stack);
      if (stack.getCount() > this.getMaxStackSize()) {
         stack.setCount(this.getMaxStackSize());
      }

      this.setChanged();
   }

   public boolean stillValid(@NotNull Player player) {
      return BlockEntityMenuValidator.isValid(player, this);
   }

   public void clearContent() {
      this.contents.clear();
   }

   @NotNull
   public ItemStack add(ItemStack stack) {
      if (stack.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack inputStack = stack.copy();

         for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack invStack = this.getItem(i);
            if (ItemStackData.sameItemSameTags(invStack, inputStack)) {
               int limit = Math.min(this.getMaxStackSize(), invStack.getMaxStackSize());
               int amount = Math.min(inputStack.getCount(), limit - invStack.getCount());
               if (amount > 0) {
                  invStack.grow(amount);
                  inputStack.shrink(amount);
                  this.setChanged();
                  if (inputStack.isEmpty()) {
                     break;
                  }
               }
            }
         }

         if (inputStack.isEmpty()) {
            return ItemStack.EMPTY;
         } else {
            for (int ix = 0; ix < this.getContainerSize(); ix++) {
               ItemStack invStack = this.getItem(ix);
               if (invStack.isEmpty()) {
                  this.setItem(ix, inputStack.copyAndClear());
                  break;
               }
            }

            return inputStack.isEmpty() ? ItemStack.EMPTY : inputStack;
         }
      }
   }

   public int remove(ItemStack stack) {
      if (stack.isEmpty()) {
         return 0;
      } else {
         int count = stack.getCount();
         ItemStack inputStack = stack.copy();

         for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack invStack = this.getItem(i);
            if (ItemStackData.sameItemSameTags(invStack, inputStack)) {
               int amount = Math.min(inputStack.getCount(), invStack.getCount());
               if (amount > 0) {
                  invStack.shrink(amount);
                  inputStack.shrink(amount);
                  this.setChanged();
                  if (inputStack.isEmpty()) {
                     break;
                  }
               }
            }
         }

         return count - inputStack.getCount();
      }
   }
}
