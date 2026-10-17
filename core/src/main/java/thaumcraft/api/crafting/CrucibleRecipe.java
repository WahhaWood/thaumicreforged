package thaumcraft.api.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class CrucibleRecipe implements Recipe<RecipeInput> {
   public static final RecipeType<CrucibleRecipe> TYPE = RecipeType.simple(Thaumcraft.location("crucible"));
   public static final RecipeSerializer<CrucibleRecipe> SERIALIZER = new CrucibleRecipe.Serializer();
   private static final ResourceLocation GENERATED_ID = Thaumcraft.location("generated_crucible");
   private final ResourceLocation id;
   public final Ingredient catalyst;
   public final AspectList aspects;
   public final ItemStack result;
   public final String researchKey;

   public CrucibleRecipe(ResourceLocation id, String researchKey, ItemStack result, Ingredient cat, AspectList tags) {
      this.id = id;
      this.result = result;
      this.aspects = tags;
      this.researchKey = researchKey;
      this.catalyst = cat;
   }

   @NotNull
   public NonNullList<Ingredient> getIngredients() {
      return NonNullList.withSize(1, this.catalyst);
   }

   public boolean matches(@NotNull RecipeInput input, @NotNull Level level) {
      return false;
   }

   @NotNull
   public ItemStack assemble(@NotNull RecipeInput input, @NotNull Provider registryAccess) {
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

   public boolean matches(AspectList itags, ItemStack cat) {
      if (itags != null && cat != null) {
         if (this.catalyst.test(cat)) {
            for (Aspect tag : this.aspects.getAspects()) {
               if (itags.getAmount(tag) < this.aspects.getAmount(tag)) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean catalystMatches(@NotNull ItemStack cat) {
      return this.catalyst.test(cat);
   }

   public AspectList removeMatching(AspectList itags) {
      AspectList temptags = new AspectList();
      temptags.aspects.putAll(itags.aspects);

      for (Aspect tag : this.aspects.getAspects()) {
         temptags.remove(tag, this.aspects.getAmount(tag));
      }

      return temptags;
   }

   public ItemStack getResultItem() {
      return this.result;
   }

   public CrucibleRecipe(String researchKey, ItemStack result, Ingredient cat, AspectList tags) {
      this(GENERATED_ID, researchKey, result, cat, tags);
   }

   public static class Serializer implements RecipeSerializer<CrucibleRecipe> {
      @NotNull
      public MapCodec<CrucibleRecipe> codec() {
         return RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                  ExtraCodecs.NON_EMPTY_STRING.fieldOf("research").forGetter(recipe -> recipe.researchKey),
                  ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                  Ingredient.CODEC_NONEMPTY.fieldOf("catalyst").forGetter(recipe -> recipe.catalyst),
                  AspectList.CODEC.fieldOf("aspects").forGetter(recipe -> recipe.aspects)
               )
               .apply(builder, CrucibleRecipe::new)
         );
      }

      @NotNull
      public StreamCodec<RegistryFriendlyByteBuf, CrucibleRecipe> streamCodec() {
         return StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            recipe -> recipe.researchKey,
            ItemStack.STREAM_CODEC,
            recipe -> recipe.result,
            Ingredient.CONTENTS_STREAM_CODEC,
            recipe -> recipe.catalyst,
            AspectList.STREAM_CODEC,
            recipe -> recipe.aspects,
            CrucibleRecipe::new
         );
      }
   }
}
