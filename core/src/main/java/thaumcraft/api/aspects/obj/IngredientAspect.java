package thaumcraft.api.aspects.obj;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import thaumcraft.api.aspects.AspectList;

public class IngredientAspect extends ObjectAspect {
   public final Ingredient ingredient;

   public IngredientAspect(Ingredient ingredient, AspectList aspects) {
      super(aspects);
      this.ingredient = ingredient;
   }

   @Override
   public boolean matches(ItemStack stack) {
      return this.ingredient.test(stack);
   }
}
