package thaumcraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.block.arcane_alembic.ArcaneAlembicBlockEntity;
import thaumcraft.block.jar.standart.JarBlockEntity;
import thaumcraft.block.tube.TubeBlockEntity;
import thaumcraft.block.tube.buffer.TubeBufferBlockEntity;
import thaumcraft.block.tube.filter.TileTubeFilter;
import thaumcraft.block.tube.valve.TubeValveBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.wands.ItemWandCasting;

/**
 * TC4 essentia tubes: an alembic of ignis at the west end, tubes along x, a jar under the east end. The tube in the middle
 * (x = 3) is the one under test.
 */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class EssentiaTubeGameTest {
   private static final BlockPos ALEMBIC = new BlockPos(1, 2, 4);
   private static final BlockPos MIDDLE = new BlockPos(3, 2, 4);
   private static final BlockPos LAST = new BlockPos(5, 2, 4);
   private static final BlockPos JAR = LAST.below();

   @GameTest(template = "empty_9x5x9")
   public static void plainLineFillsJar(GameTestHelper helper) {
      line(helper, ModBlocks.ESSENCE_TUBE.get());
      helper.succeedWhen(() -> assertJarHasIgnis(helper));
   }

   /** TC4 TileTubeFilter: suction only for its aspect, so ignis stays put behind an aqua filter until it is set to ignis. */
   @GameTest(template = "empty_9x5x9", timeoutTicks = 300)
   public static void filterTubePassesOnlyItsAspect(GameTestHelper helper) {
      line(helper, ModBlocks.FILTERED_ESSENCE_TUBE.get());
      TileTubeFilter filter = (TileTubeFilter)helper.getBlockEntity(MIDDLE);
      filter.setFilter(Aspect.WATER);
      helper.startSequence()
         .thenIdle(100)
         .thenExecute(() -> {
            assertJarEmpty(helper, "aqua filter must hold ignis back");
            filter.setFilter(Aspect.FIRE);
         })
         .thenWaitUntil(() -> assertJarHasIgnis(helper))
         .thenSucceed();
   }

   /** TC4 TileTubeRestrict: half the suction instead of one less (jar 32 -> tube 31 -> restricted 15). */
   @GameTest(template = "empty_9x5x9")
   public static void restrictedTubeHalvesSuction(GameTestHelper helper) {
      line(helper, ModBlocks.ESSENCE_TUBE.get());
      helper.setBlock(LAST.west(), ModBlocks.RESTRICTED_ESSENCE_TUBE.get());
      TubeBlockEntity restricted = (TubeBlockEntity)helper.getBlockEntity(LAST.west());
      helper.succeedWhen(() -> {
         helper.assertTrue(restricted.getSuctionAmount(null) == 15, "restricted tube must pass on half of 31, got " + restricted.getSuctionAmount(null));
         assertJarHasIgnis(helper);
      });
   }

   /** TC4 TileTubeOneway: facing back toward the source, essentia passes... */
   @GameTest(template = "empty_9x5x9")
   public static void onewayTubePassesWithItsFacing(GameTestHelper helper) {
      line(helper, ModBlocks.DIRECTIONAL_ESSENCE_TUBE.get());
      ((TubeBlockEntity)helper.getBlockEntity(MIDDLE)).setFacing(Direction.WEST);
      helper.succeedWhen(() -> assertJarHasIgnis(helper));
   }

   /** ...and turned the other way it lets nothing through. */
   @GameTest(template = "empty_9x5x9", timeoutTicks = 200)
   public static void onewayTubeBlocksAgainstItsFacing(GameTestHelper helper) {
      line(helper, ModBlocks.DIRECTIONAL_ESSENCE_TUBE.get());
      ((TubeBlockEntity)helper.getBlockEntity(MIDDLE)).setFacing(Direction.EAST);
      helper.runAfterDelay(150, () -> {
         assertJarEmpty(helper, "one-way tube turned against the flow must block it");
         // Its suction comes only from behind, so nothing upstream even starts to pull.
         ArcaneAlembicBlockEntity alembic = (ArcaneAlembicBlockEntity)helper.getBlockEntity(ALEMBIC);
         helper.assertTrue(alembic.amount == 16, "nothing may be drawn toward a one-way tube's wrong side, alembic has " + alembic.amount);
         helper.succeed();
      });
   }

   /** TC4 TileTubeValve: a redstone signal shuts the valve, losing it opens it again. */
   @GameTest(template = "empty_9x5x9", timeoutTicks = 300)
   public static void valveShutByRedstone(GameTestHelper helper) {
      line(helper, ModBlocks.ESSENCE_VALVE.get());
      BlockPos power = MIDDLE.south();
      helper.setBlock(power, Blocks.REDSTONE_BLOCK);
      helper.startSequence()
         .thenIdle(100)
         .thenExecute(() -> {
            assertJarEmpty(helper, "powered valve must be shut");
            helper.setBlock(power, Blocks.AIR);
         })
         .thenWaitUntil(() -> assertJarHasIgnis(helper))
         .thenSucceed();
   }

   /** TC4 BlockTube.onBlockActivated: a click with anything but a wand turns the valve by hand. */
   @GameTest(template = "empty_9x5x9")
   public static void valveTurnedByHand(GameTestHelper helper) {
      line(helper, ModBlocks.ESSENCE_VALVE.get());
      TubeValveBlockEntity valve = (TubeValveBlockEntity)helper.getBlockEntity(MIDDLE);
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      use(helper, player, MIDDLE, ItemStack.EMPTY);
      helper.assertTrue(!valve.allowFlow, "first click must shut the valve");
      use(helper, player, MIDDLE, ItemStack.EMPTY);
      helper.assertTrue(valve.allowFlow, "second click must open it");
      helper.succeed();
   }

   /** TC4 TileTube.onWandRightClick: the wand on an arm shuts that side on both tubes and stops the flow; again opens it. */
   @GameTest(template = "empty_9x5x9", timeoutTicks = 300)
   public static void wandShutsAndOpensTubeSide(GameTestHelper helper) {
      line(helper, ModBlocks.ESSENCE_TUBE.get());
      TubeBlockEntity middle = (TubeBlockEntity)helper.getBlockEntity(MIDDLE);
      TubeBlockEntity next = (TubeBlockEntity)helper.getBlockEntity(MIDDLE.east());
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      wand(helper, player, MIDDLE, new Vec3(0.9, 0.5, 0.5), Direction.EAST);
      helper.assertTrue(!middle.openSides[Direction.EAST.get3DDataValue()], "clicked arm must shut");
      helper.assertTrue(!next.openSides[Direction.WEST.get3DDataValue()], "the tube beyond must shut its side too");
      helper.startSequence()
         .thenIdle(100)
         .thenExecute(() -> {
            assertJarEmpty(helper, "shut side must stop the flow");
            // The shut arm is gone from the shape, so the click lands on the core face toward the neighbour.
            wand(helper, player, MIDDLE, new Vec3(0.75, 0.5, 0.5), Direction.EAST);
            helper.assertTrue(middle.openSides[Direction.EAST.get3DDataValue()], "core face toward a tube must reopen the side");
         })
         .thenWaitUntil(() -> assertJarHasIgnis(helper))
         .thenSucceed();
   }

   /** The middle of a tube turns it to face the next side that leads to a transport (TC4 subHit 6). */
   @GameTest(template = "empty_9x5x9")
   public static void wandOnTubeMiddleTurnsIt(GameTestHelper helper) {
      line(helper, ModBlocks.DIRECTIONAL_ESSENCE_TUBE.get());
      TubeBlockEntity middle = (TubeBlockEntity)helper.getBlockEntity(MIDDLE);
      middle.setFacing(Direction.NORTH);
      wand(helper, helper.makeMockPlayer(GameType.SURVIVAL), MIDDLE, new Vec3(0.5, 0.75, 0.5), Direction.UP);
      helper.assertTrue(middle.facing == Direction.WEST, "north -> next with a transport behind is west, got " + middle.facing);
      helper.succeed();
   }

   /** TC4 TileTubeBuffer.onWandRightClick: sneaking cycles the hold-back of a side (0, 1, 2), otherwise it opens or shuts. */
   @GameTest(template = "empty_9x5x9")
   public static void wandConfiguresBuffer(GameTestHelper helper) {
      line(helper, ModBlocks.ESSENCE_BUFFER.get());
      TubeBufferBlockEntity buffer = (TubeBufferBlockEntity)helper.getBlockEntity(MIDDLE);
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      player.setShiftKeyDown(true);
      int east = Direction.EAST.get3DDataValue();
      wand(helper, player, MIDDLE, new Vec3(0.9, 0.5, 0.5), Direction.EAST);
      helper.assertTrue(buffer.chokedSides[east] == 1, "one sneaking click must hold the side back, got " + buffer.chokedSides[east]);
      wand(helper, player, MIDDLE, new Vec3(0.9, 0.5, 0.5), Direction.EAST);
      helper.assertTrue(buffer.chokedSides[east] == 2, "two must block it, got " + buffer.chokedSides[east]);
      player.setShiftKeyDown(false);
      wand(helper, player, MIDDLE, new Vec3(0.9, 0.5, 0.5), Direction.EAST);
      helper.assertTrue(!buffer.openSides[east], "a plain click must shut the side");
      TubeBlockEntity next = (TubeBlockEntity)helper.getBlockEntity(MIDDLE.east());
      helper.assertTrue(!next.openSides[Direction.WEST.get3DDataValue()], "the tube beyond must shut its side too");
      helper.succeed();
   }

   /** TC4 filter: a label with an aspect sets an empty filter and is used up; sneaking takes it out as a blank label. */
   @GameTest(template = "empty_9x5x9")
   public static void labelSetsAndClearsFilter(GameTestHelper helper) {
      line(helper, ModBlocks.FILTERED_ESSENCE_TUBE.get());
      TileTubeFilter filter = (TileTubeFilter)helper.getBlockEntity(MIDDLE);
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      ItemStack label = new ItemStack(ModItems.JAR_LABEL.get());
      ((IEssenceContainerItem)label.getItem()).setAspects(label, new AspectList().add(Aspect.FIRE, 1));
      use(helper, player, MIDDLE, label);
      helper.assertTrue(filter.aspectFilter == Aspect.FIRE, "label must set the filter to ignis, got " + filter.aspectFilter);
      helper.assertTrue(label.isEmpty(), "the label must be used up");
      player.setShiftKeyDown(true);
      use(helper, player, MIDDLE, ItemStack.EMPTY);
      helper.assertTrue(filter.aspectFilter == null, "sneaking must clear the filter");
      boolean dropped = !helper.getLevel()
         .getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(MIDDLE)).inflate(2.0), e -> e.getItem().is(ModItems.JAR_LABEL.get()))
         .isEmpty();
      helper.assertTrue(dropped, "the label must come back out");
      helper.succeed();
   }

   /** Alembic of 16 ignis, then {@code middle} at x = 3 between plain tubes, ending above an empty jar. */
   private static void line(GameTestHelper helper, Block middle) {
      helper.setBlock(ALEMBIC, ModBlocks.ARCANE_ALEMBIC.get());
      ArcaneAlembicBlockEntity alembic = (ArcaneAlembicBlockEntity)helper.getBlockEntity(ALEMBIC);
      alembic.aspect = Aspect.FIRE;
      alembic.amount = 16;
      helper.setBlock(JAR, ModBlocks.JAR.get());
      for (int x = 2; x <= 5; x++) {
         helper.setBlock(new BlockPos(x, 2, 4), x == 3 ? middle : ModBlocks.ESSENCE_TUBE.get());
      }
   }

   private static void assertJarHasIgnis(GameTestHelper helper) {
      JarBlockEntity jar = (JarBlockEntity)helper.getBlockEntity(JAR);
      helper.assertTrue(jar.aspect == Aspect.FIRE && jar.amount > 0, "jar must fill with ignis, has " + jar.amount + " " + jar.aspect);
   }

   private static void assertJarEmpty(GameTestHelper helper, String message) {
      JarBlockEntity jar = (JarBlockEntity)helper.getBlockEntity(JAR);
      helper.assertTrue(jar.amount == 0, message + ", jar has " + jar.amount);
   }

   private static void use(GameTestHelper helper, Player player, BlockPos relative, ItemStack stack) {
      player.setItemInHand(InteractionHand.MAIN_HAND, stack);
      BlockPos pos = helper.absolutePos(relative);
      helper.getBlockState(relative)
         .useItemOn(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.25, 0), Direction.UP, pos, false));
   }

   /** Wand click at {@code local} (block-relative) on {@code relative}. */
   private static void wand(GameTestHelper helper, Player player, BlockPos relative, Vec3 local, Direction face) {
      ItemStack wand = ItemWandCasting.createWand(ModItems.WOOD_WAND_ROD.get(), ModItems.IRON_WAND_CAP.get());
      player.setItemInHand(InteractionHand.MAIN_HAND, wand);
      BlockPos pos = helper.absolutePos(relative);
      Vec3 hit = Vec3.atLowerCornerOf(pos).add(local);
      wand.getItem().onItemUseFirst(wand, new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(hit, face, pos, false)));
   }
}
