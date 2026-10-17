package thaumcraft.block.arcane_workbench;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.CraftingInput.Positioned;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.recipe.ThaumcraftCraftingManager;
import thaumcraft.util.ItemStackData;

public class SlotCraftingArcaneWorkbench extends ResultSlot {
   private final CraftingContainer craftSlots;
   private final ArcaneWorkbenchMenu menu;

   public SlotCraftingArcaneWorkbench(ArcaneWorkbenchMenu menu, Player player, CraftingContainer craftSlots, Container inventory, int index, int x, int y) {
      super(player, craftSlots, inventory, index, x, y);
      this.craftSlots = craftSlots;
      this.menu = menu;
   }

   public boolean mayPlace(@NotNull ItemStack stack) {
      return false;
   }

   public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
      TransientCraftingContainer snapshot = new TransientCraftingContainer(DummyMenu.INSTANCE, 3, 3);

      for (int i = 0; i < 9; i++) {
         snapshot.setItem(i, this.craftSlots.getItem(i).copy());
      }

      Positioned positionedInput = CraftingInput.ofPositioned(3, 3, snapshot.getItems());
      CraftingInput craftingInput = positionedInput.input();
      EventHooks.firePlayerCraftingEvent(player, stack, snapshot);
      this.onQuickCraft(stack, stack.getCount());
      if (this.craftSlots instanceof MagicWorkbenchBlockEntity workbench) {
         workbench.beginCraftingMutation();

         try {
            AspectList aspects = ThaumcraftCraftingManager.findArcaneRecipeAspects(this.craftSlots, player.level(), player);
            ItemStack wandStack = this.craftSlots.getItem(9);
            if (aspects.size() > 0 && wandStack.getItem() instanceof ItemWandCasting wand) {
               wand.consumeAllVisCrafting(wandStack, player, aspects, true);
            }

            CraftingRecipe recipe = player.level()
               .getRecipeManager()
               .getRecipeFor(RecipeType.CRAFTING, craftingInput, player.level())
               .<CraftingRecipe>map(RecipeHolder::value)
               .orElse(null);
            if (recipe == null) {
               recipe = ThaumcraftCraftingManager.findArcaneRecipe(snapshot, player.level(), player);
            }

            NonNullList<ItemStack> remainingItems = NonNullList.withSize(9, ItemStack.EMPTY);
            if (recipe != null) {
               NonNullList<ItemStack> compactRemainingItems = recipe.getRemainingItems(craftingInput);

               for (int y = 0; y < craftingInput.height(); y++) {
                  for (int x = 0; x < craftingInput.width(); x++) {
                     int compactIndex = x + y * craftingInput.width();
                     int gridIndex = positionedInput.left() + x + (positionedInput.top() + y) * 3;
                     if (compactIndex < compactRemainingItems.size() && gridIndex >= 0 && gridIndex < remainingItems.size()) {
                        remainingItems.set(gridIndex, (ItemStack)compactRemainingItems.get(compactIndex));
                     }
                  }
               }
            }

            for (int i = 0; i < 9; i++) {
               ItemStack current = this.craftSlots.getItem(i);
               ItemStack remaining = (ItemStack)remainingItems.get(i);
               if (!current.isEmpty()) {
                  this.craftSlots.removeItem(i, 1);
                  current = this.craftSlots.getItem(i);
               }

               if (!remaining.isEmpty()) {
                  if (current.isEmpty()) {
                     this.craftSlots.setItem(i, remaining);
                  } else if (ItemStackData.sameItemSameTags(current, remaining)) {
                     remaining.grow(current.getCount());
                     this.craftSlots.setItem(i, remaining);
                  } else if (!player.getInventory().add(remaining)) {
                     player.drop(remaining, false);
                  }
               }
            }
         } finally {
            workbench.endCraftingMutation();
         }

         this.menu.slotsChanged(this.craftSlots);
      }
   }
}
