package thaumcraft.init.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.world.features.AuraNodeFeature;
import thaumcraft.world.features.CinderPearlFeature;
import thaumcraft.world.features.GreatwoodTreeFeature;
import thaumcraft.world.features.HilltopStonesStructureFeature;
import thaumcraft.world.features.ManaPodsFeature;
import thaumcraft.world.features.MoundStructureFeature;
import thaumcraft.world.features.ScatteredStructureNodeFeature;
import thaumcraft.world.features.SilverwoodTreeFeature;
import thaumcraft.world.features.TotemStructureFeature;
import thaumcraft.world.features.EldritchRingFeature;
import thaumcraft.world.features.VisCrystalFeature;
import thaumcraft.world.features.biomes.magical_forest.BigMagicTreeFeature;
import thaumcraft.world.features.biomes.magical_forest.MagicalForestTreesFeature;
import thaumcraft.world.features.biomes.magical_forest.ShimmerleafNearWoodFeature;
import thaumcraft.world.features.biomes.taint.TaintFibresFeature;
import thaumcraft.world.features.dimension.nether.NetherAuraNodeFeature;
import thaumcraft.world.features.dimension.nether.NetherTotemFeature;

public class ModFeatures {
   public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(BuiltInRegistries.FEATURE, "thaumcraft");
   public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> STRUCTURE_TOTEM = FEATURES.register(
      "structure_totem", () -> new TotemStructureFeature(NoneFeatureConfiguration.CODEC)
   );
   public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> STRUCTURE_HILLTOP_STONES = FEATURES.register(
      "structure_hilltop_stones", () -> new HilltopStonesStructureFeature(NoneFeatureConfiguration.CODEC)
   );
   public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> STRUCTURE_MOUND = FEATURES.register(
      "structure_mound", () -> new MoundStructureFeature(NoneFeatureConfiguration.CODEC)
   );
   public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> NETHER_TOTEM = FEATURES.register(
      "nether_totem", () -> new NetherTotemFeature(NoneFeatureConfiguration.CODEC)
   );
   public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> ELDRITCH_RING = FEATURES.register(
      "eldritch_ring", () -> new EldritchRingFeature(NoneFeatureConfiguration.CODEC)
   );
   public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> CINDER_PEARL = FEATURES.register(
      "cinder_pearl", () -> new CinderPearlFeature(NoneFeatureConfiguration.CODEC)
   );
   public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> NETHER_AURA_NODE = FEATURES.register(
      "nether_aura_node", () -> new NetherAuraNodeFeature(NoneFeatureConfiguration.CODEC)
   );
   public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> SCATTERED_STRUCTURE_NODE = FEATURES.register(
      "scattered_structure_node", () -> new ScatteredStructureNodeFeature(NoneFeatureConfiguration.CODEC)
   );
   public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> AURA_NODE = FEATURES.register(
      "aura_node", () -> new AuraNodeFeature(NoneFeatureConfiguration.CODEC)
   );
   public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> VIS_CRYSTAL = FEATURES.register(
      "vis_crystal", () -> new VisCrystalFeature(NoneFeatureConfiguration.CODEC)
   );
   public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> TAINT_FIBRES = FEATURES.register(
      "taint_fibres", () -> new TaintFibresFeature(NoneFeatureConfiguration.CODEC)
   );
   public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> SHIMMERLEAF_NEAR_WOOD = FEATURES.register(
      "shimmerleaf_near_wood", () -> new ShimmerleafNearWoodFeature(NoneFeatureConfiguration.CODEC)
   );
   public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> MANA_PODS = FEATURES.register(
      "mana_pods", () -> new ManaPodsFeature(NoneFeatureConfiguration.CODEC)
   );
   public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> MAGICAL_FOREST_TREES = FEATURES.register(
      "magical_forest_trees", () -> new MagicalForestTreesFeature(NoneFeatureConfiguration.CODEC)
   );
   public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> BIG_MAGIC_TREE = FEATURES.register(
      "big_magic_tree", () -> new BigMagicTreeFeature(NoneFeatureConfiguration.CODEC)
   );
   public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> SILVERWOOD_TREE = FEATURES.register(
      "silverwood_tree", () -> new SilverwoodTreeFeature(NoneFeatureConfiguration.CODEC)
   );
   public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> GREATWOOD_TREE = FEATURES.register(
      "greatwood_tree", () -> new GreatwoodTreeFeature(NoneFeatureConfiguration.CODEC)
   );

   public static void init(IEventBus bus) {
      FEATURES.register(bus);
      Thaumcraft.log.info("[Thaumcraft] Registering world generation features");
   }
}
