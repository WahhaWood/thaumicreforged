package thaumcraft.block.thaumatorium;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.util.UtilsFX;

@OnlyIn(Dist.CLIENT)
public class ThaumatoriumScreen extends AbstractContainerScreen<ThaumatoriumMenu> {
   private static final int MAX_VISIBLE_ASPECTS = 6;
   private final ResourceLocation texture = Thaumcraft.location("textures/screen/thaumatorium.png");
   private int index = 0;
   private int lastSize = 0;
   private int startAspect = 0;

   public ThaumatoriumScreen(ThaumatoriumMenu menu, Inventory inventory, Component component) {
      super(menu, inventory, component);
      ((ThaumatoriumMenu)this.menu).updateRecipes();
      this.lastSize = ((ThaumatoriumMenu)this.menu).getRecipeCount();
      this.refreshSelectedIndex();
   }

   private void refreshSelectedIndex() {
      if (((ThaumatoriumMenu)this.menu).hasRecipes()) {
         for (int i = 0; i < ((ThaumatoriumMenu)this.menu).getRecipeCount(); i++) {
            if (((ThaumatoriumMenu)this.menu).entity.isRecipeSelected(((ThaumatoriumMenu)this.menu).getRecipeId(i))) {
               this.index = i;
               break;
            }
         }
      }

      this.startAspect = 0;
   }

   private void clampIndices() {
      if (!((ThaumatoriumMenu)this.menu).hasRecipes()) {
         this.index = 0;
         this.startAspect = 0;
      } else {
         this.index = Mth.clamp(this.index, 0, ((ThaumatoriumMenu)this.menu).getRecipeCount() - 1);
         int maxStart = Math.max(0, this.getCurrentRecipe().aspects.size() - 6);
         this.startAspect = Mth.clamp(this.startAspect, 0, maxStart);
      }
   }

   private CrucibleRecipe getCurrentRecipe() {
      return ((ThaumatoriumMenu)this.menu).getRecipe(this.index);
   }

   private boolean isOutputHovered(double mouseX, double mouseY) {
      double relX = mouseX - (this.leftPos + 112);
      double relY = mouseY - (this.topPos + 16);
      return relX >= 0.0 && relY >= 0.0 && relX < 16.0 && relY < 16.0;
   }

   public void render(GuiGraphics graphics, int mx, int my, float pt) {
      super.render(graphics, mx, my, pt);
      this.renderTooltip(graphics, mx, my);
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float pt, int mx, int my) {
      PoseStack pose = graphics.pose();
      int x = this.leftPos;
      int y = this.topPos;
      RenderSystem.enableBlend();
      graphics.blit(this.texture, x, y, 0, 0, this.imageWidth, this.imageHeight);
      if (((ThaumatoriumMenu)this.menu).hasRecipes()) {
         if (this.lastSize != ((ThaumatoriumMenu)this.menu).getRecipeCount()) {
            this.lastSize = ((ThaumatoriumMenu)this.menu).getRecipeCount();
            this.refreshSelectedIndex();
         }

         this.clampIndices();
         CrucibleRecipe currentRecipe = this.getCurrentRecipe();
         if (((ThaumatoriumMenu)this.menu).getRecipeCount() > 1) {
            if (this.index > 0) {
               graphics.blit(this.texture, x + 128, y + 16, 192, 16, 16, 8);
            } else {
               graphics.blit(this.texture, x + 128, y + 16, 176, 16, 16, 8);
            }

            if (this.index < ((ThaumatoriumMenu)this.menu).getRecipeCount() - 1) {
               graphics.blit(this.texture, x + 128, y + 24, 192, 24, 16, 8);
            } else {
               graphics.blit(this.texture, x + 128, y + 24, 176, 24, 16, 8);
            }
         }

         if (currentRecipe.aspects.size() > 6) {
            if (this.startAspect > 0) {
               graphics.blit(this.texture, x + 32, y + 40, 192, 32, 8, 16);
            } else {
               graphics.blit(this.texture, x + 32, y + 40, 176, 32, 8, 16);
            }

            if (this.startAspect < currentRecipe.aspects.size() - 6) {
               graphics.blit(this.texture, x + 136, y + 40, 200, 32, 8, 16);
            } else {
               graphics.blit(this.texture, x + 136, y + 40, 184, 32, 8, 16);
            }
         } else {
            this.startAspect = 0;
         }

         if (((ThaumatoriumMenu)this.menu).entity.hasSelectedRecipes()) {
            if (this.isOutputHovered(mx, my) || ((ThaumatoriumMenu)this.menu).entity.isRecipeSelected(((ThaumatoriumMenu)this.menu).getRecipeId(this.index))) {
               pose.pushPose();
               RenderSystem.enableBlend();
               graphics.blit(this.texture, x + 104, y + 8, 176, 96, 48, 48);
               RenderSystem.disableBlend();
               pose.popPose();
            }

            pose.pushPose();
            RenderSystem.enableBlend();
            graphics.setColor(1.0F, 1.0F, 1.0F, 0.6F + Mth.sin(this.minecraft.player.tickCount / 5.0F) * 0.4F + 0.4F);
            graphics.blit(this.texture, x + 88, y + 16, 176, 56, 24, 24);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            pose.popPose();
         }

         this.drawAspects(graphics, x, y);
         this.drawOutput(graphics, x, y, mx, my);
         if (((ThaumatoriumMenu)this.menu).entity.getMaxRecipes() > 1) {
            pose.pushPose();
            pose.translate(x + 136, y + 33, 0.0F);
            pose.scale(0.5F, 0.5F, 0.0F);
            String string = ((ThaumatoriumMenu)this.menu).entity.getSelectedRecipeCount() + "/" + ((ThaumatoriumMenu)this.menu).entity.getMaxRecipes();
            graphics.drawString(this.font, string, -(this.font.width(string) / 2), 0, 16777215);
            pose.popPose();
         }
      }

      RenderSystem.disableBlend();
   }

   private void drawAspects(GuiGraphics graphics, int n, int n2) {
      PoseStack pose = graphics.pose();
      int visibleIndex = 0;
      int aspectIndex = 0;
      CrucibleRecipe recipe = this.getCurrentRecipe();
      if (((ThaumatoriumMenu)this.menu).entity.isRecipeSelected(((ThaumatoriumMenu)this.menu).getRecipeId(this.index))) {
         for (Aspect aspect : recipe.aspects.getAspectsSorted()) {
            if (aspectIndex >= this.startAspect) {
               pose.pushPose();
               graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
               graphics.blit(this.texture, n + 41 + 16 * visibleIndex, n2 + 57, 176, 8, 14, 6);
               int progress = (int)((float)((ThaumatoriumMenu)this.menu).entity.containerContains(aspect) / recipe.aspects.getAmount(aspect) * 12.0F);
               Color color = new Color(aspect.getColor());
               graphics.setColor(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, 1.0F);
               graphics.blit(this.texture, n + 42 + 16 * visibleIndex, n2 + 58, 176, 0, progress, 4);
               pose.popPose();
               visibleIndex++;
            }

            if (++aspectIndex >= 6 + this.startAspect) {
               break;
            }
         }
      }

      visibleIndex = 0;
      aspectIndex = 0;

      for (Aspect aspect : recipe.aspects.getAspectsSorted()) {
         if (aspectIndex >= this.startAspect) {
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            UtilsFX.drawTag(graphics, n + 40 + 16 * visibleIndex, n2 + 40, aspect, recipe.aspects.getAmount(aspect), 0, 0.0F);
            visibleIndex++;
         }

         if (++aspectIndex >= 6 + this.startAspect) {
            break;
         }
      }
   }

   private void drawOutput(GuiGraphics graphics, int n, int n2, int n3, int n4) {
      PoseStack pose = graphics.pose();
      pose.pushPose();
      CrucibleRecipe recipe = this.getCurrentRecipe();
      if (((ThaumatoriumMenu)this.menu).entity.getSelectedRecipeCount() < ((ThaumatoriumMenu)this.menu).entity.getMaxRecipes()
         || ((ThaumatoriumMenu)this.menu).entity.isRecipeSelected(((ThaumatoriumMenu)this.menu).getRecipeId(this.index))) {
         graphics.setColor(0.5F, 0.5F, 0.5F, 0.3F + Mth.sin(this.minecraft.player.tickCount / 4.0F) * 0.3F + 0.3F);
      }

      RenderSystem.enableBlend();
      ItemStack stack = recipe.getResultItem();
      graphics.renderItem(stack, n + 112, n2 + 16);
      graphics.renderItemDecorations(this.font, stack, n + 112, n2 + 16);
      graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      if (this.isOutputHovered(n3, n4)) {
         graphics.renderTooltip(this.font, stack, n3, n4);
      }

      RenderSystem.disableBlend();
      pose.popPose();
   }

   public boolean mouseClicked(double mx, double my, int button) {
      if (((ThaumatoriumMenu)this.menu).hasRecipes()) {
         this.clampIndices();
         if (this.isOutputHovered(mx, my)) {
            this.minecraft.gameMode.handleInventoryButtonClick(((ThaumatoriumMenu)this.menu).containerId, this.index);
            this.playButtonSelect();
            return true;
         }

         if (((ThaumatoriumMenu)this.menu).getRecipeCount() > 1) {
            if (this.index > 0) {
               double relX = mx - (this.leftPos + 128);
               double relY = my - (this.topPos + 16);
               if (relX >= 0.0 && relY >= 0.0 && relX < 16.0 && relY < 8.0) {
                  this.index--;
                  this.startAspect = 0;
                  this.playButtonClick();
                  return true;
               }
            }

            if (this.index < ((ThaumatoriumMenu)this.menu).getRecipeCount() - 1) {
               double relX = mx - (this.leftPos + 128);
               double relY = my - (this.topPos + 24);
               if (relX >= 0.0 && relY >= 0.0 && relX < 16.0 && relY < 8.0) {
                  this.index++;
                  this.startAspect = 0;
                  this.playButtonClick();
                  return true;
               }
            }
         }

         if (this.getCurrentRecipe().aspects.size() > 6) {
            if (this.startAspect > 0) {
               double relX = mx - (this.leftPos + 32);
               double relY = my - (this.topPos + 40);
               if (relX >= 0.0 && relY >= 0.0 && relX < 8.0 && relY < 16.0) {
                  this.startAspect--;
                  this.playButtonClick();
                  return true;
               }
            }

            if (this.startAspect < this.getCurrentRecipe().aspects.size() - 6) {
               double relX = mx - (this.leftPos + 136);
               double relY = my - (this.topPos + 40);
               if (relX >= 0.0 && relY >= 0.0 && relX < 8.0 && relY < 16.0) {
                  this.startAspect++;
                  this.playButtonClick();
                  return true;
               }
            }
         }
      }

      return super.mouseClicked(mx, my, button);
   }

   private void playButtonSelect() {
      Entity entity = this.minecraft.cameraEntity;
      entity.level().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), (SoundEvent)ModSounds.HHON.get(), SoundSource.BLOCKS, 0.3F, 1.0F, false);
   }

   private void playButtonClick() {
      Entity entity = this.minecraft.cameraEntity;
      entity.level()
         .playLocalSound(entity.getX(), entity.getY(), entity.getZ(), (SoundEvent)ModSounds.CAMERA_CLACK.get(), SoundSource.BLOCKS, 0.4F, 1.0F, false);
   }
}
