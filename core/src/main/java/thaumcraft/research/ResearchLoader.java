package thaumcraft.research;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.research.IResearchItemFactory;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.api.research.page.ArrayCraftPage;
import thaumcraft.compat.kubejs.ThaumcraftKubeJSAspectBridge;
import thaumcraft.compat.kubejs.ThaumcraftKubeJSAspectPostBridge;
import thaumcraft.compat.kubejs.ThaumcraftKubeJSBridge;
import thaumcraft.init.registry.Config;

public class ResearchLoader extends SimpleJsonResourceReloadListener {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation DEFAULT_RESEARCH_TYPE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "default");
   private static final int DEFAULT_LINE_COLOR = 2763306;
   private final LinkedHashMap<String, ResearchLoader.CategoryDefinition> defaults = new LinkedHashMap<>();
   private final LinkedHashMap<String, ResearchCategoryList> byName = new LinkedHashMap<>();
   private final Map<String, Function<JsonObject, ResearchPage>> pages = new HashMap<>();
   private final Map<ResourceLocation, IResearchItemFactory> itemTypes = new HashMap<>();
   private final ArrayList<ResearchItem> hidden = new ArrayList<>();
   private final ArrayList<ResearchItem> valid = new ArrayList<>();
   private Map<ResourceLocation, JsonElement> cachedData = new LinkedHashMap<>();
   private Map<String, JsonObject> cachedCategories = new LinkedHashMap<>();

   public ResearchLoader() {
      super(GSON, "research");
   }

   protected void apply(@NotNull Map<ResourceLocation, JsonElement> elementMap, @NotNull ResourceManager manager, @NotNull ProfilerFiller filler) {
      if (ModList.get().isLoaded("kubejs")) {
         LinkedHashMap<String, JsonObject> aspectDefinitions = new LinkedHashMap<>();
         ArrayList<JsonObject> objectTags = new ArrayList<>();
         ArrayList<JsonObject> entityTags = new ArrayList<>();
         ThaumcraftKubeJSAspectPostBridge.collect(aspectDefinitions, objectTags, entityTags);
         ThaumcraftKubeJSAspectBridge.reloadServerData(aspectDefinitions, objectTags, entityTags);
      } else {
         ThaumcraftKubeJSAspectBridge.clearServerData();
      }

      LinkedHashMap<String, JsonObject> categoryData = this.createDefaultCategoryPayload();
      LinkedHashMap<ResourceLocation, JsonElement> researchData = this.filterResearchData(elementMap);
      if (ModList.get().isLoaded("kubejs")) {
         ThaumcraftKubeJSBridge.applyResearchMutations(categoryData, researchData);
      }

      this.rebuildFromData(categoryData, researchData, true);
      LOGGER.info("Loaded {} categories, {} researches", this.byName.size(), this.byName.values().stream().mapToInt(value -> value.research.size()).sum());
   }

   public void applyFromNetwork(@NotNull Map<String, JsonObject> categoryData, @NotNull Map<ResourceLocation, JsonElement> elementMap) {
      this.rebuildFromData(categoryData, elementMap, false);
      LOGGER.info(
         "[Client] Synced {} categories, {} researches from server",
         this.byName.size(),
         this.byName.values().stream().mapToInt(value -> value.research.size()).sum()
      );
   }

   public Map<ResourceLocation, JsonElement> getCachedData() {
      return this.cachedData;
   }

   public Map<String, JsonObject> getCachedCategories() {
      return this.cachedCategories;
   }

   private LinkedHashMap<ResourceLocation, JsonElement> filterResearchData(Map<ResourceLocation, JsonElement> elementMap) {
      LinkedHashMap<ResourceLocation, JsonElement> filtered = new LinkedHashMap<>();

      for (Entry<ResourceLocation, JsonElement> entry : elementMap.entrySet()) {
         ResourceLocation id = entry.getKey();
         JsonElement value = entry.getValue();

         try {
            if (value.isJsonObject()
               && value.getAsJsonObject().has("conditions")
               && !ICondition.LIST_CODEC
                  .parse(JsonOps.INSTANCE, value.getAsJsonObject().get("conditions"))
                  .result()
                  .orElse(List.of())
                  .stream()
                  .allMatch(condition -> condition.test(IContext.EMPTY))) {
               LOGGER.debug("Skipping loading research item {} as it's conditions were not met", id);
            } else {
               filtered.put(id, value.deepCopy());
            }
         } catch (Exception var8) {
            LOGGER.error("Condition error loading research {}", id, var8);
         }
      }

      return filtered;
   }

   private LinkedHashMap<String, JsonObject> createDefaultCategoryPayload() {
      LinkedHashMap<String, JsonObject> out = new LinkedHashMap<>();

      for (Entry<String, ResearchLoader.CategoryDefinition> entry : this.defaults.entrySet()) {
         out.put(entry.getKey(), entry.getValue().toJson());
      }

      return out;
   }

   private void clearResearchData() {
      ThaumcraftApi.clearResearchWarp();
      this.byName.clear();
      this.hidden.clear();
      this.valid.clear();
   }

   private void rebuildFromData(@NotNull Map<String, JsonObject> categoryData, @NotNull Map<ResourceLocation, JsonElement> researchData, boolean cachePayload) {
      this.clearResearchData();
      this.rebuildCategories(categoryData);
      this.loadResearches(researchData);
      this.runPostLoadHooks();
      this.rebuildLookupLists();
      if (cachePayload) {
         this.cachedCategories = this.deepCopyCategoryMap(categoryData);
         this.cachedData = this.deepCopyResearchMap(researchData);
      }
   }

   private void rebuildCategories(@NotNull Map<String, JsonObject> categoryData) {
      for (Entry<String, JsonObject> entry : categoryData.entrySet()) {
         try {
            this.byName.put(entry.getKey(), this.categoryFromJson(entry.getKey(), entry.getValue()));
         } catch (JsonParseException | IllegalArgumentException var5) {
            LOGGER.error("Parsing error loading research category {}", entry.getKey(), var5);
         }
      }
   }

   private void loadResearches(@NotNull Map<ResourceLocation, JsonElement> researchData) {
      label49:
      for (Entry<ResourceLocation, JsonElement> entry : researchData.entrySet()) {
         ResourceLocation id = entry.getKey();
         String path = id.getPath();

         try {
            int index = path.indexOf(47);
            if (index < 0) {
               LOGGER.warn("Skipping loading research item {} as it doesn't include a category path", id);
            } else {
               String categoryId = path.substring(0, index);
               ResearchCategoryList categoryList = this.byName.get(categoryId);
               if (categoryList == null) {
                  LOGGER.info("Skipping loading research item {} as it's category not found", id);
               } else {
                  String key = path.substring(index + 1);
                  ResearchItem researchItem = this.researchItemFromJson(GsonHelper.convertToJsonObject(entry.getValue(), "top element"), key, categoryId);
                  if (!researchItem.isVirtual()) {
                     for (ResearchItem researchItem2 : categoryList.research.values()) {
                        if (researchItem2.displayColumn == researchItem.displayColumn && researchItem2.displayRow == researchItem.displayRow) {
                           LOGGER.error(
                              "[Thaumcraft] Research [{}] not added as it overlaps with existing research [{}]",
                              researchItem.getName(),
                              researchItem2.getName()
                           );
                           continue label49;
                        }
                     }
                  }

                  categoryList.research.put(key, researchItem);
                  categoryList.maxColumn = Math.max(categoryList.maxColumn, researchItem.displayColumn);
                  categoryList.minColumn = Math.min(categoryList.minColumn, researchItem.displayColumn);
                  categoryList.maxRow = Math.max(categoryList.maxRow, researchItem.displayRow);
                  categoryList.minRow = Math.min(categoryList.minRow, researchItem.displayRow);
               }
            }
         } catch (JsonParseException | IllegalArgumentException var13) {
            LOGGER.error("Parsing error loading research {}", id, var13);
         }
      }
   }

   private Map<String, JsonObject> deepCopyCategoryMap(Map<String, JsonObject> source) {
      LinkedHashMap<String, JsonObject> copy = new LinkedHashMap<>();

      for (Entry<String, JsonObject> entry : source.entrySet()) {
         copy.put(entry.getKey(), entry.getValue().deepCopy());
      }

      return copy;
   }

   private Map<ResourceLocation, JsonElement> deepCopyResearchMap(Map<ResourceLocation, JsonElement> source) {
      LinkedHashMap<ResourceLocation, JsonElement> copy = new LinkedHashMap<>();

      for (Entry<ResourceLocation, JsonElement> entry : source.entrySet()) {
         copy.put(entry.getKey(), entry.getValue().deepCopy());
      }

      return copy;
   }

   private void rebuildLookupLists() {
      this.hidden.clear();
      this.valid.clear();

      for (ResearchCategoryList cat : this.byName.values()) {
         for (ResearchItem ri : cat.research.values()) {
            if (ri.isHidden() && ri.tags != null && ri.tags.size() > 0) {
               this.hidden.add(ri);
            }

            boolean secondary = ri.isSecondary() && Config.researchDifficulty == 0 || Config.researchDifficulty == -1;
            if (!secondary && !ri.isHidden() && !ri.isLost() && !ri.isAutoUnlock() && !ri.isVirtual() && !ri.isStub()) {
               this.valid.add(ri);
            }
         }
      }
   }

   public void registerCategory(String name, ResourceLocation icon, ResourceLocation background) {
      this.registerCategory(name, new ResearchCategoryList(icon, background));
   }

   public void registerCategory(String name, ResourceLocation icon, ResourceLocation background, int lineColor) {
      this.registerCategory(name, new ResearchCategoryList(icon, background, lineColor));
   }

   public void registerCategory(String name, ResearchCategoryList categoryList) {
      ResearchLoader.CategoryDefinition definition = ResearchLoader.CategoryDefinition.of(categoryList);
      this.defaults.put(name, definition);
      this.byName.put(name, definition.createList());
   }

   public void registerPageType(String name, Function<JsonObject, ResearchPage> function) {
      this.pages.put(name, function);
   }

   public void registerResearchItemType(ResourceLocation id, IResearchItemFactory factory) {
      this.itemTypes.put(id, factory);
   }

   @Nullable
   public ResearchCategoryList get(String name) {
      return this.byName.get(name);
   }

   public LinkedHashMap<String, ResearchCategoryList> getByName() {
      return this.byName;
   }

   public ArrayList<ResearchItem> getHidden() {
      return this.hidden;
   }

   public ArrayList<ResearchItem> getValid() {
      return this.valid;
   }

   private ResearchCategoryList categoryFromJson(String categoryId, JsonObject object) {
      ResourceLocation icon = ResourceLocation.parse(GsonHelper.getAsString(object, "icon"));
      ResourceLocation background = ResourceLocation.parse(GsonHelper.getAsString(object, "background"));
      int lineColor = 2763306;
      if (GsonHelper.isNumberValue(object, "line_color")) {
         lineColor = GsonHelper.getAsInt(object, "line_color");
      } else if (GsonHelper.isNumberValue(object, "lineColor")) {
         lineColor = GsonHelper.getAsInt(object, "lineColor");
      }

      if (lineColor < 0) {
         LOGGER.debug("Category {} uses negative line color {}", categoryId, lineColor);
      }

      return new ResearchCategoryList(icon, background, lineColor);
   }

   private ResearchItem researchItemFromJson(JsonObject object, String key, String category) {
      JsonArray array = GsonHelper.getAsJsonArray(object, "pages");
      ResearchPage[] pages = new ResearchPage[array.size()];

      for (int i = 0; i < array.size(); i++) {
         pages[i] = this.researchPageFromJson(array.get(i).getAsJsonObject());
      }

      if (GsonHelper.isNumberValue(object, "warp")) {
         ThaumcraftApi.addWarpToResearch(key, GsonHelper.getAsInt(object, "warp"));
      }

      ResourceLocation typeId = DEFAULT_RESEARCH_TYPE;
      if (GsonHelper.isStringValue(object, "research_type")) {
         typeId = ResourceLocation.parse(GsonHelper.getAsString(object, "research_type"));
      } else if (GsonHelper.isStringValue(object, "type")) {
         typeId = ResourceLocation.parse(GsonHelper.getAsString(object, "type"));
      }

      IResearchItemFactory factory = this.itemTypes.get(typeId);
      if (factory == null) {
         LOGGER.warn("Unknown research type {} for {}. Falling back to {}", new Object[]{typeId, key, DEFAULT_RESEARCH_TYPE});
         factory = this.itemTypes.get(DEFAULT_RESEARCH_TYPE);
      }

      if (factory == null) {
         throw new JsonParseException("Default research item type not registered");
      } else {
         return factory.create(object, key, category, pages);
      }
   }

   private void runPostLoadHooks() {
      for (ResearchCategoryList cat : this.byName.values()) {
         for (ResearchItem ri : cat.research.values()) {
            try {
               ri.onPostLoad();
            } catch (Exception var6) {
               LOGGER.error("Post-load hook failed for research {}", ri.key, var6);
            }
         }
      }
   }

   private ResearchPage researchPageFromJson(JsonObject object) {
      String type = GsonHelper.getAsString(object, "type");
      if (!type.equals("recipes")) {
         Function<JsonObject, ResearchPage> function = this.pages.get(type);
         return function != null ? function.apply(object) : null;
      } else {
         JsonArray array = GsonHelper.getAsJsonArray(object, "value");
         ResearchPage[] pages = new ResearchPage[array.size()];

         for (int i = 0; i < array.size(); i++) {
            pages[i] = this.researchPageFromJson(array.get(i).getAsJsonObject());
         }

         return new ArrayCraftPage(pages);
      }
   }

   private static final class CategoryDefinition {
      private final ResourceLocation icon;
      private final ResourceLocation background;
      private final int lineColor;

      private CategoryDefinition(ResourceLocation icon, ResourceLocation background, int lineColor) {
         this.icon = icon;
         this.background = background;
         this.lineColor = lineColor;
      }

      public static ResearchLoader.CategoryDefinition of(ResearchCategoryList categoryList) {
         return new ResearchLoader.CategoryDefinition(categoryList.icon, categoryList.background, categoryList.lineColor);
      }

      public ResearchCategoryList createList() {
         return new ResearchCategoryList(this.icon, this.background, this.lineColor);
      }

      public JsonObject toJson() {
         JsonObject object = new JsonObject();
         object.addProperty("icon", this.icon.toString());
         object.addProperty("background", this.background.toString());
         object.addProperty("line_color", this.lineColor);
         return object;
      }
   }
}
