package thaumcraft.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.block.arcane_workbench.MagicWorkbenchBlockEntity;
import thaumcraft.block.flux_scrubber.FluxScrubberBlockEntity;
import thaumcraft.block.vis_relay.VisRelayBlock;
import thaumcraft.block.vis_relay.VisRelayBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.wands.ItemWandCasting;

/** TC4 vis network around an energized node: relays (TileVisRelay), the charge relay and the flux scrubber. */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class VisNetGameTest {
   /** Energized node with the TC4 default aura (20 of each primal, 4 a tick). */
   private static final BlockPos NODE = new BlockPos(1, 2, 8);

   /** TC4 canNodeBeSeen: a relay does not link through a wall, links once it is gone, and glows brighter when linked. */
   @GameTest(template = "empty_17x9x17", timeoutTicks = 200)
   public static void relayNeedsLineOfSight(GameTestHelper helper) {
      node(helper);
      BlockPos relayPos = relay(helper, new BlockPos(6, 2, 8));
      BlockPos wall = new BlockPos(3, 2, 8);
      helper.setBlock(wall, Blocks.STONE);
      VisRelayBlockEntity relay = (VisRelayBlockEntity)helper.getBlockEntity(relayPos);

      helper.startSequence()
         .thenIdle(50)
         .thenExecute(() -> {
            helper.assertTrue(!VisNetHandler.isNodeValid(relay.getParent()), "relay must not link through the wall");
            helper.assertTrue(!helper.getBlockState(relayPos).getValue(VisRelayBlock.CONNECTED), "lone relay must stay dim");
            helper.setBlock(wall, Blocks.AIR);
         })
         .thenWaitUntil(() -> {
            helper.assertTrue(VisNetHandler.isNodeValid(relay.getParent()), "relay must link once the wall is gone");
            helper.assertTrue(helper.getBlockState(relayPos).getValue(VisRelayBlock.CONNECTED), "linked relay must glow");
            helper.assertTrue(helper.getBlockState(relayPos).getLightEmission() == 10, "linked relay light must be 10");
         })
         .thenSucceed();
   }

   /**
    * The client draws the relay beam from the parent offset in the sync tag (TC4 px/py/pz), and lights it up in the
    * aspect's colour on block event 0, which the server only broadcasts when its own triggerEvent returns true.
    */
   @GameTest(template = "empty_17x9x17", timeoutTicks = 200)
   public static void relayBeamReachesClient(GameTestHelper helper) {
      node(helper);
      BlockPos relayPos = relay(helper, new BlockPos(6, 2, 8));
      VisRelayBlockEntity relay = (VisRelayBlockEntity)helper.getBlockEntity(relayPos);

      helper.startSequence()
         .thenWaitUntil(() -> helper.assertTrue(VisNetHandler.isNodeValid(relay.getParent()), "relay must link to the node"))
         .thenExecute(() -> {
            CompoundTag tag = relay.getUpdateTag(helper.getLevel().registryAccess());
            BlockPos offset = relayPos.subtract(NODE);
            helper.assertTrue(
               tag.getByte("px") == offset.getX() && tag.getByte("py") == offset.getY() && tag.getByte("pz") == offset.getZ(),
               "sync tag must point at the node " + offset + ", got " + tag.getByte("px") + " " + tag.getByte("py") + " " + tag.getByte("pz")
            );

            int drained = VisNetHandler.drainVis(helper.getLevel(), helper.absolutePos(relayPos.east(2)), Aspect.FIRE, 5);
            helper.assertTrue(drained > 0, "drain next to the relay must get ignis");
            BlockPos abs = helper.absolutePos(relayPos);
            boolean broadcast = helper.getLevel().getBlockState(abs).triggerEvent(helper.getLevel(), abs, 0, 1);
            helper.assertTrue(broadcast, "server must accept the pulse event, or clients never see it");
         })
         .thenSucceed();
   }

   /** Shards tune relays; differently tuned relays do not link, alike or untuned ones do; the same shard again clears the tuning. */
   @GameTest(template = "empty_17x9x17", timeoutTicks = 200)
   public static void shardTuningControlsLinks(GameTestHelper helper) {
      node(helper);
      BlockPos nearPos = relay(helper, new BlockPos(7, 2, 8));
      // 13 blocks from the node, out of its range: it can only hang on the near relay.
      BlockPos farPos = relay(helper, new BlockPos(14, 2, 8));
      VisRelayBlockEntity near = (VisRelayBlockEntity)helper.getBlockEntity(nearPos);
      VisRelayBlockEntity far = (VisRelayBlockEntity)helper.getBlockEntity(farPos);
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      useShard(helper, player, nearPos, ModItems.FIRE_SHARD.get());
      useShard(helper, player, farPos, ModItems.WATER_SHARD.get());
      helper.assertTrue(near.color == 1 && far.color == 2, "shards must tune to ignis and aqua, got " + near.color + " and " + far.color);

      helper.startSequence()
         .thenIdle(50)
         .thenExecute(() -> {
            helper.assertTrue(VisNetHandler.isNodeValid(near.getParent()), "ignis relay must link to the untuned node");
            helper.assertTrue(!VisNetHandler.isNodeValid(far.getParent()), "aqua relay must not link to the ignis relay");
            useShard(helper, player, farPos, ModItems.FIRE_SHARD.get());
         })
         .thenWaitUntil(() -> helper.assertTrue(VisNetHandler.isNodeValid(far.getParent()) && far.getParent().get() == near, "ignis relays must link"))
         .thenExecute(() -> {
            useShard(helper, player, farPos, ModItems.FIRE_SHARD.get());
            helper.assertTrue(far.color == -1, "the same shard again must clear the tuning, got " + far.color);
            useShard(helper, player, nearPos, ModItems.BALANCED_SHARD.get());
            helper.assertTrue(near.color == -1, "balanced shard must clear the tuning, got " + near.color);
         })
         .thenSucceed();
   }

   /** TC4 TileVisRelay.onWandRightClick: the wand cycles none, the six primals, none. */
   @GameTest(template = "empty_17x9x17")
   public static void wandCyclesRelayTuning(GameTestHelper helper) {
      BlockPos relayPos = relay(helper, new BlockPos(6, 2, 8));
      VisRelayBlockEntity relay = (VisRelayBlockEntity)helper.getBlockEntity(relayPos);
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      StringBuilder seen = new StringBuilder();
      for (int i = 0; i < 7; i++) {
         int ret = relay.onWandRightClick(helper.getLevel(), player, InteractionHand.MAIN_HAND, helper.absolutePos(relayPos), Direction.UP);
         helper.assertTrue(ret == 1, "wand click on a relay must be handled");
         seen.append(relay.color).append(' ');
      }

      helper.assertTrue("0 1 2 3 4 5 -1 ".contentEquals(seen), "wand must cycle 0..5 then none, got " + seen);
      helper.succeed();
   }

   /** TC4 BlockMetalDevice.onNeighborBlockChange: the relay drops when its support turns to air, and only then. */
   @GameTest(template = "empty_17x9x17")
   public static void relayDropsWithoutSupport(GameTestHelper helper) {
      BlockPos relayPos = relay(helper, new BlockPos(6, 2, 8));
      helper.setBlock(relayPos.east(), Blocks.STONE);
      helper.setBlock(relayPos.east(), Blocks.AIR);
      helper.assertBlockPresent(ModBlocks.VIS_RELAY.get(), relayPos);

      helper.setBlock(relayPos.below(), Blocks.AIR);
      helper.assertBlockNotPresent(ModBlocks.VIS_RELAY.get(), relayPos);
      boolean dropped = !helper.getLevel()
         .getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(relayPos)).inflate(2.0), e -> e.getItem().is(ModItems.VIS_RELAY.get()))
         .isEmpty();
      helper.assertTrue(dropped, "relay must drop as an item");
      helper.succeed();
   }

   /** TC4 TileMagicWorkbenchCharger: the charge relay fills the wand lying in the arcane workbench below it. */
   @GameTest(template = "empty_17x9x17")
   public static void chargeRelayChargesWorkbenchWand(GameTestHelper helper) {
      node(helper);
      BlockPos bench = new BlockPos(5, 1, 8);
      helper.setBlock(bench, ModBlocks.ARCANE_WORKBENCH.get());
      helper.setBlock(bench.above(), ModBlocks.VIS_CHARGE_RELAY.get());
      ItemStack wand = ItemWandCasting.createWand(ModItems.WOOD_WAND_ROD.get(), ModItems.IRON_WAND_CAP.get());
      MagicWorkbenchBlockEntity workbench = (MagicWorkbenchBlockEntity)helper.getBlockEntity(bench);
      workbench.setItem(9, wand);

      helper.succeedWhen(() -> {
         ItemStack inBench = workbench.getItem(9);
         ItemWandCasting item = (ItemWandCasting)inBench.getItem();
         for (Aspect aspect : Aspect.getPrimalAspects()) {
            helper.assertTrue(item.getVis(inBench, aspect) > 0, "wand in the workbench must get " + aspect.getTag() + ", has " + item.getAllVis(inBench));
         }
      });
   }

   /** TC4 TileFluxScrubber: powered by Aer from the network it removes flux within 16 blocks. */
   @GameTest(template = "empty_17x9x17", timeoutTicks = 400)
   public static void fluxScrubberCleansFlux(GameTestHelper helper) {
      node(helper);
      BlockPos scrubberPos = new BlockPos(5, 2, 8);
      helper.setBlock(scrubberPos, ModBlocks.FLUX_SCRUBBER.get());
      for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(9, 1, 4), new BlockPos(15, 1, 12))) {
         helper.setBlock(pos.below(), Blocks.STONE);
         helper.setBlock(pos, ModBlocks.FLUX_GOO.get());
      }

      FluxScrubberBlockEntity scrubber = (FluxScrubberBlockEntity)helper.getBlockEntity(scrubberPos);
      helper.succeedWhen(() -> helper.assertTrue(scrubber.charges > 0 || scrubber.essence > 0, "scrubber must clean flux"));
   }

   /** TC4 TileFluxScrubber.takeEssentia: tubes on its face take the Praecantatio it made. */
   @GameTest(template = "empty_17x9x17")
   public static void fluxScrubberGivesEssence(GameTestHelper helper) {
      BlockPos scrubberPos = new BlockPos(5, 2, 8);
      helper.setBlock(scrubberPos, ModBlocks.FLUX_SCRUBBER.get());
      FluxScrubberBlockEntity scrubber = (FluxScrubberBlockEntity)helper.getBlockEntity(scrubberPos);
      scrubber.essence = 3;
      Direction face = scrubber.getFacing();
      helper.assertTrue(scrubber.getEssenceType(face) == Aspect.MAGIC, "scrubber must offer praecantatio");
      int taken = scrubber.extractEssence(Aspect.MAGIC, 5, face);
      helper.assertTrue(taken == 3 && scrubber.essence == 0, "tube must take all 3 essence, took " + taken + ", left " + scrubber.essence);
      helper.succeed();
   }

   /** Stabilizer and transducer first, the node last: held, it survives flux flowing up to it, and the idle transducer takes 950 ticks to revert it. */
   private static void node(GameTestHelper helper) {
      helper.setBlock(NODE.below(), ModBlocks.NODE_STABILIZER.get());
      helper.setBlock(NODE.above(), ModBlocks.NODE_TRANSDUCER.get());
      helper.setBlock(NODE, ModBlocks.ENERGIZED_AURA_NODE.get());
   }

   /** A relay standing on stone, facing its support. */
   private static BlockPos relay(GameTestHelper helper, BlockPos pos) {
      helper.setBlock(pos.below(), Blocks.STONE);
      helper.setBlock(pos, ModBlocks.VIS_RELAY.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.DOWN));
      return pos;
   }

   /** Right-clicks the relay with a shard through the block's own item interaction. */
   private static void useShard(GameTestHelper helper, Player player, BlockPos relative, Item shard) {
      ItemStack stack = new ItemStack(shard);
      player.setItemInHand(InteractionHand.MAIN_HAND, stack);
      BlockPos pos = helper.absolutePos(relative);
      BlockState state = helper.getLevel().getBlockState(pos);
      state.useItemOn(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
   }
}
