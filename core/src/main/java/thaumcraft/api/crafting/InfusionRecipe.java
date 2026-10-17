package thaumcraft.api.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.AspectList;

public class InfusionRecipe implements Recipe<RecipeInput> {
   public static final RecipeType<InfusionRecipe> TYPE = RecipeType.simple(Thaumcraft.location("infusion"));
   public static final RecipeSerializer<InfusionRecipe> SERIALIZER = new InfusionRecipe.Serializer();
   private static final ResourceLocation GENERATED_ID = Thaumcraft.location("generated_infusion");
   public final ResourceLocation id;
   public final AspectList aspects;
   public final String researchKey;
   public final NonNullList<Ingredient> components;
   public final Ingredient central;
   public final ItemStack result;
   public final int instability;

   public InfusionRecipe(
      ResourceLocation id, String researchKey, ItemStack result, int instability, AspectList aspects, Ingredient central, NonNullList<Ingredient> components
   ) {
      this.id = id;
      this.researchKey = researchKey;
      this.result = result;
      this.central = central;
      this.aspects = aspects;
      this.components = components;
      this.instability = instability;
   }

   @NotNull
   public NonNullList<Ingredient> getIngredients() {
      return this.components;
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
      return this.result;
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

   public boolean matches(List<ItemStack> input, ItemStack central) {
      if (input.size() != this.components.size()) {
         return false;
      } else if (!this.central.test(central)) {
         return false;
      } else {
         List<ItemStack> inputs = new ArrayList<>();

         for (ItemStack is : input) {
            inputs.add(is.copy());
         }

         label34:
         for (Ingredient ingredient : this.components) {
            for (int i = 0; i < inputs.size(); i++) {
               if (ingredient.test(inputs.get(i))) {
                  inputs.remove(i);
                  continue label34;
               }
            }

            return false;
         }

         return inputs.isEmpty();
      }
   }

   public ItemStack getResultItem() {
      return this.result;
   }

   public InfusionRecipe(String researchKey, ItemStack result, int instability, AspectList aspects, Ingredient central, NonNullList<Ingredient> components) {
      this(GENERATED_ID, researchKey, result, instability, aspects, central, components);
   }

   public int getInstability() {
      return this.instability;
   }

   public Ingredient getCentral() {
      return this.central;
   }

   public static class Serializer implements RecipeSerializer<InfusionRecipe> {
      @NotNull
      public MapCodec<InfusionRecipe> codec() {
         return RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                  Codec.STRING.fieldOf("research").forGetter(recipe -> recipe.researchKey),
                  ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                  Codec.INT.optionalFieldOf("instability", 0).forGetter(recipe -> recipe.instability),
                  AspectList.CODEC.fieldOf("aspects").forGetter(recipe -> recipe.aspects),
                  Ingredient.CODEC_NONEMPTY.fieldOf("central").forGetter(recipe -> recipe.central),
                  Ingredient.CODEC_NONEMPTY
                     .listOf()
                     .xmap(list -> NonNullList.of(Ingredient.EMPTY, list.toArray(Ingredient[]::new)), Function.identity())
                     .fieldOf("components")
                     .forGetter(recipe -> recipe.components)
               )
               .apply(
                  builder,
                  (researchKey, result, instability, aspects, central, components) -> new InfusionRecipe(
                     researchKey, result, instability, aspects, central, components
                  )
               )
         );
      }

      @NotNull
      public StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> streamCodec() {
         return StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            recipe -> recipe.researchKey,
            ItemStack.STREAM_CODEC,
            recipe -> recipe.result,
            ByteBufCodecs.VAR_INT,
            recipe -> recipe.instability,
            AspectList.STREAM_CODEC,
            recipe -> recipe.aspects,
            Ingredient.CONTENTS_STREAM_CODEC,
            recipe -> recipe.central,
            ByteBufCodecs.collection(NonNullList::createWithCapacity, Ingredient.CONTENTS_STREAM_CODEC),
            recipe -> recipe.components,
            (researchKey, result, instability, aspects, central, components) -> new InfusionRecipe(
               researchKey, result, instability, aspects, central, components
            )
         );
      }
   }
}
