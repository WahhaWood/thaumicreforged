package thaumcraft.api.research;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.util.DataGenCompat;

public class ResearchItem {
   public final String key;
   public final String category;
   public final AspectList tags;
   public String[] parents;
   public String[] parentsHidden;
   public String[] siblings;
   public final int displayColumn;
   public final int displayRow;
   @Nullable
   public final Object icon;
   private final int complexity;
   private final boolean special;
   private final boolean secondary;
   private final boolean round;
   private final boolean stub;
   private final boolean virtual;
   private final boolean concealed;
   private final boolean hidden;
   private final boolean lost;
   private final boolean autoUnlock;
   private Ingredient[] itemTriggers;
   private String[] entityTriggers;
   private Aspect[] aspectTriggers;
   private final ResearchPage[] pages;

   public ResearchItem(JsonObject object, String key, String category, ResearchPage[] pages) {
      this.key = key;
      this.category = category;
      this.tags = GsonHelper.isArrayNode(object, "tags") ? AspectList.fromJson(GsonHelper.getAsJsonArray(object, "tags")) : new AspectList();
      this.displayColumn = GsonHelper.getAsInt(object, "column");
      this.displayRow = GsonHelper.getAsInt(object, "row");
      JsonElement iconElement = object.get("icon");
      if (iconElement == null || iconElement.isJsonNull() || iconElement.isJsonPrimitive()) {
         this.icon = ResourceLocation.parse(GsonHelper.getAsString(object, "icon"));
      } else if (iconElement.isJsonObject()) {
         JsonObject iconObj = iconElement.getAsJsonObject();
         if (GsonHelper.isStringValue(iconObj, "block")) {
            this.icon = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(GsonHelper.getAsString(iconObj, "block")));
         } else {
            this.icon = DataGenCompat.ingredientFromJson(iconElement);
         }
      } else {
         this.icon = DataGenCompat.ingredientFromJson(iconElement);
      }

      if (GsonHelper.isArrayNode(object, "parents")) {
         JsonArray array = GsonHelper.getAsJsonArray(object, "parents");
         this.parents = new String[array.size()];

         for (int i = 0; i < array.size(); i++) {
            this.parents[i] = array.get(i).getAsString().toLowerCase(Locale.ROOT);
         }
      }

      if (GsonHelper.isArrayNode(object, "parents_hidden")) {
         JsonArray array = GsonHelper.getAsJsonArray(object, "parents_hidden");
         this.parentsHidden = new String[array.size()];

         for (int i = 0; i < array.size(); i++) {
            this.parentsHidden[i] = array.get(i).getAsString().toLowerCase(Locale.ROOT);
         }
      }

      if (GsonHelper.isArrayNode(object, "siblings")) {
         JsonArray array = GsonHelper.getAsJsonArray(object, "siblings");
         this.siblings = new String[array.size()];

         for (int i = 0; i < array.size(); i++) {
            this.siblings[i] = array.get(i).getAsString().toLowerCase(Locale.ROOT);
         }
      }

      this.complexity = GsonHelper.getAsInt(object, "complexity", 0);
      this.special = GsonHelper.getAsBoolean(object, "special", false);
      this.secondary = GsonHelper.getAsBoolean(object, "secondary", false);
      this.round = GsonHelper.getAsBoolean(object, "round", false);
      this.stub = GsonHelper.getAsBoolean(object, "stub", false);
      this.virtual = GsonHelper.getAsBoolean(object, "virtual", false);
      this.concealed = GsonHelper.getAsBoolean(object, "concealed", false);
      this.hidden = GsonHelper.getAsBoolean(object, "hidden", false);
      this.lost = GsonHelper.getAsBoolean(object, "lost", false);
      this.autoUnlock = GsonHelper.getAsBoolean(object, "autoUnlock", false);
      if (GsonHelper.isArrayNode(object, "item_triggers")) {
         JsonArray array = GsonHelper.getAsJsonArray(object, "item_triggers");
         this.itemTriggers = new Ingredient[array.size()];

         for (int i = 0; i < array.size(); i++) {
            this.itemTriggers[i] = DataGenCompat.ingredientFromJson(array.get(i));
         }
      }

      if (GsonHelper.isArrayNode(object, "entity_triggers")) {
         JsonArray array = GsonHelper.getAsJsonArray(object, "entity_triggers");
         this.entityTriggers = new String[array.size()];

         for (int i = 0; i < array.size(); i++) {
            this.entityTriggers[i] = array.get(i).getAsString();
         }
      }

      if (GsonHelper.isArrayNode(object, "aspect_triggers")) {
         JsonArray array = GsonHelper.getAsJsonArray(object, "aspect_triggers");
         this.aspectTriggers = new Aspect[array.size()];

         for (int i = 0; i < array.size(); i++) {
            this.aspectTriggers[i] = Aspect.getAspect(array.get(i).getAsString());
         }
      }

      this.pages = pages;
   }

   public ResearchPage[] getPages() {
      return this.pages;
   }

   public void addSibling(String siblingKey) {
      if (siblingKey != null && !siblingKey.isEmpty()) {
         if (this.siblings == null) {
            this.siblings = new String[]{siblingKey};
         } else {
            for (String sibling : this.siblings) {
               if (siblingKey.equals(sibling)) {
                  return;
               }
            }

            String[] next = Arrays.copyOf(this.siblings, this.siblings.length + 1);
            next[this.siblings.length] = siblingKey;
            this.siblings = next;
         }
      }
   }

   public Ingredient[] getItemTriggers() {
      return this.itemTriggers;
   }

   public String[] getEntityTriggers() {
      return this.entityTriggers;
   }

   public Aspect[] getAspectTriggers() {
      return this.aspectTriggers;
   }

   public void onPostLoad() {
   }

   public void onPlayerKnowledgeLoaded(Player player, Set<String> completed) {
   }

   public boolean doesPlayerHaveRequisites(Player player, Set<String> completed) {
      boolean out = true;
      String[] parents = this.parents;
      if (parents != null && parents.length > 0) {
         out = false;
         if (!completed.isEmpty()) {
            out = true;

            for (String item : parents) {
               if (!completed.contains(item)) {
                  return false;
               }
            }
         }
      }

      parents = this.parentsHidden;
      if (parents != null && parents.length > 0) {
         out = false;
         if (!completed.isEmpty()) {
            out = true;

            for (String itemx : parents) {
               if (!completed.contains(itemx)) {
                  return false;
               }
            }
         }
      }

      return out;
   }

   public MutableComponent getName() {
      return Component.translatable("tc.research." + this.key + ".name");
   }

   public MutableComponent getText() {
      return Component.translatable("tc.research." + this.key + ".text");
   }

   public boolean isSpecial() {
      return this.special;
   }

   public boolean isStub() {
      return this.stub;
   }

   public boolean isLost() {
      return this.lost;
   }

   public boolean isConcealed() {
      return this.concealed;
   }

   public boolean isHidden() {
      return this.hidden;
   }

   public boolean isVirtual() {
      return this.virtual;
   }

   public boolean isAutoUnlock() {
      return this.autoUnlock;
   }

   public boolean isRound() {
      return this.round;
   }

   public boolean isSecondary() {
      return this.secondary;
   }

   public int getComplexity() {
      return this.complexity;
   }

   public Aspect getResearchPrimaryTag() {
      Aspect aspect = null;
      int amount = 0;
      if (this.tags != null) {
         for (Aspect aspect2 : this.tags.getAspects()) {
            if (this.tags.getAmount(aspect2) > amount) {
               aspect = aspect2;
               amount = this.tags.getAmount(aspect2);
            }
         }
      }

      return aspect;
   }
}
