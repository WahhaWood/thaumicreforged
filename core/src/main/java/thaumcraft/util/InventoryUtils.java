package thaumcraft.util;

import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner.BlockType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InventoryUtils {
   @NotNull
   public static ItemStack placeItemStackIntoInventory(@NotNull ItemStack stack, Container inventory, @Nullable Direction side, boolean doit) {
      ItemStack inserted = stack.copy();
      ItemStack result = insertStack(inventory, inserted, side, doit);
      if (doit && inserted.getCount() != result.getCount()) {
         inventory.setChanged();
      }

      return result;
   }

   @NotNull
   public static ItemStack insertStack(Container container, @NotNull ItemStack stack1, @Nullable Direction side, boolean doit) {
      return container instanceof WorldlyContainer worldlyContainer && side != null
         ? ItemHandlerHelper.insertItemStacked(new SidedInvWrapper(worldlyContainer, side), stack1, !doit)
         : ItemHandlerHelper.insertItemStacked(new InvWrapper(container), stack1, !doit);
   }

   @NotNull
   public static ItemStack insertItemStacked(IItemHandler handler, @NotNull ItemStack stack, boolean simulate) {
      return ItemHandlerHelper.insertItemStacked(handler, stack, simulate);
   }

   public static boolean canExtractItemFromInventory(Container container, ItemStack stack, int amount, Direction direction) {
      return stack != null && !(container instanceof WorldlyContainer worldlyContainer && !worldlyContainer.canTakeItemThroughFace(amount, stack, direction));
   }

   public static boolean areItemStacksEqualStrict(ItemStack stack0, ItemStack stack1) {
      return areItemStacksEqual(stack0, stack1, false, false, false);
   }

   public static boolean areItemStacksEqual(ItemStack stack0, ItemStack stack1, boolean useOre, boolean ignoreDamage, boolean ignoreNBT) {
      return ignoreNBT ? ItemStack.isSameItem(stack0, stack1) : ItemStackData.sameItemSameTags(stack0, stack1);
   }

   public static boolean consumeInventoryItem(Player player, ItemStack stack) {
      Inventory inventory = player.getInventory();
      int slot = inventory.findSlotMatchingItem(stack);
      if (slot >= 0) {
         inventory.removeItem(slot, 1);
         return true;
      } else {
         return false;
      }
   }

   public static void dropItems(Level level, BlockPos blockPos) {
      if (level.getBlockEntity(blockPos) instanceof Container container) {
         Containers.dropContents(level, blockPos, container);
         container.clearContent();
      }
   }

   public static int isPlayerCarrying(Player player, ItemStack stack) {
      return player.getInventory().findSlotMatchingItem(stack);
   }

   @NotNull
   public static ItemStack damageItem(int amount, @NotNull ItemStack stack, Level level) {
      if (stack.isDamageableItem()) {
         stack.setDamageValue(stack.getDamageValue() + amount);
         if (stack.getDamageValue() >= stack.getMaxDamage()) {
            return ItemStack.EMPTY;
         }
      }

      return stack;
   }

   public static ChestBlockEntity getDoubleChest(BlockEntity blockEntity) {
      if (blockEntity instanceof ChestBlockEntity chest) {
         BlockState blockState = chest.getBlockState();
         if (ChestBlock.getBlockType(blockState) == BlockType.SINGLE) {
            return null;
         }

         BlockPos blockPos = blockEntity.getBlockPos().relative(ChestBlock.getConnectedDirection(blockState));
         if (Objects.requireNonNull(blockEntity.getLevel()).getBlockEntity(blockPos) instanceof ChestBlockEntity chestBlockEntity) {
            return chestBlockEntity;
         }
      }

      return null;
   }

   @NotNull
   public static ItemStack cycleItemStack(Object input) {
      if (input instanceof ItemStack stack) {
         return stack;
      } else if (input instanceof Ingredient ingredient) {
         return cycleItemStack(ingredient.getItems());
      } else if (input instanceof ArrayList<?> q) {
         return cycleItemStack((ItemStack[])q.toArray());
      } else {
         return input instanceof TagKey<?> tagKey && tagKey.isFor(Registries.ITEM) ? cycleItemStack(Ingredient.of((net.minecraft.tags.TagKey) tagKey).getItems()) : ItemStack.EMPTY;
      }
   }

   @NotNull
   public static ItemStack cycleItemStack(ItemStack[] stacks) {
      return stacks.length > 0 ? stacks[(int)(System.currentTimeMillis() / 1000L % stacks.length)] : ItemStack.EMPTY;
   }
}
