package thaumcraft.api.research.page;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import thaumcraft.api.ScreenApi;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.block.table.ResearchRecipeScreen;
import thaumcraft.util.InventoryUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class CraftingRecipePage implements ResearchPage {
   private CraftingRecipe recipe;
   private ResourceLocation recipeId;

   public CraftingRecipePage(CraftingRecipe recipe) {
      this.recipe = recipe;
   }

   public CraftingRecipePage(JsonObject object) {
      this.recipeId = ResourceLocation.parse(GsonHelper.getAsString(object, "id"));
   }

   public CraftingRecipe getRecipe() {
      if (this.recipeId != null) {
         this.recipe = ThaumcraftApi.getRecipe(RecipeType.CRAFTING, this.recipeId);
         this.recipeId = null;
      }

      return this.recipe;
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void render(ResearchRecipeScreen screen, GuiGraphics graphics, Font font, int page, int x, int y, int mx, int my) {
      CraftingRecipe recipe = this.getRecipe();
      if (recipe != null) {
         x -= 4;
         y -= 8;
         PoseStack pose = graphics.pose();
         pose.pushPose();
         ScreenApi.setShaderTexture(ResearchRecipeScreen.RESEARCH_BOOK_OVERLAY);
         pose.pushPose();
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.enableBlend();
         pose.translate(x, y, 0.0F);
         pose.scale(2.0F, 2.0F, 1.0F);
         ScreenApi.renderTextureRect(graphics, 2.0F, 32.0F, 60, 15, 52.0F, 52.0F);
         ScreenApi.renderTextureRect(graphics, 20.0F, 12.0F, 20, 3, 16.0F, 16.0F);
         pose.popPose();
         pose.pushPose();
         pose.translate(0.0, 0.0, 100.0);
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         screen.renderItemAndTooltips(graphics, mx, my, recipe.getResultItem(RegistryAccess.EMPTY), x + 48, y + 32, false);
         pose.popPose();
         if (recipe instanceof ShapedRecipe shapedRecipe) {
            ScreenApi.MAGIC.renderCenterText(graphics, I18n.get("recipe.type.workbench", new Object[0]), x + 56, y, 0.15F, 5263440);
            NonNullList<Ingredient> ingredients = shapedRecipe.getIngredients();
            int width = shapedRecipe.getWidth();
            int height = shapedRecipe.getHeight();

            for (int xX = 0; xX < width; xX++) {
               for (int yY = 0; yY < height; yY++) {
                  Ingredient ingredient = (Ingredient)ingredients.get(xX + yY * width);
                  if (!ingredient.isEmpty()) {
                     pose.pushPose();
                     graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                     screen.renderItemAndTooltips(graphics, mx, my, InventoryUtils.cycleItemStack(ingredient), x + 16 + xX * 32, y + 76 + yY * 32);
                     pose.popPose();
                  }
               }
            }
         }

         if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            ScreenApi.MAGIC.renderCenterText(graphics, I18n.get("recipe.type.workbenchshapeless", new Object[0]), x + 56, y, 0.15F, 5263440);
            NonNullList<Ingredient> ingredients = shapelessRecipe.getIngredients();

            for (int index = 0; index < ingredients.size() && index < 9; index++) {
               Ingredient ingredient = (Ingredient)ingredients.get(index);
               if (!ingredient.isEmpty()) {
                  pose.pushPose();
                  graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                  screen.renderItemAndTooltips(graphics, mx, my, InventoryUtils.cycleItemStack(ingredient), x + 16 + index % 3 * 32, y + 76 + index / 3 * 32);
                  pose.popPose();
               }
            }
         }

         pose.popPose();
      }
   }
}
