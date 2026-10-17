package thaumcraft.api;

import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.obj.ObjectAspect;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.init.registry.ModItems;
import thaumcraft.util.ItemStackData;

final class AspectGenerationManager {
   private static final ThreadLocal<Integer> GENERATION_DEPTH = ThreadLocal.withInitial(() -> 0);

   private AspectGenerationManager() {
   }

   @Nullable
   static AspectList generateTags(ItemStack stack) {
      return generateTags(stack, null);
   }

   @Nullable
   static AspectList generateTags(ItemStack stack, @Nullable ObjectAspect excludedAspect) {
      if (stack != null && !stack.isEmpty()) {
         RecipeManager recipeManager = ThaumcraftApi.getSafeRecipeManager();
         if (recipeManager == null) {
            return null;
         } else {
            int generationDepth = Objects.requireNonNullElse(GENERATION_DEPTH.get(), 0);
            if (generationDepth > 100) {
               return null;
            } else {
               GENERATION_DEPTH.set(generationDepth + 1);

               AspectList var4;
               try {
                  var4 = generateBaseTags(stack, new ArrayList<>(), recipeManager, excludedAspect);
               } finally {
                  GENERATION_DEPTH.set(generationDepth);
               }

               return var4;
            }
         }
      } else {
         return null;
      }
   }

   @Nullable
   private static AspectList generateBaseTags(
      ItemStack stack, ArrayList<ItemStack> generationHistory, RecipeManager recipeManager, @Nullable ObjectAspect excludedAspect
   ) {
      if (ThaumcraftApi.exists(stack, excludedAspect)) {
         return getCappedRegisteredBaseTags(stack, excludedAspect);
      } else if (containsHistoryStack(generationHistory, stack)) {
         return null;
      } else {
         generationHistory.add(normalizeGenerationStack(stack));
         if (generationHistory.size() >= 100) {
            return null;
         } else {
            AspectList generatedBaseTags = generateRecipeDerivedTags(stack, generationHistory, recipeManager, excludedAspect);
            AspectList cappedGeneratedTags = capAspects(generatedBaseTags, 64);
            cacheExactGeneratedTags(stack, cappedGeneratedTags, excludedAspect);
            return cappedGeneratedTags;
         }
      }
   }

   @Nullable
   private static AspectList getCappedRegisteredBaseTags(ItemStack stack, @Nullable ObjectAspect excludedAspect) {
      AspectList registeredTags = ThaumcraftApi.getObjectTags(stack, excludedAspect);
      AspectList bonusEnrichedTags = ThaumcraftApiHelper.getBonusObjectTags(stack, registeredTags);
      return capAspects(bonusEnrichedTags, 64);
   }

   private static void cacheExactGeneratedTags(ItemStack stack, @Nullable AspectList generatedBaseTags, @Nullable ObjectAspect excludedAspect) {
      if (generatedBaseTags != null && excludedAspect == null) {
         ThaumcraftApi.registerObjectTag(stack, generatedBaseTags);
      }
   }

   @Nullable
   private static AspectList generateTagsFromCrucibleRecipes(
      ItemStack stack, ArrayList<ItemStack> generationHistory, RecipeManager recipeManager, @Nullable ObjectAspect excludedAspect
   ) {
      for (RecipeHolder<CrucibleRecipe> holder : recipeManager.getAllRecipesFor(CrucibleRecipe.TYPE)) {
         CrucibleRecipe recipe = (CrucibleRecipe)holder.value();
         if (matchesRecipeResult(recipe.getResultItem(), stack)) {
            return getAspectsFromIngredients(recipe.getIngredients(), recipe.getResultItem(), recipe.aspects, generationHistory, recipeManager, excludedAspect);
         }
      }

      return null;
   }

   @Nullable
   private static AspectList generateTagsFromArcaneRecipes(
      ItemStack stack, ArrayList<ItemStack> generationHistory, RecipeManager recipeManager, @Nullable ObjectAspect excludedAspect
   ) {
      AspectList bestMatch = null;
      int bestVis = Integer.MAX_VALUE;

      for (RecipeHolder<ShapedArcaneRecipe> holder : recipeManager.getAllRecipesFor(ShapedArcaneRecipe.TYPE)) {
         ShapedArcaneRecipe recipe = (ShapedArcaneRecipe)holder.value();
         if (matchesRecipeResult(recipe.getResultItem(), stack)) {
            AspectList candidate = getAspectsFromIngredients(
               recipe.getIngredients(), recipe.getResultItem(), recipe.getAspects(), generationHistory, recipeManager, excludedAspect
            );
            if (candidate != null && candidate.visSize() > 0 && candidate.visSize() < bestVis) {
               bestMatch = candidate;
               bestVis = candidate.visSize();
            }
         }
      }

      for (RecipeHolder<ShapelessArcaneRecipe> holderx : recipeManager.getAllRecipesFor(ShapelessArcaneRecipe.TYPE)) {
         ShapelessArcaneRecipe recipe = (ShapelessArcaneRecipe)holderx.value();
         if (matchesRecipeResult(recipe.getResultItem(), stack)) {
            AspectList candidate = getAspectsFromIngredients(
               recipe.getIngredients(), recipe.getResultItem(), recipe.getAspects(), generationHistory, recipeManager, excludedAspect
            );
            if (candidate != null && candidate.visSize() > 0 && candidate.visSize() < bestVis) {
               bestMatch = candidate;
               bestVis = candidate.visSize();
            }
         }
      }

      return bestMatch;
   }

   @Nullable
   private static AspectList generateTagsFromInfusionRecipes(
      ItemStack stack, ArrayList<ItemStack> generationHistory, RecipeManager recipeManager, @Nullable ObjectAspect excludedAspect
   ) {
      for (RecipeHolder<InfusionRecipe> holder : recipeManager.getAllRecipesFor(InfusionRecipe.TYPE)) {
         InfusionRecipe recipe = (InfusionRecipe)holder.value();
         if (matchesRecipeResult(recipe.getResultItem(), stack)) {
            NonNullList<Ingredient> ingredients = NonNullList.create();
            ingredients.add(recipe.getCentral());
            ingredients.addAll(recipe.getIngredients());
            return getAspectsFromIngredients(ingredients, recipe.getResultItem(), recipe.aspects, generationHistory, recipeManager, excludedAspect);
         }
      }

      return null;
   }

   @Nullable
   private static AspectList generateTagsFromCraftingRecipes(
      ItemStack stack, ArrayList<ItemStack> generationHistory, RecipeManager recipeManager, @Nullable ObjectAspect excludedAspect
   ) {
      AspectList bestMatch = null;
      int bestVis = Integer.MAX_VALUE;

      for (RecipeHolder<CraftingRecipe> holder : recipeManager.getAllRecipesFor(RecipeType.CRAFTING)) {
         CraftingRecipe recipe = (CraftingRecipe)holder.value();
         ItemStack result = recipe.getResultItem(RegistryAccess.EMPTY);
         if (matchesRecipeResult(result, stack)) {
            AspectList candidate = getAspectsFromIngredients(recipe.getIngredients(), result, null, generationHistory, recipeManager, excludedAspect);
            if (candidate != null && candidate.visSize() > 0 && candidate.visSize() < bestVis) {
               bestMatch = candidate;
               bestVis = candidate.visSize();
            }
         }
      }

      return bestMatch;
   }

   @Nullable
   private static AspectList generateRecipeDerivedTags(
      ItemStack stack, ArrayList<ItemStack> generationHistory, RecipeManager recipeManager, @Nullable ObjectAspect excludedAspect
   ) {
      AspectList generatedTags = generateTagsFromCrucibleRecipes(stack, generationHistory, recipeManager, excludedAspect);
      if (generatedTags != null) {
         return generatedTags;
      } else {
         generatedTags = generateTagsFromArcaneRecipes(stack, generationHistory, recipeManager, excludedAspect);
         if (generatedTags != null) {
            return generatedTags;
         } else {
            generatedTags = generateTagsFromInfusionRecipes(stack, generationHistory, recipeManager, excludedAspect);
            return generatedTags != null ? generatedTags : generateTagsFromCraftingRecipes(stack, generationHistory, recipeManager, excludedAspect);
         }
      }
   }

   @Nullable
   private static AspectList getAspectsFromIngredients(
      NonNullList<Ingredient> ingredients,
      ItemStack result,
      @Nullable AspectList recipeAspects,
      ArrayList<ItemStack> generationHistory,
      RecipeManager recipeManager,
      @Nullable ObjectAspect excludedAspect
   ) {
      AspectList resultAspects = new AspectList();
      AspectList ingredientAspects = new AspectList();

      for (Ingredient ingredient : ingredients) {
         ItemStack ingredientStack = getIngredientStack(ingredient, generationHistory, recipeManager, excludedAspect);
         if (ingredientStack != null && !ingredientStack.isEmpty()) {
            Item item = ingredientStack.getItem();
            ItemStack containerItem = item.getCraftingRemainingItem(ingredientStack);
            if (containerItem.isEmpty()) {
               AspectList generatedIngredientTags = generateBaseTags(ingredientStack, generationHistory, recipeManager, excludedAspect);
               if (generatedIngredientTags != null) {
                  for (Aspect aspect : generatedIngredientTags.getAspects()) {
                     if (aspect != null) {
                        ingredientAspects.add(aspect, generatedIngredientTags.getAmount(aspect));
                     }
                  }
               }
            } else if (containerItem.getItem() != item) {
               AspectList generatedContainerTags = generateBaseTags(containerItem, generationHistory, recipeManager, excludedAspect);
               if (generatedContainerTags != null) {
                  for (Aspect aspectx : generatedContainerTags.getAspects()) {
                     resultAspects.reduce(aspectx, generatedContainerTags.getAmount(aspectx));
                  }
               }
            }
         }
      }

      for (Aspect aspectx : ingredientAspects.getAspects()) {
         if (aspectx != null) {
            resultAspects.add(aspectx, (int)(ingredientAspects.getAmount(aspectx) * 0.75F / result.getCount()));
         }
      }

      if (recipeAspects != null) {
         for (Aspect aspectxx : recipeAspects.getAspects()) {
            resultAspects.add(aspectxx, (int)(Math.sqrt(recipeAspects.getAmount(aspectxx)) / result.getCount()));
         }
      }

      for (Aspect aspectxx : resultAspects.getAspects()) {
         if (resultAspects.getAmount(aspectxx) <= 0) {
            resultAspects.remove(aspectxx);
         }
      }

      return resultAspects;
   }

   private static ItemStack getIngredientStack(
      Ingredient ingredient, ArrayList<ItemStack> generationHistory, RecipeManager recipeManager, @Nullable ObjectAspect excludedAspect
   ) {
      if (ingredient == null) {
         return ItemStack.EMPTY;
      } else {
         ItemStack[] items = ingredient.getItems();
         if (items.length == 0) {
            return ItemStack.EMPTY;
         } else if (items.length == 1) {
            return normalizeGenerationStack(items[0]);
         } else {
            for (ItemStack candidate : items) {
               if (!candidate.isEmpty()) {
                  AspectList candidateTags = generateBaseTags(candidate, generationHistory, recipeManager, excludedAspect);
                  if (candidateTags != null && candidateTags.size() > 0) {
                     return normalizeGenerationStack(candidate);
                  }
               }
            }

            return ItemStack.EMPTY;
         }
      }
   }

   private static boolean matchesRecipeResult(ItemStack recipeResult, ItemStack stack) {
      if (!ItemStack.isSameItem(recipeResult, stack)) {
         return false;
      } else {
         return recipeResult.is((Item)ModItems.MOB_ASPECT.get()) ? ItemStackData.sameItemSameTags(recipeResult, stack) : true;
      }
   }

   @Nullable
   private static AspectList capAspects(@Nullable AspectList sourcetags, int amount) {
      if (sourcetags == null) {
         return null;
      } else {
         AspectList out = new AspectList();

         for (Aspect aspect : sourcetags.getAspects()) {
            out.merge(aspect, Math.min(amount, sourcetags.getAmount(aspect)));
         }

         return out;
      }
   }

   private static boolean containsHistoryStack(ArrayList<ItemStack> generationHistory, ItemStack stack) {
      for (ItemStack previous : generationHistory) {
         if (ItemStackData.sameItemSameTags(previous, stack)) {
            return true;
         }
      }

      return false;
   }

   private static ItemStack normalizeGenerationStack(ItemStack stack) {
      if (stack.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack copy = stack.copy();
         copy.setCount(1);
         return copy;
      }
   }
}
