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
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.ShapedArcaneRecipe;

public class ShapedArcaneRecipeBuilder extends AbstractThaumcraftRecipeBuilder<ShapedArcaneRecipeBuilder> {
   private final String researchKey;
   private final ItemStack result;
   private final List<String> rows = Lists.newArrayList();
   private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
   private final AspectList aspectList = new AspectList();

   public ShapedArcaneRecipeBuilder(String researchKey, ItemLike result, int count) {
      this(researchKey, new ItemStack(result, count));
   }

   public ShapedArcaneRecipeBuilder(String researchKey, ItemStack result) {
      this.researchKey = researchKey;
      this.result = result;
   }

   @Override
   protected Recipe<?> createRecipe(ResourceLocation id) {
      return new ShapedArcaneRecipe(
         "", CraftingBookCategory.MISC, ShapedRecipePattern.of(this.key, this.rows), this.result, true, this.researchKey, this.aspectList
      );
   }

   public ShapedArcaneRecipeBuilder define(Character c, TagKey<Item> tagKey) {
      return this.define(c, Ingredient.of(tagKey));
   }

   public ShapedArcaneRecipeBuilder define(Character c, ItemLike itemLike) {
      return this.define(c, Ingredient.of(new ItemLike[]{itemLike}));
   }

   public ShapedArcaneRecipeBuilder define(Character c, Ingredient ingredient) {
      if (this.key.containsKey(c)) {
         throw new IllegalArgumentException("Symbol '" + c + "' is already defined!");
      } else if (c == ' ') {
         throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
      } else {
         this.key.put(c, ingredient);
         return this;
      }
   }

   public ShapedArcaneRecipeBuilder pattern(String p_126131_) {
      if (!this.rows.isEmpty() && p_126131_.length() != this.rows.get(0).length()) {
         throw new IllegalArgumentException("Pattern must be the same width on every line!");
      } else {
         this.rows.add(p_126131_);
         return this;
      }
   }

   @NotNull
   public ShapedArcaneRecipeBuilder aspect(@NotNull Aspect aspect) {
      return this.aspect(aspect, 1);
   }

   @NotNull
   public ShapedArcaneRecipeBuilder aspect(@NotNull Aspect aspect, int amount) {
      this.aspectList.add(aspect, amount);
      return this;
   }

   public static ShapedArcaneRecipeBuilder shaped(String researchKey, ItemLike result) {
      return shaped(researchKey, result, 1);
   }

   public static ShapedArcaneRecipeBuilder shaped(String researchKey, ItemLike result, int count) {
      return new ShapedArcaneRecipeBuilder(researchKey, result, count);
   }

   public static ShapedArcaneRecipeBuilder shaped(String researchKey, ItemStack result) {
      return new ShapedArcaneRecipeBuilder(researchKey, result);
   }
}
