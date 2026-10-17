package thaumcraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.block.arcane_pedestal.ArcanePedestalBlockEntity;
import thaumcraft.block.infusion_matrix.RunicMatrixBlockEntity;
import thaumcraft.block.infusion_matrix.handlers.InfusionInstabilityHandler;
import thaumcraft.block.jar.standart.JarBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.util.ItemStackData;

/**
 * Infusion altar: catalyst-modifying recipes run end to end, runes are built into the matrix, and the
 * calculator reports the same event rate the matrix uses.
 */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class MatrixInfusionGameTest {
   private static final BlockPos MATRIX = new BlockPos(4, 3, 4);
   private static final BlockPos CENTER = new BlockPos(4, 1, 4);

   /** Goggles on a fortress helm: recipe start, essentia from jars, both items, the modified catalyst. */
   @GameTest(template = "empty_9x5x9", timeoutTicks = 600)
   public static void fortressGogglesInfuseEndToEnd(GameTestHelper helper) {
      RunicMatrixBlockEntity matrix = buildAltar(helper);
      // Speed rune keeps the test short; the stability rune keeps random events out of it.
      matrix.isUpgraded = true;
      matrix.upgrades = new byte[]{4, 0, 0, 0, 1};
      pedestal(helper, CENTER).setItem(0, new ItemStack(ModItems.THAUMIUM_FORTRESS_HELMET.get()));
      pedestal(helper, new BlockPos(4, 1, 1)).setItem(0, new ItemStack(Items.SLIME_BALL));
      pedestal(helper, new BlockPos(4, 1, 7)).setItem(0, new ItemStack(ModItems.GOGGLES_REVEALING.get()));
      jar(helper, new BlockPos(1, 1, 4), Aspect.SENSES, 32);
      jar(helper, new BlockPos(7, 1, 4), Aspect.AURA, 16);
      jar(helper, new BlockPos(1, 1, 7), Aspect.ARMOR, 16);

      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      PlayerKnowledge.of(player).getResearchCompleted().add("helm_goggles");
      BlockPos matrixPos = helper.absolutePos(MATRIX);
      helper.assertTrue(matrix.onWandRightClick(helper.getLevel(), player, InteractionHand.MAIN_HAND, matrixPos, Direction.UP) == 1, "wand must open the matrix");
      helper.assertTrue(matrix.onWandRightClick(helper.getLevel(), player, InteractionHand.MAIN_HAND, matrixPos, Direction.UP) == 1, "wand must start the craft");
      helper.assertTrue(matrix.isCrafting() && matrix.getRecipeType() == 2, "goggles must start as a catalyst-modifying recipe");

      helper.succeedWhen(() -> {
         ItemStack out = pedestal(helper, CENTER).getItem(0);
         helper.assertTrue(out.is(ModItems.THAUMIUM_FORTRESS_HELMET.get()) && ItemStackData.getBoolean(out, "goggles"), "helm must get the goggles, got " + out);
         helper.assertTrue(pedestal(helper, new BlockPos(4, 1, 1)).getItem(0).isEmpty(), "slime ball must be used");
         helper.assertFalse(matrix.isCrafting(), "craft must be over");
      });
   }

   /** A rune recipe ends by building the rune into the matrix; the catalyst is consumed and no item appears. */
   @GameTest(template = "empty_9x5x9", timeoutTicks = 100)
   public static void runeIsBuiltIntoMatrix(GameTestHelper helper) {
      RunicMatrixBlockEntity matrix = buildAltar(helper);
      matrix.isUpgraded = true;
      ItemStack rune = new ItemStack(ModItems.SPEED_RUNE.get());
      pedestal(helper, CENTER).setItem(0, rune.copy());
      ItemStack info = new ItemStack(ModItems.RUNIC_MATRIX.get());
      ItemStackData.putBoolean(info, "isInfo", true);
      ItemStackData.putByte(info, "metaUpdate", (byte)RunicMatrixBlockEntity.UPGRADE_SPEED);

      // Resume a craft that has drawn everything: only the finish step is left.
      CompoundTag tag = matrix.saveWithoutMetadata(helper.getLevel().registryAccess());
      tag.putBoolean("crafting", true);
      tag.put("recipeInput", ItemStackData.saveStack(rune));
      tag.put("recipeOutput", ItemStackData.saveStack(info));
      tag.put("recipeIngredients", new ListTag());
      tag.put("recipeEssentia", new CompoundTag());
      matrix.loadWithComponents(tag, helper.getLevel().registryAccess());

      helper.succeedWhen(() -> {
         helper.assertTrue(matrix.runeLevel(RunicMatrixBlockEntity.UPGRADE_SPEED) == 1, "speed rune must be built in");
         helper.assertTrue(pedestal(helper, CENTER).getItem(0).isEmpty(), "rune catalyst must be consumed, no item handed out");
      });
   }

   /** The calculator and the matrix share one formula: a roll every 2 s with chance (deficit + 1) / 500. */
   @GameTest(template = "empty_1x1x1")
   public static void calculatorEventRateMatchesMatrix(GameTestHelper helper) {
      helper.assertTrue(Math.abs(InfusionInstabilityHandler.eventIntervalSeconds(0) - 1000.0) < 1.0E-6, "no deficit: one event per ~1000 s");
      helper.assertTrue(Math.abs(InfusionInstabilityHandler.eventIntervalSeconds(9) - 100.0) < 1.0E-6, "deficit 9: one event per ~100 s");
      helper.assertTrue(InfusionInstabilityHandler.requiredStability(0) == 10 && InfusionInstabilityHandler.requiredStability(10) == 100, "tier costs 10..100");
      helper.succeed();
   }

   private static RunicMatrixBlockEntity buildAltar(GameTestHelper helper) {
      helper.setBlock(CENTER, ModBlocks.ARCANE_PEDESTAL.get());
      for (int dx : new int[]{-1, 1}) {
         for (int dz : new int[]{-1, 1}) {
            helper.setBlock(CENTER.offset(dx, 0, dz), ModBlocks.INFUSION_PILLAR.get());
         }
      }

      helper.setBlock(new BlockPos(4, 1, 1), ModBlocks.ARCANE_PEDESTAL.get());
      helper.setBlock(new BlockPos(4, 1, 7), ModBlocks.ARCANE_PEDESTAL.get());
      helper.setBlock(MATRIX, ModBlocks.RUNIC_MATRIX.get());
      return (RunicMatrixBlockEntity)helper.getBlockEntity(MATRIX);
   }

   private static ArcanePedestalBlockEntity pedestal(GameTestHelper helper, BlockPos pos) {
      return (ArcanePedestalBlockEntity)helper.getBlockEntity(pos);
   }

   private static void jar(GameTestHelper helper, BlockPos pos, Aspect aspect, int amount) {
      helper.setBlock(pos, ModBlocks.JAR.get());
      JarBlockEntity jar = (JarBlockEntity)helper.getBlockEntity(pos);
      jar.aspect = aspect;
      jar.amount = amount;
   }
}
