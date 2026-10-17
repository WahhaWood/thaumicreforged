package thaumcraft.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.init.registry.ModItems;
import thaumcraft.recipe.TagSmeltingRecipe;

/** TC4 smelting extras: clusters into two ingots (ore dictionary result for modded metals) and the infernal furnace bonus table. */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class SmeltingGameTest {
   /** A tag result resolves to the tag's first item and does not match while nothing is in the tag. */
   @GameTest(template = "empty_1x1x1")
   public static void tagSmeltingResolvesTagResult(GameTestHelper helper) {
      ItemStack tinCluster = new ItemStack(ModItems.TIN_CLUSTER.get());
      TagSmeltingRecipe toIron = recipe(tinCluster.getItem(), "ingots/iron");
      helper.assertTrue(toIron.matches(new SingleRecipeInput(tinCluster), helper.getLevel()), "a filled tag must match");
      ItemStack result = toIron.assemble(new SingleRecipeInput(tinCluster), helper.getLevel().registryAccess());
      helper.assertTrue(result.is(Items.IRON_INGOT) && result.getCount() == 2, "result must be two of the tag's first item, got " + result);

      TagSmeltingRecipe toNothing = recipe(tinCluster.getItem(), "ingots/unobtainium");
      helper.assertTrue(!toNothing.matches(new SingleRecipeInput(tinCluster), helper.getLevel()), "an empty tag must not match");

      // The shipped JSON parses into the tag recipe.
      var loaded = helper.getLevel().getRecipeManager().byKey(Thaumcraft.location("smelting/tin_cluster_to_tin_ingot"));
      helper.assertTrue(loaded.isPresent() && loaded.get().value() instanceof TagSmeltingRecipe, "tin cluster recipe must load as a tag recipe");
      helper.succeed();
   }

   /** Vanilla furnaces get two ingots from an iron cluster as well. */
   @GameTest(template = "empty_1x1x1")
   public static void clusterSmeltsIntoTwoIngots(GameTestHelper helper) {
      ItemLike[][] cases = {
         {ModItems.IRON_CLUSTER.get(), Items.IRON_INGOT}, {ModItems.GOLD_CLUSTER.get(), Items.GOLD_INGOT},
         {ModItems.COPPER_CLUSTER.get(), Items.COPPER_INGOT}, {ModItems.CINNABAR_CLUSTER.get(), ModItems.QUICKSILVER.get()}
      };
      for (ItemLike[] c : cases) {
         SingleRecipeInput input = new SingleRecipeInput(new ItemStack(c[0]));
         ItemStack out = helper.getLevel()
            .getRecipeManager()
            .getRecipeFor(RecipeType.SMELTING, input, helper.getLevel())
            .map(r -> r.value().assemble(input, helper.getLevel().registryAccess()))
            .orElse(ItemStack.EMPTY);
         helper.assertTrue(out.is(c[1].asItem()) && out.getCount() == 2, c[0].asItem() + " must smelt into two " + c[1].asItem() + ", got " + out);
      }

      helper.succeed();
   }

   /** TC4 ConfigRecipes bonus table: ores and raw metal, clusters, meat; nothing for other items. */
   @GameTest(template = "empty_1x1x1")
   public static void smeltingBonusTable(GameTestHelper helper) {
      ItemLike[][] cases = {
         {Items.RAW_IRON, Items.IRON_NUGGET}, {Items.IRON_ORE, Items.IRON_NUGGET}, {Items.DEEPSLATE_GOLD_ORE, Items.GOLD_NUGGET},
         {Items.RAW_COPPER, ModItems.COPPER_NUGGET.get()}, {ModItems.CINNABAR_ORE.get(), ModItems.QUICKSILVER_NUGGET.get()},
         {ModItems.IRON_CLUSTER.get(), Items.IRON_NUGGET}, {ModItems.CINNABAR_CLUSTER.get(), ModItems.QUICKSILVER_NUGGET.get()},
         {Items.CHICKEN, ModItems.CHICKEN_NUGGET.get()}, {Items.BEEF, ModItems.BEEF_NUGGET.get()},
         {Items.PORKCHOP, ModItems.PORK_NUGGET.get()}, {Items.COD, ModItems.FISH_NUGGET.get()}
      };
      for (ItemLike[] c : cases) {
         ItemStack bonus = ThaumcraftApi.getSmeltingBonus(new ItemStack(c[0]));
         helper.assertTrue(bonus.is(c[1].asItem()) && bonus.getCount() == 1, c[0].asItem() + " must give " + c[1].asItem() + ", got " + bonus);
      }

      helper.assertTrue(ThaumcraftApi.getSmeltingBonus(new ItemStack(Items.STONE)).isEmpty(), "stone has no bonus");
      helper.succeed();
   }

   private static TagSmeltingRecipe recipe(Item input, String tag) {
      return new TagSmeltingRecipe(
         "", CookingBookCategory.MISC, Ingredient.of(input), ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", tag)), 2, 1.0F, 200
      );
   }
}
