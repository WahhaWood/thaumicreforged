package thaumcraft.api.research.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.util.GsonHelper;
import thaumcraft.api.research.ResearchApi;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;

public class KamiResearchItem extends TTResearchItem {
   private final Set<String> allowedCategories = new LinkedHashSet<>();
   private final Set<String> blacklistPrefixes = new LinkedHashSet<>();
   private final String blacklistSuffix;
   private final boolean autoExpandHiddenParents;

   public KamiResearchItem(JsonObject object, String key, String category, ResearchPage[] pages) {
      super(object, key, category, pages);
      this.blacklistSuffix = GsonHelper.getAsString(object, "kami_blacklist_suffix", "kami").toLowerCase(Locale.ROOT);
      this.autoExpandHiddenParents = GsonHelper.getAsBoolean(object, "kami_expand_parents", true);
      JsonArray categoriesJson = GsonHelper.isArrayNode(object, "kami_categories") ? GsonHelper.getAsJsonArray(object, "kami_categories") : null;
      if (categoriesJson != null) {
         for (JsonElement element : categoriesJson) {
            this.allowedCategories.add(element.getAsString().toLowerCase(Locale.ROOT));
         }
      } else {
         this.allowedCategories.add(category.toLowerCase(Locale.ROOT));
         this.allowedCategories.add("basics");
         this.allowedCategories.add("golemancy");
         this.allowedCategories.add("artifice");
         this.allowedCategories.add("alchemy");
         this.allowedCategories.add("thaumaturgy");
         this.allowedCategories.add("tinkerer");
         this.allowedCategories.add("thaumictinkerer");
      }

      JsonArray blacklistJson = GsonHelper.isArrayNode(object, "kami_blacklist_prefixes") ? GsonHelper.getAsJsonArray(object, "kami_blacklist_prefixes") : null;
      if (blacklistJson != null) {
         for (JsonElement element : blacklistJson) {
            this.blacklistPrefixes.add(element.getAsString().toLowerCase(Locale.ROOT));
         }
      } else {
         this.blacklistPrefixes.add("minilith");
      }
   }

   @Override
   protected String defaultPrefixKey() {
      return "ttresearch.prefix.kami";
   }

   @Override
   public boolean isConcealed() {
      return true;
   }

   @Override
   protected boolean checkInfusionRequirement() {
      return false;
   }

   @Override
   public void onPostLoad() {
      if (this.autoExpandHiddenParents && !this.isAutoUnlock()) {
         ArrayList<String> requirements = new ArrayList<>();
         if (this.parentsHidden != null && this.parentsHidden.length > 0) {
            requirements.addAll(Arrays.asList(this.parentsHidden));
         }

         for (Entry<String, ResearchCategoryList> categoryEntry : ResearchApi.getResearchLists().entrySet()) {
            String categoryKey = categoryEntry.getKey().toLowerCase(Locale.ROOT);
            if (this.allowedCategories.contains(categoryKey)) {
               ResearchCategoryList category = categoryEntry.getValue();

               for (ResearchItem research : category.research.values()) {
                  if (this.isValidKamiRequirement(research, requirements)) {
                     requirements.add(research.key);
                  }
               }
            }
         }

         this.parentsHidden = requirements.toArray(new String[0]);
         super.onPostLoad();
      } else {
         super.onPostLoad();
      }
   }

   private boolean isValidKamiRequirement(ResearchItem research, ArrayList<String> requirements) {
      if (research == this || research instanceof KamiResearchItem) {
         return false;
      } else if (requirements.contains(research.key)) {
         return false;
      } else if (research.isLost() || research.isVirtual()) {
         return false;
      } else if (research.parentsHidden == null && research.parents == null) {
         return false;
      } else if (research.getAspectTriggers() == null && research.getEntityTriggers() == null && research.getItemTriggers() == null) {
         String researchKey = research.key.toLowerCase(Locale.ROOT);
         if (!this.blacklistSuffix.isEmpty() && researchKey.endsWith(this.blacklistSuffix)) {
            return false;
         } else {
            for (String prefix : this.blacklistPrefixes) {
               if (!prefix.isEmpty() && researchKey.startsWith(prefix)) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }
}
