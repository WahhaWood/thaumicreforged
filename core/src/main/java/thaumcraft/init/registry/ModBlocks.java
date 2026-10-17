package thaumcraft.init.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.block.advanced_alchemy_furnace.AdvAlchemyFurnaceBlock;
import thaumcraft.block.advanced_alchemy_furnace.AdvAlchemyFurnaceBurnBlock;
import thaumcraft.block.advanced_alchemy_furnace.AdvAlchemyFurnaceTankBlock;
import thaumcraft.block.advanced_node_infuser.AdvancedNodeInfuserBlock;
import thaumcraft.block.airy.aura_node.AuraNodeBlock;
import thaumcraft.block.airy.energized_aura.EnergizedAuraNodeBlock;
import thaumcraft.block.airy.gazy.BlockGaseousLight;
import thaumcraft.block.airy.gazy.BlockGaseousShadow;
import thaumcraft.block.airy.gazy.BlockNitorGas;
import thaumcraft.block.airy.impassable.ImpassableBlock;
import thaumcraft.block.airy.nitor.NitorBlock;
import thaumcraft.block.airy.sapping_field.SappingFieldBlock;
import thaumcraft.block.airy.static_field.StaticFieldBlock;
import thaumcraft.block.airy.warding_aura.WardingAuraBlock;
import thaumcraft.block.alchemy_furnace.AlchemyFurnaceBlock;
import thaumcraft.block.arcane_alembic.ArcaneAlembicBlock;
import thaumcraft.block.arcane_door.ArcaneDoorBlock;
import thaumcraft.block.arcane_pedestal.ArcanePedestalBlock;
import thaumcraft.block.arcane_workbench.ArcaneWorkbenchBlock;
import thaumcraft.block.banner.BannerBlock;
import thaumcraft.block.banner.WallBannerBlock;
import thaumcraft.block.bellows.BellowsBlock;
import thaumcraft.block.bore.BoreBaseBlock;
import thaumcraft.block.bore.BoreBlock;
import thaumcraft.block.candle.CandleBlock;
import thaumcraft.block.cosmetic.CosmeticSolidBlock;
import thaumcraft.block.cosmetic.DarkQuartzBlock;
import thaumcraft.block.cosmetic.DarkQuartzPillarBlock;
import thaumcraft.block.cosmetic.GolemFetterBlock;
import thaumcraft.block.cosmetic.TravelStairsBlock;
import thaumcraft.block.cosmetic.TravelStoneBlock;
import thaumcraft.block.cosmetic.TravelStoneSlab;
import thaumcraft.block.cosmetic.WardingStoneBlock;
import thaumcraft.block.crucible.CrucibleBlock;
import thaumcraft.block.crystal.CrystalClusterBlock;
import thaumcraft.block.crystal.EldritchCrystalBlock;
import thaumcraft.block.crystal.VisCrystalBlock;
import thaumcraft.block.deconstruction_table.DeconstructionTableBlock;
import thaumcraft.block.eccence_reservoir.BlockEssenceReservoir;
import thaumcraft.block.eldritch.EndritchBlock;
import thaumcraft.block.eldritch.altar.BlockEldritchAltar;
import thaumcraft.block.eldritch.crab_spawner.BlockEldritchCrabSpawner;
import thaumcraft.block.eldritch.doorway.AncientDoorwayBlock;
import thaumcraft.block.eldritch.lock.BlockEldritchLock;
import thaumcraft.block.eldritch.nothing.BlockEldritchNothing;
import thaumcraft.block.eldritch.obelisk.BlockEldritchObelisk;
import thaumcraft.block.eldritch.obelisk.BlockEldritchObeliskPart;
import thaumcraft.block.eldritch.portal.BlockEldritchPortal;
import thaumcraft.block.eldritch.trap.BlockEldritchTrap;
import thaumcraft.block.encharter.EnchanterBlock;
import thaumcraft.block.essence_crystallizer.EssenceCrystallizerBlock;
import thaumcraft.block.ethereal_bloom.BlockEtherealBloom;
import thaumcraft.block.fluid.BlockFluidDeath;
import thaumcraft.block.fluid.BlockFluidPure;
import thaumcraft.block.fluid.BlockFluxGas;
import thaumcraft.block.fluid.BlockFluxGoo;
import thaumcraft.block.flux_scrubber.FluxScrubberBlock;
import thaumcraft.block.focal_manipulator.FocalManipulatorBlock;
import thaumcraft.block.funnel.FunnelBlock;
import thaumcraft.block.glimmer_of_light.GlimmerOfLightBlock;
import thaumcraft.block.grate.GrateBlock;
import thaumcraft.block.hole.HoleBlock;
import thaumcraft.block.hungry_chest.ChestHungryBlock;
import thaumcraft.block.infernal_furnace.InfernalFurnaceBlock;
import thaumcraft.block.infusion_claw.InfusionClawBlock;
import thaumcraft.block.infusion_matrix.RunicMatrixBlock;
import thaumcraft.block.infusion_pillar.InfusionPillarBlock;
import thaumcraft.block.jar.blank.VoidJarBlock;
import thaumcraft.block.jar.brain.BrainJarBlock;
import thaumcraft.block.jar.node.NodeJarBlock;
import thaumcraft.block.jar.standart.JarBlock;
import thaumcraft.block.lamp.ArcaneLampBlock;
import thaumcraft.block.lamp.fertility.ArcaneLampFertilityBlock;
import thaumcraft.block.lamp.growth.ArcaneLampGrowthBlock;
import thaumcraft.block.lifter.LifterBlock;
import thaumcraft.block.log.BlockMagicalLog;
import thaumcraft.block.loot.BlockLoot;
import thaumcraft.block.magic_box.BlockMagicBox;
import thaumcraft.block.magnet.MagnetBlock;
import thaumcraft.block.mana_pod.BlockManaPod;
import thaumcraft.block.mirror.BlockMirror;
import thaumcraft.block.mnemonic.MnemonicMatrixBlock;
import thaumcraft.block.mobilizer.MobilizerBlock;
import thaumcraft.block.mobilizer.MobilizerRelayBlock;
import thaumcraft.block.node_stabilizer.NodeStabilizerBlock;
import thaumcraft.block.node_transducer.NodeTransducerBlock;
import thaumcraft.block.opaque.AmberBlock;
import thaumcraft.block.opaque.AmberBricksBlock;
import thaumcraft.block.opaque.WardedGlassBlock;
import thaumcraft.block.ore.CustomOreBlock;
import thaumcraft.block.plant.BlockCustomPlant;
import thaumcraft.block.platform.PlatformBlock;
import thaumcraft.block.portal.BlockBedrockPortal;
import thaumcraft.block.pressure_plate.PressurePlateBlock;
import thaumcraft.block.repairer.RepairerBlock;
import thaumcraft.block.sensor.SensorBlock;
import thaumcraft.block.spa.SpaBlock;
import thaumcraft.block.summoner.SummonerBlock;
import thaumcraft.block.table.ResearchTableBlock;
import thaumcraft.block.table.TableBlock;
import thaumcraft.block.taint.BlockTaint;
import thaumcraft.block.taint.BlockTaintFibres;
import thaumcraft.block.taint.FibrousTaintBlock;
import thaumcraft.block.taint.TaintedGrassBlock;
import thaumcraft.block.taint.TaintedPlantBlock;
import thaumcraft.block.thaumatorium.ThaumatoriumBlock;
import thaumcraft.block.totem.ChargeObsidianTotemBlock;
import thaumcraft.block.totem.ObsidianTotemBlock;
import thaumcraft.block.transvector.TransvectorDislocatorBlock;
import thaumcraft.block.transvector.TransvectorInterfaceBlock;
import thaumcraft.block.tube.BlockTube;
import thaumcraft.block.tube.buffer.BlockTubeBuffer;
import thaumcraft.block.tube.centrifuge.CentrifugeBlock;
import thaumcraft.block.tube.filter.BlockTubeFiltered;
import thaumcraft.block.tube.oneway.BlockTubeOneway;
import thaumcraft.block.tube.restrict.BlockTubeRestrict;
import thaumcraft.block.tube.valve.BlockValve;
import thaumcraft.block.vis_charged_relay.VisChargeRelayBlock;
import thaumcraft.block.vis_relay.VisRelayBlock;
import thaumcraft.block.wand_charger.WandChargerBlock;
import thaumcraft.block.wand_pedestal_focus.CompoundRechargeFocusBlock;
import thaumcraft.block.wand_recharger.WandPedestalBlock;
import thaumcraft.block.warded.WardedBlock;
import thaumcraft.block.warp_gate.WarpGateBlock;
import thaumcraft.item.sapling.ThaumcraftSaplingBlock;
import thaumcraft.world.growers.GreatwoodTreeGrower;
import thaumcraft.world.growers.SilverwoodTreeGrower;

public class ModBlocks {
   public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(BuiltInRegistries.BLOCK, "thaumcraft");
   public static final DeferredHolder<Block, Block> NITOR_GAS = REGISTER.register("nitor_gas", () -> new BlockNitorGas());
   public static final DeferredHolder<Block, Block> GASEOUS_SHADOW = REGISTER.register("gaseous_shadow", () -> new BlockGaseousShadow());
   public static final DeferredHolder<Block, Block> GASEOUS_LIGHT = REGISTER.register("gaseous_light", () -> new BlockGaseousLight());
   public static final DeferredHolder<Block, LiquidBlock> FLUX_GOO = REGISTER.register(
      "flux_goo", () -> new BlockFluxGoo(ModFluids.FLUX_GOO.getSource(), Properties.ofFullCopy(Blocks.WATER).noLootTable())
   );
   public static final DeferredHolder<Block, LiquidBlock> FLUX_GAS = REGISTER.register(
      "flux_gas", () -> new BlockFluxGas(ModFluids.FLUX_GAS.getSource(), Properties.ofFullCopy(Blocks.WATER).noLootTable())
   );
   public static final DeferredHolder<Block, LiquidBlock> FLUID_PURE = REGISTER.register(
      "fluid_pure", () -> new BlockFluidPure(ModFluids.FLUID_PURE.getSource(), Properties.ofFullCopy(Blocks.WATER).noLootTable())
   );
   public static final DeferredHolder<Block, LiquidBlock> FLUID_DEATH = REGISTER.register(
      "fluid_death", () -> new BlockFluidDeath(ModFluids.FLUID_DEATH.getSource(), Properties.ofFullCopy(Blocks.WATER).noLootTable())
   );
   public static final DeferredHolder<Block, Block> HOLE = REGISTER.register("hole", () -> new HoleBlock());
   public static final DeferredHolder<Block, Block> INFERNAL_FURNACE = REGISTER.register("infernal_furnace", () -> new InfernalFurnaceBlock());
   public static final DeferredHolder<Block, Block> CRUCIBLE = REGISTER.register("crucible", () -> new CrucibleBlock());
   public static final DeferredHolder<Block, Block> ARCANE_ALEMBIC = REGISTER.register("arcane_alembic", () -> new ArcaneAlembicBlock());
   public static final DeferredHolder<Block, Block> ALCHEMICAL_CONSTRUCT = REGISTER.register(
      "alchemical_construct", () -> new Block(Properties.of().sound(SoundType.METAL).strength(3.0F, 17.0F))
   );
   public static final DeferredHolder<Block, Block> ADVANCED_ALCHEMICAL_CONSTRUCT = REGISTER.register(
      "advanced_alchemical_construct", () -> new Block(Properties.of().sound(SoundType.METAL).strength(3.0F, 17.0F).lightLevel(value -> 11))
   );
   public static final DeferredHolder<Block, Block> GRATE = REGISTER.register("grate", () -> new GrateBlock());
   public static final DeferredHolder<Block, Block> ARCANE_LAMP = REGISTER.register("arcane_lamp", () -> new ArcaneLampBlock());
   public static final DeferredHolder<Block, Block> ARCANE_LAMP_GROWTH = REGISTER.register("arcane_lamp_growth", () -> new ArcaneLampGrowthBlock());
   public static final DeferredHolder<Block, Block> ARCANE_LAMP_FERTILITY = REGISTER.register("arcane_lamp_fertility", () -> new ArcaneLampFertilityBlock());
   public static final DeferredHolder<Block, Block> THAUMATORIUM = REGISTER.register("thaumatorium", () -> new ThaumatoriumBlock());
   public static final DeferredHolder<Block, Block> MNEMONIC_MATRIX = REGISTER.register("mnemonic_matrix", () -> new MnemonicMatrixBlock());
   public static final DeferredHolder<Block, Block> VIS_RELAY = REGISTER.register("vis_relay", () -> new VisRelayBlock());
   public static final DeferredHolder<Block, Block> VIS_CHARGE_RELAY = REGISTER.register("vis_charge_relay", () -> new VisChargeRelayBlock());
   public static final DeferredHolder<Block, Block> ALCHEMY_FURNACE = REGISTER.register("alchemy_furnace", () -> new AlchemyFurnaceBlock());
   public static final DeferredHolder<Block, Block> ARCANE_PEDESTAL = REGISTER.register("arcane_pedestal", () -> new ArcanePedestalBlock());
   public static final DeferredHolder<Block, Block> RUNIC_MATRIX = REGISTER.register("runic_matrix", () -> new RunicMatrixBlock());
   public static final DeferredHolder<Block, Block> INFUSION_PILLAR = REGISTER.register("infusion_pillar", () -> new InfusionPillarBlock());
   public static final DeferredHolder<Block, Block> WAND_PEDESTAL = REGISTER.register("wand_pedestal", () -> new WandPedestalBlock());
   public static final DeferredHolder<Block, Block> WAND_PEDESTAL_FOCUS = REGISTER.register("wand_pedestal_focus", () -> new CompoundRechargeFocusBlock());
   public static final DeferredHolder<Block, Block> WAND_CHARGER = REGISTER.register("wand_charger", () -> new WandChargerBlock());
   public static final DeferredHolder<Block, Block> NODE_STABILIZER = REGISTER.register("node_stabilizer", () -> new NodeStabilizerBlock());
   public static final DeferredHolder<Block, Block> ADVANCED_NODE_STABILIZER = REGISTER.register("advanced_node_stabilizer", () -> new NodeStabilizerBlock());
   public static final DeferredHolder<Block, Block> ADVANCED_NODE_INFUSER = REGISTER.register("advanced_node_infuser", () -> new AdvancedNodeInfuserBlock());
   public static final DeferredHolder<Block, Block> NODE_TRANSDUCER = REGISTER.register("node_transducer", () -> new NodeTransducerBlock());
   public static final DeferredHolder<Block, Block> ARCANE_SPA = REGISTER.register("arcane_spa", () -> new SpaBlock());
   public static final DeferredHolder<Block, Block> FOCAL_MANIPULATOR = REGISTER.register("focal_manipulator", () -> new FocalManipulatorBlock());
   public static final DeferredHolder<Block, Block> SUMMONER = REGISTER.register("summoner", () -> new SummonerBlock());
   public static final DeferredHolder<Block, Block> ENCHANTER = REGISTER.register("enchanter", () -> new EnchanterBlock());
   public static final DeferredHolder<Block, Block> FLUX_SCRUBBER = REGISTER.register("flux_scrubber", () -> new FluxScrubberBlock());
   public static final DeferredHolder<Block, Block> ARCANE_BELLOWS = REGISTER.register("arcane_bellows", () -> new BellowsBlock());
   public static final DeferredHolder<Block, Block> ARCANE_SENSOR = REGISTER.register("arcane_sensor", () -> new SensorBlock());
   public static final DeferredHolder<Block, Block> ARCANE_PRESSURE_PLATE = REGISTER.register("arcane_pressure_plate", () -> new PressurePlateBlock());
   public static final DeferredHolder<Block, Block> ARCANE_BORE_BASE = REGISTER.register("arcane_bore_base", () -> new BoreBaseBlock());
   public static final DeferredHolder<Block, Block> ARCANE_BORE = REGISTER.register("arcane_bore", () -> new BoreBlock());
   public static final DeferredHolder<Block, Block> CRIMSON_CULT_BANNER = REGISTER.register("crimson_cult_banner", () -> new BannerBlock(DyeColor.RED));
   public static final Map<DyeColor, DeferredHolder<Block, Block>> BANNER = registerColors("banner", BannerBlock::new);
   public static final DeferredHolder<Block, Block> CRIMSON_CULT_WALL_BANNER = REGISTER.register(
      "crimson_cult_wall_banner", () -> new WallBannerBlock(DyeColor.RED)
   );
   public static final Map<DyeColor, DeferredHolder<Block, Block>> WALL_BANNER = registerColors("wall_banner", WallBannerBlock::new);
   public static final DeferredHolder<Block, Block> GREATWOOD_LOG = REGISTER.register(
      "greatwood_log", () -> new RotatedPillarBlock(Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2.5F).ignitedByLava())
   );
   public static final DeferredHolder<Block, Block> GREATWOOD_PLANKS = REGISTER.register(
      "greatwood_planks",
      () -> new Block(Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F, 10.0F).sound(SoundType.WOOD).ignitedByLava())
   );
   public static final DeferredHolder<Block, Block> GREATWOOD_SLAB = REGISTER.register(
      "greatwood_slab", () -> new SlabBlock(Properties.of().strength(2.0F, 5.0F).sound(SoundType.WOOD))
   );
   public static final DeferredHolder<Block, Block> GREATWOOD_STAIRS = REGISTER.register(
      "greatwood_stairs", () -> new StairBlock(((Block)GREATWOOD_PLANKS.get()).defaultBlockState(), Properties.of())
   );
   public static final DeferredHolder<Block, Block> GREATWOOD_LEAVES = REGISTER.register(
      "greatwood_leaves", () -> new LeavesBlock(Properties.ofFullCopy(Blocks.OAK_LEAVES).strength(0.2F).replaceable())
   );
   public static final DeferredHolder<Block, Block> GREATWOOD_SAPLING = REGISTER.register(
      "greatwood_sapling", () -> new ThaumcraftSaplingBlock(GreatwoodTreeGrower.GROWER, 25, Properties.ofFullCopy(Blocks.OAK_SAPLING))
   );
   public static final DeferredHolder<Block, Block> SILVERWOOD_LOG = REGISTER.register(
      "silverwood_log",
      () -> new RotatedPillarBlock(Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2.5F).lightLevel(value -> 7).ignitedByLava())
   );
   public static final DeferredHolder<Block, Block> SILVERWOOD_PLANKS = REGISTER.register(
      "silverwood_planks",
      () -> new Block(Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F, 10.0F).sound(SoundType.WOOD).ignitedByLava())
   );
   public static final DeferredHolder<Block, Block> SILVERWOOD_SLAB = REGISTER.register(
      "silverwood_slab", () -> new SlabBlock(Properties.of().strength(2.0F, 5.0F).sound(SoundType.WOOD))
   );
   public static final DeferredHolder<Block, Block> SILVERWOOD_STAIRS = REGISTER.register(
      "silverwood_stairs", () -> new StairBlock(((Block)SILVERWOOD_PLANKS.get()).defaultBlockState(), Properties.of())
   );
   public static final DeferredHolder<Block, Block> SILVERWOOD_LEAVES = REGISTER.register(
      "silverwood_leaves", () -> new LeavesBlock(Properties.ofFullCopy(Blocks.OAK_LEAVES).strength(0.2F).replaceable())
   );
   public static final DeferredHolder<Block, Block> SILVERWOOD_SAPLING = REGISTER.register(
      "silverwood_sapling", () -> new ThaumcraftSaplingBlock(SilverwoodTreeGrower.GROWER, 50, Properties.ofFullCopy(Blocks.OAK_SAPLING))
   );
   public static final DeferredHolder<Block, Block> TABLE = REGISTER.register("table", () -> new TableBlock());
   public static final DeferredHolder<Block, Block> RESEARCH_TABLE = REGISTER.register("research_table", () -> new ResearchTableBlock());
   public static final DeferredHolder<Block, Block> DECONSTRUCTION_TABLE = REGISTER.register("deconstruction_table", () -> new DeconstructionTableBlock());
   public static final DeferredHolder<Block, Block> ARCANE_WORKBENCH = REGISTER.register("arcane_workbench", () -> new ArcaneWorkbenchBlock());
   public static final DeferredHolder<Block, Block> HUNGRY_CHEST = REGISTER.register("hungry_chest", () -> new ChestHungryBlock());
   public static final DeferredHolder<Block, Block> FUNNEL = REGISTER.register("funnel", () -> new FunnelBlock());
   public static final DeferredHolder<Block, Block> CINNABAR_ORE = REGISTER.register("cinnabar_ore", () -> new CustomOreBlock());
   public static final DeferredHolder<Block, Block> AMBER_ORE = REGISTER.register("amber_ore", () -> new CustomOreBlock());
   public static final DeferredHolder<Block, Block> AIR_SHARD_ORE = REGISTER.register("air_shard_ore", () -> new CustomOreBlock(true, 16777086));
   public static final DeferredHolder<Block, Block> FIRE_SHARD_ORE = REGISTER.register("fire_shard_ore", () -> new CustomOreBlock(true, 16727041));
   public static final DeferredHolder<Block, Block> WATER_SHARD_ORE = REGISTER.register("water_shard_ore", () -> new CustomOreBlock(true, 37119));
   public static final DeferredHolder<Block, Block> EARTH_SHARD_ORE = REGISTER.register("earth_shard_ore", () -> new CustomOreBlock(true, 40960));
   public static final DeferredHolder<Block, Block> ORDER_SHARD_ORE = REGISTER.register("order_shard_ore", () -> new CustomOreBlock(true, 15650047));
   public static final DeferredHolder<Block, Block> ENTROPY_SHARD_ORE = REGISTER.register("entropy_shard_ore", () -> new CustomOreBlock(true, 5592439));
   public static final DeferredHolder<Block, Block> SHIMMER_LEAF = REGISTER.register(
      "shimmer_leaf", () -> new BlockCustomPlant(BlockCustomPlant.PlantKind.SHIMMER_LEAF)
   );
   public static final DeferredHolder<Block, Block> CINDER_PEARL = REGISTER.register(
      "cinder_pearl", () -> new BlockCustomPlant(BlockCustomPlant.PlantKind.CINDER_PEARL)
   );
   public static final DeferredHolder<Block, Block> ETHEREAL_BLOOM = REGISTER.register("ethereal_bloom", BlockEtherealBloom::new);
   public static final DeferredHolder<Block, Block> VISHROOM = REGISTER.register("vishroom", () -> new BlockCustomPlant(BlockCustomPlant.PlantKind.VISHROOM));
   public static final Map<DyeColor, DeferredHolder<Block, Block>> CANDLE = registerColors("candle", CandleBlock::new);
   public static final DeferredHolder<Block, Block> JAR = REGISTER.register("jar", () -> new JarBlock());
   public static final DeferredHolder<Block, Block> BRAIN_JAR = REGISTER.register("brain_jar", () -> new BrainJarBlock());
   public static final DeferredHolder<Block, Block> NODE_JAR = REGISTER.register("node_jar", () -> new NodeJarBlock());
   public static final DeferredHolder<Block, Block> VOID_JAR = REGISTER.register("void_jar", () -> new VoidJarBlock());
   public static final DeferredHolder<Block, Block> ARCANE_DOOR = REGISTER.register("arcane_door", () -> new ArcaneDoorBlock());
   public static final DeferredHolder<Block, Block> WARDED = REGISTER.register("warded", () -> new WardedBlock());
   public static final DeferredHolder<Block, Block> LIFTER = REGISTER.register("lifter", () -> new LifterBlock());
   public static final DeferredHolder<Block, Block> TRANSVECTOR_INTERFACE = REGISTER.register("transvector_interface", () -> new TransvectorInterfaceBlock());
   public static final DeferredHolder<Block, Block> TRANSVECTOR_DISLOCATOR = REGISTER.register("transvector_dislocator", () -> new TransvectorDislocatorBlock());
   public static final DeferredHolder<Block, Block> MOBILIZER = REGISTER.register("mobilizer", () -> new MobilizerBlock());
   public static final DeferredHolder<Block, Block> MOBILIZER_RELAY = REGISTER.register("mobilizer_relay", () -> new MobilizerRelayBlock());
   public static final DeferredHolder<Block, Block> MAGNET = REGISTER.register("magnet", () -> new MagnetBlock(false));
   public static final DeferredHolder<Block, Block> MOB_MAGNET = REGISTER.register("mob_magnet", () -> new MagnetBlock(true));
   public static final DeferredHolder<Block, Block> SILVERWOOD_LOG_NODE = REGISTER.register("silverwood_log_node", () -> new BlockMagicalLog());
   public static final DeferredHolder<Block, Block> AURA_NODE = REGISTER.register("aura_node", () -> new AuraNodeBlock());
   public static final DeferredHolder<Block, Block> NITOR = REGISTER.register("nitor", () -> new NitorBlock());
   public static final DeferredHolder<Block, Block> GLIMMER_OF_LIGHT = REGISTER.register("glimmer_of_light", () -> new GlimmerOfLightBlock());
   public static final DeferredHolder<Block, Block> WARDING_AURA = REGISTER.register("warding_aura", () -> new WardingAuraBlock());
   public static final DeferredHolder<Block, Block> ENERGIZED_AURA_NODE = REGISTER.register("energized_aura_node", () -> new EnergizedAuraNodeBlock());
   public static final DeferredHolder<Block, Block> STATIC_FIELD = REGISTER.register("static_field", () -> new StaticFieldBlock());
   public static final DeferredHolder<Block, Block> SAPPING_FIELD = REGISTER.register("sapping_field", () -> new SappingFieldBlock());
   public static final DeferredHolder<Block, Block> IMPASSABLE = REGISTER.register("impassable", () -> new ImpassableBlock());
   public static final DeferredHolder<Block, Block> AIR_CRYSTAL_CLUSTER = REGISTER.register("air_crystal_cluster", () -> new CrystalClusterBlock());
   public static final DeferredHolder<Block, Block> FIRE_CRYSTAL_CLUSTER = REGISTER.register("fire_crystal_cluster", () -> new CrystalClusterBlock());
   public static final DeferredHolder<Block, Block> WATER_CRYSTAL_CLUSTER = REGISTER.register("water_crystal_cluster", () -> new CrystalClusterBlock());
   public static final DeferredHolder<Block, Block> EARTH_CRYSTAL_CLUSTER = REGISTER.register("earth_crystal_cluster", () -> new CrystalClusterBlock());
   public static final DeferredHolder<Block, Block> ORDER_CRYSTAL_CLUSTER = REGISTER.register("order_crystal_cluster", () -> new CrystalClusterBlock());
   public static final DeferredHolder<Block, Block> ENTROPY_CRYSTAL_CLUSTER = REGISTER.register("entropy_crystal_cluster", () -> new CrystalClusterBlock());
   public static final DeferredHolder<Block, Block> MIXED_CRYSTAL_CLUSTER = REGISTER.register("mixed_crystal_cluster", () -> new CrystalClusterBlock());
   public static final DeferredHolder<Block, Block> STRANGE_CRYSTALS = REGISTER.register("strange_crystals", () -> new EldritchCrystalBlock());
   public static final DeferredHolder<Block, Block> AIR_CRYSTAL = REGISTER.register("air_crystal", () -> new VisCrystalBlock(Aspect.AIR));
   public static final DeferredHolder<Block, Block> FIRE_CRYSTAL = REGISTER.register("fire_crystal", () -> new VisCrystalBlock(Aspect.FIRE));
   public static final DeferredHolder<Block, Block> WATER_CRYSTAL = REGISTER.register("water_crystal", () -> new VisCrystalBlock(Aspect.WATER));
   public static final DeferredHolder<Block, Block> EARTH_CRYSTAL = REGISTER.register("earth_crystal", () -> new VisCrystalBlock(Aspect.EARTH));
   public static final DeferredHolder<Block, Block> ORDER_CRYSTAL = REGISTER.register("order_crystal", () -> new VisCrystalBlock(Aspect.ORDER));
   public static final DeferredHolder<Block, Block> ENTROPY_CRYSTAL = REGISTER.register("entropy_crystal", () -> new VisCrystalBlock(Aspect.ENTROPY));
   public static final DeferredHolder<Block, Block> AMBER_BLOCK = REGISTER.register("amber_block", () -> new AmberBlock());
   public static final DeferredHolder<Block, Block> AMBER_BRICKS = REGISTER.register("amber_bricks", () -> new AmberBricksBlock());
   public static final DeferredHolder<Block, Block> WARDED_GLASS = REGISTER.register("warded_glass", () -> new WardedGlassBlock());
   public static final DeferredHolder<Block, Block> OBSIDIAN_TOTEM = REGISTER.register("obsidian_totem", () -> new ObsidianTotemBlock(Properties.of()));
   public static final DeferredHolder<Block, Block> CHARGED_OBSIDIAN_TOTEM = REGISTER.register(
      "charged_obsidian_totem", () -> new ChargeObsidianTotemBlock(Properties.of())
   );
   public static final DeferredHolder<Block, Block> OBSIDIAN_TILE = REGISTER.register(
      "obsidian_tile", () -> new CosmeticSolidBlock(Properties.of().strength(30.0F, 1200.0F), true)
   );
   public static final DeferredHolder<Block, Block> TRAVEL_STONE = REGISTER.register("travel_stone", () -> new TravelStoneBlock());
   public static final DeferredHolder<Block, Block> TRAVEL_STONE_SLAB = REGISTER.register(
      "travel_stone_slab", () -> new TravelStoneSlab(Properties.of().strength(2.0F, 10.0F).lightLevel(s -> 9))
   );
   public static final DeferredHolder<Block, Block> TRAVEL_STONE_STAIRS = REGISTER.register("travel_stone_stairs", () -> new TravelStairsBlock());
   public static final DeferredHolder<Block, Block> DARK_QUARTZ_BLOCK = REGISTER.register("dark_quartz_block", () -> new DarkQuartzBlock());
   public static final DeferredHolder<Block, Block> CHISELED_DARK_QUARTZ_BLOCK = REGISTER.register("chiseled_dark_quartz_block", () -> new DarkQuartzBlock());
   public static final DeferredHolder<Block, Block> DARK_QUARTZ_PILLAR = REGISTER.register("dark_quartz_pillar", () -> new DarkQuartzPillarBlock());
   public static final DeferredHolder<Block, Block> DARK_QUARTZ_SLAB = REGISTER.register(
      "dark_quartz_slab", () -> new SlabBlock(Properties.of().strength(0.8F, 10.0F).sound(SoundType.STONE))
   );
   public static final DeferredHolder<Block, Block> DARK_QUARTZ_STAIRS = REGISTER.register(
      "dark_quartz_stairs",
      () -> new StairBlock(((Block)DARK_QUARTZ_BLOCK.get()).defaultBlockState(), Properties.of().strength(0.8F, 10.0F).sound(SoundType.STONE))
   );
   public static final DeferredHolder<Block, Block> WARP_GATE = REGISTER.register("warp_gate", () -> new WarpGateBlock());
   public static final DeferredHolder<Block, Block> WARDING_STONE = REGISTER.register("warding_stone", () -> new WardingStoneBlock());
   public static final DeferredHolder<Block, Block> PLATFORM = REGISTER.register("platform", () -> new PlatformBlock());
   public static final DeferredHolder<Block, Block> THAUMIUM_BLOCK = REGISTER.register(
      "thaumium_block", () -> new CosmeticSolidBlock(Properties.of().sound(SoundType.METAL), true)
   );
   public static final DeferredHolder<Block, Block> ARCANE_STONE = REGISTER.register("arcane_stone", () -> new CosmeticSolidBlock(Properties.of(), true));
   public static final DeferredHolder<Block, Block> ARCANE_BRICKS = REGISTER.register("arcane_bricks", () -> new CosmeticSolidBlock(Properties.of(), true));
   public static final DeferredHolder<Block, Block> GOLEM_FETTER = REGISTER.register("golem_fetter", () -> new GolemFetterBlock());
   public static final DeferredHolder<Block, Block> ANCIENT_STONE = REGISTER.register(
      "ancient_stone", () -> new CosmeticSolidBlock(Properties.of().strength(2.0F, 10.0F), false)
   );
   public static final DeferredHolder<Block, Block> ANCIENT_ROCK = REGISTER.register(
      "ancient_rock", () -> new CosmeticSolidBlock(Properties.of().strength(2.0F, 10.0F), true)
   );
   public static final DeferredHolder<Block, Block> CRUSTED_STONE = REGISTER.register(
      "crusted_stone", () -> new CosmeticSolidBlock(Properties.of().strength(2.0F, 10.0F).lightLevel(s -> 4), true)
   );
   public static final DeferredHolder<Block, Block> ANCIENT_STONE_PEDESTAL = REGISTER.register(
      "ancient_stone_pedestal", () -> new CosmeticSolidBlock(Properties.of(), true)
   );
   public static final DeferredHolder<Block, Block> MAGIC_TALLOW_BLOCK = REGISTER.register(
      "magic_tallow_block", () -> new CosmeticSolidBlock(Properties.of().strength(2.0F, 10.0F), true)
   );
   public static final DeferredHolder<Block, Block> MIRROR = REGISTER.register("mirror", () -> new BlockMirror(false));
   public static final DeferredHolder<Block, Block> MIRROR_ESSENCE = REGISTER.register("mirror_essence", () -> new BlockMirror(true));
   public static final DeferredHolder<Block, Block> TAINT_CRUST = REGISTER.register("taint_crust", () -> new BlockTaint(BlockTaint.Type.CRUST));
   public static final DeferredHolder<Block, Block> TAINT_SOIL = REGISTER.register("taint_soil", () -> new BlockTaint(BlockTaint.Type.SOIL));
   public static final DeferredHolder<Block, Block> FLESH_BLOCK = REGISTER.register("flesh_block", () -> new BlockTaint(BlockTaint.Type.FLESH));
   public static final DeferredHolder<Block, Block> FIBROUS_TAINT = REGISTER.register("fibrous_taint", () -> new FibrousTaintBlock());
   public static final DeferredHolder<Block, Block> TAINTED_GRASS = REGISTER.register("tainted_grass", () -> new TaintedGrassBlock());
   public static final DeferredHolder<Block, Block> TAINTED_PLANT = REGISTER.register("tainted_plant", () -> new TaintedPlantBlock());
   public static final DeferredHolder<Block, Block> SPORE_STALK = REGISTER.register("spore_stalk", () -> new BlockTaintFibres(false));
   public static final DeferredHolder<Block, Block> MATURE_SPORE_STALK = REGISTER.register("mature_spore_stalk", () -> new BlockTaintFibres(true));
   public static final DeferredHolder<Block, Block> MANA_BEAN = REGISTER.register("mana_bean", () -> new BlockManaPod());
   private static final Properties TUBE_PROPS = Properties.of().strength(0.5F).noOcclusion().forceSolidOn();
   public static final DeferredHolder<Block, Block> ESSENCE_TUBE = REGISTER.register("essence_tube", () -> new BlockTube(TUBE_PROPS));
   public static final DeferredHolder<Block, Block> ESSENCE_VALVE = REGISTER.register("essence_valve", () -> new BlockValve(TUBE_PROPS));
   public static final DeferredHolder<Block, Block> FILTERED_ESSENCE_TUBE = REGISTER.register("filtered_essence_tube", () -> new BlockTubeFiltered(TUBE_PROPS));
   public static final DeferredHolder<Block, Block> ESSENCE_BUFFER = REGISTER.register("essence_buffer", () -> new BlockTubeBuffer(TUBE_PROPS));
   public static final DeferredHolder<Block, Block> RESTRICTED_ESSENCE_TUBE = REGISTER.register(
      "restricted_essence_tube", () -> new BlockTubeRestrict(TUBE_PROPS)
   );
   public static final DeferredHolder<Block, Block> DIRECTIONAL_ESSENCE_TUBE = REGISTER.register(
      "directional_essence_tube", () -> new BlockTubeOneway(TUBE_PROPS)
   );
   public static final DeferredHolder<Block, Block> ESSENCE_CRYSTALLIZER = REGISTER.register(
      "essence_crystallizer", () -> new EssenceCrystallizerBlock()
   );
   public static final DeferredHolder<Block, Block> MAGIC_BOX = REGISTER.register("magic_box", () -> new BlockMagicBox());
   public static final DeferredHolder<Block, Block> REPAIRER = REGISTER.register("repairer", () -> new RepairerBlock());
   public static final DeferredHolder<Block, Block> ALCHEMICAL_CENTRIFUGE = REGISTER.register("alchemical_centrifuge", () -> new CentrifugeBlock());
   public static final DeferredHolder<Block, Block> ELDRITCH_ALTAR = REGISTER.register("eldritch_altar", () -> new BlockEldritchAltar());
   public static final DeferredHolder<Block, Block> ELDRITCH_TRAP = REGISTER.register("eldritch_trap", () -> new BlockEldritchTrap());
   public static final DeferredHolder<Block, Block> ELDRITCH_CRAB_SPAWNER = REGISTER.register("eldritch_crab_spawner", () -> new BlockEldritchCrabSpawner());
   public static final DeferredHolder<Block, Block> ELDRITCH_OBELISK = REGISTER.register("eldritch_obelisk", () -> new BlockEldritchObelisk());
   public static final DeferredHolder<Block, Block> ELDRITCH_OBELISK_PART = REGISTER.register("eldritch_obelisk_part", () -> new BlockEldritchObeliskPart());
   public static final DeferredHolder<Block, Block> ELDRITCH_CAPSTONE = REGISTER.register(
      "eldritch_capstone", () -> new thaumcraft.block.eldritch.capstone.EldritchCapstoneBlock()
   );
   public static final DeferredHolder<Block, Block> ANCIENT_DOORWAY = REGISTER.register("ancient_doorway", () -> new AncientDoorwayBlock());
   public static final DeferredHolder<Block, Block> ANCIENT_LOCKING_MECHANISM = REGISTER.register("ancient_locking_mechanism", () -> new BlockEldritchLock());
   public static final DeferredHolder<Block, Block> GLOWING_CRUSTED_STONE = REGISTER.register("glowing_crusted_stone", () -> new EndritchBlock());
   public static final DeferredHolder<Block, Block> GLYPHED_STONE = REGISTER.register("glyphed_stone", () -> new EndritchBlock());
   public static final DeferredHolder<Block, Block> RUNED_STONE = REGISTER.register("runed_stone", () -> new EndritchBlock());
   public static final DeferredHolder<Block, Block> ELDRITCH_PORTAL = REGISTER.register("eldritch_portal", () -> new BlockEldritchPortal());
   public static final DeferredHolder<Block, Block> BEDROCK_PORTAL = REGISTER.register("bedrock_portal", () -> new BlockBedrockPortal());
   public static final DeferredHolder<Block, Block> ESSENCE_RESERVOIR = REGISTER.register("essence_reservoir", () -> new BlockEssenceReservoir());
   public static final DeferredHolder<Block, Block> ARCANE_STONE_SLAB = REGISTER.register(
      "arcane_stone_slab", () -> new SlabBlock(Properties.of().strength(2.0F, 10.0F).sound(SoundType.STONE))
   );
   public static final DeferredHolder<Block, Block> ARCANE_STONE_STAIRS = REGISTER.register(
      "arcane_stone_stairs", () -> new StairBlock(((Block)ARCANE_STONE.get()).defaultBlockState(), Properties.of())
   );
   public static final DeferredHolder<Block, Block> ELDRITCH_STONE_SLAB = REGISTER.register(
      "eldritch_stone_slab", () -> new SlabBlock(Properties.of().strength(2.0F, 10.0F).sound(SoundType.STONE))
   );
   public static final DeferredHolder<Block, Block> ELDRITCH_STONE_STAIRS = REGISTER.register(
      "eldritch_stone_stairs", () -> new StairBlock(((Block)ANCIENT_STONE.get()).defaultBlockState(), Properties.of())
   );
   public static final DeferredHolder<Block, Block> ELDRITCH_NOTHING = REGISTER.register("eldritch_nothing", () -> new BlockEldritchNothing());
   public static final DeferredHolder<Block, Block> ADVANCED_ALCHEMY_FURNACE = REGISTER.register("adv_alch", () -> new AdvAlchemyFurnaceBlock());
   public static final DeferredHolder<Block, Block> ADVANCED_ALCHEMY_FURNACE_BURN = REGISTER.register("adv_alch_burn", () -> new AdvAlchemyFurnaceBurnBlock());
   public static final DeferredHolder<Block, Block> ADVANCED_ALCHEMY_FURNACE_TANK = REGISTER.register("adv_alch_tank", () -> new AdvAlchemyFurnaceTankBlock());
   public static final DeferredHolder<Block, Block> LOOT_URN = REGISTER.register(
      "loot_urn",
      () -> new BlockLoot(
         0,
         Properties.of()
            .sound(
               new DeferredSoundType(
                  1.0F,
                  0.7F,
                  ModSounds.URN_BREAK,
                  () -> SoundEvents.DECORATED_POT_STEP,
                  () -> SoundEvents.DECORATED_POT_PLACE,
                  () -> SoundEvents.DECORATED_POT_HIT,
                  () -> SoundEvents.DECORATED_POT_FALL
               )
            )
      )
   );
   public static final DeferredHolder<Block, Block> LOOT_URN_UNCOMMON = REGISTER.register(
      "loot_urn_uncommon",
      () -> new BlockLoot(
         1,
         Properties.of()
            .sound(
               new DeferredSoundType(
                  1.0F,
                  0.7F,
                  ModSounds.URN_BREAK,
                  () -> SoundEvents.DECORATED_POT_STEP,
                  () -> SoundEvents.DECORATED_POT_PLACE,
                  () -> SoundEvents.DECORATED_POT_HIT,
                  () -> SoundEvents.DECORATED_POT_FALL
               )
            )
      )
   );
   public static final DeferredHolder<Block, Block> LOOT_URN_RARE = REGISTER.register(
      "loot_urn_rare",
      () -> new BlockLoot(
         2,
         Properties.of()
            .sound(
               new DeferredSoundType(
                  1.0F,
                  0.7F,
                  ModSounds.URN_BREAK,
                  () -> SoundEvents.DECORATED_POT_STEP,
                  () -> SoundEvents.DECORATED_POT_PLACE,
                  () -> SoundEvents.DECORATED_POT_HIT,
                  () -> SoundEvents.DECORATED_POT_FALL
               )
            )
      )
   );
   public static final DeferredHolder<Block, Block> LOOT_CRATE = REGISTER.register("loot_crate", () -> new BlockLoot(0, Properties.of().sound(SoundType.WOOD)));
   public static final DeferredHolder<Block, Block> LOOT_CRATE_UNCOMMON = REGISTER.register(
      "loot_crate_uncommon", () -> new BlockLoot(1, Properties.of().sound(SoundType.WOOD))
   );
   public static final DeferredHolder<Block, Block> LOOT_CRATE_RARE = REGISTER.register(
      "loot_crate_rare", () -> new BlockLoot(2, Properties.of().sound(SoundType.WOOD))
   );
   public static final DeferredHolder<Block, Block> INFUSION_CLAW = REGISTER.register("infusion_claw", () -> new InfusionClawBlock());

   public static void init(IEventBus bus) {
      REGISTER.register(bus);
   }

   private static <T extends Block> Map<DyeColor, DeferredHolder<Block, Block>> registerColors(String prefix, Function<DyeColor, T> function) {
      Map<DyeColor, DeferredHolder<Block, Block>> map = new HashMap<>();

      for (DyeColor value : DyeColor.values()) {
         DeferredHolder<Block, T> holder = REGISTER.register(value.getName() + "_" + prefix, () -> function.apply(value));
         map.put(value, (DeferredHolder<Block, Block>) (DeferredHolder<?, ?>) holder);
      }

      return map;
   }
}
