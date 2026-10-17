package thaumcraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.block.arcane_alembic.ArcaneAlembicBlockEntity;
import thaumcraft.block.thaumatorium.ThaumatoriumBlock;
import thaumcraft.block.thaumatorium.ThaumatoriumBlockEntity;
import thaumcraft.block.thaumatorium.ThaumatoriumMenu;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.WandManager;
import thaumcraft.research.PlayerKnowledge;

/**
 * Тауматорий: сборка жезлом и разборка при потере любой части.
 *
 * <p>Колонна двухблочная (оригинал ставит md 10 и md 11): центр над тиглем и верх над центром.
 * Портирована сюда вся верхняя половина — из-за её отсутствия модель
 * {@code thaumatorium_top} не использовалась нигде, а {@code ThaumatoriumBlockEntity.getMaster}
 * не доходил до ветки TOP.
 */
@GameTestHolder(Thaumcraft.MODID)
public class ThaumatoriumGameTest {
   /** Нижняя конструкция колонны — то, что жезл превращает в центр тауматория. */
   private static final BlockPos BASE = new BlockPos(2, 2, 2);

   private static final BlockPos CRUCIBLE = BASE.below();
   private static final BlockPos TOP = BASE.above();

   /**
    * Жезл превращает две алхимические конструкции, стоящие на тигле, в двухблочную колонну.
    * Проверяет и высоту (верхняя половина обязана появиться), и facing обеих половин.
    */
   @GameTest
   public static void wandAssemblesThaumatorium(GameTestHelper helper) {
      buildBlueprint(helper);
      assemble(helper);

      helper.assertTrue(helper.getBlockState(BASE).is(ModBlocks.THAUMATORIUM.get()), "нет нижней половины тауматория");
      helper.assertTrue(
         helper.getBlockState(TOP).is(ModBlocks.THAUMATORIUM.get()),
         "нет верхней половины тауматория, а стоит " + helper.getBlockState(TOP).getBlock()
      );
      helper.assertValueEqual(
         helper.getBlockState(BASE).getValue(ThaumatoriumBlock.ELEMENT), ThaumatoriumBlock.Element.CENTER,
         "нижняя половина обязана быть элементом CENTER"
      );
      helper.assertValueEqual(
         helper.getBlockState(TOP).getValue(ThaumatoriumBlock.ELEMENT), ThaumatoriumBlock.Element.TOP,
         "верхняя половина обязана быть элементом TOP"
      );
      helper.assertValueEqual(
         helper.getBlockState(BASE).getValue(BlockStateProperties.HORIZONTAL_FACING), Direction.NORTH,
         "неверный facing нижней половины"
      );
      helper.assertValueEqual(
         helper.getBlockState(TOP).getValue(BlockStateProperties.HORIZONTAL_FACING), Direction.NORTH,
         "неверный facing верхней половины"
      );
      clearRegion(helper);
      helper.succeed();
   }

   /**
    * Сломали тигель-основание — колонна без него не тауматорий.
    * Это основная проверка оригинала: md 10 требует md 0 под собой.
    */
   @GameTest
   public static void dismantleByBreakingCrucible(GameTestHelper helper) {
      buildBlueprint(helper);
      assemble(helper);

      helper.destroyBlock(CRUCIBLE);
      assertRolledBack(helper, false, false);
      helper.succeed();
   }

   /** Сломали верхнюю половину — центр без неё тоже рассыпается. */
   @GameTest
   public static void dismantleByBreakingTop(GameTestHelper helper) {
      buildBlueprint(helper);
      assemble(helper);

      helper.destroyBlock(TOP);
      assertRolledBack(helper, false, true);
      helper.succeed();
   }

   /**
    * Сломали центр — верхняя половина не должна остаться висеть одна.
    * Уведомления до верхней половины не дойдут: она узнаёт о разборе из
    * {@code onRemove} центра, который откатывает её явно.
    */
   @GameTest
   public static void dismantleByBreakingCenter(GameTestHelper helper) {
      buildBlueprint(helper);
      assemble(helper);

      helper.destroyBlock(BASE);
      assertRolledBack(helper, true, false);
      helper.succeed();
   }

   /**
    * TC4 TileThaumatorium: heated from below, with a recipe chosen and its catalyst inside, it draws the recipe's essentia
    * from a neighbour (here an alembic of praecantatio) and throws the result out of its front.
    */
   @GameTest(timeoutTicks = 400)
   @SuppressWarnings("unchecked")
   public static void thaumatoriumCraftsWithDrawnEssentia(GameTestHelper helper) {
      buildBlueprint(helper);
      assemble(helper);
      helper.setBlock(CRUCIBLE.below(), Blocks.LAVA);
      ThaumatoriumBlockEntity thaumatorium = (ThaumatoriumBlockEntity)helper.getBlockEntity(BASE);
      var recipe = helper.getLevel().getRecipeManager().byKey(Thaumcraft.location("crucible/thaumium_ingot")).orElseThrow();
      thaumatorium.toggleRecipeSelection((RecipeHolder<CrucibleRecipe>)(RecipeHolder<?>)recipe);
      thaumatorium.setItem(0, new ItemStack(Items.IRON_INGOT));
      helper.setBlock(BASE.east(), ModBlocks.ARCANE_ALEMBIC.get());
      ArcaneAlembicBlockEntity alembic = (ArcaneAlembicBlockEntity)helper.getBlockEntity(BASE.east());
      alembic.aspect = Aspect.MAGIC;
      alembic.amount = 8;
      helper.succeedWhen(() -> {
         boolean made = !helper.getLevel()
            .getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(BASE)).inflate(3.0), e -> e.getItem().is(ModItems.THAUMIUM_INGOT.get()))
            .isEmpty();
         helper.assertTrue(made, "thaumatorium must make a thaumium ingot");
         helper.assertTrue(alembic.amount == 4, "it must have drawn exactly 4 praecantatio, alembic has " + alembic.amount);
      });
   }

   /**
    * TC4 ContainerThaumatorium.enchantItem: a click on the recipe in the screen reaches the server as a menu button and
    * toggles that recipe; the chosen recipe then crafts. The screen sent the click, but the menu had no handler.
    */
   @GameTest(timeoutTicks = 400)
   public static void recipeChosenInMenuCrafts(GameTestHelper helper) {
      buildBlueprint(helper);
      assemble(helper);
      helper.setBlock(CRUCIBLE.below(), Blocks.LAVA);
      ThaumatoriumBlockEntity thaumatorium = (ThaumatoriumBlockEntity)helper.getBlockEntity(BASE);
      thaumatorium.setItem(0, new ItemStack(Items.IRON_INGOT));
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      PlayerKnowledge.of(player).getResearchCompleted().add("thaumium");
      ThaumatoriumMenu menu = new ThaumatoriumMenu(0, player.getInventory(), thaumatorium);
      ResourceLocation thaumium = Thaumcraft.location("crucible/thaumium_ingot");
      int index = -1;
      for (int i = 0; i < menu.getRecipeCount(); i++) {
         if (menu.getRecipeId(i).equals(thaumium)) {
            index = i;
         }
      }

      helper.assertTrue(index >= 0, "the menu must offer the thaumium ingot for an iron ingot, offers " + menu.getRecipes());
      helper.assertFalse(menu.clickMenuButton(player, menu.getRecipeCount()), "a button past the list must be refused");
      helper.assertTrue(menu.clickMenuButton(player, index), "the recipe button must be handled");
      helper.assertTrue(thaumatorium.isRecipeSelected(thaumium), "the click must choose the recipe");
      menu.clickMenuButton(player, index);
      helper.assertFalse(thaumatorium.isRecipeSelected(thaumium), "a second click must drop the recipe");
      menu.clickMenuButton(player, index);
      CompoundTag saved = new CompoundTag();
      thaumatorium.saveCustom(saved);
      helper.assertValueEqual(
         saved.getList("recipes", 10).getCompound(0).getString("player"), player.getName().getString(),
         "the thaumatorium must remember who chose the recipe (TC4 recipePlayer)"
      );
      helper.setBlock(BASE.east(), ModBlocks.ARCANE_ALEMBIC.get());
      ArcaneAlembicBlockEntity alembic = (ArcaneAlembicBlockEntity)helper.getBlockEntity(BASE.east());
      alembic.aspect = Aspect.MAGIC;
      alembic.amount = 8;
      helper.succeedWhen(() -> {
         boolean made = !helper.getLevel()
            .getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(BASE)).inflate(3.0), e -> e.getItem().is(ModItems.THAUMIUM_INGOT.get()))
            .isEmpty();
         helper.assertTrue(made, "the recipe chosen in the menu must make a thaumium ingot");
      });
   }

   private static void assemble(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      // createThaumatorium читает мир напрямую, а helper.setBlock ставит блоки по
      // относительным координатам площадки — поэтому клик переводим в абсолютные.
      helper.assertTrue(
         WandManager.createThaumatorium(
            chargedWand(), player, helper.getLevel(), helper.absolutePos(BASE), Direction.NORTH
         ),
         "жезл обязан собрать тауматорий из двух конструкций на тигле"
      );
   }

   /** Тигель снизу, две алхимические конструкции колонкой над ним. */
   private static void buildBlueprint(GameTestHelper helper) {
      helper.setBlock(CRUCIBLE, ModBlocks.CRUCIBLE.get().defaultBlockState());
      helper.setBlock(BASE, ModBlocks.ALCHEMICAL_CONSTRUCT.get().defaultBlockState());
      helper.setBlock(TOP, ModBlocks.ALCHEMICAL_CONSTRUCT.get().defaultBlockState());
   }

   /**
    * Половины, которые остались на месте, вернулись в обычную алхимическую конструкцию —
    * оригинальный {@code setBlock(this, 9, 3)}. Выбитая игроком половина остаётся дырой:
    * откатывается лишь та блок-состояние, которая ещё стоит, поэтому снесённый блок
    * не воскресает.
    */
   private static void assertRolledBack(GameTestHelper helper, boolean baseHole, boolean topHole) {
      assertRolledBackAt(helper, BASE, baseHole, "нижняя половина после разборки обязана стать конструктом");
      assertRolledBackAt(helper, TOP, topHole, "верхняя половина после разборки обязана стать конструктом");
      clearRegion(helper);
   }

   private static void assertRolledBackAt(GameTestHelper helper, BlockPos pos, boolean hole, String message) {
      String where = " (" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")";

      if (hole) {
         helper.assertTrue(helper.getBlockState(pos).isAir(), "выбитая половина не должна воскресать" + where);
         return;
      }

      helper.assertFalse(helper.getBlockState(pos).is(ModBlocks.THAUMATORIUM.get()), message + where);
      helper.assertTrue(helper.getBlockState(pos).is(ModBlocks.ALCHEMICAL_CONSTRUCT.get()), message + where);
   }

   private static void assertConstruct(GameTestHelper helper, BlockPos pos, String message) {
      helper.assertFalse(
         helper.getBlockState(pos).is(ModBlocks.THAUMATORIUM.get()),
         message + " (" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")"
      );
      helper.assertTrue(
         helper.getBlockState(pos).is(ModBlocks.ALCHEMICAL_CONSTRUCT.get()),
         message + " (" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")"
      );
   }

   /**
    * Убирает площадку после теста.
    *
    * <p>Верхняя половина снимается первой: пока стоит центр, снятие соседа разбудит
    * проверку структуры и откатит колонну, и на месте теста остался бы конструк��.
    */
   private static void clearRegion(GameTestHelper helper) {
      for (BlockPos pos : new BlockPos[]{TOP, BASE, CRUCIBLE}) {
         helper.getLevel().setBlock(
            helper.absolutePos(pos), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS
         );
      }
   }

   /** Посох на серебряном древке — вмещает огонь, порядок и воду, нужные для сборки. */
   private static ItemStack chargedWand() {
      ItemStack stack = new ItemStack(ModItems.WAND_CASTING.get());
      ItemWandCasting wand = (ItemWandCasting)stack.getItem();
      wand.setRod(stack, ModItems.SILVERWOOD_WAND_ROD.get());
      wand.addVis(stack, Aspect.FIRE, 10000, true);
      wand.addVis(stack, Aspect.ORDER, 10000, true);
      wand.addVis(stack, Aspect.WATER, 10000, true);
      return stack;
   }
}