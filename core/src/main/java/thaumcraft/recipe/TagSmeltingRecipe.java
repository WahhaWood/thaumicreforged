package thaumcraft.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Furnace recipe whose result is the first item of a tag, looked up when the furnace asks (TC4 Config smelted tin, silver
 * and lead clusters into whatever ingot the ore dictionary listed first). Without such an item the recipe does not match.
 */
public class TagSmeltingRecipe extends SmeltingRecipe {
   public static final RecipeSerializer<TagSmeltingRecipe> SERIALIZER = new Serializer();
   private final TagKey<Item> resultTag;
   private final int count;

   public TagSmeltingRecipe(
      String group, CookingBookCategory category, Ingredient ingredient, TagKey<Item> resultTag, int count, float experience, int cookingTime
   ) {
      super(group, category, ingredient, ItemStack.EMPTY, experience, cookingTime);
      this.resultTag = resultTag;
      this.count = count;
   }

   public ItemStack result() {
      return BuiltInRegistries.ITEM
         .getTag(this.resultTag)
         .flatMap(set -> set.stream().findFirst())
         .map(item -> new ItemStack(item, this.count))
         .orElse(ItemStack.EMPTY);
   }

   @Override
   public boolean matches(@NotNull SingleRecipeInput input, @NotNull Level level) {
      return super.matches(input, level) && !this.result().isEmpty();
   }

   @NotNull
   @Override
   public ItemStack assemble(@NotNull SingleRecipeInput input, @NotNull Provider registries) {
      return this.result();
   }

   @NotNull
   @Override
   public ItemStack getResultItem(@NotNull Provider registries) {
      return this.result();
   }

   @NotNull
   @Override
   public RecipeSerializer<?> getSerializer() {
      return SERIALIZER;
   }

   private static class Serializer implements RecipeSerializer<TagSmeltingRecipe> {
      private static final MapCodec<TagSmeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(
         instance -> instance.group(
               Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
               CookingBookCategory.CODEC.fieldOf("category").orElse(CookingBookCategory.MISC).forGetter(recipe -> recipe.category),
               Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(recipe -> recipe.ingredient),
               TagKey.codec(Registries.ITEM).fieldOf("result_tag").forGetter(recipe -> recipe.resultTag),
               Codec.INT.optionalFieldOf("count", 1).forGetter(recipe -> recipe.count),
               Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(recipe -> recipe.experience),
               Codec.INT.fieldOf("cookingtime").orElse(200).forGetter(recipe -> recipe.cookingTime)
            )
            .apply(instance, TagSmeltingRecipe::new)
      );
      private static final StreamCodec<RegistryFriendlyByteBuf, TagSmeltingRecipe> STREAM_CODEC = StreamCodec.of(
         (buf, recipe) -> {
            buf.writeUtf(recipe.group);
            buf.writeEnum(recipe.category);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.ingredient);
            buf.writeResourceLocation(recipe.resultTag.location());
            buf.writeVarInt(recipe.count);
            buf.writeFloat(recipe.experience);
            buf.writeVarInt(recipe.cookingTime);
         },
         buf -> new TagSmeltingRecipe(
            buf.readUtf(),
            buf.readEnum(CookingBookCategory.class),
            Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
            TagKey.create(Registries.ITEM, buf.readResourceLocation()),
            buf.readVarInt(),
            buf.readFloat(),
            buf.readVarInt()
         )
      );

      @NotNull
      @Override
      public MapCodec<TagSmeltingRecipe> codec() {
         return CODEC;
      }

      @NotNull
      @Override
      public StreamCodec<RegistryFriendlyByteBuf, TagSmeltingRecipe> streamCodec() {
         return STREAM_CODEC;
      }
   }
}
