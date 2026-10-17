package thaumcraft.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;
import thaumcraft.init.registry.ModRecipes;

public class ShapelessNBTRecipe extends ShapelessRecipe {
   public ShapelessNBTRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
      super(group, category, result, ingredients);
   }

   @NotNull
   public RecipeSerializer<?> getSerializer() {
      return (RecipeSerializer<?>)ModRecipes.SHAPELESS_NBT_SERIALIZER.get();
   }

   public static class Serializer implements RecipeSerializer<ShapelessNBTRecipe> {
      private static final MapCodec<ShapelessNBTRecipe> CODEC = RecipeSerializer.SHAPELESS_RECIPE
         .codec()
         .xmap(ShapelessNBTRecipe.Serializer::wrap, recipe -> recipe);
      private static final StreamCodec<RegistryFriendlyByteBuf, ShapelessNBTRecipe> STREAM_CODEC = RecipeSerializer.SHAPELESS_RECIPE
         .streamCodec()
         .map(ShapelessNBTRecipe.Serializer::wrap, recipe -> recipe);

      @NotNull
      public MapCodec<ShapelessNBTRecipe> codec() {
         return CODEC;
      }

      @NotNull
      public StreamCodec<RegistryFriendlyByteBuf, ShapelessNBTRecipe> streamCodec() {
         return STREAM_CODEC;
      }

      private static ShapelessNBTRecipe wrap(ShapelessRecipe recipe) {
         return new ShapelessNBTRecipe(recipe.getGroup(), recipe.category(), recipe.getResultItem((Provider)null), recipe.getIngredients());
      }
   }
}
