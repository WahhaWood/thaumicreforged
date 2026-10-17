package thaumcraft.compat.jei;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import thaumcraft.block.infusion_matrix.handlers.InfusionInstabilityHandler;
import thaumcraft.init.registry.ModBlocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * JEI-вкладка стабилизации: сколько очков даёт блок в зеркальной паре,
 * базовая стабильность матрицы, потолок, штраф за одиночку и сколько
 * очков требует каждый класс рецепта.
 *
 * <p>Все числа берутся из {@link InfusionInstabilityHandler} — источник истины
 * один с матрицей и калькулятором.
 */
public class StabilizerRecipeCategory implements IRecipeCategory<StabilizerRecipeCategory.StabilizerEntry> {
   public static final RecipeType<StabilizerEntry> RECIPE_TYPE = RecipeType.create("thaumcraft", "stabilizer", StabilizerEntry.class);
   private static final int BACKGROUND_WIDTH = 126;
   private static final int BACKGROUND_HEIGHT = 150;
   private static final int CENTER_X = 63;
   /** Пороги очков стабильности по классам рецепта — из handler'а, не дублируем. */
   private final IDrawable background;
   private final IDrawable icon;

   public StabilizerRecipeCategory(IGuiHelper guiHelper) {
      this.background = guiHelper.createBlankDrawable(BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
      this.icon = guiHelper.createDrawableItemStack(ModBlocks.MIXED_CRYSTAL_CLUSTER.get().asItem().getDefaultInstance());
   }

   public RecipeType<StabilizerEntry> getRecipeType() {
      return RECIPE_TYPE;
   }

   public Component getTitle() {
      return Component.translatable("recipe.type.stabilizer");
   }

   public IDrawable getBackground() {
      return this.background;
   }

   @Nullable
   public IDrawable getIcon() {
      return this.icon;
   }

   public void setRecipe(IRecipeLayoutBuilder builder, StabilizerEntry recipe, IFocusGroup focuses) {
      builder.addSlot(RecipeIngredientRole.INPUT, 55, 6).setSlotName("stabilizer").addItemStack(recipe.stack());
   }

   @OnlyIn(Dist.CLIENT)
   public void draw(StabilizerEntry recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
      Font font = Minecraft.getInstance().font;
      // Вес блока в паре = сумма двух блоков, поэтому у одиночки показываем половину.
      drawCentered(guiGraphics, font, Component.translatable("jei.stabilizer.pair", fmt(recipe.pairValue())), 30, 16777215);
      drawCentered(guiGraphics, font, Component.translatable("jei.stabilizer.single", fmt(recipe.pairValue() / 2.0)), 41, 11184810);
      drawCentered(guiGraphics, font, Component.translatable("jei.stabilizer.orphan", InfusionInstabilityHandler.SINGLE_PENALTY), 52, 16732309);
      drawCentered(
         guiGraphics, font,
         Component.translatable("jei.stabilizer.altar",
            InfusionInstabilityHandler.BASE_STABILITY, InfusionInstabilityHandler.MAX_STABILITY),
         66, 0x55FF55
      );
      drawCentered(guiGraphics, font, Component.translatable("jei.stabilizer.pedestal", InfusionInstabilityHandler.PEDESTAL_PENALTY), 77, 0xFFAA00);
      drawCentered(guiGraphics, font, Component.translatable("jei.stabilizer.legend"), 92, 16776960);

      for (int tier = 0; tier < 6; tier++) {
         Component line = Component.translatable("tc.inst." + tier)
            .append(Component.literal(" " + InfusionInstabilityHandler.requiredStability(tier * 2)));
         drawCentered(guiGraphics, font, line, 103 + tier * 9, 16777215);
      }
   }

   @OnlyIn(Dist.CLIENT)
   private static void drawCentered(GuiGraphics guiGraphics, Font font, Component text, int y, int color) {
      guiGraphics.drawString(font, text, CENTER_X - font.width(text) / 2, y, color, false);
   }

   /** Дробный вес свечи (2.5) показываем как «2.5», целые — без дробной части. */
   private static String fmt(double value) {
      return value == Math.rint(value) ? Long.toString(Math.round(value)) : String.format("%.1f", value);
   }

   /**
    * @param stack блок-стабилизатор
    * @param pairValue очки за зеркальную пару из двух таких блоков
    */
   public record StabilizerEntry(ItemStack stack, double pairValue) {
   }
}