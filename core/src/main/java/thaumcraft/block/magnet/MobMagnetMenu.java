package thaumcraft.block.magnet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.container.SlotLimitedByClass;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.item.soul_mould.ItemSoulMould;

public class MobMagnetMenu extends AbstractContainerMenu {
   public final MobMagnetBlockEntity magnet;

   public MobMagnetMenu(int windowsId, Inventory inventory, FriendlyByteBuf buf) {
      this(windowsId, inventory, (MobMagnetBlockEntity)inventory.player.level().getBlockEntity(buf.readBlockPos()));
   }

   public MobMagnetMenu(int windowsId, Inventory inventory, MobMagnetBlockEntity magnet) {
      super((MenuType)ModMenuTypes.MOB_MAGNET.get(), windowsId);
      this.magnet = magnet;
      this.addSlot(new SlotLimitedByClass(ItemSoulMould.class, magnet, 0, 53, 37));

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int k = 0; k < 9; k++) {
         this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
      }
   }

   public boolean clickMenuButton(@NotNull Player player, int id) {
      if (id == 0) {
         this.magnet.setAdultMode(true);
         return true;
      } else if (id == 1) {
         this.magnet.setAdultMode(false);
         return true;
      } else {
         return super.clickMenuButton(player, id);
      }
   }

   public boolean stillValid(@NotNull Player player) {
      return this.magnet.stillValid(player);
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
         } else if (!this.magnet.canPlaceItem(0, stack) || !this.moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }
      }

      return copy;
   }
}
