package thaumicenergistics.client.screen;

import appeng.client.gui.Icon;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.menu.KnowledgeInscriberMenu;
import thaumicenergistics.network.KnowledgeInscriberActionPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class KnowledgeInscriberScreen extends AbstractContainerScreen<KnowledgeInscriberMenu> {
   private static final int GUI_WIDTH = 210;
   private static final int GUI_HEIGHT = 244;
   private static final int BUTTON_SAVE_POS_X = 141;
   private static final int BUTTON_SAVE_POS_Y = 109;
   private static final int BUTTON_CLEAR_POS_X = 80;
   private static final int BUTTON_CLEAR_POS_Y = 89;
   private static final ResourceLocation GUI_TEXTURE = ThaumicEnergistics.location("textures/screen/knowledge_inscriber.png");
   private static final ResourceLocation RESEARCH_TEXTURE = ThaumicEnergistics.location("textures/research/research_background.png");
   private KnowledgeInscriberMenu.SaveState saveState = KnowledgeInscriberMenu.SaveState.DISABLED_MISSING_CORE;
   /** The strip above the pattern grid, up to the core slot tab. */
   private static final int TITLE_WIDTH = 160;
   private InscriberIconButton buttonSave;
   private InscriberIconButton buttonClear;
   private final List<KnowledgeInscriberScreen.SaveParticle> saveParticles = new ArrayList<>();

   public KnowledgeInscriberScreen(KnowledgeInscriberMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 210;
      this.imageHeight = 244;
      this.inventoryLabelY = 151;
   }

   protected void init() {
      super.init();
      this.buttonSave = new InscriberIconButton(
         this.leftPos + 141, this.topPos + 109, 16, 16, null, false, button -> KnowledgeInscriberActionPacket.sendSaveDelete()
      );
      this.addRenderableWidget(this.buttonSave);
      this.updateSaveButton();
      this.buttonClear = new InscriberIconButton(
         this.leftPos + 80, this.topPos + 89, 8, 8, Icon.CLEAR, true, button -> KnowledgeInscriberActionPacket.sendClearGrid()
      );
      this.addRenderableWidget(this.buttonClear);
      KnowledgeInscriberActionPacket.sendFullUpdateRequest();
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
      graphics.drawString(
         this.font, ScreenText.fitTitle(this.font, this.title, TITLE_WIDTH), this.titleLabelX, this.titleLabelY, ScreenText.TITLE_COLOR, false
      );
      graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, ScreenText.TITLE_COLOR, false);
      this.saveParticles.removeIf(particle -> particle.render(graphics));
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

   public void onReceiveSaveState(KnowledgeInscriberMenu.SaveState saveState, boolean justSaved) {
      this.saveState = saveState;
      this.updateSaveButton();
      if (justSaved) {
         this.saveParticles.clear();

         for (int i = 0; i < 9; i++) {
            int col = i % 3;
            int row = i / 3;
            float startX = (float)(26 + col * 18 + 8);
            float startY = (float)(90 + row * 18 + 8);
            float targetX = 194.0F;
            float targetY = 16.0F;
            this.saveParticles.add(new KnowledgeInscriberScreen.SaveParticle(startX, startY, targetX, targetY, (long)i * 50L));
         }
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
         case DISABLED_MISSING_CORE -> Component.translatable("tooltip.thaumicenergistics.pattern.no_core.knowledge");
         case DISABLED_RECIPE -> Component.translatable("tooltip.thaumicenergistics.pattern.recipe_unsupported");
      };
   }

   public static class SaveParticle {
      private final float startX;
      private final float startY;
      private final float targetX;
      private final float targetY;
      private final long startTime;
      private final long delay;
      private boolean finished = false;

      public SaveParticle(float startX, float startY, float targetX, float targetY, long delay) {
         this.startX = startX;
         this.startY = startY;
         this.targetX = targetX;
         this.targetY = targetY;
         this.delay = delay;
         this.startTime = System.currentTimeMillis();
      }

      public boolean render(GuiGraphics graphics) {
         if (this.finished) {
            return true;
         } else {
            long elapsed = System.currentTimeMillis() - this.startTime - this.delay;
            if (elapsed < 0L) {
               return false;
            } else {
               float progress = Math.min(1.0F, (float)elapsed / 400.0F);
               float currentX = this.startX + (this.targetX - this.startX) * progress;
               float currentY = this.startY + (this.targetY - this.startY) * progress;
               int particleColor = -1439465217;
               graphics.fill((int)currentX - 2, (int)currentY - 2, (int)currentX + 2, (int)currentY + 2, particleColor);
               if (progress >= 1.0F) {
                  this.finished = true;
                  return true;
               } else {
                  return false;
               }
            }
         }
      }
   }
}
