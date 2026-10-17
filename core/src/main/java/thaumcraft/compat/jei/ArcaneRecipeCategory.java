package thaumcraft.compat.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.init.registry.ModItems;
import thaumcraft.util.UtilsFX;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ArcaneRecipeCategory implements IRecipeCategory<IArcaneRecipe> {
   public static final RecipeType<IArcaneRecipe> RECIPE_TYPE = RecipeType.create("thaumcraft", "arcane", IArcaneRecipe.class);
   private static final int BACKGROUND_WIDTH = 116;
   private static final int BACKGROUND_HEIGHT = 188;
   private final IDrawable background;
   private final IDrawable icon;

   public ArcaneRecipeCategory(IGuiHelper guiHelper) {
      this.background = guiHelper.createBlankDrawable(116, 188);
      this.icon = guiHelper.createDrawableItemStack(((BlockItem)ModItems.ARCANE_WORKBENCH.get()).getDefaultInstance());
   }

   public RecipeType<IArcaneRecipe> getRecipeType() {
      return RECIPE_TYPE;
   }

   public Component getTitle() {
      return Component.translatable("recipe.type.arcane");
   }

   public IDrawable getBackground() {
      return this.background;
   }

   @Nullable
   public IDrawable getIcon() {
      return this.icon;
   }

   public void setRecipe(IRecipeLayoutBuilder builder, IArcaneRecipe recipe, IFocusGroup focuses) {
      if (recipe instanceof ShapedArcaneRecipe shapedRecipe) {
         addShapedInputs(builder, shapedRecipe);
      } else if (recipe instanceof ShapelessArcaneRecipe shapelessRecipe) {
         addShapelessInputs(builder, shapelessRecipe);
         builder.setShapeless(92, 8);
      }

      builder.addSlot(RecipeIngredientRole.OUTPUT, 48, 22).setSlotName("result").addItemStack(recipe.getResultItem());
      builder.moveRecipeTransferButton(110, 175);
   }

   @OnlyIn(Dist.CLIENT)
   public void draw(IArcaneRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
      PoseStack pose = guiGraphics.pose();
      pose.pushPose();
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      pose.scale(2.0F, 2.0F, 1.0F);
      guiGraphics.blit(JeiUtil.TEXTURE, 2, 27, 112.0F, 15.0F, 52, 52, 256, 256);
      guiGraphics.blit(JeiUtil.TEXTURE, 20, 7, 20.0F, 3.0F, 16, 16, 256, 256);
      pose.popPose();
      pose.pushPose();
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 0.4F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      pose.translate(0.0F, 164.0F, 0.0F);
      pose.scale(2.0F, 2.0F, 1.0F);
      guiGraphics.blit(JeiUtil.TEXTURE, 0, 0, 68.0F, 76.0F, 12, 12, 256, 256);
      pose.popPose();
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      AspectList aspects = recipe.getAspects();
      if (aspects != null && aspects.size() > 0) {
         int index = 0;

         for (Aspect aspect : aspects.getAspectsSortedAmount()) {
            UtilsFX.drawTag(guiGraphics, 14 + 18 * index + (5 - aspects.size()) * 8, 172, aspect, (float)aspects.getAmount(aspect), 0, 0.0F, 771, 1.0F, false);
            index++;
         }
      }
   }

   public List<Component> getTooltipStrings(IArcaneRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
      AspectList aspects = recipe.getAspects();
      if (aspects != null && aspects.size() > 0) {
         int index = 0;

         for (Aspect aspect : aspects.getAspectsSortedAmount()) {
            int x = 14 + 18 * index + (5 - aspects.size()) * 8;
            if (JeiUtil.isHovering(mouseX, mouseY, x, 172, 16, 16)) {
               return JeiUtil.getAspectTooltip(aspect, aspects.getAmount(aspect));
            }

            index++;
         }

         return List.of();
      } else {
         return List.of();
      }
   }

   private static void addShapedInputs(IRecipeLayoutBuilder builder, ShapedArcaneRecipe recipe) {
      int width = recipe.getWidth();
      int height = recipe.getHeight();

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 3; col++) {
            Ingredient ingredient = Ingredient.EMPTY;
            if (row < height && col < width) {
               ingredient = (Ingredient)recipe.getIngredients().get(col + row * width);
            }

            builder.addSlot(RecipeIngredientRole.INPUT, 16 + col * 32, 66 + row * 32).addIngredients(ingredient);
         }
      }
   }

   private static void addShapelessInputs(IRecipeLayoutBuilder builder, ShapelessArcaneRecipe recipe) {
      for (int index = 0; index < recipe.getIngredients().size() && index < 9; index++) {
         Ingredient ingredient = (Ingredient)recipe.getIngredients().get(index);
         if (!ingredient.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 16 + index % 3 * 32, 66 + index / 3 * 32).addIngredients(ingredient);
         }
      }
   }
}
