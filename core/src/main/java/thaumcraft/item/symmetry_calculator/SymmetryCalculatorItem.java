package thaumcraft.item.symmetry_calculator;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import thaumcraft.block.infusion_matrix.RunicMatrixBlock;
import thaumcraft.block.infusion_matrix.RunicMatrixBlockEntity;
import thaumcraft.block.infusion_matrix.handlers.InfusionInstabilityHandler;

/**
 * Калькулятор стабильности: показывает, хватает ли алтарю очков стабильности
 * для конкретного рецепта, и — если рецепт не запущен — сколько очков ещё надо
 * добрать. Считает ровно теми же числами, что и матрица ({@code refreshInstability}).
 *
 * <p>Формат отчёта: сколько очков есть, сколько нужно, хватает ли, и если не
 * хватает — как часто прилетит событие.
 */
public class SymmetryCalculatorItem extends Item {
   public SymmetryCalculatorItem(Properties properties) {
      super(properties);
   }

   @NotNull
   @Override
   public InteractionResult useOn(@NotNull UseOnContext context) {
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      Player player = context.getPlayer();
      if (player == null || !(level.getBlockState(pos).getBlock() instanceof RunicMatrixBlock)) {
         return InteractionResult.PASS;
      }

      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof RunicMatrixBlockEntity matrix) {
         this.report(matrix, player);
         // Фиолетовая искра — матрица «ответила» прибору.
         level.blockEvent(pos, level.getBlockState(pos).getBlock(), 1, 0);
      }

      return InteractionResult.sidedSuccess(level.isClientSide());
   }

   private void report(RunicMatrixBlockEntity matrix, Player player) {
      InfusionInstabilityHandler.StructureScan scan = matrix.getInstabilityHandler().scanStructureDetail();

      player.displayClientMessage(Component.translatable("message.thaumcalc.header").withStyle(ChatFormatting.DARK_PURPLE), false);

      // 1. Сколько очков есть и из чего они сложились.
      player.displayClientMessage(
         Component.translatable("message.thaumcalc.score", scan.stability(), InfusionInstabilityHandler.BASE_STABILITY)
            .withStyle(scan.stability() > InfusionInstabilityHandler.BASE_STABILITY ? ChatFormatting.GREEN : ChatFormatting.RED),
         false
      );
      player.displayClientMessage(
         Component.translatable("message.thaumcalc.breakdown",
            fmt(scan.crystalPairs()), fmt(scan.skullPairs()), fmt(scan.candlePairs())
         ).withStyle(ChatFormatting.GRAY),
         false
      );
      player.displayClientMessage(
         Component.translatable("message.thaumcalc.pedestals", scan.pairs(), scan.pedestals())
            .withStyle(scan.pedestalPenalty() == 0 ? ChatFormatting.GREEN : ChatFormatting.RED),
         false
      );
      if (scan.singles() > 0) {
         player.displayClientMessage(
            Component.translatable("message.thaumcalc.singles", scan.singles(), scan.singles() * InfusionInstabilityHandler.SINGLE_PENALTY)
               .withStyle(ChatFormatting.RED),
            false
         );
      }

      // 2. Хватает ли на текущий рецепт.
      if (matrix.getRecipeInstability() > 0) {
         int required = InfusionInstabilityHandler.requiredStability(matrix.getRecipeInstability());
         int deficit = Math.max(0, required - scan.stability());
         player.displayClientMessage(
            Component.translatable("message.thaumcalc.recipe", required, Component.translatable(tierKey(matrix.getRecipeInstability())))
               .withStyle(ChatFormatting.AQUA),
            false
         );
         player.displayClientMessage(deficitLine(deficit), false);
      } else {
         player.displayClientMessage(
            // Ключ norecipe объявляет шесть %s под сами очки, поэтому передаём именно их:
            // tierCosts() здесь свернул бы значения в один Component, и пять плейсхолдеров
            // остались бы в сообщении как литеральные «%s».
            Component.translatable("message.thaumcalc.norecipe", tierPointArgs()).withStyle(ChatFormatting.DARK_AQUA), false
         );
      }
   }

   /** «Хватает» / «Не хватает N — событие примерно раз в T сек». */
   private static Component deficitLine(int deficit) {
      if (deficit <= 0) {
         return Component.translatable("message.thaumcalc.enough").withStyle(ChatFormatting.GREEN);
      }

      double seconds = InfusionInstabilityHandler.eventIntervalSeconds(deficit);
      return Component.translatable("message.thaumcalc.shortage", deficit, fmt(seconds))
         .withStyle(deficit >= 40 ? ChatFormatting.RED : ChatFormatting.GOLD);
   }

   /** Шесть требований по классам, развёрнутые в массив — для ключа с плейсхолдерами. */
   private static Object[] tierPointArgs() {
      return new Object[]{
         InfusionInstabilityHandler.requiredStability(0), InfusionInstabilityHandler.requiredStability(2),
         InfusionInstabilityHandler.requiredStability(4), InfusionInstabilityHandler.requiredStability(6),
         InfusionInstabilityHandler.requiredStability(8), InfusionInstabilityHandler.requiredStability(10)
      };
   }

   /** Подпись класса рецепта: {@code tc.inst.N} (Negligible … Dangerous). */
   private static String tierKey(int recipeInstability) {
      return "tc.inst." + InfusionInstabilityHandler.tierOf(recipeInstability);
   }

   /** Дробные веса (2.5 за пару свечей) показываем без хвостов вида «5.0». */
   private static String fmt(float value) {
      if (value == Math.rint(value)) {
         return Integer.toString(Math.round(value));
      }
      return String.format("%.1f", value);
   }

   /** Частота событий в секундах — одна значная после запятой достаточно. */
   private static String fmt(double value) {
      return String.format("%.1f", value);
   }
}