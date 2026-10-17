package thaumcraft.data.builder;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.FortressArmorUpgradeRecipe;

public class FortressArmorUpgradeRecipeBuilder {
   private final String researchKey;
   private final FortressArmorUpgradeRecipe.UpgradeKind kind;
   private final Ingredient central;
   private final List<Ingredient> components = Lists.newArrayList();
   private final AspectList aspectList = new AspectList();
   private int instability;

   public FortressArmorUpgradeRecipeBuilder(String researchKey, FortressArmorUpgradeRecipe.UpgradeKind kind, Ingredient central) {
      this.researchKey = researchKey;
      this.kind = kind;
      this.central = central;
   }

   public static FortressArmorUpgradeRecipeBuilder fortress(String researchKey, FortressArmorUpgradeRecipe.UpgradeKind kind, ItemLike central) {
      return new FortressArmorUpgradeRecipeBuilder(researchKey, kind, Ingredient.of(new ItemLike[]{central}));
   }

   public void save(@NotNull RecipeOutput output, @NotNull ResourceLocation id) {
      output.accept(
         id,
         new FortressArmorUpgradeRecipe(id, this.researchKey, this.kind, this.instability, this.aspectList, this.central, NonNullList.copyOf(this.components)),
         null
      );
   }

   @NotNull
   public FortressArmorUpgradeRecipeBuilder requires(@NotNull TagKey<Item> tagKey) {
      return this.requires(Ingredient.of(tagKey));
   }

   @NotNull
   public FortressArmorUpgradeRecipeBuilder requires(@NotNull ItemLike itemLike) {
      return this.requires(itemLike, 1);
   }

   @NotNull
   public FortressArmorUpgradeRecipeBuilder requires(@NotNull ItemLike itemLike, int count) {
      for (int i = 0; i < count; i++) {
         this.requires(Ingredient.of(new ItemLike[]{itemLike}));
      }

      return this;
   }

   @NotNull
   public FortressArmorUpgradeRecipeBuilder requires(@NotNull Ingredient ingredient) {
      this.components.add(ingredient);
      return this;
   }

   @NotNull
   public FortressArmorUpgradeRecipeBuilder aspect(@NotNull Aspect aspect, int amount) {
      this.aspectList.add(aspect, amount);
      return this;
   }

   @NotNull
   public FortressArmorUpgradeRecipeBuilder instability(int value) {
      this.instability = value;
      return this;
   }
}
