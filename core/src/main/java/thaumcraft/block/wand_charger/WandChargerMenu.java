package thaumcraft.block.wand_charger;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.item.wands.ItemWandCasting;

public class WandChargerMenu extends AbstractContainerMenu {
   private static final int MACHINE_SLOT_COUNT = 5;
   private static final int PLAYER_INV_START = 5;
   private static final int PLAYER_INV_END = 32;
   private static final int HOTBAR_START = 32;
   private static final int HOTBAR_END = 41;
   public final WandChargerBlockEntity tile;
   private final ContainerData data;

   public WandChargerMenu(int windowId, Inventory inventory, FriendlyByteBuf buffer) {
      this(windowId, inventory, getTile(inventory, buffer));
   }

   private static WandChargerBlockEntity getTile(Inventory inventory, FriendlyByteBuf buffer) {
      BlockPos pos = buffer.readBlockPos();
      if (inventory.player.level().getBlockEntity(pos) instanceof WandChargerBlockEntity tile) {
         return tile;
      } else {
         throw new IllegalStateException("Wand Charger BE not found at " + pos);
      }
   }

   public WandChargerMenu(int windowId, Inventory inventory, WandChargerBlockEntity tile) {
      this(windowId, inventory, tile, tile.createAspectStorageData());
   }

   private WandChargerMenu(int windowId, Inventory inventory, final WandChargerBlockEntity tile, ContainerData data) {
      super((MenuType)ModMenuTypes.WAND_CHARGER.get(), windowId);
      this.tile = tile;
      this.data = data;
      checkContainerDataCount(data, WandChargerBlockEntity.getChargeAspects().size());
      this.addDataSlots(data);

      for (int slot = 0; slot < 5; slot++) {
         int x = switch (slot) {
            case 0 -> 80;
            case 1 -> 80;
            case 2 -> 104;
            case 3 -> 56;
            case 4 -> 80;
            default -> throw new IllegalStateException("Unexpected slot " + slot);
         };

         int y = switch (slot) {
            case 0 -> 37;
            case 1 -> 13;
            case 2 -> 37;
            case 3 -> 37;
            case 4 -> 61;
            default -> throw new IllegalStateException("Unexpected slot " + slot);
         };
         final int slotIndex = slot;
         this.addSlot(new Slot(tile, slotIndex, x, y) {
            public boolean mayPlace(@NotNull ItemStack stack) {
               return tile.canPlaceItem(slotIndex, stack);
            }

            public int getMaxStackSize() {
               return 1;
            }
         });
      }

      for (int row = 0; row < 3; row++) {
         for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 103 + row * 18));
         }
      }

      for (int slot = 0; slot < 9; slot++) {
         this.addSlot(new Slot(inventory, slot, 8 + slot * 18, 161));
      }
   }

   public boolean stillValid(@NotNull Player player) {
      return this.tile.stillValid(player);
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int index) {
      ItemStack copy = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (!slot.hasItem()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack stack = slot.getItem();
         copy = stack.copy();
         if (index < 5) {
            if (!this.moveItemStackTo(stack, 5, 41, true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveIntoMachineSlots(stack)) {
            if (index < 32) {
               if (!this.moveItemStackTo(stack, 32, 41, false)) {
                  return ItemStack.EMPTY;
               }
            } else {
               if (index >= 41) {
                  return ItemStack.EMPTY;
               }

               if (!this.moveItemStackTo(stack, 5, 32, false)) {
                  return ItemStack.EMPTY;
               }
            }
         }

         if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
         } else {
            slot.onTake(player, stack);
            return copy;
         }
      }
   }

   public int getStoredVis(int index) {
      return this.data.get(index);
   }

   public int getStoredVis(Aspect aspect) {
      return this.getStoredVis(WandChargerBlockEntity.getChargeAspects().indexOf(aspect));
   }

   public int getMaxVis() {
      return this.tile.getMaxVis();
   }

   public double getWandChargePercent() {
      ItemStack stack = this.tile.getItem(0);
      if (!(stack.getItem() instanceof ItemWandCasting wandItem)) {
         return -1.0;
      } else {
         int var6 = 0;

         for (Aspect aspect : WandChargerBlockEntity.getChargeAspects()) {
            var6 += wandItem.getVis(stack, aspect);
         }

         int max = wandItem.getMaxVis(stack) * WandChargerBlockEntity.getChargeAspects().size();
         return max <= 0 ? 0.0 : Mth.clamp((double)var6 / max * 100.0, 0.0, 100.0);
      }
   }

   private boolean moveIntoMachineSlots(ItemStack stack) {
      if (this.tile.canPlaceItem(0, stack) && this.moveItemStackTo(stack, 0, 1, false)) {
         return true;
      } else {
         for (int machineSlot = 1; machineSlot < 5; machineSlot++) {
            if (this.tile.canPlaceItem(machineSlot, stack) && this.moveItemStackTo(stack, machineSlot, machineSlot + 1, false)) {
               return true;
            }
         }

         return false;
      }
   }
}
