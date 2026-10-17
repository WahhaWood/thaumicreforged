package thaumcraft.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor.ARGB32;
import org.joml.Matrix4f;

public class AspectTooltipClientComponent implements ClientTooltipComponent {
   private static final int ICON_SIZE = 16;
   private static final int ICON_TEXT_GAP = 3;
   private final AspectTooltipData data;
   private final Component line;

   public AspectTooltipClientComponent(AspectTooltipData data) {
      this.data = data;
      this.line = Component.empty().append(data.name()).append(Component.literal(" x" + data.amount()));
   }

   public int getHeight() {
      return 16;
   }

   public int getWidth(Font font) {
      return 19 + font.width(this.line);
   }

   public void renderText(Font font, int x, int y, Matrix4f matrix4f, BufferSource bufferSource) {
      int yOffset = (16 - 9) / 2;
      font.drawInBatch(this.line, x + 16 + 3, y + yOffset, 16777215, false, matrix4f, bufferSource, DisplayMode.NORMAL, 0, 15728880);
   }

   public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
      int color = this.data.color();
      float red = ARGB32.red(color) / 255.0F;
      float green = ARGB32.green(color) / 255.0F;
      float blue = ARGB32.blue(color) / 255.0F;
      guiGraphics.setColor(red, green, blue, 1.0F);
      guiGraphics.blit(this.data.texture(), x, y, 0.0F, 0.0F, 16, 16, 16, 16);
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
   }
}
