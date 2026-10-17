package thaumcraft.api.research;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import thaumcraft.block.table.ResearchRecipeScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface ResearchPage {
   @OnlyIn(Dist.CLIENT)
   default void render(ResearchRecipeScreen screen, GuiGraphics graphics, Font font, int page, int x, int y, int mx, int my) {
   }
}
