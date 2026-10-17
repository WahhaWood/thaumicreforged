package thaumcraft.data.provider.recipe;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.crafting.FortressArmorUpgradeRecipe;
import thaumcraft.data.builder.FortressArmorUpgradeRecipeBuilder;
import thaumcraft.init.registry.ModItems;

public class FortressArmorUpgradeRecipeGenerator extends RecipeProvider {
   public FortressArmorUpgradeRecipeGenerator(PackOutput output, CompletableFuture<Provider> registries) {
      super(output, registries);
   }

   public void generate(@NotNull RecipeOutput consumer) {
      this.buildRecipes(consumer);
   }

   protected void buildRecipes(@NotNull RecipeOutput consumer) {
      FortressArmorUpgradeRecipeBuilder.fortress(
            "helm_goggles", FortressArmorUpgradeRecipe.UpgradeKind.GOGGLES, (ItemLike)ModItems.THAUMIUM_FORTRESS_HELMET.get()
         )
         .aspect(Aspect.SENSES, 32)
         .aspect(Aspect.AURA, 16)
         .aspect(Aspect.ARMOR, 16)
         .instability(5)
         .requires(Items.SLIME_BALL)
         .requires((ItemLike)ModItems.GOGGLES_REVEALING.get())
         .save(consumer, Thaumcraft.location("fortress/helm_goggles"));
      FortressArmorUpgradeRecipeBuilder.fortress(
            "mask_grinning_devil", FortressArmorUpgradeRecipe.UpgradeKind.MASK_GRINNING_DEVIL, (ItemLike)ModItems.THAUMIUM_FORTRESS_HELMET.get()
         )
         .aspect(Aspect.MIND, 64)
         .aspect(Aspect.HEAL, 64)
         .aspect(Aspect.ARMOR, 16)
         .instability(8)
         .requires(net.neoforged.neoforge.common.Tags.Items.DYES_RED)
         .requires(Items.IRON_INGOT)
         .requires(Items.LEATHER)
         .requires((ItemLike)ModItems.CINDER_PEARL.get())
         .requires((ItemLike)ModItems.ZOMBIE_BRAIN.get())
         .requires(Items.IRON_INGOT)
         .save(consumer, Thaumcraft.location("fortress/mask_grinning_devil"));
      FortressArmorUpgradeRecipeBuilder.fortress(
            "mask_angry_ghost", FortressArmorUpgradeRecipe.UpgradeKind.MASK_ANGRY_GHOST, (ItemLike)ModItems.THAUMIUM_FORTRESS_HELMET.get()
         )
         .aspect(Aspect.ENTROPY, 64)
         .aspect(Aspect.DEATH, 64)
         .aspect(Aspect.ARMOR, 16)
         .instability(8)
         .requires(net.neoforged.neoforge.common.Tags.Items.DYES_BLACK)
         .requires(Items.IRON_INGOT)
         .requires(Items.LEATHER)
         .requires(Items.POISONOUS_POTATO)
         .requires(Items.WITHER_SKELETON_SKULL)
         .requires(Items.IRON_INGOT)
         .save(consumer, Thaumcraft.location("fortress/mask_angry_ghost"));
      FortressArmorUpgradeRecipeBuilder.fortress(
            "mask_sipping_fiend", FortressArmorUpgradeRecipe.UpgradeKind.MASK_SIPPING_FIEND, (ItemLike)ModItems.THAUMIUM_FORTRESS_HELMET.get()
         )
         .aspect(Aspect.UNDEAD, 64)
         .aspect(Aspect.LIFE, 64)
         .aspect(Aspect.ARMOR, 16)
         .instability(8)
         .requires(net.neoforged.neoforge.common.Tags.Items.DYES_ORANGE)
         .requires(Items.IRON_INGOT)
         .requires(Items.LEATHER)
         .requires(Items.GHAST_TEAR)
         .requires(Items.MILK_BUCKET)
         .requires(Items.IRON_INGOT)
         .save(consumer, Thaumcraft.location("fortress/mask_sipping_fiend"));
   }
}
