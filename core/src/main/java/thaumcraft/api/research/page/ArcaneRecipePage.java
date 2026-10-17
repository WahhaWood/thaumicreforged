package thaumcraft.api.research.page;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import thaumcraft.api.ScreenApi;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.block.table.ResearchRecipeScreen;
import thaumcraft.util.InventoryUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ArcaneRecipePage implements ResearchPage {
   private IArcaneRecipe recipe;
   private ResourceLocation recipeId;

   public ArcaneRecipePage(IArcaneRecipe recipe) {
      this.recipe = recipe;
   }

   public ArcaneRecipePage(JsonObject object) {
      this.recipeId = ResourceLocation.parse(GsonHelper.getAsString(object, "id"));
   }

   public IArcaneRecipe getRecipe() {
      if (this.recipeId != null) {
         this.recipe = ThaumcraftApi.getRecipe(ShapedArcaneRecipe.TYPE, this.recipeId);
         if (this.recipe == null) {
            this.recipe = ThaumcraftApi.getRecipe(ShapelessArcaneRecipe.TYPE, this.recipeId);
         }

         this.recipeId = null;
      }

      return this.recipe;
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void render(ResearchRecipeScreen screen, GuiGraphics graphics, Font font, int page, int x, int y, int mx, int my) {
      IArcaneRecipe recipe = this.getRecipe();
      if (recipe != null) {
         x -= 4;
         y -= 8;
         PoseStack pose = graphics.pose();
         pose.pushPose();
         pose.pushPose();
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.enableBlend();
         ScreenApi.setShaderTexture(ResearchRecipeScreen.RESEARCH_BOOK_OVERLAY);
         pose.translate(x, y, 0.0F);
         pose.scale(2.0F, 2.0F, 1.0F);
         ScreenApi.renderTextureRect(graphics, 2.0F, 27.0F, 112, 15, 52.0F, 52.0F);
         ScreenApi.renderTextureRect(graphics, 20.0F, 7.0F, 20, 3, 16.0F, 16.0F);
         pose.popPose();
         pose.pushPose();
         graphics.setColor(1.0F, 1.0F, 1.0F, 0.4F);
         RenderSystem.enableBlend();
         pose.translate(x, y + 164, 0.0F);
         pose.scale(2.0F, 2.0F, 1.0F);
         ScreenApi.renderTextureRect(graphics, 0.0F, 0.0F, 68, 76, 12.0F, 12.0F);
         pose.popPose();
         AspectList aspects = recipe.getAspects();
         if (aspects != null && aspects.size() > 0) {
            int index = 0;

            for (Aspect aspect : aspects.getAspectsSortedAmount()) {
               screen.renderAspectAndTooltips(graphics, mx, my, aspect, aspects.getAmount(aspect), x + 14 + 18 * index + (5 - aspects.size()) * 8, y + 172);
               index++;
            }
         }

         pose.pushPose();
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         pose.translate(0.0, 0.0, 100.0);
         screen.renderItemAndTooltips(graphics, mx, my, InventoryUtils.cycleItemStack(recipe.getResultItem()), x + 48, y + 22, false);
         pose.popPose();
         ScreenApi.MAGIC.renderCenterText(graphics, I18n.get("recipe.type.arcane", new Object[0]), x + 56, y, 0.15F, 5263440);
         if (recipe instanceof ShapedArcaneRecipe arcaneRecipe) {
            int width = arcaneRecipe.getWidth();
            int height = arcaneRecipe.getHeight();
            NonNullList<Ingredient> ingredients = arcaneRecipe.getIngredients();

            for (int xX = 0; xX < width; xX++) {
               for (int yY = 0; yY < height; yY++) {
                  Ingredient ingredient = (Ingredient)ingredients.get(xX + yY * width);
                  if (!ingredient.isEmpty()) {
                     pose.pushPose();
                     pose.translate(0.0, 0.0, 100.0);
                     graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                     screen.renderItemAndTooltips(graphics, mx, my, InventoryUtils.cycleItemStack(ingredient), x + 16 + xX * 32, y + 66 + yY * 32);
                     pose.popPose();
                  }
               }
            }
         } else if (recipe instanceof ShapelessArcaneRecipe arcaneRecipe) {
            NonNullList<Ingredient> ingredients = arcaneRecipe.getIngredients();

            for (int index = 0; index < ingredients.size() && index < 9; index++) {
               Ingredient ingredient = (Ingredient)ingredients.get(index);
               if (!ingredient.isEmpty()) {
                  pose.pushPose();
                  pose.translate(0.0, 0.0, 100.0);
                  graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                  screen.renderItemAndTooltips(graphics, mx, my, InventoryUtils.cycleItemStack(ingredient), x + 16 + index % 3 * 32, y + 66 + index / 3 * 32);
                  pose.popPose();
               }
            }
         }

         pose.popPose();
      }
   }
}
