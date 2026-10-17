package thaumcraft.data.provider.recipe;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import org.jetbrains.annotations.NotNull;
import thaumcraft.data.builder.InfusionEnchantmentRecipeBuilder;

public class ThaumcraftRecipeGenerator extends RecipeProvider {
   private final List<Consumer<RecipeOutput>> providers;

   public ThaumcraftRecipeGenerator(PackOutput output, CompletableFuture<Provider> registries) {
      super(output, registries);
      this.providers = List.of(
         new CrafingRecipeGenerator(output, registries)::generate,
         new SmeltingRecipeGenerator(output, registries)::generate,
         new ArcaneRecipeGenerator(output, registries)::generate,
         new CrucibleRecipeGenerator(output, registries)::generate,
         new InfusionRecipeGenerator(output, registries)::generate,
         new InfusionEnchantmentRecipeGenerator(output, registries)::generate,
         new FortressArmorUpgradeRecipeGenerator(output, registries)::generate
      );
   }

   protected void buildRecipes(@NotNull RecipeOutput consumer) {
      for (Consumer<RecipeOutput> provider : this.providers) {
         provider.accept(consumer);
      }
   }

   protected void buildRecipes(RecipeOutput consumer, Provider holderLookup) {
      InfusionEnchantmentRecipeBuilder.withEnchantmentLookup(holderLookup.lookupOrThrow(Registries.ENCHANTMENT), () -> this.buildRecipes(consumer));
   }
}
