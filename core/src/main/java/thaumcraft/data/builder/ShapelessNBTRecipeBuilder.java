package thaumcraft.data.builder;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import thaumcraft.recipe.ShapelessNBTRecipe;

public class ShapelessNBTRecipeBuilder extends AbstractThaumcraftRecipeBuilder<ShapelessNBTRecipeBuilder> {
   private final ItemStack result;
   private final List<Ingredient> ingredients = Lists.newArrayList();

   public ShapelessNBTRecipeBuilder(ItemStack result) {
      this.result = result.copy();
   }

   @Override
   protected Recipe<?> createRecipe(ResourceLocation id) {
      return new ShapelessNBTRecipe("", CraftingBookCategory.MISC, this.result, NonNullList.copyOf(this.ingredients));
   }

   public static ShapelessNBTRecipeBuilder shapeless(ItemStack result) {
      return new ShapelessNBTRecipeBuilder(result);
   }

   @NotNull
   public ShapelessNBTRecipeBuilder requires(@NotNull TagKey<Item> tagKey) {
      return this.requires(Ingredient.of(tagKey));
   }

   @NotNull
   public ShapelessNBTRecipeBuilder requires(@NotNull ItemLike itemLike) {
      return this.requires(itemLike, 1);
   }

   @NotNull
   public ShapelessNBTRecipeBuilder requires(@NotNull ItemLike itemLike, int count) {
      for (int i = 0; i < count; i++) {
         this.requires(Ingredient.of(new ItemLike[]{itemLike}));
      }

      return this;
   }

   @NotNull
   public ShapelessNBTRecipeBuilder requires(@NotNull Ingredient ingredient) {
      return this.requires(ingredient, 1);
   }

   @NotNull
   public ShapelessNBTRecipeBuilder requires(@NotNull Ingredient ingredient, int count) {
      for (int i = 0; i < count; i++) {
         this.ingredients.add(ingredient);
      }

      return this;
   }
}
