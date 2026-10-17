package thaumcraft.init.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.FortressArmorUpgradeRecipe;
import thaumcraft.api.crafting.InfusionEnchantmentRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.recipe.InfusionRunicAugmentRecipe;
import thaumcraft.recipe.ShapedNBTRecipe;
import thaumcraft.recipe.ShapelessNBTRecipe;
import thaumcraft.recipe.SpellClothRecipe;
import thaumcraft.recipe.TagSmeltingRecipe;

public class ModRecipes {
   private static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, "thaumcraft");
   private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, "thaumcraft");
   public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> SPELL_CLOTH_SERIALIZER = SERIALIZERS.register(
      "spellcloth", () -> new SimpleCraftingRecipeSerializer(SpellClothRecipe::new)
   );
   public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> SHAPED_NBT_SERIALIZER = SERIALIZERS.register(
      "shaped_nbt", ShapedNBTRecipe.Serializer::new
   );
   public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> SHAPELESS_NBT_SERIALIZER = SERIALIZERS.register(
      "shapeless_nbt", ShapelessNBTRecipe.Serializer::new
   );

   public static void init(IEventBus bus) {
      REGISTER.register(bus);
      SERIALIZERS.register(bus);
      SERIALIZERS.register("tag_smelting", () -> TagSmeltingRecipe.SERIALIZER);
      REGISTER.register("crucible", () -> CrucibleRecipe.TYPE);
      SERIALIZERS.register("crucible", () -> CrucibleRecipe.SERIALIZER);
      REGISTER.register("infusion", () -> InfusionRecipe.TYPE);
      SERIALIZERS.register("infusion", () -> InfusionRecipe.SERIALIZER);
      REGISTER.register("shaped_arcane", () -> ShapedArcaneRecipe.TYPE);
      SERIALIZERS.register("shaped_arcane", () -> ShapedArcaneRecipe.SERIALIZER);
      REGISTER.register("shapeless_arcane", () -> ShapelessArcaneRecipe.TYPE);
      SERIALIZERS.register("shapeless_arcane", () -> ShapelessArcaneRecipe.SERIALIZER);
      REGISTER.register("runic_augmentation", () -> InfusionRunicAugmentRecipe.TYPE);
      SERIALIZERS.register("runic_augmentation", () -> InfusionRunicAugmentRecipe.SERIALIZER);
      REGISTER.register("infusion_enchantment", () -> InfusionEnchantmentRecipe.TYPE);
      SERIALIZERS.register("infusion_enchantment", () -> InfusionEnchantmentRecipe.SERIALIZER);
      REGISTER.register("fortress_armor_upgrade", () -> FortressArmorUpgradeRecipe.TYPE);
      SERIALIZERS.register("fortress_armor_upgrade", () -> FortressArmorUpgradeRecipe.SERIALIZER);
      ThaumcraftApi.registerInfusionModifierRecipeType(InfusionRunicAugmentRecipe.TYPE);
      ThaumcraftApi.registerInfusionModifierRecipeType(InfusionEnchantmentRecipe.TYPE);
      ThaumcraftApi.registerInfusionModifierRecipeType(FortressArmorUpgradeRecipe.TYPE);
   }
}
