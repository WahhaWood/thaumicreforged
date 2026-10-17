package thaumicenergistics.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionRecipe;

public final class InfusionRecipeHelper {
   public static final InfusionRecipeHelper INSTANCE = new InfusionRecipeHelper();

   private InfusionRecipeHelper() {
   }

   @Nullable
   public InfusionRecipe findMatchingMatrixResult(Container sourceInventory, int firstSlotIndex, int gridSize, @NotNull Player player) {
      ArrayList<ItemStack> inputs = new ArrayList<>();
      ItemStack center = ItemStack.EMPTY;

      for (int i = firstSlotIndex; i < firstSlotIndex + gridSize; i++) {
         ItemStack stack = sourceInventory.getItem(i);
         if (!stack.isEmpty()) {
            if (i == firstSlotIndex) {
               center = stack.copy();
            } else {
               inputs.add(stack.copy());
            }
         }
      }

      return !inputs.isEmpty() && !center.isEmpty() ? this.findMatchingRecipe(inputs, center, player.level()) : null;
   }

   @Nullable
   public InfusionRecipe findMatchingRecipe(List<ItemStack> inputs, ItemStack center, @NotNull Level level) {
      for (RecipeHolder<InfusionRecipe> holder : level.getRecipeManager().getAllRecipesFor(InfusionRecipe.TYPE)) {
         InfusionRecipe recipe = (InfusionRecipe)holder.value();
         if (recipe.matches(inputs, center)) {
            return recipe;
         }
      }

      return null;
   }

   @Nullable
   public AspectList getRecipeAspectCost(InfusionRecipe recipe) {
      return recipe == null ? null : recipe.aspects;
   }

   @Nullable
   public ItemStack getRecipeOutput(InfusionRecipe recipe) {
      return recipe == null ? null : recipe.getResultItem();
   }
}
