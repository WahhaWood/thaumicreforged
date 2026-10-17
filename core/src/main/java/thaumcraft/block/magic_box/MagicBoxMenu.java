package thaumcraft.block.magic_box;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.util.BlockEntityMenuValidator;

public class MagicBoxMenu extends AbstractContainerMenu {
   private final IItemHandler boxHandler;
   private final MagicBoxBlockEntity boxTile;
   public final int numRows;

   public MagicBoxMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
      this(id, playerInv, getClientHandler(playerInv, buf), getClientTile(playerInv, buf));
   }

   public MagicBoxMenu(int id, Inventory playerInv, IItemHandler handler, MagicBoxBlockEntity tile) {
      super((MenuType)ModMenuTypes.MAGIC_BOX.get(), id);
      this.boxHandler = handler;
      this.boxTile = tile;
      this.numRows = handler.getSlots() / 9;

      for (int i = 0; i < this.numRows; i++) {
         for (int j = 0; j < 9; j++) {
            int index = j + i * 9;
            this.addSlot(new SlotItemHandler(handler, index, 8 + j * 18, 18 + i * 18));
         }
      }

      this.bindPlayerInventory(playerInv, 18 + this.numRows * 18 + 15);
   }

   private static MagicBoxBlockEntity getClientTile(Inventory inv, FriendlyByteBuf buf) {
      buf.markReaderIndex();
      BlockPos pos = buf.readBlockPos();
      buf.resetReaderIndex();
      return inv.player.level().getBlockEntity(pos) instanceof MagicBoxBlockEntity box ? box : null;
   }

   private static IItemHandler getClientHandler(Inventory inv, FriendlyByteBuf buf) {
      BlockPos pos = buf.readBlockPos();
      if (inv.player.level().getBlockEntity(pos) instanceof MagicBoxBlockEntity box) {
         IItemHandler handler = box.getItemHandler(null);
         return (IItemHandler)(handler != null ? handler : new ItemStackHandler(27));
      } else {
         return new ItemStackHandler(27);
      }
   }

   private void bindPlayerInventory(Inventory playerInv, int yOffset) {
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, yOffset + i * 18));
         }
      }

      for (int k = 0; k < 9; k++) {
         this.addSlot(new Slot(playerInv, k, 8 + k * 18, yOffset + 58));
      }
   }

   public boolean stillValid(Player player) {
      return BlockEntityMenuValidator.isValid(player, this.boxTile);
   }

   public ItemStack quickMoveStack(Player player, int index) {
      ItemStack copy = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot.hasItem()) {
         ItemStack stack = slot.getItem();
         copy = stack.copy();
         int boxSlots = this.boxHandler.getSlots();
         if (index < boxSlots) {
            if (!this.moveItemStackTo(stack, boxSlots, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(stack, 0, boxSlots, false)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }
      }

      return copy;
   }
}
