package thaumcraft.init.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.block.advanced_alchemy_furnace.AdvAlchemyFurnaceBlockEntity;
import thaumcraft.block.advanced_alchemy_furnace.AdvAlchemyFurnaceNozzleBlockEntity;
import thaumcraft.block.advanced_node_infuser.AdvancedNodeInfuserBlockEntity;
import thaumcraft.block.airy.aura_node.AuraNodeBlockEntity;
import thaumcraft.block.airy.energized_aura.NodeEnergizedBlockEntity;
import thaumcraft.block.airy.nitor.NitorBlockEntity;
import thaumcraft.block.airy.warding_aura.WardingStoneFenceBlockEntity;
import thaumcraft.block.alchemy_furnace.AlchemyFurnaceBlockEntity;
import thaumcraft.block.arcane_alembic.ArcaneAlembicBlockEntity;
import thaumcraft.block.arcane_door.ArcaneDoorBlockEntity;
import thaumcraft.block.arcane_pedestal.ArcanePedestalBlockEntity;
import thaumcraft.block.arcane_workbench.MagicWorkbenchBlockEntity;
import thaumcraft.block.banner.BannerBlockEntity;
import thaumcraft.block.bellows.BellowsBlockEntity;
import thaumcraft.block.bore.BoreBaseBlockEntity;
import thaumcraft.block.bore.BoreBlockEntity;
import thaumcraft.block.cosmetic.WardingStoneBlockEntity;
import thaumcraft.block.crucible.CrucibleBlockEntity;
import thaumcraft.block.crystal.CrystalClusterBlock;
import thaumcraft.block.crystal.EldritchCrystalBlock;
import thaumcraft.block.deconstruction_table.DeconstructionTableBlockEntity;
import thaumcraft.block.eccence_reservoir.EssenceReservoirBlockEntity;
import thaumcraft.block.eldritch.altar.EldritchAltarBlockEntity;
import thaumcraft.block.eldritch.crab_spawner.EldritchCrabSpawnerBlockEntity;
import thaumcraft.block.eldritch.lock.EldritchLockBlockEntity;
import thaumcraft.block.eldritch.nothing.EldritchNothingBlockEntity;
import thaumcraft.block.eldritch.obelisk.EldritchObeliskBlockEntity;
import thaumcraft.block.eldritch.portal.EldritchPortalBlockEntity;
import thaumcraft.block.eldritch.trap.EldritchTrapBlockEntity;
import thaumcraft.block.encharter.EnchanterBlockEntity;
import thaumcraft.block.essence_crystallizer.EssenceCrystalizerBlockEntity;
import thaumcraft.block.ethereal_bloom.EtherealBloomBlockEntity;
import thaumcraft.block.flux_scrubber.FluxScrubberBlockEntity;
import thaumcraft.block.focal_manipulator.FocalManipulatorBlockEntity;
import thaumcraft.block.funnel.FunnelBlockEntity;
import thaumcraft.block.glimmer_of_light.LightSourceTile;
import thaumcraft.block.grate.GrateBlockEntity;
import thaumcraft.block.hole.HoleBlockEntity;
import thaumcraft.block.hungry_chest.ChestHungryBlockEntity;
import thaumcraft.block.infernal_furnace.InfernalFurnaceBlockEntity;
import thaumcraft.block.infernal_furnace.InfernalFurnaceNozzleBlockEntity;
import thaumcraft.block.infusion_claw.InfusionClawBlockEntity;
import thaumcraft.block.infusion_matrix.RunicMatrixBlockEntity;
import thaumcraft.block.infusion_pillar.InfusionPillarBlockEntity;
import thaumcraft.block.jar.blank.VoidJarBlockEntity;
import thaumcraft.block.jar.brain.BrainJarBlockEntity;
import thaumcraft.block.jar.node.NodeJarBlockEntity;
import thaumcraft.block.jar.standart.JarBlockEntity;
import thaumcraft.block.lamp.ArcaneLampBlockEntity;
import thaumcraft.block.lamp.ArcaneLampLightBlockEntity;
import thaumcraft.block.lamp.fertility.ArcaneLampFertilityBlockEntity;
import thaumcraft.block.lamp.growth.ArcaneLampGrowthBlockEntity;
import thaumcraft.block.lifter.LifterBlockEntity;
import thaumcraft.block.magic_box.MagicBoxBlockEntity;
import thaumcraft.block.magnet.MagnetBlockEntity;
import thaumcraft.block.magnet.MobMagnetBlockEntity;
import thaumcraft.block.mana_pod.ManaPodBlockEntity;
import thaumcraft.block.mirror.MirrorBlockEntity;
import thaumcraft.block.mirror.MirrorEssenceBlockEntity;
import thaumcraft.block.mobilizer.MobilizerBlockEntity;
import thaumcraft.block.mobilizer.MobilizerRelayBlockEntity;
import thaumcraft.block.node_stabilizer.NodeStabilizerBlockEntity;
import thaumcraft.block.node_transducer.NodeTransducerBlockEntity;
import thaumcraft.block.opaque.OwnedBlockEntity;
import thaumcraft.block.platform.PlatformBlockEntity;
import thaumcraft.block.portal.BedrockPortalBlockEntity;
import thaumcraft.block.pressure_plate.PressurePlateBlockEntity;
import thaumcraft.block.repairer.RepairerBlockEntity;
import thaumcraft.block.sensor.SensorBlockEntity;
import thaumcraft.block.spa.SpaBlockEntity;
import thaumcraft.block.summoner.SummonerBlockEntity;
import thaumcraft.block.table.ResearchTableBlockEntity;
import thaumcraft.block.thaumatorium.ThaumatoriumBlockEntity;
import thaumcraft.block.transvector.TransvectorDislocatorBlockEntity;
import thaumcraft.block.transvector.TransvectorInterfaceBlockEntity;
import thaumcraft.block.tube.TubeBlockEntity;
import thaumcraft.block.tube.buffer.TubeBufferBlockEntity;
import thaumcraft.block.tube.centrifuge.CentrifugeBlockEntity;
import thaumcraft.block.tube.filter.TileTubeFilter;
import thaumcraft.block.tube.oneway.TubeOnewayBlockEntity;
import thaumcraft.block.tube.restrict.TileTubeRestrict;
import thaumcraft.block.tube.valve.TubeValveBlockEntity;
import thaumcraft.block.vis_charged_relay.VisChargeRelayBlockEntity;
import thaumcraft.block.vis_relay.VisRelayBlockEntity;
import thaumcraft.block.wand_charger.WandChargerBlockEntity;
import thaumcraft.block.wand_recharger.WandPedestalBlockEntity;
import thaumcraft.block.warded.WardedBlockEntity;
import thaumcraft.block.warp_gate.WarpGateBlockEntity;

public class ModBlockEntities {
   public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "thaumcraft");
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfernalFurnaceBlockEntity>> INFERNAL_FURNACE = register(
      "infernal_furnace", InfernalFurnaceBlockEntity::new, ModBlocks.INFERNAL_FURNACE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrucibleBlockEntity>> CRUCIBLE = register(
      "crucible", CrucibleBlockEntity::new, ModBlocks.CRUCIBLE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcaneAlembicBlockEntity>> ARCANE_ALEMBIC = register(
      "arcane_alembic", ArcaneAlembicBlockEntity::new, ModBlocks.ARCANE_ALEMBIC
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GrateBlockEntity>> GRATE = register("grate", GrateBlockEntity::new, ModBlocks.GRATE);
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcaneLampBlockEntity>> ARCANE_LAMP = register(
      "arcane_lamp", ArcaneLampBlockEntity::new, ModBlocks.ARCANE_LAMP
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcaneLampGrowthBlockEntity>> ARCANE_LAMP_GROWTH = register(
      "arcane_lamp_growth", ArcaneLampGrowthBlockEntity::new, ModBlocks.ARCANE_LAMP_GROWTH
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcaneLampFertilityBlockEntity>> ARCANE_LAMP_FERTILITY = register(
      "arcane_lamp_fertility", ArcaneLampFertilityBlockEntity::new, ModBlocks.ARCANE_LAMP_FERTILITY
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfernalFurnaceNozzleBlockEntity>> INFERNAL_FURNACE_NOZZLE = register(
      "infernal_furnace_nozzle", InfernalFurnaceNozzleBlockEntity::new, ModBlocks.INFERNAL_FURNACE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ThaumatoriumBlockEntity>> THAUMATORIUM = register(
      "thaumatorium", ThaumatoriumBlockEntity::new, ModBlocks.THAUMATORIUM
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VisRelayBlockEntity>> VIS_RELAY = register(
      "vis_relay", VisRelayBlockEntity::new, ModBlocks.VIS_RELAY
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VisChargeRelayBlockEntity>> VIS_CHARGE_RELAY = register(
      "vis_charge_relay", VisChargeRelayBlockEntity::new, ModBlocks.VIS_CHARGE_RELAY
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlchemyFurnaceBlockEntity>> ALCHEMY_FURNACE = register(
      "alchemy_furnace", AlchemyFurnaceBlockEntity::new, ModBlocks.ALCHEMY_FURNACE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcanePedestalBlockEntity>> ARCANE_PEDESTAL = register(
      "arcane_pedestal", ArcanePedestalBlockEntity::new, ModBlocks.ARCANE_PEDESTAL
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RunicMatrixBlockEntity>> RUNIC_MATRIX = register(
      "runic_matrix", RunicMatrixBlockEntity::new, ModBlocks.RUNIC_MATRIX
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfusionPillarBlockEntity>> INFUSION_PILLAR = register(
      "infusion_pillar", InfusionPillarBlockEntity::new, ModBlocks.INFUSION_PILLAR
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WandPedestalBlockEntity>> WAND_PEDESTAL = register(
      "wand_pedestal", WandPedestalBlockEntity::new, ModBlocks.WAND_PEDESTAL
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WandChargerBlockEntity>> WAND_CHARGER = register(
      "wand_charger", WandChargerBlockEntity::new, ModBlocks.WAND_CHARGER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NodeStabilizerBlockEntity>> NODE_STABILIZER = register(
      "node_stabilizer", NodeStabilizerBlockEntity::new, ModBlocks.NODE_STABILIZER, ModBlocks.ADVANCED_NODE_STABILIZER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedNodeInfuserBlockEntity>> ADVANCED_NODE_INFUSER = register(
      "advanced_node_infuser", AdvancedNodeInfuserBlockEntity::new, ModBlocks.ADVANCED_NODE_INFUSER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NodeTransducerBlockEntity>> NODE_TRANSDUCER = register(
      "node_transducer", NodeTransducerBlockEntity::new, ModBlocks.NODE_TRANSDUCER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpaBlockEntity>> ARCANE_SPA = register(
      "arcane_spa", SpaBlockEntity::new, ModBlocks.ARCANE_SPA
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FocalManipulatorBlockEntity>> FOCAL_MANIPULATOR = register(
      "focal_manipulator", FocalManipulatorBlockEntity::new, ModBlocks.FOCAL_MANIPULATOR
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SummonerBlockEntity>> SUMMONER = register(
      "summoner", SummonerBlockEntity::new, ModBlocks.SUMMONER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnchanterBlockEntity>> ENCHANTER = register(
      "enchanter", EnchanterBlockEntity::new, ModBlocks.ENCHANTER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluxScrubberBlockEntity>> FLUX_SCRUBBER = register(
      "flux_scrubber", FluxScrubberBlockEntity::new, ModBlocks.FLUX_SCRUBBER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcaneDoorBlockEntity>> ARCANE_DOOR = register(
      "arcane_door", ArcaneDoorBlockEntity::new, ModBlocks.ARCANE_DOOR
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BellowsBlockEntity>> ARCANE_BELLOWS = register(
      "arcane_bellows", BellowsBlockEntity::new, ModBlocks.ARCANE_BELLOWS
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SensorBlockEntity>> ARCANE_SENSOR = register(
      "arcane_sensor", SensorBlockEntity::new, ModBlocks.ARCANE_SENSOR
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PressurePlateBlockEntity>> ARCANE_PRESSURE_PLATE = register(
      "arcane_pressure_plate", PressurePlateBlockEntity::new, ModBlocks.ARCANE_PRESSURE_PLATE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoreBaseBlockEntity>> ARCANE_BORE_BASE = register(
      "arcane_bore_base", BoreBaseBlockEntity::new, ModBlocks.ARCANE_BORE_BASE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoreBlockEntity>> ARCANE_BORE = register(
      "arcane_bore", BoreBlockEntity::new, ModBlocks.ARCANE_BORE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OwnedBlockEntity>> OWNED = register(
      "owned", OwnedBlockEntity::new, ModBlocks.WARDED_GLASS
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BannerBlockEntity>> BANNER = register(
      "banner", BannerBlockEntity::new, (Collection<DeferredHolder<Block, Block>>)Util.make(() -> {
         List<DeferredHolder<Block, Block>> list = new ArrayList<>(ModBlocks.BANNER.values());
         list.add(ModBlocks.CRIMSON_CULT_BANNER);
         list.addAll(ModBlocks.WALL_BANNER.values());
         list.add(ModBlocks.CRIMSON_CULT_WALL_BANNER);
         return list;
      })
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvAlchemyFurnaceNozzleBlockEntity>> ADVANCED_ALCHEMY_FURNACE_NOZZLE = register(
      "advanced_alchemy_furnace_nozzle", AdvAlchemyFurnaceNozzleBlockEntity::new, ModBlocks.ADVANCED_ALCHEMY_FURNACE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResearchTableBlockEntity>> RESEARCH_TABLE = register(
      "research_table", ResearchTableBlockEntity::new, ModBlocks.RESEARCH_TABLE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeconstructionTableBlockEntity>> DECONSTRUCTION_TABLE = register(
      "deconstruction_table", DeconstructionTableBlockEntity::new, ModBlocks.DECONSTRUCTION_TABLE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MagicWorkbenchBlockEntity>> ARCANE_WORKBENCH = register(
      "arcane_workbench", MagicWorkbenchBlockEntity::new, ModBlocks.ARCANE_WORKBENCH
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChestHungryBlockEntity>> HUNGRY_CHEST = register(
      "hungry_chest", ChestHungryBlockEntity::new, ModBlocks.HUNGRY_CHEST
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FunnelBlockEntity>> FUNNEL = register(
      "funnel", FunnelBlockEntity::new, ModBlocks.FUNNEL
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<JarBlockEntity>> JAR = register("jar", JarBlockEntity::new, ModBlocks.JAR);
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BrainJarBlockEntity>> BRAIN_JAR = register(
      "brain_jar", BrainJarBlockEntity::new, ModBlocks.BRAIN_JAR
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NodeJarBlockEntity>> NODE_JAR = register(
      "node_jar", NodeJarBlockEntity::new, ModBlocks.NODE_JAR
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<JarBlockEntity>> VOID_JAR = register(
      "void_jar", VoidJarBlockEntity::new, ModBlocks.VOID_JAR
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WardedBlockEntity>> WARDED = register(
      "warded", WardedBlockEntity::new, ModBlocks.WARDED
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LifterBlockEntity>> LIFTER = register(
      "lifter", LifterBlockEntity::new, ModBlocks.LIFTER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AuraNodeBlockEntity>> AURA_NODE = register(
      "aura_node", AuraNodeBlockEntity::new, ModBlocks.AURA_NODE, ModBlocks.SILVERWOOD_LOG_NODE, ModBlocks.CHARGED_OBSIDIAN_TOTEM
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NitorBlockEntity>> NITOR = register("nitor", NitorBlockEntity::new, ModBlocks.NITOR);
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LightSourceTile>> GLIMMER_OF_LIGHT = register(
      "glimmer_of_light", LightSourceTile::new, ModBlocks.GLIMMER_OF_LIGHT
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WardingStoneFenceBlockEntity>> WARDING_AURA = register(
      "warding_aura", WardingStoneFenceBlockEntity::new, ModBlocks.WARDING_AURA
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NodeEnergizedBlockEntity>> ENERGIZED_AURA_NODE = register(
      "energized_aura_node", NodeEnergizedBlockEntity::new, ModBlocks.ENERGIZED_AURA_NODE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrystalClusterBlock.CrystalClusterBlockEntity>> CRYSTAL_CLUSTER = register(
      "crystal_cluster",
      CrystalClusterBlock.CrystalClusterBlockEntity::new,
      ModBlocks.AIR_CRYSTAL_CLUSTER,
      ModBlocks.FIRE_CRYSTAL_CLUSTER,
      ModBlocks.WATER_CRYSTAL_CLUSTER,
      ModBlocks.EARTH_CRYSTAL_CLUSTER,
      ModBlocks.ORDER_CRYSTAL_CLUSTER,
      ModBlocks.ENTROPY_CRYSTAL_CLUSTER,
      ModBlocks.MIXED_CRYSTAL_CLUSTER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EldritchCrystalBlock.EldritchCrystalBlockEntity>> STRANGE_CRYSTALS = register(
      "strange_crystals", EldritchCrystalBlock.EldritchCrystalBlockEntity::new, ModBlocks.STRANGE_CRYSTALS
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MirrorBlockEntity>> MIRROR = register(
      "mirror", MirrorBlockEntity::new, ModBlocks.MIRROR
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MirrorEssenceBlockEntity>> MIRROR_ESSENCE = register(
      "mirror_essence", MirrorEssenceBlockEntity::new, ModBlocks.MIRROR_ESSENCE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CentrifugeBlockEntity>> ALCHEMICAL_CENTRIFUGE = register(
      "alchemical_centrifuge", CentrifugeBlockEntity::new, ModBlocks.ALCHEMICAL_CENTRIFUGE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TubeBlockEntity>> TUBE = register(
      "tube", TubeBlockEntity::new, ModBlocks.ESSENCE_TUBE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TubeBufferBlockEntity>> TUBE_BUFFER = register(
      "tube_buffer", TubeBufferBlockEntity::new, ModBlocks.ESSENCE_BUFFER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TubeValveBlockEntity>> TUBE_VALVE = register(
      "tube_valve", TubeValveBlockEntity::new, ModBlocks.ESSENCE_VALVE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileTubeFilter>> TUBE_FILTER = register(
      "tube_filter", TileTubeFilter::new, ModBlocks.FILTERED_ESSENCE_TUBE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileTubeRestrict>> TUBE_RESTRICT = register(
      "tube_restrict", TileTubeRestrict::new, ModBlocks.RESTRICTED_ESSENCE_TUBE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TubeOnewayBlockEntity>> TUBE_ONEWAY = register(
      "tube_oneway", TubeOnewayBlockEntity::new, ModBlocks.DIRECTIONAL_ESSENCE_TUBE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MagicBoxBlockEntity>> MAGIC_BOX = register(
      "magic_box", MagicBoxBlockEntity::new, ModBlocks.MAGIC_BOX
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RepairerBlockEntity>> REPAIRER = register(
      "repairer", RepairerBlockEntity::new, ModBlocks.REPAIRER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WarpGateBlockEntity>> WARP_GATE = register(
      "warp_gate", WarpGateBlockEntity::new, ModBlocks.WARP_GATE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EldritchPortalBlockEntity>> ELDRITCH_PORTAL = register(
      "eldritch_portal", EldritchPortalBlockEntity::new, ModBlocks.ELDRITCH_PORTAL
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BedrockPortalBlockEntity>> BEDROCK_PORTAL = register(
      "bedrock_portal", BedrockPortalBlockEntity::new, ModBlocks.BEDROCK_PORTAL
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EssenceReservoirBlockEntity>> ESSENCE_RESERVOIR = register(
      "essence_reservoir", EssenceReservoirBlockEntity::new, ModBlocks.ESSENCE_RESERVOIR
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TransvectorInterfaceBlockEntity>> TRANSVECTOR_INTERFACE = register(
      "transvector_interface", TransvectorInterfaceBlockEntity::new, ModBlocks.TRANSVECTOR_INTERFACE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TransvectorDislocatorBlockEntity>> TRANSVECTOR_DISLOCATOR = register(
      "transvector_dislocator", TransvectorDislocatorBlockEntity::new, ModBlocks.TRANSVECTOR_DISLOCATOR
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MobilizerBlockEntity>> MOBILIZER = register(
      "mobilizer", MobilizerBlockEntity::new, ModBlocks.MOBILIZER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MobilizerRelayBlockEntity>> MOBILIZER_RELAY = register(
      "mobilizer_relay", MobilizerRelayBlockEntity::new, ModBlocks.MOBILIZER_RELAY
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MagnetBlockEntity>> MAGNET = register(
      "magnet", MagnetBlockEntity::new, ModBlocks.MAGNET
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MobMagnetBlockEntity>> MOB_MAGNET = register(
      "mob_magnet", MobMagnetBlockEntity::new, ModBlocks.MOB_MAGNET
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvAlchemyFurnaceBlockEntity>> ADVANCED_ALCHEMY_FURNACE = register(
      "adv_alch", AdvAlchemyFurnaceBlockEntity::new, ModBlocks.ADVANCED_ALCHEMY_FURNACE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfusionClawBlockEntity>> INFUSION_CLAW = register(
      "infusion_claw", InfusionClawBlockEntity::new, ModBlocks.INFUSION_CLAW
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EldritchNothingBlockEntity>> ELDRITCH_NOTHING = register(
      "eldritch_nothing", EldritchNothingBlockEntity::new, ModBlocks.ELDRITCH_NOTHING
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EldritchObeliskBlockEntity>> ELDRITCH_OBELISK = register(
      "eldritch_obelisk", EldritchObeliskBlockEntity::new, ModBlocks.ELDRITCH_OBELISK
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EldritchTrapBlockEntity>> ELDRITCH_TRAP = register(
      "eldritch_trap", EldritchTrapBlockEntity::new, ModBlocks.ELDRITCH_TRAP
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EldritchAltarBlockEntity>> ELDRITCH_ALTAR = register(
      "eldritch_altar", EldritchAltarBlockEntity::new, ModBlocks.ELDRITCH_ALTAR
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EldritchCrabSpawnerBlockEntity>> ELDRITCH_CRAB_SPAWNER = register(
      "eldritch_crab_spawner", EldritchCrabSpawnerBlockEntity::new, ModBlocks.ELDRITCH_CRAB_SPAWNER
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EldritchLockBlockEntity>> ELDRITCH_LOCK = register(
      "ancient_locking_mechanism", EldritchLockBlockEntity::new, ModBlocks.ANCIENT_LOCKING_MECHANISM
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ManaPodBlockEntity>> MANA_POD = register(
      "mana_pod", ManaPodBlockEntity::new, ModBlocks.MANA_BEAN
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcaneLampLightBlockEntity>> ARCANE_LAMP_LIGHT = register(
      "arcane_lamp_light", ArcaneLampLightBlockEntity::new, ModBlocks.GLIMMER_OF_LIGHT
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EtherealBloomBlockEntity>> ETHEREAL_BLOOM = register(
      "ethereal_bloom", EtherealBloomBlockEntity::new, ModBlocks.ETHEREAL_BLOOM
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WardingStoneBlockEntity>> WARDING_STONE = register(
      "warding_stone", WardingStoneBlockEntity::new, ModBlocks.WARDING_STONE
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlatformBlockEntity>> PLATFORM = register(
      "platform", PlatformBlockEntity::new, ModBlocks.PLATFORM
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HoleBlockEntity>> HOLE = register("hole", HoleBlockEntity::new, ModBlocks.HOLE);
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EssenceCrystalizerBlockEntity>> ESSENTIA_CRYSTALLIZER = register(
      "essentia_crystallizer", EssenceCrystalizerBlockEntity::new, ModBlocks.ESSENCE_CRYSTALLIZER
   );

   private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(
      String name, BlockEntitySupplier<T> supplier, DeferredHolder<?, ?>... blocks
   ) {
      return REGISTER.register(name, () -> Builder.of(supplier, Arrays.stream(blocks).map(DeferredHolder::get).toArray(Block[]::new)).build(null));
   }

   private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(
      String name, BlockEntitySupplier<T> supplier, Collection<DeferredHolder<Block, Block>> collection
   ) {
      return REGISTER.register(name, () -> Builder.of(supplier, collection.stream().map(DeferredHolder::get).toArray(Block[]::new)).build(null));
   }
}
