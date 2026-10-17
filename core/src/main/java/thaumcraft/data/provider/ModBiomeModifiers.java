package thaumcraft.data.provider;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers.AddFeaturesBiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers.AddSpawnsBiomeModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries.Keys;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModEntities;

public class ModBiomeModifiers {
   public static void bootstrap(BootstrapContext<BiomeModifier> context) {
      HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
      context.register(
         createKey("ores"),
         createOre(
            biomes,
            WorldGenPlacedFeatures.ORE_AMBER,
            WorldGenPlacedFeatures.ORE_CINNABAR,
            WorldGenPlacedFeatures.ORE_AIR_SHARD,
            WorldGenPlacedFeatures.ORE_FIRE_SHARD,
            WorldGenPlacedFeatures.ORE_WATER_SHARD,
            WorldGenPlacedFeatures.ORE_EARTH_SHARD,
            WorldGenPlacedFeatures.ORE_ORDER_SHARD,
            WorldGenPlacedFeatures.ORE_ENTROPY_SHARD
         )
      );
      // Кластеры кристаллов в TC4 НЕ генерируются: по вики это декоративные блоки,
      // которые игрок собирает из кристаллов ("can be combined together to form
      // crystal clusters... adding a touch of class"). Здесь была выдуманная
      // генерация count=4 на высоте -55..280 — кластеры росли в камне под землёй.
      // Фича и плейсмент оставлены (пригодится), но из мира убраны.
      context.register(createKey("add_greatwood_overworld"), createFeatures(biomes, BiomeTags.IS_OVERWORLD, Decoration.VEGETAL_DECORATION, WorldGenPlacedFeatures.GREATWOOD_TREE));
      context.register(
         createKey("add_silverwood_overworld"),
         createFeatures(
            biomes,
            thaumcraft.world.biomes.BiomeHandler.biomeTag("thaumcraft", "allows_silverwood"),
            Decoration.VEGETAL_DECORATION,
            WorldGenPlacedFeatures.SILVERWOOD_TREE
         )
      );
      context.register(
         createKey("add_aura_node_overworld"),
         createFeatures(biomes, BiomeTags.IS_OVERWORLD, Decoration.TOP_LAYER_MODIFICATION, WorldGenPlacedFeatures.AURA_NODE)
      );
      context.register(
         createKey("add_cinder_pearl_overworld"),
         createFeatures(biomes, BiomeTags.IS_OVERWORLD, Decoration.VEGETAL_DECORATION, WorldGenPlacedFeatures.CINDER_PEARL)
      );
      context.register(
         createKey("add_nether_aura_node"),
         createFeatures(biomes, BiomeTags.IS_NETHER, Decoration.TOP_LAYER_MODIFICATION, WorldGenPlacedFeatures.NETHER_AURA_NODE)
      );
      context.register(
         createKey("add_nether_totem"),
         createFeatures(biomes, BiomeTags.IS_NETHER, Decoration.LOCAL_MODIFICATIONS, WorldGenPlacedFeatures.NETHER_TOTEM)
      );
      // Наблюдатель (водный страж): плейсмент IN_WATER был, а спавна не было
      // нигде — только яйцо. Редкий спавн в океанах и реках.
      context.register(
         createKey("watcher_ocean_spawns"),
         new AddSpawnsBiomeModifier(biomes.getOrThrow(BiomeTags.IS_OCEAN), List.of(new SpawnerData((EntityType)ModEntities.WATCHER.get(), 4, 1, 2)))
      );
      context.register(
         createKey("watcher_river_spawns"),
         new AddSpawnsBiomeModifier(biomes.getOrThrow(BiomeTags.IS_RIVER), List.of(new SpawnerData((EntityType)ModEntities.WATCHER.get(), 4, 1, 1)))
      );
      if (Config.spawnFireBat) {
         context.register(createKey("fire_bat_spawns"), createSpawn(biomes, ModEntities.FIRE_BAT, 10, 1, 3));
      }

      if (Config.spawnWisp) {
         context.register(createKey("wisp_nether_spawns"), createSpawn(biomes, ModEntities.WISP, 5, 1, 1));
      }
   }

   @SafeVarargs
   private static BiomeModifier createFeatures(
      HolderGetter<Biome> lookup, net.minecraft.tags.TagKey<Biome> biomes, Decoration step, Holder<PlacedFeature>... placed
   ) {
      return new AddFeaturesBiomeModifier(lookup.getOrThrow(biomes), HolderSet.direct(placed), step);
   }

   private static ResourceKey<BiomeModifier> createKey(String name) {
      return ResourceKey.create(Keys.BIOME_MODIFIERS, Thaumcraft.location(name));
   }

   @SafeVarargs
   private static BiomeModifier createOre(HolderGetter<Biome> lookup, Holder<PlacedFeature>... placed) {
      return new AddFeaturesBiomeModifier(lookup.getOrThrow(BiomeTags.IS_OVERWORLD), HolderSet.direct(placed), Decoration.UNDERGROUND_ORES);
   }

   private static BiomeModifier createSpawn(HolderGetter<Biome> lookup, Holder<EntityType<?>> holder, int weight, int min, int max) {
      return new AddSpawnsBiomeModifier(lookup.getOrThrow(BiomeTags.IS_NETHER), List.of(new SpawnerData((EntityType)holder.value(), weight, min, max)));
   }
}
