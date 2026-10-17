package thaumcraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.block.airy.aura_node.AuraNodeBlockEntity;
import thaumcraft.block.node_stabilizer.NodeStabilizerBlock;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;

/** Node stabilizers: below the node facing up (TC4) and above it facing down (E12). */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class NodeStabilizerGameTest {
   private static final BlockPos NODE = new BlockPos(4, 2, 4);
   private static final BlockPos BELOW = NODE.below();
   private static final BlockPos ABOVE = NODE.above();

   /** An advanced stabilizer hanging above the node locks it like one standing below. */
   @GameTest(template = "empty_9x5x9")
   public static void stabilizerAboveLocksNode(GameTestHelper helper) {
      AuraNodeBlockEntity node = node(helper);
      stabilizer(helper, ABOVE, ModBlocks.ADVANCED_NODE_STABILIZER.get(), Direction.DOWN);
      helper.succeedWhen(() -> helper.assertTrue(node.getLock() == 2, "advanced stabilizer above must lock 2, got " + node.getLock()));
   }

   /** Only a stabilizer facing the node counts: the advanced one above facing up is ignored, the normal one below works. */
   @GameTest(template = "empty_9x5x9")
   public static void stabilizerFacingAwayIsIgnored(GameTestHelper helper) {
      AuraNodeBlockEntity node = node(helper);
      stabilizer(helper, BELOW, ModBlocks.NODE_STABILIZER.get(), Direction.UP);
      stabilizer(helper, ABOVE, ModBlocks.ADVANCED_NODE_STABILIZER.get(), Direction.UP);
      helper.succeedWhen(() -> helper.assertTrue(node.getLock() == 1, "only the stabilizer below faces the node, lock must be 1, got " + node.getLock()));
   }

   /** Redstone switches off the stabilizer above too (TC4: a powered stabilizer does nothing). */
   @GameTest(template = "empty_9x5x9")
   public static void poweredStabilizerAboveIsOff(GameTestHelper helper) {
      AuraNodeBlockEntity node = node(helper);
      stabilizer(helper, BELOW, ModBlocks.NODE_STABILIZER.get(), Direction.UP);
      stabilizer(helper, ABOVE, ModBlocks.ADVANCED_NODE_STABILIZER.get(), Direction.DOWN);
      helper.setBlock(ABOVE.east(), Blocks.REDSTONE_BLOCK);
      helper.succeedWhen(() -> helper.assertTrue(node.getLock() == 1, "powered stabilizer above must be off, lock must be 1, got " + node.getLock()));
   }

   /** Placing picks the side the node is on, whichever face of the node was clicked; with no node it follows the clicked face. */
   @GameTest(template = "empty_9x5x9")
   public static void placementFacesTheNode(GameTestHelper helper) {
      node(helper);
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);

      place(helper, player, NODE, Direction.UP);
      assertFacing(helper, ABOVE, Direction.DOWN);
      place(helper, player, NODE, Direction.DOWN);
      assertFacing(helper, BELOW, Direction.UP);

      BlockPos ceiling = new BlockPos(1, 3, 1);
      helper.setBlock(ceiling, Blocks.STONE);
      place(helper, player, ceiling, Direction.DOWN);
      assertFacing(helper, ceiling.below(), Direction.DOWN);
      helper.succeed();
   }

   private static AuraNodeBlockEntity node(GameTestHelper helper) {
      helper.setBlock(NODE, ModBlocks.AURA_NODE.get());
      AuraNodeBlockEntity node = (AuraNodeBlockEntity)helper.getBlockEntity(NODE);
      node.setAspects(new AspectList().add(Aspect.AIR, 20));
      return node;
   }

   private static void stabilizer(GameTestHelper helper, BlockPos pos, Block block, Direction facing) {
      helper.setBlock(pos, block.defaultBlockState().setValue(NodeStabilizerBlock.FACING, facing));
   }

   /** Clicks the given face of the block at {@code relative} with an advanced stabilizer in hand. */
   private static void place(GameTestHelper helper, Player player, BlockPos relative, Direction face) {
      ItemStack stack = new ItemStack(ModItems.ADVANCED_NODE_STABILIZER.get());
      player.setItemInHand(InteractionHand.MAIN_HAND, stack);
      BlockPos pos = helper.absolutePos(relative);
      Vec3 hit = Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.2));
      stack.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(hit, face, pos, false)));
   }

   private static void assertFacing(GameTestHelper helper, BlockPos pos, Direction facing) {
      helper.assertBlockPresent(ModBlocks.ADVANCED_NODE_STABILIZER.get(), pos);
      Direction actual = helper.getBlockState(pos).getValue(NodeStabilizerBlock.FACING);
      helper.assertTrue(actual == facing, "stabilizer at " + pos + " must face " + facing + ", got " + actual);
   }
}
