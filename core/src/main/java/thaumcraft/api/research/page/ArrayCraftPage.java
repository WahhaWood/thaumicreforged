package thaumcraft.api.research.page;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.block.table.ResearchRecipeScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ArrayCraftPage implements ResearchPage {
   private final ResearchPage[] pages;

   public ArrayCraftPage(ResearchPage[] pages) {
      this.pages = pages;
   }

   public ResearchPage[] getPages() {
      return this.pages;
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void render(ResearchRecipeScreen screen, GuiGraphics graphics, Font font, int page, int x, int y, int mx, int my) {
      if (this.pages.length != 0) {
         this.pages[(int)(System.currentTimeMillis() / 1000L % this.pages.length)].render(screen, graphics, font, page, x, y, mx, my);
      }
   }
}
