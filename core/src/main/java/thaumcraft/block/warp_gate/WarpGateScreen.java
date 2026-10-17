package thaumcraft.block.warp_gate;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class WarpGateScreen extends AbstractContainerScreen<WarpGateMenu> {
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/screen/warp_gate.png");

   public WarpGateScreen(WarpGateMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 176;
      this.imageHeight = 166;
   }

   protected void init() {
      super.init();
      this.addRenderableWidget(new WarpGateScreen.LockButton(this.leftPos + 5, this.topPos + 5, ((WarpGateMenu)this.menu).getGate().isLocked()));
   }

   public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      this.renderTooltip(guiGraphics, mouseX, mouseY);
   }

   protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
      guiGraphics.drawString(this.font, Component.translatable("ttmisc.lockedGate"), this.leftPos + 20, this.topPos + 7, 10066329, true);
   }

   protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
   }

   private class LockButton extends AbstractButton {
      private boolean locked;

      public LockButton(int x, int y, boolean locked) {
         super(x, y, 13, 13, Component.empty());
         this.locked = locked;
      }

      public void onPress() {
         this.locked = !this.locked;
         if (WarpGateScreen.this.minecraft != null && WarpGateScreen.this.minecraft.gameMode != null) {
            WarpGateScreen.this.minecraft.gameMode.handleInventoryButtonClick(((WarpGateMenu)WarpGateScreen.this.menu).containerId, 0);
         }
      }

      public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
         if (this.locked) {
            int vOffset = 0;
            guiGraphics.blit(WarpGateScreen.TEXTURE, this.getX(), this.getY(), 176, vOffset, this.width, this.height);
         }
      }

      protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
      }
   }
}
