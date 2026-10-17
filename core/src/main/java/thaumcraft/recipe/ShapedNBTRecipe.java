package thaumcraft.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.jetbrains.annotations.NotNull;
import thaumcraft.init.registry.ModItems;
import thaumcraft.init.registry.ModRecipes;
import thaumcraft.util.ItemStackData;

public class ShapedNBTRecipe extends ShapedRecipe {
   public ShapedNBTRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification) {
      super(group, category, pattern, result, showNotification);
   }

   @NotNull
   public RecipeSerializer<?> getSerializer() {
      return (RecipeSerializer<?>)ModRecipes.SHAPED_NBT_SERIALIZER.get();
   }

   public static class Serializer implements RecipeSerializer<ShapedNBTRecipe> {
      private static final MapCodec<ShapedNBTRecipe> CODEC = RecipeSerializer.SHAPED_RECIPE.codec().xmap(ShapedNBTRecipe.Serializer::wrap, recipe -> recipe);
      private static final StreamCodec<RegistryFriendlyByteBuf, ShapedNBTRecipe> STREAM_CODEC = RecipeSerializer.SHAPED_RECIPE
         .streamCodec()
         .map(ShapedNBTRecipe.Serializer::wrap, recipe -> recipe);

      @NotNull
      public MapCodec<ShapedNBTRecipe> codec() {
         return CODEC;
      }

      @NotNull
      public StreamCodec<RegistryFriendlyByteBuf, ShapedNBTRecipe> streamCodec() {
         return STREAM_CODEC;
      }

      private static ShapedNBTRecipe wrap(ShapedRecipe recipe) {
         return new ShapedNBTRecipe(recipe.getGroup(), recipe.category(), recipe.pattern, recipe.getResultItem((Provider)null), recipe.showNotification());
      }

      public static ItemStack getResultItem(JsonObject object) {
         JsonObject result = GsonHelper.getAsJsonObject(object, "result");
         ItemStack stack = (ItemStack)ItemStack.STRICT_CODEC
            .parse(RegistryOps.create(JsonOps.INSTANCE, Provider.create(Stream.empty())), result)
            .getOrThrow(JsonParseException::new);
         if (result.has("nbt")) {
            ItemStackData.setTag(stack, parseCompoundTag(result.get("nbt")));
            normalizeResultTag(stack);
         }

         return stack;
      }

      private static void normalizeResultTag(ItemStack stack) {
         if (stack.getItem() == ModItems.MOB_ASPECT.get() && ItemStackData.hasTag(stack)) {
            CompoundTag tag = ItemStackData.getTag(stack);
            if (tag != null && tag.contains("Type")) {
               tag.put("Type", ByteTag.valueOf(tag.getByte("Type")));
            }
         }
      }

      private static CompoundTag parseCompoundTag(JsonElement element) {
         if (element.isJsonPrimitive()) {
            try {
               return TagParser.parseTag(GsonHelper.convertToString(element, "nbt"));
            } catch (Exception var2) {
               throw new JsonParseException("Invalid nbt string for recipe result", var2);
            }
         } else if (element.isJsonObject()) {
            return parseCompoundObject(element.getAsJsonObject());
         } else {
            throw new JsonParseException("Unsupported nbt payload for recipe result: " + element);
         }
      }

      private static CompoundTag parseCompoundObject(JsonObject object) {
         JsonObject source = object.has("tags") && object.get("tags").isJsonObject() ? object.getAsJsonObject("tags") : object;
         CompoundTag tag = new CompoundTag();

         for (String key : source.keySet()) {
            tag.put(key, parseTag(source.get(key)));
         }

         return tag;
      }

      private static Tag parseTag(JsonElement element) {
         if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            return (Tag)(object.has("data") && object.size() == 1 ? parsePrimitive(object.get("data")) : parseCompoundObject(object));
         } else if (!element.isJsonArray()) {
            return parsePrimitive(element);
         } else {
            ListTag list = new ListTag();

            for (JsonElement child : element.getAsJsonArray()) {
               list.add(parseTag(child));
            }

            return list;
         }
      }

      private static Tag parsePrimitive(JsonElement element) {
         if (!element.isJsonPrimitive()) {
            throw new JsonParseException("Unsupported nbt primitive: " + element);
         } else if (element.getAsJsonPrimitive().isBoolean()) {
            return IntTag.valueOf(element.getAsBoolean() ? 1 : 0);
         } else if (element.getAsJsonPrimitive().isNumber()) {
            Number number = element.getAsNumber();
            double doubleValue = number.doubleValue();
            return (Tag)(Math.rint(doubleValue) == doubleValue ? IntTag.valueOf(number.intValue()) : DoubleTag.valueOf(doubleValue));
         } else {
            return StringTag.valueOf(element.getAsString());
         }
      }
   }
}
