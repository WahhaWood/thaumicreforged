package thaumcraft.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import java.util.Map.Entry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.CraftingHelper;

public final class DataGenCompat {
   private static final RegistryOps<JsonElement> REGISTRY_JSON_OPS = RegistryOps.create(
      JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
   );

   private DataGenCompat() {
   }

   public static JsonElement ingredientToJson(Ingredient ingredient) {
      JsonElement encoded = (JsonElement)CraftingHelper.makeIngredientCodec(true).encodeStart(REGISTRY_JSON_OPS, ingredient).getOrThrow();
      return normalizeIngredientJson(encoded);
   }

   public static Ingredient ingredientFromJson(JsonElement element) {
      return (Ingredient)CraftingHelper.makeIngredientCodec(true)
         .parse(REGISTRY_JSON_OPS, normalizeIngredientJson(element))
         .getOrThrow(JsonParseException::new);
   }

   private static JsonElement normalizeIngredientJson(JsonElement element) {
      if (element == null || element.isJsonNull() || element.isJsonPrimitive()) {
         return element;
      } else if (element.isJsonArray()) {
         JsonArray normalized = new JsonArray();

         for (JsonElement child : element.getAsJsonArray()) {
            normalized.add(normalizeIngredientJson(child));
         }

         return normalized;
      } else {
         JsonObject normalized = new JsonObject();

         for (Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            normalized.add(entry.getKey(), normalizeIngredientJson(entry.getValue()));
         }

         if (normalized.has("type")
            && normalized.get("type").isJsonPrimitive()
            && "neoforge:components".equals(normalized.get("type").getAsString())
            && normalized.has("items")
            && !normalized.get("items").isJsonArray()) {
            JsonArray items = new JsonArray();
            items.add(normalized.get("items"));
            normalized.add("items", items);
         }

         return normalized;
      }
   }

   public static JsonElement conditionToJson(ICondition condition) {
      return (JsonElement)ICondition.CODEC.encodeStart(JsonOps.INSTANCE, condition).getOrThrow();
   }

   public static boolean hasTag(ItemStack stack) {
      return ItemStackData.hasTag(stack);
   }

   public static CompoundTag getTag(ItemStack stack) {
      return ItemStackData.getTag(stack);
   }
}
