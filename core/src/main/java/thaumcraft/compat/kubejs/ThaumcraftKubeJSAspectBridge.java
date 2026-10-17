package thaumcraft.compat.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.slf4j.Logger;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.obj.EntityAspect;
import thaumcraft.api.aspects.obj.IngredientAspect;
import thaumcraft.api.aspects.obj.ObjectAspect;
import thaumcraft.api.aspects.obj.TagAspect;

public final class ThaumcraftKubeJSAspectBridge {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final LinkedHashMap<String, JsonObject> CACHED_ASPECTS = new LinkedHashMap<>();
   private static final ArrayList<JsonObject> CACHED_OBJECT_TAGS = new ArrayList<>();
   private static final ArrayList<JsonObject> CACHED_ENTITY_TAGS = new ArrayList<>();
   private static final ArrayList<Aspect> APPLIED_ASPECTS = new ArrayList<>();
   private static final ArrayList<ObjectAspect> APPLIED_OBJECT_TAGS = new ArrayList<>();
   private static final ArrayList<EntityAspect> APPLIED_ENTITY_TAGS = new ArrayList<>();

   private ThaumcraftKubeJSAspectBridge() {
   }

   public static void reloadServerData(Map<String, JsonObject> aspectDefinitions, List<JsonObject> objectTags, List<JsonObject> entityTags) {
      LinkedHashMap<String, JsonObject> aspectCopy = new LinkedHashMap<>();

      for (Entry<String, JsonObject> entry : aspectDefinitions.entrySet()) {
         aspectCopy.put(entry.getKey(), entry.getValue().deepCopy());
      }

      ArrayList<JsonObject> objectCopy = new ArrayList<>(objectTags.size());

      for (JsonObject objectTag : objectTags) {
         objectCopy.add(objectTag.deepCopy());
      }

      ArrayList<JsonObject> entityCopy = new ArrayList<>(entityTags.size());

      for (JsonObject entityTag : entityTags) {
         entityCopy.add(entityTag.deepCopy());
      }

      applyPayload(aspectCopy, objectCopy, entityCopy);
      cachePayload(aspectCopy, objectCopy, entityCopy);
   }

   public static void clearServerData() {
      LinkedHashMap<String, JsonObject> emptyAspects = new LinkedHashMap<>();
      ArrayList<JsonObject> emptyObjectTags = new ArrayList<>();
      ArrayList<JsonObject> emptyEntityTags = new ArrayList<>();
      applyPayload(emptyAspects, emptyObjectTags, emptyEntityTags);
      cachePayload(emptyAspects, emptyObjectTags, emptyEntityTags);
   }

   public static void applyClientPayload(Map<String, JsonObject> aspectDefinitions, List<JsonObject> objectTags, List<JsonObject> entityTags) {
      LinkedHashMap<String, JsonObject> aspectCopy = new LinkedHashMap<>();

      for (Entry<String, JsonObject> entry : aspectDefinitions.entrySet()) {
         aspectCopy.put(entry.getKey(), entry.getValue().deepCopy());
      }

      ArrayList<JsonObject> objectCopy = new ArrayList<>(objectTags.size());

      for (JsonObject entry : objectTags) {
         objectCopy.add(entry.deepCopy());
      }

      ArrayList<JsonObject> entityCopy = new ArrayList<>(entityTags.size());

      for (JsonObject entry : entityTags) {
         entityCopy.add(entry.deepCopy());
      }

      applyPayload(aspectCopy, objectCopy, entityCopy);
   }

   public static void clearRuntimeData() {
      clearAppliedData();
   }

   public static Map<String, JsonObject> getCachedAspects() {
      LinkedHashMap<String, JsonObject> copy = new LinkedHashMap<>();

      for (Entry<String, JsonObject> entry : CACHED_ASPECTS.entrySet()) {
         copy.put(entry.getKey(), entry.getValue().deepCopy());
      }

      return copy;
   }

   public static List<JsonObject> getCachedObjectTags() {
      ArrayList<JsonObject> copy = new ArrayList<>(CACHED_OBJECT_TAGS.size());

      for (JsonObject objectTag : CACHED_OBJECT_TAGS) {
         copy.add(objectTag.deepCopy());
      }

      return copy;
   }

   public static List<JsonObject> getCachedEntityTags() {
      ArrayList<JsonObject> copy = new ArrayList<>(CACHED_ENTITY_TAGS.size());

      for (JsonObject entityTag : CACHED_ENTITY_TAGS) {
         copy.add(entityTag.deepCopy());
      }

      return copy;
   }

   private static void applyPayload(LinkedHashMap<String, JsonObject> aspectDefinitions, List<JsonObject> objectTags, List<JsonObject> entityTags) {
      clearAppliedData();
      registerCustomAspects(aspectDefinitions);
      registerObjectTags(objectTags);
      registerEntityTags(entityTags);
   }

   private static void clearAppliedData() {
      ThaumcraftApi.unregisterAspectTags(APPLIED_OBJECT_TAGS);
      APPLIED_OBJECT_TAGS.clear();
      ThaumcraftApi.unregisterEntityTags(APPLIED_ENTITY_TAGS);
      APPLIED_ENTITY_TAGS.clear();

      for (Aspect aspect : APPLIED_ASPECTS) {
         Aspect.aspects.remove(aspect.getTag());
      }

      APPLIED_ASPECTS.clear();
   }

   private static void registerCustomAspects(LinkedHashMap<String, JsonObject> aspectDefinitions) {
      LinkedHashMap<String, JsonObject> pending = new LinkedHashMap<>();

      for (Entry<String, JsonObject> entry : aspectDefinitions.entrySet()) {
         pending.put(entry.getKey(), entry.getValue().deepCopy());
      }

      boolean progress;
      do {
         progress = false;
         Iterator<Entry<String, JsonObject>> iterator = pending.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<String, JsonObject> entry = iterator.next();

            try {
               if (hasInvalidComponents(entry.getKey(), entry.getValue())) {
                  iterator.remove();
                  progress = true;
               } else if (Aspect.getAspect(entry.getKey()) != null) {
                  LOGGER.error("Skipping KubeJS aspect {} because it is already registered", entry.getKey());
                  iterator.remove();
                  progress = true;
               } else {
                  Aspect aspect = tryCreateAspect(entry.getKey(), entry.getValue());
                  if (aspect != null) {
                     APPLIED_ASPECTS.add(aspect);
                     iterator.remove();
                     progress = true;
                  }
               }
            } catch (Exception var6) {
               LOGGER.error("Failed to register KubeJS aspect {}", entry.getKey(), var6);
               iterator.remove();
               progress = true;
            }
         }
      } while (progress && !pending.isEmpty());

      for (String aspectId : pending.keySet()) {
         LOGGER.error("Failed to register KubeJS aspect {} because one or more components are missing", aspectId);
      }
   }

   private static Aspect tryCreateAspect(String aspectId, JsonObject object) {
      Aspect[] components = null;
      if (object.has("components") && !object.get("components").isJsonNull()) {
         JsonArray array = GsonHelper.getAsJsonArray(object, "components");
         components = new Aspect[2];

         for (int i = 0; i < 2; i++) {
            String componentId = GsonHelper.convertToString(array.get(i), "components[" + i + "]").toLowerCase(Locale.ROOT);
            Aspect component = Aspect.getAspect(componentId);
            if (component == null) {
               return null;
            }

            components[i] = component;
         }
      }

      int color = parseFlexibleInt(object, "color", 16777215);
      int blend = parseFlexibleInt(object, "blend", 1);
      ResourceLocation texture = parseTexture(object, aspectId);
      return new Aspect(aspectId, color, components, texture, blend);
   }

   private static void registerObjectTags(List<JsonObject> objectTags) {
      for (JsonObject object : objectTags) {
         try {
            String type = GsonHelper.getAsString(object, "type");
            String id = GsonHelper.getAsString(object, "id");
            boolean prepend = GsonHelper.getAsBoolean(object, "prepend", true);
            AspectList aspects = parseAspectList(GsonHelper.getAsJsonArray(object, "aspects"), id);
            if (aspects.size() > 0) {
               ObjectAspect tag = switch (type) {
                  case "item" -> createItemTag(id, aspects);
                  case "tag" -> createForgeTag(id, aspects);
                  default -> {
                     LOGGER.error("Skipping KubeJS object tag {} because type '{}' is unsupported", id, type);
                     yield null;
                  }
               };
               if (tag != null) {
                  ThaumcraftApi.registerAspectTag(tag, prepend);
                  APPLIED_OBJECT_TAGS.add(tag);
               }
            }
         } catch (Exception var10) {
            LOGGER.error("Failed to apply KubeJS object aspect tag {}", object, var10);
         }
      }
   }

   private static ObjectAspect createItemTag(String id, AspectList aspects) {
      Item item = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
      if (item == null) {
         LOGGER.error("Skipping KubeJS object tag {} because item was not found", id);
         return null;
      } else {
         return new IngredientAspect(Ingredient.of(new ItemLike[]{item}), aspects);
      }
   }

   private static ObjectAspect createForgeTag(String id, AspectList aspects) {
      TagKey<Item> tagKey = ItemTags.create(ResourceLocation.parse(id));
      return new TagAspect(tagKey, aspects);
   }

   private static void registerEntityTags(List<JsonObject> entityTags) {
      for (JsonObject object : entityTags) {
         try {
            String id = GsonHelper.getAsString(object, "id");
            boolean prepend = GsonHelper.getAsBoolean(object, "prepend", true);
            AspectList aspects = parseAspectList(GsonHelper.getAsJsonArray(object, "aspects"), id);
            if (aspects.size() > 0) {
               EntityType<?> entityType = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(id));
               if (entityType == null) {
                  LOGGER.error("Skipping KubeJS entity aspect tag {} because entity type was not found", id);
               } else {
                  EntityAspect entityAspect = new EntityAspect((EntityType<? extends Entity>)entityType, aspects, null, null);
                  ThaumcraftApi.registerEntityTag(entityAspect, prepend);
                  APPLIED_ENTITY_TAGS.add(entityAspect);
               }
            }
         } catch (Exception var8) {
            LOGGER.error("Failed to apply KubeJS entity aspect tag {}", object, var8);
         }
      }
   }

   private static void cachePayload(Map<String, JsonObject> aspectDefinitions, List<JsonObject> objectTags, List<JsonObject> entityTags) {
      CACHED_ASPECTS.clear();

      for (Entry<String, JsonObject> entry : aspectDefinitions.entrySet()) {
         CACHED_ASPECTS.put(entry.getKey(), entry.getValue().deepCopy());
      }

      CACHED_OBJECT_TAGS.clear();

      for (JsonObject objectTag : objectTags) {
         CACHED_OBJECT_TAGS.add(objectTag.deepCopy());
      }

      CACHED_ENTITY_TAGS.clear();

      for (JsonObject entityTag : entityTags) {
         CACHED_ENTITY_TAGS.add(entityTag.deepCopy());
      }
   }

   private static int parseFlexibleInt(JsonObject object, String key, int defaultValue) {
      JsonElement element = object.get(key);
      if (element == null || element.isJsonNull()) {
         return defaultValue;
      } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
         return element.getAsInt();
      } else {
         String raw = element.getAsString().trim().toLowerCase(Locale.ROOT);

         try {
            if (raw.startsWith("#")) {
               return Integer.parseInt(raw.substring(1), 16);
            } else {
               return raw.startsWith("0x") ? Integer.parseInt(raw.substring(2), 16) : Integer.parseInt(raw);
            }
         } catch (NumberFormatException var6) {
            throw new IllegalArgumentException("Field '" + key + "' must be an integer or hex string", var6);
         }
      }
   }

   private static ResourceLocation parseTexture(JsonObject object, String aspectId) {
      if (GsonHelper.isStringValue(object, "texture")) {
         return ResourceLocation.parse(GsonHelper.getAsString(object, "texture"));
      } else {
         return GsonHelper.isStringValue(object, "image")
            ? ResourceLocation.parse(GsonHelper.getAsString(object, "image"))
            : ResourceLocation.fromNamespaceAndPath("kubejs", "textures/aspects/" + aspectId + ".png");
      }
   }

   private static boolean hasInvalidComponents(String aspectId, JsonObject object) {
      if (object.has("components") && !object.get("components").isJsonNull()) {
         JsonArray array = GsonHelper.getAsJsonArray(object, "components");
         if (array.size() == 2) {
            return false;
         } else {
            LOGGER.error("Skipping KubeJS aspect {} because components must contain exactly 2 aspect ids", aspectId);
            return true;
         }
      } else {
         return false;
      }
   }

   private static AspectList parseAspectList(JsonArray array, String ownerId) {
      AspectList aspects = new AspectList();

      for (JsonElement element : array) {
         JsonObject entry = element.getAsJsonObject();
         String aspectId = GsonHelper.getAsString(entry, "key").toLowerCase(Locale.ROOT);
         Aspect aspect = Aspect.getAspect(aspectId);
         if (aspect == null) {
            LOGGER.error("Skipping unknown aspect {} in KubeJS registration {}", aspectId, ownerId);
         } else {
            aspects.add(aspect, GsonHelper.getAsInt(entry, "amount", 1));
         }
      }

      return aspects;
   }
}
