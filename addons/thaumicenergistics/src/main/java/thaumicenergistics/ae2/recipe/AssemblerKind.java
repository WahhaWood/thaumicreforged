package thaumicenergistics.ae2.recipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;

/**
 * The recipe-backed assemblers: which Thaumcraft recipes each one offers to the ME network and whether
 * they start enabled. Recipes are off by default (research gates and costs: the player opts in).
 * No AE2 types here, the client screen uses it too.
 */
public enum AssemblerKind {
   ARCANE(false),
   CRUCIBLE(false),
   INFUSION(false);

   public final boolean enabledByDefault;

   AssemblerKind(boolean enabledByDefault) {
      this.enabledByDefault = enabledByDefault;
   }

   /** The kind that takes this recipe, or null when no assembler does. */
   @Nullable
   public static AssemblerKind of(Recipe<?> recipe) {
      if (recipe instanceof ShapedArcaneRecipe || recipe instanceof ShapelessArcaneRecipe) {
         return ARCANE;
      }

      if (recipe instanceof CrucibleRecipe) {
         return CRUCIBLE;
      }

      return recipe instanceof InfusionRecipe ? INFUSION : null;
   }

   /** Item-and-aspect form of a recipe of this kind, or null if it is unusable (empty tag, no output). */
   @Nullable
   public static AssemblerRecipe convert(ResourceLocation id, Recipe<?> recipe, HolderLookup.Provider registries) {
      if (recipe instanceof ShapedArcaneRecipe || recipe instanceof ShapelessArcaneRecipe) {
         IArcaneRecipe arcane = (IArcaneRecipe)recipe;
         // empty ingredients are the gaps of a shaped grid; the vis cost is paid by the assembler, not by the network
         return AssemblerRecipe.of(id, ARCANE, recipe.getIngredients(), true, arcane.getAspects(), recipe.getResultItem(registries));
      }

      if (recipe instanceof CrucibleRecipe crucible) {
         return AssemblerRecipe.of(id, CRUCIBLE, List.of(crucible.catalyst), false, crucible.aspects, crucible.getResultItem(registries));
      }

      if (recipe instanceof InfusionRecipe infusion) {
         List<Ingredient> ingredients = new ArrayList<>(1 + infusion.components.size());
         ingredients.add(infusion.central);
         ingredients.addAll(infusion.components);
         return AssemblerRecipe.of(id, INFUSION, ingredients, false, infusion.aspects, infusion.getResultItem(registries));
      }

      return null;
   }

   /** All usable recipes of this kind ordered by id, the same on server and client (both see the synced recipes). */
   public Map<ResourceLocation, AssemblerRecipe> collect(Level level) {
      List<RecipeHolder<? extends Recipe<?>>> holders = new ArrayList<>();
      switch (this) {
         case ARCANE -> {
            holders.addAll(level.getRecipeManager().getAllRecipesFor(ShapedArcaneRecipe.TYPE));
            holders.addAll(level.getRecipeManager().getAllRecipesFor(ShapelessArcaneRecipe.TYPE));
         }
         case CRUCIBLE -> holders.addAll(level.getRecipeManager().getAllRecipesFor(CrucibleRecipe.TYPE));
         case INFUSION -> holders.addAll(level.getRecipeManager().getAllRecipesFor(InfusionRecipe.TYPE));
      }

      List<AssemblerRecipe> list = new ArrayList<>(holders.size());
      for (RecipeHolder<? extends Recipe<?>> holder : holders) {
         AssemblerRecipe recipe = convert(holder.id(), holder.value(), level.registryAccess());
         if (recipe != null) {
            list.add(recipe);
         }
      }

      list.sort(Comparator.comparing(recipe -> recipe.id().toString()));
      Map<ResourceLocation, AssemblerRecipe> byId = new LinkedHashMap<>();
      for (AssemblerRecipe recipe : list) {
         byId.put(recipe.id(), recipe);
      }

      return byId;
   }

   static AspectList copyOf(@Nullable AspectList aspects) {
      return aspects == null ? new AspectList() : aspects.copy();
   }
}
