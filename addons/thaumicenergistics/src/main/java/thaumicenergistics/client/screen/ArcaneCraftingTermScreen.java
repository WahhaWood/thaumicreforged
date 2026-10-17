package thaumicenergistics.client.screen;

import appeng.api.config.ActionItems;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.ActionButton;
import appeng.core.AEConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import thaumicenergistics.block.arcane_assembler.ArcaneAssemblerBlockEntity;
import thaumicenergistics.menu.ArcaneCraftingTermMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.util.UtilsFX;

public class ArcaneCraftingTermScreen extends MEStorageScreen<ArcaneCraftingTermMenu> {
   /** The strip left of the search box; the title is drawn here (not by the style) so it is cut instead of running under the box. */
   private static final int TITLE_X = 8;
   private static final int TITLE_Y = 6;
   private static final int TITLE_WIDTH = 68;
   private static final String STYLE_PATH = "/screens/terminals/arcane_crafting_terminal.json";

   public ArcaneCraftingTermScreen(ArcaneCraftingTermMenu menu, Inventory playerInventory, Component title) {
      super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/terminals/arcane_crafting_terminal.json"));
      ActionButton clearBtn = new ActionButton(ActionItems.S_STASH, btn -> menu.clearCraftingGrid());
      clearBtn.setHalfSize(true);
      clearBtn.setDisableBackground(true);
      this.widgets.add("clearCraftingGrid", clearBtn);
   }

   public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
      super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);
      guiGraphics.drawString(
         this.font, ScreenText.fitTitle(this.font, this.title, TITLE_WIDTH), TITLE_X, TITLE_Y, ScreenText.TITLE_COLOR, false
      );
      this.drawCraftingAspects(guiGraphics);
   }

   private void drawCraftingAspects(GuiGraphics guiGraphics) {
      IArcaneRecipe recipe = ((ArcaneCraftingTermMenu)this.getMenu()).getArcaneRecipe();
      if (recipe != null) {
         AspectList recipeAspects = recipe.getAspects();
         if (recipeAspects != null) {
            AspectList wandVis = null;
            ItemStack wandStack = ((ArcaneCraftingTermMenu)this.getMenu()).getWandSlot().getItem();
            if (!wandStack.isEmpty() && wandStack.getItem() instanceof ItemWandCasting wandItem) {
               wandVis = wandItem.getAllVis(wandStack);
            }

            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(136.5F, (float)this.imageHeight - 151.5F, 0.0F);

            for (int i = 0; i < ArcaneAssemblerBlockEntity.PRIMALS.length && i < 6; i++) {
               Aspect aspect = ArcaneAssemblerBlockEntity.PRIMALS[i];
               int cost = recipeAspects.getAmount(aspect);
               if (cost > 0) {
                  float alpha = 1.0F;
                  if (wandVis == null || wandVis.getAmount(aspect) < cost * 100) {
                     alpha = pingPongFromTime(0.5, 0.25F, 0.75F);
                  }

                  int x = i % 2 * 18;
                  int y = i / 2 * 18;
                  UtilsFX.drawTag(guiGraphics, x, y, aspect, (float)cost, 0, 0.0F, 771, alpha, false);
               }
            }

            pose.popPose();
         }
      }
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      int x = mouseX - this.leftPos;
      int y = mouseY - this.topPos;
      boolean cut = this.font.width(this.title.getString()) > TITLE_WIDTH;
      if (cut && x >= TITLE_X && x < TITLE_X + TITLE_WIDTH && y >= TITLE_Y && y < TITLE_Y + this.font.lineHeight) {
         guiGraphics.renderTooltip(this.font, this.title, mouseX, mouseY);
      }
   }

   public void onClose() {
      if (AEConfig.instance().isClearGridOnClose()) {
         ((ArcaneCraftingTermMenu)this.getMenu()).clearCraftingGrid();
      }

      super.onClose();
   }

   private static float pingPongFromTime(double intervalSeconds, float min, float max) {
      double time = (double)System.currentTimeMillis() / 1000.0;
      double t = time / intervalSeconds % 2.0;
      if (t > 1.0) {
         t = 2.0 - t;
      }

      return min + (float)((double)(max - min) * t);
   }
}
