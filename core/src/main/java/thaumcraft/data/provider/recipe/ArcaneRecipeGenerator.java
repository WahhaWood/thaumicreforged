package thaumcraft.data.provider.recipe;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.data.builder.ShapedArcaneRecipeBuilder;
import thaumcraft.data.builder.ShappelesArcaneRecipeBuilder;
import thaumcraft.init.registry.ModItemTags;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.wands.ItemWandCap;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.ItemWandRod;

public class ArcaneRecipeGenerator extends RecipeProvider {
   public ArcaneRecipeGenerator(PackOutput output, CompletableFuture<Provider> registries) {
      super(output, registries);
   }

   public void generate(@NotNull RecipeOutput consumer) {
      this.buildRecipes(consumer);
   }

   protected void buildRecipes(@NotNull RecipeOutput consumer) {
      for (DyeColor color : DyeColor.values()) {
         this.banner(color, consumer);
      }

      this.shaped("basic_artiface", (ItemLike)ModItems.PRIMAL_CHARM.get())
         .aspect(Aspect.EARTH, 25)
         .aspect(Aspect.FIRE, 25)
         .aspect(Aspect.AIR, 25)
         .aspect(Aspect.WATER, 25)
         .aspect(Aspect.ORDER, 25)
         .aspect(Aspect.ENTROPY, 25)
         .pattern("123")
         .pattern("ISI")
         .pattern("456")
         .define('S', (ItemLike)ModItems.BALANCED_SHARD.get())
         .define('I', Items.GOLD_INGOT)
         .define('1', (ItemLike)ModItems.AIR_SHARD.get())
         .define('2', (ItemLike)ModItems.FIRE_SHARD.get())
         .define('3', (ItemLike)ModItems.WATER_SHARD.get())
         .define('4', (ItemLike)ModItems.EARTH_SHARD.get())
         .define('5', (ItemLike)ModItems.ORDER_SHARD.get())
         .define('6', (ItemLike)ModItems.ENTROPY_SHARD.get())
         .save(consumer, Thaumcraft.location("arcane/primal_charm"));
      this.shaped("warded_arcana", (ItemLike)ModItems.ARCANE_DOOR.get())
         .aspect(Aspect.WATER, 20)
         .aspect(Aspect.ORDER, 10)
         .aspect(Aspect.EARTH, 10)
         .aspect(Aspect.FIRE, 5)
         .pattern("TDT")
         .pattern("DBD")
         .pattern("TDT")
         .define('T', ModItemTags.THAUMIUM_INGOT)
         .define('B', (ItemLike)ModItems.ZOMBIE_BRAIN.get())
         .define('D', (ItemLike)ModItems.GREATWOOD_PLANKS.get())
         .save(consumer, Thaumcraft.location("arcane/arcane_door"));
      this.shaped("warded_arcana", (ItemLike)ModItems.WARDED_GLASS.get(), 8)
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.ORDER, 10)
         .aspect(Aspect.EARTH, 5)
         .aspect(Aspect.FIRE, 5)
         .pattern("GGG")
         .pattern("WBW")
         .pattern("GGG")
         .define('B', (ItemLike)ModItems.ZOMBIE_BRAIN.get())
         .define('G', net.neoforged.neoforge.common.Tags.Items.GLASS_BLOCKS)
         .define('W', (ItemLike)ModItems.GREATWOOD_PLANKS.get())
         .save(consumer, Thaumcraft.location("arcane/warded_glass"));
      this.shaped("warded_arcana", (ItemLike)ModItems.IRON_KEY.get(), 2)
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.ORDER, 5)
         .pattern("NNI")
         .pattern("N  ")
         .define('I', Items.IRON_INGOT)
         .define('N', Items.IRON_NUGGET)
         .save(consumer, Thaumcraft.location("arcane/iron_key"));
      this.shaped("warded_arcana", (ItemLike)ModItems.GOLD_KEY.get(), 2)
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.ORDER, 5)
         .pattern("NNI")
         .pattern("N  ")
         .define('I', Items.GOLD_INGOT)
         .define('N', Items.GOLD_NUGGET)
         .save(consumer, Thaumcraft.location("arcane/gold_key"));
      this.shaped("warded_arcana", (ItemLike)ModItems.ARCANE_PRESSURE_PLATE.get())
         .aspect(Aspect.WATER, 20)
         .aspect(Aspect.ORDER, 10)
         .aspect(Aspect.FIRE, 5)
         .aspect(Aspect.EARTH, 10)
         .pattern(" B ")
         .pattern("TDT")
         .define('T', ModItemTags.THAUMIUM_INGOT)
         .define('B', (ItemLike)ModItems.ZOMBIE_BRAIN.get())
         .define('D', (ItemLike)ModItems.GREATWOOD_PLANKS.get())
         .save(consumer, Thaumcraft.location("arcane/arcane_pressure_plate"));
      this.shaped("flux_scrubber", (ItemLike)ModItems.FLUX_SCRUBBER.get())
         .aspect(Aspect.WATER, 16)
         .aspect(Aspect.ORDER, 16)
         .aspect(Aspect.AIR, 8)
         .pattern(" B ")
         .pattern("GOG")
         .pattern("STS")
         .define('B', (ItemLike)ModItems.ARCANE_BELLOWS.get())
         .define('G', Items.IRON_BARS)
         .define('T', (ItemLike)ModItems.ESSENTIA_TUBE.get())
         .define('O', (ItemLike)ModItems.VIS_FILTER.get())
         .define('S', (ItemLike)ModItems.ARCANE_BRICKS.get())
         .save(consumer, Thaumcraft.location("arcane/flux_scrubber"));
      this.shaped("node_stabilizer", (ItemLike)ModItems.NODE_STABILIZER.get())
         .aspect(Aspect.WATER, 32)
         .aspect(Aspect.EARTH, 32)
         .aspect(Aspect.ORDER, 32)
         .pattern(" G ")
         .pattern("QPQ")
         .pattern("SNS")
         .define('S', (ItemLike)ModItems.ARCANE_BRICKS.get())
         .define('G', Items.GOLD_INGOT)
         .define('P', Blocks.PISTON)
         .define('Q', Blocks.QUARTZ_BLOCK)
         .define('N', (ItemLike)ModItems.NITOR.get())
         .save(consumer, Thaumcraft.location("arcane/node_stabilizer"));
      this.shaped("vis_power", (ItemLike)ModItems.NODE_TRANSDUCER.get())
         .aspect(Aspect.FIRE, 32)
         .aspect(Aspect.AIR, 32)
         .aspect(Aspect.ENTROPY, 32)
         .pattern("RCR")
         .pattern("ISI")
         .pattern("RAR")
         .define('S', (ItemLike)ModItems.NODE_STABILIZER.get())
         .define('C', Items.COMPARATOR)
         .define('I', Items.IRON_INGOT)
         .define('R', Items.REDSTONE_BLOCK)
         .define('A', (ItemLike)ModItems.NITOR.get())
         .save(consumer, Thaumcraft.location("arcane/node_transducer"));
      this.shaped("vis_power", (ItemLike)ModItems.VIS_RELAY.get(), 2)
         .aspect(Aspect.FIRE, 8)
         .aspect(Aspect.ORDER, 8)
         .pattern(" I ")
         .pattern("ISI")
         .pattern(" I ")
         .define('I', Items.IRON_INGOT)
         .define('S', (ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("arcane/vis_relay"));
      this.shaped("vis_charge_relay", (ItemLike)ModItems.VIS_CHARGE_RELAY.get())
         .aspect(Aspect.FIRE, 16)
         .aspect(Aspect.ORDER, 16)
         .aspect(Aspect.AIR, 16)
         .pattern(" R ")
         .pattern("W W")
         .pattern("I I")
         .define('I', Items.IRON_INGOT)
         .define('R', (ItemLike)ModItems.VIS_RELAY.get())
         .define('W', (ItemLike)ModItems.GREATWOOD_WAND_ROD.get())
         .save(consumer, Thaumcraft.location("arcane/vis_charge_relay"));
      this.shaped("focal_manipulator", (ItemLike)ModItems.FOCAL_MANIPULATOR.get())
         .aspect(Aspect.FIRE, 32)
         .aspect(Aspect.AIR, 32)
         .aspect(Aspect.ENTROPY, 32)
         .aspect(Aspect.EARTH, 32)
         .aspect(Aspect.WATER, 32)
         .aspect(Aspect.ORDER, 32)
         .pattern("IQI")
         .pattern("SPS")
         .pattern("GTG")
         .define('Q', (ItemLike)ModItems.ARCANE_STONE_STAIRS.get())
         .define('S', (ItemLike)ModItems.ARCANE_STONE.get())
         .define('T', (ItemLike)ModItems.TABLE.get())
         .define('I', Items.IRON_INGOT)
         .define('G', Items.GOLD_INGOT)
         .define('P', (ItemLike)ModItems.PRIMAL_CHARM.get())
         .save(consumer, Thaumcraft.location("arcane/focal_manipulator"));
      this.shaped("golem_fetter", (ItemLike)ModItems.GOLEM_FETTER.get())
         .aspect(Aspect.EARTH, 5)
         .aspect(Aspect.ORDER, 5)
         .pattern("SSS")
         .pattern("IRI")
         .pattern("BBB")
         .define('S', (ItemLike)ModItems.ARCANE_STONE.get())
         .define('I', Items.IRON_INGOT)
         .define('B', (ItemLike)ModItems.ARCANE_BRICKS.get())
         .define('R', Items.REDSTONE_BLOCK)
         .save(consumer, Thaumcraft.location("arcane/golem_fetter"));
      this.shaped("arcane_stone", (ItemLike)ModItems.ARCANE_STONE.get(), 9)
         .aspect(Aspect.EARTH, 1)
         .aspect(Aspect.FIRE, 1)
         .pattern("SSS")
         .pattern("SCS")
         .pattern("SSS")
         .define('S', net.neoforged.neoforge.common.Tags.Items.STONES)
         .define('C', ModItemTags.SHARDS)
         .save(consumer, Thaumcraft.location("arcane/arcane_stone_block"));
      this.shaped("pave_travel", (ItemLike)ModItems.TRAVEL_STONE.get(), 4)
         .aspect(Aspect.EARTH, 10)
         .aspect(Aspect.AIR, 10)
         .pattern("SAS")
         .pattern("SBS")
         .define('S', (ItemLike)ModItems.ARCANE_BRICKS.get())
         .define('A', (ItemLike)ModItems.AIR_SHARD.get())
         .define('B', (ItemLike)ModItems.EARTH_SHARD.get())
         .save(consumer, Thaumcraft.location("arcane/travel_stone"));
      this.shaped("arcane_lamp", (ItemLike)ModItems.ARCANE_LAMP.get())
         .aspect(Aspect.FIRE, 8)
         .aspect(Aspect.AIR, 8)
         .aspect(Aspect.WATER, 4)
         .aspect(Aspect.ENTROPY, 4)
         .pattern(" S ")
         .pattern("IAI")
         .pattern(" N ")
         .define('A', (ItemLike)ModItems.AMBER_BLOCK.get())
         .define('S', Blocks.DAYLIGHT_DETECTOR)
         .define('N', (ItemLike)ModItems.NITOR.get())
         .define('I', Items.IRON_INGOT)
         .save(consumer, Thaumcraft.location("arcane/arcane_lamp"));
      this.shaped("arcane_spa", (ItemLike)ModItems.ARCANE_SPA.get())
         .aspect(Aspect.WATER, 16)
         .aspect(Aspect.ORDER, 8)
         .aspect(Aspect.EARTH, 4)
         .pattern("QIQ")
         .pattern("SJS")
         .pattern("SPS")
         .define('P', Blocks.PISTON)
         .define('J', (ItemLike)ModItems.JAR.get())
         .define('S', (ItemLike)ModItems.ARCANE_STONE.get())
         .define('Q', Blocks.QUARTZ_BLOCK)
         .define('I', Blocks.IRON_BARS)
         .save(consumer, Thaumcraft.location("arcane/arcane_spa"));
      this.shaped("pave_ward", (ItemLike)ModItems.WARDING_STONE.get(), 4)
         .aspect(Aspect.FIRE, 10)
         .aspect(Aspect.ORDER, 10)
         .pattern("SAS")
         .pattern("SBS")
         .define('S', (ItemLike)ModItems.ARCANE_BRICKS.get())
         .define('A', (ItemLike)ModItems.FIRE_SHARD.get())
         .define('B', (ItemLike)ModItems.ORDER_SHARD.get())
         .save(consumer, Thaumcraft.location("arcane/warding_stone"));
      this.shaped("levitator", (ItemLike)ModItems.LIFTER.get())
         .aspect(Aspect.AIR, 10)
         .aspect(Aspect.EARTH, 5)
         .pattern("WEW")
         .pattern("BNB")
         .pattern("WAW")
         .define('W', (ItemLike)ModItems.GREATWOOD_PLANKS.get())
         .define('E', (ItemLike)ModItems.EARTH_SHARD.get())
         .define('A', (ItemLike)ModItems.AIR_SHARD.get())
         .define('N', (ItemLike)ModItems.NITOR.get())
         .define('B', Items.IRON_INGOT)
         .save(consumer, Thaumcraft.location("arcane/lifter"));
      this.shaped("arcane_ear", (ItemLike)ModItems.ARCANE_SENSOR.get())
         .aspect(Aspect.AIR, 10)
         .aspect(Aspect.ORDER, 10)
         .pattern("GIG")
         .pattern("GBG")
         .pattern("WRW")
         .define('W', (ItemLike)ModItems.GREATWOOD_PLANKS.get())
         .define('R', Items.REDSTONE)
         .define('I', Items.IRON_INGOT)
         .define('G', Items.GOLD_INGOT)
         .define('B', (ItemLike)ModItems.ZOMBIE_BRAIN.get())
         .save(consumer, Thaumcraft.location("arcane/arcane_sensor"));
      this.shapeless("basic_artiface", (ItemLike)ModItems.MIRROR_GLASS.get())
         .aspect(Aspect.FIRE, 10)
         .aspect(Aspect.EARTH, 10)
         .requires((ItemLike)ModItems.QUICKSILVER.get())
         .requires(net.neoforged.neoforge.common.Tags.Items.GLASS_PANES)
         .save(consumer, Thaumcraft.location("arcane/mirror_glass"));
      this.shaped("bone_bow", (ItemLike)ModItems.BONE_BOW.get())
         .aspect(Aspect.AIR, 16)
         .aspect(Aspect.ENTROPY, 32)
         .pattern("SB ")
         .pattern("SEB")
         .pattern("SB ")
         .define('E', (ItemLike)ModItems.ENTROPY_SHARD.get())
         .define('B', Items.BONE)
         .define('S', Items.STRING)
         .save(consumer, Thaumcraft.location("arcane/bone_bow"));
      this.primalArrow((ItemLike)ModItems.ARROW_AIR.get(), Aspect.AIR, (ItemLike)ModItems.AIR_SHARD.get(), "air", consumer);
      this.primalArrow((ItemLike)ModItems.ARROW_FIRE.get(), Aspect.FIRE, (ItemLike)ModItems.FIRE_SHARD.get(), "fire", consumer);
      this.primalArrow((ItemLike)ModItems.ARROW_WATER.get(), Aspect.WATER, (ItemLike)ModItems.WATER_SHARD.get(), "water", consumer);
      this.primalArrow((ItemLike)ModItems.ARROW_EARTH.get(), Aspect.EARTH, (ItemLike)ModItems.EARTH_SHARD.get(), "earth", consumer);
      this.primalArrow((ItemLike)ModItems.ARROW_ORDER.get(), Aspect.ORDER, (ItemLike)ModItems.ORDER_SHARD.get(), "order", consumer);
      this.primalArrow((ItemLike)ModItems.ARROW_ENTROPY.get(), Aspect.ENTROPY, (ItemLike)ModItems.ENTROPY_SHARD.get(), "entropy", consumer);
      this.shaped("infusion", (ItemLike)ModItems.RUNIC_MATRIX.get())
         .aspect(Aspect.ORDER, 40)
         .pattern("SBS")
         .pattern("BEB")
         .pattern("SBS")
         .define('S', (ItemLike)ModItems.ARCANE_STONE.get())
         .define('E', Items.ENDER_PEARL)
         .define('B', ModItemTags.SHARDS)
         .save(consumer, Thaumcraft.location("arcane/runix_matrix"));
      this.shaped("infusion", (ItemLike)ModItems.ARCANE_PEDESTAL.get(), 2)
         .aspect(Aspect.AIR, 5)
         .pattern("SSS")
         .pattern(" S ")
         .pattern("SSS")
         .define('S', (ItemLike)ModItems.ARCANE_STONE.get())
         .save(consumer, Thaumcraft.location("arcane/arcane_pedestal"));
      this.shaped("distilessence", (ItemLike)ModItems.JAR.get())
         .aspect(Aspect.WATER, 1)
         .pattern("GWG")
         .pattern("G G")
         .pattern("GGG")
         .define('W', ItemTags.WOODEN_SLABS)
         .define('G', net.neoforged.neoforge.common.Tags.Items.GLASS_PANES)
         .save(consumer, Thaumcraft.location("arcane/jar"));
      this.shaped("void_jar", (ItemLike)ModItems.VOID_JAR.get())
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.ENTROPY, 15)
         .pattern("O")
         .pattern("J")
         .pattern("P")
         .define('O', Blocks.OBSIDIAN)
         .define('P', Items.BLAZE_POWDER)
         .define('J', (ItemLike)ModItems.JAR.get())
         .save(consumer, Thaumcraft.location("arcane/void_jar"));
      this.sceptre("sceptre_iron_wood", (ItemWandCap)ModItems.IRON_WAND_CAP.get(), Items.STICK, (ItemWandRod)ModItems.WOOD_WAND_ROD.get(), consumer);
      this.sceptre(
         "sceptre_gold_greatwood",
         (ItemWandCap)ModItems.GOLD_WAND_CAP.get(),
         (ItemLike)ModItems.GREATWOOD_WAND_ROD.get(),
         (ItemWandRod)ModItems.GREATWOOD_WAND_ROD.get(),
         consumer
      );
      this.sceptre(
         "sceptre_thaumium_silverwood",
         (ItemWandCap)ModItems.THAUMIUM_WAND_CAP.get(),
         (ItemLike)ModItems.SILVERWOOD_WAND_ROD.get(),
         (ItemWandRod)ModItems.SILVERWOOD_WAND_ROD.get(),
         consumer
      );
      ItemWandCap wandCap = (ItemWandCap)ModItems.GOLD_WAND_CAP.get();
      this.shaped("gold_cap", wandCap)
         .aspect(Aspect.ORDER, wandCap.getCraftCost())
         .aspect(Aspect.FIRE, wandCap.getCraftCost())
         .aspect(Aspect.AIR, wandCap.getCraftCost())
         .pattern("NNN")
         .pattern("N N")
         .define('N', Items.GOLD_NUGGET)
         .save(consumer, Thaumcraft.location("arcane/gold_wand_cap"));
      wandCap = (ItemWandCap)ModItems.COPPER_WAND_CAP.get();
      this.shaped("copper_cap", (ItemLike)ModItems.COPPER_WAND_CAP.get())
         .aspect(Aspect.ORDER, wandCap.getCraftCost())
         .aspect(Aspect.FIRE, wandCap.getCraftCost())
         .aspect(Aspect.AIR, wandCap.getCraftCost())
         .pattern("NNN")
         .pattern("N N")
         .define('N', ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "nuggets/copper")))
         .save(consumer, Thaumcraft.location("arcane/copper_wand_cap"));
      wandCap = (ItemWandCap)ModItems.SILVER_WAND_CAP.get();
      this.shaped("silver_cap", (ItemLike)ModItems.SILVER_INERT_WAND_CAP.get())
         .aspect(Aspect.ORDER, wandCap.getCraftCost())
         .aspect(Aspect.FIRE, wandCap.getCraftCost())
         .aspect(Aspect.AIR, wandCap.getCraftCost())
         .pattern("NNN")
         .pattern("N N")
         .define('N', ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "nuggets/silver")))
         .save(consumer, Thaumcraft.location("arcane/silver_wand_cap"));
      wandCap = (ItemWandCap)ModItems.THAUMIUM_WAND_CAP.get();
      this.shaped("thaumium_cap", (ItemLike)ModItems.THAUMIUM_INERT_WAND_CAP.get())
         .aspect(Aspect.ORDER, wandCap.getCraftCost())
         .aspect(Aspect.FIRE, wandCap.getCraftCost())
         .aspect(Aspect.AIR, wandCap.getCraftCost())
         .pattern("NNN")
         .pattern("N N")
         .define('N', ModItemTags.THAUMIUM_NUGGET)
         .save(consumer, Thaumcraft.location("arcane/thaumium_inert_wand_cap"));
      wandCap = (ItemWandCap)ModItems.VOID_WAND_CAP.get();
      this.shaped("void_cap", (ItemLike)ModItems.VOID_INERT_WAND_CAP.get())
         .aspect(Aspect.ENTROPY, wandCap.getCraftCost() * 3)
         .aspect(Aspect.ORDER, wandCap.getCraftCost() * 3)
         .aspect(Aspect.FIRE, wandCap.getCraftCost() * 2)
         .aspect(Aspect.AIR, wandCap.getCraftCost() * 2)
         .pattern("NNN")
         .pattern("N N")
         .define('N', ModItemTags.VOID_NUGGET)
         .save(consumer, Thaumcraft.location("arcane/void_inert_wand_cap"));
      ItemWandRod wandRod = (ItemWandRod)ModItems.GREATWOOD_WAND_ROD.get();
      this.shaped("greatwood_rod", wandRod)
         .aspect(Aspect.ENTROPY, wandRod.getCraftCost())
         .pattern(" G")
         .pattern("G ")
         .define('G', (ItemLike)ModItems.GREATWOOD_LOG.get())
         .save(consumer, Thaumcraft.location("arcane/greatwood_wand_rod"));
      wandRod = (ItemWandRod)ModItems.GREATWOOD_STAFF_ROD.get();
      this.shaped("greatwood_staff_rod", wandRod)
         .aspect(Aspect.ORDER, wandRod.getCraftCost())
         .pattern("  S")
         .pattern(" G ")
         .pattern("G  ")
         .define('S', (ItemLike)ModItems.PRIMAL_CHARM.get())
         .define('G', (ItemLike)ModItems.GREATWOOD_WAND_ROD.get())
         .save(consumer, Thaumcraft.location("arcane/greatwood_staff_rod"));
      wandRod = (ItemWandRod)ModItems.OBSIDIAN_STAFF_ROD.get();
      this.shaped("obsidian_staff_rod", wandRod)
         .aspect(Aspect.ORDER, wandRod.getCraftCost())
         .pattern("  S")
         .pattern(" G ")
         .pattern("G  ")
         .define('S', (ItemLike)ModItems.PRIMAL_CHARM.get())
         .define('G', (ItemLike)ModItems.OBSIDIAN_STAFF_ROD.get())
         .save(consumer, Thaumcraft.location("arcane/obsidian_staff_rod"));
      wandRod = (ItemWandRod)ModItems.SILVERWOOD_STAFF_ROD.get();
      this.shaped("silverwood_staff_rod", wandRod)
         .aspect(Aspect.ORDER, wandRod.getCraftCost())
         .pattern("  S")
         .pattern(" G ")
         .pattern("G  ")
         .define('S', (ItemLike)ModItems.PRIMAL_CHARM.get())
         .define('G', (ItemLike)ModItems.SILVERWOOD_WAND_ROD.get())
         .save(consumer, Thaumcraft.location("arcane/silverwood_staff_rod"));
      wandRod = (ItemWandRod)ModItems.ICE_STAFF_ROD.get();
      this.shaped("ice_staff_rod", wandRod)
         .aspect(Aspect.ORDER, wandRod.getCraftCost())
         .pattern("  S")
         .pattern(" G ")
         .pattern("G  ")
         .define('S', (ItemLike)ModItems.PRIMAL_CHARM.get())
         .define('G', (ItemLike)ModItems.ICE_WAND_ROD.get())
         .save(consumer, Thaumcraft.location("arcane/ice_staff_rod"));
      wandRod = (ItemWandRod)ModItems.QUARTZ_STAFF_ROD.get();
      this.shaped("quartz_staff_rod", wandRod)
         .aspect(Aspect.ORDER, wandRod.getCraftCost())
         .pattern("  S")
         .pattern(" G ")
         .pattern("G  ")
         .define('S', (ItemLike)ModItems.PRIMAL_CHARM.get())
         .define('G', (ItemLike)ModItems.QUARTZ_WAND_ROD.get())
         .save(consumer, Thaumcraft.location("arcane/quartz_staff_rod"));
      wandRod = (ItemWandRod)ModItems.REED_STAFF_ROD.get();
      this.shaped("reed_staff_rod", wandRod)
         .aspect(Aspect.ORDER, wandRod.getCraftCost())
         .pattern("  S")
         .pattern(" G ")
         .pattern("G  ")
         .define('S', (ItemLike)ModItems.PRIMAL_CHARM.get())
         .define('G', (ItemLike)ModItems.REED_WAND_ROD.get())
         .save(consumer, Thaumcraft.location("arcane/reed_staff_rod"));
      wandRod = (ItemWandRod)ModItems.BLAZE_STAFF_ROD.get();
      this.shaped("blaze_staff_rod", wandRod)
         .aspect(Aspect.ORDER, wandRod.getCraftCost())
         .pattern("  S")
         .pattern(" G ")
         .pattern("G  ")
         .define('S', (ItemLike)ModItems.PRIMAL_CHARM.get())
         .define('G', (ItemLike)ModItems.BLAZE_WAND_ROD.get())
         .save(consumer, Thaumcraft.location("arcane/blaze_staff_rod"));
      wandRod = (ItemWandRod)ModItems.BONE_STAFF_ROD.get();
      this.shaped("bone_staff_rod", wandRod)
         .aspect(Aspect.ORDER, wandRod.getCraftCost())
         .pattern("  S")
         .pattern(" G ")
         .pattern("G  ")
         .define('S', (ItemLike)ModItems.PRIMAL_CHARM.get())
         .define('G', (ItemLike)ModItems.BONE_WAND_ROD.get())
         .save(consumer, Thaumcraft.location("arcane/bone_staff_rod"));
      this.shaped("focus_fire", (ItemLike)ModItems.FOCUS_FIRE.get())
         .aspect(Aspect.FIRE, 20)
         .aspect(Aspect.ENTROPY, 10)
         .pattern("CQC")
         .pattern("Q#Q")
         .pattern("CQC")
         .define('#', Items.FIRE_CHARGE)
         .define('Q', Items.QUARTZ)
         .define('C', (ItemLike)ModItems.FIRE_SHARD.get())
         .save(consumer, Thaumcraft.location("arcane/focus_fire"));
      this.shaped("focus_frost", (ItemLike)ModItems.FOCUS_FROST.get())
         .aspect(Aspect.WATER, 10)
         .aspect(Aspect.ORDER, 10)
         .aspect(Aspect.ENTROPY, 10)
         .pattern("CQC")
         .pattern("Q#Q")
         .pattern("CQC")
         .define('#', Items.DIAMOND)
         .define('Q', Items.QUARTZ)
         .define('C', (ItemLike)ModItems.WATER_SHARD.get())
         .save(consumer, Thaumcraft.location("arcane/focus_frost"));
      this.shaped("focus_shock", (ItemLike)ModItems.FOCUS_SHOCK.get())
         .aspect(Aspect.AIR, 10)
         .aspect(Aspect.ORDER, 10)
         .aspect(Aspect.ENTROPY, 10)
         .pattern("CQC")
         .pattern("Q#Q")
         .pattern("CQC")
         .define('#', Items.POTATO)
         .define('Q', Items.QUARTZ)
         .define('C', (ItemLike)ModItems.AIR_SHARD.get())
         .save(consumer, Thaumcraft.location("arcane/focus_shock"));
      this.shaped("focus_trade", (ItemLike)ModItems.FOCUS_TRADE.get())
         .aspect(Aspect.ORDER, 15)
         .aspect(Aspect.ENTROPY, 15)
         .aspect(Aspect.EARTH, 10)
         .pattern("CQE")
         .pattern("Q#Q")
         .pattern("CQE")
         .define('#', (ItemLike)ModItems.QUICKSILVER.get())
         .define('Q', Items.QUARTZ)
         .define('C', (ItemLike)ModItems.BALANCED_SHARD.get())
         .define('E', (ItemLike)ModItems.BALANCED_SHARD.get())
         .save(consumer, Thaumcraft.location("arcane/focus_trade"));
      // Пех — торговец: изумруд в центре, шарды земли и энтропии по углам.
      this.shaped("focus_pech", (ItemLike)ModItems.FOCUS_PECH.get())
         .aspect(Aspect.ENTROPY, 15)
         .aspect(Aspect.EARTH, 15)
         .pattern("CQE")
         .pattern("Q#Q")
         .pattern("CQE")
         .define('#', Items.EMERALD)
         .define('Q', Items.QUARTZ)
         .define('C', (ItemLike)ModItems.EARTH_SHARD.get())
         .define('E', (ItemLike)ModItems.ENTROPY_SHARD.get())
         .save(consumer, Thaumcraft.location("arcane/focus_pech"));
      this.shaped("focus_excavation", (ItemLike)ModItems.FOCUS_EXCAVATION.get())
         .aspect(Aspect.EARTH, 20)
         .aspect(Aspect.ENTROPY, 5)
         .aspect(Aspect.ORDER, 5)
         .pattern("CQC")
         .pattern("Q#Q")
         .pattern("CQC")
         .define('#', net.neoforged.neoforge.common.Tags.Items.GEMS_EMERALD)
         .define('Q', Items.QUARTZ)
         .define('C', (ItemLike)ModItems.EARTH_SHARD.get())
         .save(consumer, Thaumcraft.location("arcane/focus_excavation"));
      this.shaped("focus_primal", (ItemLike)ModItems.FOCUS_PRIMAL.get())
         .aspect(Aspect.EARTH, 25)
         .aspect(Aspect.ENTROPY, 25)
         .aspect(Aspect.ORDER, 25)
         .aspect(Aspect.AIR, 25)
         .aspect(Aspect.FIRE, 25)
         .aspect(Aspect.WATER, 25)
         .pattern("CQC")
         .pattern("Q#Q")
         .pattern("CQC")
         .define('#', (ItemLike)ModItems.PRIMAL_CHARM.get())
         .define('Q', Items.QUARTZ)
         .define('C', Items.DIAMOND)
         .save(consumer, Thaumcraft.location("arcane/focus_primal"));
      this.shaped("focus_pouch", (ItemLike)ModItems.FOCUS_POUCH.get())
         .aspect(Aspect.EARTH, 10)
         .aspect(Aspect.ORDER, 10)
         .aspect(Aspect.ENTROPY, 10)
         .pattern("LGL")
         .pattern("LBL")
         .pattern("LLL")
         .define('B', (ItemLike)ModItems.CURIOS_BELT.get())
         .define('L', Items.LEATHER)
         .define('G', Items.GOLD_INGOT)
         .save(consumer, Thaumcraft.location("arcane/focus_pouch"));
      this.shaped("deconstructor", (ItemLike)ModItems.DECONSTRUCTION_TABLE.get())
         .aspect(Aspect.ENTROPY, 20)
         .pattern(" S ")
         .pattern("ATP")
         .define('T', (ItemLike)ModItems.TABLE.get())
         .define('S', (ItemLike)ModItems.THAUMOMETER.get())
         .define('P', Items.GOLDEN_PICKAXE)
         .define('A', Items.GOLDEN_AXE)
         .save(consumer, Thaumcraft.location("arcane/deconstruction_table"));
       // Фрагменты метеорита тёмного солнца: рецептов не было вообще, предметы
       // (с осмысленным применением из page.2) лежали только в креативе.
       this.shaped("distorted_fragment", (ItemLike)ModItems.DISTORTED_FRAGMENT.get())
          .aspect(Aspect.DARKNESS, 10)
          .aspect(Aspect.MAGIC, 10)
          .aspect(Aspect.TAINT, 5)
          .pattern("OGO")
          .pattern("GEG")
          .pattern("OGO")
          .define('O', Items.OBSIDIAN)
          .define('G', Items.GLASS)
          .define('E', Items.ENDER_PEARL)
          .save(consumer, Thaumcraft.location("arcane/distorted_fragment"));
       this.shaped("clean_fragment", (ItemLike)ModItems.CLEAN_FRAGMENT.get())
          .aspect(Aspect.MAGIC, 10)
          .aspect(Aspect.ORDER, 15)
          .pattern(" B ")
          .pattern("BDB")
          .pattern(" B ")
          .define('B', (ItemLike)ModItems.ETHEREAL_BLOOM.get())
          .define('D', (ItemLike)ModItems.DISTORTED_FRAGMENT.get())
          .save(consumer, Thaumcraft.location("arcane/clean_fragment"));
       this.shaped("volume_of_purification", (ItemLike)ModItems.VOLUME_OF_PURIFICATION.get())
          .aspect(Aspect.MAGIC, 10)
          .aspect(Aspect.ORDER, 20)
          .pattern(" G ")
          .pattern("BCB")
          .pattern(" G ")
          .define('B', Items.BOOK)
          .define('C', (ItemLike)ModItems.CLEAN_FRAGMENT.get())
          .define('G', Items.GLOWSTONE_DUST)
          .save(consumer, Thaumcraft.location("arcane/volume_of_purification"));
      this.shaped("arcane_bore", (ItemLike)ModItems.ARCANE_BORE_BASE.get())
         .aspect(Aspect.AIR, 10)
         .aspect(Aspect.ORDER, 10)
         .pattern("WIW")
         .pattern("IDI")
         .pattern("WIW")
         .define('W', (ItemLike)ModItems.GREATWOOD_PLANKS.get())
         .define('I', Items.IRON_INGOT)
         .define('D', Blocks.DISPENSER)
         .save(consumer, Thaumcraft.location("arcane/arcane_bore_base"));
      this.shaped("enchfabric", (ItemLike)ModItems.CLOTH.get())
         .aspect(Aspect.AIR, 1)
         .aspect(Aspect.EARTH, 1)
         .aspect(Aspect.FIRE, 1)
         .aspect(Aspect.WATER, 1)
         .aspect(Aspect.ORDER, 1)
         .aspect(Aspect.ENTROPY, 1)
         .pattern(" S ")
         .pattern("SCS")
         .pattern(" S ")
         .define('S', net.neoforged.neoforge.common.Tags.Items.STRINGS)
         .define('C', ItemTags.WOOL)
         .save(consumer, Thaumcraft.location("arcane/cloth"));
      this.shaped("enchfabric", (ItemLike)ModItems.CLOTH_CHEST.get())
         .aspect(Aspect.AIR, 5)
         .pattern("I I")
         .pattern("III")
         .pattern("III")
         .define('I', (ItemLike)ModItems.CLOTH.get())
         .save(consumer, Thaumcraft.location("arcane/cloth_chest"));
      this.shaped("enchfabric", (ItemLike)ModItems.CLOTH_LEGGINGS.get())
         .aspect(Aspect.WATER, 5)
         .pattern("III")
         .pattern("I I")
         .pattern("I I")
         .define('I', (ItemLike)ModItems.CLOTH.get())
         .save(consumer, Thaumcraft.location("arcane/cloth_leggings"));
      this.shaped("enchfabric", (ItemLike)ModItems.CLOTH_BOOTS.get())
         .aspect(Aspect.EARTH, 3)
         .pattern("I I")
         .pattern("I I")
         .define('I', (ItemLike)ModItems.CLOTH.get())
         .save(consumer, Thaumcraft.location("arcane/cloth_boots"));
      this.shaped("goggles", (ItemLike)ModItems.GOGGLES_REVEALING.get())
         .aspect(Aspect.AIR, 5)
         .aspect(Aspect.FIRE, 5)
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.EARTH, 5)
         .aspect(Aspect.ENTROPY, 3)
         .aspect(Aspect.ORDER, 3)
         .pattern("LGL")
         .pattern("L L")
         .pattern("TGT")
         .define('T', (ItemLike)ModItems.THAUMOMETER.get())
         .define('G', Items.GOLD_INGOT)
         .define('L', Items.LEATHER)
         .save(consumer, Thaumcraft.location("arcane/goggles_revealing"));
      this.shaped("hungry_chest", (ItemLike)ModItems.HUNGRY_CHEST.get())
         .aspect(Aspect.AIR, 5)
         .aspect(Aspect.ORDER, 3)
         .aspect(Aspect.ENTROPY, 3)
         .pattern("WTW")
         .pattern("W W")
         .pattern("WWW")
         .define('W', ItemTags.PLANKS)
         .define('T', ItemTags.TRAPDOORS)
         .save(consumer, Thaumcraft.location("arcane/hungry_chest"));
      this.shaped("golem_bell", (ItemLike)ModItems.GOLEM_BELL.get())
         .aspect(Aspect.ORDER, 5)
         .pattern(" QQ")
         .pattern(" QQ")
         .pattern("S  ")
         .define('S', Items.STICK)
         .define('Q', Items.QUARTZ)
         .save(consumer, Thaumcraft.location("arcane/iron_bell"));
      this.shaped("golem_core_gather", (ItemLike)ModItems.GOLEM_CORE_BLANK.get())
         .aspect(Aspect.ORDER, 5)
         .aspect(Aspect.FIRE, 5)
         .pattern(" C ")
         .pattern("CNC")
         .pattern(" C ")
         .define('C', Items.BRICK)
         .define('N', (ItemLike)ModItems.NITOR.get())
         .save(consumer, Thaumcraft.location("arcane/golem_core_blank"));
      this.shaped("golem_upgrade_air", (ItemLike)ModItems.GOLEM_UPGRADE_AIR.get())
         .aspect(Aspect.AIR, 10)
         .pattern("NNN")
         .pattern("NCN")
         .pattern("NNN")
         .define('N', Items.GOLD_NUGGET)
         .define('C', (ItemLike)ModItems.AIR_SHARD.get())
         .save(consumer, Thaumcraft.location("arcane/golem_upgrade_air"));
      this.shaped("golem_upgrade_earth", (ItemLike)ModItems.GOLEM_UPGRADE_EARTH.get())
         .aspect(Aspect.EARTH, 10)
         .pattern("NNN")
         .pattern("NCN")
         .pattern("NNN")
         .define('N', Items.GOLD_NUGGET)
         .define('C', (ItemLike)ModItems.EARTH_SHARD.get())
         .save(consumer, Thaumcraft.location("arcane/golem_upgrade_earth"));
      this.shaped("golem_upgrade_fire", (ItemLike)ModItems.GOLEM_UPGRADE_FIRE.get())
         .aspect(Aspect.FIRE, 10)
         .pattern("NNN")
         .pattern("NCN")
         .pattern("NNN")
         .define('N', Items.GOLD_NUGGET)
         .define('C', (ItemLike)ModItems.FIRE_SHARD.get())
         .save(consumer, Thaumcraft.location("arcane/golem_upgrade_fire"));
      this.shaped("golem_upgrade_water", (ItemLike)ModItems.GOLEM_UPGRADE_WATER.get())
         .aspect(Aspect.WATER, 10)
         .pattern("NNN")
         .pattern("NCN")
         .pattern("NNN")
         .define('N', Items.GOLD_NUGGET)
         .define('C', (ItemLike)ModItems.WATER_SHARD.get())
         .save(consumer, Thaumcraft.location("arcane/golem_upgrade_water"));
      this.shaped("golem_upgrade_order", (ItemLike)ModItems.GOLEM_UPGRADE_ORDER.get())
         .aspect(Aspect.ORDER, 10)
         .pattern("NNN")
         .pattern("NCN")
         .pattern("NNN")
         .define('N', Items.GOLD_NUGGET)
         .define('C', (ItemLike)ModItems.ORDER_SHARD.get())
         .save(consumer, Thaumcraft.location("arcane/golem_upgrade_order"));
      this.shaped("golem_upgrade_entropy", (ItemLike)ModItems.GOLEM_UPGRADE_ENTROPY.get())
         .aspect(Aspect.ENTROPY, 10)
         .pattern("NNN")
         .pattern("NCN")
         .pattern("NNN")
         .define('N', Items.GOLD_NUGGET)
         .define('C', (ItemLike)ModItems.ENTROPY_SHARD.get())
         .save(consumer, Thaumcraft.location("arcane/golem_upgrade_entropy"));
      this.shaped("golem_decor_top_hat", (ItemLike)ModItems.GOLEM_DECOR_TOP_HAT.get())
         .aspect(Aspect.ORDER, 8)
         .aspect(Aspect.FIRE, 8)
         .pattern(" C ")
         .pattern(" G ")
         .pattern("CCC")
         .define('C', Blocks.BLACK_WOOL)
         .define('G', Items.GOLD_INGOT)
         .save(consumer, Thaumcraft.location("arcane/golem_decor_top_hat"));
      this.shaped("golem_decor_fez", (ItemLike)ModItems.GOLEM_DECOR_FEZ.get())
         .aspect(Aspect.WATER, 4)
         .aspect(Aspect.EARTH, 4)
         .pattern("CCS")
         .pattern("CCS")
         .pattern("  S")
         .define('C', Blocks.RED_WOOL)
         .define('S', net.neoforged.neoforge.common.Tags.Items.STRINGS)
         .save(consumer, Thaumcraft.location("arcane/golem_decor_fez"));
      this.shaped("golem_decor_bowtie", (ItemLike)ModItems.GOLEM_DECOR_BOWTIE.get())
         .aspect(Aspect.AIR, 4)
         .aspect(Aspect.ORDER, 4)
         .pattern("CSC")
         .pattern("C C")
         .define('C', Blocks.BLACK_WOOL)
         .define('S', net.neoforged.neoforge.common.Tags.Items.STRINGS)
         .save(consumer, Thaumcraft.location("arcane/golem_decor_bowtie"));
      this.shaped("golem_decor_googles", (ItemLike)ModItems.GOLEM_DECOR_GOGGLES.get())
         .aspect(Aspect.AIR, 4)
         .aspect(Aspect.WATER, 4)
         .pattern("GIG")
         .define('G', Blocks.GLASS)
         .define('I', Items.IRON_INGOT)
         .save(consumer, Thaumcraft.location("arcane/golem_decor_googles"));
      this.shaped("golem_decor_dart", (ItemLike)ModItems.GOLEM_DECOR_DART.get())
         .aspect(Aspect.AIR, 4)
         .aspect(Aspect.FIRE, 4)
         .pattern("AIA")
         .pattern("ADA")
         .pattern("AIA")
         .define('I', Items.IRON_INGOT)
         .define('D', Blocks.DISPENSER)
         .define('A', Items.ARROW)
         .save(consumer, Thaumcraft.location("arcane/golem_decor_dart"));
      this.shaped("golem_decor_visor", (ItemLike)ModItems.GOLEM_DECOR_VISOR.get())
         .aspect(Aspect.EARTH, 4)
         .aspect(Aspect.WATER, 4)
         .pattern("IHI")
         .define('I', Items.IRON_INGOT)
         .define('H', Items.IRON_HELMET)
         .save(consumer, Thaumcraft.location("arcane/golem_decor_visor"));
      this.shaped("golem_decor_armor", (ItemLike)ModItems.GOLEM_DECOR_ARMOR.get())
         .aspect(Aspect.EARTH, 8)
         .pattern("I I")
         .pattern("IAI")
         .define('I', Items.IRON_INGOT)
         .define('A', Items.IRON_CHESTPLATE)
         .save(consumer, Thaumcraft.location("arcane/golem_decor_armor"));
      this.shaped("golem_decor_mace", (ItemLike)ModItems.GOLEM_DECOR_MACE.get())
         .aspect(Aspect.EARTH, 4)
         .aspect(Aspect.FIRE, 4)
         .pattern("III")
         .pattern("III")
         .pattern(" I ")
         .define('I', Items.IRON_INGOT)
         .save(consumer, Thaumcraft.location("arcane/golem_decor_mace"));
      this.shaped("distilessence", (ItemLike)ModItems.VIS_FILTER.get())
         .aspect(Aspect.ORDER, 5)
         .aspect(Aspect.WATER, 5)
         .pattern("GWG")
         .define('G', Items.GOLD_INGOT)
         .define('W', (ItemLike)ModItems.SILVERWOOD_PLANKS.get())
         .save(consumer, Thaumcraft.location("arcane/vis_filter"));
      this.shaped("distilessence", (ItemLike)ModItems.ALCHEMY_FURNACE.get())
         .aspect(Aspect.FIRE, 5)
         .aspect(Aspect.WATER, 5)
         .pattern("SCS")
         .pattern("SFS")
         .pattern("SSS")
         .define('C', (ItemLike)ModItems.CRUCIBLE.get())
         .define('F', Blocks.FURNACE)
         .define('S', (ItemLike)ModItems.ARCANE_STONE.get())
         .save(consumer, Thaumcraft.location("arcane/alchemy_furnace"));
      this.shaped("gas_remover", (ItemLike)ModItems.GAS_REMOVAL.get())
         .aspect(Aspect.AIR, 2)
         .aspect(Aspect.ORDER, 2)
         .pattern("DDD")
         .pattern("T G")
         .pattern("QQQ")
         .define('D', (ItemLike)ModItems.DARK_QUARTZ.get())
         .define('T', (ItemLike)ModItems.GAS_SHADOW.get())
         .define('G', (ItemLike)ModItems.GAS_LIGHT.get())
         .define('Q', Items.QUARTZ)
         .save(consumer, Thaumcraft.location("arcane/gas_remover"));
      this.shaped("funnel", (ItemLike)ModItems.FUNNEL.get())
         .aspect(Aspect.ORDER, 1)
         .aspect(Aspect.ENTROPY, 1)
         .pattern("STS")
         .define('S', Blocks.STONE)
         .define('T', ModItemTags.THAUMIUM_INGOT)
         .save(consumer, Thaumcraft.location("arcane/funnel"));
      this.shaped("platform", (ItemLike)ModItems.PLATFORM.get(), 2)
         .aspect(Aspect.AIR, 2)
         .aspect(Aspect.ENTROPY, 4)
         .pattern(" S ")
         .pattern("G G")
         .define('G', (ItemLike)ModItems.TRAVEL_STONE.get())
         .define('S', (ItemLike)ModItems.WARDING_STONE.get())
         .save(consumer, Thaumcraft.location("arcane/platform"));
      this.shaped("interface", (ItemLike)ModItems.TRANSVECTOR_INTERFACE.get())
         .aspect(Aspect.ORDER, 12)
         .aspect(Aspect.ENTROPY, 16)
         .pattern("BRB")
         .pattern("LEL")
         .pattern("BRB")
         .define('B', (ItemLike)ModItems.OBSIDIAN_TILE.get())
         .define('E', Items.ENDER_PEARL)
         .define('L', net.neoforged.neoforge.common.Tags.Items.DYES_BLUE)
         .define('R', Items.REDSTONE)
         .save(consumer, Thaumcraft.location("arcane/transvector_interface"));
      this.shaped("interface", (ItemLike)ModItems.CONNECTOR.get())
         .aspect(Aspect.ORDER, 2)
         .pattern(" I ")
         .pattern(" WI")
         .pattern("S  ")
         .define('I', Items.IRON_INGOT)
         .define('W', Items.STICK)
         .define('S', (ItemLike)ModItems.ORDER_SHARD.get())
         .save(consumer, Thaumcraft.location("arcane/connector"));
      this.shaped("dislocator", (ItemLike)ModItems.TRANSVECTOR_DISLOCATOR.get())
         .aspect(Aspect.EARTH, 5)
         .aspect(Aspect.ENTROPY, 5)
         .pattern(" M ")
         .pattern(" I ")
         .pattern(" C ")
         .define('M', (ItemLike)ModItems.PRIMAL_CHARM.get())
         .define('I', (ItemLike)ModItems.TRANSVECTOR_INTERFACE.get())
         .define('C', Items.COMPARATOR)
         .save(consumer, Thaumcraft.location("arcane/transvector_dislocator"));
      this.shaped("mobilizer", (ItemLike)ModItems.MOBILIZER_RELAY.get())
         .aspect(Aspect.AIR, 20)
         .aspect(Aspect.ORDER, 5)
         .aspect(Aspect.EARTH, 15)
         .pattern("WFW")
         .pattern("SIS")
         .pattern("WFW")
         .define('I', Items.IRON_INGOT)
         .define('S', (ItemLike)ModItems.AIR_SHARD.get())
         .define('W', (ItemLike)ModItems.GREATWOOD_LOG.get())
         .define('F', Blocks.GLASS)
         .save(consumer, Thaumcraft.location("arcane/mobilizer_relay"));
      this.shaped("revealing_helm", (ItemLike)ModItems.REVEALING_HELMET.get())
         .aspect(Aspect.AIR, 5)
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.EARTH, 5)
         .pattern("GHG")
         .define('G', (ItemLike)ModItems.GOGGLES_REVEALING.get())
         .define('H', (ItemLike)ModItems.THAUMIUM_HELMET.get())
         .save(consumer, Thaumcraft.location("arcane/revealing_helmet"));
      this.shaped("focus_smelt", (ItemLike)ModItems.FOCUS_SMELT.get())
         .aspect(Aspect.FIRE, 20)
         .aspect(Aspect.ORDER, 10)
         .pattern("FNF")
         .pattern("NSN")
         .pattern("FNF")
         .define('F', (ItemLike)ModItems.FIRE_SHARD.get())
         .define('N', Items.QUARTZ)
         .define('S', (ItemLike)ModItems.FOCUS_FIRE.get())
         .save(consumer, Thaumcraft.location("arcane/focus_smelt"));
      this.shaped("infusion_claw", (ItemLike)ModItems.INFUSION_CLAW.get())
         .aspect(Aspect.AIR, 32)
         .aspect(Aspect.EARTH, 32)
         .aspect(Aspect.WATER, 32)
         .aspect(Aspect.FIRE, 32)
         .aspect(Aspect.ORDER, 32)
         .aspect(Aspect.ENTROPY, 32)
         .pattern("ABA")
         .pattern("BCB")
         .pattern("ABA")
         .define('A', ModItemTags.VOID_INGOT)
         .define('B', (ItemLike)ModItems.ADVANCED_ALCHEMICAL_CONSTRUCT.get())
         .define('C', (ItemLike)ModItems.PRIMAL_CHARM.get())
         .save(consumer, Thaumcraft.location("arcane/infusion_claw"));
      this.shaped("ichor_cloth", (ItemLike)ModItems.ICHOR_CLOTH.get(), 3)
         .aspect(Aspect.AIR, 125)
         .aspect(Aspect.EARTH, 125)
         .aspect(Aspect.WATER, 125)
         .aspect(Aspect.FIRE, 125)
         .aspect(Aspect.ORDER, 125)
         .aspect(Aspect.ENTROPY, 125)
         .pattern("FFF")
         .pattern("III")
         .pattern("DDD")
         .define('F', (ItemLike)ModItems.CLOTH.get())
         .define('I', (ItemLike)ModItems.ICHOR.get())
         .define('D', Items.DIAMOND)
         .save(consumer, Thaumcraft.location("arcane/ichor_cloth"));
      this.shaped("ichorium", (ItemLike)ModItems.ICHORIUM.get())
         .aspect(Aspect.AIR, 125)
         .aspect(Aspect.EARTH, 125)
         .aspect(Aspect.WATER, 125)
         .aspect(Aspect.FIRE, 125)
         .aspect(Aspect.ORDER, 125)
         .aspect(Aspect.ENTROPY, 125)
         .pattern(" D ")
         .pattern("ITI")
         .pattern(" I ")
         .define('D', Items.DIAMOND)
         .define('I', (ItemLike)ModItems.ICHOR.get())
         .define('T', ModItemTags.THAUMIUM_INGOT)
         .save(consumer, Thaumcraft.location("arcane/ichorium"));
      this.shaped("cap_ichor", (ItemLike)ModItems.ICHOR_WAND_CAP.get(), 2)
         .aspect(Aspect.AIR, 100)
         .aspect(Aspect.EARTH, 100)
         .aspect(Aspect.WATER, 100)
         .aspect(Aspect.FIRE, 100)
         .aspect(Aspect.ORDER, 100)
         .aspect(Aspect.ENTROPY, 100)
         .pattern("ITI")
         .pattern(" G ")
         .pattern("ITI")
         .define('I', (ItemLike)ModItems.ICHOR.get())
         .define('T', (ItemLike)ModItems.THAUMIUM_WAND_CAP.get())
         .define('G', ModItemTags.ICHORIUM_INGOT)
         .save(consumer, Thaumcraft.location("arcane/cap_ichor"));
      this.shaped("ichor_tools", (ItemLike)ModItems.ICHOR_PICKAXE.get())
         .aspect(Aspect.FIRE, 50)
         .aspect(Aspect.ORDER, 50)
         .aspect(Aspect.ENTROPY, 50)
         .pattern("III")
         .pattern(" S ")
         .pattern(" S ")
         .define('I', ModItemTags.ICHORIUM_INGOT)
         .define('S', (ItemLike)ModItems.SILVERWOOD_WAND_ROD.get())
         .save(consumer, Thaumcraft.location("arcane/ichor_pickaxe"));
      this.shaped("ichor_tools", (ItemLike)ModItems.ICHOR_SHOVEL.get())
         .aspect(Aspect.FIRE, 50)
         .aspect(Aspect.ORDER, 50)
         .aspect(Aspect.EARTH, 50)
         .pattern(" I ")
         .pattern(" S ")
         .pattern(" S ")
         .define('I', ModItemTags.ICHORIUM_INGOT)
         .define('S', (ItemLike)ModItems.SILVERWOOD_WAND_ROD.get())
         .save(consumer, Thaumcraft.location("arcane/ichor_shovel"));
      this.shaped("ichor_tools", (ItemLike)ModItems.ICHOR_AXE.get())
         .aspect(Aspect.FIRE, 50)
         .aspect(Aspect.ORDER, 50)
         .aspect(Aspect.AIR, 50)
         .pattern("II ")
         .pattern("IS ")
         .pattern(" S ")
         .define('I', ModItemTags.ICHORIUM_INGOT)
         .define('S', (ItemLike)ModItems.SILVERWOOD_WAND_ROD.get())
         .save(consumer, Thaumcraft.location("arcane/ichor_axe"));
      this.shaped("ichor_tools", (ItemLike)ModItems.ICHOR_SWORD.get())
         .aspect(Aspect.FIRE, 50)
         .aspect(Aspect.ORDER, 50)
         .aspect(Aspect.WATER, 50)
         .pattern(" I ")
         .pattern(" I ")
         .pattern(" S ")
         .define('I', ModItemTags.ICHORIUM_INGOT)
         .define('S', (ItemLike)ModItems.SILVERWOOD_WAND_ROD.get())
         .save(consumer, Thaumcraft.location("arcane/ichor_sword"));
      this.shaped("ichorcloth_armor", (ItemLike)ModItems.ICHOR_HELMET.get())
         .aspect(Aspect.FIRE, 50)
         .aspect(Aspect.ORDER, 50)
         .aspect(Aspect.WATER, 50)
         .pattern("CCC")
         .pattern("C C")
         .define('C', (ItemLike)ModItems.ICHOR_CLOTH.get())
         .save(consumer, Thaumcraft.location("arcane/ichor_helmet"));
      this.shaped("ichorcloth_armor", (ItemLike)ModItems.ICHOR_CHEST.get())
         .aspect(Aspect.FIRE, 50)
         .aspect(Aspect.ORDER, 50)
         .aspect(Aspect.AIR, 50)
         .pattern("C C")
         .pattern("CCC")
         .pattern("CCC")
         .define('C', (ItemLike)ModItems.ICHOR_CLOTH.get())
         .save(consumer, Thaumcraft.location("arcane/ichor_chest"));
      this.shaped("ichorcloth_armor", (ItemLike)ModItems.ICHOR_LEGGINGS.get())
         .aspect(Aspect.FIRE, 50)
         .aspect(Aspect.ORDER, 50)
         .aspect(Aspect.EARTH, 50)
         .pattern("CCC")
         .pattern("C C")
         .pattern("C C")
         .define('C', (ItemLike)ModItems.ICHOR_CLOTH.get())
         .save(consumer, Thaumcraft.location("arcane/ichor_leggings"));
      this.shaped("ichorcloth_armor", (ItemLike)ModItems.ICHOR_BOOTS.get())
         .aspect(Aspect.FIRE, 50)
         .aspect(Aspect.ORDER, 50)
         .aspect(Aspect.ENTROPY, 50)
         .pattern("C C")
         .pattern("C C")
         .define('C', (ItemLike)ModItems.ICHOR_CLOTH.get())
         .save(consumer, Thaumcraft.location("arcane/ichor_boots"));
      this.shaped("distilessence", (ItemLike)ModItems.ARCANE_ALEMBIC.get())
         .aspect(Aspect.AIR, 5)
         .aspect(Aspect.WATER, 5)
         .pattern("FIG")
         .pattern("IBI")
         .pattern("I I")
         .define('I', Items.IRON_INGOT)
         .define('B', Items.BUCKET)
         .define('G', Items.GOLD_INGOT)
         .define('F', (ItemLike)ModItems.VIS_FILTER.get())
         .save(consumer, Thaumcraft.location("arcane/arcane_alembic"));
      this.shaped("bellows", (ItemLike)ModItems.ARCANE_BELLOWS.get())
         .aspect(Aspect.AIR, 10)
         .aspect(Aspect.ORDER, 5)
         .pattern("WW ")
         .pattern("LCI")
         .pattern("WW ")
         .define('W', ItemTags.PLANKS)
         .define('C', (ItemLike)ModItems.AIR_SHARD.get())
         .define('I', Items.IRON_INGOT)
         .define('L', Items.LEATHER)
         .save(consumer, Thaumcraft.location("arcane/arcane_bellows"));
      this.shaped("tubes", (ItemLike)ModItems.ESSENTIA_TUBE.get(), 8)
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.ORDER, 5)
         .pattern(" Q ")
         .pattern("IGI")
         .pattern(" B ")
         .define('I', Items.IRON_INGOT)
         .define('B', Items.GOLD_NUGGET)
         .define('G', Blocks.GLASS)
         .define('Q', (ItemLike)ModItems.QUICKSILVER.get())
         .save(consumer, Thaumcraft.location("arcane/essence_tube"));
      this.shaped("tubes", (ItemLike)ModItems.RESONATOR.get())
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.AIR, 5)
         .pattern("I I")
         .pattern("INI")
         .pattern(" S ")
         .define('I', Items.IRON_INGOT)
         .define('N', Items.QUARTZ)
         .define('S', Items.STICK)
         .save(consumer, Thaumcraft.location("arcane/resonator"));
      this.shapeless("tubes", (ItemLike)ModItems.ESSENTIA_VALVE.get())
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.ORDER, 5)
         .requires((ItemLike)ModItems.ESSENTIA_TUBE.get())
         .requires(Blocks.LEVER)
         .save(consumer, Thaumcraft.location("arcane/essence_valve"));
      this.shapeless("tube_filter", (ItemLike)ModItems.FILTERED_ESSENTIA_TUBE.get())
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.ORDER, 16)
         .requires((ItemLike)ModItems.ESSENTIA_TUBE.get())
         .requires((ItemLike)ModItems.VIS_FILTER.get())
         .save(consumer, Thaumcraft.location("arcane/filtered_essence_tube"));
      this.shapeless("tube_filter", (ItemLike)ModItems.RESTRICTED_ESSENTIA_TUBE.get())
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.EARTH, 16)
         .requires((ItemLike)ModItems.ESSENTIA_TUBE.get())
         .requires(net.neoforged.neoforge.common.Tags.Items.STONES)
         .save(consumer, Thaumcraft.location("arcane/restricted_essence_tube"));
      this.shapeless("tube_filter", (ItemLike)ModItems.DIRECTIONAL_ESSENTIA_TUBE.get())
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.ORDER, 8)
         .aspect(Aspect.ENTROPY, 8)
         .requires((ItemLike)ModItems.ESSENTIA_TUBE.get())
         .requires(net.neoforged.neoforge.common.Tags.Items.DYES_BLACK)
         .save(consumer, Thaumcraft.location("arcane/directional_essence_tube"));
      this.shaped("centrifuge", (ItemLike)ModItems.ESSENTIA_BUFFER.get())
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.ORDER, 5)
         .pattern("PVP")
         .pattern("T T")
         .pattern("PRP")
         .define('T', (ItemLike)ModItems.ESSENTIA_TUBE.get())
         .define('V', (ItemLike)ModItems.ESSENTIA_VALVE.get())
         .define('R', (ItemLike)ModItems.RESTRICTED_ESSENTIA_TUBE.get())
         .define('P', (ItemLike)ModItems.PHIAL_ESSENCE.get())
         .save(consumer, Thaumcraft.location("arcane/essence_buffer"));
      this.shaped("distilessence", (ItemLike)ModItems.ALCHEMICAL_CONSTRUCT.get())
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.ORDER, 5)
         .pattern("VTF")
         .pattern("TWT")
         .pattern("FTV")
         .define('W', (ItemLike)ModItems.GREATWOOD_PLANKS.get())
         .define('V', (ItemLike)ModItems.ESSENTIA_VALVE.get())
         .define('T', (ItemLike)ModItems.ESSENTIA_TUBE.get())
         .define('F', (ItemLike)ModItems.VIS_FILTER.get())
         .save(consumer, Thaumcraft.location("arcane/alchemical_construct"));
      this.shaped("adv_alchemy_furnace", (ItemLike)ModItems.ADVANCED_ALCHEMICAL_CONSTRUCT.get(), 4)
         .aspect(Aspect.WATER, 10)
         .aspect(Aspect.ORDER, 30)
         .aspect(Aspect.EARTH, 10)
         .pattern("VAV")
         .pattern("APA")
         .pattern("VAV")
         .define('A', (ItemLike)ModItems.ALCHEMICAL_CONSTRUCT.get())
         .define('V', ModItemTags.VOID_INGOT)
         .define('P', (ItemLike)ModItems.PRIMORDIAL_PEARL.get())
         .save(consumer, Thaumcraft.location("arcane/advanced_alchemical_construct"));
      this.shaped("centrifuge", (ItemLike)ModItems.ALCHEMICAL_CENTRIFUGE.get())
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.ORDER, 5)
         .aspect(Aspect.ENTROPY, 5)
         .pattern(" T ")
         .pattern("ACP")
         .pattern(" T ")
         .define('T', (ItemLike)ModItems.ESSENTIA_TUBE.get())
         .define('P', Blocks.PISTON)
         .define('A', (ItemLike)ModItems.ARCANE_ALEMBIC.get())
         .define('C', (ItemLike)ModItems.ALCHEMICAL_CONSTRUCT.get())
         .save(consumer, Thaumcraft.location("arcane/alchemical_centrifuge"));
      this.shaped("essence_crystal", (ItemLike)ModItems.ESSENTIA_CRYSTALLIZER.get())
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.EARTH, 15)
         .aspect(Aspect.ORDER, 5)
         .pattern("IDI")
         .pattern("QCQ")
         .pattern("WTW")
         .define('T', (ItemLike)ModItems.ESSENTIA_TUBE.get())
         .define('D', Blocks.DISPENSER)
         .define('Q', (ItemLike)ModItems.BALANCED_SHARD.get())
         .define('I', Items.IRON_INGOT)
         .define('W', ItemTags.PLANKS)
         .define('C', (ItemLike)ModItems.ALCHEMICAL_CONSTRUCT.get())
         .save(consumer, Thaumcraft.location("arcane/essence_crystallizer"));
      this.shaped("thaumatorium", (ItemLike)ModItems.MNEMONIC_MATRIX.get())
         .aspect(Aspect.FIRE, 5)
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.ORDER, 5)
         .pattern("IAI")
         .pattern("ABA")
         .pattern("IAI")
         .define('B', (ItemLike)ModItems.ZOMBIE_BRAIN.get())
         .define('A', (ItemLike)ModItems.AMBER.get())
         .define('I', Items.IRON_INGOT)
         .save(consumer, Thaumcraft.location("arcane/mnemonic_matrix"));
   }

   private ShappelesArcaneRecipeBuilder shapeless(String key, ItemLike itemLike) {
      return this.shapeless(key, itemLike, 1);
   }

   private ShappelesArcaneRecipeBuilder shapeless(String key, ItemLike itemLike, int count) {
      return ShappelesArcaneRecipeBuilder.shapeless(key, itemLike, count);
   }

   private ShapedArcaneRecipeBuilder shaped(String key, ItemLike itemLike) {
      return this.shaped(key, itemLike, 1);
   }

   private ShapedArcaneRecipeBuilder shaped(String key, ItemLike itemLike, int count) {
      return ShapedArcaneRecipeBuilder.shaped(key, itemLike, count);
   }

   private ShapedArcaneRecipeBuilder shaped(String key, ItemStack stack) {
      return ShapedArcaneRecipeBuilder.shaped(key, stack);
   }

   private void primalArrow(ItemLike result, Aspect aspect, ItemLike shard, String type, RecipeOutput consumer) {
      this.shaped("primal_arrow", result, 8)
         .aspect(aspect, 8)
         .pattern("AAA")
         .pattern("ASA")
         .pattern("AAA")
         .define('A', Items.ARROW)
         .define('S', shard)
         .save(consumer, Thaumcraft.location("arcane/" + type + "_arrow"));
   }

   private void sceptre(String id, ItemWandCap cap, ItemLike rodIngredient, ItemWandRod rod, RecipeOutput consumer) {
      int cost = (int)(cap.getCraftCost() * rod.getCraftCost() * 1.5F);
      ItemStack result = ItemWandCasting.createSceptre(rod, cap);
      ShapedArcaneRecipeBuilder builder = this.shaped("sceptre", result)
         .pattern(" TF")
         .pattern(" RT")
         .pattern("T  ")
         .define('T', cap)
         .define('R', rodIngredient)
         .define('F', (ItemLike)ModItems.PRIMAL_CHARM.get());

      for (Aspect aspect : Aspect.getPrimalAspects()) {
         builder.aspect(aspect, cost);
      }

      builder.save(consumer, Thaumcraft.location("arcane/" + id));
   }

   private void banner(DyeColor color, RecipeOutput consumer) {
      this.shaped("banners", (ItemLike)ModItems.BANNER.get(color).get())
         .aspect(Aspect.WATER, 5)
         .aspect(Aspect.EARTH, 5)
         .pattern("WS")
         .pattern("WS")
         .pattern("WB")
         .define('W', this.wool(color))
         .define('S', Items.STICK)
         .define('B', ItemTags.WOODEN_SLABS)
         .save(consumer, Thaumcraft.location("arcane/banner_" + color.getName()));
   }

   private ItemLike wool(DyeColor color) {
      return switch (color) {
         case WHITE -> Blocks.WHITE_WOOL;
         case ORANGE -> Blocks.ORANGE_WOOL;
         case MAGENTA -> Blocks.MAGENTA_WOOL;
         case LIGHT_BLUE -> Blocks.LIGHT_BLUE_WOOL;
         case YELLOW -> Blocks.YELLOW_WOOL;
         case LIME -> Blocks.LIME_WOOL;
         case PINK -> Blocks.PINK_WOOL;
         case GRAY -> Blocks.GRAY_WOOL;
         case LIGHT_GRAY -> Blocks.LIGHT_GRAY_WOOL;
         case CYAN -> Blocks.CYAN_WOOL;
         case PURPLE -> Blocks.PURPLE_WOOL;
         case BLUE -> Blocks.BLUE_WOOL;
         case BROWN -> Blocks.BROWN_WOOL;
         case GREEN -> Blocks.GREEN_WOOL;
         case RED -> Blocks.RED_WOOL;
         case BLACK -> Blocks.BLACK_WOOL;
         default -> throw new MatchException(null, null);
      };
   }
}
