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
import thaumcraft.recipe.InfusionRunicAugmentRecipe;

public class RunicAugmentRecipeBuilder extends AbstractThaumcraftRecipeBuilder<RunicAugmentRecipeBuilder> {
   private final String researchKey;
   private final ItemStack display;
   private final List<Ingredient> components = Lists.newArrayList();

   public RunicAugmentRecipeBuilder(String researchKey, ItemStack display) {
      this.researchKey = researchKey;
      this.display = display.copy();
   }

   @Override
   protected Recipe<?> createRecipe(ResourceLocation id) {
      return new InfusionRunicAugmentRecipe(id, this.researchKey, this.display, NonNullList.copyOf(this.components));
   }

   public static RunicAugmentRecipeBuilder runicAugment(String researchKey, ItemStack display) {
      return new RunicAugmentRecipeBuilder(researchKey, display);
   }

   @NotNull
   public RunicAugmentRecipeBuilder requires(@NotNull TagKey<Item> tagKey) {
      return this.requires(Ingredient.of(tagKey));
   }

   @NotNull
   public RunicAugmentRecipeBuilder requires(@NotNull ItemLike itemLike) {
      return this.requires(itemLike, 1);
   }

   @NotNull
   public RunicAugmentRecipeBuilder requires(@NotNull ItemLike itemLike, int count) {
      for (int i = 0; i < count; i++) {
         this.requires(Ingredient.of(new ItemLike[]{itemLike}));
      }

      return this;
   }

   @NotNull
   public RunicAugmentRecipeBuilder requires(@NotNull Ingredient ingredient) {
      return this.requires(ingredient, 1);
   }

   @NotNull
   public RunicAugmentRecipeBuilder requires(@NotNull Ingredient ingredient, int count) {
      for (int i = 0; i < count; i++) {
         this.components.add(ingredient);
      }

      return this;
   }
}
