package thaumcraft.compat.kubejs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.rhino.Context;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;
import net.minecraft.resources.ResourceLocation;

public class ThaumcraftResearchEventJS implements KubeEvent {
   private final LinkedHashMap<String, JsonObject> categories;
   private final LinkedHashMap<ResourceLocation, JsonElement> researches;

   public ThaumcraftResearchEventJS() {
      this(new LinkedHashMap<>(), new LinkedHashMap<>());
   }

   public ThaumcraftResearchEventJS(LinkedHashMap<String, JsonObject> categories, LinkedHashMap<ResourceLocation, JsonElement> researches) {
      this.categories = categories;
      this.researches = researches;
   }

   public void addCategory(Context cx, String id, Object data) {
      this.categories.put(normalizeCategoryId(id), toJsonObject(cx, data, "category"));
   }

   public void mergeCategory(Context cx, String id, Object data) {
      String categoryId = normalizeCategoryId(id);
      JsonObject target = this.categories.computeIfAbsent(categoryId, ignored -> new JsonObject());
      mergeJson(target, toJsonObject(cx, data, "category"));
   }

   public void removeCategory(Context cx, String id) {
      String categoryId = normalizeCategoryId(id);
      this.categories.remove(categoryId);
      this.researches.entrySet().removeIf(entry -> entry.getKey().getPath().startsWith(categoryId + "/"));
   }

   public boolean hasCategory(Context cx, String id) {
      return this.categories.containsKey(normalizeCategoryId(id));
   }

   public void addResearch(Context cx, String id, Object data) {
      this.researches.put(parseResearchId(id), toJsonObject(cx, data, "research"));
   }

   public void mergeResearch(Context cx, String id, Object data) {
      ResourceLocation researchId = parseResearchId(id);
      JsonElement current = this.researches.get(researchId);
      JsonObject target = current != null && current.isJsonObject() ? current.getAsJsonObject() : new JsonObject();
      mergeJson(target, toJsonObject(cx, data, "research"));
      this.researches.put(researchId, target);
   }

   public void removeResearch(Context cx, String id) {
      this.researches.remove(parseResearchId(id));
   }

   public boolean hasResearch(Context cx, String id) {
      return this.researches.containsKey(parseResearchId(id));
   }

   private static JsonObject toJsonObject(Context cx, Object data, String type) {
      JsonObject json = JsonUtils.of(cx, data).getAsJsonObject();
      if (json == null) {
         throw new IllegalArgumentException("Thaumcraft KubeJS " + type + " payload must be a JSON-like object");
      } else {
         return json.deepCopy();
      }
   }

   private static String normalizeCategoryId(String id) {
      if (id == null) {
         throw new IllegalArgumentException("Category id can't be null");
      } else {
         String normalized = id.trim().toLowerCase(Locale.ROOT);
         if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Category id can't be empty");
         } else {
            return normalized;
         }
      }
   }

   private static ResourceLocation parseResearchId(String id) {
      if (id == null) {
         throw new IllegalArgumentException("Research id can't be null");
      } else {
         String normalized = id.trim().toLowerCase(Locale.ROOT);
         if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Research id can't be empty");
         } else {
            ResourceLocation location = normalized.indexOf(58) >= 0
               ? ResourceLocation.parse(normalized)
               : ResourceLocation.fromNamespaceAndPath("thaumcraft", normalized);
            if (location.getPath().indexOf(47) < 0) {
               throw new IllegalArgumentException("Research id must look like '<category>/<key>' or '<namespace>:<category>/<key>'");
            } else {
               return location;
            }
         }
      }
   }

   private static void mergeJson(JsonObject target, JsonObject patch) {
      for (Entry<String, JsonElement> entry : patch.entrySet()) {
         JsonElement value = entry.getValue();
         if (value == null || value.isJsonNull()) {
            target.remove(entry.getKey());
         } else if (target.has(entry.getKey()) && target.get(entry.getKey()).isJsonObject() && value.isJsonObject()) {
            mergeJson(target.getAsJsonObject(entry.getKey()), value.getAsJsonObject());
         } else {
            target.add(entry.getKey(), value.deepCopy());
         }
      }
   }
}
