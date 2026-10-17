package thaumcraft.api.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.AspectList;

public class ShapelessArcaneRecipe extends ShapelessRecipe implements IArcaneRecipe {
   public static final RecipeType<ShapelessArcaneRecipe> TYPE = RecipeType.simple(Thaumcraft.location("shapeless_arcane"));
   public static final RecipeSerializer<ShapelessArcaneRecipe> SERIALIZER = new ShapelessArcaneRecipe.Serializer();
   private static final ResourceLocation GENERATED_ID = Thaumcraft.location("generated_shapeless_arcane");
   public final AspectList aspects;
   public final String researchKey;
   private final ResourceLocation id;

   public ShapelessArcaneRecipe(ResourceLocation id, String researchKey, AspectList aspects, ItemStack result, NonNullList<Ingredient> ingredients) {
      super("", CraftingBookCategory.MISC, result, ingredients);
      this.id = id;
      this.researchKey = researchKey;
      this.aspects = aspects;
   }

   @Override
   public AspectList getAspects() {
      return this.aspects;
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
   public String getResearchKey() {
      return this.researchKey;
   }

   public ShapelessArcaneRecipe(
      String group, CraftingBookCategory category, String researchKey, AspectList aspects, ItemStack result, NonNullList<Ingredient> ingredients
   ) {
      super(group, category, result, ingredients);
      this.id = GENERATED_ID;
      this.researchKey = researchKey;
      this.aspects = aspects;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public static class Serializer implements RecipeSerializer<ShapelessArcaneRecipe> {
      public static final MapCodec<ShapelessArcaneRecipe> CODEC = RecordCodecBuilder.mapCodec(
         builder -> builder.group(
               Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessRecipe::getGroup),
               CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapelessRecipe::category),
               ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.getResultItem((Provider)null)),
               Ingredient.CODEC_NONEMPTY
                  .listOf()
                  .flatXmap(
                     list -> {
                        Ingredient[] ingredients = list.toArray(Ingredient[]::new);
                        if (ingredients.length == 0) {
                           return DataResult.error(() -> "No ingredients for shapeless recipe");
                        } else {
                           return ingredients.length > ShapedRecipePattern.getMaxWidth() * ShapedRecipePattern.getMaxHeight()
                              ? DataResult.error(
                                 () -> "Too many ingredients for shapeless recipe. The maximum is: "
                                    + ShapedRecipePattern.getMaxWidth() * ShapedRecipePattern.getMaxHeight()
                              )
                              : DataResult.success(NonNullList.of(Ingredient.EMPTY, ingredients));
                        }
                     },
                     DataResult::success
                  )
                  .fieldOf("ingredients")
                  .forGetter(recipe -> recipe.getIngredients()),
               Codec.STRING.fieldOf("research").forGetter(recipe -> recipe.researchKey),
               AspectList.CODEC.fieldOf("aspects").forGetter(recipe -> recipe.aspects)
            )
            .apply(
               builder,
               (group, category, result, ingredients, researchKey, aspects) -> new ShapelessArcaneRecipe(
                  group, category, researchKey, aspects, result, ingredients
               )
            )
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessArcaneRecipe> STREAM_CODEC = StreamCodec.of(
         ShapelessArcaneRecipe.Serializer::toNetwork, ShapelessArcaneRecipe.Serializer::fromNetwork
      );

      @NotNull
      public MapCodec<ShapelessArcaneRecipe> codec() {
         return CODEC;
      }

      @NotNull
      public StreamCodec<RegistryFriendlyByteBuf, ShapelessArcaneRecipe> streamCodec() {
         return STREAM_CODEC;
      }

      private static ShapelessArcaneRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
         String group = buf.readUtf();
         CraftingBookCategory category = (CraftingBookCategory)buf.readEnum(CraftingBookCategory.class);
         int count = buf.readVarInt();
         NonNullList<Ingredient> ingredients = NonNullList.withSize(count, Ingredient.EMPTY);
         ingredients.replaceAll(ignored -> (Ingredient)Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
         ItemStack result = (ItemStack)ItemStack.STREAM_CODEC.decode(buf);
         String researchKey = buf.readUtf();
         AspectList aspects = (AspectList)AspectList.STREAM_CODEC.decode(buf);
         return new ShapelessArcaneRecipe(group, category, researchKey, aspects, result, ingredients);
      }

      private static void toNetwork(RegistryFriendlyByteBuf buf, ShapelessArcaneRecipe recipe) {
         buf.writeUtf(recipe.getGroup());
         buf.writeEnum(recipe.category());
         buf.writeVarInt(recipe.getIngredients().size());

         for (Ingredient ingredient : recipe.getIngredients()) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
         }

         ItemStack.STREAM_CODEC.encode(buf, recipe.getResultItem((Provider)null));
         buf.writeUtf(recipe.researchKey);
         AspectList.STREAM_CODEC.encode(buf, recipe.aspects);
      }
   }
}
