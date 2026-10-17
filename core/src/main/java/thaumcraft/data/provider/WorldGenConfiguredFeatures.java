package thaumcraft.data.provider;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModFeatures;

public class WorldGenConfiguredFeatures {
   public static Holder<ConfiguredFeature<?, ?>> ORE_CINNABAR;
   public static Holder<ConfiguredFeature<?, ?>> ORE_AMBER;
   public static Holder<ConfiguredFeature<?, ?>> ORE_AIR_SHARD;
   public static Holder<ConfiguredFeature<?, ?>> ORE_FIRE_SHARD;
   public static Holder<ConfiguredFeature<?, ?>> ORE_WATER_SHARD;
   public static Holder<ConfiguredFeature<?, ?>> ORE_EARTH_SHARD;
   public static Holder<ConfiguredFeature<?, ?>> ORE_ORDER_SHARD;
   public static Holder<ConfiguredFeature<?, ?>> ORE_ENTROPY_SHARD;
   public static Holder<ConfiguredFeature<?, ?>> VIS_CRYSTAL;
   public static Holder<ConfiguredFeature<?, ?>> GREATWOOD;
   public static Holder<ConfiguredFeature<?, ?>> SILVERWOOD;
   public static Holder<ConfiguredFeature<?, ?>> BIG_MAGIC_TREE;
   public static Holder<ConfiguredFeature<?, ?>> VISHROOM;
   public static Holder<ConfiguredFeature<?, ?>> MAGICAL_FOREST_TREES;
   public static Holder<ConfiguredFeature<?, ?>> MAGICAL_FOREST_BLOBS;
   public static Holder<ConfiguredFeature<?, ?>> TAINT_BLOBS;
   public static Holder<ConfiguredFeature<?, ?>> MAGICAL_FOREST_MANA_PODS;
   public static Holder<ConfiguredFeature<?, ?>> MAGICAL_FOREST_SHIMMERLEAF;
   public static Holder<ConfiguredFeature<?, ?>> TAINT_FIBRES;
   public static Holder<ConfiguredFeature<?, ?>> AURA_NODE;
   public static Holder<ConfiguredFeature<?, ?>> SCATTERED_STRUCTURE_NODE;
   public static Holder<ConfiguredFeature<?, ?>> NETHER_AURA_NODE;
   public static Holder<ConfiguredFeature<?, ?>> CINDER_PEARL;
   public static Holder<ConfiguredFeature<?, ?>> NETHER_TOTEM;
   public static Holder<ConfiguredFeature<?, ?>> STRUCTURE_MOUND;
   public static Holder<ConfiguredFeature<?, ?>> STRUCTURE_HILLTOP_STONES;
   public static Holder<ConfiguredFeature<?, ?>> STRUCTURE_TOTEM;
   public static Holder<ConfiguredFeature<?, ?>> ELDRITCH_RING;
   public static Holder<ConfiguredFeature<?, ?>> MAGICAL_FOREST_MUSHROOMS;

   public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> ctx) {
      HolderGetter<ConfiguredFeature<?, ?>> cfgLookup = ctx.lookup(Registries.CONFIGURED_FEATURE);
      ORE_CINNABAR = ctx.register(createKey("ore_cinnabar"), createOreInStoneAndDeepslate(ModBlocks.CINNABAR_ORE, 3));
      ORE_AMBER = ctx.register(createKey("ore_amber"), createOreInStoneAndDeepslate(ModBlocks.AMBER_ORE, 3));
      ORE_AIR_SHARD = ctx.register(createKey("ore_air_shard"), createOreInStoneAndDeepslate(ModBlocks.AIR_SHARD_ORE, 6));
      ORE_FIRE_SHARD = ctx.register(createKey("ore_fire_shard"), createOreInStoneAndDeepslate(ModBlocks.FIRE_SHARD_ORE, 6));
      ORE_WATER_SHARD = ctx.register(createKey("ore_water_shard"), createOreInStoneAndDeepslate(ModBlocks.WATER_SHARD_ORE, 6));
      ORE_EARTH_SHARD = ctx.register(createKey("ore_earth_shard"), createOreInStoneAndDeepslate(ModBlocks.EARTH_SHARD_ORE, 6));
      ORE_ORDER_SHARD = ctx.register(createKey("ore_order_shard"), createOreInStoneAndDeepslate(ModBlocks.ORDER_SHARD_ORE, 6));
      ORE_ENTROPY_SHARD = ctx.register(createKey("ore_entropy_shard"), createOreInStoneAndDeepslate(ModBlocks.ENTROPY_SHARD_ORE, 6));
      VIS_CRYSTAL = ctx.register(createKey("vis_crystal"), createNoneConfig(ModFeatures.VIS_CRYSTAL));
      GREATWOOD = ctx.register(createKey("greatwood_tree"), createNoneConfig(ModFeatures.GREATWOOD_TREE));
      MAGICAL_FOREST_MUSHROOMS = ctx.register(
         createKey("magical_forest_mushrooms"),
         new ConfiguredFeature(
            Feature.RANDOM_BOOLEAN_SELECTOR,
            new RandomBooleanFeatureConfiguration(
               PlacementUtils.inlinePlaced(cfgLookup.getOrThrow(TreeFeatures.HUGE_BROWN_MUSHROOM), new PlacementModifier[0]),
               PlacementUtils.inlinePlaced(cfgLookup.getOrThrow(TreeFeatures.HUGE_RED_MUSHROOM), new PlacementModifier[0])
            )
         )
      );
      SILVERWOOD = ctx.register(createKey("silverwood_tree"), createNoneConfig(ModFeatures.SILVERWOOD_TREE));
      BIG_MAGIC_TREE = ctx.register(createKey("big_magic_tree"), createNoneConfig(ModFeatures.BIG_MAGIC_TREE));
      MAGICAL_FOREST_TREES = ctx.register(createKey("magical_forest_trees"), createNoneConfig(ModFeatures.MAGICAL_FOREST_TREES));
      MAGICAL_FOREST_BLOBS = ctx.register(
         createKey("magical_forest_blobs"),
         new ConfiguredFeature(Feature.FOREST_ROCK, new BlockStateConfiguration(Blocks.MOSSY_COBBLESTONE.defaultBlockState()))
      );
      TAINT_BLOBS = ctx.register(
         createKey("taint_blobs"),
         new ConfiguredFeature(Feature.FOREST_ROCK, new BlockStateConfiguration(((Block)ModBlocks.TAINT_CRUST.get()).defaultBlockState()))
      );
      MAGICAL_FOREST_MANA_PODS = ctx.register(createKey("magical_forest_mana_pods"), createNoneConfig(ModFeatures.MANA_PODS));
      MAGICAL_FOREST_SHIMMERLEAF = ctx.register(createKey("magical_forest_shimmerleaf"), createNoneConfig(ModFeatures.SHIMMERLEAF_NEAR_WOOD));
      TAINT_FIBRES = ctx.register(createKey("taint_fibres"), createNoneConfig(ModFeatures.TAINT_FIBRES));
      AURA_NODE = ctx.register(createKey("aura_node"), createNoneConfig(ModFeatures.AURA_NODE));
      SCATTERED_STRUCTURE_NODE = ctx.register(createKey("scattered_structure_node"), createNoneConfig(ModFeatures.SCATTERED_STRUCTURE_NODE));
      NETHER_AURA_NODE = ctx.register(createKey("nether_aura_node"), createNoneConfig(ModFeatures.NETHER_AURA_NODE));
      CINDER_PEARL = ctx.register(createKey("cinder_pearl"), createNoneConfig(ModFeatures.CINDER_PEARL));
      NETHER_TOTEM = ctx.register(createKey("nether_totem"), createNoneConfig(ModFeatures.NETHER_TOTEM));
      STRUCTURE_MOUND = ctx.register(createKey("structure_mound"), createNoneConfig(ModFeatures.STRUCTURE_MOUND));
      STRUCTURE_HILLTOP_STONES = ctx.register(createKey("structure_hilltop_stones"), createNoneConfig(ModFeatures.STRUCTURE_HILLTOP_STONES));
      STRUCTURE_TOTEM = ctx.register(createKey("structure_totem"), createNoneConfig(ModFeatures.STRUCTURE_TOTEM));
      ELDRITCH_RING = ctx.register(createKey("eldritch_ring"), createNoneConfig(ModFeatures.ELDRITCH_RING));
      VISHROOM = ctx.register(
         createKey("vishroom"),
         new ConfiguredFeature(
            Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(((Block)ModBlocks.VISHROOM.get()).defaultBlockState()))
         )
      );
   }

   private static ResourceKey<ConfiguredFeature<?, ?>> createKey(String name) {
      return ResourceKey.create(Registries.CONFIGURED_FEATURE, Thaumcraft.location(name));
   }

   private static ConfiguredFeature<?, ?> createOre(Supplier<Block> ore, int size) {
      return new ConfiguredFeature(
         Feature.ORE,
         new OreConfiguration(List.of(OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), ore.get().defaultBlockState())), size)
      );
   }

   private static ConfiguredFeature<?, ?> createOreInStoneAndDeepslate(Supplier<Block> ore, int size) {
      return new ConfiguredFeature(
         Feature.ORE,
         new OreConfiguration(
            List.of(
               OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), ore.get().defaultBlockState()),
               OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), ore.get().defaultBlockState())
            ),
            size
         )
      );
   }

   private static ConfiguredFeature<?, ?> createNoneConfig(Supplier<Feature<NoneFeatureConfiguration>> supplier) {
      return new ConfiguredFeature(supplier.get(), NoneFeatureConfiguration.INSTANCE);
   }
}
