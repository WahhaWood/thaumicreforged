package thaumicenergistics.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.block.arcane_assembler.ArcaneAssemblerBlockEntity;
import thaumicenergistics.menu.ArcaneAssemblerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

@OnlyIn(Dist.CLIENT)
public class ArcaneAssemblerScreen extends RecipeAssemblerScreen<ArcaneAssemblerMenu> {
   private static final int MAIN_GUI_WIDTH = 176;
   private static final int UPGRADE_GUI_WIDTH = 58;
   private static final int GUI_HEIGHT = 197;
   private static final int UPGRADE_GUI_HEIGHT = 104;
   private static final int TITLE_POS_X = 6;
   private static final int TITLE_POS_Y = 6;
   private static final int VIS_EMPTY_Y = 87;
   private static final int VIS_FULL_Y = 202;
   private static final int VIS_BAR_HEIGHT = 16;
   private static final int VIS_BAR_WIDTH = 4;
   private static final ResourceLocation GUI_TEXTURE = ThaumicEnergistics.location("textures/screen/infusion_assembler.png");
   private final Map<Aspect, Integer> visBarPositionMap = new HashMap<>();
   private final Aspect progressAspect = Aspect.MECHANISM;

   public ArcaneAssemblerScreen(ArcaneAssemblerMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title, new RecipeGridPanel.Layout(26, 25, 7, 3, 232));
      this.imageWidth = 234;
      this.imageHeight = 197;
      this.visBarPositionMap.put(Aspect.AIR, 41);
      this.visBarPositionMap.put(Aspect.WATER, 59);
      this.visBarPositionMap.put(Aspect.FIRE, 77);
      this.visBarPositionMap.put(Aspect.ORDER, 95);
      this.visBarPositionMap.put(Aspect.ENTROPY, 113);
      this.visBarPositionMap.put(Aspect.EARTH, 131);
      this.visBarPositionMap.put(this.progressAspect, 149);
   }

   private void drawVisBar(GuiGraphics graphics, Aspect aspect, float percent) {
      int height = (int)Math.floor((double)(percent * 16.0F));
      if (height > 0) {
         Integer xuPos = this.visBarPositionMap.get(aspect);
         if (xuPos != null) {
            int yPos = 87 + (16 - height);
            int vPos = 202 + (16 - height);
            graphics.blit(GUI_TEXTURE, this.leftPos + xuPos, this.topPos + yPos, xuPos, vPos, 4, height);
         }
      }
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.blit(GUI_TEXTURE, this.leftPos, this.topPos, 0, 0, 176, this.imageHeight);
      graphics.blit(GUI_TEXTURE, this.leftPos + 176, this.topPos, 176, 0, 58, 104);
      // the knowledge core slot is gone: paint the panel over its art
      graphics.fill(this.leftPos + 186, this.topPos + 7, this.leftPos + 204, this.topPos + 25, 0xFFC6C6C6);
      this.renderRecipes(graphics);
      ArcaneAssemblerBlockEntity assembler = ((ArcaneAssemblerMenu)this.menu).assembler;
      AspectList storedVis = assembler.getStoredVis();

      for (Aspect aspect : ArcaneAssemblerBlockEntity.PRIMALS) {
         float percent = (float)storedVis.getAmount(aspect) / (float)assembler.getMaxStoredCvis();
         this.drawVisBar(graphics, aspect, percent);
      }

      this.drawVisBar(graphics, this.progressAspect, assembler.getPercentComplete());
   }

   protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
      graphics.drawString(this.font, ScreenText.fitTitle(this.font, this.title, MAIN_GUI_WIDTH - 2 * TITLE_POS_X), TITLE_POS_X, TITLE_POS_Y, ScreenText.TITLE_COLOR, false);
   }

   @Override
   protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
      this.renderVisTooltips(graphics, mouseX, mouseY);
   }

   private void renderVisTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
      ArcaneAssemblerBlockEntity assembler = ((ArcaneAssemblerMenu)this.menu).assembler;

      for (Aspect aspect : ArcaneAssemblerBlockEntity.PRIMALS) {
         Integer left = this.visBarPositionMap.get(aspect);
         if (left != null && this.isHovering(left, 87, 4, 16, (double)mouseX, (double)mouseY)) {
            int amountCvis = assembler.getStoredVis().getAmount(aspect);
            float amount = (float)amountCvis / 10.0F;
            int maxVis = assembler.getMaxStoredCvis() / 10;
            float discount = assembler.getVisDiscountForAspect(aspect);
            String aspectName = capitalize(aspect.getTag());
            List<Component> tooltip = List.of(
               Component.literal(aspectName).withStyle(style -> style.withColor(TextColor.fromRgb(aspect.getColor()))),
               Component.literal(amount + " / " + maxVis),
               Component.literal(String.format("%.0f%% ", discount * 100.0F)).append(Component.translatable("tc.vis.costavg"))
            );
            graphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
            break;
         }
      }
   }

   private static String capitalize(String value) {
      if (value != null && !value.isEmpty()) {
         return value.length() == 1 ? value.toUpperCase(Locale.ROOT) : value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
      } else {
         return value;
      }
   }
}
