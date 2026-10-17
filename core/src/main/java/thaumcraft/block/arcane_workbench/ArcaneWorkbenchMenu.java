package thaumcraft.block.arcane_workbench;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.container.SlotLimitedByWand;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.recipe.ThaumcraftCraftingManager;
import thaumcraft.util.ItemStackData;

public class ArcaneWorkbenchMenu extends AbstractContainerMenu {
   public final MagicWorkbenchBlockEntity workbench;
   public final Inventory inventory;
   public final ResultContainer resultContainer = new ResultContainer();
   private static final int SLOT_RESULT = 0;
   private static final int SLOT_WAND = 1;
   private static final int CRAFT_GRID_START = 2;
   private static final int CRAFT_GRID_END = 11;
   private static final int PLAYER_INV_START = 11;
   private static final int HOTBAR_START = 38;
   private static final int PLAYER_INV_END = 47;

   public ArcaneWorkbenchMenu(int windowId, Inventory inventory, FriendlyByteBuf buf) {
      this(windowId, inventory, (MagicWorkbenchBlockEntity)inventory.player.level().getBlockEntity(buf.readBlockPos()));
   }

   public ArcaneWorkbenchMenu(int windowId, Inventory inventory, MagicWorkbenchBlockEntity workbench) {
      super((MenuType)ModMenuTypes.ARCANE_WORKBENCH.get(), windowId);
      this.workbench = workbench;
      this.inventory = inventory;
      if (!inventory.player.level().isClientSide) {
         this.workbench.bindMenu(this);
      }

      this.addSlot(new SlotCraftingArcaneWorkbench(this, inventory.player, this.workbench, this.resultContainer, 0, 160, 64));
      this.addSlot(new SlotLimitedByWand(this.workbench, 9, 160, 24));

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 3; col++) {
            this.addSlot(new Slot(this.workbench, col + row * 3, 40 + col * 24, 40 + row * 24));
         }
      }

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col + row * 9 + 9, 16 + col * 18, 151 + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         this.addSlot(new Slot(inventory, col, 16 + col * 18, 209));
      }

      this.slotsChanged(this.workbench);
   }

   public void slotsChanged(@NotNull Container container) {
      Level level = this.workbench.getLevel();
      if (level != null) {
         this.resultContainer.setItem(0, ItemStack.EMPTY);
         RecipeManager recipeManager = level.getRecipeManager();
         TransientCraftingContainer craftingMatrix = new TransientCraftingContainer(DummyMenu.INSTANCE, 3, 3);

         for (int i = 0; i < 9; i++) {
            craftingMatrix.setItem(i, this.workbench.getItem(i));
         }

         CraftingInput craftingInput = CraftingInput.of(3, 3, craftingMatrix.getItems());
         ItemStack vanillaResult = recipeManager.getRecipeFor(RecipeType.CRAFTING, craftingInput, level)
            .<CraftingRecipe>map(RecipeHolder::value)
            .map(recipe -> recipe.assemble(craftingInput, level.registryAccess()))
            .orElse(ItemStack.EMPTY);
         if (!vanillaResult.isEmpty()) {
            this.resultContainer.setItem(0, vanillaResult);
         } else if (this.workbench.getItem(9).getItem() instanceof ItemWandCasting wand) {
            AspectList aspects = ThaumcraftCraftingManager.findArcaneRecipeAspects(this.workbench, level, this.inventory.player);
            if (wand.consumeAllVisCrafting(this.workbench.getItem(9), this.inventory.player, aspects, false)) {
               this.resultContainer.setItem(0, ThaumcraftCraftingManager.findArcaneRecipeResult(this.workbench, level, this.inventory.player));
            }
         }

         this.broadcastChanges();
      }
   }

   public void removed(@NotNull Player player) {
      super.removed(player);
      if (!player.level().isClientSide) {
         this.workbench.unbindMenu(this);
      }
   }

   public boolean stillValid(@NotNull Player player) {
      return this.workbench.stillValid(player);
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int index) {
      ItemStack result = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (!slot.hasItem()) {
         return result;
      } else {
         ItemStack stack = slot.getItem();
         result = stack.copy();
         if (index == 0) {
            if (!this.moveItemStackTo(stack, 11, 47, true)) {
               return ItemStack.EMPTY;
            } else {
               slot.onQuickCraft(stack, result);
               slot.onTake(player, result);

               while (true) {
                  ItemStack newResult = slot.getItem();
                  if (newResult.isEmpty() || !ItemStackData.sameItemSameTags(result, newResult)) {
                     break;
                  }

                  ItemStack toMove = newResult.copy();
                  if (!this.moveItemStackTo(toMove, 11, 47, true) || toMove.getCount() >= newResult.getCount()) {
                     break;
                  }

                  newResult.shrink(newResult.getCount() - toMove.getCount());
                  if (newResult.isEmpty()) {
                     slot.setByPlayer(ItemStack.EMPTY);
                  }

                  slot.onTake(player, newResult);
               }

               return result;
            }
         } else {
            if (index == 1) {
               if (!this.moveItemStackTo(stack, 11, 47, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (index >= 2 && index < 11) {
               if (!this.moveItemStackTo(stack, 11, 47, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (index >= 11 && index < 47) {
               ItemStack stackCopy = stack.copy();
               if (stack.getItem() instanceof ItemWandCasting wand && !wand.isStaff(stack)) {
                  this.moveItemStackTo(stack, 1, 2, false);
               }

               if (stack.getCount() == stackCopy.getCount() && !this.moveItemStackTo(stack, 2, 11, false)) {
                  if (index < 38) {
                     if (!this.moveItemStackTo(stack, 38, 47, false)) {
                        return ItemStack.EMPTY;
                     }
                  } else if (!this.moveItemStackTo(stack, 11, 38, false)) {
                     return ItemStack.EMPTY;
                  }
               }
            }

            if (stack.isEmpty()) {
               slot.setByPlayer(ItemStack.EMPTY);
            } else {
               slot.setChanged();
            }

            if (stack.getCount() == result.getCount()) {
               return ItemStack.EMPTY;
            } else {
               slot.onTake(player, stack);
               return result;
            }
         }
      }
   }

   public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
      if (clickType == ClickType.THROW) {
         button = 1;
      } else if ((slotId == 0 || slotId == 1) && button > 0) {
         button = 0;
      }

      super.clicked(slotId, button, clickType, player);
   }

   public boolean canTakeItemForPickAll(@NotNull ItemStack stack, @NotNull Slot slot) {
      return slot.container != this.workbench && super.canTakeItemForPickAll(stack, slot);
   }
}
