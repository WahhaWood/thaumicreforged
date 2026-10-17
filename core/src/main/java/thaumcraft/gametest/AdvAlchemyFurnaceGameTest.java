package thaumcraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.block.advanced_alchemy_furnace.AdvAlchemyFurnaceBlock;
import thaumcraft.block.advanced_alchemy_furnace.AdvAlchemyFurnaceBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.WandManager;

/**
 * Продвинутая алхимическая печь: сборка жезлом и разборка при потере любой детали.
 *
 * <p>Сборка идёт штатным путём {@code WandManager.createAdvancedAlchemicalFurnace}.
 * Игрок создаётся через {@code makeMockPlayer}: {@code makeMockServerPlayerInLevel}
 * добавляет игрока в список игроков и роняет тест синхронизацией датапака.
 */
@GameTestHolder(Thaumcraft.MODID)
public class AdvAlchemyFurnaceGameTest {
   /** Ядро конструкции; блоки занимают смещения -1..1 по горизонтали и 0..1 по вертикали. */
   private static final BlockPos CORE = new BlockPos(2, 1, 2);

   /**
    * Жезл превращает печь с кольцом конструктов и козырьком из алембиков
    * в 17 деталей: ядро, 4 клапана и 4 основания снизу, 4 топки и 4 бака сверху.
    */
   @GameTest
   public static void wandAssemblesAdvancedFurnace(GameTestHelper helper) {
      buildBlueprint(helper);
      assemble(helper);
      assertAssembled(helper);
      clearRegion(helper);
      helper.succeed();
   }

   /**
    * Сломали клапан — ядро узнаёт об этом по соседству и разбирает печь сразу.
    * Это ветка {@code md == 0} оригинального {@code func_149749_a}.
    */
   @GameTest
   public static void dismantleByBreakingValve(GameTestHelper helper) {
      buildBlueprint(helper);
      assemble(helper);

      // Клапан стоит вплотную к ядру, поэтому сообщение о смене блока получает именно ядро.
      helper.destroyBlock(CORE.east());
      assertRestored(helper, 1, 0, 0);
      helper.succeed();
   }

   /**
    * Сломали бак — он стоит над основанием по диагонали и с ядром гранями не соседствует,
    * поэтому разборку запускает основание снизу, а оно лишь ставит ядру флаг
    * {@code destroy} (ветка {@code md != 0} оригинала). Разбор происходит на тике.
    */
   @GameTest
   public static void dismantleByBreakingTank(GameTestHelper helper) {
      buildBlueprint(helper);
      assemble(helper);

      helper.destroyBlock(CORE.east().south().above());

      helper.assertTrue(
         helper.getBlockEntity(CORE) instanceof AdvAlchemyFurnaceBlockEntity,
         "в ядре обязан быть блок-энтити печи"
      );

      AdvAlchemyFurnaceBlockEntity tile = (AdvAlchemyFurnaceBlockEntity)helper.getBlockEntity(CORE);
      tile.serverTick(helper.getLevel(), helper.absolutePos(CORE), helper.getBlockState(CORE));
      assertRestored(helper, 1, 1, 1);
      helper.succeed();
   }

   /** Сломали само ядро — печь рассыпается. */
   @GameTest
   public static void dismantleByBreakingCore(GameTestHelper helper) {
      buildBlueprint(helper);
      assemble(helper);

      helper.destroyBlock(CORE);
      assertRestored(helper, 0, 0, 0);
      helper.succeed();
   }

   private static void assemble(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      // createAdvancedAlchemicalFurnace читает мир напрямую, а helper.setBlock ставит блоки по
      // относительным координатам площадки — поэтому клик переводим в абсолютные.
      helper.assertTrue(
         WandManager.createAdvancedAlchemicalFurnace(chargedWand(), player, helper.getLevel(), helper.absolutePos(CORE), Direction.UP),
         "жезл обязан собрать продвинутую печь из правильной конструкции"
      );
   }

   /**
    * Чертеж, который проверяет {@code WandManager.fitAdvancedAlchemicalFurnace}:
    * печь в центре, кольцо конструктов в нижнем слое, сверху по диагоналям алембики,
    * по рёбрам — простые конструкты.
    */
   private static void buildBlueprint(GameTestHelper helper) {
      helper.setBlock(CORE, ModBlocks.ALCHEMY_FURNACE.get().defaultBlockState());

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            if (dx == 0 && dz == 0) {
               continue;
            }

            helper.setBlock(CORE.offset(dx, 0, dz), ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get().defaultBlockState());
            helper.setBlock(
               CORE.offset(dx, 1, dz),
               (dx != 0 && dz != 0 ? ModBlocks.ARCANE_ALEMBIC : ModBlocks.ALCHEMICAL_CONSTRUCT).get().defaultBlockState()
            );
         }
      }
   }

   /** Все 17 деталей на местах и с теми ролями, которые задаёт сборка. */
   private static void assertAssembled(GameTestHelper helper) {
      helper.assertTrue(
         helper.getBlockState(CORE).is(ModBlocks.ADVANCED_ALCHEMY_FURNACE.get()),
         "нет ядра печи, а стоит " + helper.getBlockState(CORE).getBlock()
      );
      helper.assertValueEqual(
         helper.getBlockState(CORE).getValue(AdvAlchemyFurnaceBlock.ELEMENT), AdvAlchemyFurnaceBlock.Element.CENTER,
         "ядро должно быть элементом CENTER"
      );

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            if (dx == 0 && dz == 0) {
               continue;
            }

            String where = " (" + dx + ",0," + dz + ")";
            boolean diagonal = dx != 0 && dz != 0;
            BlockPos lower = CORE.offset(dx, 0, dz);
            BlockPos upper = CORE.offset(dx, 1, dz);

            helper.assertTrue(
               helper.getBlockState(lower).is(ModBlocks.ADVANCED_ALCHEMY_FURNACE.get()),
               "нет нижней детали печи" + where
            );
            helper.assertValueEqual(
               helper.getBlockState(lower).getValue(AdvAlchemyFurnaceBlock.ELEMENT),
               diagonal ? AdvAlchemyFurnaceBlock.Element.BASE : AdvAlchemyFurnaceBlock.Element.VALVE,
               "неверная роль нижней детали" + where
            );
            helper.assertTrue(
               helper.getBlockState(upper).is(
                  diagonal ? ModBlocks.ADVANCED_ALCHEMY_FURNACE_TANK.get() : ModBlocks.ADVANCED_ALCHEMY_FURNACE_BURN.get()
               ),
               "нет верхней детали печи" + where
            );
         }
      }
   }

   /**
    * После разборки не остаётся ни одной детали печи, а вместо них стоят ровно те блоки,
    * из которых печь собиралась: печь, конструкты, простые конструкты и алембики.
    *
    * <p>Ячейка, из которой игрок выбил блок, остаётся воздухом — ровно как в оригинале
    * ({@code TileAlchemyFurnaceAdvanced.func_145845_h} меняет лишь позиции, где блок печи
    * ещё стоит, и потому выбитый блок не воскресает). Если выбито ядро, оно и остаётся
    * пустым: {@code restoreParts} его восстанавливает только когда оно ещё на месте.
    */
   private static void assertRestored(GameTestHelper helper, int holeX, int holeY, int holeZ) {
      boolean coreBroken = holeX == 0 && holeY == 0 && holeZ == 0;
      helper.assertValueEqual(
         helper.getBlockState(CORE).is(ModBlocks.ALCHEMY_FURNACE.get()), !coreBroken,
         coreBroken
            ? "выбитое ядро не должно воскресать"
            : "ядро после разборки обязано вернуться печью"
      );

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            if (dx == 0 && dz == 0) {
               continue;
            }

            boolean diagonal = dx != 0 && dz != 0;
            String where = " (" + dx + "," + dz + ")";
            boolean lowerHole = holeX == dx && holeY == 0 && holeZ == dz;
            boolean upperHole = holeX == dx && holeY == 1 && holeZ == dz;

            if (lowerHole || upperHole) {
               helper.assertTrue(
                  helper.getBlockState(CORE.offset(dx, holeY, dz)).isAir(),
                  "выбитая деталь не должна воскресать" + where
               );
               continue;
            }

            helper.assertFalse(
               helper.getBlockState(CORE.offset(dx, 0, dz)).is(ModBlocks.ADVANCED_ALCHEMY_FURNACE.get()),
               "нижняя деталь печи осталась после разборки" + where
            );
            helper.assertTrue(
               helper.getBlockState(CORE.offset(dx, 0, dz)).is(ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get()),
               "нижняя деталь после разборки обязана стать конструктом" + where
            );
            helper.assertFalse(
               helper.getBlockState(CORE.offset(dx, 1, dz)).is(ModBlocks.ADVANCED_ALCHEMY_FURNACE_TANK.get()),
               "верхняя деталь печи осталась после разборки" + where
            );
            helper.assertTrue(
               helper.getBlockState(CORE.offset(dx, 1, dz)).is(
                  (diagonal ? ModBlocks.ARCANE_ALEMBIC : ModBlocks.ALCHEMICAL_CONSTRUCT).get()
               ),
               "верхняя деталь после разборки обязана вернуться как в чертеже" + where
            );
         }
      }

      clearRegion(helper);
   }

   /**
    * Убирает кусок площадки после теста.
    *
    * <p>Ядро снимается первым: пока оно стоит, снятие любой соседней детали разбудит
    * разборку, и печь вернётся на место. Обратный порядок оставил бы на площадке
    * собранную конструкцию.
    */
   private static void clearRegion(GameTestHelper helper) {
      for (int dy = 0; dy <= 1; dy++) {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               helper.getLevel().setBlock(
                  helper.absolutePos(CORE.offset(dx, dy, dz)),
                  Blocks.AIR.defaultBlockState(),
                  Block.UPDATE_CLIENTS
               );
            }
         }
      }
   }

   /** Посох на серебряном древке — только он вмещает нужные для сборки огонь, воду и порядок. */
   private static ItemStack chargedWand() {
      ItemStack stack = new ItemStack(ModItems.WAND_CASTING.get());
      ItemWandCasting wand = (ItemWandCasting)stack.getItem();
      wand.setRod(stack, ModItems.SILVERWOOD_WAND_ROD.get());
      wand.addVis(stack, Aspect.FIRE, 10000, true);
      wand.addVis(stack, Aspect.WATER, 10000, true);
      wand.addVis(stack, Aspect.ORDER, 10000, true);
      return stack;
   }
}