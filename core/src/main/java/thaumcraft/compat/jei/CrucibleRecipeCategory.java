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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.init.registry.ModItems;
import thaumcraft.util.UtilsFX;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class CrucibleRecipeCategory implements IRecipeCategory<CrucibleRecipe> {
   public static final RecipeType<CrucibleRecipe> RECIPE_TYPE = RecipeType.create("thaumcraft", "crucible", CrucibleRecipe.class);
   private static final int BACKGROUND_WIDTH = 132;
   private static final int BACKGROUND_HEIGHT = 150;
   private static final int DRAW_X = 17;
   private static final int DRAW_Y = 3;
   private static final float DRAW_SCALE = 1.75F;
   private static final int RESULT_X = 58;
   private static final int RESULT_Y = 8;
   private static final int CATALYST_X = 38;
   private static final int CATALYST_Y = 30;
   private static final int ASPECT_BASE_X = 10;
   private static final int ASPECT_BASE_Y = -21;
   private final IDrawable background;
   private final IDrawable icon;

   public CrucibleRecipeCategory(IGuiHelper guiHelper) {
      this.background = guiHelper.createBlankDrawable(132, 150);
      this.icon = guiHelper.createDrawableItemStack(((BlockItem)ModItems.CRUCIBLE.get()).getDefaultInstance());
   }

   public RecipeType<CrucibleRecipe> getRecipeType() {
      return RECIPE_TYPE;
   }

   public Component getTitle() {
      return Component.translatable("recipe.type.crucible");
   }

   public IDrawable getBackground() {
      return this.background;
   }

   @Nullable
   public IDrawable getIcon() {
      return this.icon;
   }

   public void setRecipe(IRecipeLayoutBuilder builder, CrucibleRecipe recipe, IFocusGroup focuses) {
      builder.addSlot(RecipeIngredientRole.OUTPUT, 58, 8).setSlotName("result").addItemStack(recipe.getResultItem());
      builder.addSlot(RecipeIngredientRole.INPUT, 38, 30).setSlotName("catalyst").addIngredients(recipe.catalyst);
      builder.moveRecipeTransferButton(114, 132);
   }

   @OnlyIn(Dist.CLIENT)
   public void draw(CrucibleRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
      PoseStack pose = guiGraphics.pose();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      pose.pushPose();
      pose.translate(17.0F, 3.0F, 0.0F);
      pose.scale(1.75F, 1.75F, 1.0F);
      guiGraphics.blit(JeiUtil.TEXTURE, 0, 0, 0.0F, 3.0F, 56, 17, 256, 256);
      pose.translate(0.0F, 26.0F, 0.0F);
      guiGraphics.blit(JeiUtil.TEXTURE, 0, 0, 0.0F, 20.0F, 56, 48, 256, 256);
      pose.translate(21.0F, -8.0F, 0.0F);
      guiGraphics.blit(JeiUtil.TEXTURE, 0, 0, 100.0F, 84.0F, 11, 13, 256, 256);
      pose.popPose();

      for (AspectPosition position : getAspectPositions(recipe.aspects)) {
         UtilsFX.drawTag(guiGraphics, position.x(), position.y(), position.aspect(), position.amount(), 0, 0.0F);
      }
   }

   public List<Component> getTooltipStrings(CrucibleRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
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
         Aspect[] orderedAspects = aspects.getAspectsSortedAmount();
         int lastRow = (orderedAspects.length - 1) / 3;
         int lastRowOffset = (3 - orderedAspects.length % 3) * 10;
         int startX = 38;
         int startY = 79 - 10 * lastRow;

         for (int index = 0; index < orderedAspects.length; index++) {
            int rowOffset = 0;
            if (index / 3 >= lastRow && (lastRow > 1 || orderedAspects.length < 3)) {
               rowOffset = lastRowOffset;
            }

            Aspect aspect = orderedAspects[index];
            positions.add(new AspectPosition(aspect, aspects.getAmount(aspect), startX + index % 3 * 20 + rowOffset, startY + index / 3 * 20));
         }

         return positions;
      } else {
         return List.of();
      }
   }
}
