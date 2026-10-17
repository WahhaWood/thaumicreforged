package thaumcraft.api.research.type;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.api.research.page.ArrayCraftPage;
import thaumcraft.api.research.page.InfusionEnchantmentRecipePage;
import thaumcraft.api.research.page.InfusionRecipePage;

public class TTResearchItem extends ResearchItem {
   private final String nameTranslationKey;
   private final String textTranslationKey;
   private final boolean showTooltipPrefix;
   private final String tooltipPrefixKey;
   private final String infusionParentKey;
   private final boolean autoInfusionParent;

   public TTResearchItem(JsonObject object, String key, String category, ResearchPage[] pages) {
      super(object, key, category, pages);
      this.nameTranslationKey = GsonHelper.getAsString(object, "name_key", "tc.research." + key + ".name");
      this.textTranslationKey = GsonHelper.getAsString(object, "text_key", "ttresearch.lore." + key);
      this.showTooltipPrefix = GsonHelper.getAsBoolean(object, "show_prefix", false);
      this.tooltipPrefixKey = GsonHelper.getAsString(object, "prefix_key", this.defaultPrefixKey());
      this.infusionParentKey = GsonHelper.getAsString(object, "infusion_parent", "infusion");
      this.autoInfusionParent = GsonHelper.getAsBoolean(object, "auto_infusion_parent", true);
   }

   protected String defaultPrefixKey() {
      return "ttresearch.prefix";
   }

   protected boolean checkInfusionRequirement() {
      return true;
   }

   @Override
   public MutableComponent getName() {
      return Component.translatable(this.nameTranslationKey);
   }

   @Override
   public MutableComponent getText() {
      MutableComponent lore = Component.translatable(this.textTranslationKey);
      return !this.showTooltipPrefix ? lore : Component.translatable(this.tooltipPrefixKey).append(lore);
   }

   @Override
   public void onPostLoad() {
      if (this.autoInfusionParent && this.checkInfusionRequirement() && this.hasInfusionPage(this.getPages())) {
         this.prependHiddenParent(this.infusionParentKey);
      }
   }

   private boolean hasInfusionPage(ResearchPage[] pages) {
      if (pages != null && pages.length != 0) {
         for (ResearchPage page : pages) {
            if (page instanceof InfusionRecipePage || page instanceof InfusionEnchantmentRecipePage) {
               return true;
            }

            if (page instanceof ArrayCraftPage arrayPage && this.hasInfusionPage(arrayPage.getPages())) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   protected void prependHiddenParent(String parentKey) {
      if (parentKey != null && !parentKey.isEmpty()) {
         if (this.parentsHidden != null && this.parentsHidden.length != 0) {
            for (String existing : this.parentsHidden) {
               if (parentKey.equals(existing)) {
                  return;
               }
            }

            String[] next = new String[this.parentsHidden.length + 1];
            next[0] = parentKey;
            System.arraycopy(this.parentsHidden, 0, next, 1, this.parentsHidden.length);
            this.parentsHidden = next;
         } else {
            this.parentsHidden = new String[]{parentKey};
         }
      }
   }
}
