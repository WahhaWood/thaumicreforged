package thaumicenergistics.client.screen;

import appeng.client.gui.Icon;
import com.mojang.blaze3d.systems.RenderSystem;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.menu.InfusionInscriberMenu;
import thaumicenergistics.network.InfusionInscriberActionPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class InfusionInscriberScreen extends AbstractContainerScreen<InfusionInscriberMenu> {
   private static final int GUI_WIDTH = 208;
   private static final int GUI_HEIGHT = 256;
   private static final int BUTTON_SAVE_POS_X = 155;
   private static final int BUTTON_SAVE_POS_Y = 45;
   private static final int BUTTON_CLEAR_POS_X = 98;
   private static final int BUTTON_CLEAR_POS_Y = 8;
   private static final ResourceLocation GUI_TEXTURE = ThaumicEnergistics.location("textures/screen/infusion_inscriber.png");
   private static final ResourceLocation RESEARCH_TEXTURE = ThaumicEnergistics.location("textures/research/research_background.png");
   private InfusionInscriberMenu.SaveState saveState = InfusionInscriberMenu.SaveState.DISABLED_MISSING_CORE;
   private InscriberIconButton buttonSave;
   private InscriberIconButton buttonClear;
   private boolean justSaved = false;
   private long justSavedTime = 0L;

   public InfusionInscriberScreen(InfusionInscriberMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 208;
      this.imageHeight = 256;
      this.inventoryLabelY = 163;
   }

   protected void init() {
      super.init();
      this.buttonSave = new InscriberIconButton(
         this.leftPos + 155, this.topPos + 45, 16, 16, null, false, button -> InfusionInscriberActionPacket.sendSaveDelete()
      );
      this.addRenderableWidget(this.buttonSave);
      this.updateSaveButton();
      this.buttonClear = new InscriberIconButton(
         this.leftPos + 98, this.topPos + 8, 8, 8, Icon.CLEAR, true, button -> InfusionInscriberActionPacket.sendClearGrid()
      );
      this.addRenderableWidget(this.buttonClear);
      InfusionInscriberActionPacket.sendFullUpdateRequest();
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
      long slowTime = System.currentTimeMillis() / 100L;
      float redBounce = Math.abs(1.0F - (float)(slowTime % 50L) / 50.0F * 2.0F);
      float greenBounce = 1.0F - redBounce;
      RenderSystem.setShaderColor(redBounce, greenBounce, 1.0F, 1.0F);
      int xpos = (int)(slowTime % 10L);
      graphics.blit(RESEARCH_TEXTURE, this.leftPos + 80, this.topPos + 106, 55 - xpos, 25, 35, 20);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.blit(GUI_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
   }

   protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
      if (this.justSaved) {
         long elapsed = System.currentTimeMillis() - this.justSavedTime;
         if (elapsed < 1000L) {
            float alpha = 1.0F - (float)elapsed / 1000.0F;
            int color = (int)(alpha * 255.0F) << 24 | 3377407;
            graphics.fill(182, 6, 202, 26, color);
         } else {
            this.justSaved = false;
         }
      }

      // no title: the slot grid starts at the top edge of the window
      graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, ScreenText.TITLE_COLOR, false);
   }

   public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(graphics, mouseX, mouseY, partialTick);
      super.render(graphics, mouseX, mouseY, partialTick);
      this.renderTooltip(graphics, mouseX, mouseY);
      if (this.buttonSave != null && this.buttonSave.isMouseOver((double)mouseX, (double)mouseY)) {
         Component tooltip = this.getSaveStateTooltip();
         if (tooltip != null) {
            graphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
         }
      }
   }

   public void onReceiveSaveState(InfusionInscriberMenu.SaveState saveState, boolean justSaved) {
      this.saveState = saveState;
      this.updateSaveButton();
      if (justSaved) {
         this.justSaved = true;
         this.justSavedTime = System.currentTimeMillis();
      }
   }

   private void updateSaveButton() {
      if (this.buttonSave != null) {
         switch (this.saveState) {
            case ENABLED_SAVE:
               this.buttonSave.active = true;
               this.buttonSave.setIcon(Icon.ACCESS_WRITE);
               break;
            case ENABLED_DELETE:
               this.buttonSave.active = true;
               this.buttonSave.setIcon(Icon.CONDENSER_OUTPUT_TRASH);
               break;
            default:
               this.buttonSave.active = false;
               this.buttonSave.setIcon(null);
         }
      }
   }

   private Component getSaveStateTooltip() {
      return switch (this.saveState) {
         case ENABLED_SAVE -> Component.translatable("tooltip.thaumicenergistics.pattern.save");
         case ENABLED_DELETE -> Component.translatable("tooltip.thaumicenergistics.pattern.delete");
         case DISABLED_INVALID_RECIPE -> Component.translatable("tooltip.thaumicenergistics.pattern.invalid_recipe");
         case DISABLED_CORE_FULL -> Component.translatable("tooltip.thaumicenergistics.pattern.core_full");
         case DISABLED_MISSING_CORE -> Component.translatable("tooltip.thaumicenergistics.pattern.no_core.brain");
         case DISABLED_RECIPE -> Component.translatable("tooltip.thaumicenergistics.pattern.recipe_unsupported");
      };
   }
}
