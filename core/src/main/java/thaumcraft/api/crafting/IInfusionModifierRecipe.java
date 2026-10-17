package thaumcraft.api.crafting;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import thaumcraft.api.aspects.AspectList;

public interface IInfusionModifierRecipe extends Recipe<RecipeInput> {
   boolean matches(List<ItemStack> var1, ItemStack var2);

   ItemStack modifierItem(ItemStack var1);

   AspectList getAspects();

   default AspectList getAspects(ItemStack central) {
      return this.getAspects();
   }

   int getInstability();

   default int getInstability(ItemStack central) {
      return this.getInstability();
   }

   String getResearchKey();

   List<Ingredient> getIngredientList();

   default List<Ingredient> getIngredientList(ItemStack central) {
      return this.getIngredientList();
   }

   default int getExperienceCost(ItemStack central) {
      return 0;
   }
}
