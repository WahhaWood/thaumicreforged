package thaumcraft.block.deconstruction_table;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.container.SlotLimitedHasAspects;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.recipe.ThaumcraftCraftingManager;

public class DeconstructionTableMenu extends AbstractContainerMenu {
   public final DeconstructionTableBlockEntity table;

   public DeconstructionTableMenu(int windowId, Inventory inv, FriendlyByteBuf buf) {
      this(windowId, inv, getTable(inv, buf));
   }

   private static DeconstructionTableBlockEntity getTable(Inventory inv, FriendlyByteBuf buf) {
      BlockPos pos = buf.readBlockPos();
      if (inv.player.level().getBlockEntity(pos) instanceof DeconstructionTableBlockEntity table) {
         return table;
      } else {
         throw new IllegalStateException("DeconstructionTable BE not found at " + pos);
      }
   }

   public DeconstructionTableMenu(int windowId, Inventory inv, final DeconstructionTableBlockEntity table) {
      super((MenuType)ModMenuTypes.DECONSTRUCTION_TABLE.get(), windowId);
      this.table = table;
      this.addSlot(new SlotLimitedHasAspects(table, 0, 64, 16));

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int k = 0; k < 9; k++) {
         this.addSlot(new Slot(inv, k, 8 + k * 18, 142));
      }

      this.addDataSlot(new DataSlot() {
         public int get() {
            return table.breaktime;
         }

         public void set(int v) {
            table.breaktime = v;
         }
      });
   }

   public boolean stillValid(@NotNull Player player) {
      return this.table.stillValid(player);
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int index) {
      ItemStack copy = ItemStack.EMPTY;
      Slot slot = this.getSlot(index);
      if (slot != null && slot.hasItem()) {
         ItemStack stack = slot.getItem();
         copy = stack.copy();
         int oldCount = stack.getCount();
         if (index == 0) {
            if (!this.moveItemStackTo(stack, 1, 37, false)) {
               return ItemStack.EMPTY;
            }
         } else {
            AspectList bonus = ThaumcraftCraftingManager.getBonusTags(stack, ThaumcraftCraftingManager.getObjectTags(stack));
            if (bonus != null && bonus.size() > 0) {
               if (!this.moveItemStackTo(stack, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (index >= 1 && index < 28) {
               if (!this.moveItemStackTo(stack, 28, 37, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (index >= 28 && index < 37 && !this.moveItemStackTo(stack, 1, 28, false)) {
               return ItemStack.EMPTY;
            }
         }

         if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (stack.getCount() == oldCount) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, stack);
      }

      return copy;
   }

   @Override
   public boolean clickMenuButton(@NotNull Player player, int id) {
      if (id == 1 && this.table != null && this.table.aspect != null) {
         if (!player.level().isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            thaumcraft.research.PlayerKnowledge knowledge = thaumcraft.research.PlayerKnowledge.of(player);
            knowledge.addAspectPool(this.table.aspect, (short)1);
            thaumcraft.research.ResearchManager.scheduleSave(player);
            thaumcraft.network.PacketHandler.sendTo(
               new thaumcraft.network.playerdata.PacketAspectPool(
                  this.table.aspect.getTag(),
                  (short)1,
                  (short)knowledge.getAspectPoolFor(this.table.aspect)
               ),
               serverPlayer
            );
            this.table.aspect = null;
            this.table.setChanged();
            if (this.table.getLevel() != null) {
               this.table.getLevel().sendBlockUpdated(this.table.getBlockPos(), this.table.getBlockState(), this.table.getBlockState(), 3);
            }
         }
         return true;
      }
      return false;
   }
}
