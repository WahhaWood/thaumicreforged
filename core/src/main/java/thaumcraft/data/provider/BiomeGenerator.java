package thaumcraft.data.provider;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeBuilder;
import net.minecraft.world.level.biome.MobSpawnSettings.Builder;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.levelgen.GenerationStep.Carving;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.Nullable;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModBiomes;
import thaumcraft.init.registry.ModEntities;

public class BiomeGenerator {
   private static final Music FOREST_MUSIC = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FOREST);

   public static void bootstrapBiomes(BootstrapContext<Biome> context) {
      HolderGetter<ConfiguredWorldCarver<?>> carverGetter = context.lookup(Registries.CONFIGURED_CARVER);
      HolderGetter<PlacedFeature> placedFeatureGetter = context.lookup(Registries.PLACED_FEATURE);
      context.register(ModBiomes.TAINT, createTaintBiome(placedFeatureGetter, carverGetter));
      context.register(ModBiomes.EERIE, createEerieBiome(placedFeatureGetter, carverGetter));
      context.register(ModBiomes.MAGICAL_FOREST, createMagicalForestBiome(placedFeatureGetter, carverGetter));
      context.register(ModBiomes.ELDRITCH_LANDS, createEldritchLandsBiome(placedFeatureGetter, carverGetter));
   }

   public static Biome createMagicalForestBiome(HolderGetter<PlacedFeature> placed, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
      Builder spawns = new Builder();
      BiomeDefaultFeatures.commonSpawns(spawns);
      BiomeDefaultFeatures.farmAnimals(spawns);
      spawns.addSpawn(MobCategory.CREATURE, new SpawnerData(EntityType.WOLF, 2, 1, 3));
      spawns.addSpawn(MobCategory.CREATURE, new SpawnerData(EntityType.HORSE, 2, 1, 3));
      spawns.addSpawn(MobCategory.MONSTER, new SpawnerData(EntityType.WITCH, 3, 1, 1));
      spawns.addSpawn(MobCategory.MONSTER, new SpawnerData(EntityType.ENDERMAN, 3, 1, 1));
      if (Config.spawnPech) {
         spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.PECH.get(), 10, 1, 2));
      }

      if (Config.spawnWisp) {
         spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.WISP.get(), 10, 1, 2));
      }

      // Таум-слайм: плейсмент был, спавна не было — только яйцо.
      spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.THAUMIC_SLIME.get(), 3, 1, 1));

      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder gen = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder(placed, carvers);
      globalOverworldGenerationNoLavaLakes(gen);
      BiomeDefaultFeatures.addDefaultOres(gen);
      BiomeDefaultFeatures.addDefaultSoftDisks(gen);
      gen.addFeature(Decoration.VEGETAL_DECORATION, WorldGenPlacedFeatures.MAGICAL_FOREST_TREES);
      BiomeDefaultFeatures.addDefaultFlowers(gen);
      BiomeDefaultFeatures.addForestGrass(gen);
      BiomeDefaultFeatures.addDefaultMushrooms(gen);
      gen.addFeature(Decoration.VEGETAL_DECORATION, WorldGenPlacedFeatures.MAGICAL_FOREST_MUSHROOMS);
      gen.addFeature(Decoration.LOCAL_MODIFICATIONS, WorldGenPlacedFeatures.MAGICAL_FOREST_BLOBS);
      gen.addFeature(Decoration.VEGETAL_DECORATION, WorldGenPlacedFeatures.MAGICAL_FOREST_MANA_PODS);
      gen.addFeature(Decoration.VEGETAL_DECORATION, WorldGenPlacedFeatures.MAGICAL_FOREST_SHIMMERLEAF);
      gen.addFeature(Decoration.VEGETAL_DECORATION, WorldGenPlacedFeatures.VISHROOM);
      int grass = Config.blueBiome ? 6728396 : 5635969;
      int foliage = Config.blueBiome ? 7851246 : 6750149;
      return biomeWithColorOverrides(true, 0.7F, 0.6F, 30702, 30702, grass, foliage, spawns, gen, FOREST_MUSIC);
   }

   public static Biome createTaintBiome(HolderGetter<PlacedFeature> placed, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
      Builder spawns = new Builder();
      if (Config.spawnTaintacle) {
         spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.TENTACLE.get(), 1, 1, 1));
      }

      // Таинт-звери: плейсменты были, а спавнов нет — жили только на яйцах.
      spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.TAINT_CHICKEN.get(), 6, 1, 2));
      spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.TAINT_COW.get(), 6, 1, 2));
      spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.TAINT_CREEPER.get(), 4, 1, 1));
      spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.TAINT_PIG.get(), 6, 1, 2));
      spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.TAINT_SHEEP.get(), 6, 1, 2));
      spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.TAINT_SPIDER.get(), 4, 1, 2));
      spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.TENTACLE_SMALL.get(), 4, 1, 1));
      spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.TAINT_SWARM.get(), 2, 1, 1));
      spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.TAINT_VILLAGER.get(), 2, 1, 1));

      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder gen = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder(placed, carvers);
      globalOverworldGenerationNoLavaLakes(gen);
      BiomeDefaultFeatures.addDefaultOres(gen);
      gen.addFeature(Decoration.LOCAL_MODIFICATIONS, WorldGenPlacedFeatures.TAINT_BLOBS);
      gen.addFeature(Decoration.VEGETAL_DECORATION, WorldGenPlacedFeatures.TAINT_FIBRES);
      gen.addFeature(Decoration.VEGETAL_DECORATION, WorldGenPlacedFeatures.BIG_MAGIC_TREE);
      gen.addFeature(Decoration.VEGETAL_DECORATION, WorldGenPlacedFeatures.TAINT_LAND_TREES);
      return biomeWithColorOverrides(true, 0.5F, 0.5F, 13373832, 13373832, 7160201, 7160201, 8154503, 8144127, spawns, gen, null);
   }

   public static Biome createEerieBiome(HolderGetter<PlacedFeature> placed, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
      Builder spawns = new Builder();
      BiomeDefaultFeatures.commonSpawns(spawns);
      spawns.addSpawn(MobCategory.AMBIENT, new SpawnerData(EntityType.BAT, 3, 1, 1));
      spawns.addSpawn(MobCategory.MONSTER, new SpawnerData(EntityType.WITCH, 8, 1, 1));
      spawns.addSpawn(MobCategory.MONSTER, new SpawnerData(EntityType.ENDERMAN, 4, 1, 1));
      if (Config.spawnAngryZombie) {
         spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.BRAINY_ZOMBIE.get(), 32, 1, 1));
         spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.GIANT_BRAINY_ZOMBIE.get(), 8, 1, 1));
         spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.INHABITED_ZOMBIE.get(), 12, 1, 1));
      }

      if (Config.spawnWisp) {
         spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.WISP.get(), 3, 1, 1));
      }

      if (Config.spawnElder) {
         spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.ELDRITCH_GUARDIAN.get(), 1, 1, 1));
      }

      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder gen = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder(placed, carvers);
      globalOverworldGenerationNoLavaLakes(gen);
      BiomeDefaultFeatures.addDefaultOres(gen);
      BiomeDefaultFeatures.addDefaultSoftDisks(gen);
      BiomeDefaultFeatures.addForestGrass(gen);
      BiomeDefaultFeatures.addDefaultMushrooms(gen);
      return biomeWithColorOverrides(true, 0.5F, 0.5F, 3035999, 3035999, 4212800, 4212800, 4215616, 2237081, spawns, gen, null);
   }

   public static Biome createEldritchLandsBiome(HolderGetter<PlacedFeature> placed, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
      Builder spawns = new Builder();
      spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.INHABITED_ZOMBIE.get(), 1, 1, 1));
      spawns.addSpawn(MobCategory.MONSTER, new SpawnerData((EntityType)ModEntities.ELDRITCH_GUARDIAN.get(), 1, 1, 1));
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder gen = new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder(placed, carvers);
      gen.addCarver(Carving.AIR, Carvers.CAVE);
      return biome(false, 0.5F, 0.0F, 4159204, 329011, 1118481, 0, spawns, gen, null);
   }

   private static void globalOverworldGenerationNoLavaLakes(net.minecraft.world.level.biome.BiomeGenerationSettings.Builder builder) {
      builder.addCarver(Carving.AIR, Carvers.CAVE);
      builder.addCarver(Carving.AIR, Carvers.CAVE_EXTRA_UNDERGROUND);
      builder.addCarver(Carving.AIR, Carvers.CANYON);
      BiomeDefaultFeatures.addDefaultCrystalFormations(builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(builder);
      BiomeDefaultFeatures.addDefaultSprings(builder);
      BiomeDefaultFeatures.addSurfaceFreezing(builder);
   }

   private static Biome biomeWithColorOverrides(
      boolean hasPrecipitation,
      float temperature,
      float downfall,
      int waterColor,
      int waterFogColor,
      int grassColor,
      int foliageColor,
      Builder spawnBuilder,
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder biomeBuilder,
      @Nullable Music music
   ) {
      return new BiomeBuilder()
         .hasPrecipitation(hasPrecipitation)
         .temperature(temperature)
         .downfall(downfall)
         .specialEffects(
            new net.minecraft.world.level.biome.BiomeSpecialEffects.Builder()
               .waterColor(waterColor)
               .waterFogColor(waterFogColor)
               .fogColor(12638463)
               .grassColorOverride(grassColor)
               .foliageColorOverride(foliageColor)
               .skyColor(calculateSkyColor(temperature))
               .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
               .backgroundMusic(music)
               .build()
         )
         .mobSpawnSettings(spawnBuilder.build())
         .generationSettings(biomeBuilder.build())
         .build();
   }

   private static Biome biomeWithColorOverrides(
      boolean hasPrecipitation,
      float temperature,
      float downfall,
      int waterColor,
      int waterFogColor,
      int fogColor,
      int grassColor,
      int foliageColor,
      int skyColor,
      Builder spawnBuilder,
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder biomeBuilder,
      @Nullable Music music
   ) {
      return new BiomeBuilder()
         .hasPrecipitation(hasPrecipitation)
         .temperature(temperature)
         .downfall(downfall)
         .specialEffects(
            new net.minecraft.world.level.biome.BiomeSpecialEffects.Builder()
               .waterColor(waterColor)
               .waterFogColor(waterFogColor)
               .fogColor(fogColor)
               .grassColorOverride(grassColor)
               .foliageColorOverride(foliageColor)
               .skyColor(skyColor)
               .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
               .backgroundMusic(music)
               .build()
         )
         .mobSpawnSettings(spawnBuilder.build())
         .generationSettings(biomeBuilder.build())
         .build();
   }

   private static Biome biome(
      boolean hasPrecipitation,
      float temperature,
      float downfall,
      int waterColor,
      int waterFogColor,
      int fogColor,
      int skyColor,
      Builder spawnBuilder,
      net.minecraft.world.level.biome.BiomeGenerationSettings.Builder biomeBuilder,
      @Nullable Music music
   ) {
      return new BiomeBuilder()
         .hasPrecipitation(hasPrecipitation)
         .temperature(temperature)
         .downfall(downfall)
         .specialEffects(
            new net.minecraft.world.level.biome.BiomeSpecialEffects.Builder()
               .waterColor(waterColor)
               .waterFogColor(waterFogColor)
               .fogColor(fogColor)
               .skyColor(skyColor)
               .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
               .backgroundMusic(music)
               .build()
         )
         .mobSpawnSettings(spawnBuilder.build())
         .generationSettings(biomeBuilder.build())
         .build();
   }

   protected static int calculateSkyColor(float temp) {
      float t = temp / 3.0F;
      t = Mth.clamp(t, -1.0F, 1.0F);
      return Mth.hsvToRgb(0.62222224F - t * 0.05F, 0.5F + t * 0.1F, 1.0F);
   }
}
