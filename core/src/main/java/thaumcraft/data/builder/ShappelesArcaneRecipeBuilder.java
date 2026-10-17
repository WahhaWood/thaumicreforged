package thaumcraft.data.builder;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;

public class ShappelesArcaneRecipeBuilder extends AbstractThaumcraftRecipeBuilder<ShappelesArcaneRecipeBuilder> {
   private final String researchKey;
   private final Item result;
   private final int count;
   private final List<Ingredient> ingredients = Lists.newArrayList();
   private final AspectList aspectList = new AspectList();

   public ShappelesArcaneRecipeBuilder(String researchKey, ItemLike result, int count) {
      this.researchKey = researchKey;
      this.result = result.asItem();
      this.count = count;
   }

   @Override
   protected Recipe<?> createRecipe(ResourceLocation id) {
      return new ShapelessArcaneRecipe(id, this.researchKey, this.aspectList, new ItemStack(this.result, this.count), NonNullList.copyOf(this.ingredients));
   }

   @NotNull
   public ShappelesArcaneRecipeBuilder requires(@NotNull TagKey<Item> tagKey) {
      return this.requires(Ingredient.of(tagKey));
   }

   @NotNull
   public ShappelesArcaneRecipeBuilder requires(@NotNull ItemLike itemLike) {
      return this.requires(itemLike, 1);
   }

   @NotNull
   public ShappelesArcaneRecipeBuilder requires(@NotNull ItemLike itemLike, int count) {
      for (int i = 0; i < count; i++) {
         this.requires(Ingredient.of(new ItemLike[]{itemLike}));
      }

      return this;
   }

   @NotNull
   public ShappelesArcaneRecipeBuilder requires(@NotNull Ingredient ingredient) {
      return this.requires(ingredient, 1);
   }

   @NotNull
   public ShappelesArcaneRecipeBuilder requires(@NotNull Ingredient ingredient, int count) {
      for (int i = 0; i < count; i++) {
         this.ingredients.add(ingredient);
      }

      return this;
   }

   @NotNull
   public ShappelesArcaneRecipeBuilder aspect(@NotNull Aspect aspect) {
      return this.aspect(aspect, 1);
   }

   @NotNull
   public ShappelesArcaneRecipeBuilder aspect(@NotNull Aspect aspect, int amount) {
      this.aspectList.add(aspect, amount);
      return this;
   }

   public static ShappelesArcaneRecipeBuilder shapeless(String researchKey, ItemLike like) {
      return new ShappelesArcaneRecipeBuilder(researchKey, like, 1);
   }

   public static ShappelesArcaneRecipeBuilder shapeless(String researchKey, ItemLike like, int count) {
      return new ShappelesArcaneRecipeBuilder(researchKey, like, count);
   }
}
