package thaumcraft.api.research.page;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import thaumcraft.api.ScreenApi;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionEnchantmentRecipe;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.block.table.ResearchRecipeScreen;
import thaumcraft.util.InventoryUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.block.infusion_matrix.handlers.InfusionInstabilityHandler;

public class InfusionEnchantmentRecipePage implements ResearchPage {
   private InfusionEnchantmentRecipe recipe;
   private ResourceLocation recipeId;

   public InfusionEnchantmentRecipePage(InfusionEnchantmentRecipe recipe) {
      this.recipe = recipe;
   }

   public InfusionEnchantmentRecipePage(JsonObject object) {
      this.recipeId = ResourceLocation.parse(GsonHelper.getAsString(object, "id"));
   }

   public InfusionEnchantmentRecipe getRecipe() {
      if (this.recipeId != null) {
         this.recipe = ThaumcraftApi.getRecipe(InfusionEnchantmentRecipe.TYPE, this.recipeId);
         this.recipeId = null;
      }

      return this.recipe;
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void render(ResearchRecipeScreen screen, GuiGraphics graphics, Font font, int page, int x, int y, int mx, int my) {
      InfusionEnchantmentRecipe recipe = this.getRecipe();
      if (recipe != null) {
         x -= 4;
         y -= 8;
         PoseStack pose = graphics.pose();
         pose.pushPose();
         int n7 = (int)(1L + System.currentTimeMillis() / 1000L % ((Enchantment)recipe.enchantment.value()).getMaxLevel());
         ScreenApi.MAGIC.renderCenterText(graphics, I18n.get("recipe.type.infusionenchantment", new Object[0]), x + 56, y, 0.15F, 5263440);
         String string = I18n.get("tc.inst", new Object[]{InfusionInstabilityHandler.requiredStability(recipe.instability)}) + " " + I18n.get("tc.inst." + InfusionInstabilityHandler.tierOf(recipe.instability), new Object[0]);
         ScreenApi.MAGIC.renderCenterText(graphics, string, (float)(x + 56), (float)(y + 194), 5263440);
         Enchantment var10000 = (Enchantment)recipe.enchantment.value();
         Component translatedName = Enchantment.getFullname(recipe.enchantment, n7);
         ScreenApi.MAGIC.renderCenterText(graphics, translatedName, (float)(x + 56), (float)(y + 24), 7360656);
         String string2 = recipe.recipeXP * n7 + " levels";
         ScreenApi.MAGIC.renderCenterText(graphics, string2, (float)(x + 56), (float)(y + 40), 5277776);
         pose.pushPose();
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.enableBlend();
         pose.translate(x, y + 20, 0.0F);
         pose.scale(2.0F, 2.0F, 1.0F);
         pose.translate(0.0F, 19.0F, 0.0F);
         graphics.blit(ResearchRecipeScreen.RESEARCH_BOOK_OVERLAY, 0, 0, 200, 77, 60, 44);
         pose.popPose();
         AspectList aspectList = recipe.aspects;
         int n8 = 0;
         int n9 = (aspectList.size() - 1) / 5;
         int n10 = (5 - aspectList.size() % 5) * 10;
         int n11 = x + 8;
         int n12 = y + 164 - 10 * n9;

         for (Aspect aspect : aspectList.getAspectsSorted()) {
            int n13 = 0;
            if (n8 / 5 >= n9 && (n9 > 1 || aspectList.size() < 5)) {
               n13 = 1;
            }

            screen.renderAspectAndTooltips(graphics, mx, my, aspect, aspectList.getAmount(aspect) * n7, n11 + n8 % 5 * 20 + n10 * n13, n12 + n8 / 5 * 20);
            n8++;
         }

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
