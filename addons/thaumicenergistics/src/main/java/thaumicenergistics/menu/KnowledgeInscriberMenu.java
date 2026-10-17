package thaumicenergistics.menu;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import java.util.ArrayList;
import java.util.Iterator;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.ae2.pattern.ArcanePatternItem;
import thaumicenergistics.ae2.pattern.TEArcanePattern;
import thaumicenergistics.block.knowledge_inscriber.KnowledgeInscriberBlockEntity;
import thaumicenergistics.init.ModItems;
import thaumicenergistics.init.ModMenuTypes;
import thaumicenergistics.item.ItemKnowledgeCore;
import thaumicenergistics.network.KnowledgeInscriberSaveStatePacket;
import thaumicenergistics.util.ArcaneRecipeHelper;
import thaumicenergistics.util.HandlerKnowledgeCore;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.util.BlockEntityMenuValidator;
import thaumcraft.util.ItemStackData;

public class KnowledgeInscriberMenu extends AbstractContainerMenu {
   private static final int PLAYER_INV_POSITION_Y = 162;
   private static final int HOTBAR_INV_POSITION_Y = 220;
   public static final int KCORE_SLOT_X = 186;
   public static final int KCORE_SLOT_Y = 8;
   public static final int PATTERN_SLOT_X = 26;
   public static final int PATTERN_SLOT_Y = 18;
   public static final int PATTERN_ROWS = 3;
   public static final int PATTERN_COLS = 7;
   public static final int PATTERN_SLOT_SPACING = 18;
   public static final int PATTERN_SLOT_COUNT = 21;
   public static final int CRAFTING_SLOT_X = 26;
   public static final int CRAFTING_SLOT_Y = 90;
   public static final int CRAFTING_ROWS = 3;
   public static final int CRAFTING_COLS = 3;
   public static final int CRAFTING_GRID_SIZE = 9;
   public static final int CRAFTING_SLOT_SPACING = 18;
   private static final int PLAYER_INV_START = 0;
   private static final int PLAYER_INV_END = 27;
   private static final int HOTBAR_START = 27;
   private static final int HOTBAR_END = 36;
   private static final int KCORE_SLOT_INDEX = 36;
   private static final int PATTERN_START = 37;
   private static final int PATTERN_END = 58;
   private static final int CRAFTING_START = 58;
   private static final int CRAFTING_END = 67;
   private static final int RESULT_SLOT_INDEX = 67;
   public final KnowledgeInscriberBlockEntity inscriber;
   private final Player player;
   private final SimpleContainer ghostContainer;
   private final Slot[] patternSlots = new Slot[21];
   private final Slot[] craftingSlots = new Slot[9];
   private Slot resultSlot;
   private Slot knowledgeCoreSlot;
   private final HandlerKnowledgeCore knowledgeCoreHandler;
   private IArcaneRecipe activeRecipe = null;
   private ItemStack lastKnowledgeCoreStack = ItemStack.EMPTY;

   public KnowledgeInscriberMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
      this(containerId, inventory, getBlockEntity(inventory, buf));
   }

   public KnowledgeInscriberMenu(int containerId, Inventory playerInv, KnowledgeInscriberBlockEntity inscriber) {
      super((MenuType)ModMenuTypes.KNOWLEDGE_INSCRIBER.get(), containerId);
      this.inscriber = inscriber;
      this.player = playerInv.player;
      int ghostSize = 31;
      this.ghostContainer = new SimpleContainer(ghostSize);
      this.bindPlayerInventory(playerInv);
      Container knowledgeCoreContainer = inscriber.getInternalInventory().toContainer();
      this.knowledgeCoreSlot = this.addSlot(new Slot(knowledgeCoreContainer, 0, 186, 8) {
         public boolean mayPlace(@NotNull ItemStack stack) {
            return stack.getItem() instanceof ItemKnowledgeCore;
         }

         public int getMaxStackSize() {
            return 1;
         }
      });
      this.initPatternSlots();
      this.initCraftingSlots();
      int resultGhostIndex = 30;
      this.resultSlot = this.addSlot(new KnowledgeInscriberMenu.GhostSlot(this.ghostContainer, resultGhostIndex, 116, 108));
      this.knowledgeCoreHandler = new HandlerKnowledgeCore(21, TEArcanePattern::new);
   }

   private static KnowledgeInscriberBlockEntity getBlockEntity(Inventory inventory, FriendlyByteBuf buf) {
      BlockEntity blockEntity = inventory.player.level().getBlockEntity(buf.readBlockPos());
      if (blockEntity instanceof KnowledgeInscriberBlockEntity) {
         return (KnowledgeInscriberBlockEntity)blockEntity;
      } else {
         throw new IllegalStateException("Expected KnowledgeInscriberBlockEntity at menu position");
      }
   }

   private void bindPlayerInventory(Inventory playerInv) {
      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 162 + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         this.addSlot(new Slot(playerInv, col, 8 + col * 18, 220));
      }
   }

   private void initPatternSlots() {
      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 7; col++) {
            int index = row * 7 + col;
            int posX = 26 + 18 * col;
            int posY = 18 + 18 * row;
            this.patternSlots[index] = this.addSlot(new KnowledgeInscriberMenu.DisplaySlot(this.ghostContainer, index, posX, posY));
         }
      }
   }

   private void initCraftingSlots() {
      int ghostOffset = 21;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 3; col++) {
            int slotIndex = row * 3 + col;
            int posX = 26 + col * 18;
            int posY = 90 + row * 18;
            this.craftingSlots[slotIndex] = this.addSlot(new KnowledgeInscriberMenu.GhostSlot(this.ghostContainer, ghostOffset + slotIndex, posX, posY));
         }
      }
   }

   private KnowledgeInscriberMenu.SaveState getSaveState() {
      if (!this.knowledgeCoreHandler.hasCore()) {
         return KnowledgeInscriberMenu.SaveState.DISABLED_MISSING_CORE;
      } else if (this.activeRecipe == null) {
         return KnowledgeInscriberMenu.SaveState.DISABLED_INVALID_RECIPE;
      } else {
         ItemStack recipeOutput = this.resultSlot.getItem();
         if (recipeOutput.isEmpty()) {
            return KnowledgeInscriberMenu.SaveState.DISABLED_INVALID_RECIPE;
         } else {
            boolean isNew = !this.knowledgeCoreHandler.hasPatternFor(recipeOutput);
            if (isNew) {
               return this.knowledgeCoreHandler.hasRoomToStorePattern()
                  ? KnowledgeInscriberMenu.SaveState.ENABLED_SAVE
                  : KnowledgeInscriberMenu.SaveState.DISABLED_CORE_FULL;
            } else {
               return KnowledgeInscriberMenu.SaveState.ENABLED_DELETE;
            }
         }
      }
   }

   private void loadPattern(TEArcanePattern pattern) {
      if (pattern != null) {
         for (Slot slot : this.craftingSlots) {
            slot.set(ItemStack.EMPTY);
         }

         GenericStack[] sparseInputs = pattern.getSparseInputs();

         for (int index = 0; index < Math.min(sparseInputs.length, this.craftingSlots.length); index++) {
            GenericStack ingStack = sparseInputs[index];
            if (ingStack != null && ingStack.what() instanceof AEItemKey itemKey) {
               this.craftingSlots[index].set(itemKey.toStack());
            } else {
               this.craftingSlots[index].set(ItemStack.EMPTY);
            }
         }
      }
   }

   private void updatePatternSlots() {
      Iterator<ItemStack> iterator = null;
      if (this.knowledgeCoreHandler.hasCore()) {
         ArrayList<ItemStack> storedResults = this.knowledgeCoreHandler.getStoredOutputs();
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

   private void sendSaveState(boolean justSaved) {
      if (this.player instanceof ServerPlayer serverPlayer) {
         KnowledgeInscriberSaveStatePacket.sendToPlayer(serverPlayer, this.getSaveState(), justSaved);
      }
   }

   public void onClientRequestSaveState() {
      this.sendSaveState(false);
   }

   public void onClientRequestClearGrid() {
      for (Slot slot : this.craftingSlots) {
         slot.set(ItemStack.EMPTY);
      }

      this.onCraftMatrixChanged();
   }

   public void acceptJEIRecipe(ItemStack[] inputs) {
      for (int i = 0; i < this.craftingSlots.length; i++) {
         ItemStack stack = inputs != null && i < inputs.length ? inputs[i] : ItemStack.EMPTY;
         this.craftingSlots[i].set(stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
      }

      this.onCraftMatrixChanged();
   }

   public void onClientRequestSaveOrDelete(Player player) {
      KnowledgeInscriberMenu.SaveState saveState = this.getSaveState();
      if (saveState == KnowledgeInscriberMenu.SaveState.ENABLED_SAVE) {
         ItemStack[] inputs = new ItemStack[9];

         for (int i = 0; i < this.craftingSlots.length; i++) {
            ItemStack stack = this.craftingSlots[i].getItem();
            inputs[i] = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
         }

         ItemStack output = this.resultSlot.getItem();
         if (output.isEmpty()) {
            return;
         }

         ArcanePatternItem patternItem = (ArcanePatternItem)ModItems.ARCANE_PATTERN.get();
         ItemStack encodedStack = patternItem.encode(inputs, output, false, player.level());

         TEArcanePattern pattern;
         try {
            pattern = new TEArcanePattern(AEItemKey.of(encodedStack), player.level());
         } catch (IllegalStateException var9) {
            ThaumicEnergistics.LOGGER.warn("Failed to encode arcane pattern for {}: {}", output, var9.getMessage());
            this.sendSaveState(false);
            return;
         }

         this.knowledgeCoreHandler.addPattern(pattern);
         this.updatePatternSlots();
         this.loadPattern(pattern);
         this.sendSaveState(true);
         this.inscriber.setChanged();
      } else if (saveState == KnowledgeInscriberMenu.SaveState.ENABLED_DELETE) {
         GenericStack resultStack = GenericStack.fromItemStack(this.resultSlot.getItem());
         if (resultStack == null) {
            return;
         }

         IPatternDetails pattern = this.knowledgeCoreHandler.getPatternForItem(resultStack);
         if (pattern != null) {
            this.knowledgeCoreHandler.removePattern(pattern);
            this.updatePatternSlots();
            this.sendSaveState(false);
            this.inscriber.setChanged();
         }
      }
   }

   private void onCraftMatrixChanged() {
      this.activeRecipe = ArcaneRecipeHelper.INSTANCE.findMatchingMatrixResult(this.ghostContainer, 21, 9, this.player);
      ItemStack craftResult = this.activeRecipe == null
         ? ItemStack.EMPTY
         : ArcaneRecipeHelper.INSTANCE.findMatchingMatrixOutput(this.ghostContainer, 21, 9, this.player);
      this.resultSlot.set(craftResult);
      if (!this.player.level().isClientSide) {
         this.sendSaveState(false);
      }

      this.broadcastChanges();
   }

   public void broadcastChanges() {
      super.broadcastChanges();
      ItemStack currentCoreStack = this.knowledgeCoreSlot.getItem();
      if (!ItemStackData.sameItemSameTags(currentCoreStack, this.lastKnowledgeCoreStack)) {
         this.lastKnowledgeCoreStack = currentCoreStack.copy();
         if (!currentCoreStack.isEmpty() && currentCoreStack.getItem() instanceof ItemKnowledgeCore) {
            this.knowledgeCoreHandler.open(currentCoreStack, this.player.level());
         } else {
            this.knowledgeCoreHandler.close();
         }

         this.updatePatternSlots();
         this.sendSaveState(false);
      }
   }

   public boolean stillValid(@NotNull Player player) {
      return BlockEntityMenuValidator.isValid(player, this.inscriber);
   }

   public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
      if (slotId >= 0 && slotId < this.slots.size()) {
         Slot slot = this.getSlot(slotId);

         for (Slot craftSlot : this.craftingSlots) {
            if (craftSlot == slot) {
               ItemStack carried = this.getCarried();
               if (!carried.isEmpty()) {
                  ItemStack copiedStack = carried.copy();
                  copiedStack.setCount(1);
                  slot.set(copiedStack);
               } else {
                  slot.set(ItemStack.EMPTY);
               }

               this.onCraftMatrixChanged();
               return;
            }
         }

         for (Slot patternSlot : this.patternSlots) {
            if (patternSlot == slot) {
               if (patternSlot.hasItem()) {
                  GenericStack gs = GenericStack.fromItemStack(patternSlot.getItem());
                  if (gs != null && this.knowledgeCoreHandler.getPatternForItem(gs) instanceof TEArcanePattern arcanePattern) {
                     this.loadPattern(arcanePattern);
                     this.onCraftMatrixChanged();
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
               if (slotStack.getItem() instanceof ItemKnowledgeCore) {
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
      DISABLED_RECIPE,
      ENABLED_DELETE;
   }
}
