package thaumcraft.api.research.page;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import thaumcraft.api.ScreenApi;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.block.table.ResearchRecipeScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class SmeltingRecipePage implements ResearchPage {
   private SmeltingRecipe recipe;
   private ResourceLocation recipeId;

   public SmeltingRecipePage(SmeltingRecipe recipe) {
      this.recipe = recipe;
   }

   public SmeltingRecipePage(JsonObject object) {
      this.recipeId = ResourceLocation.parse(GsonHelper.getAsString(object, "id"));
   }

   public SmeltingRecipe getRecipe() {
      if (this.recipeId != null) {
         this.recipe = ThaumcraftApi.getRecipe(RecipeType.SMELTING, this.recipeId);
         this.recipeId = null;
      }

      return this.recipe;
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void render(ResearchRecipeScreen screen, GuiGraphics graphics, Font font, int page, int x, int y, int mx, int my) {
      SmeltingRecipe recipe = this.getRecipe();
      if (recipe != null) {
         x -= 4;
         y -= 8;
         Ingredient ingredient = (Ingredient)recipe.getIngredients().get(0);
         ItemStack smeltingResult = recipe.getResultItem(RegistryAccess.EMPTY);
         PoseStack pose = graphics.pose();
         pose.pushPose();
         ScreenApi.MAGIC.renderCenterText(graphics, I18n.get("recipe.type.smelting", new Object[0]), x + 56, y, 0.15F, 5263440);
         pose.pushPose();
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.enableBlend();
         pose.translate(x, y + 28, 0.0F);
         pose.scale(2.0F, 2.0F, 1.0F);
         graphics.blit(ResearchRecipeScreen.RESEARCH_BOOK_OVERLAY, 0, 0, 0, 192, 56, 64);
         pose.popPose();
         pose.pushPose();
         pose.translate(0.0, 0.0, 100.0);
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         screen.renderItemAndTooltips(graphics, mx, my, ingredient, x + 48, y + 64);
         pose.popPose();
         pose.pushPose();
         pose.translate(0.0, 0.0, 100.0);
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         screen.renderItemAndTooltips(graphics, mx, my, smeltingResult, x + 48, y + 144, false);
         pose.popPose();
         pose.popPose();
      }
   }
}
