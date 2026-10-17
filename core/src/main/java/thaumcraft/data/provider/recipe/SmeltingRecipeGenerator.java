package thaumcraft.data.provider.recipe;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import thaumcraft.init.registry.ModItems;
import thaumcraft.recipe.TagSmeltingRecipe;

public class SmeltingRecipeGenerator extends RecipeProvider {
   public SmeltingRecipeGenerator(PackOutput output, CompletableFuture<Provider> registries) {
      super(output, registries);
   }

   public void generate(@NotNull RecipeOutput consumer) {
      this.buildRecipes(consumer);
   }

   protected void buildRecipes(@NotNull RecipeOutput consumer) {
      oreSmelting(consumer, List.of((ItemLike)ModItems.AMBER_ORE.get()), RecipeCategory.MISC, (ItemLike)ModItems.AMBER.get(), 0.7F, 200, "amber");
      oreBlasting(consumer, List.of((ItemLike)ModItems.AMBER_ORE.get()), RecipeCategory.MISC, (ItemLike)ModItems.AMBER.get(), 0.7F, 100, "amber");
      oreSmelting(consumer, List.of((ItemLike)ModItems.CINNABAR_ORE.get()), RecipeCategory.MISC, (ItemLike)ModItems.QUICKSILVER.get(), 0.7F, 200, "quicksilver");
      oreBlasting(consumer, List.of((ItemLike)ModItems.CINNABAR_ORE.get()), RecipeCategory.MISC, (ItemLike)ModItems.QUICKSILVER.get(), 0.7F, 100, "quicksilver");
      SimpleCookingRecipeBuilder.smelting(
            Ingredient.of(new ItemLike[]{(ItemLike)ModItems.BALANCED_SHARD.get()}), RecipeCategory.MISC, (ItemLike)ModItems.SALIS_MUNDUS.get(), 0.0F, 200
         )
         .unlockedBy(getHasName((ItemLike)ModItems.BALANCED_SHARD.get()), has((ItemLike)ModItems.BALANCED_SHARD.get()))
         .save(consumer, "thaumcraft:salis_mundus_from_smelting");
      smeltingResultFromBase(
         consumer,
         "iron_cluster_to_iron_ingot",
         new ItemStack(Items.IRON_INGOT, 2),
         Ingredient.of(new ItemLike[]{(ItemLike)ModItems.IRON_CLUSTER.get()}),
         (ItemLike)ModItems.IRON_CLUSTER.get(),
         1.0F
      );
      smeltingResultFromBase(
         consumer,
         "copper_cluster_to_copper_ingot",
         new ItemStack(Items.COPPER_INGOT, 2),
         Ingredient.of(new ItemLike[]{(ItemLike)ModItems.COPPER_CLUSTER.get()}),
         (ItemLike)ModItems.COPPER_CLUSTER.get(),
         1.0F
      );
      smeltingResultFromBase(
         consumer,
         "gold_cluster_to_gold_ingot",
         new ItemStack(Items.GOLD_INGOT, 2),
         Ingredient.of(new ItemLike[]{(ItemLike)ModItems.GOLD_CLUSTER.get()}),
         (ItemLike)ModItems.GOLD_CLUSTER.get(),
         1.0F
      );
      smeltingResultFromBase(
         consumer,
         "cinnabar_cluster_to_quicksilver",
         new ItemStack((ItemLike)ModItems.QUICKSILVER.get(), 2),
         Ingredient.of(new ItemLike[]{(ItemLike)ModItems.CINNABAR_CLUSTER.get()}),
         (ItemLike)ModItems.CINNABAR_CLUSTER.get(),
         1.0F
      );
      smeltingResultFromBase(
         consumer,
         "gold_coin_to_gold_nugget",
         new ItemStack(Items.GOLD_NUGGET),
         Ingredient.of(new ItemLike[]{(ItemLike)ModItems.GOLD_COIN.get()}),
         (ItemLike)ModItems.GOLD_COIN.get(),
         0.0F
      );
      // TC4 Config: tin, silver and lead clusters smelt into two of whatever ingot another mod adds.
      for (ItemLike cluster : List.of(ModItems.TIN_CLUSTER.get(), ModItems.SILVER_CLUSTER.get(), ModItems.LEAD_CLUSTER.get())) {
         String metal = BuiltInRegistries.ITEM.getKey(cluster.asItem()).getPath().replace("_cluster", "");
         TagKey<Item> ingot = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/" + metal));
         consumer.accept(
            ResourceLocation.fromNamespaceAndPath("thaumcraft", "smelting/" + metal + "_cluster_to_" + metal + "_ingot"),
            new TagSmeltingRecipe("", CookingBookCategory.MISC, Ingredient.of(cluster), ingot, 2, 1.0F, 200),
            null
         );
      }
   }

   protected static void smeltingResultFromBase(
      RecipeOutput consumer, String recipeName, ItemStack result, Ingredient ingredient, ItemLike unlockItem, float exp
   ) {
      SimpleCookingRecipeBuilder.smelting(ingredient, RecipeCategory.MISC, result, exp, 200)
         .unlockedBy(getHasName(unlockItem), has(unlockItem))
         .save(consumer, ResourceLocation.fromNamespaceAndPath("thaumcraft", "smelting/" + recipeName));
   }
}
