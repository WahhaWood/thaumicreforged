package thaumcraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.block.crucible.CrucibleBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;
import thaumcraft.research.PlayerKnowledge;

/** Crucible against TC4 TileCrucible: heat sources and recipes gated by the thrower's research. */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class CrucibleGameTest {
   private static final BlockPos CRUCIBLE = new BlockPos(4, 1, 4);

   @GameTest(template = "empty_1x1x1")
   public static void onlyLavaFireAndNitorHeat(GameTestHelper helper) {
      helper.assertTrue(CrucibleBlockEntity.isHeatSource(Blocks.LAVA.defaultBlockState()), "lava heats");
      helper.assertTrue(CrucibleBlockEntity.isHeatSource(Blocks.FIRE.defaultBlockState()), "fire heats");
      helper.assertTrue(CrucibleBlockEntity.isHeatSource(ModBlocks.NITOR.get().defaultBlockState()), "nitor heats");
      helper.assertFalse(CrucibleBlockEntity.isHeatSource(Blocks.WATER.defaultBlockState()), "water must not heat");
      helper.succeed();
   }

   /** Alumentum (coal + potentia/ignis/perditio) needs the alumentum research of whoever threw the coal. */
   @GameTest(template = "empty_9x5x9")
   public static void recipeFollowsThrowerResearch(GameTestHelper helper) {
      CrucibleBlockEntity crucible = hotCrucible(helper);

      Player novice = helper.makeMockPlayer(GameType.SURVIVAL);
      crucible.attemptSmelt(thrownCoal(helper, novice));
      helper.assertTrue(crucible.aspects.getAmount(Aspect.ENERGY) >= 3, "without research the coal must only dissolve");
      helper.assertFalse(hasAlumentum(helper), "without research nothing must be crafted");

      Player adept = helper.makeMockPlayer(GameType.SURVIVAL);
      PlayerKnowledge.of(adept).getResearchCompleted().add("alumentum");
      crucible.attemptSmelt(thrownCoal(helper, adept));
      helper.assertTrue(hasAlumentum(helper), "with the thrower's research alumentum must come out");
      helper.succeed();
   }

   /** An item without a thrower (hopper, dropper) never crafts. */
   @GameTest(template = "empty_9x5x9")
   public static void itemWithoutThrowerOnlyDissolves(GameTestHelper helper) {
      CrucibleBlockEntity crucible = hotCrucible(helper);
      crucible.attemptSmelt(new ItemEntity(helper.getLevel(), 0, 0, 0, new ItemStack(Items.COAL)));
      helper.assertFalse(hasAlumentum(helper), "an item without a thrower must not craft");
      helper.succeed();
   }

   /** The crucible ejects its output as a SpecialItemEntity, which has its own entity type. */
   private static boolean hasAlumentum(GameTestHelper helper) {
      return helper.getLevel()
         .getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(CRUCIBLE)).inflate(3.0))
         .stream()
         .anyMatch(item -> item.getItem().is(ModItems.ALUMENTUM.get()));
   }

   private static CrucibleBlockEntity hotCrucible(GameTestHelper helper) {
      helper.setBlock(CRUCIBLE, ModBlocks.CRUCIBLE.get());
      CrucibleBlockEntity crucible = (CrucibleBlockEntity)helper.getBlockEntity(CRUCIBLE);
      crucible.tank.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
      crucible.heat = 200;
      crucible.aspects.add(Aspect.ENERGY, 3);
      crucible.aspects.add(Aspect.FIRE, 3);
      crucible.aspects.add(Aspect.ENTROPY, 3);
      return crucible;
   }

   private static ItemEntity thrownCoal(GameTestHelper helper, Player thrower) {
      BlockPos pos = helper.absolutePos(CRUCIBLE);
      ItemEntity coal = new ItemEntity(helper.getLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(Items.COAL));
      coal.setThrower(thrower);
      return coal;
   }
}
