package thaumcraft.block.warp_gate;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.item.sky_pearl.ItemSkyPearl;

public class WarpGateMenu extends AbstractContainerMenu {
   private static final int GATE_SLOT_COUNT = 10;
   private final WarpGateBlockEntity gate;

   public WarpGateMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
      this(id, playerInventory, getClientBlockEntity(playerInventory, buf));
   }

   public WarpGateMenu(int id, Inventory playerInventory, WarpGateBlockEntity gate) {
      super((MenuType)ModMenuTypes.WARP_GATE.get(), id);
      this.gate = gate;

      for (int row = 0; row < 2; row++) {
         for (int col = 0; col < 5; col++) {
            int index = row * 5 + col;
            this.addSlot(new SlotItemHandler(new InvWrapper(gate), index, 30 + col * 25, 27 + row * 25) {
               public int getMaxStackSize() {
                  return 1;
               }

               public boolean mayPlace(@NotNull ItemStack stack) {
                  return stack.getItem() instanceof ItemSkyPearl;
               }
            });
         }
      }

      this.bindPlayerInventory(playerInventory);
   }

   private static WarpGateBlockEntity getClientBlockEntity(Inventory inventory, FriendlyByteBuf buf) {
      BlockPos pos = buf.readBlockPos();
      if (inventory.player.level().getBlockEntity(pos) instanceof WarpGateBlockEntity gate) {
         return gate;
      } else {
         throw new IllegalStateException("Missing warp gate block entity at " + pos);
      }
   }

   private void bindPlayerInventory(Inventory playerInventory) {
      int yOffset = 84;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, yOffset + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         this.addSlot(new Slot(playerInventory, col, 8 + col * 18, yOffset + 58));
      }
   }

   public WarpGateBlockEntity getGate() {
      return this.gate;
   }

   public boolean stillValid(@NotNull Player player) {
      return this.gate.stillValid(player);
   }

   public boolean clickMenuButton(@NotNull Player player, int id) {
      if (id == 0) {
         this.gate.setLocked(!this.gate.isLocked());
         return true;
      } else {
         return super.clickMenuButton(player, id);
      }
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int index) {
      ItemStack result = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot.hasItem()) {
         ItemStack stack = slot.getItem();
         result = stack.copy();
         if (index < 10) {
            if (!this.moveItemStackTo(stack, 10, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(stack, 0, 10, false)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (stack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
         }
      }

      return result;
   }
}
