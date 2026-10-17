package thaumcraft.block.magic_box;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MagicBoxScreen extends AbstractContainerScreen<MagicBoxMenu> {
   public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");
   private final int inventoryRows;

   public MagicBoxScreen(MagicBoxMenu menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title);
      this.inventoryRows = menu.numRows;
      this.imageWidth = 176;
      this.imageHeight = 114 + this.inventoryRows * 18;
   }

   public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(gg, mouseX, mouseY, partialTick);
      super.render(gg, mouseX, mouseY, partialTick);
      this.renderTooltip(gg, mouseX, mouseY);
   }

   protected void renderBg(GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      int x = (this.width - this.imageWidth) / 2;
      int y = (this.height - this.imageHeight) / 2;
      gg.blit(TEXTURE, x, y, 0, 0, this.imageWidth, 17);

      for (int i = 0; i < this.inventoryRows; i++) {
         gg.blit(TEXTURE, x, y + 17 + i * 18, 0, 17, this.imageWidth, 18);
      }

      gg.blit(TEXTURE, x, y + 17 + this.inventoryRows * 18, 0, 126, this.imageWidth, 96);
   }

   protected void renderLabels(GuiGraphics gg, int mouseX, int mouseY) {
      gg.drawString(this.font, this.title, 8, 6, 4210752, false);
      gg.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 94, 4210752, false);
   }
}
