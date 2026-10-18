package thaumcraft.data.provider.recipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.data.builder.ShapedTagRecipeBuilder;
import thaumcraft.data.builder.ShapelessNBTRecipeBuilder;
import thaumcraft.init.registry.ModItemTags;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.mob_aspect.ItemMobAspect;
import thaumcraft.item.mob_aspect.NumericAspectHelper;
import thaumcraft.item.research_notes.ItemResearchNotes;
import thaumcraft.item.wands.ItemWandCap;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.ItemWandRod;

public class CrafingRecipeGenerator extends RecipeProvider {
   public CrafingRecipeGenerator(PackOutput output, CompletableFuture<Provider> registries) {
      super(output, registries);
   }

   public void generate(@NotNull RecipeOutput consumer) {
      this.buildRecipes(consumer);
   }

   protected void buildRecipes(@NotNull RecipeOutput consumer) {
      this.shapeless((ItemLike)ModItems.THAUMIUM_NUGGET.get(), 9)
         .requires((ItemLike)ModItems.THAUMIUM_INGOT.get())
         .unlockedBy("has_thaumium_ingot", has((ItemLike)ModItems.THAUMIUM_INGOT.get()))
         .save(consumer, Thaumcraft.location("shapeless/thaumium_nugget"));
      this.shapeless((ItemLike)ModItems.VOID_METAL_NUGGET.get(), 9)
         .requires(ModItemTags.VOID_INGOT)
         .unlockedBy("has_void_metal_ingot", has((ItemLike)ModItems.VOID_METAL_INGOT.get()))
         .save(consumer, Thaumcraft.location("shapeless/void_metal_nugget"));
      this.shapeless((ItemLike)ModItems.QUICKSILVER_NUGGET.get(), 9)
         .requires((ItemLike)ModItems.QUICKSILVER.get())
         .unlockedBy("has_quicksilver", has((ItemLike)ModItems.QUICKSILVER.get()))
         .save(consumer, Thaumcraft.location("shapeless/quicksilver_nugget"));
      this.shapeless((ItemLike)ModItems.ICHORIUM_NUGGET.get(), 9)
         .requires((ItemLike)ModItems.ICHORIUM.get())
         .unlockedBy("has_ichorium", has((ItemLike)ModItems.ICHORIUM.get()))
         .save(consumer, Thaumcraft.location("shapeless/ichorium_nugget"));
      this.shapeless((ItemLike)ModItems.TRIPLE_MEAT_TREAT.get())
         .requires(Items.SUGAR)
         .requires((ItemLike)ModItems.BEEF_NUGGET.get())
         .requires((ItemLike)ModItems.CHICKEN_NUGGET.get())
         .requires((ItemLike)ModItems.PORK_NUGGET.get())
         .unlockedBy("has_beef_nugget", has((ItemLike)ModItems.BEEF_NUGGET.get()))
         .unlockedBy("has_chicken_nugget", has((ItemLike)ModItems.CHICKEN_NUGGET.get()))
         .unlockedBy("has_pork_nugget", has((ItemLike)ModItems.PORK_NUGGET.get()))
         .save(consumer, Thaumcraft.location("shapeless/triple_meat_treat_0"));
      this.shapeless((ItemLike)ModItems.TRIPLE_MEAT_TREAT.get())
         .requires(Items.SUGAR)
         .requires((ItemLike)ModItems.BEEF_NUGGET.get())
         .requires((ItemLike)ModItems.CHICKEN_NUGGET.get())
         .requires((ItemLike)ModItems.FISH_NUGGET.get())
         .unlockedBy("has_beef_nugget", has((ItemLike)ModItems.BEEF_NUGGET.get()))
         .unlockedBy("has_chicken_nugget", has((ItemLike)ModItems.CHICKEN_NUGGET.get()))
         .unlockedBy("has_fish_nugget", has((ItemLike)ModItems.FISH_NUGGET.get()))
         .save(consumer, Thaumcraft.location("shapeless/triple_meat_treat_1"));
      this.shapeless((ItemLike)ModItems.TRIPLE_MEAT_TREAT.get())
         .requires(Items.SUGAR)
         .requires((ItemLike)ModItems.BEEF_NUGGET.get())
         .requires((ItemLike)ModItems.FISH_NUGGET.get())
         .requires((ItemLike)ModItems.PORK_NUGGET.get())
         .unlockedBy("has_beef_nugget", has((ItemLike)ModItems.BEEF_NUGGET.get()))
         .unlockedBy("has_fish_nugget", has((ItemLike)ModItems.FISH_NUGGET.get()))
         .unlockedBy("has_pork_nugget", has((ItemLike)ModItems.PORK_NUGGET.get()))
         .save(consumer, Thaumcraft.location("shapeless/triple_meat_treat_2"));
      this.shapeless((ItemLike)ModItems.TRIPLE_MEAT_TREAT.get())
         .requires(Items.SUGAR)
         .requires((ItemLike)ModItems.FISH_NUGGET.get())
         .requires((ItemLike)ModItems.CHICKEN_NUGGET.get())
         .requires((ItemLike)ModItems.PORK_NUGGET.get())
         .unlockedBy("has_fish_nugget", has((ItemLike)ModItems.FISH_NUGGET.get()))
         .unlockedBy("has_chicken_nugget", has((ItemLike)ModItems.CHICKEN_NUGGET.get()))
         .unlockedBy("has_pork_nugget", has((ItemLike)ModItems.PORK_NUGGET.get()))
         .save(consumer, Thaumcraft.location("shapeless/triple_meat_treat_3"));
      this.shapeless((ItemLike)ModItems.QUICKSILVER.get())
         .requires((ItemLike)ModItems.SHIMMER_LEAF.get())
         .unlockedBy("has_shimmer_leaf", has((ItemLike)ModItems.SHIMMER_LEAF.get()))
         .save(consumer, Thaumcraft.location("shapeless/quicksilver_from_leaf"));
      this.shapeless(Items.BLAZE_POWDER)
         .requires((ItemLike)ModItems.CINDER_PEARL.get())
         .unlockedBy("has_cinder_pearl", has((ItemLike)ModItems.CINDER_PEARL.get()))
         .save(consumer, Thaumcraft.location("shapeless/blaze_powder_from_pearl"));
       // Мясные наггетсы: без них triple_meat_treat не скрафтить (источника не было).
       this.shapeless((ItemLike)ModItems.CHICKEN_NUGGET.get(), 2)
          .requires(Items.CHICKEN)
          .unlockedBy("has_chicken", has(Items.CHICKEN))
          .save(consumer, Thaumcraft.location("shapeless/chicken_nugget"));
       this.shapeless((ItemLike)ModItems.BEEF_NUGGET.get(), 2)
          .requires(Items.BEEF)
          .unlockedBy("has_beef", has(Items.BEEF))
          .save(consumer, Thaumcraft.location("shapeless/beef_nugget"));
       this.shapeless((ItemLike)ModItems.PORK_NUGGET.get(), 2)
          .requires(Items.PORKCHOP)
          .unlockedBy("has_porkchop", has(Items.PORKCHOP))
          .save(consumer, Thaumcraft.location("shapeless/pork_nugget"));
       this.shapeless((ItemLike)ModItems.FISH_NUGGET.get(), 2)
          .requires(Items.COD)
          .unlockedBy("has_cod", has(Items.COD))
          .save(consumer, Thaumcraft.location("shapeless/fish_nugget"));      this.shapeless((ItemLike)ModItems.JAR_LABEL.get(), 4)
         .requires(net.neoforged.neoforge.common.Tags.Items.DYES_BLACK)
         .requires(Items.SLIME_BALL)
         .requires(Items.PAPER)
         .requires(Items.PAPER)
         .requires(Items.PAPER)
         .requires(Items.PAPER)
         .unlockedBy("has_dyes_black", has(net.neoforged.neoforge.common.Tags.Items.DYES_BLACK))
         .save(consumer, Thaumcraft.location("shapeless/jar_label"));
      this.jarLabelRecipes(consumer);
      this.shapeless((ItemLike)ModItems.GREATWOOD_PLANKS.get(), 4)
         .requires((ItemLike)ModItems.GREATWOOD_LOG.get())
         .unlockedBy("has_greatwood_log", has((ItemLike)ModItems.GREATWOOD_LOG.get()))
         .save(consumer, Thaumcraft.location("shapeless/greatwood_planks"));
      this.shapeless((ItemLike)ModItems.SILVERWOOD_PLANKS.get(), 4)
         .requires((ItemLike)ModItems.SILVERWOOD_LOG.get())
         .unlockedBy("has_silverwood_log", has((ItemLike)ModItems.SILVERWOOD_LOG.get()))
         .save(consumer, Thaumcraft.location("shapeless/silverwood_planks"));
      this.shapeless((ItemLike)ModItems.THAUMIUM_INGOT.get(), 9)
         .requires((ItemLike)ModItems.THAUMIUM_BLOCK.get())
         .unlockedBy("has_thaumium_block", has((ItemLike)ModItems.THAUMIUM_BLOCK.get()))
         .save(consumer, Thaumcraft.location("shapeless/thaumium_ingot"));
      this.shapeless((ItemLike)ModItems.MAGIC_TALLOW.get(), 9)
         .requires((ItemLike)ModItems.MAGIC_TALLOW_BLOCK.get())
         .unlockedBy("has_magic_tallow", has((ItemLike)ModItems.MAGIC_TALLOW.get()))
         .save(consumer, Thaumcraft.location("shaped/magic_tallow"));
      this.shapelessCluster((ItemLike)ModItems.AIR_CRYSTAL_CLUSTER.get(), (ItemLike)ModItems.AIR_SHARD.get(), consumer);
      this.shapelessCluster((ItemLike)ModItems.FIRE_CRYSTAL_CLUSTER.get(), (ItemLike)ModItems.FIRE_SHARD.get(), consumer);
      this.shapelessCluster((ItemLike)ModItems.WATER_CRYSTAL_CLUSTER.get(), (ItemLike)ModItems.WATER_SHARD.get(), consumer);
      this.shapelessCluster((ItemLike)ModItems.EARTH_CRYSTAL_CLUSTER.get(), (ItemLike)ModItems.EARTH_SHARD.get(), consumer);
      this.shapelessCluster((ItemLike)ModItems.ORDER_CRYSTAL_CLUSTER.get(), (ItemLike)ModItems.ORDER_SHARD.get(), consumer);
      this.shapelessCluster((ItemLike)ModItems.ENTROPY_CRYSTAL_CLUSTER.get(), (ItemLike)ModItems.ENTROPY_SHARD.get(), consumer);
      this.shapeless((ItemLike)ModItems.MIXED_CRYSTAL_CLUSTER.get())
         .requires((ItemLike)ModItems.AIR_SHARD.get())
         .requires((ItemLike)ModItems.FIRE_SHARD.get())
         .requires((ItemLike)ModItems.WATER_SHARD.get())
         .requires((ItemLike)ModItems.EARTH_SHARD.get())
         .requires((ItemLike)ModItems.ORDER_SHARD.get())
         .requires((ItemLike)ModItems.ENTROPY_SHARD.get())
         .unlockedBy("has_shards", has(ModItemTags.SHARDS))
         .save(consumer, Thaumcraft.location("shapeless/mixed_crystal_cluster"));
      this.shapeless((ItemLike)ModItems.AMBER.get(), 4)
         .requires((ItemLike)ModItems.AMBER_BLOCK.get())
         .unlockedBy("has_amber_block", has((ItemLike)ModItems.AMBER_BLOCK.get()))
         .save(consumer, Thaumcraft.location("shapeless/amber_block"));
      this.shapeless((ItemLike)ModItems.AMBER.get(), 4)
         .requires((ItemLike)ModItems.AMBER_BRICKS.get())
         .unlockedBy("has_amber_block", has((ItemLike)ModItems.AMBER_BLOCK.get()))
         .save(consumer, Thaumcraft.location("shapeless/amber_bricks"));
      this.shapeless((ItemLike)ModItems.INKWELL.get())
         .requires((ItemLike)ModItems.PHIAL_ESSENCE.get())
         .requires(Items.FEATHER)
         .requires(net.neoforged.neoforge.common.Tags.Items.DYES_BLACK)
         .unlockedBy("has_dyes_black", has(net.neoforged.neoforge.common.Tags.Items.DYES_BLACK))
         .save(consumer, Thaumcraft.location("shapeless/inkwell_from_phial"));
      this.shapeless((ItemLike)ModItems.INKWELL.get())
         .requires(Items.GLASS_BOTTLE)
         .requires(Items.FEATHER)
         .requires(net.neoforged.neoforge.common.Tags.Items.DYES_BLACK)
         .unlockedBy("has_dyes_black", has(net.neoforged.neoforge.common.Tags.Items.DYES_BLACK))
         .save(consumer, Thaumcraft.location("shapeless/inkwell"));
      this.shapeless((ItemLike)ModItems.INKWELL.get())
         .requires((ItemLike)ModItems.INKWELL.get())
         .requires(net.neoforged.neoforge.common.Tags.Items.DYES_BLACK)
         .unlockedBy("has_dyes_black", has(net.neoforged.neoforge.common.Tags.Items.DYES_BLACK))
         .save(consumer, Thaumcraft.location("shapeless/inkwell_repair"));
      this.shaped((ItemLike)ModItems.THAUMIUM_INGOT.get())
         .pattern("###")
         .pattern("###")
         .pattern("###")
         .define('#', ModItemTags.THAUMIUM_NUGGET)
         .unlockedBy("has_thaumium_nugget", has((ItemLike)ModItems.THAUMIUM_NUGGET.get()))
         .save(consumer, Thaumcraft.location("shaped/thaumium_ingot"));
      this.shaped((ItemLike)ModItems.VOID_METAL_INGOT.get())
         .pattern("###")
         .pattern("###")
         .pattern("###")
         .define('#', ModItemTags.VOID_NUGGET)
         .unlockedBy("has_void_metal_nugget", has((ItemLike)ModItems.VOID_METAL_NUGGET.get()))
         .save(consumer, Thaumcraft.location("shaped/void_metal_ingot"));
      this.shaped((ItemLike)ModItems.QUICKSILVER.get())
         .pattern("###")
         .pattern("###")
         .pattern("###")
         .define('#', (ItemLike)ModItems.QUICKSILVER_NUGGET.get())
         .unlockedBy("has_quicksilver_nugget", has((ItemLike)ModItems.QUICKSILVER_NUGGET.get()))
         .save(consumer, Thaumcraft.location("shaped/quicksilver"));
      this.shaped((ItemLike)ModItems.ICHORIUM.get())
         .pattern("###")
         .pattern("###")
         .pattern("###")
         .define('#', ModItemTags.ICHORIUM_NUGGET)
         .unlockedBy("has_ichorium_nugget", has((ItemLike)ModItems.ICHORIUM_NUGGET.get()))
         .save(consumer, Thaumcraft.location("shaped/ichorium"));
      this.shaped((ItemLike)ModItems.CURIOS_AMULET.get())
         .pattern(" S ")
         .pattern("S S")
         .pattern(" I ")
         .define('S', net.neoforged.neoforge.common.Tags.Items.STRINGS)
         .define('I', Items.GOLD_INGOT)
         .unlockedBy("has_gold_nugget", has(Items.GOLD_INGOT))
         .save(consumer, Thaumcraft.location("shaped/curios_amulet"));
      this.shaped((ItemLike)ModItems.CURIOS_RING.get())
         .pattern(" N ")
         .pattern("N N")
         .pattern(" N ")
         .define('N', Items.GOLD_NUGGET)
         .unlockedBy("has_gold_nugget", has(Items.GOLD_INGOT))
         .save(consumer, Thaumcraft.location("shaped/curios_ring"));
      this.shaped((ItemLike)ModItems.CURIOS_BELT.get())
         .pattern(" L ")
         .pattern("L L")
         .pattern(" I ")
         .define('L', net.neoforged.neoforge.common.Tags.Items.LEATHERS)
         .define('I', Items.GOLD_INGOT)
         .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
         .save(consumer, Thaumcraft.location("shaped/curios_belt"));
      this.shaped((ItemLike)ModItems.IRON_WAND_CAP.get())
         .pattern("NNN")
         .pattern("N N")
         .define('N', Items.IRON_NUGGET)
         .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
         .save(consumer, Thaumcraft.location("shaped/iron_wand_cap"));
      ShapedTagRecipeBuilder.shaped(ItemWandCasting.createWand((ItemWandRod)ModItems.WOOD_WAND_ROD.get(), (ItemWandCap)ModItems.IRON_WAND_CAP.get()))
         .pattern("  I")
         .pattern(" S ")
         .pattern("I  ")
         .define('I', (ItemLike)ModItems.IRON_WAND_CAP.get())
         .define('S', Items.STICK)
         .save(consumer, Thaumcraft.location("shaped/basic_wand"));
      ItemStack stack = new ItemStack((ItemLike)ModItems.RESEARCH_NOTES.get());
      ((ItemResearchNotes)stack.getItem()).setKnowledgeFragment(stack);
      ShapedTagRecipeBuilder.shaped(stack)
         .pattern("###")
         .pattern("###")
         .pattern("###")
         .define('#', (ItemLike)ModItems.KNOWLEDGE_FRAGMENT.get())
         .save(consumer, Thaumcraft.location("shaped/research_notes"));
      this.shaped((ItemLike)ModItems.DARK_QUARTZ.get(), 4)
         .pattern("Q Q")
         .pattern(" C ")
         .pattern("Q Q")
         .define('Q', Items.QUARTZ)
         .define('C', Items.COAL)
         .unlockedBy("has_quartz", has(Items.QUARTZ))
         .save(consumer, Thaumcraft.location("shaped/dark_quartz_coal"));
      this.shaped((ItemLike)ModItems.DARK_QUARTZ.get(), 4)
         .pattern("Q Q")
         .pattern(" C ")
         .pattern("Q Q")
         .define('Q', Items.QUARTZ)
         .define('C', Items.CHARCOAL)
         .unlockedBy("has_quartz", has(Items.QUARTZ))
         .save(consumer, Thaumcraft.location("shaped/dark_quartz_charcoal"));

      for (int i = 0; i < NumericAspectHelper.getCount(); i++) {
         Aspect aspect = NumericAspectHelper.getAspect(i);
         if (aspect != null) {
            ItemStack mobAspect = ItemMobAspect.getStackFromAspect(aspect);
            ShapedTagRecipeBuilder.shaped(ItemMobAspect.getCondensedStackFromAspect(aspect))
               .pattern("XXX")
               .pattern("XXX")
               .pattern("XXX")
               .define('X', DataComponentIngredient.of(true, mobAspect))
               .save(consumer, Thaumcraft.location("shaped/mob_aspect_condensed_" + aspect.getTag()));
         }
      }

      this.shaped((ItemLike)ModItems.GREATWOOD_STAIRS.get())
         .pattern("K  ")
         .pattern("KK ")
         .pattern("KKK")
         .define('K', (ItemLike)ModItems.GREATWOOD_PLANKS.get())
         .unlockedBy("has_greatwood_planks", has((ItemLike)ModItems.GREATWOOD_PLANKS.get()))
         .save(consumer, Thaumcraft.location("shaped/greatwood_stairs"));
      this.shaped((ItemLike)ModItems.SILVERWOOD_STAIRS.get())
         .pattern("K  ")
         .pattern("KK ")
         .pattern("KKK")
         .define('K', (ItemLike)ModItems.SILVERWOOD_PLANKS.get())
         .unlockedBy("has_silverwood_planks", has((ItemLike)ModItems.SILVERWOOD_PLANKS.get()))
         .save(consumer, Thaumcraft.location("shaped/silverwood_stairs"));
      this.shaped((ItemLike)ModItems.GREATWOOD_SLAB.get())
         .pattern("KKK")
         .define('K', (ItemLike)ModItems.GREATWOOD_PLANKS.get())
         .unlockedBy("has_greatwood_planks", has((ItemLike)ModItems.GREATWOOD_PLANKS.get()))
         .save(consumer, Thaumcraft.location("shaped/greatwood_slab"));
      this.shaped((ItemLike)ModItems.SILVERWOOD_SLAB.get())
         .pattern("KKK")
         .define('K', (ItemLike)ModItems.SILVERWOOD_PLANKS.get())
         .unlockedBy("has_silverwood_planks", has((ItemLike)ModItems.SILVERWOOD_PLANKS.get()))
         .save(consumer, Thaumcraft.location("shaped/silverwood_slab"));
      this.shaped((ItemLike)ModItems.FLESH_BLOCK.get())
         .pattern("###")
         .pattern("###")
         .pattern("###")
         .define('#', Items.ROTTEN_FLESH)
         .unlockedBy("has_rotten_flesh", has(Items.ROTTEN_FLESH))
         .save(consumer, Thaumcraft.location("shaped/flesh_block"));
      this.shaped((ItemLike)ModItems.THAUMIUM_BLOCK.get())
         .pattern("###")
         .pattern("###")
         .pattern("###")
         .define('#', ModItemTags.THAUMIUM_INGOT)
         .unlockedBy("has_thaumium_ingot", has((ItemLike)ModItems.THAUMIUM_INGOT.get()))
         .save(consumer, Thaumcraft.location("shaped/thaumium_block"));
      this.shaped((ItemLike)ModItems.MAGIC_TALLOW_BLOCK.get())
         .pattern("###")
         .pattern("###")
         .pattern("###")
         .define('#', (ItemLike)ModItems.MAGIC_TALLOW.get())
         .unlockedBy("has_magic_tallow", has((ItemLike)ModItems.MAGIC_TALLOW.get()))
         .save(consumer, Thaumcraft.location("shaped/magic_tallow_block"));
      this.shaped((ItemLike)ModItems.AMBER_BLOCK.get())
         .pattern("##")
         .pattern("##")
         .define('#', (ItemLike)ModItems.AMBER.get())
         .unlockedBy("has_amber", has((ItemLike)ModItems.AMBER.get()))
         .save(consumer, Thaumcraft.location("shaped/amber_block"));
      this.shaped((ItemLike)ModItems.AMBER_BRICKS.get(), 4)
         .pattern("##")
         .pattern("##")
         .define('#', (ItemLike)ModItems.AMBER_BLOCK.get())
         .unlockedBy("has_amber", has((ItemLike)ModItems.AMBER.get()))
         .save(consumer, Thaumcraft.location("shaped/amber_bricks"));
      this.shaped((ItemLike)ModItems.OBSIDIAN_TILE.get(), 4)
         .pattern("##")
         .pattern("##")
         .define('#', Items.OBSIDIAN)
         .unlockedBy("has_obsidian", has(Items.OBSIDIAN))
         .save(consumer, Thaumcraft.location("shaped/obsidian_tile"));
      this.shaped((ItemLike)ModItems.GRATE.get())
         .pattern("#")
         .pattern("T")
         .pattern("#")
         .define('#', Items.IRON_BARS)
         .define('T', ItemTags.TRAPDOORS)
         .unlockedBy("has_iron_bars", has(Items.IRON_BARS))
         .save(consumer, Thaumcraft.location("shaped/grate"));
      this.shaped((ItemLike)ModItems.PHIAL_ESSENCE.get(), 8)
         .pattern(" C ")
         .pattern("G G")
         .pattern(" G ")
         .define('G', net.neoforged.neoforge.common.Tags.Items.GLASS_BLOCKS)
         .define('C', Items.CLAY_BALL)
         .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
         .save(consumer, Thaumcraft.location("shaped/phial_essence"));
      this.shaped((ItemLike)ModItems.TABLE.get())
         .pattern("SSS")
         .pattern("W W")
         .define('S', ItemTags.WOODEN_SLABS)
         .define('W', ItemTags.PLANKS)
         .unlockedBy("has_planks", has(ItemTags.PLANKS))
         .save(consumer, Thaumcraft.location("shaped/table"));
      this.shaped((ItemLike)ModItems.THAUMOMETER.get())
         .pattern(" 1 ")
         .pattern("IGI")
         .pattern(" 1 ")
         .define('I', Items.GOLD_INGOT)
         .define('G', Items.GLASS)
         .define('1', ModItemTags.SHARDS_BASICS)
         .unlockedBy("has_shards_basics", has(ModItemTags.SHARDS_BASICS))
         .save(consumer, Thaumcraft.location("shaped/thaumometer"));
      this.shaped((ItemLike)ModItems.THAUMIUM_HELMET.get())
         .pattern("III")
         .pattern("I I")
         .define('I', ModItemTags.THAUMIUM_INGOT)
         .unlockedBy("has_thaumium_ingot", has((ItemLike)ModItems.THAUMIUM_INGOT.get()))
         .save(consumer, Thaumcraft.location("shaped/thaumium_helmet"));
      this.shaped((ItemLike)ModItems.THAUMIUM_CHEST.get())
         .pattern("I I")
         .pattern("III")
         .pattern("III")
         .define('I', ModItemTags.THAUMIUM_INGOT)
         .unlockedBy("has_thaumium_ingot", has((ItemLike)ModItems.THAUMIUM_INGOT.get()))
         .save(consumer, Thaumcraft.location("shaped/thaumium_chest"));
      this.shaped((ItemLike)ModItems.THAUMIUM_LEGGINGS.get())
         .pattern("III")
         .pattern("I I")
         .pattern("I I")
         .define('I', ModItemTags.THAUMIUM_INGOT)
         .unlockedBy("has_thaumium_ingot", has((ItemLike)ModItems.THAUMIUM_INGOT.get()))
         .save(consumer, Thaumcraft.location("shaped/thaumium_leggings"));
      this.shaped((ItemLike)ModItems.THAUMIUM_BOOTS.get())
         .pattern("I I")
         .pattern("I I")
         .define('I', ModItemTags.THAUMIUM_INGOT)
         .unlockedBy("has_thaumium_ingot", has((ItemLike)ModItems.THAUMIUM_INGOT.get()))
         .save(consumer, Thaumcraft.location("shaped/thaumium_boots"));
      this.shaped((ItemLike)ModItems.THAUMIUM_SHOVEL.get())
         .pattern("I")
         .pattern("S")
         .pattern("S")
         .define('I', ModItemTags.THAUMIUM_INGOT)
         .define('S', Items.STICK)
         .unlockedBy("has_thaumium_ingot", has((ItemLike)ModItems.THAUMIUM_INGOT.get()))
         .save(consumer, Thaumcraft.location("shaped/thaumium_shovel"));
      this.shaped((ItemLike)ModItems.THAUMIUM_PICKAXE.get())
         .pattern("III")
         .pattern(" S ")
         .pattern(" S ")
         .define('I', ModItemTags.THAUMIUM_INGOT)
         .define('S', Items.STICK)
         .unlockedBy("has_thaumium_ingot", has((ItemLike)ModItems.THAUMIUM_INGOT.get()))
         .save(consumer, Thaumcraft.location("shaped/thaumium_pickaxe"));
      this.shaped((ItemLike)ModItems.THAUMIUM_AXE.get())
         .pattern("II")
         .pattern("SI")
         .pattern("S ")
         .define('I', ModItemTags.THAUMIUM_INGOT)
         .define('S', Items.STICK)
         .unlockedBy("has_thaumium_ingot", has((ItemLike)ModItems.THAUMIUM_INGOT.get()))
         .save(consumer, Thaumcraft.location("shaped/thaumium_axe"));
      this.shaped((ItemLike)ModItems.THAUMIUM_HOE.get())
         .pattern("II")
         .pattern("S ")
         .pattern("S ")
         .define('I', ModItemTags.THAUMIUM_INGOT)
         .define('S', Items.STICK)
         .unlockedBy("has_thaumium_ingot", has((ItemLike)ModItems.THAUMIUM_INGOT.get()))
         .save(consumer, Thaumcraft.location("shaped/thaumium_hoe"));
      this.shaped((ItemLike)ModItems.THAUMIUM_SWORD.get())
         .pattern("I")
         .pattern("I")
         .pattern("S")
         .define('I', ModItemTags.THAUMIUM_INGOT)
         .define('S', Items.STICK)
         .unlockedBy("has_thaumium_ingot", has((ItemLike)ModItems.THAUMIUM_INGOT.get()))
         .save(consumer, Thaumcraft.location("shaped/thaumium_sword"));
      this.shaped((ItemLike)ModItems.VOID_HELMET.get())
         .pattern("III")
         .pattern("I I")
         .define('I', ModItemTags.VOID_INGOT)
         .unlockedBy("has_void_metal_ingot", has((ItemLike)ModItems.VOID_METAL_NUGGET.get()))
         .save(consumer, Thaumcraft.location("shaped/void_helmet"));
      this.shaped((ItemLike)ModItems.VOID_CHEST.get())
         .pattern("I I")
         .pattern("III")
         .pattern("III")
         .define('I', ModItemTags.VOID_INGOT)
         .unlockedBy("has_void_metal_ingot", has((ItemLike)ModItems.VOID_METAL_NUGGET.get()))
         .save(consumer, Thaumcraft.location("shaped/void_chest"));
      this.shaped((ItemLike)ModItems.VOID_LEGGINGS.get())
         .pattern("III")
         .pattern("I I")
         .pattern("I I")
         .define('I', ModItemTags.VOID_INGOT)
         .unlockedBy("has_void_metal_ingot", has((ItemLike)ModItems.VOID_METAL_NUGGET.get()))
         .save(consumer, Thaumcraft.location("shaped/void_leggings"));
      this.shaped((ItemLike)ModItems.VOID_BOOTS.get())
         .pattern("I I")
         .pattern("I I")
         .define('I', ModItemTags.VOID_INGOT)
         .unlockedBy("has_void_metal_ingot", has((ItemLike)ModItems.VOID_METAL_NUGGET.get()))
         .save(consumer, Thaumcraft.location("shaped/void_boots"));
      this.shaped((ItemLike)ModItems.VOID_SHOVEL.get())
         .pattern("I")
         .pattern("S")
         .pattern("S")
         .define('I', ModItemTags.VOID_INGOT)
         .define('S', Items.STICK)
         .unlockedBy("has_void_metal_ingot", has((ItemLike)ModItems.VOID_METAL_NUGGET.get()))
         .save(consumer, Thaumcraft.location("shaped/void_shovel"));
      this.shaped((ItemLike)ModItems.VOID_PICKAXE.get())
         .pattern("III")
         .pattern(" S ")
         .pattern(" S ")
         .define('I', ModItemTags.VOID_INGOT)
         .define('S', Items.STICK)
         .unlockedBy("has_void_metal_ingot", has((ItemLike)ModItems.VOID_METAL_NUGGET.get()))
         .save(consumer, Thaumcraft.location("shaped/void_pickaxe"));
      this.shaped((ItemLike)ModItems.VOID_AXE.get())
         .pattern("II")
         .pattern("SI")
         .pattern("S ")
         .define('I', ModItemTags.VOID_INGOT)
         .define('S', Items.STICK)
         .unlockedBy("has_void_metal_ingot", has((ItemLike)ModItems.VOID_METAL_NUGGET.get()))
         .save(consumer, Thaumcraft.location("shaped/void_axe"));
      this.shaped((ItemLike)ModItems.VOID_HOE.get())
         .pattern("II")
         .pattern("S ")
         .pattern("S ")
         .define('I', ModItemTags.VOID_INGOT)
         .define('S', Items.STICK)
         .unlockedBy("has_void_metal_ingot", has((ItemLike)ModItems.VOID_METAL_NUGGET.get()))
         .save(consumer, Thaumcraft.location("shaped/void_hoe"));
      this.shaped((ItemLike)ModItems.VOID_SWORD.get())
         .pattern("I")
         .pattern("I")
         .pattern("S")
         .define('I', ModItemTags.VOID_INGOT)
         .define('S', Items.STICK)
         .unlockedBy("has_void_metal_ingot", has((ItemLike)ModItems.VOID_METAL_NUGGET.get()))
         .save(consumer, Thaumcraft.location("shaped/void_sword"));
      this.shaped((ItemLike)ModItems.CANDLE.get(DyeColor.WHITE).get(), 3)
         .pattern(" S ")
         .pattern(" T ")
         .pattern(" T ")
         .define('S', net.neoforged.neoforge.common.Tags.Items.STRINGS)
         .define('T', (ItemLike)ModItems.MAGIC_TALLOW.get())
         .unlockedBy("has_magic_tallow", has((ItemLike)ModItems.MAGIC_TALLOW.get()))
         .save(consumer, Thaumcraft.location("shaped/candle"));

      for (DyeColor color : DyeColor.values()) {
         if (color != DyeColor.WHITE) {
            this.shapeless((ItemLike)ModItems.CANDLE.get(color).get())
               .requires(this.dye(color))
               .requires((ItemLike)ModItems.CANDLE.get(DyeColor.WHITE).get())
               .unlockedBy("has_white_candle", has((ItemLike)ModItems.CANDLE.get(DyeColor.WHITE).get()))
               .save(consumer, Thaumcraft.location("shapeless/" + color.getName() + "_candle"));
         }
      }

      this.shapeless((ItemLike)ModItems.CANDLE.get(DyeColor.WHITE).get())
         .requires(Items.WHITE_DYE)
         .requires(ModItemTags.CANDLES)
         .unlockedBy("has_candle", has(ModItemTags.CANDLES))
         .save(consumer, Thaumcraft.location("shapeless/white_candle"));
      this.shaped((ItemLike)ModItems.TRAVEL_STONE_SLAB.get(), 6)
         .pattern("KKK")
         .define('K', (ItemLike)ModItems.TRAVEL_STONE.get())
         .unlockedBy("has_travel_stone", has((ItemLike)ModItems.TRAVEL_STONE.get()))
         .save(consumer, Thaumcraft.location("shaped/travel_stone_slab"));
      this.shaped((ItemLike)ModItems.TRAVEL_STONE_STAIRS.get(), 4)
         .pattern("K  ")
         .pattern("KK ")
         .pattern("KKK")
         .define('K', (ItemLike)ModItems.TRAVEL_STONE.get())
         .unlockedBy("has_travel_stone", has((ItemLike)ModItems.TRAVEL_STONE.get()))
         .save(consumer, Thaumcraft.location("shaped/travel_stone_stairs"));
      this.shaped((ItemLike)ModItems.DARK_QUARTZ_BLOCK.get())
         .pattern("QQ")
         .pattern("QQ")
         .define('Q', (ItemLike)ModItems.DARK_QUARTZ.get())
         .unlockedBy("has_dark_quartz", has((ItemLike)ModItems.DARK_QUARTZ.get()))
         .save(consumer, Thaumcraft.location("shaped/dark_quartz_block"));
      this.shaped((ItemLike)ModItems.DARK_QUARTZ_SLAB.get(), 6)
         .pattern("QQQ")
         .define('Q', (ItemLike)ModItems.DARK_QUARTZ_BLOCK.get())
         .unlockedBy("has_dark_quartz_block", has((ItemLike)ModItems.DARK_QUARTZ_BLOCK.get()))
         .save(consumer, Thaumcraft.location("shaped/dark_quartz_slab"));
      this.shaped((ItemLike)ModItems.DARK_QUARTZ_STAIRS.get(), 4)
         .pattern("Q  ")
         .pattern("QQ ")
         .pattern("QQQ")
         .define('Q', (ItemLike)ModItems.DARK_QUARTZ_BLOCK.get())
         .unlockedBy("has_dark_quartz_block", has((ItemLike)ModItems.DARK_QUARTZ_BLOCK.get()))
         .save(consumer, Thaumcraft.location("shaped/dark_quartz_stairs"));
      this.shaped((ItemLike)ModItems.DARK_QUARTZ_PILLAR.get(), 2)
         .pattern("Q")
         .pattern("Q")
         .define('Q', (ItemLike)ModItems.DARK_QUARTZ_BLOCK.get())
         .unlockedBy("has_dark_quartz_block", has((ItemLike)ModItems.DARK_QUARTZ_BLOCK.get()))
         .save(consumer, Thaumcraft.location("shaped/dark_quartz_pillar"));
      this.shaped((ItemLike)ModItems.CHISELED_DARK_QUARTZ_BLOCK.get())
         .pattern("Q")
         .pattern("Q")
         .define('Q', (ItemLike)ModItems.DARK_QUARTZ_SLAB.get())
         .unlockedBy("has_dark_quartz_slab", has((ItemLike)ModItems.DARK_QUARTZ_SLAB.get()))
         .save(consumer, Thaumcraft.location("shaped/chiseled_dark_quartz_block"));
      this.shaped((ItemLike)ModItems.ARCANE_BRICKS.get(), 4)
         .pattern("SS")
         .pattern("SS")
         .define('S', (ItemLike)ModItems.ARCANE_STONE.get())
         .unlockedBy("has_arcane_stone", has((ItemLike)ModItems.ARCANE_STONE.get()))
         .save(consumer, Thaumcraft.location("shaped/arcane_stone_bricks"));
      this.shaped((ItemLike)ModItems.ARCANE_STONE_STAIRS.get(), 4)
         .pattern("K  ")
         .pattern("KK ")
         .pattern("KKK")
         .define('K', (ItemLike)ModItems.ARCANE_BRICKS.get())
         .unlockedBy("has_arcane_stone", has((ItemLike)ModItems.ARCANE_STONE.get()))
         .save(consumer, Thaumcraft.location("shaped/arcane_stone_stairs"));
      this.shaped((ItemLike)ModItems.ARCANE_STONE_SLAB.get(), 6)
         .pattern("KKK")
         .define('K', (ItemLike)ModItems.ARCANE_BRICKS.get())
         .unlockedBy("has_arcane_stone", has((ItemLike)ModItems.ARCANE_STONE.get()))
         .save(consumer, Thaumcraft.location("shaped/arcane_stone_slab"));
   }

   private ShapelessRecipeBuilder shapeless(ItemLike itemLike) {
      return this.shapeless(itemLike, 1);
   }

   private ShapelessRecipeBuilder shapeless(ItemLike itemLike, int count) {
      return ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, itemLike, count);
   }

   private ShapedRecipeBuilder shaped(ItemLike itemLike) {
      return this.shaped(itemLike, 1);
   }

   private ShapedRecipeBuilder shaped(ItemLike itemLike, int count) {
      return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, itemLike, count);
   }

   private void shapelessCluster(ItemLike result, ItemLike input, RecipeOutput consumer) {
      ShapelessRecipeBuilder builder = this.shapeless(result).requires(input).requires(input).requires(input).requires(input).requires(input).requires(input);
      builder.unlockedBy("has_shards", has(ModItemTags.SHARDS))
         .save(consumer, Thaumcraft.location("shapeless/" + BuiltInRegistries.ITEM.getKey(result.asItem()).getPath()));
   }

   private ItemLike dye(DyeColor color) {
      return switch (color) {
         case WHITE -> Items.WHITE_DYE;
         case ORANGE -> Items.ORANGE_DYE;
         case MAGENTA -> Items.MAGENTA_DYE;
         case LIGHT_BLUE -> Items.LIGHT_BLUE_DYE;
         case YELLOW -> Items.YELLOW_DYE;
         case LIME -> Items.LIME_DYE;
         case PINK -> Items.PINK_DYE;
         case GRAY -> Items.GRAY_DYE;
         case LIGHT_GRAY -> Items.LIGHT_GRAY_DYE;
         case CYAN -> Items.CYAN_DYE;
         case PURPLE -> Items.PURPLE_DYE;
         case BLUE -> Items.BLUE_DYE;
         case BROWN -> Items.BROWN_DYE;
         case GREEN -> Items.GREEN_DYE;
         case RED -> Items.RED_DYE;
         case BLACK -> Items.BLACK_DYE;
         default -> throw new MatchException(null, null);
      };
   }

   private void jarLabelRecipes(RecipeOutput consumer) {
      Ingredient blankJarLabel = DifferenceIngredient.of(Ingredient.of(new ItemLike[]{(ItemLike)ModItems.JAR_LABEL.get()}), this.allLabeledJarLabels());

      for (Aspect aspect : this.sortedAspects()) {
         ItemStack label = this.jarLabelStack(aspect);
         ShapelessNBTRecipeBuilder.shapeless(label)
            .requires(blankJarLabel)
            .requires(DataComponentIngredient.of(true, this.phialStack(aspect)))
            .save(consumer, Thaumcraft.location("shapeless/jar_label_" + aspect.getTag()));
      }

      this.shapeless((ItemLike)ModItems.JAR_LABEL.get())
         .requires(this.allLabeledJarLabels())
         .unlockedBy("has_jar_label", has((ItemLike)ModItems.JAR_LABEL.get()))
         .save(consumer, Thaumcraft.location("shapeless/jar_label_null"));
   }

   private Ingredient allLabeledJarLabels() {
      List<Ingredient> ingredients = new ArrayList<>();

      for (Aspect aspect : this.sortedAspects()) {
         ingredients.add(DataComponentIngredient.of(true, this.jarLabelStack(aspect)));
      }

      return CompoundIngredient.of(ingredients.toArray(Ingredient[]::new));
   }

   private ItemStack phialStack(Aspect aspect) {
      ItemStack stack = new ItemStack((ItemLike)ModItems.PHIAL_ESSENCE.get());
      ((IEssenceContainerItem)stack.getItem()).setAspects(stack, new AspectList().add(aspect, 8));
      return stack;
   }

   private ItemStack jarLabelStack(Aspect aspect) {
      ItemStack stack = new ItemStack((ItemLike)ModItems.JAR_LABEL.get());
      ((IEssenceContainerItem)stack.getItem()).setAspects(stack, new AspectList().add(aspect, 1));
      return stack;
   }

   private List<Aspect> sortedAspects() {
      List<Aspect> aspects = new ArrayList<>(Aspect.aspects.values());
      aspects.removeIf(aspect -> aspect == null);
      aspects.sort(Comparator.comparing(Aspect::getTag));
      return aspects;
   }
}
