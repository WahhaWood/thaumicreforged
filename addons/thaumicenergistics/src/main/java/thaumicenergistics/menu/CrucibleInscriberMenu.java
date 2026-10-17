package thaumicenergistics.menu;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.ae2.pattern.CruciblePatternItem;
import thaumicenergistics.ae2.pattern.TECruciblePattern;
import thaumicenergistics.block.crucible_inscriber.CrucibleInscriberBlockEntity;
import thaumicenergistics.init.ModItems;
import thaumicenergistics.init.ModMenuTypes;
import thaumicenergistics.item.ItemCrucibleCore;
import thaumicenergistics.network.CrucibleInscriberSaveStatePacket;
import thaumicenergistics.util.HandlerCrucibleCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.util.BlockEntityMenuValidator;
import thaumcraft.util.ItemStackData;

public class CrucibleInscriberMenu extends AbstractContainerMenu {
   private static final int PLAYER_INV_POSITION_Y = 120;
   private static final int HOTBAR_INV_POSITION_Y = 178;
   public static final int KCORE_SLOT_X = 5;
   public static final int KCORE_SLOT_Y = 50;
   private static final int PATTERN_SLOT_X = 126;
   private static final int PATTERN_SLOT_Y = 14;
   private static final int PATTERN_ROWS = 5;
   private static final int PATTERN_COLS = 5;
   private static final int PATTERN_SLOT_COUNT = 25;
   private static final int PLAYER_INV_START = 0;
   private static final int PLAYER_INV_END = 27;
   private static final int HOTBAR_START = 27;
   private static final int HOTBAR_END = 36;
   private static final int KCORE_SLOT_INDEX = 36;
   private static final int PATTERN_START = 37;
   private static final int PATTERN_END = 62;
   private static final int CATALYST_SLOT_INDEX = 62;
   private static final int RESULT_SLOT_INDEX = 63;
   public final CrucibleInscriberBlockEntity inscriber;
   private final Player player;
   private final SimpleContainer ghostContainer;
   private final Slot[] patternSlots = new Slot[25];
   private Slot catalystSlot;
   private Slot resultSlot;
   private Slot crucibleCoreSlot;
   private final HandlerCrucibleCore crucibleCoreHandler;
   private final ArrayList<CrucibleRecipe> currentRecipes = new ArrayList<>();
   private int selectedRecipe;
   private boolean currentRecipeExists = false;
   private ItemStack lastCoreStack = ItemStack.EMPTY;

   public CrucibleInscriberMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
      this(containerId, inventory, getBlockEntity(inventory, buf));
   }

   public CrucibleInscriberMenu(int containerId, Inventory playerInv, CrucibleInscriberBlockEntity inscriber) {
      super((MenuType)ModMenuTypes.CRUCIBLE_INSCRIBER.get(), containerId);
      this.inscriber = inscriber;
      this.player = playerInv.player;
      int ghostSize = 27;
      this.ghostContainer = new SimpleContainer(ghostSize);
      this.bindPlayerInventory(playerInv);
      Container coreContainer = inscriber.getInternalInventory().toContainer();
      this.crucibleCoreSlot = this.addSlot(new Slot(coreContainer, 0, 5, 50) {
         public boolean mayPlace(@NotNull ItemStack stack) {
            return stack.getItem() instanceof ItemCrucibleCore;
         }

         public int getMaxStackSize() {
            return 1;
         }
      });
      this.initPatternSlots();
      this.catalystSlot = this.addSlot(new CrucibleInscriberMenu.GhostSlot(this.ghostContainer, 25, 46, 16));
      this.resultSlot = this.addSlot(new CrucibleInscriberMenu.DisplaySlot(this.ghostContainer, 26, 83, 16));
      this.crucibleCoreHandler = new HandlerCrucibleCore(25, TECruciblePattern::new);
   }

   private static CrucibleInscriberBlockEntity getBlockEntity(Inventory inventory, FriendlyByteBuf buf) {
      BlockEntity blockEntity = inventory.player.level().getBlockEntity(buf.readBlockPos());
      if (blockEntity instanceof CrucibleInscriberBlockEntity) {
         return (CrucibleInscriberBlockEntity)blockEntity;
      } else {
         throw new IllegalStateException("Expected CrucibleInscriberBlockEntity at menu position");
      }
   }

   private void bindPlayerInventory(Inventory playerInv) {
      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col + row * 9 + 9, 30 + col * 18, 120 + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         this.addSlot(new Slot(playerInv, col, 30 + col * 18, 178));
      }
   }

   private void initPatternSlots() {
      for (int row = 0; row < 5; row++) {
         for (int col = 0; col < 5; col++) {
            int index = row * 5 + col;
            int posX = 126 + 18 * col;
            int posY = 14 + 18 * row;
            this.patternSlots[index] = this.addSlot(new CrucibleInscriberMenu.DisplaySlot(this.ghostContainer, index, posX, posY));
         }
      }
   }

   public boolean canScroll(boolean next) {
      if (this.currentRecipes.size() <= 1) {
         return false;
      } else {
         return next ? this.selectedRecipe < this.currentRecipes.size() - 1 : this.selectedRecipe > 0;
      }
   }

   public void scrollCurrentRecipe(boolean next) {
      if (this.currentRecipes.size() <= 1) {
         this.selectedRecipe = 0;
      } else {
         if (next) {
            if (this.selectedRecipe >= this.currentRecipes.size() - 1) {
               return;
            }

            this.selectedRecipe++;
         } else {
            if (this.selectedRecipe <= 0) {
               return;
            }

            this.selectedRecipe--;
            if (this.selectedRecipe >= this.currentRecipes.size()) {
               this.selectedRecipe = this.currentRecipes.size() - 1;
            }
         }

         CrucibleRecipe r = this.currentRecipes.get(this.selectedRecipe);
         this.currentRecipeExists = this.isPatternStored(r);
         this.resultSlot.set(r.getResultItem().copy());
         this.sendSaveState(false);
      }
   }

   private void refreshCurrentRecipe() {
      this.currentRecipes.clear();
      ItemStack catalyst = this.catalystSlot.getItem();
      if (!catalyst.isEmpty()) {
         this.currentRecipes.addAll(this.findCrucibleRecipes(catalyst));
      }

      if (this.selectedRecipe >= this.currentRecipes.size()) {
         this.selectedRecipe = Math.max(0, this.currentRecipes.size() - 1);
      }

      if (!this.currentRecipes.isEmpty()) {
         CrucibleRecipe r = this.currentRecipes.get(this.selectedRecipe);
         this.currentRecipeExists = this.isPatternStored(r);
         this.resultSlot.set(r.getResultItem().copy());
      } else {
         this.currentRecipeExists = false;
         this.resultSlot.set(ItemStack.EMPTY);
      }

      this.sendSaveState(false);
   }

   private boolean isPatternStored(CrucibleRecipe recipe) {
      for (IPatternDetails p : this.crucibleCoreHandler.getPatterns()) {
         if (p instanceof TECruciblePattern cruciblePattern && cruciblePattern.isSimilar(recipe)) {
            return true;
         }
      }

      return false;
   }

   public void acceptJEIRecipe(ItemStack catalyst, AspectList aspects, ItemStack output) {
      CrucibleRecipe craft = TECruciblePattern.findCrucibleRecipe(this.player.level(), catalyst, aspects, output);
      if (craft != null) {
         this.catalystSlot.set(catalyst.copy());
         this.currentRecipes.clear();
         this.currentRecipes.addAll(this.findCrucibleRecipes(catalyst));
         this.selectedRecipe = this.currentRecipes.indexOf(craft);
         if (this.selectedRecipe < 0) {
            this.selectedRecipe = 0;
         }

         this.refreshCurrentRecipe();
      }
   }

   private List<CrucibleRecipe> findCrucibleRecipes(ItemStack catalyst) {
      ArrayList<CrucibleRecipe> result = new ArrayList<>();

      for (RecipeHolder<CrucibleRecipe> holder : this.player.level().getRecipeManager().getAllRecipesFor(CrucibleRecipe.TYPE)) {
         CrucibleRecipe recipe = (CrucibleRecipe)holder.value();
         if (recipe.catalystMatches(catalyst)) {
            result.add(recipe);
         }
      }

      return result;
   }

   private CrucibleInscriberMenu.SaveState getSaveState() {
      if (!this.crucibleCoreHandler.hasCore()) {
         return CrucibleInscriberMenu.SaveState.DISABLED_MISSING_CORE;
      } else if (!this.currentRecipes.isEmpty() && this.selectedRecipe < this.currentRecipes.size()) {
         CrucibleRecipe recipe = this.currentRecipes.get(this.selectedRecipe);
         if (recipe.getResultItem().isEmpty()) {
            return CrucibleInscriberMenu.SaveState.DISABLED_INVALID_RECIPE;
         } else if (this.currentRecipeExists) {
            return CrucibleInscriberMenu.SaveState.ENABLED_DELETE;
         } else {
            return this.crucibleCoreHandler.hasRoomToStorePattern()
               ? CrucibleInscriberMenu.SaveState.ENABLED_SAVE
               : CrucibleInscriberMenu.SaveState.DISABLED_CORE_FULL;
         }
      } else {
         return CrucibleInscriberMenu.SaveState.DISABLED_INVALID_RECIPE;
      }
   }

   private void sendSaveState(boolean justSaved) {
      if (this.player instanceof ServerPlayer serverPlayer) {
         boolean canBack = this.canScroll(false);
         boolean canNext = this.canScroll(true);
         CrucibleInscriberSaveStatePacket.sendToPlayer(serverPlayer, this.getSaveState(), justSaved, canBack, canNext);
      }
   }

   public void onClientRequestSaveState() {
      this.refreshCurrentRecipe();
      this.sendSaveState(false);
   }

   public void onClientRequestSaveOrDelete(Player player) {
      CrucibleInscriberMenu.SaveState saveState = this.getSaveState();
      if (saveState == CrucibleInscriberMenu.SaveState.ENABLED_SAVE) {
         if (this.selectedRecipe >= this.currentRecipes.size()) {
            return;
         }

         CrucibleRecipe recipe = this.currentRecipes.get(this.selectedRecipe);
         ItemStack catalyst = this.catalystSlot.getItem();
         if (catalyst.isEmpty()) {
            return;
         }

         CruciblePatternItem patternItem = (CruciblePatternItem)ModItems.CRUCIBLE_PATTERN.get();
         ItemStack encodedStack = patternItem.encode(catalyst, recipe.getResultItem(), false, player.level());

         TECruciblePattern pattern;
         try {
            pattern = new TECruciblePattern(AEItemKey.of(encodedStack), player.level());
         } catch (IllegalStateException var9) {
            ThaumicEnergistics.LOGGER.warn("Failed to encode crucible pattern for {}: {}", recipe.getResultItem(), var9.getMessage());
            this.sendSaveState(false);
            return;
         }

         this.crucibleCoreHandler.addPattern(pattern);
         this.currentRecipeExists = true;
         this.updatePatternSlots();
         this.sendSaveState(true);
         this.inscriber.setChanged();
      } else if (saveState == CrucibleInscriberMenu.SaveState.ENABLED_DELETE) {
         if (this.selectedRecipe >= this.currentRecipes.size()) {
            return;
         }

         CrucibleRecipe recipex = this.currentRecipes.get(this.selectedRecipe);
         IPatternDetails toRemove = null;

         for (IPatternDetails p : this.crucibleCoreHandler.getPatterns()) {
            if (p instanceof TECruciblePattern cp && cp.isSimilar(recipex)) {
               toRemove = p;
               break;
            }
         }

         if (toRemove != null) {
            this.crucibleCoreHandler.removePattern(toRemove);
            this.currentRecipeExists = false;
            this.updatePatternSlots();
            this.sendSaveState(false);
            this.inscriber.setChanged();
         }
      }
   }

   private void updatePatternSlots() {
      Iterator<ItemStack> iterator = null;
      if (this.crucibleCoreHandler.hasCore()) {
         ArrayList<ItemStack> storedResults = this.crucibleCoreHandler.getStoredOutputs();
         iterator = storedResults.iterator();
      }

      for (Slot patternSlot : this.patternSlots) {
         if (iterator != null && iterator.hasNext()) {
            patternSlot.set(iterator.next());
         } else {
            patternSlot.set(ItemStack.EMPTY);
         }
      }
   }

   public void broadcastChanges() {
      super.broadcastChanges();
      ItemStack currentCoreStack = this.crucibleCoreSlot.getItem();
      if (!ItemStackData.sameItemSameTags(currentCoreStack, this.lastCoreStack)) {
         this.lastCoreStack = currentCoreStack.copy();
         if (!currentCoreStack.isEmpty() && currentCoreStack.getItem() instanceof ItemCrucibleCore) {
            this.crucibleCoreHandler.open(currentCoreStack, this.player.level());
         } else {
            this.crucibleCoreHandler.close();
         }

         this.updatePatternSlots();
         this.refreshCurrentRecipe();
      }
   }

   public boolean stillValid(@NotNull Player player) {
      return BlockEntityMenuValidator.isValid(player, this.inscriber);
   }

   public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
      if (slotId >= 0 && slotId < this.slots.size()) {
         Slot slot = this.getSlot(slotId);
         if (slot == this.catalystSlot) {
            ItemStack carried = this.getCarried();
            if (!carried.isEmpty()) {
               ItemStack copiedStack = carried.copy();
               copiedStack.setCount(1);
               slot.set(copiedStack);
            } else {
               slot.set(ItemStack.EMPTY);
            }

            if (!player.level().isClientSide) {
               this.refreshCurrentRecipe();
            }

            return;
         }

         for (Slot patternSlot : this.patternSlots) {
            if (patternSlot == slot) {
               // Как в Knowledge/Infusion: клик по сохранённому паттерну
               // загружает его обратно в сетку (здесь — катализатор).
               if (patternSlot.hasItem()) {
                  GenericStack gs = GenericStack.fromItemStack(patternSlot.getItem());
                  if (gs != null && this.crucibleCoreHandler.getPatternForItem(gs) instanceof TECruciblePattern cruciblePattern
                     && cruciblePattern.getRecipe() != null) {
                     ItemStack[] options = cruciblePattern.getRecipe().catalyst.getItems();
                     if (options.length > 0) {
                        ItemStack catalyst = options[0].copy();
                        catalyst.setCount(1);
                        this.catalystSlot.set(catalyst);
                        this.refreshCurrentRecipe();
                     }
                  }
               }

               return;
            }
         }

         if (slot == this.resultSlot) {
            return;
         }
      }

      super.clicked(slotId, button, clickType, player);
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
      if (player.level().isClientSide) {
         return ItemStack.EMPTY;
      } else {
         Slot slot = slotIndex >= 0 && slotIndex < this.slots.size() ? (Slot)this.slots.get(slotIndex) : null;
         if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            boolean didMerge = false;
            if (slotIndex >= 0 && slotIndex < 36) {
               if (slotStack.getItem() instanceof ItemCrucibleCore) {
                  didMerge = this.moveItemStackTo(slotStack, 36, 37, false);
               }

               if (!didMerge) {
                  if (slotIndex < 27) {
                     didMerge = this.moveItemStackTo(slotStack, 27, 36, false);
                  } else {
                     didMerge = this.moveItemStackTo(slotStack, 0, 27, false);
                  }
               }
            } else if (slotIndex == 36) {
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

   public static class GhostSlot extends Slot {
      public GhostSlot(Container container, int index, int x, int y) {
         super(container, index, x, y);
      }

      public boolean mayPlace(@NotNull ItemStack stack) {
         return false;
      }

      public boolean mayPickup(@NotNull Player player) {
         return false;
      }

      public void set(@NotNull ItemStack stack) {
         if (!stack.isEmpty()) {
            stack = stack.copy();
            stack.setCount(Math.min(stack.getCount(), this.getMaxStackSize()));
         }

         super.set(stack);
      }
   }

   public static enum SaveState {
      DISABLED_INVALID_RECIPE,
      DISABLED_CORE_FULL,
      DISABLED_MISSING_CORE,
      ENABLED_SAVE,
      ENABLED_DELETE;
   }
}
