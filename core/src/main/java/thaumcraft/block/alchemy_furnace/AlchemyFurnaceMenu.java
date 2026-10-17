package thaumcraft.block.alchemy_furnace;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.container.SlotLimitedHasAspects;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.recipe.ThaumcraftCraftingManager;
import thaumcraft.util.BlockEntityMenuValidator;

public class AlchemyFurnaceMenu extends AbstractContainerMenu {
   public final AlchemyFurnaceBlockEntity furnace;
   private int lastCookTime;
   private int lastBurnTime;
   private int lastItemBurnTime;
   private int lastVis;
   private int lastSmelt;
   @Nullable
   private ContainerSynchronizer synchronizer;

   public AlchemyFurnaceMenu(int windowsId, Inventory inventory, FriendlyByteBuf buf) {
      this(windowsId, inventory, (AlchemyFurnaceBlockEntity)inventory.player.level().getBlockEntity(buf.readBlockPos()));
   }

   public AlchemyFurnaceMenu(int windowsId, Inventory inventory, AlchemyFurnaceBlockEntity furnace) {
      super((MenuType)ModMenuTypes.ALCHEMY_FURNACE.get(), windowsId);
      this.furnace = furnace;
      this.addSlot(new SlotLimitedHasAspects(furnace, 0, 80, 8));
      this.addSlot(new Slot(furnace, 1, 80, 48));

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int k = 0; k < 9; k++) {
         this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
      }
   }

   public void setSynchronizer(@NotNull ContainerSynchronizer synchronizer) {
      super.setSynchronizer(synchronizer);
      synchronizer.sendDataChange(this, 0, this.furnace.furnaceCookTime);
      synchronizer.sendDataChange(this, 1, this.furnace.furnaceBurnTime);
      synchronizer.sendDataChange(this, 2, this.furnace.currentItemBurnTime);
      synchronizer.sendDataChange(this, 3, this.furnace.vis);
      synchronizer.sendDataChange(this, 4, this.furnace.smeltTime);
      this.synchronizer = synchronizer;
   }

   public void broadcastChanges() {
      super.broadcastChanges();
      if (this.synchronizer != null) {
         if (this.lastCookTime != this.furnace.furnaceCookTime) {
            this.synchronizer.sendDataChange(this, 0, this.furnace.furnaceCookTime);
         }

         if (this.lastBurnTime != this.furnace.furnaceBurnTime) {
            this.synchronizer.sendDataChange(this, 1, this.furnace.furnaceBurnTime);
         }

         if (this.lastItemBurnTime != this.furnace.currentItemBurnTime) {
            this.synchronizer.sendDataChange(this, 2, this.furnace.currentItemBurnTime);
         }

         if (this.lastVis != this.furnace.vis) {
            this.synchronizer.sendDataChange(this, 3, this.furnace.vis);
         }

         if (this.lastSmelt != this.furnace.smeltTime) {
            this.synchronizer.sendDataChange(this, 4, this.furnace.smeltTime);
         }

         this.lastCookTime = this.furnace.furnaceCookTime;
         this.lastBurnTime = this.furnace.furnaceBurnTime;
         this.lastItemBurnTime = this.furnace.currentItemBurnTime;
         this.lastVis = this.furnace.vis;
         this.lastSmelt = this.furnace.smeltTime;
      }
   }

   public void setData(int index, int value) {
      if (index == 0) {
         this.furnace.furnaceCookTime = value;
      }

      if (index == 1) {
         this.furnace.furnaceBurnTime = value;
      }

      if (index == 2) {
         this.furnace.currentItemBurnTime = value;
      }

      if (index == 3) {
         this.furnace.vis = value;
      }

      if (index == 4) {
         this.furnace.smeltTime = value;
      }
   }

   public boolean stillValid(@NotNull Player player) {
      return BlockEntityMenuValidator.isValid(player, this.furnace);
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int index) {
      ItemStack copy = ItemStack.EMPTY;
      Slot slot = this.getSlot(index);
      if (slot.hasItem()) {
         ItemStack stack = slot.getItem();
         copy = stack.copy();
         if (index != 1 && index != 0) {
            AspectList bonusTags = ThaumcraftCraftingManager.getBonusTags(stack, ThaumcraftCraftingManager.getObjectTags(stack));
            if (AlchemyFurnaceBlockEntity.isItemFuel(stack)) {
               if (!this.moveItemStackTo(stack, 1, 2, false) && !this.moveItemStackTo(stack, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (bonusTags != null && bonusTags.size() > 0) {
               if (!this.moveItemStackTo(stack, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (index >= 2 && index < 29) {
               if (!this.moveItemStackTo(stack, 29, 38, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (index >= 29 && index < 38 && !this.moveItemStackTo(stack, 2, 29, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(stack, 2, 38, false)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, stack);
      }

      return copy;
   }
}
