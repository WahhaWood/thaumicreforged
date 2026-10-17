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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.block.airy.aura_node.AuraNodeBlockEntity;
import thaumcraft.block.jar.node.NodeJarBlockEntity;
import thaumcraft.block.jar.node.NodeJarBlockItem;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.aura_node.PresetAuraNodeItem;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.WandManager;

/** Node in a jar (TC4 ItemJarNode / TileJarNode / BlockJar) and the hungry node's block ray. */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class NodeJarGameTest {
   private static final BlockPos FLOOR = new BlockPos(4, 1, 4);
   private static final BlockPos JAR = FLOOR.above();

   /** The jar placed from the item holds the node, and breaking it by hand gives the same jar back, any number of times. */
   @GameTest(template = "empty_9x5x9")
   public static void placedJarKeepsNodeAcrossBreaks(GameTestHelper helper) {
      helper.setBlock(FLOOR, Blocks.STONE);
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      ItemStack stack = jarItem(NodeType.HUNGRY, null);

      for (int round = 0; round < 3; round++) {
         place(helper, player, stack);
         NodeJarBlockEntity jar = (NodeJarBlockEntity)helper.getBlockEntity(JAR);
         helper.assertTrue(jar.getAspects().getAmount(Aspect.AIR) == 20, "round " + round + ": placed jar must hold the node's aer");
         helper.assertTrue(jar.getAspects().getAmount(Aspect.HUNGER) == 7, "round " + round + ": placed jar must hold the node's fames");
         helper.assertTrue(jar.getNodeType() == NodeType.HUNGRY, "round " + round + ": node type must survive, got " + jar.getNodeType());
         helper.assertTrue(jar.getNodeModifier() == null, "round " + round + ": no modifier must stay none, got " + jar.getNodeModifier());
         helper.assertTrue("test:node".equals(jar.getId()), "round " + round + ": node id must survive, got " + jar.getId());

         helper.getLevel().destroyBlock(helper.absolutePos(JAR), true, player);
         stack = pickUpJar(helper);
         helper.assertTrue(!stack.isEmpty(), "round " + round + ": broken jar must drop");
      }

      helper.succeed();
   }

   /** Even a jar whose node is drained to nothing drops (TC4 checks only the drop flag). */
   @GameTest(template = "empty_9x5x9")
   public static void emptyJarStillDrops(GameTestHelper helper) {
      helper.setBlock(JAR, ModBlocks.NODE_JAR.get());
      helper.getLevel().destroyBlock(helper.absolutePos(JAR), true);
      helper.assertTrue(!pickUpJar(helper).isEmpty(), "jar without vis must still drop");
      helper.succeed();
   }

   /** A wand releases the node: the jar becomes an aura node with the same contents and drops nothing. */
   @GameTest(template = "empty_9x5x9")
   public static void wandReleasesNode(GameTestHelper helper) {
      helper.setBlock(FLOOR, Blocks.STONE);
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      place(helper, player, jarItem(NodeType.PURE, NodeModifier.BRIGHT));

      ItemStack wand = ItemWandCasting.createWand(ModItems.WOOD_WAND_ROD.get(), ModItems.IRON_WAND_CAP.get());
      player.setItemInHand(InteractionHand.MAIN_HAND, wand);
      wand.getItem().onItemUseFirst(wand, clickTop(helper, player, JAR));

      helper.assertBlockPresent(ModBlocks.AURA_NODE.get(), JAR);
      AuraNodeBlockEntity node = (AuraNodeBlockEntity)helper.getBlockEntity(JAR);
      helper.assertTrue(node.getAspects().getAmount(Aspect.AIR) == 20, "released node must keep its aer");
      helper.assertTrue(node.getAspectsBase().getAmount(Aspect.HUNGER) == 7, "released node must keep its base fames");
      helper.assertTrue(node.getNodeType() == NodeType.PURE, "released node must keep its type");
      helper.assertTrue(node.getNodeModifier() == NodeModifier.BRIGHT, "released node must keep its modifier");
      helper.assertTrue("test:node".equals(node.getId()), "released node must keep its id");
      helper.assertTrue(pickUpJar(helper).isEmpty(), "releasing the node must not drop the jar");
      helper.succeed();
   }

   /** TC4 rayTraceIgnoringSource: first block on the line, skipping the block the ray starts in; null on a miss. */
   @GameTest(template = "empty_9x5x9")
   public static void rayHitsFirstBlockPastSource(GameTestHelper helper) {
      BlockPos source = new BlockPos(1, 2, 4);
      helper.setBlock(source, Blocks.STONE);
      helper.setBlock(new BlockPos(5, 2, 4), Blocks.DIRT);
      helper.setBlock(new BlockPos(7, 2, 4), Blocks.DIRT);
      Vec3 from = helper.absolutePos(source).getCenter();

      BlockHitResult hit = ThaumcraftApiHelper.rayTraceIgnoringSource(
         helper.getLevel(), from, helper.absolutePos(new BlockPos(8, 2, 4)).getCenter(), true, false, false
      );
      helper.assertTrue(hit != null, "ray through dirt must hit");
      helper.assertTrue(hit.getBlockPos().equals(helper.absolutePos(new BlockPos(5, 2, 4))), "ray must stop at the first dirt, got " + hit.getBlockPos());

      BlockHitResult miss = ThaumcraftApiHelper.rayTraceIgnoringSource(
         helper.getLevel(), from, helper.absolutePos(new BlockPos(1, 2, 8)).getCenter(), true, false, false
      );
      helper.assertTrue(miss == null, "ray through air must miss even though it starts inside stone, got " + miss);
      helper.succeed();
   }

   /** A hungry node walled in dirt eats one wall block within its first 50-tick cycles. */
   @GameTest(template = "empty_9x5x9", timeoutTicks = 260)
   public static void hungryNodeEatsBlocks(GameTestHelper helper) {
      BlockPos center = new BlockPos(4, 2, 4);
      for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
         helper.setBlock(pos, Blocks.DIRT);
      }

      helper.setBlock(center, ModBlocks.AURA_NODE.get());
      AuraNodeBlockEntity node = (AuraNodeBlockEntity)helper.getBlockEntity(center);
      node.setAspects(new AspectList().add(Aspect.HUNGER, 10));
      node.setNodeType(NodeType.HUNGRY);

      helper.succeedWhen(() -> {
         int eaten = 0;
         for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            if (!pos.equals(center) && helper.getBlockState(pos).isAir()) {
               eaten++;
            }
         }

         helper.assertTrue(eaten > 0, "hungry node has not eaten any dirt yet");
      });
   }

   /** Preset node: the grand primal node keeps its energized output through capture, the jar item and release. */
   @GameTest(template = "empty_9x5x9")
   public static void grandNodeOutputSurvivesJar(GameTestHelper helper) {
      // TC4 node jar: wooden slabs on top, glass around and below, the node in the middle (fitNodeJar).
      for (BlockPos pos : BlockPos.betweenClosed(JAR.offset(-1, -1, -1), JAR.offset(1, 1, 1))) {
         helper.setBlock(pos, Blocks.GLASS);
      }

      for (BlockPos pos : BlockPos.betweenClosed(JAR.offset(-1, 2, -1), JAR.offset(1, 2, 1))) {
         helper.setBlock(pos, Blocks.OAK_SLAB);
      }

      helper.setBlock(JAR, ModBlocks.AURA_NODE.get());
      ((PresetAuraNodeItem)ModItems.PRIMAL_256_AURA_NODE.get()).configurePlacedNode(helper.getLevel(), helper.absolutePos(JAR), null);
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      ItemStack wand = new ItemStack(ModItems.WAND_CASTING.get());
      ItemWandCasting wandItem = (ItemWandCasting)wand.getItem();
      wandItem.setRod(wand, ModItems.SILVERWOOD_WAND_ROD.get());
      for (Aspect aspect : Aspect.getPrimalAspects()) {
         wandItem.addVis(wand, aspect, 100, true);
      }

      helper.assertTrue(WandManager.createNodeJar(wand, player, helper.getLevel(), helper.absolutePos(JAR.offset(1, 2, 1))), "wand must jar the node");
      // TC4: the sealed jar shows its enchanting glyphs to clients (event 9).
      BlockEventGameTest.assertQueued(helper, JAR, ModBlocks.NODE_JAR.get(), 9, 0);
      NodeJarBlockEntity captured = (NodeJarBlockEntity)helper.getBlockEntity(JAR);
      helper.assertTrue(captured.getAwakenPrimalVisAmount() == 256, "captured jar must keep 256, got " + captured.getAwakenPrimalVisAmount());

      helper.getLevel().destroyBlock(helper.absolutePos(JAR), true, player);
      ItemStack stack = pickUpJar(helper);
      NodeJarBlockItem item = (NodeJarBlockItem)ModItems.NODE_JAR.get();
      helper.assertTrue(item.getAwakenPrimalVisAmount(stack) == 256, "jar item must keep 256, got " + item.getAwakenPrimalVisAmount(stack));

      helper.setBlock(FLOOR, Blocks.STONE);
      place(helper, player, stack);
      ItemStack releaseWand = ItemWandCasting.createWand(ModItems.WOOD_WAND_ROD.get(), ModItems.IRON_WAND_CAP.get());
      player.setItemInHand(InteractionHand.MAIN_HAND, releaseWand);
      releaseWand.getItem().onItemUseFirst(releaseWand, clickTop(helper, player, JAR));
      AuraNodeBlockEntity node = (AuraNodeBlockEntity)helper.getBlockEntity(JAR);
      helper.assertTrue(node.getAwakenPrimalVisAmount() == 256, "released node must keep 256, got " + node.getAwakenPrimalVisAmount());
      helper.succeed();
   }

   private static ItemStack jarItem(NodeType type, NodeModifier modifier) {
      ItemStack stack = new ItemStack(ModItems.NODE_JAR.get());
      NodeJarBlockItem item = (NodeJarBlockItem)stack.getItem();
      item.setAspects(stack, new AspectList().add(Aspect.AIR, 20).add(Aspect.HUNGER, 7));
      item.setNodeAttributes(stack, type, modifier, "test:node");
      return stack;
   }

   private static void place(GameTestHelper helper, Player player, ItemStack stack) {
      player.setItemInHand(InteractionHand.MAIN_HAND, stack);
      stack.getItem().useOn(clickTop(helper, player, FLOOR));
      helper.assertBlockPresent(ModBlocks.NODE_JAR.get(), JAR);
   }

   private static UseOnContext clickTop(GameTestHelper helper, Player player, BlockPos relative) {
      BlockPos pos = helper.absolutePos(relative);
      return new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false));
   }

   /** Removes the dropped node jar from the arena and returns it, or EMPTY when nothing dropped. */
   private static ItemStack pickUpJar(GameTestHelper helper) {
      for (ItemEntity entity : helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(JAR)).inflate(3.0))) {
         if (entity.getItem().is(ModItems.NODE_JAR.get())) {
            ItemStack stack = entity.getItem().copy();
            entity.discard();
            return stack;
         }
      }

      return ItemStack.EMPTY;
   }
}
