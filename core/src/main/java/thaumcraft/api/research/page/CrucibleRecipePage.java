package thaumcraft.api.research.page;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import thaumcraft.api.ScreenApi;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.block.table.ResearchRecipeScreen;
import thaumcraft.util.InventoryUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class CrucibleRecipePage implements ResearchPage {
   private CrucibleRecipe recipe;
   private ResourceLocation recipeId;

   public CrucibleRecipePage(CrucibleRecipe recipe) {
      this.recipe = recipe;
   }

   public CrucibleRecipePage(JsonObject object) {
      this.recipeId = ResourceLocation.parse(GsonHelper.getAsString(object, "id"));
   }

   public CrucibleRecipe getRecipe() {
      if (this.recipeId != null) {
         this.recipe = ThaumcraftApi.getRecipe(CrucibleRecipe.TYPE, this.recipeId);
         this.recipeId = null;
      }

      return this.recipe;
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void render(ResearchRecipeScreen screen, GuiGraphics graphics, Font font, int page, int x, int y, int mx, int my) {
      CrucibleRecipe recipe = this.getRecipe();
      if (recipe != null) {
         x -= 4;
         y -= 8;
         PoseStack pose = graphics.pose();
         pose.pushPose();
         ScreenApi.MAGIC.renderCenterText(graphics, I18n.get("recipe.type.crucible", new Object[0]), x + 56, y, 0.15F, 5263440);
         pose.pushPose();
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.enableBlend();
         ScreenApi.setShaderTexture(ResearchRecipeScreen.RESEARCH_BOOK_OVERLAY);
         pose.translate(x, y + 28, 0.0F);
         pose.scale(2.0F, 2.0F, 1.0F);
         ScreenApi.renderTextureRect(graphics, 0.0F, 0.0F, 0, 3, 56.0F, 17.0F);
         pose.translate(0.0F, 32.0F, 0.0F);
         ScreenApi.renderTextureRect(graphics, 0.0F, 0.0F, 0, 20, 56.0F, 48.0F);
         pose.translate(21.0F, -8.0F, 0.0F);
         ScreenApi.renderTextureRect(graphics, 0.0F, 0.0F, 100, 84, 11.0F, 13.0F);
         pose.popPose();
         AspectList aspectList = recipe.aspects;
         int n7 = 0;
         int n8 = (aspectList.size() - 1) / 3;
         int n9 = (3 - aspectList.size() % 3) * 10;
         int n10 = x + 28;
         int n11 = y + 96 + 32 - 10 * n8;

         for (Aspect aspect : aspectList.getAspectsSorted()) {
            int n12 = 0;
            if (n7 / 3 >= n8 && (n8 > 1 || aspectList.size() < 3)) {
               n12 = 1;
            }

            screen.renderAspectAndTooltips(graphics, mx, my, aspect, aspectList.getAmount(aspect), n10 + n7 % 3 * 20 + n9 * n12, n11 + n7 / 3 * 20);
            n7++;
         }

         pose.pushPose();
         pose.translate(0.0, 0.0, 100.0);
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         screen.renderItemAndTooltips(graphics, mx, my, recipe.getResultItem(), x + 48, y + 36, false);
         pose.popPose();
         pose.pushPose();
         pose.translate(0.0, 0.0, 100.0);
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         screen.renderItemAndTooltips(graphics, mx, my, InventoryUtils.cycleItemStack(recipe.catalyst), x + 26, y + 72);
         pose.popPose();
         pose.popPose();
      }
   }
}
