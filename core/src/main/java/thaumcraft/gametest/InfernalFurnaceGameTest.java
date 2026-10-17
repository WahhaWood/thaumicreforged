package thaumcraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.gametest.GameTestHolder;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.block.infernal_furnace.InfernalFurnaceBlock;
import thaumcraft.block.infernal_furnace.InfernalFurnaceBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.WandManager;

/**
 * Адская печь: сборка жезлом, плавка и разборка.
 *
 * <p>Сборка идёт штатным путём {@code WandManager.createArcaneFurnace} — тем же кодом,
 * что и в игре. Игрок создаётся через {@code makeMockPlayer}, а не
 * {@code makeMockServerPlayerInLevel}: последний добавляет игрока в список игроков,
 * что роняет тест синхронизацией датапака ещё до первой проверки.
 */
@GameTestHolder(Thaumcraft.MODID)
public class InfernalFurnaceGameTest {

   /** Левый нижний угол конструкции; ядро сдвинуто на +1 по каждой оси. */
   private static final int BASE_X = 4;
   private static final int BASE_Y = 2;
   private static final int BASE_Z = 4;

   private static final BlockPos BASE = new BlockPos(BASE_X, BASE_Y, BASE_Z);
   private static final BlockPos CORE = new BlockPos(BASE_X + 1, BASE_Y + 1, BASE_Z + 1);

   /**
    * Точка клика жезлом. Как и в 1.7.10, сборка ищет основание перебором
    * {@code clicked - 2 .. clicked}, то есть клик должен приходиться на дальний
    * верхний угол 3×3×3, а не на ближний нижний.
    */
   private static final BlockPos CLICK = new BlockPos(BASE_X + 2, BASE_Y + 2, BASE_Z + 2);

   /**
    * Жезл превращает 3×3×3 из незеритовых кирпичей, обсидиана, лавы и решётки
    * в печь: 26 деталей с индексами 0..25, у каждой свой индекс и общий facing.
    */
   @GameTest
   public static void wandAssemblesFurnace(GameTestHelper helper) {
      buildBlueprint(helper, Direction.EAST);
      assemble(helper);

      assertIntactFurnace(helper);
      // TC4: sparkles over each of the 26 parts (event 1).
      long sparkles = BlockEventGameTest.queuedEvents(helper.getLevel())
         .stream()
         .filter(e -> e.block() == ModBlocks.INFERNAL_FURNACE.get() && e.paramA() == 1 && e.pos().closerThan(helper.absolutePos(CORE), 2.0))
         .count();
      helper.assertTrue(sparkles == 26, "assembled furnace must send 26 sparkle events, sent " + sparkles);
      clearRegion(helper);
      helper.succeed();
   }

   /**
    * The assembled furnace is visible: every outer face of the 3x3x3 is drawn, faces between two solid parts
    * are hidden, and the faces around the core are drawn. In 1.0.6.0 skipRendering hid every face of every
    * solid part and the furnace looked transparent.
    */
   @GameTest
   public static void assembledFurnaceIsVisible(GameTestHelper helper) {
      buildBlueprint(helper, Direction.EAST);
      assemble(helper);

      BlockPos core = helper.absolutePos(CORE);
      for (BlockPos pos : BlockPos.betweenClosed(core.offset(-1, -1, -1), core.offset(1, 1, 1))) {
         BlockState state = helper.getLevel().getBlockState(pos);
         if (!(state.getBlock() instanceof InfernalFurnaceBlock)) {
            continue;
         }

         for (Direction side : Direction.values()) {
            BlockPos neighbor = pos.relative(side);
            boolean outside = Math.abs(neighbor.getX() - core.getX()) > 1 || Math.abs(neighbor.getY() - core.getY()) > 1
               || Math.abs(neighbor.getZ() - core.getZ()) > 1;
            BlockState neighborState = helper.getLevel().getBlockState(neighbor);
            boolean betweenSolidParts = !outside && neighborState.getBlock() instanceof InfernalFurnaceBlock
               && state.getValue(InfernalFurnaceBlock.INDEX) != 0 && neighborState.getValue(InfernalFurnaceBlock.INDEX) != 0;
            boolean drawn = Block.shouldRenderFace(state, helper.getLevel(), pos, side, neighbor);
            helper.assertTrue(drawn != betweenSolidParts,
               "face " + side + " of part " + state.getValue(InfernalFurnaceBlock.INDEX) + " at " + pos + ": drawn=" + drawn);
         }
      }

      clearRegion(helper);
      helper.succeed();
   }

   /**
    * Углы — незеритовые кирпичи, рёбра и дно — обсидиан, отверстие — решётка.
    * Если карта индексов съехала, разборка вернёт не то, что было собрано.
    *
    * <p>Ломается дно прямо под ядром: в 1.7.10 проверку запускает только ядро
    * ({@code metadata == 0}), а хук зовут лишь у граней изменившейся клетки.
    * Дальние углы стоят к ядру по диагонали и разборку не запускают — как в оригинале.
    */
   @GameTest
   public static void dismantleRestoresOriginalBlocks(GameTestHelper helper) {
      buildBlueprint(helper, Direction.EAST);
      assemble(helper);

      helper.destroyBlock(CORE.below());
      assertRestored(helper);

      helper.succeed();
   }

   /** Разборка сработала и с ядра — core лежит в грани соседей, но не в грани угла. */
   @GameTest
   public static void dismantleByBreakingCore(GameTestHelper helper) {
      buildBlueprint(helper, Direction.EAST);
      assemble(helper);

      helper.destroyBlock(CORE);
      assertRestored(helper);

      helper.succeed();
   }

   /**
    * Печь принимает предмет в ядро и плавит его с выбросом результата.
    * Без рабочего ядра пользоваться печью нельзя вовсе — это и есть исходный баг.
    */
   @GameTest
   public static void furnaceSmeltsRawIron(GameTestHelper helper) {
      buildBlueprint(helper, Direction.EAST);
      assemble(helper);

      helper.assertTrue(
         helper.getBlockEntity(CORE) instanceof InfernalFurnaceBlockEntity,
         "в ядре обязан быть блок-энтити печи"
      );

      InfernalFurnaceBlockEntity furnace = (InfernalFurnaceBlockEntity)helper.getBlockEntity(CORE);
      helper.assertTrue(
         furnace.addItemsToInventory(new ItemStack(Items.RAW_IRON)),
         "ядро обязано принять предмет"
      );
      helper.assertValueEqual(
         furnace.getItem(0).getItem(), Items.RAW_IRON,
         "сырое железо обязано лежать в инвентаре ядра"
      );

      // Плавка идёт через тот же serverTick, что и тикер блока; рецепт vanilla — 140 тиков.
      BlockPos absoluteCore = helper.absolutePos(CORE);

      for (int tick = 0; tick < 200; tick++) {
         InfernalFurnaceBlockEntity.serverTick(helper.getLevel(), absoluteCore, helper.getBlockState(CORE), furnace);
      }

      helper.assertTrue(
         furnace.getItem(0).isEmpty(),
         "после плавки сырое железо обязано закончиться"
      );

      boolean ingotDropped = helper.getEntities(EntityType.ITEM, CORE, 4.0).stream()
         .map(ItemEntity::getItem)
         .anyMatch(stack -> stack.is(Items.IRON_INGOT));

      helper.assertTrue(ingotDropped, "печь обязана выбросить слиток в сторону решётки");
      clearRegion(helper);
      helper.succeed();
   }

   /**
    * TC4: a cluster smelts into two ingots, and the furnace throws bonus nuggets (1 in 4 per item without bellows),
    * for meat too. 40 of each: no nugget at all would happen once in 10^5 runs.
    */
   @GameTest
   public static void furnaceDoublesClustersAndThrowsBonus(GameTestHelper helper) {
      buildBlueprint(helper, Direction.EAST);
      assemble(helper);
      InfernalFurnaceBlockEntity furnace = (InfernalFurnaceBlockEntity)helper.getBlockEntity(CORE);
      helper.assertTrue(furnace.addItemsToInventory(new ItemStack(ModItems.IRON_CLUSTER.get(), 40)), "core must take the clusters");
      helper.assertTrue(furnace.addItemsToInventory(new ItemStack(Items.CHICKEN, 40)), "core must take the chicken");
      BlockPos absoluteCore = helper.absolutePos(CORE);
      for (int tick = 0; tick < 20000 && !(furnace.getItem(0).isEmpty() && furnace.getItem(1).isEmpty()); tick++) {
         InfernalFurnaceBlockEntity.serverTick(helper.getLevel(), absoluteCore, helper.getBlockState(CORE), furnace);
      }

      helper.assertTrue(furnace.getItem(0).isEmpty() && furnace.getItem(1).isEmpty(), "everything must be smelted");
      helper.assertValueEqual(countDropped(helper, Items.IRON_INGOT), 80, "iron ingots from 40 clusters");
      helper.assertValueEqual(countDropped(helper, Items.COOKED_CHICKEN), 40, "cooked chicken");
      helper.assertTrue(countDropped(helper, Items.IRON_NUGGET) > 0, "clusters must throw bonus iron nuggets");
      helper.assertTrue(countDropped(helper, ModItems.CHICKEN_NUGGET.get()) > 0, "chicken must throw bonus chicken nuggets");
      clearRegion(helper);
      helper.succeed();
   }

   private static int countDropped(GameTestHelper helper, Item item) {
      return helper.getEntities(EntityType.ITEM, CORE, 6.0).stream().map(ItemEntity::getItem).filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
   }

   private static void assemble(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      // createArcaneFurnace читает мир напрямую, а helper.setBlock ставит блоки по
      // относительным координатам площадки — поэтому клик переводим в абсолютные.
      helper.assertTrue(
         WandManager.createArcaneFurnace(chargedWand(), player, helper.getLevel(), helper.absolutePos(CLICK), Direction.UP),
         "жезл обязан собрать печь из правильной конструкции"
      );
   }

   /** Ставит 3×3×3 по чертежу 1.7.10: НБ по углам, обсидиан по рёбрам и в дне, лава в центре. */
   private static void buildBlueprint(GameTestHelper helper, Direction opening) {
      for (int dy = -1; dy <= 1; dy++) {
         for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
               boolean corner = dx != 0 && dz != 0;
               boolean center = dx == 0 && dz == 0;

               if (center && dy == 1) {
                  continue;  // дыра над ядром — в чертеже 1.7.10 тут воздух
               }

               BlockState block;

               if (center) {
                  block = dy == 0 ? Blocks.LAVA.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState();
               } else if (corner) {
                  block = Blocks.NETHER_BRICKS.defaultBlockState();
               } else if (dy == 0 && dx == opening.getStepX() && dz == opening.getStepZ()) {
                  block = Blocks.IRON_BARS.defaultBlockState();
               } else {
                  block = Blocks.OBSIDIAN.defaultBlockState();
               }

               helper.setBlock(CORE.offset(dx, dy, dz), block);
            }
         }
      }
   }

   /** Все 26 деталей на местах, с верными индексами и общим facing. */
   private static void assertIntactFurnace(GameTestHelper helper) {
      Direction opening = Direction.EAST;

      for (int dy = -1; dy <= 1; dy++) {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (dx == 0 && dz == 0 && dy == 1) {
                  continue;  // дыра над ядром
               }

               BlockPos at = CORE.offset(dx, dy, dz);
               BlockState state = helper.getBlockState(at);
               String where = " (" + dx + "," + dy + "," + dz + ")";

               helper.assertTrue(state.is(ModBlocks.INFERNAL_FURNACE.get()), "нет детали печи" + where);
               helper.assertValueEqual(
                  state.getValue(InfernalFurnaceBlock.INDEX), expectedIndex(dx, dy, dz, opening),
                  "неверный индекс детали" + where
               );
               helper.assertValueEqual(
                  state.getValue(BlockStateProperties.HORIZONTAL_FACING), opening,
                  "неверный facing детали" + where
               );
            }
         }
      }
   }

   /** После разборки не остаётся ни одной детали печи, а ядро исчезло. */
   private static void assertRestored(GameTestHelper helper) {
      helper.assertTrue(helper.getBlockState(CORE).isAir(), "ядро печи обязано исчезнуть при разборке");

      for (int dy = -1; dy <= 1; dy++) {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (dx == 0 && dz == 0 && dy == 1) {
                  continue;  // дыра над ядром
               }

               helper.assertFalse(
                  helper.getBlockState(CORE.offset(dx, dy, dz)).is(ModBlocks.INFERNAL_FURNACE.get()),
                  "деталь печи осталась после разборки (" + dx + "," + dy + "," + dz + ")"
               );
            }
         }
      }

      clearRegion(helper);
   }

   /**
    * Убирает кусок площадки после теста.
    *
    * <p>Флаги обновления не нужны: блоки ставятся в воздух молча, иначе первая же
    * замена разбудит проверку целостности и печь соберётся заново из соседей.
    */
   private static void clearRegion(GameTestHelper helper) {
      for (int dy = -1; dy <= 1; dy++) {
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

   /** Смещение детали от ядра в канонической схеме, развёрнутое на facing. */
   private static int expectedIndex(int dx, int dy, int dz, Direction opening) {
      int steps = 0;
      Direction cursor = Direction.EAST;

      while (cursor != opening) {
         cursor = cursor.getClockWise();
         steps++;
      }

      int rx = dx;
      int rz = dz;

      for (int i = 0; i < steps; i++) {
         int nextX = rz;
         int nextZ = -rx;
         rx = nextX;
         rz = nextZ;
      }

      if (dy < 0) {
         return (rz + 1) * 3 + (rx + 1) + 1;
      }

      int[][] ring = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};

      for (int i = 0; i < ring.length; i++) {
         if (ring[i][0] == rx && ring[i][1] == rz) {
            return (dy == 0 ? 10 : 18) + i;
         }
      }

      return 0;
   }

   /** Посох на серебряном древке — только он вмещает 5000 огня и 5000 земли, нужных сборке. */
   private static ItemStack chargedWand() {
      ItemStack stack = new ItemStack(ModItems.WAND_CASTING.get());
      ItemWandCasting wand = (ItemWandCasting)stack.getItem();
      wand.setRod(stack, ModItems.SILVERWOOD_WAND_ROD.get());
      wand.addVis(stack, Aspect.FIRE, 10000, true);
      wand.addVis(stack, Aspect.EARTH, 10000, true);
      return stack;
   }
}
