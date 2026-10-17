package thaumcraft.data.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.util.DataGenCompat;

public class ResearchItemBuilder {
   public final String key;
   public final String category;
   public final AspectList tags;
   public final int displayColumn;
   public final int displayRow;
   public final Object icon;
   public String[] parents;
   public String[] parentsHidden;
   public String[] siblings;
   private int complexity;
   private boolean special;
   private boolean secondary;
   private boolean round;
   private boolean stub;
   private boolean virtual;
   private boolean concealed;
   private boolean hidden;
   private boolean lost;
   private boolean autoUnlock;
   private Ingredient[] itemTriggers;
   private String[] entityTriggers;
   private Aspect[] aspectTriggers;
   private final List<JsonObject> pages = new ArrayList<>();
   private final List<ICondition> conditions = new ArrayList<>();
   private int warp;
   private ResourceLocation researchType;
   private Boolean autoInfusionParent;

   public ResearchItemBuilder(String key, String category, int column, int row, Object icon) {
      this.key = key;
      this.category = category;
      this.tags = new AspectList();
      this.displayColumn = column;
      this.displayRow = row;
      this.icon = icon;
   }

   public void finish(Consumer<ResearchItemBuilder> consumer) {
      consumer.accept(this);
   }

   public ResearchItemBuilder tag(Aspect aspect, int amount) {
      this.tags.add(aspect, amount);
      return this;
   }

   public ResearchItemBuilder parent(String... values) {
      this.parents = values;
      return this;
   }

   public ResearchItemBuilder parentHidden(String... values) {
      this.parentsHidden = values;
      return this;
   }

   public ResearchItemBuilder siblings(String... values) {
      this.siblings = values;
      return this;
   }

   public ResearchItemBuilder complexity() {
      return this.complexity(1);
   }

   public ResearchItemBuilder complexity(int value) {
      this.complexity = value;
      return this;
   }

   public ResearchItemBuilder special() {
      this.special = true;
      return this;
   }

   public ResearchItemBuilder secondary() {
      this.secondary = true;
      return this;
   }

   public ResearchItemBuilder round() {
      this.round = true;
      return this;
   }

   public ResearchItemBuilder stub() {
      this.stub = true;
      return this;
   }

   public ResearchItemBuilder virtual() {
      this.virtual = true;
      return this;
   }

   public ResearchItemBuilder concealed() {
      this.concealed = true;
      return this;
   }

   public ResearchItemBuilder hidden() {
      this.hidden = true;
      return this;
   }

   public ResearchItemBuilder lost() {
      this.lost = true;
      return this;
   }

   public ResearchItemBuilder autoUnlock() {
      this.autoUnlock = true;
      return this;
   }

   public ResearchItemBuilder researchType(ResourceLocation id) {
      this.researchType = id;
      return this;
   }

   public ResearchItemBuilder autoInfusionParent(boolean value) {
      this.autoInfusionParent = value;
      return this;
   }

   public ResearchItemBuilder triggers(ItemStack... stacks) {
      return this.triggers(Arrays.stream(stacks).map(xva$0 -> Ingredient.of(new ItemStack[]{xva$0})).toArray(Ingredient[]::new));
   }

   public ResearchItemBuilder trigger(TagKey<Item> tagKey) {
      return this.triggers(Ingredient.of(tagKey));
   }

   public ResearchItemBuilder triggers(Ingredient... ingredients) {
      this.itemTriggers = ingredients;
      return this;
   }

   public ResearchItemBuilder triggers(String... entities) {
      this.entityTriggers = entities;
      return this;
   }

   public ResearchItemBuilder triggers(Aspect... aspects) {
      this.aspectTriggers = aspects;
      return this;
   }

   public ResearchItemBuilder condition(ICondition condition) {
      this.conditions.add(condition);
      return this;
   }

   public ResearchItemBuilder pageDesc(int key) {
      return this.pageText("tc.research." + this.key + ".page." + key);
   }

   public ResearchItemBuilder pageDesc(int from, int to) {
      for (int i = from; i <= to; i++) {
         this.pageDesc(i);
      }

      return this;
   }

   public ResearchItemBuilder pageText(String key) {
      JsonObject object = new JsonObject();
      object.addProperty("type", "text");
      object.addProperty("key", key);
      this.pages.add(object);
      return this;
   }

   public ResearchItemBuilder pageImage(String key, String image) {
      JsonObject object = new JsonObject();
      object.addProperty("type", "text");
      object.addProperty("key", key);
      object.addProperty("image", image);
      this.pages.add(object);
      return this;
   }

   public ResearchItemBuilder pageImage(String key, String image, int width, int height) {
      JsonObject object = new JsonObject();
      object.addProperty("type", "text");
      object.addProperty("key", key);
      object.addProperty("image", image);
      object.addProperty("width", width);
      object.addProperty("height", height);
      this.pages.add(object);
      return this;
   }

   public ResearchItemBuilder pageRecipe(String type, String id) {
      JsonObject object = new JsonObject();
      object.addProperty("type", type);
      object.addProperty("id", id);
      this.pages.add(object);
      return this;
   }

   public ResearchItemBuilder pageRecipes(String type, String... ids) {
      JsonObject object = new JsonObject();
      object.addProperty("type", "recipes");
      JsonArray array = new JsonArray();

      for (String id : ids) {
         JsonObject recipeObj = new JsonObject();
         recipeObj.addProperty("type", type);
         recipeObj.addProperty("id", id);
         array.add(recipeObj);
      }

      object.add("value", array);
      this.pages.add(object);
      return this;
   }

   public ResearchItemBuilder pageStructure(@Nullable AspectList aspectList, Object... objects) {
      return this.pageStructure(1, 2, 1, aspectList, objects);
   }

   public ResearchItemBuilder pageStructure(int dx, int dy, int dz, @Nullable AspectList aspectList, Object... objects) {
      JsonObject object = new JsonObject();
      object.addProperty("type", "formed_structure");
      if (dx != 1) {
         object.addProperty("dx", dx);
      }

      if (dy != 2) {
         object.addProperty("dy", dy);
      }

      if (dz != 1) {
         object.addProperty("dz", dz);
      }

      if (aspectList != null) {
         object.add("aspects", aspectList.toJson());
      }

      JsonArray ingredients = new JsonArray();

      for (Object obj : objects) {
         if (obj instanceof TagKey) {
            ingredients.add(DataGenCompat.ingredientToJson(Ingredient.of((TagKey)obj)));
         } else if (obj instanceof Ingredient ing) {
            ingredients.add(DataGenCompat.ingredientToJson(ing));
         } else if (obj instanceof ItemLike itemLike) {
            if (itemLike != Blocks.AIR && (itemLike.asItem() != Items.AIR || obj instanceof Block)) {
               JsonObject ingredient = new JsonObject();
               if (itemLike.asItem() == Items.AIR && obj instanceof Block block) {
                  ingredient.addProperty("block", BuiltInRegistries.BLOCK.getKey(block).toString());
               } else {
                  ingredient.addProperty("item", BuiltInRegistries.ITEM.getKey(itemLike.asItem()).toString());
               }

               ingredients.add(ingredient);
            } else {
               ingredients.add("empty");
            }
         }
      }

      object.add("ingredients", ingredients);
      this.pages.add(object);
      return this;
   }

   public ResearchItemBuilder warp(int value) {
      this.warp = value;
      return this;
   }

   public JsonObject toJson() {
      JsonObject object = new JsonObject();
      if (!this.conditions.isEmpty()) {
         JsonArray array = new JsonArray();

         for (ICondition condition : this.conditions) {
            array.add(DataGenCompat.conditionToJson(condition));
         }

         object.add("conditions", array);
      }

      if (this.researchType != null) {
         object.addProperty("research_type", this.researchType.toString());
      }

      if (this.autoInfusionParent != null) {
         object.addProperty("auto_infusion_parent", this.autoInfusionParent);
      }

      if (!this.tags.aspects.isEmpty()) {
         object.add("tags", this.tags.toJson());
      }

      object.addProperty("column", this.displayColumn);
      object.addProperty("row", this.displayRow);
      if (this.icon instanceof Ingredient ingredient) {
         object.add("icon", DataGenCompat.ingredientToJson(ingredient));
      } else if (this.icon instanceof Block block) {
         JsonObject icon = new JsonObject();
         icon.addProperty("block", BuiltInRegistries.BLOCK.getKey(block).toString());
         object.add("icon", icon);
      } else {
         object.addProperty("icon", this.icon.toString());
      }

      if (this.parents != null) {
         JsonArray array = new JsonArray();

         for (String parent : this.parents) {
            array.add(parent);
         }

         object.add("parents", array);
      }

      if (this.parentsHidden != null) {
         JsonArray array = new JsonArray();

         for (String parent : this.parentsHidden) {
            array.add(parent);
         }

         object.add("parents_hidden", array);
      }

      if (this.siblings != null) {
         JsonArray array = new JsonArray();

         for (String parent : this.siblings) {
            array.add(parent);
         }

         object.add("siblings", array);
      }

      if (this.complexity > 0) {
         object.addProperty("complexity", this.complexity);
      }

      if (this.special) {
         object.addProperty("special", true);
      }

      if (this.secondary) {
         object.addProperty("secondary", true);
      }

      if (this.round) {
         object.addProperty("round", true);
      }

      if (this.stub) {
         object.addProperty("stub", true);
      }

      if (this.virtual) {
         object.addProperty("virtual", true);
      }

      if (this.concealed) {
         object.addProperty("concealed", true);
      }

      if (this.hidden) {
         object.addProperty("hidden", true);
      }

      if (this.lost) {
         object.addProperty("lost", true);
      }

      if (this.autoUnlock) {
         object.addProperty("autoUnlock", true);
      }

      if (this.itemTriggers != null) {
         JsonArray array = new JsonArray();

         for (Ingredient ingredient : this.itemTriggers) {
            array.add(DataGenCompat.ingredientToJson(ingredient));
         }

         object.add("item_triggers", array);
      }

      if (this.entityTriggers != null) {
         JsonArray array = new JsonArray();

         for (String entityType : this.entityTriggers) {
            array.add(entityType);
         }

         object.add("entity_triggers", array);
      }

      if (this.aspectTriggers != null) {
         JsonArray array = new JsonArray();

         for (Aspect aspect : this.aspectTriggers) {
            array.add(aspect.getTag());
         }

         object.add("aspect_triggers", array);
      }

      if (!this.pages.isEmpty()) {
         JsonArray array = new JsonArray();

         for (JsonObject page : this.pages) {
            array.add(page);
         }

         object.add("pages", array);
      }

      if (this.warp > 0) {
         object.addProperty("warp", this.warp);
      }

      return object;
   }

   public static ResearchItemBuilder research(String key, String category, int column, int row, ItemLike icon) {
      return icon.asItem() == Items.AIR && icon instanceof Block block
         ? new ResearchItemBuilder(key, category, column, row, block)
         : new ResearchItemBuilder(key, category, column, row, Ingredient.of(new ItemLike[]{icon}));
   }

   public static ResearchItemBuilder research(String key, String category, int column, int row, TagKey<Item> icon) {
      return new ResearchItemBuilder(key, category, column, row, Ingredient.of(icon));
   }

   public static ResearchItemBuilder research(String key, String category, int column, int row, ItemStack icon) {
      return new ResearchItemBuilder(
         key, category, column, row, DataGenCompat.hasTag(icon) ? DataComponentIngredient.of(true, icon) : Ingredient.of(new ItemStack[]{icon})
      );
   }

   public static ResearchItemBuilder research(String key, String category, int column, int row, Ingredient icon) {
      return new ResearchItemBuilder(key, category, column, row, icon);
   }

   public static ResearchItemBuilder research(String key, String category, int column, int row, ResourceLocation icon) {
      return new ResearchItemBuilder(key, category, column, row, icon);
   }
}
