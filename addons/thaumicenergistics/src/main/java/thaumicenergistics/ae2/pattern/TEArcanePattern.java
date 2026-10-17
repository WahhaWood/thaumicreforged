package thaumicenergistics.ae2.pattern;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import java.util.Arrays;
import java.util.Objects;
import thaumicenergistics.init.ModItems;
import thaumicenergistics.util.ArcaneRecipeHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.util.ItemStackData;

public class TEArcanePattern extends AbstractAspectPattern {
   private final IArcaneRecipe recipe;

   public TEArcanePattern(CompoundTag tag, Level level) {
      this(PatternEncoding.definitionKey((Item)ModItems.ARCANE_PATTERN.get(), tag), level);
   }

   public TEArcanePattern(AEItemKey definition, Level level) {
      super(definition);
      CompoundTag tag = Objects.requireNonNull(ItemStackData.getTag(definition.getReadOnlyStack()));
      this.sparseInputs = PatternEncoding.getCraftingInputs(tag, level.registryAccess());
      ItemStack[] testInputs = new ItemStack[this.sparseInputs.length];

      for (int i = 0; i < this.sparseInputs.length; i++) {
         if (this.sparseInputs[i] != null && this.sparseInputs[i].what() instanceof AEItemKey itemKey) {
            testInputs[i] = itemKey.toStack();
         } else {
            testInputs[i] = ItemStack.EMPTY;
         }
      }

      IArcaneRecipe found = ArcaneRecipeHelper.INSTANCE.findMatchingRecipe(testInputs, level);
      this.recipe = found;
      if (this.recipe == null) {
         throw new IllegalStateException("The recipe " + Arrays.toString((Object[])this.sparseInputs) + " no longer matches the encoded input.");
      } else {
         ItemStack output = ArcaneRecipeHelper.INSTANCE.getRecipeOutput(this.recipe, testInputs, level);
         if (output.isEmpty()) {
            throw new IllegalStateException("The recipe " + Arrays.toString((Object[])this.sparseInputs) + " produced an empty item stack result.");
         } else {
            this.sparseOutputs = new GenericStack[]{Objects.requireNonNull(GenericStack.fromItemStack(output))};
            AspectList recipeAspects = ArcaneRecipeHelper.INSTANCE.getRecipeAspectCost(this.recipe, testInputs, level);
            this.aspects = recipeAspects == null ? new AspectList() : recipeAspects;
            int itemInputCount = 0;

            for (GenericStack sparseInput : this.sparseInputs) {
               if (sparseInput != null) {
                  itemInputCount++;
               }
            }

            this.inputs = new AbstractAspectPattern.Input[itemInputCount];
            int j = 0;

            for (int ix = 0; ix < this.sparseInputs.length; ix++) {
               if (this.sparseInputs[ix] != null) {
                  this.inputs[j++] = new AbstractAspectPattern.Input(this, ix, this.sparseInputs[ix]);
               }
            }
         }
      }
   }

   @Override
   protected Object getRecipeIngredient(int slot) {
      if (this.recipe != null && slot < this.sparseInputs.length) {
         NonNullList<Ingredient> ingredients = this.recipe.getIngredients();
         if (slot < ingredients.size()) {
            Ingredient ing = (Ingredient)ingredients.get(slot);
            return ing.isEmpty() ? null : ing;
         }
      }

      return null;
   }
}
