package thaumicenergistics.menu;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.ae2.pattern.InfusionPatternItem;
import thaumicenergistics.ae2.pattern.TEInfusionPattern;
import thaumicenergistics.block.infusion_inscriber.InfusionInscriberBlockEntity;
import thaumicenergistics.init.ModItems;
import thaumicenergistics.init.ModMenuTypes;
import thaumicenergistics.item.ItemBrainCore;
import thaumicenergistics.network.InfusionInscriberSaveStatePacket;
import thaumicenergistics.util.HandlerBrainCore;
import thaumicenergistics.util.InfusionRecipeHelper;
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
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.util.BlockEntityMenuValidator;
import thaumcraft.util.ItemStackData;

public class InfusionInscriberMenu extends AbstractContainerMenu {
   private static final int PLAYER_INV_POSITION_Y = 174;
   private static final int HOTBAR_INV_POSITION_Y = 232;
   public static final int KCORE_SLOT_X = 184;
   public static final int KCORE_SLOT_Y = 8;
   private static final int PATTERN_SLOT_X = 8;
   private static final int PATTERN_SLOT_Y = 100;
   private static final int PATTERN_ROWS = 4;
   private static final int PATTERN_COLS = 9;
   private static final int PATTERN_SLOT_SPACING = 18;
   private static final int PATTERN_SLOT_COUNT = 36;
   public static final int CRAFTING_SLOT_X = 26;
   public static final int CRAFTING_SLOT_Y = 90;
   public static final int CRAFTING_ROWS = 4;
   public static final int CRAFTING_COLS = 4;
   public static final int CRAFTING_GRID_SIZE = 17;
   public static final int CRAFTING_SLOT_SPACING = 18;
   private static final int PLAYER_INV_START = 0;
   private static final int PLAYER_INV_END = 27;
   private static final int HOTBAR_START = 27;
   private static final int HOTBAR_END = 36;
   private static final int KCORE_SLOT_INDEX = 36;
   private static final int PATTERN_START = 37;
   private static final int PATTERN_END = 73;
   private static final int CRAFTING_START = 73;
   private static final int CRAFTING_END = 90;
   private static final int RESULT_SLOT_INDEX = 90;
   public final InfusionInscriberBlockEntity inscriber;
   private final Player player;
   private final SimpleContainer ghostContainer;
   private final Slot[] patternSlots = new Slot[36];
   private final Slot[] craftingSlots = new Slot[17];
   private Slot resultSlot;
   private Slot brainCoreSlot;
   private final HandlerBrainCore brainCoreHandler;
   private InfusionRecipe activeRecipe = null;
   private ItemStack lastBrainCoreStack = ItemStack.EMPTY;

   public InfusionInscriberMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
      this(containerId, inventory, getBlockEntity(inventory, buf));
   }

   public InfusionInscriberMenu(int containerId, Inventory playerInv, InfusionInscriberBlockEntity inscriber) {
      super((MenuType)ModMenuTypes.INFUSION_INSCRIBER.get(), containerId);
      this.inscriber = inscriber;
      this.player = playerInv.player;
      int ghostSize = 54;
      this.ghostContainer = new SimpleContainer(ghostSize);
      this.bindPlayerInventory(playerInv);
      Container brainCoreContainer = inscriber.getInternalInventory().toContainer();
      this.brainCoreSlot = this.addSlot(new Slot(brainCoreContainer, 0, 184, 8) {
         public boolean mayPlace(@NotNull ItemStack stack) {
            return stack.getItem() instanceof ItemBrainCore;
         }

         public int getMaxStackSize() {
            return 1;
         }
      });
      this.initPatternSlots();
      this.initCraftingSlots();
      int resultGhostIndex = 53;
      this.resultSlot = this.addSlot(new InfusionInscriberMenu.GhostSlot(this.ghostContainer, resultGhostIndex, 132, 45));
      this.brainCoreHandler = new HandlerBrainCore(36, TEInfusionPattern::new);
   }

   private static InfusionInscriberBlockEntity getBlockEntity(Inventory inventory, FriendlyByteBuf buf) {
      BlockEntity blockEntity = inventory.player.level().getBlockEntity(buf.readBlockPos());
      if (blockEntity instanceof InfusionInscriberBlockEntity) {
         return (InfusionInscriberBlockEntity)blockEntity;
      } else {
         throw new IllegalStateException("Expected InfusionInscriberBlockEntity at menu position");
      }
   }

   private void bindPlayerInventory(Inventory playerInv) {
      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 174 + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         this.addSlot(new Slot(playerInv, col, 8 + col * 18, 232));
      }
   }

   private void initPatternSlots() {
      for (int row = 0; row < 4; row++) {
         for (int col = 0; col < 9; col++) {
            int index = row * 9 + col;
            int posX = 8 + 18 * col;
            int posY = 100 + 18 * row;
            this.patternSlots[index] = this.addSlot(new InfusionInscriberMenu.DisplaySlot(this.ghostContainer, index, posX, posY));
         }
      }
   }

   private void initCraftingSlots() {
      int slotIndex = 0;
      int ghostOffset = 36;
      this.craftingSlots[slotIndex] = this.addSlot(new InfusionInscriberMenu.GhostSlot(this.ghostContainer, ghostOffset + slotIndex, 45, 45));
      slotIndex++;

      for (int y = 0; y < 5; y++) {
         if (y != 0 && y != 4) {
            this.craftingSlots[slotIndex] = this.addSlot(new InfusionInscriberMenu.GhostSlot(this.ghostContainer, ghostOffset + slotIndex, 8, 8 + y * 18));
            slotIndex++;
            this.craftingSlots[slotIndex] = this.addSlot(new InfusionInscriberMenu.GhostSlot(this.ghostContainer, ghostOffset + slotIndex, 80, 8 + y * 18));
            slotIndex++;
         } else {
            for (int x = 0; x < 5; x++) {
               this.craftingSlots[slotIndex] = this.addSlot(
                  new InfusionInscriberMenu.GhostSlot(this.ghostContainer, ghostOffset + slotIndex, 8 + x * 18, 8 + y * 18)
               );
               slotIndex++;
            }
         }
      }
   }

   private InfusionInscriberMenu.SaveState getSaveState() {
      if (!this.brainCoreHandler.hasCore()) {
         return InfusionInscriberMenu.SaveState.DISABLED_MISSING_CORE;
      } else if (this.activeRecipe == null) {
         return InfusionInscriberMenu.SaveState.DISABLED_INVALID_RECIPE;
      } else {
         ItemStack recipeOutput = this.activeRecipe.getResultItem();
         if (recipeOutput.isEmpty()) {
            return InfusionInscriberMenu.SaveState.DISABLED_INVALID_RECIPE;
         } else {
            boolean isNew = !this.brainCoreHandler.hasPatternFor(recipeOutput);
            if (isNew) {
               return this.brainCoreHandler.hasRoomToStorePattern()
                  ? InfusionInscriberMenu.SaveState.ENABLED_SAVE
                  : InfusionInscriberMenu.SaveState.DISABLED_CORE_FULL;
            } else {
               return InfusionInscriberMenu.SaveState.ENABLED_DELETE;
            }
         }
      }
   }

   private void loadPattern(TEInfusionPattern pattern) {
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
      if (this.brainCoreHandler.hasCore()) {
         ArrayList<ItemStack> storedResults = this.brainCoreHandler.getStoredOutputs();
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
         InfusionInscriberSaveStatePacket.sendToPlayer(serverPlayer, this.getSaveState(), justSaved);
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
      InfusionInscriberMenu.SaveState saveState = this.getSaveState();
      if (saveState == InfusionInscriberMenu.SaveState.ENABLED_SAVE) {
         List<ItemStack> stacks = new ArrayList<>();

         for (Slot craftingSlot : this.craftingSlots) {
            ItemStack stack = craftingSlot.getItem();
            if (!stack.isEmpty()) {
               stacks.add(stack.copy());
            }
         }

         ItemStack[] inputs = stacks.toArray(new ItemStack[0]);
         ItemStack output = this.resultSlot.getItem();
         if (output.isEmpty()) {
            return;
         }

         InfusionPatternItem patternItem = (InfusionPatternItem)ModItems.INFUSION_PATTERN.get();
         ItemStack encodedStack = patternItem.encode(inputs, output, false, player.level());

         TEInfusionPattern pattern;
         try {
            pattern = new TEInfusionPattern(AEItemKey.of(encodedStack), player.level());
         } catch (IllegalStateException var10) {
            ThaumicEnergistics.LOGGER.warn("Failed to encode infusion pattern for {}: {}", output, var10.getMessage());
            this.sendSaveState(false);
            return;
         }

         this.brainCoreHandler.addPattern(pattern);
         this.updatePatternSlots();
         this.loadPattern(pattern);
         this.sendSaveState(true);
         this.inscriber.setChanged();
      } else if (saveState == InfusionInscriberMenu.SaveState.ENABLED_DELETE) {
         GenericStack resultStack = GenericStack.fromItemStack(this.resultSlot.getItem());
         if (resultStack == null) {
            return;
         }

         IPatternDetails pattern = this.brainCoreHandler.getPatternForItem(resultStack);
         if (pattern != null) {
            this.brainCoreHandler.removePattern(pattern);
            this.updatePatternSlots();
            this.sendSaveState(false);
            this.inscriber.setChanged();
         }
      }
   }

   private void onCraftMatrixChanged() {
      this.activeRecipe = InfusionRecipeHelper.INSTANCE.findMatchingMatrixResult(this.ghostContainer, 36, 17, this.player);
      ItemStack craftResult = ItemStack.EMPTY;
      if (this.activeRecipe != null) {
         craftResult = this.activeRecipe.getResultItem();
      }

      this.resultSlot.set(craftResult);
      if (!this.player.level().isClientSide) {
         this.sendSaveState(false);
      }

      this.broadcastChanges();
   }

   public void broadcastChanges() {
      super.broadcastChanges();
      ItemStack currentCoreStack = this.brainCoreSlot.getItem();
      if (!ItemStackData.sameItemSameTags(currentCoreStack, this.lastBrainCoreStack)) {
         this.lastBrainCoreStack = currentCoreStack.copy();
         if (!currentCoreStack.isEmpty() && currentCoreStack.getItem() instanceof ItemBrainCore) {
            this.brainCoreHandler.open(currentCoreStack, this.player.level());
         } else {
            this.brainCoreHandler.close();
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
                  if (gs != null && this.brainCoreHandler.getPatternForItem(gs) instanceof TEInfusionPattern infusionPattern) {
                     this.loadPattern(infusionPattern);
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
               if (slotStack.getItem() instanceof ItemBrainCore) {
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
