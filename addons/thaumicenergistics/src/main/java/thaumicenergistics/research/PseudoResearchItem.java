package thaumicenergistics.research;

import com.google.gson.JsonObject;
import java.util.Set;
import thaumicenergistics.ThaumicEnergistics;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.research.ResearchApi;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;

public class PseudoResearchItem extends ResearchItem {
   @Nullable
   private final String pseudoFor;
   @Nullable
   private ResearchItem original;

   public PseudoResearchItem(JsonObject object, String key, String category, ResearchPage[] pages) {
      super(object, key, category, pages);
      this.pseudoFor = GsonHelper.getAsString(object, "pseudo_for", "");
   }

   public void onPostLoad() {
      if (this.pseudoFor != null && !this.pseudoFor.isEmpty()) {
         this.original = ResearchApi.getResearch(this.pseudoFor);
         if (this.original == null) {
            ThaumicEnergistics.LOGGER.warn("Pseudo research {} references missing target {}", this.key, this.pseudoFor);
         } else {
            this.original.addSibling(this.key);
         }
      }
   }

   public void onPlayerKnowledgeLoaded(Player player, Set<String> completed) {
      if (this.pseudoFor != null && !this.pseudoFor.isEmpty()) {
         if ((completed.contains(this.pseudoFor) || completed.contains("@" + this.pseudoFor)) && !completed.contains(this.key)) {
            ResearchApi.completeResearch(player, this.key);
         }
      }
   }

   public ResearchPage[] getPages() {
      return this.original != null ? this.original.getPages() : super.getPages();
   }

   public MutableComponent getName() {
      return this.original != null ? this.original.getName() : super.getName();
   }

   public MutableComponent getText() {
      return this.original != null ? this.original.getText() : super.getText();
   }

   public boolean isStub() {
      return true;
   }

   public boolean isHidden() {
      return true;
   }

   public boolean isSecondary() {
      return super.isSecondary() || this.original != null && this.original.isSecondary();
   }

   public int getComplexity() {
      return 1;
   }
}
