package thaumcraft.compat.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ScreenApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.init.registry.ModItems;
import thaumcraft.util.UtilsFX;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.block.infusion_matrix.handlers.InfusionInstabilityHandler;

public class InfusionRecipeCategory implements IRecipeCategory<InfusionRecipe> {
   public static final RecipeType<InfusionRecipe> RECIPE_TYPE = RecipeType.create("thaumcraft", "infusion", InfusionRecipe.class);
   private static final int BACKGROUND_WIDTH = 126;
   private static final int BACKGROUND_HEIGHT = 190;
   private static final int PAGE_X = 7;
   private static final int PAGE_Y = -12;
   private static final int RESULT_X = 55;
   private static final int RESULT_Y = 16;
   private static final int CENTRAL_X = 55;
   private static final int CENTRAL_Y = 82;
   private static final int CENTER_X = 63;
   private static final int CENTER_Y = 90;
   private static final int RADIUS = 40;
   private static final int ASPECT_X = 15;
   private static final int ASPECT_Y = 152;
   private static final int INSTABILITY_Y = 182;
   private final IDrawable background;
   private final IDrawable icon;

   public InfusionRecipeCategory(IGuiHelper guiHelper) {
      this.background = guiHelper.createBlankDrawable(126, 190);
      this.icon = guiHelper.createDrawableItemStack(((BlockItem)ModItems.RUNIC_MATRIX.get()).getDefaultInstance());
   }

   public RecipeType<InfusionRecipe> getRecipeType() {
      return RECIPE_TYPE;
   }

   public Component getTitle() {
      return Component.translatable("recipe.type.infusion");
   }

   public IDrawable getBackground() {
      return this.background;
   }

   @Nullable
   public IDrawable getIcon() {
      return this.icon;
   }

   public void setRecipe(IRecipeLayoutBuilder builder, InfusionRecipe recipe, IFocusGroup focuses) {
      builder.addSlot(RecipeIngredientRole.OUTPUT, 55, 16).setSlotName("result").addItemStack(recipe.getResultItem());
      builder.addSlot(RecipeIngredientRole.INPUT, 55, 82).setSlotName("central").addIngredients(recipe.getCentral());
      List<InfusionRecipeCategory.IngredientPosition> components = getComponentPositions(recipe);

      for (int index = 0; index < components.size(); index++) {
         InfusionRecipeCategory.IngredientPosition position = components.get(index);
         builder.addSlot(RecipeIngredientRole.INPUT, position.x(), position.y()).setSlotName("component_" + index).addIngredients(position.ingredient());
      }

      builder.moveRecipeTransferButton(108, 172);
   }

   @OnlyIn(Dist.CLIENT)
   public void draw(InfusionRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
      PoseStack pose = guiGraphics.pose();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      pose.pushPose();
      pose.translate(7.0F, 8.0F, 0.0F);
      pose.scale(2.0F, 2.0F, 1.0F);
      guiGraphics.blit(JeiUtil.TEXTURE, 0, 0, 0.0F, 3.0F, 56, 17, 256, 256);
      pose.translate(0.0F, 19.0F, 0.0F);
      guiGraphics.blit(JeiUtil.TEXTURE, 0, 0, 200.0F, 77.0F, 60, 44, 256, 256);
      pose.popPose();
      String instability = I18n.get("tc.inst", new Object[]{InfusionInstabilityHandler.requiredStability(recipe.getInstability())}) + I18n.get("tc.inst." + InfusionInstabilityHandler.tierOf(recipe.getInstability()), new Object[0]);
      ScreenApi.MAGIC.renderCenterText(guiGraphics, instability, 63.0F, 182.0F, 0.15F, 5263440);

      for (AspectPosition position : getAspectPositions(recipe.aspects)) {
         UtilsFX.drawTag(guiGraphics, position.x(), position.y(), position.aspect(), position.amount(), 0, 0.0F);
      }
   }

   public List<Component> getTooltipStrings(InfusionRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
      for (AspectPosition position : getAspectPositions(recipe.aspects)) {
         if (JeiUtil.isHovering(mouseX, mouseY, position.x(), position.y(), 16, 16)) {
            return JeiUtil.getAspectTooltip(position.aspect(), position.amount());
         }
      }

      return List.of();
   }

   private static List<AspectPosition> getAspectPositions(AspectList aspects) {
      if (aspects != null && aspects.size() > 0) {
         ArrayList<AspectPosition> positions = new ArrayList<>();
         Aspect[] orderedAspects = aspects.getAspectsSorted();
         int lastRow = (orderedAspects.length - 1) / 5;
         int lastRowSize = orderedAspects.length % 5 == 0 ? 5 : orderedAspects.length % 5;
         int lastRowOffset = (5 - lastRowSize) * 10;
         int startY = 152 - 10 * lastRow;

         for (int index = 0; index < orderedAspects.length; index++) {
            int rowOffset = 0;
            if (index / 5 >= lastRow && (lastRow > 1 || orderedAspects.length < 5)) {
               rowOffset = lastRowOffset;
            }

            Aspect aspect = orderedAspects[index];
            positions.add(new AspectPosition(aspect, aspects.getAmount(aspect), 15 + index % 5 * 20 + rowOffset, startY + index / 5 * 20));
         }

         return positions;
      } else {
         return List.of();
      }
   }

   private static List<InfusionRecipeCategory.IngredientPosition> getComponentPositions(InfusionRecipe recipe) {
      ArrayList<InfusionRecipeCategory.IngredientPosition> positions = new ArrayList<>();
      int componentCount = recipe.components.size();
      if (componentCount <= 0) {
         return positions;
      } else {
         float step = 360.0F / componentCount;
         float angle = -90.0F;

         for (Ingredient ingredient : recipe.components) {
            int x = 63 + (int)(Mth.cos(angle / 180.0F * (float) Math.PI) * 40.0F) - 8;
            int y = 90 + (int)(Mth.sin(angle / 180.0F * (float) Math.PI) * 40.0F) - 8;
            positions.add(new InfusionRecipeCategory.IngredientPosition(ingredient, x, y));
            angle += step;
         }

         return positions;
      }
   }

   private record IngredientPosition(Ingredient ingredient, int x, int y) {
   }
}
