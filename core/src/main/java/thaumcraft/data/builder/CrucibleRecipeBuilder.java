package thaumcraft.data.builder;

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
import thaumcraft.api.crafting.CrucibleRecipe;

public class CrucibleRecipeBuilder extends AbstractThaumcraftRecipeBuilder<CrucibleRecipeBuilder> {
   private final String researchKey;
   private final Item result;
   private final int count;
   private final AspectList aspectList = new AspectList();
   private Ingredient catalyst;

   public CrucibleRecipeBuilder(String researchKey, ItemLike result, int count) {
      this.researchKey = researchKey;
      this.result = result.asItem();
      this.count = count;
   }

   @Override
   protected Recipe<?> createRecipe(ResourceLocation id) {
      return new CrucibleRecipe(id, this.researchKey, new ItemStack(this.result, this.count), this.catalyst, this.aspectList);
   }

   @NotNull
   public CrucibleRecipeBuilder catalyst(@NotNull ItemLike itemLike) {
      return this.catalyst(Ingredient.of(new ItemLike[]{itemLike}));
   }

   @NotNull
   public CrucibleRecipeBuilder catalyst(@NotNull TagKey<Item> tagKey) {
      return this.catalyst(Ingredient.of(tagKey));
   }

   @NotNull
   public CrucibleRecipeBuilder catalyst(@NotNull Ingredient catalyst) {
      this.catalyst = catalyst;
      return this;
   }

   @NotNull
   public CrucibleRecipeBuilder aspect(@NotNull Aspect aspect) {
      return this.aspect(aspect, 1);
   }

   @NotNull
   public CrucibleRecipeBuilder aspect(@NotNull Aspect aspect, int amount) {
      this.aspectList.add(aspect, amount);
      return this;
   }

   public static CrucibleRecipeBuilder crucible(String researchKey, ItemLike result) {
      return crucible(researchKey, result, 1);
   }

   public static CrucibleRecipeBuilder crucible(String researchKey, ItemLike result, int count) {
      return new CrucibleRecipeBuilder(researchKey, result, count);
   }
}
