package thaumcraft.data.provider;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import thaumcraft.init.registry.ModBiomes;
import thaumcraft.world.biomes.BiomeHandler;

public class BiomeTagsGenerator extends BiomeTagsProvider {
   public BiomeTagsGenerator(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
      super(output, lookupProvider, "thaumcraft", existingFileHelper);
   }

   protected void addTags(@NotNull Provider provider) {
      this.tag(BiomeHandler.biomeTag("thaumcraft", "magical_forest")).addOptional(ModBiomes.MAGICAL_FOREST.location());
      this.tag(BiomeHandler.biomeTag("thaumcraft", "taint")).addOptional(ModBiomes.TAINT.location());
      this.tag(BiomeHandler.biomeTag("thaumcraft", "allows_silverwood")).addOptional(ModBiomes.MAGICAL_FOREST.location()).addOptional(ModBiomes.EERIE.location());
      this.tag(BiomeTags.IS_OVERWORLD).addOptional(ModBiomes.MAGICAL_FOREST.location()).addOptional(ModBiomes.TAINT.location()).addOptional(ModBiomes.EERIE.location());
   }
}
