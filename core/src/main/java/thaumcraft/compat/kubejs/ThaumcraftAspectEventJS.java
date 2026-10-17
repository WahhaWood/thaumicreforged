package thaumcraft.compat.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.rhino.Context;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;

public class ThaumcraftAspectEventJS implements KubeEvent {
   private final LinkedHashMap<String, JsonObject> aspectDefinitions;
   private final ArrayList<JsonObject> objectTags;
   private final ArrayList<JsonObject> entityTags;

   public ThaumcraftAspectEventJS() {
      this(new LinkedHashMap<>(), new ArrayList<>(), new ArrayList<>());
   }

   public ThaumcraftAspectEventJS(LinkedHashMap<String, JsonObject> aspectDefinitions, ArrayList<JsonObject> objectTags, ArrayList<JsonObject> entityTags) {
      this.aspectDefinitions = aspectDefinitions;
      this.objectTags = objectTags;
      this.entityTags = entityTags;
   }

   public void addAspect(Context cx, String id, Object data) {
      String aspectId = normalizeAspectId(id);
      JsonObject json = toJsonObject(cx, data, "aspect");
      json.addProperty("id", aspectId);
      this.aspectDefinitions.put(aspectId, json);
   }

   public void removeAspect(Context cx, String id) {
      this.aspectDefinitions.remove(normalizeAspectId(id));
   }

   public boolean hasAspect(Context cx, String id) {
      return this.aspectDefinitions.containsKey(normalizeAspectId(id));
   }

   public void item(Context cx, String id, Object aspects) {
      this.item(cx, id, aspects, true);
   }

   public void item(Context cx, String id, Object aspects, boolean prepend) {
      this.objectTags.add(createObjectTag(cx, "item", id, aspects, prepend));
   }

   public void tag(Context cx, String id, Object aspects) {
      this.tag(cx, id, aspects, true);
   }

   public void tag(Context cx, String id, Object aspects, boolean prepend) {
      this.objectTags.add(createObjectTag(cx, "tag", id, aspects, prepend));
   }

   public void entity(Context cx, String id, Object aspects) {
      this.entity(cx, id, aspects, true);
   }

   public void entity(Context cx, String id, Object aspects, boolean prepend) {
      JsonObject json = new JsonObject();
      json.addProperty("id", normalizeResourceId(id, "entity"));
      json.add("aspects", toAspectArray(cx, aspects));
      json.addProperty("prepend", prepend);
      this.entityTags.add(json);
   }

   private static JsonObject createObjectTag(Context cx, String type, String id, Object aspects, boolean prepend) {
      JsonObject json = new JsonObject();
      json.addProperty("type", type);
      json.addProperty("id", normalizeResourceId(id, type));
      json.add("aspects", toAspectArray(cx, aspects));
      json.addProperty("prepend", prepend);
      return json;
   }

   private static JsonArray toAspectArray(Context cx, Object aspects) {
      JsonObject json = toJsonObject(cx, aspects, "aspects");
      JsonArray array = new JsonArray();

      for (Entry<String, JsonElement> entry : json.entrySet()) {
         JsonElement amountElement = entry.getValue();
         if (amountElement != null && !amountElement.isJsonNull()) {
            int amount;
            try {
               amount = amountElement.getAsInt();
            } catch (RuntimeException var9) {
               throw new IllegalArgumentException("Thaumcraft KubeJS aspect amount for '" + entry.getKey() + "' must be numeric", var9);
            }

            JsonObject object = new JsonObject();
            object.addProperty("key", normalizeAspectId(entry.getKey()));
            if (amount != 1) {
               object.addProperty("amount", amount);
            }

            array.add(object);
         }
      }

      return array;
   }

   private static JsonObject toJsonObject(Context cx, Object data, String type) {
      JsonObject json = JsonUtils.of(cx, data).getAsJsonObject();
      if (json == null) {
         throw new IllegalArgumentException("Thaumcraft KubeJS " + type + " payload must be a JSON-like object");
      } else {
         return json.deepCopy();
      }
   }

   private static String normalizeAspectId(String id) {
      if (id == null) {
         throw new IllegalArgumentException("Aspect id can't be null");
      } else {
         String normalized = id.trim().toLowerCase(Locale.ROOT);
         if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Aspect id can't be empty");
         } else if (!normalized.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("Aspect id must contain only lowercase latin letters, digits and underscore");
         } else {
            return normalized;
         }
      }
   }

   private static String normalizeResourceId(String id, String type) {
      if (id == null) {
         throw new IllegalArgumentException("Thaumcraft KubeJS " + type + " id can't be null");
      } else {
         String normalized = id.trim().toLowerCase(Locale.ROOT);
         if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Thaumcraft KubeJS " + type + " id can't be empty");
         } else {
            if (normalized.indexOf(58) < 0) {
               normalized = "minecraft:" + normalized;
            }

            return normalized;
         }
      }
   }
}
