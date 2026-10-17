package thaumcraft.init.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.food.FoodProperties.Builder;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Items;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.block.deconstruction_table.DeconstructionTableBlockItem;
import thaumcraft.block.eccence_reservoir.ItemEssenceReservoir;
import thaumcraft.block.infusion_claw.ItemInfusionClaw;
import thaumcraft.block.infusion_matrix.RunicMatrixBlockItem;
import thaumcraft.block.jar.node.NodeJarBlockItem;
import thaumcraft.block.jar.standart.JarBlockItem;
import thaumcraft.block.magnet.ItemMagnetBlock;
import thaumcraft.block.repairer.RepairerBlockItem;
import thaumcraft.block.wand_charger.WandChargerItemUpgrade;
import thaumcraft.entities.smart.golem.EnumGolemType;
import thaumcraft.item.alumentum.AlumentumItem;
import thaumcraft.item.symmetry_calculator.SymmetryCalculatorItem;
import thaumcraft.item.armor.cultist_boots.ItemCultistBoots;
import thaumcraft.item.armor.cultist_leader.ItemCultistLeaderArmor;
import thaumcraft.item.armor.cultist_plate.ItemCultistPlateArmor;
import thaumcraft.item.armor.cultist_robe.ItemCultistRobeArmor;
import thaumcraft.item.armor.goggles.ItemGoggles;
import thaumcraft.item.armor.hover_harness.ItemHoverHarness;
import thaumcraft.item.armor.ichor.ItemIchorArmor;
import thaumcraft.item.armor.ichor_gem.ItemGemBoots;
import thaumcraft.item.armor.ichor_gem.ItemGemChest;
import thaumcraft.item.armor.ichor_gem.ItemGemHelm;
import thaumcraft.item.armor.ichor_gem.ItemGemLegs;
import thaumcraft.item.armor.revealing_helmet.ItemRevealingHelmet;
import thaumcraft.item.armor.robe.ItemRobeArmor;
import thaumcraft.item.armor.thaumium.ItemThaumiumArmor;
import thaumcraft.item.armor.thaumium_fortress.ItemFortressArmor;
import thaumcraft.item.armor.traveller_boots.ItemBootsTraveller;
import thaumcraft.item.armor.void_robe.ItemVoidRobeArmor;
import thaumcraft.item.armor.voidy.ItemVoidArmor;
import thaumcraft.item.aura_node.ItemAuraNode;
import thaumcraft.item.aura_node.PresetAuraNodeItem;
import thaumcraft.item.banner.ItemBanner;
import thaumcraft.item.bath_salts.ItemBathSalts;
import thaumcraft.item.block_talisman.ItemBlockTalisman;
import thaumcraft.item.bottle_taint.ItemBottleTaint;
import thaumcraft.item.bright_nitor.ItemBrightNitor;
import thaumcraft.item.cat_amulet.ItemCatAmulet;
import thaumcraft.item.cleansing_talisman.ItemCleansingTalisman;
import thaumcraft.item.compass_stone.ItemCompassStone;
import thaumcraft.item.connector.ItemConnector;
import thaumcraft.item.crystal_cluster.ItemCrystalCluster;
import thaumcraft.item.crystall_essence.ItemCrystalEssence;
import thaumcraft.item.curio.CurioItem;
import thaumcraft.item.curios.ItemAmuletRunic;
import thaumcraft.item.curios.ItemAmuletVis;
import thaumcraft.item.curios.ItemBaubleBlanks;
import thaumcraft.item.curios.ItemGirdleHover;
import thaumcraft.item.curios.ItemGirdleRunic;
import thaumcraft.item.curios.ItemRingRunic;
import thaumcraft.item.eldritch.CrimsonRitesItem;
import thaumcraft.item.eldritch.EldritchObjectItem;
import thaumcraft.item.eldritch.ItemEldritchAltarPlacer;
import thaumcraft.item.eldritch.PrimordialPearlItem;
import thaumcraft.item.eldritch.RunedTabletItem;
import thaumcraft.item.equipment.ItemBowBone;
import thaumcraft.item.equipment.ItemCrimsonSword;
import thaumcraft.item.equipment.ItemElementalAxe;
import thaumcraft.item.equipment.ItemElementalHoe;
import thaumcraft.item.equipment.ItemElementalPickaxe;
import thaumcraft.item.equipment.ItemElementalShovel;
import thaumcraft.item.equipment.ItemElementalSword;
import thaumcraft.item.equipment.ItemPrimalCrusher;
import thaumcraft.item.equipment.ItemVoidAxe;
import thaumcraft.item.equipment.ItemVoidHoe;
import thaumcraft.item.equipment.ItemVoidPickaxe;
import thaumcraft.item.equipment.ItemVoidShovel;
import thaumcraft.item.equipment.ItemVoidSword;
import thaumcraft.item.equipment.blood_sword.ItemBloodSword;
import thaumcraft.item.equipment.ichor.ItemIchorAxe;
import thaumcraft.item.equipment.ichor.ItemIchorAxeAdv;
import thaumcraft.item.equipment.ichor.ItemIchorPickaxe;
import thaumcraft.item.equipment.ichor.ItemIchorPickaxeAdv;
import thaumcraft.item.equipment.ichor.ItemIchorShovel;
import thaumcraft.item.equipment.ichor.ItemIchorShovelAdv;
import thaumcraft.item.equipment.ichor.ItemIchorSword;
import thaumcraft.item.equipment.ichor.ItemIchorSwordAdv;
import thaumcraft.item.gas.ItemGas;
import thaumcraft.item.gas_removal.ItemGasRemover;
import thaumcraft.item.golem_bell.ItemGolemBell;
import thaumcraft.item.golem_core.GolemCore;
import thaumcraft.item.golem_core.ItemGolemCore;
import thaumcraft.item.golem_decoration.GolemDecorationType;
import thaumcraft.item.golem_decoration.ItemGolemDecoration;
import thaumcraft.item.golem_placer.ItemGolemPlacer;
import thaumcraft.item.golem_upgrade.GolemUpgrade;
import thaumcraft.item.golem_upgrade.ItemGolemUpgrade;
import thaumcraft.item.hand_mirror.ItemHandMirror;
import thaumcraft.item.hungry_chest.ItemHungryChest;
import thaumcraft.item.ichor_pouch.ItemIchorPouch;
import thaumcraft.item.ink_well.InkWell;
import thaumcraft.item.jar_label.JarLabelItem;
import thaumcraft.item.key.ItemKey;
import thaumcraft.item.knowledge_fragment.KnowledgeFragmentItem;
import thaumcraft.item.loot_bag.ItemLootBag;
import thaumcraft.item.magic.CleanFragmentItem;
import thaumcraft.item.magic.DistortedFragmentItem;
import thaumcraft.item.magic.ResearchVolumeItem;
import thaumcraft.item.magic.VolumeOfPurificationItem;
import thaumcraft.item.mana_bean.ItemManaBean;
import thaumcraft.item.matrix_upgrade.MatrixUpgradeItem;
import thaumcraft.item.mirror.ItemMirror;
import thaumcraft.item.mob_aspect.ItemMobAspect;
import thaumcraft.item.phial_essence.ItemPhialEssence;
import thaumcraft.item.placement_mirror.ItemPlacementMirror;
import thaumcraft.item.primal_charm.PrimalCharmItem;
import thaumcraft.item.protoclay.ItemProtoclay;
import thaumcraft.item.relics.ItemResonator;
import thaumcraft.item.relics.ItemSanityChecker;
import thaumcraft.item.relics.ItemThaumometer;
import thaumcraft.item.relics.thaumonomicon.ItemThaumonomicon;
import thaumcraft.item.research_notes.ItemResearchNotes;
import thaumcraft.item.sanity_soap.ItemSanitySoap;
import thaumcraft.item.shard.ItemShard;
import thaumcraft.item.sky_pearl.ItemSkyPearl;
import thaumcraft.item.soul_mould.ItemSoulMould;
import thaumcraft.item.spell_cloth.ItemSpellCloth;
import thaumcraft.item.taint.TaintItem;
import thaumcraft.item.trunk_spawner.ItemTrunkSpawner;
import thaumcraft.item.tube.ItemTube;
import thaumcraft.item.wands.ItemFocusPouchBauble;
import thaumcraft.item.wands.ItemWandCap;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.ItemWandRod;
import thaumcraft.item.wands.WandRodPrimalOnUpdate;
import thaumcraft.item.wands.foci.ItemFocusDeflect;
import thaumcraft.item.wands.foci.ItemFocusEnderChest;
import thaumcraft.item.wands.foci.ItemFocusExcavation;
import thaumcraft.item.wands.foci.ItemFocusFire;
import thaumcraft.item.wands.foci.ItemFocusFlight;
import thaumcraft.item.wands.foci.ItemFocusFrost;
import thaumcraft.item.wands.foci.ItemFocusHeal;
import thaumcraft.item.wands.foci.ItemFocusHellbat;
import thaumcraft.item.wands.foci.ItemFocusPech;
import thaumcraft.item.wands.foci.ItemFocusPortableHole;
import thaumcraft.item.wands.foci.ItemFocusPrimal;
import thaumcraft.item.wands.foci.ItemFocusRecall;
import thaumcraft.item.wands.foci.ItemFocusShadowbeam;
import thaumcraft.item.wands.foci.ItemFocusShock;
import thaumcraft.item.wands.foci.ItemFocusSmelt;
import thaumcraft.item.wands.foci.ItemFocusTelekinesis;
import thaumcraft.item.wands.foci.ItemFocusTrade;
import thaumcraft.item.wands.foci.ItemFocusWarding;
import thaumcraft.item.wands.foci.ItemFocusXPDrain;
import thaumcraft.item.wisp_essence.ItemWispEssence;
import thaumcraft.item.xp_talisman.ItemXPTalisman;
import thaumcraft.item.zombie_brain.ItemZombieBrain;

public class ModItems {
   public static final Items REGISTER = DeferredRegister.createItems("thaumcraft");
   public static final List<DeferredItem<Item>> SPAWN_EGGS = new ArrayList<>();
   public static final DeferredItem<BlockItem> CRUCIBLE = registerBlock("crucible", ModBlocks.CRUCIBLE);
   public static final DeferredItem<BlockItem> ARCANE_ALEMBIC = registerBlock("arcane_alembic", ModBlocks.ARCANE_ALEMBIC);
   public static final DeferredItem<BlockItem> ALCHEMICAL_CONSTRUCT = registerBlock("alchemical_construct", ModBlocks.ALCHEMICAL_CONSTRUCT);
   public static final DeferredItem<BlockItem> ADVANCED_ALCHEMICAL_CONSTRUCT = registerBlock(
      "advanced_alchemical_construct", ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT
   );
   public static final DeferredItem<BlockItem> GRATE = registerBlock("grate", ModBlocks.GRATE);
   public static final DeferredItem<BlockItem> ARCANE_LAMP = registerBlock("arcane_lamp", ModBlocks.ARCANE_LAMP);
   public static final DeferredItem<BlockItem> ARCANE_LAMP_GROWTH = registerBlock("arcane_lamp_growth", ModBlocks.ARCANE_LAMP_GROWTH);
   public static final DeferredItem<BlockItem> ARCANE_LAMP_FERTILITY = registerBlock("arcane_lamp_fertility", ModBlocks.ARCANE_LAMP_FERTILITY);
   public static final DeferredItem<Item> FLUX_GOO_BUCKET = REGISTER.register(
      "flux_goo_bucket",
      () -> new BucketItem((Fluid)ModFluids.FLUX_GOO.getSource().get(), new Properties().stacksTo(1).craftRemainder(net.minecraft.world.item.Items.BUCKET))
   );
   public static final DeferredItem<Item> FLUX_GAS_BUCKET = REGISTER.register(
      "flux_gas_bucket",
      () -> new BucketItem((Fluid)ModFluids.FLUX_GAS.getSource().get(), new Properties().stacksTo(1).craftRemainder(net.minecraft.world.item.Items.BUCKET))
   );
   public static final DeferredItem<Item> FLUID_PURE_BUCKET = REGISTER.register(
      "fluid_pure_bucket",
      () -> new BucketItem((Fluid)ModFluids.FLUID_PURE.getSource().get(), new Properties().stacksTo(1).craftRemainder(net.minecraft.world.item.Items.BUCKET))
   );
   public static final DeferredItem<Item> FLUID_DEATH_BUCKET = REGISTER.register(
      "fluid_death_bucket",
      () -> new BucketItem((Fluid)ModFluids.FLUID_DEATH.getSource().get(), new Properties().stacksTo(1).craftRemainder(net.minecraft.world.item.Items.BUCKET))
   );
   public static final DeferredItem<BlockItem> MNEMONIC_MATRIX = registerBlock("mnemonic_matrix", ModBlocks.MNEMONIC_MATRIX);
   public static final DeferredItem<BlockItem> VIS_RELAY = registerBlock("vis_relay", ModBlocks.VIS_RELAY);
   public static final DeferredItem<BlockItem> VIS_CHARGE_RELAY = registerBlock("vis_charge_relay", ModBlocks.VIS_CHARGE_RELAY);
   public static final DeferredItem<BlockItem> ALCHEMY_FURNACE = registerBlock("alchemy_furnace", ModBlocks.ALCHEMY_FURNACE);
   public static final DeferredItem<BlockItem> ARCANE_PEDESTAL = registerBlock("arcane_pedestal", ModBlocks.ARCANE_PEDESTAL);
   public static final DeferredItem<BlockItem> RUNIC_MATRIX = registerBlock("runic_matrix", ModBlocks.RUNIC_MATRIX, RunicMatrixBlockItem::new);
   public static final DeferredItem<BlockItem> WAND_PEDESTAL = registerBlock("wand_pedestal", ModBlocks.WAND_PEDESTAL);
   public static final DeferredItem<BlockItem> WAND_PEDESTAL_FOCUS = registerBlock("wand_pedestal_focus", ModBlocks.WAND_PEDESTAL_FOCUS);
   public static final DeferredItem<BlockItem> WAND_CHARGER = registerBlock("wand_charger", ModBlocks.WAND_CHARGER);
   public static final DeferredItem<BlockItem> SUMMONER = registerBlock("summoner", ModBlocks.SUMMONER);
   public static final DeferredItem<BlockItem> NODE_STABILIZER = registerBlock("node_stabilizer", ModBlocks.NODE_STABILIZER);
   public static final DeferredItem<BlockItem> ADVANCED_NODE_STABILIZER = registerBlock("advanced_node_stabilizer", ModBlocks.ADVANCED_NODE_STABILIZER);
   public static final DeferredItem<BlockItem> ADVANCED_NODE_INFUSER = registerBlock("advanced_node_infuser", ModBlocks.ADVANCED_NODE_INFUSER);
   public static final DeferredItem<BlockItem> NODE_TRANSDUCER = registerBlock("node_transducer", ModBlocks.NODE_TRANSDUCER);
   public static final DeferredItem<BlockItem> ARCANE_SPA = registerBlock("arcane_spa", ModBlocks.ARCANE_SPA);
   public static final DeferredItem<BlockItem> FOCAL_MANIPULATOR = registerBlock("focal_manipulator", ModBlocks.FOCAL_MANIPULATOR);
   public static final DeferredItem<BlockItem> ENCHANTER = registerBlock("enchanter", ModBlocks.ENCHANTER);
   public static final DeferredItem<BlockItem> FLUX_SCRUBBER = registerBlock("flux_scrubber", ModBlocks.FLUX_SCRUBBER);
   public static final DeferredItem<BlockItem> ARCANE_BELLOWS = registerBlock("arcane_bellows", ModBlocks.ARCANE_BELLOWS);
   public static final DeferredItem<BlockItem> ARCANE_SENSOR = registerBlock("arcane_sensor", ModBlocks.ARCANE_SENSOR);
   public static final DeferredItem<BlockItem> ARCANE_PRESSURE_PLATE = registerBlock("arcane_pressure_plate", ModBlocks.ARCANE_PRESSURE_PLATE);
   public static final DeferredItem<BlockItem> ARCANE_BORE_BASE = registerBlock("arcane_bore_base", ModBlocks.ARCANE_BORE_BASE);
   public static final DeferredItem<BlockItem> ARCANE_BORE = registerBlock("arcane_bore", ModBlocks.ARCANE_BORE);
   public static final DeferredItem<BlockItem> CRIMSON_CULT_BANNER = registerBlock(
      "crimson_cult_banner", ModBlocks.CRIMSON_CULT_BANNER, block -> new ItemBanner(block, (Block)ModBlocks.CRIMSON_CULT_WALL_BANNER.get())
   );
   public static final Map<DyeColor, DeferredItem<BlockItem>> BANNER = registerBannerBlockColors();
   public static final DeferredItem<BlockItem> GREATWOOD_LOG = registerBlock("greatwood_log", ModBlocks.GREATWOOD_LOG);
   public static final DeferredItem<BlockItem> GREATWOOD_PLANKS = registerBlock("greatwood_planks", ModBlocks.GREATWOOD_PLANKS);
   public static final DeferredItem<BlockItem> GREATWOOD_SLAB = registerBlock("greatwood_slab", ModBlocks.GREATWOOD_SLAB);
   public static final DeferredItem<BlockItem> GREATWOOD_STAIRS = registerBlock("greatwood_stairs", ModBlocks.GREATWOOD_STAIRS);
   public static final DeferredItem<BlockItem> GREATWOOD_LEAVES = registerBlock("greatwood_leaves", ModBlocks.GREATWOOD_LEAVES);
   public static final DeferredItem<Item> GREATWOOD_SAPLING = REGISTER.register(
      "greatwood_sapling", () -> new BlockItem((Block)ModBlocks.GREATWOOD_SAPLING.get(), new Properties())
   );
   public static final DeferredItem<BlockItem> SILVERWOOD_LOG = registerBlock("silverwood_log", ModBlocks.SILVERWOOD_LOG);
   public static final DeferredItem<BlockItem> SILVERWOOD_PLANKS = registerBlock("silverwood_planks", ModBlocks.SILVERWOOD_PLANKS);
   public static final DeferredItem<BlockItem> SILVERWOOD_SLAB = registerBlock("silverwood_slab", ModBlocks.SILVERWOOD_SLAB);
   public static final DeferredItem<BlockItem> SILVERWOOD_STAIRS = registerBlock("silverwood_stairs", ModBlocks.SILVERWOOD_STAIRS);
   public static final DeferredItem<BlockItem> SILVERWOOD_LEAVES = registerBlock("silverwood_leaves", ModBlocks.SILVERWOOD_LEAVES);
   public static final DeferredItem<Item> SILVERWOOD_SAPLING = REGISTER.register(
      "silverwood_sapling", () -> new BlockItem((Block)ModBlocks.SILVERWOOD_SAPLING.get(), new Properties())
   );
   public static final DeferredItem<BlockItem> TABLE = registerBlock("table", ModBlocks.TABLE);
   public static final DeferredItem<BlockItem> RESEARCH_TABLE = registerBlock("research_table", ModBlocks.RESEARCH_TABLE);
   public static final DeferredItem<Item> DECONSTRUCTION_TABLE = REGISTER.register(
      "deconstruction_table", () -> new DeconstructionTableBlockItem((Block)ModBlocks.DECONSTRUCTION_TABLE.get(), new Properties())
   );
   public static final DeferredItem<BlockItem> ARCANE_WORKBENCH = registerBlock("arcane_workbench", ModBlocks.ARCANE_WORKBENCH);
   public static final DeferredItem<Item> HUNGRY_CHEST = REGISTER.register(
      "hungry_chest", () -> new ItemHungryChest((Block)ModBlocks.HUNGRY_CHEST.get(), new Properties())
   );
   public static final DeferredItem<BlockItem> FUNNEL = registerBlock("funnel", ModBlocks.FUNNEL);
   public static final DeferredItem<BlockItem> CINNABAR_ORE = registerBlock("cinnabar_ore", ModBlocks.CINNABAR_ORE);
   public static final DeferredItem<BlockItem> AMBER_ORE = registerBlock("amber_ore", ModBlocks.AMBER_ORE);
   public static final DeferredItem<BlockItem> AIR_SHARD_ORE = registerBlock("air_shard_ore", ModBlocks.AIR_SHARD_ORE);
   public static final DeferredItem<BlockItem> FIRE_SHARD_ORE = registerBlock("fire_shard_ore", ModBlocks.FIRE_SHARD_ORE);
   public static final DeferredItem<BlockItem> WATER_SHARD_ORE = registerBlock("water_shard_ore", ModBlocks.WATER_SHARD_ORE);
   public static final DeferredItem<BlockItem> EARTH_SHARD_ORE = registerBlock("earth_shard_ore", ModBlocks.EARTH_SHARD_ORE);
   public static final DeferredItem<BlockItem> ORDER_SHARD_ORE = registerBlock("order_shard_ore", ModBlocks.ORDER_SHARD_ORE);
   public static final DeferredItem<BlockItem> ENTROPY_SHARD_ORE = registerBlock("entropy_shard_ore", ModBlocks.ENTROPY_SHARD_ORE);
   public static final DeferredItem<BlockItem> SHIMMER_LEAF = registerBlock("shimmer_leaf", ModBlocks.SHIMMER_LEAF);
   public static final DeferredItem<BlockItem> CINDER_PEARL = registerBlock("cinder_pearl", ModBlocks.CINDER_PEARL);
   public static final DeferredItem<BlockItem> ETHEREAL_BLOOM = registerBlock("ethereal_bloom", ModBlocks.ETHEREAL_BLOOM);
   public static final DeferredItem<BlockItem> VISHROOM = registerBlock("vishroom", ModBlocks.VISHROOM);
   public static final Map<DyeColor, DeferredItem<BlockItem>> CANDLE = registerBlockColors("candle", ModBlocks.CANDLE);
   public static final DeferredItem<BlockItem> JAR = registerBlock("jar", ModBlocks.JAR, JarBlockItem::new);
   public static final DeferredItem<BlockItem> BRAIN_JAR = registerBlock("brain_jar", ModBlocks.BRAIN_JAR, JarBlockItem::new);
   public static final DeferredItem<BlockItem> NODE_JAR = registerBlock("node_jar", ModBlocks.NODE_JAR, NodeJarBlockItem::new);
   public static final DeferredItem<BlockItem> VOID_JAR = registerBlock("void_jar", ModBlocks.VOID_JAR, JarBlockItem::new);
   public static final DeferredItem<BlockItem> ARCANE_DOOR = registerBlock("arcane_door", ModBlocks.ARCANE_DOOR);
   public static final DeferredItem<BlockItem> LIFTER = registerBlock("lifter", ModBlocks.LIFTER);
   public static final DeferredItem<BlockItem> TRANSVECTOR_INTERFACE = registerBlock("transvector_interface", ModBlocks.TRANSVECTOR_INTERFACE);
   public static final DeferredItem<BlockItem> TRANSVECTOR_DISLOCATOR = registerBlock("transvector_dislocator", ModBlocks.TRANSVECTOR_DISLOCATOR);
   public static final DeferredItem<BlockItem> MOBILIZER = registerBlock("mobilizer", ModBlocks.MOBILIZER);
   public static final DeferredItem<BlockItem> MOBILIZER_RELAY = registerBlock("mobilizer_relay", ModBlocks.MOBILIZER_RELAY);
   public static final DeferredItem<BlockItem> MAGNET = REGISTER.register("magnet", () -> new ItemMagnetBlock((Block)ModBlocks.MAGNET.get()));
   public static final DeferredItem<BlockItem> MOB_MAGNET = REGISTER.register("mob_magnet", () -> new ItemMagnetBlock((Block)ModBlocks.MOB_MAGNET.get()));
   public static final DeferredItem<BlockItem> SILVERWOOD_LOG_NODE = registerBlock("silverwood_log_node", ModBlocks.SILVERWOOD_LOG_NODE);
   public static final DeferredItem<BlockItem> AURA_NODE = registerBlock("aura_node", ModBlocks.AURA_NODE, ItemAuraNode::new);
   public static final DeferredItem<BlockItem> PRIMAL_16_AURA_NODE = registerBlock(
      "primal_16_aura_node", ModBlocks.AURA_NODE, PresetAuraNodeItem::primalSixteen
   );
   public static final DeferredItem<BlockItem> PRIMAL_256_AURA_NODE = registerBlock(
      "primal_256_aura_node", ModBlocks.AURA_NODE, PresetAuraNodeItem::primalTwoFiftySix
   );
   public static final DeferredItem<BlockItem> NITOR = registerBlock("nitor", ModBlocks.NITOR);
   public static final DeferredItem<Item> BRIGHT_NITOR = REGISTER.register("bright_nitor", () -> new ItemBrightNitor());
   public static final DeferredItem<BlockItem> AIR_CRYSTAL_CLUSTER = registerBlock(
      "air_crystal_cluster", ModBlocks.AIR_CRYSTAL_CLUSTER, ItemCrystalCluster::new
   );
   public static final DeferredItem<BlockItem> FIRE_CRYSTAL_CLUSTER = registerBlock(
      "fire_crystal_cluster", ModBlocks.FIRE_CRYSTAL_CLUSTER, ItemCrystalCluster::new
   );
   public static final DeferredItem<BlockItem> WATER_CRYSTAL_CLUSTER = registerBlock(
      "water_crystal_cluster", ModBlocks.WATER_CRYSTAL_CLUSTER, ItemCrystalCluster::new
   );
   public static final DeferredItem<BlockItem> EARTH_CRYSTAL_CLUSTER = registerBlock(
      "earth_crystal_cluster", ModBlocks.EARTH_CRYSTAL_CLUSTER, ItemCrystalCluster::new
   );
   public static final DeferredItem<BlockItem> ORDER_CRYSTAL_CLUSTER = registerBlock(
      "order_crystal_cluster", ModBlocks.ORDER_CRYSTAL_CLUSTER, ItemCrystalCluster::new
   );
   public static final DeferredItem<BlockItem> ENTROPY_CRYSTAL_CLUSTER = registerBlock(
      "entropy_crystal_cluster", ModBlocks.ENTROPY_CRYSTAL_CLUSTER, ItemCrystalCluster::new
   );
   public static final DeferredItem<BlockItem> MIXED_CRYSTAL_CLUSTER = registerBlock(
      "mixed_crystal_cluster", ModBlocks.MIXED_CRYSTAL_CLUSTER, ItemCrystalCluster::new
   );
   public static final DeferredItem<BlockItem> STRANGE_CRYSTALS = registerBlock("strange_crystals", ModBlocks.STRANGE_CRYSTALS);
   public static final DeferredItem<BlockItem> AIR_CRYSTAL = registerBlock("air_crystal", ModBlocks.AIR_CRYSTAL);
   public static final DeferredItem<BlockItem> FIRE_CRYSTAL = registerBlock("fire_crystal", ModBlocks.FIRE_CRYSTAL);
   public static final DeferredItem<BlockItem> WATER_CRYSTAL = registerBlock("water_crystal", ModBlocks.WATER_CRYSTAL);
   public static final DeferredItem<BlockItem> EARTH_CRYSTAL = registerBlock("earth_crystal", ModBlocks.EARTH_CRYSTAL);
   public static final DeferredItem<BlockItem> ORDER_CRYSTAL = registerBlock("order_crystal", ModBlocks.ORDER_CRYSTAL);
   public static final DeferredItem<BlockItem> ENTROPY_CRYSTAL = registerBlock("entropy_crystal", ModBlocks.ENTROPY_CRYSTAL);
   public static final DeferredItem<BlockItem> AMBER_BLOCK = registerBlock("amber_block", ModBlocks.AMBER_BLOCK);
   public static final DeferredItem<BlockItem> AMBER_BRICKS = registerBlock("amber_bricks", ModBlocks.AMBER_BRICKS);
   public static final DeferredItem<BlockItem> WARDED_GLASS = registerBlock("warded_glass", ModBlocks.WARDED_GLASS);
   public static final DeferredItem<BlockItem> OBSIDIAN_TOTEM = registerBlock("obsidian_totem", ModBlocks.OBSIDIAN_TOTEM);
   public static final DeferredItem<BlockItem> OBSIDIAN_TILE = registerBlock("obsidian_tile", ModBlocks.OBSIDIAN_TILE);
   public static final DeferredItem<BlockItem> TRAVEL_STONE = registerBlock("travel_stone", ModBlocks.TRAVEL_STONE);
   public static final DeferredItem<BlockItem> TRAVEL_STONE_SLAB = registerBlock("travel_stone_slab", ModBlocks.TRAVEL_STONE_SLAB);
   public static final DeferredItem<BlockItem> TRAVEL_STONE_STAIRS = registerBlock("travel_stone_stairs", ModBlocks.TRAVEL_STONE_STAIRS);
   public static final DeferredItem<BlockItem> DARK_QUARTZ_BLOCK = registerBlock("dark_quartz_block", ModBlocks.DARK_QUARTZ_BLOCK);
   public static final DeferredItem<BlockItem> CHISELED_DARK_QUARTZ_BLOCK = registerBlock("chiseled_dark_quartz_block", ModBlocks.CHISELED_DARK_QUARTZ_BLOCK);
   public static final DeferredItem<BlockItem> DARK_QUARTZ_PILLAR = registerBlock("dark_quartz_pillar", ModBlocks.DARK_QUARTZ_PILLAR);
   public static final DeferredItem<BlockItem> DARK_QUARTZ_SLAB = registerBlock("dark_quartz_slab", ModBlocks.DARK_QUARTZ_SLAB);
   public static final DeferredItem<BlockItem> DARK_QUARTZ_STAIRS = registerBlock("dark_quartz_stairs", ModBlocks.DARK_QUARTZ_STAIRS);
   public static final DeferredItem<BlockItem> WARDING_STONE = registerBlock("warding_stone", ModBlocks.WARDING_STONE);
   public static final DeferredItem<BlockItem> PLATFORM = registerBlock("platform", ModBlocks.PLATFORM);
   public static final DeferredItem<BlockItem> THAUMIUM_BLOCK = registerBlock("thaumium_block", ModBlocks.THAUMIUM_BLOCK);
   public static final DeferredItem<BlockItem> ARCANE_STONE = registerBlock("arcane_stone", ModBlocks.ARCANE_STONE);
   public static final DeferredItem<BlockItem> REPAIRER = registerBlock("repairer", ModBlocks.REPAIRER, RepairerBlockItem::new);
   public static final DeferredItem<BlockItem> ARCANE_BRICKS = registerBlock("arcane_bricks", ModBlocks.ARCANE_BRICKS);
   public static final DeferredItem<BlockItem> CHARGED_OBSIDIAN_TOTEM = registerBlock("charged_obsidian_totem", ModBlocks.CHARGED_OBSIDIAN_TOTEM);
   public static final DeferredItem<BlockItem> GOLEM_FETTER = registerBlock("golem_fetter", ModBlocks.GOLEM_FETTER);
   public static final DeferredItem<BlockItem> ANCIENT_STONE = registerBlock("ancient_stone", ModBlocks.ANCIENT_STONE);
   public static final DeferredItem<BlockItem> ANCIENT_ROCK = registerBlock("ancient_rock", ModBlocks.ANCIENT_ROCK);
   public static final DeferredItem<BlockItem> CRUSTED_STONE = registerBlock("crusted_stone", ModBlocks.CRUSTED_STONE);
   public static final DeferredItem<BlockItem> ANCIENT_STONE_PEDESTAL = registerBlock("ancient_stone_pedestal", ModBlocks.ANCIENT_STONE_PEDESTAL);
   public static final DeferredItem<BlockItem> MAGIC_TALLOW_BLOCK = registerBlock("magic_tallow_block", ModBlocks.MAGIC_TALLOW_BLOCK);
   public static final DeferredItem<Item> MIRROR_ESSENCE = REGISTER.register("mirror_essence", () -> new ItemMirror((Block)ModBlocks.MIRROR_ESSENCE.get()));
   public static final DeferredItem<Item> MIRROR = REGISTER.register("mirror", () -> new ItemMirror((Block)ModBlocks.MIRROR.get()));
   public static final DeferredItem<BlockItem> TAINT_CRUST = registerBlock("taint_crust", ModBlocks.TAINT_CRUST);
   public static final DeferredItem<BlockItem> TAINT_SOIL = registerBlock("taint_soil", ModBlocks.TAINT_SOIL);
   public static final DeferredItem<BlockItem> FLESH_BLOCK = registerBlock("flesh_block", ModBlocks.FLESH_BLOCK);
   public static final DeferredItem<BlockItem> FIBROUS_TAINT = registerBlock("fibrous_taint", ModBlocks.FIBROUS_TAINT);
   public static final DeferredItem<BlockItem> TAINTED_GRASS = registerBlock("tainted_grass", ModBlocks.TAINTED_GRASS);
   public static final DeferredItem<BlockItem> TAINTED_PLANT = registerBlock("tainted_plant", ModBlocks.TAINTED_PLANT);
   public static final DeferredItem<BlockItem> SPORE_STALK = registerBlock("spore_stalk", ModBlocks.SPORE_STALK);
   public static final DeferredItem<BlockItem> MATURE_SPORE_STALK = registerBlock("mature_spore_stalk", ModBlocks.MATURE_SPORE_STALK);
   public static final DeferredItem<BlockItem> MANA_BEAN = registerBlock("mana_bean", ModBlocks.MANA_BEAN, ItemManaBean::new);
   public static final DeferredItem<BlockItem> ESSENTIA_TUBE = REGISTER.register(
      "essence_tube", () -> new ItemTube((Block)ModBlocks.ESSENCE_TUBE.get(), new Properties())
   );
   public static final DeferredItem<BlockItem> ESSENTIA_VALVE = REGISTER.register(
      "essence_valve", () -> new ItemTube((Block)ModBlocks.ESSENCE_VALVE.get(), new Properties())
   );
   public static final DeferredItem<BlockItem> FILTERED_ESSENTIA_TUBE = REGISTER.register(
      "filtered_essence_tube", () -> new ItemTube((Block)ModBlocks.FILTERED_ESSENCE_TUBE.get(), new Properties())
   );
   public static final DeferredItem<BlockItem> ESSENTIA_BUFFER = REGISTER.register(
      "essence_buffer", () -> new ItemTube((Block)ModBlocks.ESSENCE_BUFFER.get(), new Properties())
   );
   public static final DeferredItem<BlockItem> RESTRICTED_ESSENTIA_TUBE = REGISTER.register(
      "restricted_essence_tube", () -> new ItemTube((Block)ModBlocks.RESTRICTED_ESSENCE_TUBE.get(), new Properties())
   );
   public static final DeferredItem<BlockItem> DIRECTIONAL_ESSENTIA_TUBE = REGISTER.register(
      "directional_essence_tube", () -> new ItemTube((Block)ModBlocks.DIRECTIONAL_ESSENCE_TUBE.get(), new Properties())
   );
   public static final DeferredItem<BlockItem> ESSENTIA_CRYSTALLIZER = registerBlock("essence_crystallizer", ModBlocks.ESSENCE_CRYSTALLIZER);
   public static final DeferredItem<BlockItem> MAGIC_BOX = registerBlock("magic_box", ModBlocks.MAGIC_BOX);
   public static final DeferredItem<BlockItem> WARP_GATE = registerBlock("warp_gate", ModBlocks.WARP_GATE);
   public static final DeferredItem<BlockItem> ALCHEMICAL_CENTRIFUGE = registerBlock("alchemical_centrifuge", ModBlocks.ALCHEMICAL_CENTRIFUGE);
   public static final DeferredItem<BlockItem> ELDRITCH_ALTAR = registerBlock("eldritch_altar", ModBlocks.ELDRITCH_ALTAR);
   public static final DeferredItem<BlockItem> BEDROCK_PORTAL = registerBlock("bedrock_portal", ModBlocks.BEDROCK_PORTAL);
   public static final DeferredItem<BlockItem> GLOWING_CRUSTED_STONE = registerBlock("glowing_crusted_stone", ModBlocks.GLOWING_CRUSTED_STONE);
   public static final DeferredItem<BlockItem> GLYPHED_STONE = registerBlock("glyphed_stone", ModBlocks.GLYPHED_STONE);
   public static final DeferredItem<BlockItem> ANCIENT_DOORWAY = registerBlock("ancient_doorway", ModBlocks.ANCIENT_DOORWAY);
   public static final DeferredItem<BlockItem> ANCIENT_LOCKING_MECHANISM = registerBlock("ancient_locking_mechanism", ModBlocks.ANCIENT_LOCKING_MECHANISM);
   public static final DeferredItem<BlockItem> RUNED_STONE = registerBlock("runed_stone", ModBlocks.RUNED_STONE);
   public static final DeferredItem<BlockItem> ESSENCE_RESERVOIR = REGISTER.register(
      "essence_reservoir", () -> new ItemEssenceReservoir((Block)ModBlocks.ESSENCE_RESERVOIR.get(), new Properties())
   );
   public static final DeferredItem<BlockItem> ARCANE_STONE_SLAB = registerBlock("arcane_stone_slab", ModBlocks.ARCANE_STONE_SLAB);
   public static final DeferredItem<BlockItem> ARCANE_STONE_STAIRS = registerBlock("arcane_stone_stairs", ModBlocks.ARCANE_STONE_STAIRS);
   public static final DeferredItem<BlockItem> ELDRITCH_STONE_SLAB = registerBlock("eldritch_stone_slab", ModBlocks.ELDRITCH_STONE_SLAB);
   public static final DeferredItem<BlockItem> ELDRITCH_STONE_STAIRS = registerBlock("eldritch_stone_stairs", ModBlocks.ELDRITCH_STONE_STAIRS);
   public static final DeferredItem<BlockItem> LOOT_URN = registerBlock("loot_urn", ModBlocks.LOOT_URN);
   public static final DeferredItem<BlockItem> LOOT_URN_UNCOMMON = registerBlock("loot_urn_uncommon", ModBlocks.LOOT_URN_UNCOMMON);
   public static final DeferredItem<BlockItem> LOOT_URN_RARE = registerBlock("loot_urn_rare", ModBlocks.LOOT_URN_RARE);
   public static final DeferredItem<BlockItem> LOOT_CRATE = registerBlock("loot_crate", ModBlocks.LOOT_CRATE);
   public static final DeferredItem<BlockItem> LOOT_CRATE_UNCOMMON = registerBlock("loot_crate_uncommon", ModBlocks.LOOT_CRATE_UNCOMMON);
   public static final DeferredItem<BlockItem> LOOT_CRATE_RARE = registerBlock("loot_crate_rare", ModBlocks.LOOT_CRATE_RARE);
   public static final DeferredItem<Item> INFUSION_CLAW = REGISTER.register(
      "infusion_claw", () -> new ItemInfusionClaw((Block)ModBlocks.INFUSION_CLAW.get(), new Properties())
   );
   public static final DeferredItem<ItemWandCasting> WAND_CASTING = REGISTER.register("wand_casting", () -> new ItemWandCasting());
   public static final DeferredItem<WandChargerItemUpgrade> WAND_CHARGER_VIS_BUFFER = REGISTER.register(
      "wand_charger_vis_buffer", () -> new WandChargerItemUpgrade(WandChargerItemUpgrade.Type.VIS_BUFFER)
   );
   public static final DeferredItem<WandChargerItemUpgrade> WAND_CHARGER_CAP_CHARGE = REGISTER.register(
      "wand_charger_cap_charge", () -> new WandChargerItemUpgrade(WandChargerItemUpgrade.Type.CAP_CHARGE)
   );
   public static final DeferredItem<WandChargerItemUpgrade> WAND_CHARGER_SPEED_CHARGE = REGISTER.register(
      "wand_charger_speed_charge", () -> new WandChargerItemUpgrade(WandChargerItemUpgrade.Type.SPEED_CHARGE)
   );
   public static final DeferredItem<WandChargerItemUpgrade> WAND_CHARGER_ALLOW_STAFF = REGISTER.register(
      "wand_charger_allow_staff", () -> new WandChargerItemUpgrade(WandChargerItemUpgrade.Type.ALLOW_STAFF)
   );
   public static final DeferredItem<ItemWandCap> IRON_WAND_CAP = REGISTER.register("wand_cap_iron", () -> new ItemWandCap("iron", 1.1F, 1));
   public static final DeferredItem<ItemWandCap> GOLD_WAND_CAP = REGISTER.register("wand_cap_gold", () -> new ItemWandCap("gold", 1.0F, 3));
   public static final DeferredItem<ItemWandCap> COPPER_WAND_CAP = REGISTER.register(
      "wand_cap_copper", () -> new ItemWandCap("copper", 1.1F, Arrays.asList(Aspect.ORDER, Aspect.ENTROPY), 1.0F, 2)
   );
   public static final DeferredItem<Item> SILVER_INERT_WAND_CAP = REGISTER.register("wand_cap_silver_inert", () -> defaultItem());
   public static final DeferredItem<ItemWandCap> SILVER_WAND_CAP = REGISTER.register(
      "wand_cap_silver", () -> new ItemWandCap("silver", 1.0F, Arrays.asList(Aspect.AIR, Aspect.EARTH, Aspect.FIRE, Aspect.WATER), 0.95F, 4)
   );
   public static final DeferredItem<Item> THAUMIUM_INERT_WAND_CAP = REGISTER.register("wand_cap_thaumium_inert", () -> defaultItem());
   public static final DeferredItem<ItemWandCap> THAUMIUM_WAND_CAP = REGISTER.register("wand_cap_thaumium", () -> new ItemWandCap("thaumium", 0.9F, 6));
   public static final DeferredItem<Item> VOID_INERT_WAND_CAP = REGISTER.register("wand_cap_void_inert", () -> defaultItem());
   public static final DeferredItem<ItemWandCap> VOID_WAND_CAP = REGISTER.register("wand_cap_void", () -> new ItemWandCap("void", 0.8F, 9));
   public static final DeferredItem<ItemWandCap> ICHOR_WAND_CAP = REGISTER.register("wand_cap_ichor", () -> new ItemWandCap("ichor", 0.7F, 10));
   public static final DeferredItem<ItemWandRod> WOOD_WAND_ROD = REGISTER.register("wand_rod_wood", () -> new ItemWandRod("wood", 25, 1));
   public static final DeferredItem<ItemWandRod> GREATWOOD_WAND_ROD = REGISTER.register("wand_rod_greatwood", () -> new ItemWandRod("greatwood", 50, 3));
   public static final DeferredItem<ItemWandRod> SILVERWOOD_WAND_ROD = REGISTER.register("wand_rod_silverwood", () -> new ItemWandRod("silverwood", 100, 9));
   public static final DeferredItem<ItemWandRod> OBSIDIAN_WAND_ROD = REGISTER.register(
      "wand_rod_obsidian", () -> new ItemWandRod("obsidian", 75, 6, new WandRodPrimalOnUpdate(Aspect.EARTH))
   );
   public static final DeferredItem<ItemWandRod> ICE_WAND_ROD = REGISTER.register(
      "wand_rod_ice", () -> new ItemWandRod("ice", 75, 6, new WandRodPrimalOnUpdate(Aspect.WATER))
   );
   public static final DeferredItem<ItemWandRod> QUARTZ_WAND_ROD = REGISTER.register(
      "wand_rod_quartz", () -> new ItemWandRod("quartz", 75, 6, new WandRodPrimalOnUpdate(Aspect.ORDER))
   );
   public static final DeferredItem<ItemWandRod> REED_WAND_ROD = REGISTER.register(
      "wand_rod_reed", () -> new ItemWandRod("reed", 75, 6, new WandRodPrimalOnUpdate(Aspect.AIR))
   );
   public static final DeferredItem<ItemWandRod> BLAZE_WAND_ROD = REGISTER.register(
      "wand_rod_blaze", () -> new ItemWandRod("blaze", 75, 6, new WandRodPrimalOnUpdate(Aspect.FIRE)).glowing()
   );
   public static final DeferredItem<ItemWandRod> BONE_WAND_ROD = REGISTER.register(
      "wand_rod_bone", () -> new ItemWandRod("bone", 75, 6, new WandRodPrimalOnUpdate(Aspect.ENTROPY))
   );
   public static final DeferredItem<ItemWandRod> ICHOR_WAND_ROD = REGISTER.register("wand_rod_ichor", () -> new ItemWandRod("ichor", 1000, 10).glowing());
   public static final DeferredItem<ItemWandRod> GREATWOOD_STAFF_ROD = REGISTER.register(
      "staff_rod_greatwood", () -> new ItemWandRod("greatwood_staff", 125, 8).staff()
   );
   public static final DeferredItem<ItemWandRod> SILVERWOOD_STAFF_ROD = REGISTER.register(
      "staff_rod_silverwood", () -> new ItemWandRod("silverwood_staff", 250, 24).staff()
   );
   public static final DeferredItem<ItemWandRod> OBSIDIAN_STAFF_ROD = REGISTER.register(
      "staff_rod_obsidian", () -> new ItemWandRod("obsidian_staff", 175, 14, new WandRodPrimalOnUpdate(Aspect.EARTH)).staff()
   );
   public static final DeferredItem<ItemWandRod> ICE_STAFF_ROD = REGISTER.register(
      "staff_rod_ice", () -> new ItemWandRod("ice_staff", 175, 14, new WandRodPrimalOnUpdate(Aspect.WATER)).staff()
   );
   public static final DeferredItem<ItemWandRod> QUARTZ_STAFF_ROD = REGISTER.register(
      "staff_rod_quartz", () -> new ItemWandRod("quartz_staff", 175, 14, new WandRodPrimalOnUpdate(Aspect.ORDER)).staff()
   );
   public static final DeferredItem<ItemWandRod> REED_STAFF_ROD = REGISTER.register(
      "staff_rod_reed", () -> new ItemWandRod("reed_staff", 175, 14, new WandRodPrimalOnUpdate(Aspect.AIR)).staff()
   );
   public static final DeferredItem<ItemWandRod> BLAZE_STAFF_ROD = REGISTER.register(
      "staff_rod_blaze", () -> new ItemWandRod("blaze_staff", 175, 14, new WandRodPrimalOnUpdate(Aspect.FIRE)).staff().glowing()
   );
   public static final DeferredItem<ItemWandRod> BONE_STAFF_ROD = REGISTER.register(
      "staff_rod_bone", () -> new ItemWandRod("bone_staff", 175, 14, new WandRodPrimalOnUpdate(Aspect.ENTROPY)).staff()
   );
   public static final DeferredItem<ItemWandRod> PRIMAL_STAFF_ROD = REGISTER.register(
      "staff_rod_primal", () -> new ItemWandRod("primal_staff", 250, 32, new WandRodPrimalOnUpdate()).staff().runes()
   );
   public static final DeferredItem<Item> FOCUS_POUCH = REGISTER.register("focus_pouch", () -> new ItemFocusPouchBauble());
   public static final DeferredItem<Item> ICHOR_POUCH = REGISTER.register("ichor_pouch", () -> new ItemIchorPouch());
   public static final DeferredItem<Item> FOCUS_FIRE = REGISTER.register("focus_fire", () -> new ItemFocusFire());
   public static final DeferredItem<Item> FOCUS_SHOCK = REGISTER.register("focus_shock", () -> new ItemFocusShock());
   public static final DeferredItem<Item> FOCUS_HELL_BAT = REGISTER.register("focus_hell_bat", () -> new ItemFocusHellbat());
   public static final DeferredItem<Item> FOCUS_FROST = REGISTER.register("focus_frost", () -> new ItemFocusFrost());
   public static final DeferredItem<Item> FOCUS_TRADE = REGISTER.register("focus_trade", () -> new ItemFocusTrade());
   public static final DeferredItem<Item> FOCUS_EXCAVATION = REGISTER.register("focus_excavation", () -> new ItemFocusExcavation());
   public static final DeferredItem<Item> FOCUS_PORTABLE_HOLE = REGISTER.register("focus_portable_hole", () -> new ItemFocusPortableHole());
   public static final DeferredItem<Item> FOCUS_PECH = REGISTER.register("focus_pech", () -> new ItemFocusPech());
   public static final DeferredItem<Item> FOCUS_WARDING = REGISTER.register("focus_warding", () -> new ItemFocusWarding());
   public static final DeferredItem<ItemFocusPrimal> FOCUS_PRIMAL = REGISTER.register("focus_primal", () -> new ItemFocusPrimal());
   public static final DeferredItem<Item> FOCUS_TELEKINESIS = REGISTER.register("focus_telekinesis", () -> new ItemFocusTelekinesis());
   public static final DeferredItem<Item> FOCUS_SMELT = REGISTER.register("focus_smelt", () -> new ItemFocusSmelt());
   public static final DeferredItem<Item> FOCUS_HEAL = REGISTER.register("focus_heal", () -> new ItemFocusHeal());
   public static final DeferredItem<Item> FOCUS_FLIGHT = REGISTER.register("focus_flight", () -> new ItemFocusFlight());
   public static final DeferredItem<Item> FOCUS_ENDER_CHEST = REGISTER.register("focus_ender_chest", () -> new ItemFocusEnderChest());
   public static final DeferredItem<Item> FOCUS_DEFLECT = REGISTER.register("focus_deflect", () -> new ItemFocusDeflect());
   public static final DeferredItem<Item> FOCUS_XP_DRAIN = REGISTER.register("focus_xp_drain", () -> new ItemFocusXPDrain());
   public static final DeferredItem<Item> FOCUS_SHADOWBEAM = REGISTER.register("focus_shadow_beam", () -> new ItemFocusShadowbeam());
   public static final DeferredItem<Item> FOCUS_RECALL = REGISTER.register("focus_recall", () -> new ItemFocusRecall());
   public static final DeferredItem<Item> PHIAL_ESSENCE = REGISTER.register("phial_essence", () -> new ItemPhialEssence());
   public static final DeferredItem<Item> WISP_ESSENCE = REGISTER.register("wisp_essence", () -> new ItemWispEssence());
   public static final DeferredItem<Item> CRYSTAL_ESSENCE = REGISTER.register("crystal_essence", () -> new ItemCrystalEssence());
   public static final DeferredItem<Item> ALUMENTUM = REGISTER.register("alumentum", () -> new AlumentumItem());
   public static final DeferredItem<Item> THAUMIUM_INGOT = REGISTER.register("thaumium_ingot", () -> defaultItem());
   public static final DeferredItem<Item> ICHOR = REGISTER.register("ichor", () -> defaultItem());
   public static final DeferredItem<Item> ICHORIUM = REGISTER.register("ichorium", () -> defaultItem());
   public static final DeferredItem<Item> ICHORIUM_NUGGET = REGISTER.register("ichorium_nugget", () -> defaultItem());
   public static final DeferredItem<Item> QUICKSILVER = REGISTER.register("quicksilver", () -> defaultItem());
   public static final DeferredItem<Item> MAGIC_TALLOW = REGISTER.register("magic_tallow", () -> defaultItem());
   public static final DeferredItem<Item> AMBER = REGISTER.register("amber", () -> defaultItem());
   public static final DeferredItem<Item> DARK_QUARTZ = REGISTER.register("dark_quartz", () -> defaultItem());
   public static final DeferredItem<Item> CLOTH = REGISTER.register("cloth", () -> defaultItem());
   public static final DeferredItem<Item> SPELL_CLOTH = REGISTER.register("spell_cloth", () -> new ItemSpellCloth());
   public static final DeferredItem<Item> ICHOR_CLOTH = REGISTER.register("ichor_cloth", () -> defaultItem());
   public static final DeferredItem<Item> VIS_FILTER = REGISTER.register("vis_filter", () -> defaultItem());
   public static final DeferredItem<Item> KNOWLEDGE_FRAGMENT = REGISTER.register("knowledge_fragment", () -> new KnowledgeFragmentItem());
   public static final DeferredItem<Item> MIRROR_GLASS = REGISTER.register("mirror_glass", () -> defaultItem());
   public static final DeferredItem<Item> TAINT_SLIME = REGISTER.register("taint_slime", () -> new TaintItem());
   public static final DeferredItem<Item> TAINT_TENDRIL = REGISTER.register("taint_tendril", () -> new TaintItem());
   public static final DeferredItem<Item> JAR_LABEL = REGISTER.register("jar_label", () -> new JarLabelItem());
   public static final DeferredItem<Item> SALIS_MUNDUS = REGISTER.register("salis_mundus", () -> defaultItem());
   public static final DeferredItem<Item> PRIMAL_CHARM = REGISTER.register("primal_charm", () -> new PrimalCharmItem());
   public static final DeferredItem<Item> VOID_METAL_INGOT = REGISTER.register("void_metal_ingot", () -> defaultItem());
   public static final DeferredItem<Item> VOID_SEED = REGISTER.register("void_seed", () -> new CurioItem());
   public static final DeferredItem<Item> GOLD_COIN = REGISTER.register("gold_coin", () -> new CurioItem());
   public static final DeferredItem<Item> AIR_SHARD = REGISTER.register("air_shard", () -> new ItemShard(0));
   public static final DeferredItem<Item> FIRE_SHARD = REGISTER.register("fire_shard", () -> new ItemShard(1));
   public static final DeferredItem<Item> WATER_SHARD = REGISTER.register("water_shard", () -> new ItemShard(2));
   public static final DeferredItem<Item> EARTH_SHARD = REGISTER.register("earth_shard", () -> new ItemShard(3));
   public static final DeferredItem<Item> ORDER_SHARD = REGISTER.register("order_shard", () -> new ItemShard(4));
   public static final DeferredItem<Item> ENTROPY_SHARD = REGISTER.register("entropy_shard", () -> new ItemShard(5));
   public static final DeferredItem<Item> BALANCED_SHARD = REGISTER.register("balanced_shard", () -> new ItemShard(6));
   public static final DeferredItem<Item> NETHER_SHARD = REGISTER.register("nether_shard", () -> defaultItem());
   public static final DeferredItem<Item> ENDER_SHARD = REGISTER.register("ender_shard", () -> defaultItem());
   public static final DeferredItem<Item> COPPER_NUGGET = REGISTER.register("copper_nugget", () -> defaultItem());
   public static final DeferredItem<Item> QUICKSILVER_NUGGET = REGISTER.register("quicksilver_nugget", () -> defaultItem());
   public static final DeferredItem<Item> THAUMIUM_NUGGET = REGISTER.register("thaumium_nugget", () -> defaultItem());
   public static final DeferredItem<Item> VOID_METAL_NUGGET = REGISTER.register("void_metal_nugget", () -> defaultItem());
   public static final DeferredItem<Item> IRON_CLUSTER = REGISTER.register("iron_cluster", () -> defaultItem());
   public static final DeferredItem<Item> COPPER_CLUSTER = REGISTER.register("copper_cluster", () -> defaultItem());
   public static final DeferredItem<Item> TIN_CLUSTER = REGISTER.register("tin_cluster", () -> defaultItem());
   public static final DeferredItem<Item> SILVER_CLUSTER = REGISTER.register("silver_cluster", () -> defaultItem());
   public static final DeferredItem<Item> LEAD_CLUSTER = REGISTER.register("lead_cluster", () -> defaultItem());
   public static final DeferredItem<Item> CINNABAR_CLUSTER = REGISTER.register("cinnabar_cluster", () -> defaultItem());
   public static final DeferredItem<Item> GOLD_CLUSTER = REGISTER.register("gold_cluster", () -> defaultItem());
   public static final DeferredItem<Item> CHICKEN_NUGGET = REGISTER.register(
      "chicken_nugget", () -> new thaumcraft.item.consumables.ItemNuggetEdible(new Properties().food(new Builder().nutrition(1).saturationModifier(0.3F).build()))
   );
   public static final DeferredItem<Item> BEEF_NUGGET = REGISTER.register(
      "beef_nugget", () -> new thaumcraft.item.consumables.ItemNuggetEdible(new Properties().food(new Builder().nutrition(1).saturationModifier(0.3F).build()))
   );
   public static final DeferredItem<Item> PORK_NUGGET = REGISTER.register(
      "pork_nugget", () -> new thaumcraft.item.consumables.ItemNuggetEdible(new Properties().food(new Builder().nutrition(1).saturationModifier(0.3F).build()))
   );
   public static final DeferredItem<Item> FISH_NUGGET = REGISTER.register(
      "fish_nugget", () -> new thaumcraft.item.consumables.ItemNuggetEdible(new Properties().food(new Builder().nutrition(1).saturationModifier(0.3F).build()))
   );
   public static final DeferredItem<Item> TRIPLE_MEAT_TREAT = REGISTER.register(
      "triple_meat_treat",
      () -> new Item(
         new Properties()
            .food(
               new Builder()
                  .nutrition(6)
                  .saturationModifier(0.8F)
                  .alwaysEdible()
                  .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 5, 0), 0.66F)
                  .build()
            )
      )
   );
   public static final DeferredItem<Item> ZOMBIE_BRAIN = REGISTER.register("zombie_brain", () -> new ItemZombieBrain());
   public static final DeferredItem<Item> RESEARCH_NOTES = REGISTER.register("research_notes", () -> new ItemResearchNotes());
   public static final DeferredItem<Item> INKWELL = REGISTER.register("inkwell", () -> new InkWell());
   public static final DeferredItem<ItemThaumometer> THAUMOMETER = REGISTER.register("thaumometer", () -> new ItemThaumometer());
   public static final DeferredItem<SymmetryCalculatorItem> SYMMETRY_CALCULATOR = REGISTER.register(
      "symmetry_calculator", () -> new SymmetryCalculatorItem(new Item.Properties())
   );
   public static final DeferredItem<Item> GOLEM_STRAW = REGISTER.register("golem_straw", () -> new ItemGolemPlacer(EnumGolemType.STRAW));
   public static final DeferredItem<Item> GOLEM_WOOD = REGISTER.register("golem_wood", () -> new ItemGolemPlacer(EnumGolemType.WOOD));
   public static final DeferredItem<Item> GOLEM_TALLOW = REGISTER.register("golem_tallow", () -> new ItemGolemPlacer(EnumGolemType.TALLOW));
   public static final DeferredItem<Item> GOLEM_CLAY = REGISTER.register("golem_clay", () -> new ItemGolemPlacer(EnumGolemType.CLAY));
   public static final DeferredItem<Item> GOLEM_FLESH = REGISTER.register("golem_flesh", () -> new ItemGolemPlacer(EnumGolemType.FLESH));
   public static final DeferredItem<Item> GOLEM_STONE = REGISTER.register("golem_stone", () -> new ItemGolemPlacer(EnumGolemType.STONE));
   public static final DeferredItem<Item> GOLEM_IRON = REGISTER.register("golem_iron", () -> new ItemGolemPlacer(EnumGolemType.IRON));
   public static final DeferredItem<Item> GOLEM_THAUMIUM = REGISTER.register("golem_thaumium", () -> new ItemGolemPlacer(EnumGolemType.THAUMIUM));
   public static final DeferredItem<Item> GOLEM_BELL = REGISTER.register("iron_bell", () -> new ItemGolemBell());
   public static final DeferredItem<Item> PLACEMENT_MIRROR = REGISTER.register("placement_mirror", () -> new ItemPlacementMirror());
   public static final DeferredItem<Item> GOLEM_DECOR_TOP_HAT = REGISTER.register(
      "golem_decor_top_hat", () -> new ItemGolemDecoration(GolemDecorationType.TOP_HAT)
   );
   public static final DeferredItem<Item> GOLEM_DECOR_GOGGLES = REGISTER.register(
      "golem_decor_googles", () -> new ItemGolemDecoration(GolemDecorationType.GOGGLES)
   );
   public static final DeferredItem<Item> GOLEM_DECOR_BOWTIE = REGISTER.register(
      "golem_decor_bowtie", () -> new ItemGolemDecoration(GolemDecorationType.BOWTIE)
   );
   public static final DeferredItem<Item> GOLEM_DECOR_FEZ = REGISTER.register("golem_decor_fez", () -> new ItemGolemDecoration(GolemDecorationType.FEZ));
   public static final DeferredItem<Item> GOLEM_DECOR_DART = REGISTER.register("golem_decor_dart", () -> new ItemGolemDecoration(GolemDecorationType.DART));
   public static final DeferredItem<Item> GOLEM_DECOR_VISOR = REGISTER.register("golem_decor_visor", () -> new ItemGolemDecoration(GolemDecorationType.VISOR));
   public static final DeferredItem<Item> GOLEM_DECOR_ARMOR = REGISTER.register("golem_decor_armor", () -> new ItemGolemDecoration(GolemDecorationType.ARMOR));
   public static final DeferredItem<Item> GOLEM_DECOR_MACE = REGISTER.register("golem_decor_mace", () -> new ItemGolemDecoration(GolemDecorationType.MACE));
   public static final DeferredItem<Item> GOLEM_CORE_FILL = REGISTER.register("golem_core_fill", () -> new ItemGolemCore(GolemCore.FILL));
   public static final DeferredItem<Item> GOLEM_CORE_EMPTY = REGISTER.register("golem_core_empty", () -> new ItemGolemCore(GolemCore.EMPTY));
   public static final DeferredItem<Item> GOLEM_CORE_GATHER = REGISTER.register("golem_core_gather", () -> new ItemGolemCore(GolemCore.GATHER));
   public static final DeferredItem<Item> GOLEM_CORE_HARVEST = REGISTER.register("golem_core_harvest", () -> new ItemGolemCore(GolemCore.HARVEST));
   public static final DeferredItem<Item> GOLEM_CORE_GUARD = REGISTER.register("golem_core_guard", () -> new ItemGolemCore(GolemCore.GUARD));
   public static final DeferredItem<Item> GOLEM_CORE_LIQUID = REGISTER.register("golem_core_liquid", () -> new ItemGolemCore(GolemCore.LIQUID));
   public static final DeferredItem<Item> GOLEM_CORE_ESSENCE = REGISTER.register("golem_core_essence", () -> new ItemGolemCore(GolemCore.ESSENCE));
   public static final DeferredItem<Item> GOLEM_CORE_LUMBER = REGISTER.register("golem_core_lumber", () -> new ItemGolemCore(GolemCore.LUMBER));
   public static final DeferredItem<Item> GOLEM_CORE_USE = REGISTER.register("golem_core_use", () -> new ItemGolemCore(GolemCore.USE));
   public static final DeferredItem<Item> GOLEM_CORE_BUTCHER = REGISTER.register("golem_core_butcher", () -> new ItemGolemCore(GolemCore.BUTCHER));
   public static final DeferredItem<Item> GOLEM_CORE_SORTING = REGISTER.register("golem_core_sorting", () -> new ItemGolemCore(GolemCore.SORTING));
   public static final DeferredItem<Item> GOLEM_CORE_FISHING = REGISTER.register("golem_core_fishing", () -> new ItemGolemCore(GolemCore.FISHING));
   public static final DeferredItem<Item> GOLEM_CORE_BLANK = REGISTER.register("golem_core_blank", () -> new ItemGolemCore(GolemCore.BLANK));
   public static final DeferredItem<Item> GOLEM_UPGRADE_AIR = REGISTER.register("golem_upgrade_air", () -> new ItemGolemUpgrade(GolemUpgrade.AIR));
   public static final DeferredItem<Item> GOLEM_UPGRADE_EARTH = REGISTER.register("golem_upgrade_earth", () -> new ItemGolemUpgrade(GolemUpgrade.EARTH));
   public static final DeferredItem<Item> GOLEM_UPGRADE_FIRE = REGISTER.register("golem_upgrade_fire", () -> new ItemGolemUpgrade(GolemUpgrade.FIRE));
   public static final DeferredItem<Item> GOLEM_UPGRADE_WATER = REGISTER.register("golem_upgrade_water", () -> new ItemGolemUpgrade(GolemUpgrade.WATER));
   public static final DeferredItem<Item> GOLEM_UPGRADE_ORDER = REGISTER.register("golem_upgrade_order", () -> new ItemGolemUpgrade(GolemUpgrade.ORDER));
   public static final DeferredItem<Item> GOLEM_UPGRADE_ENTROPY = REGISTER.register("golem_upgrade_entropy", () -> new ItemGolemUpgrade(GolemUpgrade.ENTROPY));
   public static final DeferredItem<Item> IRON_KEY = REGISTER.register("iron_key", () -> new ItemKey());
   public static final DeferredItem<Item> GOLD_KEY = REGISTER.register("gold_key", () -> new ItemKey());
   public static final DeferredItem<Item> MIRROR_HAND = REGISTER.register("mirror_hand", () -> new ItemHandMirror());
   public static final DeferredItem<Item> TRUNK_SPAWNER = REGISTER.register("trunk_spawner", () -> new ItemTrunkSpawner());
   public static final DeferredItem<Item> RESONATOR = REGISTER.register("resonator", () -> new ItemResonator());
   public static final DeferredItem<Item> CONNECTOR = REGISTER.register("connector", () -> new ItemConnector());
   public static final DeferredItem<Item> BATH_SALTS = REGISTER.register("bath_salts", () -> new ItemBathSalts());
   public static final DeferredItem<Item> GAS_REMOVAL = REGISTER.register("gas_removal", () -> new ItemGasRemover());
   public static final DeferredItem<Item> GAS_SHADOW = REGISTER.register("gas_shadow", () -> new ItemGas());
   public static final DeferredItem<Item> GAS_LIGHT = REGISTER.register("gas_light", () -> new ItemGas((Block)ModBlocks.GASEOUS_LIGHT.get()));
   public static final DeferredItem<Item> ELDRITCH_EYE = REGISTER.register("eldritch_eye", () -> new EldritchObjectItem());
   public static final DeferredItem<Item> RUNE_TABLET = REGISTER.register("rune_tablet", () -> new RunedTabletItem());
   public static final DeferredItem<Item> PRIMORDIAL_PEARL = REGISTER.register("primordial_pearl", () -> new PrimordialPearlItem());
   public static final DeferredItem<Item> ELDRITCH_ALTAR_PLACER = REGISTER.register("eldritch_altar_placer", () -> new ItemEldritchAltarPlacer());
   public static final DeferredItem<Item> SKY_PEARL = REGISTER.register("sky_pearl", () -> new ItemSkyPearl());
   public static final DeferredItem<Item> SANITY_SOAP = REGISTER.register("sanity_soap", () -> new ItemSanitySoap());
   public static final DeferredItem<Item> SOUL_MOULD = REGISTER.register("soul_mould", () -> new ItemSoulMould());
   public static final DeferredItem<Item> SANITY_CHECKER = REGISTER.register("sanity_checker", () -> new ItemSanityChecker());
   public static final DeferredItem<Item> BOTTLE_TAINT = REGISTER.register("bottle_taint", () -> new ItemBottleTaint());
   public static final DeferredItem<Item> LOOT_BAG = REGISTER.register("loot_bag", () -> new ItemLootBag(Rarity.COMMON, 0));
   public static final DeferredItem<Item> LOOT_BAG_UNCOMMON = REGISTER.register("loot_bag_uncommon", () -> new ItemLootBag(Rarity.UNCOMMON, 1));
   public static final DeferredItem<Item> LOOT_BAG_RARE = REGISTER.register("loot_bag_rare", () -> new ItemLootBag(Rarity.RARE, 2));
   public static final DeferredItem<Item> CURIOS_RING = REGISTER.register("curios_ring", () -> new ItemBaubleBlanks());
   public static final DeferredItem<Item> CURIOS_RING_IRON = REGISTER.register("curios_ring_iron", () -> new ItemBaubleBlanks());
   public static final DeferredItem<Item> CURIOS_AMULET = REGISTER.register("curios_amulet", () -> new ItemBaubleBlanks());
   public static final DeferredItem<Item> CURIOS_BELT = REGISTER.register("curios_belt", () -> new ItemBaubleBlanks());
   public static final DeferredItem<Item> RUNIC_AMULET = REGISTER.register("runic_amulet", () -> new ItemAmuletRunic(8));
   public static final DeferredItem<Item> RUNIC_AMULET_EMERGENCY = REGISTER.register("runic_amulet_emergency", () -> new ItemAmuletRunic(7));
   public static final DeferredItem<Item> RUNIC_RING_LESSER = REGISTER.register("runic_ring_lesser", () -> new ItemRingRunic(1, Rarity.UNCOMMON));
   public static final DeferredItem<Item> RUNIC_RING = REGISTER.register("runic_ring", () -> new ItemRingRunic(5, Rarity.RARE));
   public static final DeferredItem<Item> RUNIC_RING_CHARGED = REGISTER.register("runic_ring_charged", () -> new ItemRingRunic(4, Rarity.RARE));
   public static final DeferredItem<Item> RUNIC_RING_REGEN = REGISTER.register("runic_ring_regen", () -> new ItemRingRunic(4, Rarity.RARE));
   public static final DeferredItem<Item> RUNIC_GIRDLE = REGISTER.register("runic_girdle", () -> new ItemGirdleRunic(10));
   public static final DeferredItem<Item> RUNIC_GIRDLE_KINETIC = REGISTER.register("runic_girdle_kinetic", () -> new ItemGirdleRunic(9));
   public static final DeferredItem<Item> GIRDLE_HOVER = REGISTER.register("girdle_hover", () -> new ItemGirdleHover());
   public static final DeferredItem<Item> VIS_AMULET_LESSER = REGISTER.register("vis_amulet_lesser", () -> new ItemAmuletVis(2500, Rarity.UNCOMMON));
   public static final DeferredItem<Item> VIS_AMULET = REGISTER.register("vis_amulet", () -> new ItemAmuletVis(25000, Rarity.RARE));
   public static final DeferredItem<Item> CAT_AMULET = REGISTER.register("cat_amulet", () -> new ItemCatAmulet());
   public static final DeferredItem<Item> BLOCK_TALISMAN = REGISTER.register("block_talisman", () -> new ItemBlockTalisman());
   public static final DeferredItem<Item> PROTOCLAY = REGISTER.register("protoclay", () -> new ItemProtoclay());
   public static final DeferredItem<Item> XP_TALISMAN = REGISTER.register("xp_talisman", () -> new ItemXPTalisman());
   public static final DeferredItem<Item> CLEANSING_TALISMAN = REGISTER.register("cleansing_talisman", () -> new ItemCleansingTalisman());
   public static final DeferredItem<Item> BONE_BOW = REGISTER.register("bone_bow", () -> new ItemBowBone());
   public static final DeferredItem<Item> ARROW_AIR = REGISTER.register("arrow_air", () -> new ArrowItem(new Properties()));
   public static final DeferredItem<Item> ARROW_FIRE = REGISTER.register("arrow_fire", () -> new ArrowItem(new Properties()));
   public static final DeferredItem<Item> ARROW_WATER = REGISTER.register("arrow_water", () -> new ArrowItem(new Properties()));
   public static final DeferredItem<Item> ARROW_EARTH = REGISTER.register("arrow_earth", () -> new ArrowItem(new Properties()));
   public static final DeferredItem<Item> ARROW_ORDER = REGISTER.register("arrow_order", () -> new ArrowItem(new Properties()));
   public static final DeferredItem<Item> ARROW_ENTROPY = REGISTER.register("arrow_entropy", () -> new ArrowItem(new Properties()));
   public static final DeferredItem<Item> COMPASS_STONE = REGISTER.register("compass_stone", () -> new ItemCompassStone());
   public static final DeferredItem<Item> PRIMAL_CRUSHER = REGISTER.register("primal_crusher", () -> new ItemPrimalCrusher());
   public static final DeferredItem<Item> SPEED_RUNE = REGISTER.register("speed_rune", () -> new MatrixUpgradeItem("tc.rune.info.0", 0));
   public static final DeferredItem<Item> EFFICIENCY_RUNE = REGISTER.register("efficiency_rune", () -> new MatrixUpgradeItem("tc.rune.info.1", 1));
   public static final DeferredItem<Item> CLEANER_RUNE = REGISTER.register("cleaner_rune", () -> new MatrixUpgradeItem("tc.rune.info.2", 2));
   public static final DeferredItem<Item> LUCKY_RUNE = REGISTER.register("lucky_rune", () -> new MatrixUpgradeItem("tc.rune.info.3", 3));
   public static final DeferredItem<Item> STABLE_RUNE = REGISTER.register("stable_rune", () -> new MatrixUpgradeItem("tc.rune.info.4", 4));
   public static final DeferredItem<Item> DISTORTED_FRAGMENT = REGISTER.register("distorted_fragment", () -> new DistortedFragmentItem());
   public static final DeferredItem<Item> CLEAN_FRAGMENT = REGISTER.register("clean_fragment", () -> new CleanFragmentItem());
   public static final DeferredItem<ItemThaumonomicon> THAUMONOMICON = REGISTER.register("thaumonomicon", () -> new ItemThaumonomicon());
   public static final DeferredItem<Item> CRIMSON_RITES = REGISTER.register("crimson_rites", () -> new CrimsonRitesItem());
   public static final DeferredItem<Item> RESEARCH_VOLUME = REGISTER.register("research_volume", () -> new ResearchVolumeItem());
   public static final DeferredItem<Item> VOLUME_OF_PURIFICATION = REGISTER.register("volume_of_purification", () -> new VolumeOfPurificationItem());
   public static final DeferredItem<Item> CLOTH_CHEST = REGISTER.register("cloth_chest", () -> new ItemRobeArmor(Type.CHESTPLATE));
   public static final DeferredItem<Item> CLOTH_LEGGINGS = REGISTER.register("cloth_leggings", () -> new ItemRobeArmor(Type.LEGGINGS));
   public static final DeferredItem<Item> CLOTH_BOOTS = REGISTER.register("cloth_boots", () -> new ItemRobeArmor(Type.BOOTS));
   public static final DeferredItem<Item> ICHOR_HELMET = REGISTER.register("ichor_helmet", () -> new ItemIchorArmor(Type.HELMET));
   public static final DeferredItem<Item> ICHOR_CHEST = REGISTER.register("ichor_chest", () -> new ItemIchorArmor(Type.CHESTPLATE));
   public static final DeferredItem<Item> ICHOR_LEGGINGS = REGISTER.register("ichor_leggings", () -> new ItemIchorArmor(Type.LEGGINGS));
   public static final DeferredItem<Item> ICHOR_BOOTS = REGISTER.register("ichor_boots", () -> new ItemIchorArmor(Type.BOOTS));
   public static final DeferredItem<Item> ICHOR_GEM_HELMET = REGISTER.register("ichor_gem_helmet", () -> new ItemGemHelm());
   public static final DeferredItem<Item> ICHOR_GEM_CHEST = REGISTER.register("ichor_gem_chest", () -> new ItemGemChest());
   public static final DeferredItem<Item> ICHOR_GEM_LEGGINGS = REGISTER.register("ichor_gem_leggings", () -> new ItemGemLegs());
   public static final DeferredItem<Item> ICHOR_GEM_BOOTS = REGISTER.register("ichor_gem_boots", () -> new ItemGemBoots());
   public static final DeferredItem<Item> HOVER_HARNESS = REGISTER.register("hover_harness", () -> new ItemHoverHarness());
   public static final DeferredItem<Item> GOGGLES_REVEALING = REGISTER.register("goggles_revealing", () -> new ItemGoggles());
   public static final DeferredItem<Item> TRAVELLER_BOOTS = REGISTER.register("traveller_boots", () -> new ItemBootsTraveller());
   public static final DeferredItem<Item> THAUMIUM_HELMET = REGISTER.register("thaumium_helmet", () -> new ItemThaumiumArmor(Type.HELMET));
   public static final DeferredItem<Item> REVEALING_HELMET = REGISTER.register("revealing_helmet", () -> new ItemRevealingHelmet());
   public static final DeferredItem<Item> THAUMIUM_CHEST = REGISTER.register("thaumium_chest", () -> new ItemThaumiumArmor(Type.CHESTPLATE));
   public static final DeferredItem<Item> THAUMIUM_LEGGINGS = REGISTER.register("thaumium_leggings", () -> new ItemThaumiumArmor(Type.LEGGINGS));
   public static final DeferredItem<Item> THAUMIUM_BOOTS = REGISTER.register("thaumium_boots", () -> new ItemThaumiumArmor(Type.BOOTS));
   public static final DeferredItem<Item> THAUMIUM_SHOVEL = REGISTER.register(
      "thaumium_shovel",
      () -> new ShovelItem(
         ThaumcraftApi.TOOL_MATERIAL_THAUMIUM,
         new Properties().rarity(Rarity.UNCOMMON).attributes(DiggerItem.createAttributes(ThaumcraftApi.TOOL_MATERIAL_THAUMIUM, 1.5F, -3.0F))
      )
   );
   public static final DeferredItem<Item> THAUMIUM_PICKAXE = REGISTER.register(
      "thaumium_pickaxe",
      () -> new PickaxeItem(
         ThaumcraftApi.TOOL_MATERIAL_THAUMIUM,
         new Properties().rarity(Rarity.UNCOMMON).attributes(DiggerItem.createAttributes(ThaumcraftApi.TOOL_MATERIAL_THAUMIUM, 1.0F, -2.8F))
      )
   );
   public static final DeferredItem<Item> THAUMIUM_SWORD = REGISTER.register(
      "thaumium_sword",
      () -> new SwordItem(
         ThaumcraftApi.TOOL_MATERIAL_THAUMIUM,
         new Properties().rarity(Rarity.UNCOMMON).attributes(SwordItem.createAttributes(ThaumcraftApi.TOOL_MATERIAL_THAUMIUM, 3, -2.4F))
      )
   );
   public static final DeferredItem<Item> THAUMIUM_AXE = REGISTER.register(
      "thaumium_axe",
      () -> new AxeItem(
         ThaumcraftApi.TOOL_MATERIAL_THAUMIUM,
         new Properties().rarity(Rarity.UNCOMMON).attributes(DiggerItem.createAttributes(ThaumcraftApi.TOOL_MATERIAL_THAUMIUM, 6.0F, -3.1F))
      )
   );
   public static final DeferredItem<Item> THAUMIUM_HOE = REGISTER.register(
      "thaumium_hoe",
      () -> new HoeItem(
         ThaumcraftApi.TOOL_MATERIAL_THAUMIUM,
         new Properties().rarity(Rarity.UNCOMMON).attributes(DiggerItem.createAttributes(ThaumcraftApi.TOOL_MATERIAL_THAUMIUM, -2.0F, -1.0F))
      )
   );
   public static final DeferredItem<Item> ELEMENTAL_SHOVEL = REGISTER.register("elemental_shovel", () -> new ItemElementalShovel());
   public static final DeferredItem<Item> ELEMENTAL_PICKAXE = REGISTER.register("elemental_pickaxe", () -> new ItemElementalPickaxe());
   public static final DeferredItem<Item> ELEMENTAL_SWORD = REGISTER.register("elemental_sword", () -> new ItemElementalSword());
   public static final DeferredItem<Item> ELEMENTAL_AXE = REGISTER.register("elemental_axe", () -> new ItemElementalAxe());
   public static final DeferredItem<Item> ELEMENTAL_HOE = REGISTER.register("elemental_hoe", () -> new ItemElementalHoe());
   public static final DeferredItem<Item> BLOOD_SWORD = REGISTER.register("blood_sword", () -> new ItemBloodSword());
   public static final DeferredItem<Item> THAUMIUM_FORTRESS_HELMET = REGISTER.register("thaumium_fortress_helmet", () -> new ItemFortressArmor(Type.HELMET));
   public static final DeferredItem<Item> THAUMIUM_FORTRESS_CHEST = REGISTER.register("thaumium_fortress_chest", () -> new ItemFortressArmor(Type.CHESTPLATE));
   public static final DeferredItem<Item> THAUMIUM_FORTRESS_LEGGINGS = REGISTER.register(
      "thaumium_fortress_leggings", () -> new ItemFortressArmor(Type.LEGGINGS)
   );
   public static final DeferredItem<Item> VOID_HELMET = REGISTER.register("void_helmet", () -> new ItemVoidArmor(Type.HELMET));
   public static final DeferredItem<Item> VOID_CHEST = REGISTER.register("void_chest", () -> new ItemVoidArmor(Type.CHESTPLATE));
   public static final DeferredItem<Item> VOID_LEGGINGS = REGISTER.register("void_leggings", () -> new ItemVoidArmor(Type.LEGGINGS));
   public static final DeferredItem<Item> VOID_BOOTS = REGISTER.register("void_boots", () -> new ItemVoidArmor(Type.BOOTS));
   public static final DeferredItem<Item> VOID_SHOVEL = REGISTER.register("void_shovel", () -> new ItemVoidShovel());
   public static final DeferredItem<Item> VOID_PICKAXE = REGISTER.register("void_pickaxe", () -> new ItemVoidPickaxe());
   public static final DeferredItem<Item> VOID_SWORD = REGISTER.register("void_sword", () -> new ItemVoidSword());
   public static final DeferredItem<Item> VOID_AXE = REGISTER.register("void_axe", () -> new ItemVoidAxe());
   public static final DeferredItem<Item> VOID_HOE = REGISTER.register("void_hoe", () -> new ItemVoidHoe());
   public static final DeferredItem<Item> ICHOR_PICKAXE = REGISTER.register("ichor_pickaxe", () -> new ItemIchorPickaxe());
   public static final DeferredItem<Item> ICHOR_AXE = REGISTER.register("ichor_axe", () -> new ItemIchorAxe());
   public static final DeferredItem<Item> ICHOR_SHOVEL = REGISTER.register("ichor_shovel", () -> new ItemIchorShovel());
   public static final DeferredItem<Item> ICHOR_SWORD = REGISTER.register("ichor_sword", () -> new ItemIchorSword());
   public static final DeferredItem<Item> ICHOR_PICKAXE_ADV = REGISTER.register("ichor_pickaxe_adv", () -> new ItemIchorPickaxeAdv());
   public static final DeferredItem<Item> ICHOR_AXE_ADV = REGISTER.register("ichor_axe_adv", () -> new ItemIchorAxeAdv());
   public static final DeferredItem<Item> ICHOR_SHOVEL_ADV = REGISTER.register("ichor_shovel_adv", () -> new ItemIchorShovelAdv());
   public static final DeferredItem<Item> ICHOR_SWORD_ADV = REGISTER.register("ichor_sword_adv", () -> new ItemIchorSwordAdv());
   public static final DeferredItem<Item> CRIMSON_BLADE = REGISTER.register("crimson_blade", () -> new ItemCrimsonSword());
   public static final DeferredItem<Item> VOID_ROBE_HELMET = REGISTER.register("void_robe_helmet", () -> new ItemVoidRobeArmor(Type.HELMET));
   public static final DeferredItem<Item> VOID_ROBE_CHEST = REGISTER.register("void_robe_chest", () -> new ItemVoidRobeArmor(Type.CHESTPLATE));
   public static final DeferredItem<Item> VOID_ROBE_LEGGINGS = REGISTER.register("void_robe_leggings", () -> new ItemVoidRobeArmor(Type.LEGGINGS));
   public static final DeferredItem<Item> HELMET_CULTIST_ROBE = REGISTER.register("cultist_robe_helmet", () -> new ItemCultistRobeArmor(Type.HELMET));
   public static final DeferredItem<Item> CHEST_CULTIST_ROBE = REGISTER.register("cultist_robe_chest", () -> new ItemCultistRobeArmor(Type.CHESTPLATE));
   public static final DeferredItem<Item> LEGGINGS_CULTIST_ROBE = REGISTER.register("cultist_robe_leggings", () -> new ItemCultistRobeArmor(Type.LEGGINGS));
   public static final DeferredItem<Item> HELMET_CULTIST_PLATE = REGISTER.register("cultist_plate_helmet", () -> new ItemCultistPlateArmor(Type.HELMET));
   public static final DeferredItem<Item> CHEST_CULTIST_PLATE = REGISTER.register("cultist_plate_chest", () -> new ItemCultistPlateArmor(Type.CHESTPLATE));
   public static final DeferredItem<Item> LEGGINGS_CULTIST_PLATE = REGISTER.register("cultist_plate_leggings", () -> new ItemCultistPlateArmor(Type.LEGGINGS));
   public static final DeferredItem<Item> HELMET_CULTIST_LEADER = REGISTER.register("cultist_leader_helmet", () -> new ItemCultistLeaderArmor(Type.HELMET));
   public static final DeferredItem<Item> CHEST_CULTIST_LEADER = REGISTER.register("cultist_leader_chest", () -> new ItemCultistLeaderArmor(Type.CHESTPLATE));
   public static final DeferredItem<Item> LEGGINGS_CULTIST_LEADER = REGISTER.register(
      "cultist_leader_leggings", () -> new ItemCultistLeaderArmor(Type.LEGGINGS)
   );
   public static final DeferredItem<Item> BOOTS_CULTIST = REGISTER.register("cultist_boots", () -> new ItemCultistBoots());
   public static final DeferredItem<Item> MOB_ASPECT = REGISTER.register("mob_aspect", () -> new ItemMobAspect());
   public static final DeferredItem<Item> BRAINY_ZOMBIE_EGG = registerSpawnEgg("brainy_zombie_spawn_egg", ModEntities.BRAINY_ZOMBIE, 16761087, 32768);
   public static final DeferredItem<Item> GIANT_BRAINY_ZOMBIE_EGG = registerSpawnEgg(
      "giant_brainy_zombie_spawn_egg", ModEntities.GIANT_BRAINY_ZOMBIE, 16761087, 16384
   );
   public static final DeferredItem<Item> WISP_EGG = registerSpawnEgg("wisp_spawn_egg", ModEntities.WISP, 16761087, 16777215);
   public static final DeferredItem<Item> FIRE_BAT_EGG = registerSpawnEgg("fire_bat_spawn_egg", ModEntities.FIRE_BAT, 16761087, 12582912);
   public static final DeferredItem<Item> PECH_EGG = registerSpawnEgg("pech_spawn_egg", ModEntities.PECH, 16761087, 4194368);
   public static final DeferredItem<Item> MIND_SPIDER_EGG = registerSpawnEgg("mind_spider_spawn_egg", ModEntities.MIND_SPIDER, 11184810, 4210752);
   public static final DeferredItem<Item> ELDRITCH_GUARDIAN_EGG = registerSpawnEgg(
      "eldritch_guardian_spawn_egg", ModEntities.ELDRITCH_GUARDIAN, 2236962, 4210752
   );
   public static final DeferredItem<Item> ELDRITCH_WARDEN_EGG = registerSpawnEgg("eldritch_warden_spawn_egg", ModEntities.ELDRITCH_WARDEN, 5579298, 4210752);
   public static final DeferredItem<Item> CULTIST_EGG = registerSpawnEgg("cultist_spawn_egg", ModEntities.CULTIST, 16732245, 8421504);
   public static final DeferredItem<Item> CULTIST_KNIGHT_EGG = registerSpawnEgg("cultist_knight_spawn_egg", ModEntities.CULTIST_KNIGHT, 16732245, 128);   public static final DeferredItem<Item> CULTIST_CLERIC_EGG = registerSpawnEgg("cultist_cleric_spawn_egg", ModEntities.CULTIST_CLERIC, 16732245, 8388608);
   public static final DeferredItem<Item> CULTIST_LEADER_EGG = registerSpawnEgg("cultist_leader_spawn_egg", ModEntities.CULTIST_LEADER, 16732245, 5263440);
   public static final DeferredItem<Item> CULTIST_PORTAL_EGG = registerSpawnEgg("cultist_portal_spawn_egg", ModEntities.CULTIST_PORTAL, 16732245, 16732415);
   public static final DeferredItem<Item> ELDRITCH_GOLEM_EGG = registerSpawnEgg("eldritch_golem_spawn_egg", ModEntities.ELDRITCH_GOLEM, 5592405, 4210752);
   public static final DeferredItem<Item> ELDRITCH_CRAB_EGG = registerSpawnEgg("eldritch_crab_spawn_egg", ModEntities.ELDRITCH_CRAB, 5592405, 5570560);
   public static final DeferredItem<Item> INHABITED_ZOMBIE_EGG = registerSpawnEgg("inhabited_zombie_spawn_egg", ModEntities.INHABITED_ZOMBIE, 5601109, 5570560);
   public static final DeferredItem<Item> THAUMIC_SLIME_EGG = registerSpawnEgg("thaumic_slime_spawn_egg", ModEntities.THAUMIC_SLIME, 16761087, 16744703);
   public static final DeferredItem<Item> TAINT_SPIDER_EGG = registerSpawnEgg("taint_spider_spawn_egg", ModEntities.TAINT_SPIDER, 16761087, 4210752);
   public static final DeferredItem<Item> TENTACLE_EGG = registerSpawnEgg("tentacle_spawn_egg", ModEntities.TENTACLE, 16761087, 8388736);
   public static final DeferredItem<Item> TENTACLE_SMALL_EGG = registerSpawnEgg("tentacle_small_spawn_egg", ModEntities.TENTACLE_SMALL, 16761087, 8388752);
   public static final DeferredItem<Item> TAINT_SPORE_EGG = registerSpawnEgg("taint_spore_spawn_egg", ModEntities.TAINT_SPORE, 16761087, 8388720);
   public static final DeferredItem<Item> TAINT_SPORE_SWARMER_EGG = registerSpawnEgg(
      "taint_spore_swarmer_spawn_egg", ModEntities.TAINT_SPORE_SWARMER, 16761087, 8388704
   );
   public static final DeferredItem<Item> TAINT_SWARM_EGG = registerSpawnEgg("taint_swarm_spawn_egg", ModEntities.TAINT_SWARM, 16761087, 8388688);
   public static final DeferredItem<Item> TAINT_CHICKEN_EGG = registerSpawnEgg("taint_chicken_spawn_egg", ModEntities.TAINT_CHICKEN, 16761087, 12632256);
   public static final DeferredItem<Item> TAINT_COW_EGG = registerSpawnEgg("taint_cow_spawn_egg", ModEntities.TAINT_COW, 16761087, 8272443);
   public static final DeferredItem<Item> TAINT_CREEPER_EGG = registerSpawnEgg("taint_creeper_spawn_egg", ModEntities.TAINT_CREEPER, 16761087, 65280);
   public static final DeferredItem<Item> TAINT_PIG_EGG = registerSpawnEgg("taint_pig_spawn_egg", ModEntities.TAINT_PIG, 16761087, 15702511);
   public static final DeferredItem<Item> TAINT_SHEEP_EGG = registerSpawnEgg("taint_sheep_spawn_egg", ModEntities.TAINT_SHEEP, 16761087, 8421504);
   public static final DeferredItem<Item> TAINT_VILLAGER_EGG = registerSpawnEgg("taint_villager_spawn_egg", ModEntities.TAINT_VILLAGER, 16761087, 65535);
   public static final DeferredItem<Item> TENTACLE_GIANT_EGG = registerSpawnEgg("tentacle_giant_spawn_egg", ModEntities.TENTACLE_GIANT, 16761087, 8421504);
   public static final DeferredItem<Item> WATCHER_EGG = registerSpawnEgg("watcher_spawn_egg", ModEntities.WATCHER, 16732245, 8421504);

   private static Item defaultItem() {
      return new Item(new Properties());
   }

   private static Item defaultItem(Rarity rarity) {
      return new Item(new Properties().rarity(rarity));
   }

   private static BlockItem defaultBlock(Block block) {
      return new BlockItem(block, new Properties());
   }

   private static <B extends Block> DeferredItem<BlockItem> registerBlock(String name, DeferredHolder<Block, B> block) {
      return registerBlock(name, block, ModItems::defaultBlock);
   }

   private static <I extends BlockItem, B extends Block> DeferredItem<I> registerBlock(String name, DeferredHolder<Block, B> block, Function<B, I> function) {
      return REGISTER.register(name, () -> function.apply((B)block.get()));
   }

   private static DeferredItem<Item> registerSpawnEgg(String name, java.util.function.Supplier<? extends EntityType<? extends net.minecraft.world.entity.Mob>> entityType, int primaryColor, int secondaryColor) {
      DeferredItem<Item> toReturn = REGISTER.register(name, () -> new DeferredSpawnEggItem(entityType, primaryColor, secondaryColor, new Properties()));
      SPAWN_EGGS.add(toReturn);
      return toReturn;
   }

   private static <B extends Block> Map<DyeColor, DeferredItem<BlockItem>> registerBlockColors(String prefix, Map<DyeColor, DeferredHolder<Block, B>> map) {
      Map<DyeColor, DeferredItem<BlockItem>> result = new HashMap<>();

      for (DyeColor value : DyeColor.values()) {
         result.put(value, registerBlock(value.getName() + "_" + prefix, map.get(value)));
      }

      return result;
   }

   private static <B extends Block> Map<DyeColor, DeferredItem<BlockItem>> registerBannerBlockColors() {
      Map<DyeColor, DeferredItem<BlockItem>> result = new HashMap<>();

      for (DyeColor value : DyeColor.values()) {
         result.put(
            value,
            REGISTER.register(
               value.getName() + "_banner", () -> new ItemBanner((Block)ModBlocks.BANNER.get(value).get(), (Block)ModBlocks.WALL_BANNER.get(value).get())
            )
         );
      }

      return result;
   }
}
