package thaumcraft.data.builder;

import java.util.Arrays;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ItemExistsCondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import org.jetbrains.annotations.NotNull;

abstract class AbstractThaumcraftRecipeBuilder<T extends AbstractThaumcraftRecipeBuilder<T>> implements RecipeBuilder {
   protected ICondition[] conditions = new ICondition[0];

   public T modId(String... modIds) {
      return this.conditions(Arrays.stream(modIds).map(ModLoadedCondition::new).toArray(ICondition[]::new));
   }

   public T itemExists(ItemLike... items) {
      return this.conditions(
         Arrays.stream(items)
            .map(ItemLike::asItem)
            .<ResourceLocation>map(BuiltInRegistries.ITEM::getKey)
            .filter(location -> location != null && !location.toString().equals("minecraft:air"))
            .map(ItemExistsCondition::new)
            .toArray(ICondition[]::new)
      );
   }

   public T conditions(ICondition... values) {
      this.conditions = values;
      return (T)this;
   }

   protected abstract Recipe<?> createRecipe(ResourceLocation var1);

   @NotNull
   public RecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
      return this;
   }

   @NotNull
   public RecipeBuilder group(String groupName) {
      return this;
   }

   @NotNull
   public Item getResult() {
      return Items.AIR;
   }

   public void save(@NotNull RecipeOutput output, @NotNull ResourceLocation id) {
      output.accept(id, this.createRecipe(id), null, this.conditions);
   }
}
