package thaumicenergistics.menu;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.ITerminalHost;
import appeng.core.network.serverbound.InventoryActionPacket;
import appeng.helpers.ICraftingGridMenu;
import appeng.helpers.InventoryAction;
import appeng.helpers.ICraftingGridMenu.AutoCraftEntry;
import appeng.me.helpers.PlayerSource;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.CraftingMatrixSlot;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;
import thaumicenergistics.init.ModMenuTypes;
import thaumicenergistics.init.ModSlotSemantics;
import thaumicenergistics.menu.slot.ArcaneCraftingTermSlot;
import thaumicenergistics.part.PartArcaneTerminal;
import thaumicenergistics.util.ArcaneRecipeHelper;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.util.ItemStackData;

public class ArcaneCraftingTermMenu extends MEStorageMenu implements ICraftingGridMenu {
   private final ISegmentedInventory ct;
   private final CraftingMatrixSlot[] craftingSlots = new CraftingMatrixSlot[9];
   private final ArcaneCraftingTermSlot outputSlot;
   private final AppEngSlot wandSlot;
   private final Slot[] armorSlots = new Slot[4];
   private final CraftingContainer recipeTestContainer = new TransientCraftingContainer(this, 3, 3);
   private IArcaneRecipe arcaneRecipe;

   public ArcaneCraftingTermMenu(int id, Inventory ip, ITerminalHost host) {
      this((MenuType<?>)ModMenuTypes.ARCANE_CRAFTING_TERM.get(), id, ip, host, true);
   }

   public ArcaneCraftingTermMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host, boolean bindInventory) {
      super(menuType, id, ip, host, bindInventory);
      this.ct = (ISegmentedInventory)host;
      InternalInventory craftingGridInv = this.ct.getSubInventory(PartArcaneTerminal.INV_CRAFTING);

      for (int i = 0; i < 9; i++) {
         this.addSlot(this.craftingSlots[i] = new CraftingMatrixSlot(this, craftingGridInv, i), SlotSemantics.CRAFTING_GRID);
      }

      this.addSlot(
         this.outputSlot = new ArcaneCraftingTermSlot(
            this.getPlayerInventory().player,
            this.getActionSource(),
            this.energySource,
            host.getInventory(),
            craftingGridInv,
            craftingGridInv,
            this.ct.getSubInventory(PartArcaneTerminal.INV_WAND),
            this
         ),
         SlotSemantics.CRAFTING_RESULT
      );
      InternalInventory wandInv = this.ct.getSubInventory(PartArcaneTerminal.INV_WAND);
      this.wandSlot = new ArcaneCraftingTermMenu.WandSlot(wandInv, 0);
      this.addSlot(this.wandSlot, ModSlotSemantics.WAND);
      EquipmentSlot[] armorOrder = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

      for (int i = 0; i < 4; i++) {
         int armorIndex = 3 - i;
         int invIndex = ip.items.size() + armorIndex;
         this.armorSlots[i] = this.addSlot(new ArcaneCraftingTermMenu.ArmorSlot(ip, invIndex, armorOrder[i]), ModSlotSemantics.ARCANE_ARMOR);
      }

      this.updateCurrentRecipeAndOutput(true);
   }

   public void slotsChanged(Container inventory) {
      if (inventory != this.recipeTestContainer) {
         this.updateCurrentRecipeAndOutput(false);
      }
   }

   private void updateCurrentRecipeAndOutput(boolean forceUpdate) {
      boolean hasChanged = forceUpdate;

      for (int x = 0; x < 9; x++) {
         ItemStack stack = this.craftingSlots[x].getItem();
         if (!ItemStackData.sameItemSameTags(stack, this.recipeTestContainer.getItem(x))) {
            hasChanged = true;
            this.recipeTestContainer.setItem(x, stack.copy());
         }
      }

      if (hasChanged) {
         Level level = this.getPlayerInventory().player.level();
         this.arcaneRecipe = ArcaneRecipeHelper.INSTANCE.findMatchingMatrixResult(this.recipeTestContainer, 0, 9, this.getPlayerInventory().player);
         if (this.arcaneRecipe != null) {
            if (this.canUseWandForRecipe(this.arcaneRecipe)) {
               this.outputSlot.setDisplayedCraftingOutput(this.arcaneRecipe.assemble(createCraftingInput(this.recipeTestContainer), level.registryAccess()));
            } else {
               this.outputSlot.setDisplayedCraftingOutput(ItemStack.EMPTY);
            }
         } else {
            RecipeHolder<CraftingRecipe> vanillaRecipe = (RecipeHolder<CraftingRecipe>)level.getRecipeManager()
               .getRecipeFor(RecipeType.CRAFTING, createCraftingInput(this.recipeTestContainer), level)
               .orElse(null);
            if (vanillaRecipe != null) {
               this.outputSlot
                  .setDisplayedCraftingOutput(
                     ((CraftingRecipe)vanillaRecipe.value()).assemble(createCraftingInput(this.recipeTestContainer), level.registryAccess())
                  );
            } else {
               this.outputSlot.setDisplayedCraftingOutput(ItemStack.EMPTY);
            }
         }
      }
   }

   public IArcaneRecipe getArcaneRecipe() {
      return this.arcaneRecipe;
   }

   public AppEngSlot getWandSlot() {
      return this.wandSlot;
   }

   public CraftingMatrixSlot[] getCraftingSlots() {
      return this.craftingSlots;
   }

   public void clearCraftingGrid() {
      Preconditions.checkState(this.isClientSide());
      CraftingMatrixSlot slot = this.craftingSlots[0];
      InventoryActionPacket p = new InventoryActionPacket(InventoryAction.MOVE_REGION, slot.index, 0L);
      PacketDistributor.sendToServer(p, new CustomPacketPayload[0]);
   }

   public InternalInventory getCraftingMatrix() {
      return this.ct.getSubInventory(PartArcaneTerminal.INV_CRAFTING);
   }

   public IEnergySource getEnergySource() {
      return this.energySource;
   }

   public void startAutoCrafting(List<AutoCraftEntry> toCraft) {
      CraftConfirmMenu.openWithCraftingList(this.getActionHost(), (ServerPlayer)this.getPlayer(), this.getLocator(), toCraft);
   }

   public void fillCraftingGrid(Player player, AEItemKey[] gridItems) {
      if (this.storage != null) {
         this.doAction((ServerPlayer)player, InventoryAction.MOVE_REGION, this.craftingSlots[0].index, 0L);

         for (CraftingMatrixSlot craftingSlot : this.craftingSlots) {
            if (craftingSlot.hasItem()) {
               return;
            }
         }

         InternalInventory craftingGridInv = this.ct.getSubInventory(PartArcaneTerminal.INV_CRAFTING);

         for (int i = 0; i < 9; i++) {
            AEItemKey slotStack = gridItems[i];
            if (slotStack != null) {
               ItemStack matchingStack = this.requestCraftingReplenishment(player, slotStack);
               if (matchingStack != null) {
                  craftingGridInv.setItemDirect(i, matchingStack);
               }
            }
         }

         this.broadcastChanges();
      }
   }

   public ItemStack requestCraftingReplenishment(Player player, AEItemKey stack) {
      if (this.storage == null) {
         return null;
      } else {
         PlayerSource source = new PlayerSource(player, this.getActionHost());
         long replenishment = this.storage.extract(stack, 1L, Actionable.MODULATE, source);
         if (replenishment == 1L) {
            return stack.toStack();
         } else {
            KeyCounter inventoryList = this.storage.getAvailableStacks();
            Collection<Entry<AEKey>> itemList = inventoryList.findFuzzy(stack, FuzzyMode.IGNORE_ALL);
            if (!itemList.isEmpty()) {
               for (java.util.Map.Entry<AEKey, Long> entry : itemList) {
                  AEKey key = entry.getKey();
                  if (key instanceof AEItemKey itemKey) {
                     replenishment = this.storage.extract(itemKey, 1L, Actionable.MODULATE, source);
                     if (replenishment == 1L) {
                        return itemKey.toStack();
                     }
                     break;
                  }
               }
            }

            Inventory playerInventory = player.getInventory();

            for (int i = 0; i < playerInventory.items.size(); i++) {
               if (!this.isPlayerInventorySlotLocked(i)) {
                  ItemStack playerStack = playerInventory.getItem(i);
                  if (stack.matches(playerStack)) {
                     ItemStack extracted = playerStack.split(1);
                     if (!extracted.isEmpty()) {
                        return extracted;
                     }
                  }
               }
            }

            return null;
         }
      }
   }

   private static boolean isValidWand(ItemStack stack, boolean allowStaff) {
      return !stack.isEmpty() && stack.getItem() instanceof ItemWandCasting wand ? allowStaff || !wand.isStaff(stack) : false;
   }

   private boolean canUseWandForRecipe(IArcaneRecipe recipe) {
      AspectList aspects = recipe.getAspects();
      if (aspects != null && aspects.size() != 0) {
         ItemStack wandStack = this.wandSlot.getItem();
         return isValidWand(wandStack, false) && wandStack.getItem() instanceof ItemWandCasting wand
            ? wand.consumeAllVisCrafting(wandStack, this.getPlayerInventory().player, aspects, false)
            : false;
      } else {
         return true;
      }
   }

   private static CraftingInput createCraftingInput(CraftingContainer container) {
      return CraftingInput.of(3, 3, IntStream.range(0, container.getContainerSize()).mapToObj(container::getItem).toList());
   }

   public static class ArmorSlot extends Slot {
      private final Player player;
      private final EquipmentSlot slotType;

      public ArmorSlot(Inventory inv, int idx, EquipmentSlot slotType) {
         super(inv, idx, 0, 0);
         this.player = inv.player;
         this.slotType = slotType;
      }

      public boolean mayPlace(ItemStack stack) {
         return !stack.isEmpty() && this.player.getEquipmentSlotForItem(stack) == this.slotType;
      }

      public int getMaxStackSize() {
         return 1;
      }
   }

   public static class WandSlot extends AppEngSlot {
      public WandSlot(InternalInventory inv, int idx) {
         super(inv, idx);
      }

      public boolean mayPlace(ItemStack stack) {
         return ArcaneCraftingTermMenu.isValidWand(stack, false);
      }
   }
}
