package thaumcraft.api.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.AspectList;

public class InfusionEnchantmentRecipe implements IInfusionModifierRecipe {
   public static final RecipeType<InfusionEnchantmentRecipe> TYPE = RecipeType.simple(Thaumcraft.location("infusion_enchantment"));
   public static final RecipeSerializer<InfusionEnchantmentRecipe> SERIALIZER = new InfusionEnchantmentRecipe.Serializer();
   private static final ResourceLocation GENERATED_ID = Thaumcraft.location("generated_infusion_enchantment");
   public final ResourceLocation id;
   public final AspectList aspects;
   public final String researchKey;
   public final NonNullList<Ingredient> components;
   public final Holder<Enchantment> enchantment;
   public final int recipeXP;
   public final int instability;

   public InfusionEnchantmentRecipe(
      ResourceLocation id, String researchKey, Holder<Enchantment> enchantment, int instability, AspectList aspects, NonNullList<Ingredient> components
   ) {
      this.id = id;
      this.researchKey = researchKey;
      this.enchantment = enchantment;
      this.aspects = aspects;
      this.components = components;
      this.instability = instability;
      this.recipeXP = enchantment.isBound() ? Math.max(1, ((Enchantment)enchantment.value()).getMinCost(1) / 3) : 1;
   }

   public boolean matches(@NotNull RecipeInput container, @NotNull Level level) {
      return false;
   }

   @NotNull
   public ItemStack assemble(@NotNull RecipeInput container, @NotNull Provider registryAccess) {
      return ItemStack.EMPTY;
   }

   public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
      return false;
   }

   @NotNull
   public ItemStack getResultItem(@NotNull Provider registryAccess) {
      return ItemStack.EMPTY;
   }

   @NotNull
   public ResourceLocation getId() {
      return this.id;
   }

   @NotNull
   public RecipeSerializer<?> getSerializer() {
      return SERIALIZER;
   }

   @NotNull
   public RecipeType<?> getType() {
      return TYPE;
   }

   @Override
   public boolean matches(List<ItemStack> components, ItemStack central) {
      if (((Enchantment)this.enchantment.value()).canEnchant(central) && !central.getEnchantments().isEmpty()) {
         ItemEnchantments enchantments = central.getEnchantments();

         for (Entry<Holder<Enchantment>, Integer> entry : enchantments.entrySet()) {
            Holder<Enchantment> key = entry.getKey();
            if (key.is(this.enchantment)) {
               if (entry.getValue() >= ((Enchantment)key.value()).getMaxLevel()) {
                  return false;
               }
            } else if (!Enchantment.areCompatible(this.enchantment, key)) {
               return false;
            }
         }

         ArrayList<ItemStack> stacks = new ArrayList<>();

         for (ItemStack is : components) {
            stacks.add(is.copy());
         }

         label45:
         for (Ingredient ingredient : this.components) {
            for (int a = 0; a < stacks.size(); a++) {
               if (ingredient.test(stacks.get(a))) {
                  stacks.remove(a);
                  continue label45;
               }
            }

            return false;
         }

         return stacks.size() == 0;
      } else {
         return false;
      }
   }

   public Enchantment getEnchantment() {
      return (Enchantment)this.enchantment.value();
   }

   @Override
   public AspectList getAspects() {
      return this.aspects;
   }

   @Override
   public ItemStack modifierItem(ItemStack stack) {
      ItemStack copy = stack.copy();
      copy.enchant(this.enchantment, EnchantmentHelper.getTagEnchantmentLevel(this.enchantment, copy) + 1);
      return copy;
   }

   @Override
   public int getInstability() {
      return this.instability;
   }

   @Override
   public String getResearchKey() {
      return this.researchKey;
   }

   @Override
   public List<Ingredient> getIngredientList() {
      return this.components;
   }

   @Override
   public int getExperienceCost(ItemStack central) {
      return this.calcXP(central);
   }

   public int calcInstability(ItemStack recipeInput) {
      int sum = 0;

      for (Integer value : recipeInput.getEnchantments().entrySet().stream().map(Entry::getValue).toList()) {
         sum += value;
      }

      return sum / 2 + this.instability;
   }

   public int calcXP(ItemStack recipeInput) {
      return this.recipeXP * (1 + EnchantmentHelper.getTagEnchantmentLevel(this.enchantment, recipeInput));
   }

   public float getEssenceMod(ItemStack recipeInput) {
      float mod = 0.0F;
      Map<Holder<Enchantment>, Integer> map1 = recipeInput.getEnchantments().entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));

      for (Entry<Holder<Enchantment>, Integer> entry : map1.entrySet()) {
         if (!entry.getKey().is(this.enchantment)) {
            mod += entry.getValue().intValue() * 0.1F;
         } else {
            mod += entry.getValue().intValue();
         }
      }

      return mod;
   }

   public InfusionEnchantmentRecipe(
      String researchKey, Holder<Enchantment> enchantment, int instability, AspectList aspects, NonNullList<Ingredient> components
   ) {
      this(GENERATED_ID, researchKey, enchantment, instability, aspects, components);
   }

   public static class Serializer implements RecipeSerializer<InfusionEnchantmentRecipe> {
      @NotNull
      public MapCodec<InfusionEnchantmentRecipe> codec() {
         return RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                  Codec.STRING.fieldOf("research").forGetter(recipe -> recipe.researchKey),
                  Enchantment.CODEC.fieldOf("enchantment").forGetter(recipe -> recipe.enchantment),
                  Codec.INT.optionalFieldOf("instability", 0).forGetter(recipe -> recipe.instability),
                  AspectList.CODEC.fieldOf("aspects").forGetter(recipe -> recipe.aspects),
                  Ingredient.CODEC_NONEMPTY
                     .listOf()
                     .xmap(list -> NonNullList.of(Ingredient.EMPTY, list.toArray(Ingredient[]::new)), Function.identity())
                     .fieldOf("components")
                     .forGetter(recipe -> recipe.components)
               )
               .apply(
                  builder,
                  (researchKey, enchantment, instability, aspects, components) -> new InfusionEnchantmentRecipe(
                     researchKey, enchantment, instability, aspects, components
                  )
               )
         );
      }

      @NotNull
      public StreamCodec<RegistryFriendlyByteBuf, InfusionEnchantmentRecipe> streamCodec() {
         return StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            recipe -> recipe.researchKey,
            Enchantment.STREAM_CODEC,
            recipe -> recipe.enchantment,
            ByteBufCodecs.VAR_INT,
            recipe -> recipe.instability,
            AspectList.STREAM_CODEC,
            recipe -> recipe.aspects,
            ByteBufCodecs.collection(NonNullList::createWithCapacity, Ingredient.CONTENTS_STREAM_CODEC),
            recipe -> recipe.components,
            (researchKey, enchantment, instability, aspects, components) -> new InfusionEnchantmentRecipe(
               researchKey, enchantment, instability, aspects, components
            )
         );
      }
   }
}
