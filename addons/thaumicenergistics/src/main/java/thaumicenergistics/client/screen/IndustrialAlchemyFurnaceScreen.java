package thaumicenergistics.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.menu.IndustrialAlchemyFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class IndustrialAlchemyFurnaceScreen extends AbstractContainerScreen<IndustrialAlchemyFurnaceMenu> {
   private static final ResourceLocation GUI_TEXTURE = ThaumicEnergistics.location("textures/screen/industrial_alchemy_furnace.png");
   private static final int MAX_ENERGY = 80000;
   private static final double MAX_ESSENTIA = 5000.0;

   public IndustrialAlchemyFurnaceScreen(IndustrialAlchemyFurnaceMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 176;
      this.imageHeight = 184;
      this.inventoryLabelY = this.imageHeight - 94;
   }

   protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      RenderSystem.enableBlend();
      guiGraphics.blit(GUI_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
      int energyProgress = this.scaled(46, (double)((IndustrialAlchemyFurnaceMenu)this.menu).getEnergy() / 80000.0);
      guiGraphics.blit(GUI_TEXTURE, this.leftPos + 106, this.topPos + 13 + 46 - energyProgress, 216, 46 - energyProgress, 9, energyProgress);
      int essentiaProgress = this.scaled(48, (double)((IndustrialAlchemyFurnaceMenu)this.menu).getTotalEssentia() / 5000.0);
      guiGraphics.blit(GUI_TEXTURE, this.leftPos + 62, this.topPos + 12 + 48 - essentiaProgress, 200, 48 - essentiaProgress, 8, essentiaProgress);
      guiGraphics.blit(GUI_TEXTURE, this.leftPos + 61, this.topPos + 8, 232, 0, 10, 55);
      RenderSystem.disableBlend();
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      this.renderTooltip(guiGraphics, mouseX, mouseY);
   }

   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
   }

   private int scaled(int in, double scale) {
      return (int)(Math.max(0.0, Math.min(scale, 1.0)) * (double)in);
   }
}
