package thaumicenergistics.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.block.crucible_assembler.CrucibleAssemblerBlockEntity;
import thaumicenergistics.menu.CrucibleAssemblerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class CrucibleAssemblerScreen extends RecipeAssemblerScreen<CrucibleAssemblerMenu> {
   private static final int MAIN_GUI_WIDTH = 176;
   private static final int UPGRADE_GUI_WIDTH = 35;
   private static final int GUI_HEIGHT = 184;
   private static final int UPGRADE_GUI_HEIGHT = 104;
   private static final ResourceLocation GUI_TEXTURE = ThaumicEnergistics.location("textures/screen/crucible_assembler.png");
   private static final int PROGRESS_BAR_X = 164;
   private static final int PROGRESS_BAR_Y = 79;
   private static final int PROGRESS_BAR_U = 215;
   private static final int PROGRESS_BAR_V = 0;
   private static final int PROGRESS_BAR_W = 4;
   private static final int PROGRESS_BAR_H = 16;

   public CrucibleAssemblerScreen(CrucibleAssemblerMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title, new RecipeGridPanel.Layout(44, 7, 5, 5, 211));
      this.imageWidth = 211;
      this.imageHeight = 184;
      this.inventoryLabelY = 90;
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.blit(GUI_TEXTURE, this.leftPos, this.topPos, 0, 0, 176, this.imageHeight);
      graphics.blit(GUI_TEXTURE, this.leftPos + 176, this.topPos, 176, 0, 35, 104);
      // the knowledge core slot is gone: paint the panel over its art
      graphics.fill(this.leftPos + 7, this.topPos + 6, this.leftPos + 25, this.topPos + 24, 0xFF5C5C5C);
      CrucibleAssemblerBlockEntity assembler = ((CrucibleAssemblerMenu)this.menu).assembler;
      if (assembler.isCrafting) {
         float progress = 1.0F - assembler.getPercentComplete();
         int filledHeight = (int)(16.0F * progress);
         if (filledHeight < 16) {
            int yOffset = 16 - filledHeight;
            graphics.blit(GUI_TEXTURE, this.leftPos + 164, this.topPos + 79 + yOffset, 215, 0 + yOffset, 4, filledHeight);
         }
      }

      this.renderRecipes(graphics);
   }

   protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
   }
}
