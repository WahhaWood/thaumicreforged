package thaumcraft.data.provider;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.Config;

public class WorldGenPlacedFeatures {
   public static Holder<PlacedFeature> ORE_CINNABAR;
   public static Holder<PlacedFeature> ORE_AMBER;
   public static Holder<PlacedFeature> ORE_AIR_SHARD;
   public static Holder<PlacedFeature> ORE_FIRE_SHARD;
   public static Holder<PlacedFeature> ORE_WATER_SHARD;
   public static Holder<PlacedFeature> ORE_EARTH_SHARD;
   public static Holder<PlacedFeature> ORE_ORDER_SHARD;
   public static Holder<PlacedFeature> ORE_ENTROPY_SHARD;
   public static Holder<PlacedFeature> MAGICAL_FOREST_TREES;
   public static Holder<PlacedFeature> VISHROOM;
   public static Holder<PlacedFeature> MAGICAL_FOREST_BLOBS;
   public static Holder<PlacedFeature> TAINT_BLOBS;
   public static Holder<PlacedFeature> MAGICAL_FOREST_MANA_PODS;
   public static Holder<PlacedFeature> MAGICAL_FOREST_SHIMMERLEAF;
   public static Holder<PlacedFeature> TAINT_FIBRES;
   public static Holder<PlacedFeature> AURA_NODE;
   public static Holder<PlacedFeature> SCATTERED_STRUCTURE_NODE;
   public static Holder<PlacedFeature> NETHER_AURA_NODE;
   public static Holder<PlacedFeature> CINDER_PEARL;
   public static Holder<PlacedFeature> NETHER_TOTEM;
   public static Holder<PlacedFeature> GREATWOOD_TREE;
   public static Holder<PlacedFeature> SILVERWOOD_TREE;
   public static Holder<PlacedFeature> BIG_MAGIC_TREE;
   public static Holder<PlacedFeature> TAINT_LAND_TREES;
   public static Holder<PlacedFeature> VIS_CRYSTAL;
   public static Holder<PlacedFeature> MAGICAL_FOREST_MUSHROOMS;

   public static void bootstrap(BootstrapContext<PlacedFeature> ctx) {
      HolderGetter<ConfiguredFeature<?, ?>> cfg = ctx.lookup(Registries.CONFIGURED_FEATURE);
      ORE_CINNABAR = ctx.register(createKey("ore_cinnabar"), createPlaced(WorldGenConfiguredFeatures.ORE_CINNABAR, 20, false, -64, 60));
      ORE_AMBER = ctx.register(createKey("ore_amber"), createPlaced(WorldGenConfiguredFeatures.ORE_AMBER, 20, false, -64, 60));
      ORE_AIR_SHARD = ctx.register(createKey("ore_air_shard"), createPlaced(WorldGenConfiguredFeatures.ORE_AIR_SHARD, 8, true, -64, 300));
      ORE_FIRE_SHARD = ctx.register(createKey("ore_fire_shard"), createPlaced(WorldGenConfiguredFeatures.ORE_FIRE_SHARD, 6, true, -64, 60));
      ORE_WATER_SHARD = ctx.register(createKey("ore_water_shard"), createPlaced(WorldGenConfiguredFeatures.ORE_WATER_SHARD, 6, true, -64, 80));
      ORE_EARTH_SHARD = ctx.register(createKey("ore_earth_shard"), createPlaced(WorldGenConfiguredFeatures.ORE_EARTH_SHARD, 8, true, -64, 120));
      ORE_ORDER_SHARD = ctx.register(createKey("ore_order_shard"), createPlaced(WorldGenConfiguredFeatures.ORE_ORDER_SHARD, 4, true, -64, 20));
      ORE_ENTROPY_SHARD = ctx.register(createKey("ore_entropy_shard"), createPlaced(WorldGenConfiguredFeatures.ORE_ENTROPY_SHARD, 4, true, -64, 40));
      MAGICAL_FOREST_BLOBS = ctx.register(
         createKey("magical_forest_blobs"), createStructureHeighmap(WorldGenConfiguredFeatures.MAGICAL_FOREST_BLOBS, CountPlacement.of(UniformInt.of(0, 2)))
      );
      MAGICAL_FOREST_TREES = ctx.register(
         createKey("magical_forest_trees"), createStructureHeighmap(WorldGenConfiguredFeatures.MAGICAL_FOREST_TREES, CountPlacement.of(2))
      );
      MAGICAL_FOREST_MANA_PODS = ctx.register(
         createKey("magical_forest_mana_pods"), createStructureHeighmap(WorldGenConfiguredFeatures.MAGICAL_FOREST_MANA_PODS, CountPlacement.of(4))
      );
      MAGICAL_FOREST_SHIMMERLEAF = ctx.register(
         createKey("magical_forest_shimmerleaf"), createStructureHeighmap(WorldGenConfiguredFeatures.MAGICAL_FOREST_SHIMMERLEAF, CountPlacement.of(8))
      );
      TAINT_BLOBS = ctx.register(
         createKey("taint_blobs"), createStructureHeighmap(WorldGenConfiguredFeatures.TAINT_BLOBS, CountPlacement.of(UniformInt.of(0, 2)))
      );
      TAINT_FIBRES = ctx.register(createKey("taint_fibres"), createStructureHeighmap(WorldGenConfiguredFeatures.TAINT_FIBRES, CountPlacement.of(20)));
      AURA_NODE = ctx.register(createKey("aura_node"), createStructureHeighmap(WorldGenConfiguredFeatures.AURA_NODE, Config.nodeRarity));
      SCATTERED_STRUCTURE_NODE = ctx.register(
         createKey("scattered_structure_node"),
         new PlacedFeature(WorldGenConfiguredFeatures.SCATTERED_STRUCTURE_NODE, List.of(CountPlacement.of(1), InSquarePlacement.spread(), BiomeFilter.biome()))
      );
      CINDER_PEARL = ctx.register(
         createKey("cinder_pearl"),
         new PlacedFeature(
            WorldGenConfiguredFeatures.CINDER_PEARL,
            List.of(RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), HeightmapPlacement.onHeightmap(Types.WORLD_SURFACE), BiomeFilter.biome())
         )
      );
      // Кластеры кристаллов не генерируются (в TC4 их собирает игрок), но
      // плейсмент остаётся доступным для будущих применений.
      VIS_CRYSTAL = ctx.register(createKey("vis_crystal"), createStructureHeightRange(WorldGenConfiguredFeatures.VIS_CRYSTAL, CountPlacement.of(1), -55, 280));
      NETHER_AURA_NODE = ctx.register(createKey("nether_aura_node"), createStructureHeightRange(WorldGenConfiguredFeatures.NETHER_AURA_NODE, 10, 10, 120));
      NETHER_TOTEM = ctx.register(createKey("nether_totem"), createStructureHeightRange(WorldGenConfiguredFeatures.NETHER_TOTEM, 180, 20, 120));
      GREATWOOD_TREE = ctx.register(createKey("greatwood_tree"), createStructureHeighmap(WorldGenConfiguredFeatures.GREATWOOD, 120));
      SILVERWOOD_TREE = ctx.register(createKey("silverwood_tree"), createStructureHeighmap(WorldGenConfiguredFeatures.SILVERWOOD, 60));
      BIG_MAGIC_TREE = ctx.register(createKey("big_magic_tree"), createStructureHeighmap(WorldGenConfiguredFeatures.BIG_MAGIC_TREE, 4));
      TAINT_LAND_TREES = ctx.register(
         createKey("taint_land_trees"),
         new PlacedFeature(
            cfg.getOrThrow(ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.withDefaultNamespace("oak"))),
            List.of(
               CountPlacement.of(2),
               InSquarePlacement.spread(),
               SurfaceWaterDepthFilter.forMaxDepth(0),
               HeightmapPlacement.onHeightmap(Types.WORLD_SURFACE),
               BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.defaultBlockState(), BlockPos.ZERO)),
               BiomeFilter.biome()
            )
         )
      );
      MAGICAL_FOREST_MUSHROOMS = ctx.register(
         createKey("magical_forest_mushrooms"),
         new PlacedFeature(
            WorldGenConfiguredFeatures.MAGICAL_FOREST_MUSHROOMS,
            List.of(CountPlacement.of(1), InSquarePlacement.spread(), HeightmapPlacement.onHeightmap(Types.MOTION_BLOCKING), BiomeFilter.biome())
         )
      );
      VISHROOM = ctx.register(
         createKey("vishroom"),
         new PlacedFeature(
            WorldGenConfiguredFeatures.VISHROOM,
            List.of(
               RarityFilter.onAverageOnceEvery(32),
               InSquarePlacement.spread(),
               HeightmapPlacement.onHeightmap(Types.WORLD_SURFACE),
               BlockPredicateFilter.forPredicate(
                  BlockPredicate.allOf(
                     BlockPredicate.replaceable(),
                     BlockPredicate.matchesBlocks(
                        new BlockPos(0, -1, 0),
                        new Block[]{Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.MYCELIUM, Blocks.MOSS_BLOCK}
                     )
                  )
               ),
               BiomeFilter.biome()
            )
         )
      );
   }

   private static ResourceKey<PlacedFeature> createKey(String name) {
      return ResourceKey.create(Registries.PLACED_FEATURE, Thaumcraft.location(name));
   }

   private static PlacedFeature createPlaced(Holder<ConfiguredFeature<?, ?>> holder, int count, boolean trapezoid, int min, int max) {
      VerticalAnchor minAnchor = VerticalAnchor.absolute(min);
      VerticalAnchor maxAnchor = VerticalAnchor.absolute(max);
      return new PlacedFeature(
         holder,
         List.of(
            CountPlacement.of(count),
            InSquarePlacement.spread(),
            HeightRangePlacement.of((HeightProvider)(trapezoid ? TrapezoidHeight.of(minAnchor, maxAnchor) : UniformHeight.of(minAnchor, maxAnchor))),
            BiomeFilter.biome()
         )
      );
   }

   private static PlacedFeature createStructureHeighmap(Holder<ConfiguredFeature<?, ?>> holder, int chance) {
      return createStructureHeighmap(holder, RarityFilter.onAverageOnceEvery(chance));
   }

   private static PlacedFeature createStructureHeighmap(Holder<ConfiguredFeature<?, ?>> holder, PlacementModifier first) {
      return new PlacedFeature(
         holder, List.of(first, InSquarePlacement.spread(), HeightmapPlacement.onHeightmap(Types.MOTION_BLOCKING_NO_LEAVES), BiomeFilter.biome())
      );
   }

   private static PlacedFeature createStructureHeightRange(Holder<ConfiguredFeature<?, ?>> holder, int chance, int min, int max) {
      return createStructureHeightRange(holder, RarityFilter.onAverageOnceEvery(chance), min, max);
   }

   private static PlacedFeature createStructureHeightRange(Holder<ConfiguredFeature<?, ?>> holder, PlacementModifier first, int min, int max) {
      return new PlacedFeature(
         holder,
         List.of(
            first, InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max)), BiomeFilter.biome()
         )
      );
   }
}
