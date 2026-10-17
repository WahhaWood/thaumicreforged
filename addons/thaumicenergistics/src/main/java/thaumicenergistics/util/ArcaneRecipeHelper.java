package thaumicenergistics.util;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.recipe.ThaumcraftCraftingManager;

public final class ArcaneRecipeHelper {
   public static final ArcaneRecipeHelper INSTANCE = new ArcaneRecipeHelper();

   private ArcaneRecipeHelper() {
   }

   @Nullable
   public IArcaneRecipe findMatchingMatrixResult(Container sourceInventory, int firstSlotIndex, int gridSize, @NotNull Player player) {
      return this.findMatchingRecipe(this.extractInputs(sourceInventory, firstSlotIndex, gridSize), player.level(), player);
   }

   @NotNull
   public ItemStack findMatchingMatrixOutput(Container sourceInventory, int firstSlotIndex, int gridSize, @NotNull Player player) {
      return this.findMatchingRecipeOutput(this.extractInputs(sourceInventory, firstSlotIndex, gridSize), player.level(), player);
   }

   @Nullable
   public IArcaneRecipe findMatchingRecipe(ItemStack[] inputs, @NotNull Level level, @Nullable Player player) {
      return ThaumcraftCraftingManager.findArcaneRecipe(this.createCraftingContainer(inputs), level, player);
   }

   @Nullable
   public IArcaneRecipe findMatchingRecipe(ItemStack[] inputs, @NotNull Level level) {
      return this.findMatchingRecipe(inputs, level, null);
   }

   @Nullable
   public AspectList getRecipeAspectCost(IArcaneRecipe recipe) {
      return recipe == null ? null : recipe.getAspects();
   }

   @Nullable
   public AspectList getRecipeAspectCost(IArcaneRecipe recipe, ItemStack[] inputs, @NotNull Level level) {
      if (recipe == null) {
         return null;
      } else {
         AspectList aspects = recipe.getAspects(this.createCraftingContainer(inputs));
         return aspects == null ? null : aspects.copy();
      }
   }

   @Nullable
   public ItemStack getRecipeOutput(IArcaneRecipe recipe) {
      return recipe == null ? null : recipe.getResultItem();
   }

   @NotNull
   public ItemStack getRecipeOutput(IArcaneRecipe recipe, ItemStack[] inputs, @NotNull Level level) {
      return recipe == null ? ItemStack.EMPTY : recipe.getResultItem(this.createCraftingContainer(inputs), level.registryAccess()).copy();
   }

   @NotNull
   public ItemStack findMatchingRecipeOutput(ItemStack[] inputs, @NotNull Level level, @Nullable Player player) {
      return ThaumcraftCraftingManager.findArcaneRecipeResult(this.createCraftingContainer(inputs), level, player).copy();
   }

   private ItemStack[] extractInputs(Container sourceInventory, int firstSlotIndex, int gridSize) {
      ItemStack[] inputs = new ItemStack[gridSize];

      for (int i = 0; i < gridSize; i++) {
         ItemStack stack = sourceInventory.getItem(firstSlotIndex + i);
         inputs[i] = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
      }

      return inputs;
   }

   private CraftingContainer createCraftingContainer(ItemStack[] inputs) {
      CraftingContainer craftingContainer = new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {
         @NotNull
         public ItemStack quickMoveStack(@NotNull Player player, int slot) {
            return ItemStack.EMPTY;
         }

         public boolean stillValid(@NotNull Player player) {
            return false;
         }
      }, 3, 3);

      for (int i = 0; i < Math.min(inputs.length, 9); i++) {
         if (inputs[i] != null && !inputs[i].isEmpty()) {
            craftingContainer.setItem(i, inputs[i].copy());
         }
      }

      return craftingContainer;
   }
}
