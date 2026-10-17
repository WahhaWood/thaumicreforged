package thaumicenergistics.client.screen;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
class InscriberIconButton extends Button {
   private Icon icon;
   private final boolean halfSize;

   InscriberIconButton(int x, int y, int width, int height, Icon icon, boolean halfSize, OnPress onPress) {
      super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
      this.icon = icon;
      this.halfSize = halfSize;
   }

   void setIcon(Icon icon) {
      this.icon = icon;
   }

   protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      if (this.visible) {
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         if (this.halfSize) {
            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate((float)this.getX(), (float)this.getY(), 0.0F);
            pose.scale(0.5F, 0.5F, 1.0F);
            this.renderIcon(graphics, Icon.TOOLBAR_BUTTON_BACKGROUND, 0, 0);
            this.renderIcon(graphics, this.icon, 0, 0);
            pose.popPose();
         } else {
            this.renderIcon(graphics, Icon.TOOLBAR_BUTTON_BACKGROUND, this.getX(), this.getY());
            this.renderIcon(graphics, this.icon, this.getX(), this.getY());
         }

         RenderSystem.enableDepthTest();
      }
   }

   private void renderIcon(GuiGraphics graphics, Icon icon, int x, int y) {
      if (icon != null) {
         Blitter blitter = icon.getBlitter().dest(x, y);
         if (!this.active) {
            blitter.opacity(0.5F);
         }

         blitter.blit(graphics);
      }
   }
}
