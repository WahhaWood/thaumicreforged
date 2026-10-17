package thaumcraft.api.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
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
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.AspectList;

public class ShapedArcaneRecipe extends ShapedRecipe implements IArcaneRecipe {
   public static final RecipeType<ShapedArcaneRecipe> TYPE = RecipeType.simple(Thaumcraft.location("shaped_arcane"));
   public static final RecipeSerializer<ShapedArcaneRecipe> SERIALIZER = new ShapedArcaneRecipe.Serializer();
   private static final ResourceLocation GENERATED_ID = Thaumcraft.location("generated_shaped_arcane");
   public final ResourceLocation id;
   public final AspectList aspects;
   public final String researchKey;

   public ShapedArcaneRecipe(
      ResourceLocation id, String researchKey, AspectList aspects, int width, int height, NonNullList<Ingredient> stacks, ItemStack result
   ) {
      super("", CraftingBookCategory.MISC, new ShapedRecipePattern(width, height, stacks, Optional.empty()), result);
      this.id = id;
      this.researchKey = researchKey;
      this.aspects = aspects;
   }

   public ShapedArcaneRecipe(
      String group,
      CraftingBookCategory category,
      ShapedRecipePattern pattern,
      ItemStack result,
      boolean showNotification,
      String researchKey,
      AspectList aspects
   ) {
      super(group, category, pattern, result, showNotification);
      this.id = GENERATED_ID;
      this.researchKey = researchKey;
      this.aspects = aspects;
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
   public AspectList getAspects() {
      return this.aspects;
   }

   @Override
   public String getResearchKey() {
      return this.researchKey;
   }

   public static class Serializer implements RecipeSerializer<ShapedArcaneRecipe> {
      public static final MapCodec<ShapedArcaneRecipe> CODEC = RecordCodecBuilder.mapCodec(
         builder -> builder.group(
               Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
               CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
               ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
               ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.getResultItem((Provider)null)),
               Codec.BOOL.optionalFieldOf("show_notification", Boolean.TRUE).forGetter(ShapedRecipe::showNotification),
               Codec.STRING.fieldOf("research").forGetter(recipe -> recipe.researchKey),
               AspectList.CODEC.fieldOf("aspects").forGetter(recipe -> recipe.aspects)
            )
            .apply(builder, ShapedArcaneRecipe::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, ShapedArcaneRecipe> STREAM_CODEC = StreamCodec.of(
         ShapedArcaneRecipe.Serializer::toNetwork, ShapedArcaneRecipe.Serializer::fromNetwork
      );

      public MapCodec<ShapedArcaneRecipe> codec() {
         return CODEC;
      }

      public StreamCodec<RegistryFriendlyByteBuf, ShapedArcaneRecipe> streamCodec() {
         return STREAM_CODEC;
      }

      private static ShapedArcaneRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
         String group = buffer.readUtf();
         CraftingBookCategory category = (CraftingBookCategory)buffer.readEnum(CraftingBookCategory.class);
         ShapedRecipePattern pattern = (ShapedRecipePattern)ShapedRecipePattern.STREAM_CODEC.decode(buffer);
         ItemStack result = (ItemStack)ItemStack.STREAM_CODEC.decode(buffer);
         boolean showNotification = buffer.readBoolean();
         String researchKey = buffer.readUtf();
         AspectList aspects = (AspectList)AspectList.STREAM_CODEC.decode(buffer);
         return new ShapedArcaneRecipe(group, category, pattern, result, showNotification, researchKey, aspects);
      }

      private static void toNetwork(RegistryFriendlyByteBuf buffer, ShapedArcaneRecipe recipe) {
         buffer.writeUtf(recipe.getGroup());
         buffer.writeEnum(recipe.category());
         ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
         ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItem((Provider)null));
         buffer.writeBoolean(recipe.showNotification());
         buffer.writeUtf(recipe.researchKey);
         AspectList.STREAM_CODEC.encode(buffer, recipe.aspects);
      }
   }
}
