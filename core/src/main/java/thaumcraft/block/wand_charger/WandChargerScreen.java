package thaumcraft.block.wand_charger;

import com.mojang.blaze3d.systems.RenderSystem;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;

public class WandChargerScreen extends AbstractContainerScreen<WandChargerMenu> {
   private static final ResourceLocation TEXTURE = Thaumcraft.location("textures/screen/wand_charger.png");
   private static final DecimalFormat VIS_FORMAT = new DecimalFormat("0.##");
   private static final int[][] BAR_POSITIONS = new int[][]{{8, 10}, {24, 10}, {40, 10}, {8, 51}, {24, 51}, {40, 51}};
   private static final int BAR_WIDTH = 8;
   private static final int BAR_HEIGHT = 29;

   public WandChargerScreen(WandChargerMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 176;
      this.imageHeight = 186;
   }

   public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      super.renderBackground(graphics, mouseX, mouseY, partialTick);
      super.render(graphics, mouseX, mouseY, partialTick);
      List<Component> tooltip = this.getAspectTooltip(mouseX, mouseY);
      if (!tooltip.isEmpty()) {
         graphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
      }

      this.renderTooltip(graphics, mouseX, mouseY);
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
      RenderSystem.enableBlend();
      graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.blit(TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
      int maxVis = ((WandChargerMenu)this.menu).getMaxVis();

      for (int index = 0; index < WandChargerBlockEntity.getChargeAspects().size(); index++) {
         int[] pos = BAR_POSITIONS[index];
         float ratio = maxVis <= 0 ? 0.0F : (float)((WandChargerMenu)this.menu).getStoredVis(index) / maxVis;
         this.drawAspectBar(graphics, index, pos[0], pos[1], ratio);
      }

      RenderSystem.disableBlend();
   }

   protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
      graphics.drawString(this.font, this.title, (this.imageWidth - this.font.width(this.title)) / 2, 2, 4210752, false);
      double percent = ((WandChargerMenu)this.menu).getWandChargePercent();
      if (percent >= 0.0) {
         Component percentText = Component.literal(VIS_FORMAT.format(percent) + " %").withStyle(ChatFormatting.GREEN);
         graphics.drawString(this.font, percentText, (this.imageWidth - this.font.width(percentText)) / 2, 78, 43520, false);
      }
   }

   private void drawAspectBar(GuiGraphics graphics, int aspectIndex, int x, int y, float ratio) {
      int filled = Mth.clamp(Math.round(29.0F * ratio), 0, 29);
      if (filled > 0) {
         int u = 176 + aspectIndex * 8;
         int v = 29 - filled;
         graphics.blit(TEXTURE, this.leftPos + x, this.topPos + y + v, u, v, 8, filled, 256, 256);
      }
   }

   private List<Component> getAspectTooltip(int mouseX, int mouseY) {
      int relativeX = mouseX - this.leftPos;
      int relativeY = mouseY - this.topPos;

      for (int index = 0; index < WandChargerBlockEntity.getChargeAspects().size(); index++) {
         int[] pos = BAR_POSITIONS[index];
         if (relativeX >= pos[0] && relativeX < pos[0] + 8 && relativeY >= pos[1] && relativeY < pos[1] + 29) {
            Aspect aspect = WandChargerBlockEntity.getChargeAspects().get(index);
            Component line = Component.literal(
                  VIS_FORMAT.format(((WandChargerMenu)this.menu).getStoredVis(index) / 100.0)
                     + " / "
                     + VIS_FORMAT.format(((WandChargerMenu)this.menu).getMaxVis() / 100.0)
                     + " "
               )
               .append(Component.literal(aspect.getStringName()).withStyle(style -> style.withColor(aspect.getColor())));
            return List.of(line);
         }
      }

      return List.of();
   }
}
