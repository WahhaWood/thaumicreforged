package thaumcraft.api.research.page;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ScreenApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.block.table.ResearchRecipeScreen;
import thaumcraft.util.UtilsFX;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class AspectPage implements ResearchPage {
   private final AspectList list;

   public AspectPage(AspectList list) {
      this.list = list;
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void render(ResearchRecipeScreen screen, GuiGraphics graphics, Font font, int page, int x, int y, int mx, int my) {
      if (this.list != null && this.list.size() > 0) {
         x -= 10;
         y -= 10;
         PoseStack pose = graphics.pose();
         pose.pushPose();
         int n7 = 0;

         for (Aspect aspect : this.list.getAspectsSorted()) {
            if (aspect.getImage() != null) {
               int n9 = y + n7 * 50;
               if (mx >= x && my >= n9 && mx < x + 40 && my < n9 + 40) {
                  pose.pushPose();
                  RenderSystem.enableBlend();
                  RenderSystem.blendFunc(770, 771);
                  pose.translate(x - 5, y + n7 * 50 - 5, 0.0);
                  pose.scale(2.5F, 2.5F, 0.0F);
                  UtilsFX.drawTexturedQuadFull(graphics, Thaumcraft.location("textures/aspects/_back.png"), 0, 0, 0);
                  RenderSystem.disableBlend();
                  pose.popPose();
               }

               pose.pushPose();
               pose.scale(2.0F, 2.0F, 2.0F);
               UtilsFX.drawTag(graphics, x / 2, (y + n7 * 50) / 2, aspect, this.list.getAmount(aspect), 0, 0.0F);
               pose.popPose();
               ScreenApi.MAGIC.renderCenterText(graphics, aspect.getStringName(), (float)(x + 16), (float)(y + 33 + n7 * 50), 5263440);
               if (aspect.getComponents() != null) {
                  pose.pushPose();
                  pose.scale(1.5F, 1.5F, 1.5F);
                  UtilsFX.drawTag(graphics, (int)((x + 54) / 1.5F), (int)((y + 4 + n7 * 50) / 1.5F), aspect.getComponents()[0], 0.0F, 0, 0.0F);
                  UtilsFX.drawTag(graphics, (int)((x + 96) / 1.5F), (int)((y + 4 + n7 * 50) / 1.5F), aspect.getComponents()[1], 0.0F, 0, 0.0F);
                  pose.popPose();
                  ScreenApi.MAGIC
                     .renderCenterText(graphics, aspect.getComponents()[0].getStringName(), (float)(x + 16 + 50), (float)(y + 30 + n7 * 50), 5263440);
                  ScreenApi.MAGIC
                     .renderCenterText(graphics, aspect.getComponents()[1].getStringName(), (float)(x + 16 + 92), (float)(y + 30 + n7 * 50), 5263440);
                  ScreenApi.MAGIC.renderCenterText(graphics, "=", (float)(x + 7 + 32), (float)(y + 12 + n7 * 50), 10066329);
                  ScreenApi.MAGIC.renderCenterText(graphics, "+", (float)(x + 4 + 79), (float)(y + 12 + n7 * 50), 10066329);
               } else {
                  ScreenApi.MAGIC.renderCenterText(graphics, I18n.get("tc.aspect.primal", new Object[0]), (float)(x + 85), (float)(y + 12 + n7 * 50), 4473924);
               }
            }

            n7++;
         }

         int n10 = 0;

         for (Aspect aspect2 : this.list.getAspectsSorted()) {
            int n12 = y + n10 * 50;
            if (mx >= x && my >= n12 && mx < x + 40 && my < n12 + 40) {
               ArrayList<ItemStack> list2 = screen.aspectItems.get(aspect2);
               if (list2 != null && list2.size() > 0) {
                  int n13 = 0;
                  int n14 = 0;

                  for (ItemStack itemStack : list2) {
                     pose.pushPose();
                     graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                     graphics.renderItem(itemStack, mx + 8 + n13 * 17, 17 * n14 + (my - (4 + list2.size() / 8 * 8)));
                     graphics.renderItemDecorations(font, itemStack, mx + 8 + n13 * 17, 17 * n14 + (my - (4 + list2.size() / 8 * 8)));
                     pose.popPose();
                     if (++n13 >= 8) {
                        n13 = 0;
                        n14++;
                     }
                  }
               }
            }

            n10++;
         }

         pose.popPose();
      }
   }
}
