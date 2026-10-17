package thaumcraft.data.builder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import thaumcraft.recipe.ShapedNBTRecipe;

public class ShapedTagRecipeBuilder extends AbstractThaumcraftRecipeBuilder<ShapedTagRecipeBuilder> {
   private final ItemStack result;
   private final List<String> rows = Lists.newArrayList();
   private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();

   public ShapedTagRecipeBuilder(ItemStack result) {
      this.result = result;
   }

   @Override
   protected Recipe<?> createRecipe(ResourceLocation id) {
      return new ShapedNBTRecipe("", CraftingBookCategory.MISC, ShapedRecipePattern.of(this.key, this.rows), this.result, true);
   }

   public ShapedTagRecipeBuilder define(Character c, TagKey<Item> tagKey) {
      return this.define(c, Ingredient.of(tagKey));
   }

   public ShapedTagRecipeBuilder define(Character c, ItemLike itemLike) {
      return this.define(c, Ingredient.of(new ItemLike[]{itemLike}));
   }

   public ShapedTagRecipeBuilder define(Character c, Ingredient ingredient) {
      if (this.key.containsKey(c)) {
         throw new IllegalArgumentException("Symbol '" + c + "' is already defined!");
      } else if (c == ' ') {
         throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
      } else {
         this.key.put(c, ingredient);
         return this;
      }
   }

   public ShapedTagRecipeBuilder pattern(String p_126131_) {
      if (!this.rows.isEmpty() && p_126131_.length() != this.rows.get(0).length()) {
         throw new IllegalArgumentException("Pattern must be the same width on every line!");
      } else {
         this.rows.add(p_126131_);
         return this;
      }
   }

   public static ShapedTagRecipeBuilder shaped(ItemStack stack) {
      return new ShapedTagRecipeBuilder(stack);
   }
}
