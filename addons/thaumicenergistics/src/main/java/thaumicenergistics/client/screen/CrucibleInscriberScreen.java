package thaumicenergistics.client.screen;

import appeng.client.gui.Icon;
import com.mojang.blaze3d.systems.RenderSystem;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.menu.CrucibleInscriberMenu;
import thaumicenergistics.network.CrucibleInscriberActionPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class CrucibleInscriberScreen extends AbstractContainerScreen<CrucibleInscriberMenu> {
   private static final int GUI_WIDTH = 219;
   private static final int GUI_HEIGHT = 198;
   private static final ResourceLocation GUI_TEXTURE = ThaumicEnergistics.location("textures/screen/crucible_inscriber.png");
   private CrucibleInscriberMenu.SaveState saveState = CrucibleInscriberMenu.SaveState.DISABLED_MISSING_CORE;
   private boolean canBack = false;
   private boolean canNext = false;
   private InscriberIconButton buttonSave;
   private Button buttonBack;
   private Button buttonNext;
   private boolean justSaved = false;
   private long justSavedTime = 0L;

   public CrucibleInscriberScreen(CrucibleInscriberMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 219;
      this.imageHeight = 198;
      this.inventoryLabelY = 109;
   }

   protected void init() {
      super.init();
      this.buttonSave = new InscriberIconButton(
         this.leftPos + 69, this.topPos + 6, 8, 8, null, true, button -> CrucibleInscriberActionPacket.sendSaveDelete()
      );
      this.addRenderableWidget(this.buttonSave);
      this.updateSaveButton();
      this.buttonBack = Button.builder(Component.literal("<"), button -> CrucibleInscriberActionPacket.sendScrollBack())
         .bounds(this.leftPos + 101, this.topPos + 16, 7, 7)
         .build();
      this.addRenderableWidget(this.buttonBack);
      this.buttonNext = Button.builder(Component.literal(">"), button -> CrucibleInscriberActionPacket.sendScrollNext())
         .bounds(this.leftPos + 101, this.topPos + 25, 7, 7)
         .build();
      this.addRenderableWidget(this.buttonNext);
      CrucibleInscriberActionPacket.sendFullUpdateRequest();
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.blit(GUI_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
   }

   protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
      if (this.justSaved) {
         long elapsed = System.currentTimeMillis() - this.justSavedTime;
         if (elapsed < 1000L) {
            float alpha = 1.0F - (float)elapsed / 1000.0F;
            int color = (int)(alpha * 255.0F) << 24 | 3377407;
            graphics.fill(3, 48, 23, 68, color);
         } else {
            this.justSaved = false;
         }
      }

      // no title: the strip above the slots is too short for a translated name
      graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, ScreenText.TITLE_COLOR, false);
   }

   public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(graphics, mouseX, mouseY, partialTick);
      super.render(graphics, mouseX, mouseY, partialTick);
      this.renderTooltip(graphics, mouseX, mouseY);
      if (this.buttonSave != null && this.buttonSave.isHoveredOrFocused()) {
         Component tooltip = this.getSaveStateTooltip();
         if (tooltip != null) {
            graphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
         }
      }

      if (this.buttonBack != null && this.buttonBack.isHoveredOrFocused()) {
         graphics.renderTooltip(this.font, Component.translatable("info.thaumicenergistics.pattern.select"), mouseX, mouseY);
      }

      if (this.buttonNext != null && this.buttonNext.isHoveredOrFocused()) {
         graphics.renderTooltip(this.font, Component.translatable("info.thaumicenergistics.pattern.select"), mouseX, mouseY);
      }
   }

   public void onReceiveSaveState(CrucibleInscriberMenu.SaveState saveState, boolean justSaved, boolean canBack, boolean canNext) {
      this.saveState = saveState;
      this.canBack = canBack;
      this.canNext = canNext;
      this.updateSaveButton();
      this.updateScrollButtons();
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

   private void updateScrollButtons() {
      if (this.buttonBack != null) {
         this.buttonBack.active = this.canBack;
      }

      if (this.buttonNext != null) {
         this.buttonNext.active = this.canNext;
      }
   }

   private Component getSaveStateTooltip() {
      return switch (this.saveState) {
         case ENABLED_SAVE -> Component.translatable("tooltip.thaumicenergistics.pattern.encode");
         case ENABLED_DELETE -> Component.translatable("tooltip.thaumicenergistics.pattern.delete");
         case DISABLED_INVALID_RECIPE -> Component.translatable("tooltip.thaumicenergistics.pattern.invalid_recipe");
         case DISABLED_CORE_FULL -> Component.translatable("tooltip.thaumicenergistics.pattern.core_full");
         case DISABLED_MISSING_CORE -> Component.translatable("tooltip.thaumicenergistics.pattern.no_core.crucible");
      };
   }
}
