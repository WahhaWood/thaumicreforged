package thaumcraft.block.advanced_node_infuser;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.init.registry.ModMenuTypes;

public class AdvancedNodeInfuserMenu extends AbstractContainerMenu {
   public final AdvancedNodeInfuserBlockEntity tile;

   public AdvancedNodeInfuserMenu(int windowId, Inventory inventory, FriendlyByteBuf buf) {
      this(windowId, inventory, getTile(inventory, buf));
   }

   private static AdvancedNodeInfuserBlockEntity getTile(Inventory inventory, FriendlyByteBuf buf) {
      BlockPos pos = buf.readBlockPos();
      if (inventory.player.level().getBlockEntity(pos) instanceof AdvancedNodeInfuserBlockEntity tile) {
         return tile;
      } else {
         throw new IllegalStateException("AdvancedNodeInfuser BE not found at " + pos);
      }
   }

   public AdvancedNodeInfuserMenu(int windowId, Inventory inventory, final AdvancedNodeInfuserBlockEntity tile) {
      super((MenuType)ModMenuTypes.ADVANCED_NODE_INFUSER.get(), windowId);
      this.tile = tile;
      this.addSlot(new Slot(tile, 0, 80, 36) {
         public boolean mayPlace(@NotNull ItemStack stack) {
            return tile.canPlaceItem(this.getSlotIndex(), stack);
         }

         public int getMaxStackSize() {
            return 1;
         }
      });

      for (int row = 0; row < 3; row++) {
         for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 89 + row * 18));
         }
      }

      for (int slot = 0; slot < 9; slot++) {
         this.addSlot(new Slot(inventory, slot, 8 + slot * 18, 147));
      }
   }

   public boolean stillValid(@NotNull Player player) {
      return this.tile.stillValid(player);
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int index) {
      ItemStack copy = ItemStack.EMPTY;
      Slot slot = this.getSlot(index);
      if (slot.hasItem()) {
         ItemStack stack = slot.getItem();
         copy = stack.copy();
         if (index == 0) {
            if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.tile.canPlaceItem(0, stack) || !this.moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         slot.onTake(player, stack);
      }

      return copy;
   }
}
