package thaumcraft.entities.smart.golem.render.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import thaumcraft.entities.smart.golem.EntityGolemBase;
import thaumcraft.entities.smart.golem.GolemMenu;
import thaumcraft.item.golem_core.GolemCore;
import thaumcraft.item.golem_upgrade.GolemUpgrade;
import thaumcraft.util.Utils;

@OnlyIn(Dist.CLIENT)
public class GolemScreen extends AbstractContainerScreen<GolemMenu> {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/screen/golem_screen.png");
   private final EntityGolemBase golem;
   private float xSize_lo;
   private float ySize_lo;
   private int threat = -1;

   public GolemScreen(GolemMenu menu, Inventory inv, Component title) {
      super(menu, inv, title);
      this.golem = menu.golem;
      this.imageWidth = 176;
      this.imageHeight = 166;
      if (this.golem.advanced && this.golem.level().random.nextInt(4) == 0) {
         this.threat = this.golem.level().random.nextInt(9);
      }
   }

   protected void renderLabels(GuiGraphics gg, int mouseX, int mouseY) {
      gg.pose().pushPose();
      gg.pose().scale(0.5F, 0.5F, 1.0F);
      Component blurb = this.threat >= 0
         ? Component.translatable("golemthreat." + this.threat + ".text")
         : Component.translatable("golemblurb." + this.golem.getCore().getId() + ".text");
      int y = 22;

      for (FormattedCharSequence line : this.font.split(blurb, 110)) {
         gg.drawString(this.font, line, 40, y, 14540253, false);
         y += 9;
      }

      if (((GolemMenu)this.menu).maxScroll > 0) {
         String page = ((GolemMenu)this.menu).currentScroll + 1 + "/" + (((GolemMenu)this.menu).maxScroll + 1);
         gg.drawString(this.font, page, 323, 140, 14540253, false);
      }

      gg.pose().popPose();
   }

   protected void renderBg(GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
      int baseX = this.leftPos;
      int baseY = this.topPos;
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      gg.blit(TEX, baseX, baseY, 0, 0, this.imageWidth, this.imageHeight);
      int slots = this.golem.getInventory().getSlotCount();
      int typeLoc = this.golem.getGolemType().ordinal() * 24;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      if (this.golem.getCore().hasInventory()) {
         for (int a = 0; a < Math.min(6, slots); a++) {
            gg.blit(TEX, baseX + 96 + a / 2 * 28, baseY + 12 + a % 2 * 31, 184, typeLoc, 24, 24);
            if (this.golem.getUpgradeAmount(GolemUpgrade.ORDER.ordinal()) > 0) {
               gg.blit(TEX, baseX + 96 + a / 2 * 28, baseY + 4 + a % 2 * 31, 72, 168, 24, 12);
               int idx = a + ((GolemMenu)this.menu).currentScroll * 6;
               int color = this.golem.getColors(idx);
               if (color > -1) {
                  Color c = new Color(Utils.colors[color]);
                  RenderSystem.setShaderColor(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, 1.0F);
                  gg.blit(TEX, baseX + 105 + a / 2 * 28, baseY + 7 + a % 2 * 31, 0, 176, 6, 6);
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               }
            }
         }
      }

      if (this.golem.getCore() == GolemCore.LIQUID) {
         for (int ax = 0; ax < Math.min(6, slots); ax++) {
            ItemStack stack = this.golem.getInventory().getStackInSlot(ax + ((GolemMenu)this.menu).currentScroll * 6);
            FluidStack fluid = (FluidStack)FluidUtil.getFluidContained(stack).orElse(null);
            if (fluid != null) {
               IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
               ResourceLocation still = ext.getStillTexture();
               if (still != null) {
                  TextureAtlasSprite sprite = (TextureAtlasSprite)this.minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
                  if (fluid.getFluid() == Fluids.WATER) {
                     RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 0.33F);
                  } else {
                     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.33F);
                  }

                  int cx = baseX + 108 + ax / 2 * 28;
                  int cy = baseY + 24 + ax % 2 * 31;
                  gg.blit(cx - 8, cy - 8, 0, 16, 16, sprite);
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               }
            }
         }
      }

      if (slots > 6) {
         if (((GolemMenu)this.menu).currentScroll > 0) {
            gg.blit(TEX, baseX + 111, baseY + 68, 0, 200, 24, 8);
         } else {
            gg.blit(TEX, baseX + 111, baseY + 68, 0, 208, 24, 8);
         }

         if (((GolemMenu)this.menu).currentScroll < ((GolemMenu)this.menu).maxScroll) {
            gg.blit(TEX, baseX + 135, baseY + 68, 24, 200, 24, 8);
         } else {
            gg.blit(TEX, baseX + 135, baseY + 68, 24, 208, 24, 8);
         }
      }

      if (this.golem.getCore() == GolemCore.GUARD && this.golem.getUpgradeAmount(GolemUpgrade.ORDER.ordinal()) > 0) {
         gg.blit(TEX, baseX + 104, baseY + 5, 8, 168, 8, 8);
         gg.blit(TEX, baseX + 104, baseY + 21, 8, 168, 8, 8);
         gg.blit(TEX, baseX + 104, baseY + 37, 8, 168, 8, 8);
         gg.blit(TEX, baseX + 104, baseY + 53, 8, 168, 8, 8);
         if (this.golem.canAttackHostiles()) {
            gg.blit(TEX, baseX + 104, baseY + 5, 8, 176, 8, 8);
         }

         if (this.golem.canAttackAnimals()) {
            gg.blit(TEX, baseX + 104, baseY + 21, 8, 176, 8, 8);
         }

         if (this.golem.canAttackPlayers()) {
            gg.blit(TEX, baseX + 104, baseY + 37, 8, 176, 8, 8);
         }

         if (this.golem.canAttackCreepers()) {
            gg.blit(TEX, baseX + 104, baseY + 53, 8, 176, 8, 8);
         }

         gg.drawString(this.font, Component.translatable("tc.golems.gui.buttons.monsters").getString(), baseX + 122, baseY + 6, 16759068, false);
         gg.drawString(this.font, Component.translatable("tc.golems.gui.buttons.animals").getString(), baseX + 122, baseY + 22, 16774828, false);
         gg.drawString(this.font, Component.translatable("tc.golems.gui.buttons.players").getString(), baseX + 122, baseY + 38, 13414639, false);
         gg.drawString(this.font, Component.translatable("tc.golems.gui.buttons.creepers").getString(), baseX + 122, baseY + 54, 13426159, false);
      }

      if (this.golem.getCore() == GolemCore.FILL) {
         gg.blit(TEX, baseX + 62, baseY + 54, 8, 168, 8, 8);
         if (!this.golem.fillUsesAnyAmountMode()) {
            gg.blit(TEX, baseX + 62, baseY + 54, 8, 176, 8, 8);
         }

         String text = this.golem.fillUsesAnyAmountMode()
            ? Component.translatable("tc.golems.gui.buttons.any_amount").getString()
            : Component.translatable("tc.golems.gui.buttons.precise_amount").getString();
         gg.pose().pushPose();
         gg.pose().translate(baseX + 66.0F, baseY + 48.0F, 0.0F);
         gg.pose().scale(0.5F, 0.5F, 1.0F);
         gg.drawString(this.font, text, -(this.font.width(text) / 2), 0, 16645629, false);
         gg.pose().popPose();
      }

      if (this.golem.getCore() == GolemCore.USE) {
         gg.blit(TEX, baseX + 42, baseY + 40, 8, 168, 8, 8);
         gg.blit(TEX, baseX + 42, baseY + 50, 8, 168, 8, 8);
         gg.blit(TEX, baseX + 42, baseY + 60, 8, 168, 8, 8);
         if (this.golem.useTargetsEmptyBlocksOnly()) {
            gg.blit(TEX, baseX + 42, baseY + 40, 8, 176, 8, 8);
         }

         if (this.golem.useBreaksBlocks()) {
            gg.blit(TEX, baseX + 42, baseY + 50, 8, 176, 8, 8);
         }

         if (this.golem.useSneaksWhileActing()) {
            gg.blit(TEX, baseX + 42, baseY + 60, 8, 176, 8, 8);
         }

         String text1 = this.golem.useTargetsEmptyBlocksOnly()
            ? Component.translatable("tc.golems.gui.buttons.empty_space").getString()
            : Component.translatable("tc.golems.gui.buttons.block").getString();
         String text2 = this.golem.useBreaksBlocks()
            ? Component.translatable("tc.golems.gui.buttons.left_click").getString()
            : Component.translatable("tc.golems.gui.buttons.right_click").getString();
         String text3 = this.golem.useSneaksWhileActing()
            ? Component.translatable("tc.golems.gui.buttons.sneaking").getString()
            : Component.translatable("tc.golems.gui.buttons.not_sneaking").getString();
         gg.pose().pushPose();
         gg.pose().translate(baseX + 53.0F, baseY + 42.0F, 0.0F);
         gg.pose().scale(0.5F, 0.5F, 1.0F);
         gg.drawString(this.font, text1, 0, 0, 16645629, false);
         gg.drawString(this.font, text2, 0, 20, 16645629, false);
         gg.drawString(this.font, text3, 0, 40, 16645629, false);
         gg.pose().popPose();
      }

      if (this.golem.getUpgradeAmount(GolemUpgrade.ENTROPY.ordinal()) > 0 && this.golem.getCore().canSort()) {
         int shiftx = this.golem.getCore() == GolemCore.SORTING ? 66 : 180;
         int shifty = this.golem.getCore() == GolemCore.SORTING ? 12 : 0;
         gg.blit(TEX, baseX + shiftx, baseY + 24 + shifty, 8, 168, 8, 8);
         gg.blit(TEX, baseX + shiftx, baseY + 34 + shifty, 8, 168, 8, 8);
         gg.blit(TEX, baseX + shiftx, baseY + 44 + shifty, 8, 168, 8, 8);
         String text1 = Component.translatable("tc.golems.gui.buttons.ore_dictionary").getString();
         String text2 = Component.translatable("tc.golems.gui.buttons.ignore_damage").getString();
         String text3 = Component.translatable("tc.golems.gui.buttons.ignore_nbt").getString();
         if (this.golem.checkOreDict()) {
            gg.blit(TEX, baseX + shiftx, baseY + 24 + shifty, 8, 176, 8, 8);
         }

         if (this.golem.ignoreDamage()) {
            gg.blit(TEX, baseX + shiftx, baseY + 34 + shifty, 8, 176, 8, 8);
         }

         if (this.golem.ignoreNBT()) {
            gg.blit(TEX, baseX + shiftx, baseY + 44 + shifty, 8, 176, 8, 8);
         }

         gg.pose().pushPose();
         gg.pose().translate(baseX + shiftx + 10.0F, baseY + 26.0F + shifty, 0.0F);
         gg.pose().scale(0.5F, 0.5F, 1.0F);
         gg.drawString(this.font, text1, 0, 0, this.golem.checkOreDict() ? 16645629 : 6710886, false);
         gg.drawString(this.font, text2, 0, 20, this.golem.ignoreDamage() ? 16645629 : 6710886, false);
         gg.drawString(this.font, text3, 0, 40, this.golem.ignoreNBT() ? 16645629 : 6710886, false);
         gg.pose().popPose();
      }

      this.drawGolem(gg, baseX + 21, baseY + 75, 30, baseX + 21 - this.xSize_lo, baseY + 25 - this.ySize_lo);
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void drawGolem(GuiGraphics gg, int x, int y, int scale, float mx, float my) {
      InventoryScreen.renderEntityInInventoryFollowsMouse(gg, x - 30, y - 60, x + 30, y + 10, scale, 0.0625F, mx, my, this.golem);
   }

   public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
      this.xSize_lo = mouseX;
      this.ySize_lo = mouseY;
      super.render(gg, mouseX, mouseY, partialTick);
      if (this.golem.getCore() != GolemCore.BLANK && this.golem.getCore().hasInventory() && this.golem.getUpgradeAmount(GolemUpgrade.ORDER.ordinal()) > 0) {
         int baseX = this.leftPos;
         int baseY = this.topPos;
         int slots = this.golem.getInventory().getSlotCount();

         for (int a = 0; a < Math.min(6, slots); a++) {
            int mposx = mouseX - (baseX + 96 + a / 2 * 28);
            int mposy = mouseY - (baseY + 4 + a % 2 * 31);
            if (mposx >= 0 && mposy >= 0 && mposx < 24 && mposy < 12) {
               int idx = a + ((GolemMenu)this.menu).currentScroll * 6;
               Component text = this.golem.getColors(idx) < 0
                  ? Component.translatable("tc.golems.gui.buttons.any_color")
                  : Component.translatable("tc.colors.color_" + this.golem.getColors(idx));
               gg.drawString(this.font, text, baseX + 133 - this.font.width(text) / 2, baseY - 6, 16645629, false);
               break;
            }
         }
      }

      this.renderTooltip(gg, mouseX, mouseY);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         int baseX = this.leftPos;
         int baseY = this.topPos;
         int slots = this.golem.getInventory().getSlotCount();
         if (this.golem.getUpgradeAmount(GolemUpgrade.ORDER.ordinal()) > 0 && this.golem.getCore() != GolemCore.BLANK && this.golem.getCore().hasInventory()) {
            for (int a = 0; a < Math.min(6, slots); a++) {
               int var7 = (int)mouseX - (baseX + 96 + a / 2 * 28);
               int var8 = (int)mouseY - (baseY + 4 + a % 2 * 31);
               if (var7 >= 0 && var8 >= 0 && var7 < 8 && var8 < 12) {
                  this.minecraft.gameMode.handleInventoryButtonClick(((GolemMenu)this.menu).containerId, a + ((GolemMenu)this.menu).currentScroll * 6);
                  return true;
               }

               if (var7 >= 16 && var8 >= 0 && var7 < 24 && var8 < 12) {
                  this.minecraft.gameMode.handleInventoryButtonClick(((GolemMenu)this.menu).containerId, a + slots + ((GolemMenu)this.menu).currentScroll * 6);
                  return true;
               }
            }
         }

         if (slots > 6) {
            int var7x = (int)mouseX - (baseX + 111);
            int var8x = (int)mouseY - (baseY + 68);
            if (var7x >= 0 && var8x >= 0 && var7x < 24 && var8x < 8 && ((GolemMenu)this.menu).currentScroll > 0) {
               this.minecraft.gameMode.handleInventoryButtonClick(((GolemMenu)this.menu).containerId, 66);
               ((GolemMenu)this.menu).currentScroll--;
               return true;
            }

            var7x = (int)mouseX - (baseX + 135);
            var8x = (int)mouseY - (baseY + 68);
            if (var7x >= 0 && var8x >= 0 && var7x < 24 && var8x < 8 && ((GolemMenu)this.menu).currentScroll < ((GolemMenu)this.menu).maxScroll) {
               this.minecraft.gameMode.handleInventoryButtonClick(((GolemMenu)this.menu).containerId, 67);
               ((GolemMenu)this.menu).currentScroll++;
               return true;
            }
         }

         if (this.golem.getCore() == GolemCore.GUARD && this.golem.getUpgradeAmount(GolemUpgrade.ORDER.ordinal()) > 0) {
            for (int a = 0; a < 4; a++) {
               int var7xx = (int)mouseX - (baseX + 104);
               int var8xx = (int)mouseY - (baseY + 5 + 16 * a);
               if (var7xx >= 0 && var8xx >= 0 && var7xx < 8 && var8xx < 8) {
                  this.minecraft.gameMode.handleInventoryButtonClick(((GolemMenu)this.menu).containerId, 51 + a);
                  return true;
               }
            }
         }

         if (this.golem.getCore() == GolemCore.FILL) {
            int var7xx = (int)mouseX - (baseX + 62);
            int var8xx = (int)mouseY - (baseY + 54);
            if (var7xx >= 0 && var8xx >= 0 && var7xx < 8 && var8xx < 8) {
               this.minecraft.gameMode.handleInventoryButtonClick(((GolemMenu)this.menu).containerId, 50);
               return true;
            }
         }

         if (this.golem.getCore() == GolemCore.USE) {
            for (int ax = 0; ax < 3; ax++) {
               int var7xx = (int)mouseX - (baseX + 42);
               int var8xx = (int)mouseY - (baseY + 40 + 10 * ax);
               if (var7xx >= 0 && var8xx >= 0 && var7xx < 8 && var8xx < 8) {
                  this.minecraft.gameMode.handleInventoryButtonClick(((GolemMenu)this.menu).containerId, 50 + ax);
                  return true;
               }
            }
         }

         if (this.golem.getUpgradeAmount(GolemUpgrade.ENTROPY.ordinal()) > 0 && this.golem.getCore().canSort()) {
            int shiftx = this.golem.getCore() == GolemCore.SORTING ? 66 : 180;
            int shifty = this.golem.getCore() == GolemCore.SORTING ? 12 : 0;

            for (int axx = 0; axx < 3; axx++) {
               int var7xx = (int)mouseX - (baseX + shiftx);
               int var8xx = (int)mouseY - (baseY + 24 + axx * 10 + shifty);
               if (var7xx >= 0 && var8xx >= 0 && var7xx < 64 && var8xx < 8) {
                  this.minecraft.gameMode.handleInventoryButtonClick(((GolemMenu)this.menu).containerId, 55 + axx);
                  return true;
               }
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   protected void renderTooltip(GuiGraphics gg, int mouseX, int mouseY) {
      if (this.golem.getCore() == GolemCore.LIQUID && this.hoveredSlot instanceof SlotGhostFluid && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
         FluidStack fluid = (FluidStack)FluidUtil.getFluidContained(this.hoveredSlot.getItem()).orElse(null);
         if (fluid != null) {
            gg.renderTooltip(this.font, List.of(fluid.getDisplayName()), Optional.empty(), mouseX, mouseY);
            return;
         }
      }

      super.renderTooltip(gg, mouseX, mouseY);
   }
}
