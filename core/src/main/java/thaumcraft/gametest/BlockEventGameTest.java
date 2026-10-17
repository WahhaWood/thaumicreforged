package thaumcraft.gametest;

import java.lang.reflect.Field;
import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockEventData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.block.table.ResearchTableBlockEntity;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.WandManager;

/**
 * Block events (TC4 addBlockEvent) on a dedicated server. ServerLevel sends an event to clients only when the
 * server-side triggerEvent returned true, and a client-only (@OnlyIn) override is stripped from the server, so the
 * effect silently never shows up in multiplayer. The GameTest server is a dedicated server: it strips them too.
 */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class BlockEventGameTest {
   /** Every block event TC4 sends, by block: the server must accept each one. */
   @GameTest(template = "empty_17x9x17")
   public static void serverAcceptsBlockEvents(GameTestHelper helper) {
      accepts(helper, new BlockPos(1, 2, 1), ModBlocks.NODE_TRANSDUCER.get(), 10, 10);
      accepts(helper, new BlockPos(3, 2, 1), ModBlocks.MIRROR.get(), 1, 0);
      accepts(helper, new BlockPos(5, 2, 1), ModBlocks.INFUSION_PILLAR.get(), 1, 0, 11, 0, 12, 0, 13, 0);
      accepts(helper, new BlockPos(7, 2, 1), ModBlocks.RESEARCH_TABLE.get(), 1, 1);
      accepts(helper, new BlockPos(9, 2, 1), ModBlocks.ARCANE_PEDESTAL.get(), 11, 0, 12, 0);
      accepts(helper, new BlockPos(11, 2, 1), ModBlocks.NODE_JAR.get(), 9, 0);
      accepts(helper, new BlockPos(13, 2, 1), ModBlocks.CRUCIBLE.get(), 1, 1, 2, 5);
      accepts(helper, new BlockPos(1, 2, 5), ModBlocks.VIS_RELAY.get(), 0, 1);
      // The furnace part handles its events in the block itself, without a block entity.
      BlockPos furnace = helper.absolutePos(new BlockPos(3, 2, 5));
      BlockState furnaceState = ModBlocks.INFERNAL_FURNACE.get().defaultBlockState();
      for (int id : new int[]{1, 3}) {
         helper.assertTrue(furnaceState.triggerEvent(helper.getLevel(), furnace, id, 0), "infernal furnace must accept event " + id);
      }

      helper.succeed();
   }

   /** TC4 WandManager.createCrucible: sparkles over the new crucible. */
   @GameTest(template = "empty_17x9x17")
   public static void wandCrucibleSendsSparkles(GameTestHelper helper) {
      BlockPos pos = new BlockPos(8, 2, 8);
      helper.setBlock(pos.below(), Blocks.STONE);
      helper.setBlock(pos, Blocks.CAULDRON);
      helper.assertTrue(WandManager.createCrucible(chargedWand(), helper.getLevel(), helper.absolutePos(pos)), "wand must make a crucible");
      assertQueued(helper, pos, ModBlocks.CRUCIBLE.get(), 1, 1);
      helper.succeed();
   }

   /** TC4 WandManager.replaceInfusionAltar: every new pillar gets event 1 (sparkles, stone-brick dust). */
   @GameTest(template = "empty_17x9x17")
   public static void wandAltarSendsSparkles(GameTestHelper helper) {
      BlockPos anchor = new BlockPos(7, 1, 7);
      for (int xx = 0; xx < 3; xx += 2) {
         for (int zz = 0; zz < 3; zz += 2) {
            helper.setBlock(anchor.offset(xx, 0, zz), ModBlocks.ARCANE_BRICKS.get());
            helper.setBlock(anchor.offset(xx, 1, zz), ModBlocks.ARCANE_STONE.get());
         }
      }

      helper.setBlock(anchor.offset(1, 0, 1), ModBlocks.ARCANE_PEDESTAL.get());
      BlockPos matrix = anchor.offset(1, 2, 1);
      helper.setBlock(matrix, ModBlocks.RUNIC_MATRIX.get());
      Player player = helper.makeMockPlayer(GameType.CREATIVE);
      helper.assertTrue(WandManager.createInfusionAltar(chargedWand(), player, helper.getLevel(), helper.absolutePos(matrix)), "wand must build the altar");
      for (int yy = 0; yy < 2; yy++) {
         for (int xx = 0; xx < 3; xx += 2) {
            for (int zz = 0; zz < 3; zz += 2) {
               assertQueued(helper, anchor.offset(xx, yy, zz), ModBlocks.INFUSION_PILLAR.get(), 1, 0);
            }
         }
      }

      helper.succeed();
   }

   /** TC4 TileResearchTable: the learn sound is a table event, heard by everyone near the table. */
   @GameTest(template = "empty_17x9x17")
   public static void researchTableLearnEffect(GameTestHelper helper) {
      BlockPos pos = new BlockPos(8, 2, 8);
      helper.setBlock(pos, ModBlocks.RESEARCH_TABLE.get());
      ((ResearchTableBlockEntity)helper.getBlockEntity(pos)).playLearnEffect();
      assertQueued(helper, pos, ModBlocks.RESEARCH_TABLE.get(), 1, 1);
      helper.succeed();
   }

   /** TC4 getItemStackDisplayName "%CAP %ROD %OBJ": the server names the wand too (chat, death messages). */
   @GameTest(template = "empty_17x9x17")
   public static void wandNamedOnServer(GameTestHelper helper) {
      ItemStack wand = ItemWandCasting.createWand(ModItems.WOOD_WAND_ROD.get(), ModItems.IRON_WAND_CAP.get());
      ItemWandCasting item = (ItemWandCasting)wand.getItem();
      Component name = wand.getHoverName();
      String expectedCap = "item.Wand." + item.getCap(wand).getTag() + ".cap";
      boolean hasCap = name.getSiblings().stream().anyMatch(c -> c.getContents() instanceof TranslatableContents t && t.getKey().equals(expectedCap));
      boolean hasObj = name.getSiblings().stream().anyMatch(c -> c.getContents() instanceof TranslatableContents t && t.getKey().equals("item.Wand.wand.obj"));
      helper.assertTrue(hasCap && hasObj, "server wand name must be built from " + expectedCap + " and item.Wand.wand.obj, got " + name);
      helper.succeed();
   }

   /** A wand full of every primal. */
   static ItemStack chargedWand() {
      ItemStack wand = ItemWandCasting.createWand(ModItems.SILVERWOOD_WAND_ROD.get(), ModItems.IRON_WAND_CAP.get());
      ItemWandCasting item = (ItemWandCasting)wand.getItem();
      for (Aspect aspect : Aspect.getPrimalAspects()) {
         item.addVis(wand, aspect, 100, true);
      }

      return wand;
   }

   /** The event is waiting in the level's queue, which the server broadcasts at the end of the tick. */
   static void assertQueued(GameTestHelper helper, BlockPos relative, Block block, int id, int param) {
      BlockEventData expected = new BlockEventData(helper.absolutePos(relative), block, id, param);
      helper.assertTrue(queuedEvents(helper.getLevel()).contains(expected), "no block event " + id + "/" + param + " queued at " + relative);
   }

   @SuppressWarnings("unchecked")
   static Collection<BlockEventData> queuedEvents(ServerLevel level) {
      try {
         Field field = ServerLevel.class.getDeclaredField("blockEvents");
         field.setAccessible(true);
         return (Collection<BlockEventData>)field.get(level);
      } catch (ReflectiveOperationException e) {
         throw new IllegalStateException("ServerLevel.blockEvents not found", e);
      }
   }

   private static void accepts(GameTestHelper helper, BlockPos relative, Block block, int... events) {
      helper.setBlock(relative.below(), Blocks.STONE);
      helper.setBlock(relative, block);
      BlockPos pos = helper.absolutePos(relative);
      BlockState state = helper.getLevel().getBlockState(pos);
      helper.assertTrue(state.is(block) && helper.getLevel().getBlockEntity(pos) != null, block + " must stand with its block entity");
      for (int i = 0; i < events.length; i += 2) {
         helper.assertTrue(state.triggerEvent(helper.getLevel(), pos, events[i], events[i + 1]), block + " must accept event " + events[i] + " on the server");
      }
   }
}
