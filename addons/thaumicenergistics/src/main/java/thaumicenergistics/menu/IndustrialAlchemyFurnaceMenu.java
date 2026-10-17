package thaumicenergistics.menu;

import thaumicenergistics.block.industrial_alchemy_furnace.IndustrialAlchemyFurnaceBlockEntity;
import thaumicenergistics.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import thaumcraft.util.BlockEntityMenuValidator;

public class IndustrialAlchemyFurnaceMenu extends AbstractContainerMenu {
   public final IndustrialAlchemyFurnaceBlockEntity furnace;
   private final ContainerData data;

   public IndustrialAlchemyFurnaceMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
      this(containerId, inventory, getBlockEntity(inventory, extraData), new SimpleContainerData(2));
   }

   public IndustrialAlchemyFurnaceMenu(int containerId, Inventory inventory, IndustrialAlchemyFurnaceBlockEntity furnace, ContainerData data) {
      super((MenuType)ModMenuTypes.INDUSTRIAL_ALCHEMY_FURNACE.get(), containerId);
      this.furnace = furnace;
      this.data = data;
      this.addDataSlots(this.data);
      this.addSlot(new SlotItemHandler(furnace.getInternalItemHandler(), 0, 80, 8));

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
      }
   }

   private static IndustrialAlchemyFurnaceBlockEntity getBlockEntity(Inventory inventory, FriendlyByteBuf extraData) {
      if (extraData == null) {
         throw new IllegalStateException("Extra data is null! Block must use NetworkHooks.openScreen()");
      } else {
         BlockEntity blockEntity = inventory.player.level().getBlockEntity(extraData.readBlockPos());
         if (blockEntity instanceof IndustrialAlchemyFurnaceBlockEntity) {
            return (IndustrialAlchemyFurnaceBlockEntity)blockEntity;
         } else {
            throw new IllegalStateException("Expected IndustrialAlchemyFurnaceBlockEntity at menu position");
         }
      }
   }

   public int getEnergy() {
      return this.data.get(0);
   }

   public int getTotalEssentia() {
      return this.data.get(1);
   }

   public ItemStack quickMoveStack(Player player, int index) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (index < 1) {
            if (!this.moveItemStackTo(itemstack1, 1, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }
      }

      return itemstack;
   }

   public boolean stillValid(Player player) {
      return BlockEntityMenuValidator.isValid(player, this.furnace);
   }
}
