package thaumcraft.compat.jei;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import thaumcraft.Thaumcraft;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.wands.ItemWandCap;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.ItemWandRod;
import thaumcraft.recipe.ArcaneWandRecipeSupport;

public final class ArcaneJeiRecipes {
   private static List<IArcaneRecipe> dynamicArcaneRecipes;

   private ArcaneJeiRecipes() {
   }

   public static List<IArcaneRecipe> getDynamicArcaneRecipes() {
      if (dynamicArcaneRecipes != null) {
         return dynamicArcaneRecipes;
      } else {
         List<IArcaneRecipe> recipes = new ArrayList<>();
         List<ItemWandCap> caps = new ArrayList<>(ItemWandCap.TYPES.values());
         caps.sort(Comparator.comparing(ItemWandCap::getTag));
         List<ItemWandRod> rods = new ArrayList<>(ItemWandRod.TYPES.values());
         rods.removeIf(ItemWandRod::isStaff);
         rods.sort(Comparator.comparing(ItemWandRod::getTag));

         for (ItemWandCap cap : caps) {
            for (ItemWandRod rod : rods) {
               if (cap != ModItems.IRON_WAND_CAP.get() || rod != ModItems.WOOD_WAND_ROD.get()) {
                  recipes.add(createWandRecipe(cap, rod));
               }

               recipes.add(createSceptreRecipe(cap, rod));
            }
         }

         dynamicArcaneRecipes = List.copyOf(recipes);
         return dynamicArcaneRecipes;
      }
   }

   public static IArcaneRecipe createWandRecipe(ItemWandCap cap, ItemWandRod rod) {
      NonNullList<Ingredient> ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
      ingredients.set(2, Ingredient.of(new ItemLike[]{cap}));
      ingredients.set(4, Ingredient.of(new ItemStack[]{getRodIngredient(rod)}));
      ingredients.set(6, Ingredient.of(new ItemLike[]{cap}));
      return new ShapedArcaneRecipe(
         Thaumcraft.location("jei/arcane/wand/" + cap.getTag() + "_" + rod.getTag()),
         "",
         ArcaneWandRecipeSupport.getPrimalCost(cap.getCraftCost() * rod.getCraftCost()),
         3,
         3,
         ingredients,
         ItemWandCasting.createWand(rod, cap)
      );
   }

   public static IArcaneRecipe createSceptreRecipe(ItemWandCap cap, ItemWandRod rod) {
      NonNullList<Ingredient> ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
      ingredients.set(1, Ingredient.of(new ItemLike[]{cap}));
      ingredients.set(2, Ingredient.of(new ItemLike[]{(ItemLike)ModItems.PRIMAL_CHARM.get()}));
      ingredients.set(4, Ingredient.of(new ItemStack[]{getRodIngredient(rod)}));
      ingredients.set(5, Ingredient.of(new ItemLike[]{cap}));
      ingredients.set(6, Ingredient.of(new ItemLike[]{cap}));
      int cost = (int)(cap.getCraftCost() * rod.getCraftCost() * 1.5F);
      return new ShapedArcaneRecipe(
         Thaumcraft.location("jei/arcane/sceptre/" + cap.getTag() + "_" + rod.getTag()),
         "",
         ArcaneWandRecipeSupport.getPrimalCost(cost),
         3,
         3,
         ingredients,
         ItemWandCasting.createSceptre(rod, cap)
      );
   }

   public static ItemStack getRodIngredient(ItemWandRod rod) {
      return rod == ModItems.WOOD_WAND_ROD.get() ? new ItemStack(Items.STICK) : new ItemStack(rod);
   }
}
