package thaumicenergistics.menu;

import appeng.api.upgrades.Upgrades;
import thaumicenergistics.ae2.recipe.RecipeAssembler;
import thaumicenergistics.block.infusion_assembler.InfusionAssemblerBlockEntity;
import thaumicenergistics.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import thaumcraft.util.BlockEntityMenuValidator;

public class InfusionAssemblerMenu extends AbstractContainerMenu implements RecipeAssemblerMenu {
   private static final int PLAYER_INV_POSITION_Y = 114;
   private static final int HOTBAR_INV_POSITION_Y = 172;
   private static final int UPGRADE_SLOT_COUNT = 4;
   private static final int UPGRADE_SLOT_X = 187;
   private static final int UPGRADE_SLOT_Y = 26;
   private static final int TARGET_SLOT_X = 152;
   private static final int TARGET_SLOT_Y = 88;
   private static final int PLAYER_INV_START = 0;
   private static final int PLAYER_INV_END = 27;
   private static final int HOTBAR_START = 27;
   private static final int HOTBAR_END = 36;
   public final InfusionAssemblerBlockEntity assembler;
   private final Slot targetSlot;
   private final Slot[] upgradeSlots = new Slot[4];
   private final ContainerData data;

   public InfusionAssemblerMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
      this(containerId, inventory, getBlockEntity(inventory, buf));
   }

   public InfusionAssemblerMenu(int containerId, Inventory playerInv, final InfusionAssemblerBlockEntity assembler) {
      super((MenuType)ModMenuTypes.INFUSION_ASSEMBLER.get(), containerId);
      this.assembler = assembler;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 114 + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         this.addSlot(new Slot(playerInv, col, 8 + col * 18, 172));
      }

      Container upgradeContainer = assembler.getUpgradeInventory().toContainer();

      for (int i = 0; i < 4; i++) {
         this.upgradeSlots[i] = this.addSlot(new Slot(upgradeContainer, i, 187, 26 + 18 * i));
      }

      Container craftingContainer = assembler.craftingInventory.toContainer();
      this.targetSlot = this.addSlot(new InfusionAssemblerMenu.DisplaySlot(craftingContainer, 0, 152, 88));
      this.data = new ContainerData() {
         public int get(int index) {
            return switch (index) {
               case 0 -> assembler.isCrafting ? 1 : 0;
               case 1 -> assembler.craftTickCounter;
               default -> 0;
            };
         }

         public void set(int index, int value) {
            switch (index) {
               case 0:
                  assembler.isCrafting = value == 1;
                  break;
               case 1:
                  assembler.craftTickCounter = value;
            }
         }

         public int getCount() {
            return 2;
         }
      };
      this.addDataSlots(this.data);
   }

   private static InfusionAssemblerBlockEntity getBlockEntity(Inventory inventory, FriendlyByteBuf buf) {
      BlockEntity blockEntity = inventory.player.level().getBlockEntity(buf.readBlockPos());
      if (blockEntity instanceof InfusionAssemblerBlockEntity) {
         return (InfusionAssemblerBlockEntity)blockEntity;
      } else {
         throw new IllegalStateException("Expected InfusionAssemblerBlockEntity at menu position");
      }
   }

   @Override
   public RecipeAssembler recipeAssembler() {
      return this.assembler;
   }

   public boolean stillValid(@NotNull Player player) {
      return BlockEntityMenuValidator.isValid(player, this.assembler);
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
      if (player.level().isClientSide()) {
         return ItemStack.EMPTY;
      } else {
         Slot slot = slotIndex >= 0 && slotIndex < this.slots.size() ? (Slot)this.slots.get(slotIndex) : null;
         if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            boolean didMerge = false;
            if (slotIndex >= 0 && slotIndex < 36) {
               if (Upgrades.isUpgradeCardItem(slotStack)) {
                  didMerge = this.moveItemStackTo(slotStack, this.upgradeSlots[0].index, this.upgradeSlots[this.upgradeSlots.length - 1].index + 1, false);
               }

               if (!didMerge) {
                  if (slotIndex < 27) {
                     didMerge = this.moveItemStackTo(slotStack, 27, 36, false);
                  } else {
                     didMerge = this.moveItemStackTo(slotStack, 0, 27, false);
                  }
               }
            } else if (this.isUpgradeSlot(slot)) {
               didMerge = this.moveItemStackTo(slotStack, 27, 36, false);
               if (!didMerge) {
                  didMerge = this.moveItemStackTo(slotStack, 0, 27, false);
               }
            }

            if (didMerge) {
               if (slotStack.isEmpty()) {
                  slot.set(ItemStack.EMPTY);
               } else {
                  slot.setChanged();
               }

               this.broadcastChanges();
            }

            return ItemStack.EMPTY;
         } else {
            return ItemStack.EMPTY;
         }
      }
   }

   private boolean isUpgradeSlot(Slot slot) {
      for (Slot upgradeSlot : this.upgradeSlots) {
         if (slot == upgradeSlot) {
            return true;
         }
      }

      return false;
   }

   public static class DisplaySlot extends Slot {
      public DisplaySlot(Container container, int index, int x, int y) {
         super(container, index, x, y);
      }

      public boolean mayPlace(@NotNull ItemStack stack) {
         return false;
      }

      public boolean mayPickup(@NotNull Player player) {
         return false;
      }
   }
}
