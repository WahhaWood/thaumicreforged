package thaumcraft.gametest;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ScanResult;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.block.airy.aura_node.AuraNodeBlockEntity;
import thaumcraft.block.airy.energized_aura.NodeEnergizedBlockEntity;
import thaumcraft.block.node_stabilizer.NodeStabilizerBlock;
import thaumcraft.block.node_transducer.NodeTransducerBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.aura_node.PresetAuraNodeItem;
import thaumcraft.research.ScanManager;

/**
 * TC4 vis power (VISPOWER): a stabilizer below, a node transducer above and a redstone signal turn a node into an
 * energized one (TileNodeConverter), which needs both to stay whole (BlockAiry meta 5) and feeds the vis network.
 */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class EnergizedNodeGameTest {
   private static final BlockPos STABILIZER = new BlockPos(4, 1, 4);
   private static final BlockPos NODE = STABILIZER.above();
   private static final BlockPos TRANSDUCER = NODE.above();
   /** Explosion tests run in a big arena so the flux spill (up to 7 blocks out) stays inside. */
   private static final BlockPos BIG_NODE = new BlockPos(8, 4, 8);

   /** The whole player path: the transducer drains and converts the node, a relay links to it, a consumer draws vis. */
   @GameTest(template = "empty_9x5x9", timeoutTicks = 1400)
   public static void transducerEnergizesNodeThatFeedsRelay(GameTestHelper helper) {
      stabilizer(helper, STABILIZER, Direction.UP);
      helper.setBlock(NODE, ModBlocks.AURA_NODE.get());
      AuraNodeBlockEntity node = (AuraNodeBlockEntity)helper.getBlockEntity(NODE);
      node.setAspects(new AspectList().add(Aspect.FIRE, 25).add(Aspect.AIR, 16));
      helper.setBlock(TRANSDUCER, ModBlocks.NODE_TRANSDUCER.get());
      helper.setBlock(TRANSDUCER.east(), Blocks.REDSTONE_BLOCK);
      BlockPos relay = new BlockPos(1, 2, 4);
      helper.setBlock(relay.below(), Blocks.STONE);
      helper.setBlock(relay, ModBlocks.VIS_RELAY.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.DOWN));

      helper.succeedWhen(() -> {
         helper.assertBlockPresent(ModBlocks.ENERGIZED_AURA_NODE.get(), NODE);
         NodeEnergizedBlockEntity energized = (NodeEnergizedBlockEntity)helper.getBlockEntity(NODE);
         // TC4 setupNode: floor(sqrt(base)) of each primal a tick.
         helper.assertTrue(
            energized.getAspects().getAmount(Aspect.FIRE) == 5 && energized.getAspects().getAmount(Aspect.AIR) == 4,
            "energized node must make 5 ignis and 4 aer a tick, got " + energized.getAspects()
         );
         int drained = VisNetHandler.drainVis(helper.getLevel(), helper.absolutePos(new BlockPos(0, 2, 4)), Aspect.FIRE, 5);
         helper.assertTrue(drained > 0, "consumer next to the relay must draw ignis from the network");
      });
   }

   /**
    * TC4 TileNodeEnergized.setupNode: compound base aspects break into primals (reduceToPrimals with merge, so a primal
    * reached twice keeps the larger amount), then floor(sqrt) each. Lux 16 → aer 16, ignis 16; herba 8 → victus 8 +
    * terra 8 → aqua 8, terra 8. Output: aer 4, ignis 4, aqua 2, terra 2, and no compound aspect left.
    */
   @GameTest(template = "empty_9x5x9", timeoutTicks = 1400)
   public static void compoundAspectsBreakIntoPrimals(GameTestHelper helper) {
      stabilizer(helper, STABILIZER, Direction.UP);
      helper.setBlock(NODE, ModBlocks.AURA_NODE.get());
      AuraNodeBlockEntity node = (AuraNodeBlockEntity)helper.getBlockEntity(NODE);
      node.setAspects(new AspectList().add(Aspect.LIGHT, 16).add(Aspect.PLANT, 8));
      helper.setBlock(TRANSDUCER, ModBlocks.NODE_TRANSDUCER.get());
      helper.setBlock(TRANSDUCER.east(), Blocks.REDSTONE_BLOCK);

      helper.succeedWhen(() -> {
         helper.assertBlockPresent(ModBlocks.ENERGIZED_AURA_NODE.get(), NODE);
         AspectList out = ((NodeEnergizedBlockEntity)helper.getBlockEntity(NODE)).getAspects();
         helper.assertTrue(
            out.size() == 4
               && out.getAmount(Aspect.AIR) == 4
               && out.getAmount(Aspect.FIRE) == 4
               && out.getAmount(Aspect.WATER) == 2
               && out.getAmount(Aspect.EARTH) == 2,
            "energized node must make aer 4, ignis 4, aqua 2, terra 2, got " + out
         );
      });
   }

   /**
    * E18 (our addition; TC4's energized node is not an INode and the thaumometer found nothing): pointed at an energized
    * node the thaumometer scans it as its own phenomenon, with the node formula over its base aspects (max(4, n/10)).
    */
   @GameTest(template = "empty_9x5x9")
   public static void thaumometerScansEnergizedNode(GameTestHelper helper) {
      NodeEnergizedBlockEntity node = energizedNode(helper, NODE);
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      Vec3 center = Vec3.atCenterOf(helper.absolutePos(NODE));
      player.moveTo(center.x + 3.0, center.y - player.getEyeHeight(), center.z, 0.0F, 0.0F);
      player.lookAt(EntityAnchorArgument.Anchor.EYES, center);
      ItemStack thaumometer = new ItemStack(ModItems.THAUMOMETER.get());

      ScanResult scan = ModItems.THAUMOMETER.get().doScan(thaumometer, player, false);
      helper.assertTrue(
         scan != null && scan.type == ScanResult.Type.PHENOMENA && (NodeEnergizedBlockEntity.SCAN_PREFIX + node.getScanId()).equals(scan.phenomena),
         "the thaumometer must find the energized node, got " + (scan == null ? null : scan.type + " " + scan.phenomena)
      );
      ScanManager.completeScan(player, scan, "@");
      helper.assertTrue(ScanManager.hasBeenScanned(player, scan), "the energized node must count as scanned");
      AspectList aspects = ScanManager.getScanAspects(scan, helper.getLevel());
      helper.assertTrue(
         aspects.getAmount(Aspect.WATER) == 4 && aspects.getAmount(Aspect.EARTH) == 4,
         "aqua 30 and terra 9 must give 4 and 4, got " + aspects
      );
      helper.succeed();
   }

   /** The stabilizer alone only holds the node: without a signal the transducer stays idle and the node stays normal. */
   @GameTest(template = "empty_9x5x9")
   public static void transducerWithoutSignalIdles(GameTestHelper helper) {
      stabilizer(helper, STABILIZER, Direction.UP);
      helper.setBlock(NODE, ModBlocks.AURA_NODE.get());
      AuraNodeBlockEntity node = (AuraNodeBlockEntity)helper.getBlockEntity(NODE);
      node.setAspects(new AspectList().add(Aspect.AIR, 20));
      helper.setBlock(TRANSDUCER, ModBlocks.NODE_TRANSDUCER.get());
      helper.runAfterDelay(60, () -> {
         NodeTransducerBlockEntity transducer = (NodeTransducerBlockEntity)helper.getBlockEntity(TRANSDUCER);
         helper.assertTrue(transducer.status == 0, "unpowered transducer must stay idle, got status " + transducer.status);
         helper.assertBlockPresent(ModBlocks.AURA_NODE.get(), NODE);
         helper.assertTrue(node.getAspects().getAmount(Aspect.AIR) == 20, "unpowered transducer must not drain the node, got " + node.getAspects());
         helper.succeed();
      });
   }

   /** Without the signal the transducer counts down and turns the node back into a normal one, empty but with its base aspects. */
   @GameTest(template = "empty_9x5x9", timeoutTicks = 1200)
   public static void cuttingSignalRevertsNode(GameTestHelper helper) {
      stabilizer(helper, STABILIZER, Direction.UP);
      helper.setBlock(TRANSDUCER, ModBlocks.NODE_TRANSDUCER.get());
      energizedNode(helper, NODE);

      helper.succeedWhen(() -> {
         helper.assertBlockPresent(ModBlocks.AURA_NODE.get(), NODE);
         AuraNodeBlockEntity node = (AuraNodeBlockEntity)helper.getBlockEntity(NODE);
         helper.assertTrue(node.getAspects().visSize() == 0, "reverted node must be drained, got " + node.getAspects());
         helper.assertTrue(node.getAspectsBase().getAmount(Aspect.WATER) == 30, "reverted node must keep its base aqua, got " + node.getAspectsBase());
      });
   }

   /** Taking the stabilizer away blows the node up: a blast that breaks no blocks, and flux goo and gas around. */
   @GameTest(template = "empty_17x9x17")
   public static void removingStabilizerExplodesNode(GameTestHelper helper) {
      BlockPos dirt = heldEnergizedNode(helper);
      helper.runAfterDelay(5, () -> {
         helper.setBlock(BIG_NODE.below(), Blocks.AIR);
         assertExploded(helper, dirt);
         NodeTransducerBlockEntity transducer = (NodeTransducerBlockEntity)helper.getBlockEntity(BIG_NODE.above());
         helper.assertTrue(transducer.status == 0, "transducer must drop back to idle, got status " + transducer.status);
         helper.succeed();
      });
   }

   /** Taking the transducer away blows the node up too. */
   @GameTest(template = "empty_17x9x17")
   public static void removingTransducerExplodesNode(GameTestHelper helper) {
      BlockPos dirt = heldEnergizedNode(helper);
      helper.runAfterDelay(5, () -> {
         helper.setBlock(BIG_NODE.above(), Blocks.AIR);
         assertExploded(helper, dirt);
         helper.succeed();
      });
   }

   /** A powered stabilizer is off, so the next neighbour change blows the node up. */
   @GameTest(template = "empty_17x9x17")
   public static void poweredStabilizerExplodesNode(GameTestHelper helper) {
      BlockPos dirt = heldEnergizedNode(helper);
      helper.runAfterDelay(5, () -> {
         helper.setBlock(BIG_NODE.below().east(), Blocks.REDSTONE_BLOCK);
         helper.assertBlockPresent(ModBlocks.ENERGIZED_AURA_NODE.get(), BIG_NODE);
         helper.setBlock(BIG_NODE.west(), Blocks.STONE);
         assertExploded(helper, dirt);
         helper.succeed();
      });
   }

   /** A held node survives unrelated neighbour changes. */
   @GameTest(template = "empty_17x9x17")
   public static void heldNodeSurvivesNeighbourChanges(GameTestHelper helper) {
      heldEnergizedNode(helper);
      helper.runAfterDelay(5, () -> {
         helper.setBlock(BIG_NODE.west(), Blocks.STONE);
         helper.setBlock(BIG_NODE.west(), Blocks.AIR);
         helper.assertBlockPresent(ModBlocks.ENERGIZED_AURA_NODE.get(), BIG_NODE);
         helper.succeed();
      });
   }

   /** The transducer only starts over a stabilizer that faces the node (E12). */
   @GameTest(template = "empty_9x5x9")
   public static void transducerNeedsStabilizerFacingNode(GameTestHelper helper) {
      stabilizer(helper, STABILIZER, Direction.DOWN);
      helper.setBlock(NODE, ModBlocks.AURA_NODE.get());
      AuraNodeBlockEntity node = (AuraNodeBlockEntity)helper.getBlockEntity(NODE);
      node.setAspects(new AspectList().add(Aspect.AIR, 20));
      helper.setBlock(TRANSDUCER, ModBlocks.NODE_TRANSDUCER.get());
      helper.setBlock(TRANSDUCER.east(), Blocks.REDSTONE_BLOCK);
      helper.runAfterDelay(60, () -> {
         NodeTransducerBlockEntity transducer = (NodeTransducerBlockEntity)helper.getBlockEntity(TRANSDUCER);
         helper.assertTrue(transducer.status == 0, "stabilizer facing away must not let the transducer start, got status " + transducer.status);
         helper.assertTrue(node.getAspects().getAmount(Aspect.AIR) == 20, "idle transducer must not drain the node, got " + node.getAspects());
         helper.succeed();
      });
   }

   /** Preset node: the grand primal node energizes into 256 of each primal a tick, not sqrt(256). */
   @GameTest(template = "empty_9x5x9", timeoutTicks = 1400)
   public static void grandPrimalNodeEnergizesTo256(GameTestHelper helper) {
      stabilizer(helper, STABILIZER, Direction.UP);
      helper.setBlock(NODE, ModBlocks.AURA_NODE.get());
      ((PresetAuraNodeItem)ModItems.PRIMAL_256_AURA_NODE.get()).configurePlacedNode(helper.getLevel(), helper.absolutePos(NODE), null);
      helper.setBlock(TRANSDUCER, ModBlocks.NODE_TRANSDUCER.get());
      helper.setBlock(TRANSDUCER.east(), Blocks.REDSTONE_BLOCK);

      helper.succeedWhen(() -> {
         helper.assertBlockPresent(ModBlocks.ENERGIZED_AURA_NODE.get(), NODE);
         NodeEnergizedBlockEntity energized = (NodeEnergizedBlockEntity)helper.getBlockEntity(NODE);
         for (Aspect aspect : Aspect.getPrimalAspects()) {
            helper.assertTrue(energized.getAspects().getAmount(aspect) == 256, "grand primal node must make 256 " + aspect.getTag() + ", got " + energized.getAspects());
         }
      });
   }

   /** Preset node: the primal node places 256 of each primal, as its tooltip says, which energizes into 16. */
   @GameTest(template = "empty_9x5x9")
   public static void primalNodePlacesTooltipAmount(GameTestHelper helper) {
      helper.setBlock(NODE, ModBlocks.AURA_NODE.get());
      ((PresetAuraNodeItem)ModItems.PRIMAL_16_AURA_NODE.get()).configurePlacedNode(helper.getLevel(), helper.absolutePos(NODE), null);
      AuraNodeBlockEntity node = (AuraNodeBlockEntity)helper.getBlockEntity(NODE);
      for (Aspect aspect : Aspect.getPrimalAspects()) {
         helper.assertTrue(node.getAspectsBase().getAmount(aspect) == 256, "primal node must hold 256 " + aspect.getTag() + ", got " + node.getAspectsBase());
      }

      helper.succeed();
   }

   /** Stabilizer, transducer and a dirt block next to the node, then the energized node (placed last, so nothing has poked it yet). */
   private static BlockPos heldEnergizedNode(GameTestHelper helper) {
      stabilizer(helper, BIG_NODE.below(), Direction.UP);
      helper.setBlock(BIG_NODE.above(), ModBlocks.NODE_TRANSDUCER.get());
      BlockPos dirt = BIG_NODE.east();
      helper.setBlock(dirt, Blocks.DIRT);
      energizedNode(helper, BIG_NODE);
      return dirt;
   }

   private static NodeEnergizedBlockEntity energizedNode(GameTestHelper helper, BlockPos pos) {
      helper.setBlock(pos, ModBlocks.ENERGIZED_AURA_NODE.get());
      NodeEnergizedBlockEntity node = (NodeEnergizedBlockEntity)helper.getBlockEntity(pos);
      node.setAspects(new AspectList().add(Aspect.WATER, 30).add(Aspect.EARTH, 9));
      return node;
   }

   private static void stabilizer(GameTestHelper helper, BlockPos pos, Direction facing) {
      helper.setBlock(pos, ModBlocks.NODE_STABILIZER.get().defaultBlockState().setValue(NodeStabilizerBlock.FACING, facing));
   }

   private static void assertExploded(GameTestHelper helper, BlockPos dirt) {
      helper.assertBlockNotPresent(ModBlocks.ENERGIZED_AURA_NODE.get(), BIG_NODE);
      helper.assertBlockPresent(Blocks.DIRT, dirt);
      int flux = 0;
      for (BlockPos pos : BlockPos.betweenClosed(BlockPos.ZERO, new BlockPos(16, 8, 16))) {
         BlockState state = helper.getBlockState(pos);
         if (state.is(ModBlocks.FLUX_GOO.get()) || state.is(ModBlocks.FLUX_GAS.get())) {
            flux++;
         }
      }

      helper.assertTrue(flux > 0, "exploded node must spill flux");
   }
}
