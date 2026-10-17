package thaumcraft.api.research.page;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import thaumcraft.api.ScreenApi;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.FortressArmorUpgradeRecipe;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.block.table.ResearchRecipeScreen;
import thaumcraft.util.InventoryUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.block.infusion_matrix.handlers.InfusionInstabilityHandler;

public class FortressArmorUpgradeRecipePage implements ResearchPage {
   private FortressArmorUpgradeRecipe recipe;
   private ResourceLocation recipeId;

   public FortressArmorUpgradeRecipePage(FortressArmorUpgradeRecipe recipe) {
      this.recipe = recipe;
   }

   public FortressArmorUpgradeRecipePage(JsonObject object) {
      this.recipeId = ResourceLocation.parse(GsonHelper.getAsString(object, "id"));
   }

   public FortressArmorUpgradeRecipe getRecipe() {
      if (this.recipeId != null) {
         this.recipe = ThaumcraftApi.getRecipe(FortressArmorUpgradeRecipe.TYPE, this.recipeId);
         this.recipeId = null;
      }

      return this.recipe;
   }

   public ItemStack getDisplayResult() {
      FortressArmorUpgradeRecipe recipe = this.getRecipe();
      return recipe == null ? ItemStack.EMPTY : recipe.getDisplayResult();
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void render(ResearchRecipeScreen screen, GuiGraphics graphics, Font font, int page, int x, int y, int mx, int my) {
      FortressArmorUpgradeRecipe recipe = this.getRecipe();
      if (recipe != null) {
         x -= 4;
         y -= 8;
         PoseStack pose = graphics.pose();
         pose.pushPose();
         ScreenApi.MAGIC.renderCenterText(graphics, I18n.get("recipe.type.infusion", new Object[0]), x + 56, y, 0.15F, 5263440);
         String instability = I18n.get("tc.inst", new Object[]{InfusionInstabilityHandler.requiredStability(recipe.getInstability())}) + " " + I18n.get("tc.inst." + InfusionInstabilityHandler.tierOf(recipe.getInstability()), new Object[0]);
         ScreenApi.MAGIC.renderCenterText(graphics, instability, (float)(x + 56), (float)(y + 194), 5263440);
         pose.pushPose();
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.enableBlend();
         ScreenApi.setShaderTexture(ResearchRecipeScreen.RESEARCH_BOOK_OVERLAY);
         pose.translate(x, y + 20, 0.0F);
         pose.scale(2.0F, 2.0F, 1.0F);
         ScreenApi.renderTextureRect(graphics, 0.0F, 0.0F, 0, 3, 56.0F, 17.0F);
         pose.translate(0.0F, 19.0F, 0.0F);
         ScreenApi.renderTextureRect(graphics, 0.0F, 0.0F, 200, 77, 60.0F, 44.0F);
         pose.popPose();
         AspectList aspectList = recipe.getAspects();
         int aspectIndex = 0;
         int rows = (aspectList.size() - 1) / 5;
         int lastRowOffset = (5 - aspectList.size() % 5) * 10;
         int startX = x + 8;
         int startY = y + 164 - 10 * rows;

         for (Aspect aspect : aspectList.getAspectsSorted()) {
            int shift = 0;
            if (aspectIndex / 5 >= rows && (rows > 1 || aspectList.size() < 5)) {
               shift = 1;
            }

            screen.renderAspectAndTooltips(
               graphics, mx, my, aspect, aspectList.getAmount(aspect), startX + aspectIndex % 5 * 20 + lastRowOffset * shift, startY + aspectIndex / 5 * 20
            );
            aspectIndex++;
         }

         pose.pushPose();
         pose.translate(0.0, 0.0, 100.0);
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         screen.renderItemAndTooltips(graphics, mx, my, recipe.getDisplayResult(), x + 48, y + 28, false);
         pose.popPose();
         pose.pushPose();
         pose.translate(0.0, 0.0, 100.0);
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         screen.renderItemAndTooltips(graphics, mx, my, recipe.getCentral(), x + 48, y + 94);
         pose.popPose();
         pose.pushPose();
         pose.translate(0.0, 0.0, 100.0);
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         int posX = x + 56;
         int posY = y + 102;
         float step = 360 / recipe.components.size();
         float alpha = -90.0F;

         for (Ingredient ingredient : recipe.components) {
            int offX = (int)(Mth.cos(alpha / 180.0F * (float) Math.PI) * 40.0F) - 8;
            int offY = (int)(Mth.sin(alpha / 180.0F * (float) Math.PI) * 40.0F) - 8;
            screen.renderItemAndTooltips(graphics, mx, my, InventoryUtils.cycleItemStack(ingredient), posX + offX, posY + offY);
            alpha += step;
         }

         pose.popPose();
         pose.popPose();
      }
   }
}
