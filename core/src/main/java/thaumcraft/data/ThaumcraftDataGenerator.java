package thaumcraft.data;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.DataGenerator.PackGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries.Keys;
import thaumcraft.data.provider.BiomeGenerator;
import thaumcraft.data.provider.BiomeTagsGenerator;
import thaumcraft.data.provider.BlockLootGenerator;
import thaumcraft.data.provider.BlockTagsGenerator;
import thaumcraft.data.provider.EntityTypeTagsGenerator;
import thaumcraft.data.provider.ItemTagsGenerator;
import thaumcraft.data.provider.ModBiomeModifiers;
import thaumcraft.data.provider.ResearchGenerator;
import thaumcraft.data.provider.TubeModelsAndBlockstatesGenerator;
import thaumcraft.data.provider.WorldGenConfiguredFeatures;
import thaumcraft.data.provider.WorldGenPlacedFeatures;
import thaumcraft.data.provider.recipe.ThaumcraftRecipeGenerator;
import thaumcraft.init.registry.ModEnchantment;

@EventBusSubscriber(
   modid = "thaumcraft",
   bus = Bus.MOD
)
public class ThaumcraftDataGenerator {
   private static final RegistrySetBuilder WORLDGEN = new RegistrySetBuilder()
      .add(Registries.CONFIGURED_FEATURE, WorldGenConfiguredFeatures::bootstrap)
      .add(Registries.PLACED_FEATURE, WorldGenPlacedFeatures::bootstrap)
      // Структуры (type thaumcraft:code_built) ведутся ручными JSON в
      // worldgen/structure + structure_set; пустой init.registry.ModStructures
      // удалён, тип регистрирует world.structure.ModStructures.
      .add(Registries.BIOME, BiomeGenerator::bootstrapBiomes)
      .add(Registries.ENCHANTMENT, ModEnchantment::bootstrap)
      .add(Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap);

   @SubscribeEvent
   public static void onGatherData(GatherDataEvent event) {
      DataGenerator gen = event.getGenerator();
      PackGenerator generator = gen.getVanillaPack(true);
      PackOutput packOutput = gen.getPackOutput();
      CompletableFuture<Provider> lookupProvider = event.getLookupProvider();
      ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
      BlockTagsGenerator blockTags = new BlockTagsGenerator(packOutput, lookupProvider, existingFileHelper);
      gen.addProvider(event.includeServer(), blockTags);
      gen.addProvider(event.includeServer(), new ItemTagsGenerator(packOutput, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
      gen.addProvider(event.includeServer(), new BiomeTagsGenerator(packOutput, lookupProvider, existingFileHelper));
      gen.addProvider(event.includeServer(), new EntityTypeTagsGenerator(packOutput, lookupProvider, existingFileHelper));
      // Тег damage_type:bypasses_armor живёт только в src/main/resources/data/minecraft/tags/,
      // генератора для него намеренно нет: runData не доходит до тегов, потому что
      // CrafingRecipeGenerator спотыкается о recipe-баг с нугетами (result 2 при maxStackSize 1),
      // а два владельца одного файла в джарнике конфликтуют по порядку сборки.
      DatapackBuiltinEntriesProvider datapackEntries = new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, WORLDGEN, Set.of("thaumcraft"));
      gen.addProvider(event.includeServer(), datapackEntries);
      lookupProvider = datapackEntries.getRegistryProvider();
      gen.addProvider(event.includeServer(), new ThaumcraftRecipeGenerator(packOutput, lookupProvider));
      gen.addProvider(
         event.includeServer(),
         new LootTableProvider(packOutput, Set.of(), List.of(new SubProviderEntry(BlockLootGenerator::new, LootContextParamSets.BLOCK)), lookupProvider)
      );
      generator.addProvider(ResearchGenerator::new);
      gen.addProvider(event.includeClient(), new TubeModelsAndBlockstatesGenerator(packOutput, existingFileHelper));
   }
}
