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
import thaumcraft.api.crafting.InfusionRecipe;

public class InfusionRecipeBuilder extends AbstractThaumcraftRecipeBuilder<InfusionRecipeBuilder> {
   private final String researchKey;
   private final ItemStack result;
   private final List<Ingredient> components = Lists.newArrayList();
   private final AspectList aspectList = new AspectList();
   private Ingredient central;
   private int instability;

   public InfusionRecipeBuilder(String researchKey, ItemLike result, int count) {
      this(researchKey, new ItemStack(result, count));
   }

   public InfusionRecipeBuilder(String researchKey, ItemStack result) {
      this.researchKey = researchKey;
      this.result = result.copy();
   }

   @Override
   protected Recipe<?> createRecipe(ResourceLocation id) {
      return new InfusionRecipe(id, this.researchKey, this.result, this.instability, this.aspectList, this.central, NonNullList.copyOf(this.components));
   }

   @NotNull
   public InfusionRecipeBuilder central(@NotNull TagKey<Item> tagKey) {
      return this.central(Ingredient.of(tagKey));
   }

   @NotNull
   public InfusionRecipeBuilder central(@NotNull ItemLike itemLike) {
      return this.central(Ingredient.of(new ItemLike[]{itemLike}));
   }

   @NotNull
   public InfusionRecipeBuilder central(@NotNull Ingredient ingredient) {
      this.central = ingredient;
      return this;
   }

   @NotNull
   public InfusionRecipeBuilder requires(@NotNull TagKey<Item> tagKey) {
      return this.requires(Ingredient.of(tagKey));
   }

   @NotNull
   public InfusionRecipeBuilder requires(@NotNull ItemLike itemLike) {
      return this.requires(itemLike, 1);
   }

   @NotNull
   public InfusionRecipeBuilder requires(@NotNull ItemLike itemLike, int count) {
      for (int i = 0; i < count; i++) {
         this.requires(Ingredient.of(new ItemLike[]{itemLike}));
      }

      return this;
   }

   @NotNull
   public InfusionRecipeBuilder requires(@NotNull Ingredient ingredient) {
      return this.requires(ingredient, 1);
   }

   @NotNull
   public InfusionRecipeBuilder requires(@NotNull Ingredient ingredient, int count) {
      for (int i = 0; i < count; i++) {
         this.components.add(ingredient);
      }

      return this;
   }

   @NotNull
   public InfusionRecipeBuilder aspect(@NotNull Aspect aspect) {
      return this.aspect(aspect, 1);
   }

   @NotNull
   public InfusionRecipeBuilder aspect(@NotNull Aspect aspect, int amount) {
      this.aspectList.add(aspect, amount);
      return this;
   }

   @NotNull
   public InfusionRecipeBuilder instability(int value) {
      this.instability = value;
      return this;
   }

   public static InfusionRecipeBuilder infusion(String researchKey, ItemLike like) {
      return new InfusionRecipeBuilder(researchKey, like, 1);
   }

   public static InfusionRecipeBuilder infusion(String researchKey, ItemLike like, int count) {
      return new InfusionRecipeBuilder(researchKey, like, count);
   }

   public static InfusionRecipeBuilder infusion(String researchKey, ItemStack stack) {
      return new InfusionRecipeBuilder(researchKey, stack);
   }
}
