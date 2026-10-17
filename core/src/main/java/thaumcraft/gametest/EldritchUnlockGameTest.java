package thaumcraft.gametest;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.entities.brainless.aspect_orb.AspectOrbEntity;
import thaumcraft.entities.brainless.projectile.primal_orb.EntityPrimalOrb;
import thaumcraft.event.common.WarpEvents;
import thaumcraft.init.registry.ModItems;
import thaumcraft.init.registry.ModMobEffect;
import thaumcraft.item.primal_charm.PrimalCharmItem;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.foci.ItemFocusPrimal;
import thaumcraft.network.playerdata.PacketPlayerCompleteToServer;
import thaumcraft.recipe.ThaumcraftCraftingManager;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.ResearchManager;
import thaumcraft.research.ResearchNoteData;

/**
 * The whole road to the Eldritch tab and the Primal focus: the charm whisper, the warp unlock of
 * {@code eldritch_minor}, the research note for {@code focus_primal}, the arcane recipe and the cast.
 */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class EldritchUnlockGameTest {

   /** The charm in a plain inventory slot drops primal orbs and, on the 1/20000 roll, whispers @focus_primal. */
   @GameTest(template = "empty_1x1x1")
   public static void charmInInventoryRevealsFocusPrimal(GameTestHelper helper) {
      FakePlayer player = player(helper);
      player.getInventory().setItem(9, new ItemStack(ModItems.PRIMAL_CHARM.get()));
      AABB area = new AABB(player.blockPosition()).inflate(4.0);

      int ticks = 0;
      boolean orbs = false;
      while (ticks < 1_000_000 && !(orbs && ResearchManager.isResearchComplete(player, "@focus_primal"))) {
         player.getInventory().tick();
         ticks++;
         if (!orbs && ticks % 1000 == 0) {
            orbs = !helper.getLevel().getEntitiesOfClass(AspectOrbEntity.class, area).isEmpty();
         }
      }

      helper.getLevel().getEntitiesOfClass(AspectOrbEntity.class, area).forEach(e -> e.discard());
      helper.assertTrue(orbs, "the charm must drop aspect orbs from the inventory");
      helper.assertTrue(ResearchManager.isResearchComplete(player, "@focus_primal"), "the charm must reveal @focus_primal (ticks: " + ticks + ")");
      helper.assertFalse(ResearchManager.isResearchComplete(player, "focus_primal"), "the whisper reveals the research, it does not complete it");
      helper.succeed();
   }

   /** Once focus_primal is known the charm keeps dropping orbs but never whispers again. */
   @GameTest(template = "empty_1x1x1")
   public static void charmStaysSilentWhenFocusPrimalIsKnown(GameTestHelper helper) {
      FakePlayer player = player(helper);
      complete(player, "focus_primal");

      for (int i = 0; i < 400_000; i++) {
         PrimalCharmItem.tickCharm(helper.getLevel(), player);
      }

      helper.getLevel().getEntitiesOfClass(AspectOrbEntity.class, new AABB(player.blockPosition()).inflate(4.0)).forEach(e -> e.discard());
      helper.assertFalse(ResearchManager.isResearchComplete(player, "@focus_primal"), "no whisper for a known research");
      helper.succeed();
   }

   /** @focus_primal alone gives neither a research note nor the research: the parent eldritch_minor is required. */
   @GameTest(template = "empty_1x1x1")
   public static void focusPrimalIsLockedWithoutEldritchMinor(GameTestHelper helper) {
      FakePlayer player = player(helper);
      complete(player, "@focus_primal");
      player.getInventory().setItem(0, new ItemStack(ModItems.INKWELL.get()));
      player.getInventory().setItem(1, new ItemStack(Items.PAPER));

      new PacketPlayerCompleteToServer("focus_primal", (byte)1).executeServer(player);
      helper.assertTrue(ResearchManager.getResearchSlot(player, "focus_primal") < 0, "no research note before eldritch_minor");
      helper.assertTrue(player.getInventory().getItem(1).is(Items.PAPER), "paper must not be spent");

      ItemStack solved = solvedNote(helper, "focus_primal");
      player.setItemInHand(InteractionHand.MAIN_HAND, solved);
      solved.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
      helper.assertFalse(ResearchManager.isResearchComplete(player, "focus_primal"), "a solved note must not bypass the parent");
      helper.succeed();
   }

   /** Temporary warp does not count: permanent 25 plus temporary 100 keeps the Eldritch tab closed. */
   @GameTest(template = "empty_1x1x1")
   public static void temporaryWarpDoesNotOpenEldritch(GameTestHelper helper) {
      FakePlayer player = player(helper);
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      knowledge.addWarpPerm(25);
      knowledge.addWarpTemp(100);

      for (int i = 0; i < 5; i++) {
         knowledge.setWarpCounter(10_000);
         WarpEvents.checkWarpEvent(player);
      }

      helper.assertFalse(ResearchManager.isResearchComplete(player, "eldritch_minor"), "permanent + sticky 25 is not enough");
      helper.assertTrue(ResearchManager.isResearchComplete(player, "@bath_salts"), "warp above 10 reveals bath salts");
      helper.succeed();
   }

   /** Permanent 20 + sticky 6 = 26 opens eldritch_minor on the next warp event, but not eldritch_major. */
   @GameTest(template = "empty_1x1x1")
   public static void stickyWarpCountsTowardsEldritchMinor(GameTestHelper helper) {
      FakePlayer player = player(helper);
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      knowledge.addWarpPerm(20);
      knowledge.addWarpSticky(6);
      knowledge.setWarpCounter(10_000);

      WarpEvents.checkWarpEvent(player);

      helper.assertTrue(ResearchManager.isResearchComplete(player, "eldritch_minor"), "26 warp opens eldritch_minor");
      helper.assertFalse(ResearchManager.isResearchComplete(player, "eldritch_major"), "eldritch_major needs more than 50");
      helper.succeed();
   }

   /** Enough warp is not enough by itself: without a pending warp event (counter 0) nothing is granted. */
   @GameTest(template = "empty_1x1x1")
   public static void eldritchMinorNeedsAWarpEvent(GameTestHelper helper) {
      FakePlayer player = player(helper);
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      knowledge.addWarpPerm(30);
      knowledge.setWarpCounter(0);

      WarpEvents.checkWarpEvent(player);
      helper.assertFalse(ResearchManager.isResearchComplete(player, "eldritch_minor"), "no event, no research");

      knowledge.setWarpCounter(10_000);
      WarpEvents.checkWarpEvent(player);
      helper.assertTrue(ResearchManager.isResearchComplete(player, "eldritch_minor"), "the event grants it");
      helper.succeed();
   }

   @GameTest(template = "empty_1x1x1")
   public static void warpAboveFiftyOpensEldritchMajor(GameTestHelper helper) {
      FakePlayer player = player(helper);
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      knowledge.addWarpPerm(51);
      knowledge.setWarpCounter(10_000);

      WarpEvents.checkWarpEvent(player);

      helper.assertTrue(ResearchManager.isResearchComplete(player, "eldritch_minor"), "minor comes with major");
      helper.assertTrue(ResearchManager.isResearchComplete(player, "eldritch_major"), "51 warp opens eldritch_major");
      helper.succeed();
   }

   /** The real trigger: the player tick every 2000 ticks, skipped while Warp Ward is active. */
   @GameTest(template = "empty_1x1x1")
   public static void playerTickGrantsEldritchUnlessWarpWard(GameTestHelper helper) {
      FakePlayer player = player(helper);
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      knowledge.addWarpPerm(30);
      player.tickCount = 2000;

      knowledge.setWarpCounter(10_000);
      player.addEffect(new MobEffectInstance(ModMobEffect.WARP_WARD, 1000, 0));
      NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
      helper.assertFalse(ResearchManager.isResearchComplete(player, "eldritch_minor"), "warp ward blocks warp events");

      player.removeAllEffects();
      player.tickCount = 1999;
      NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
      helper.assertFalse(ResearchManager.isResearchComplete(player, "eldritch_minor"), "events run only every 2000 ticks");

      player.tickCount = 2000;
      NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
      helper.assertTrue(ResearchManager.isResearchComplete(player, "eldritch_minor"), "the tick must grant eldritch_minor");
      helper.succeed();
   }

   /** After eldritch_minor: note from the book click, solved note completes focus_primal and adds its warp once. */
   @GameTest(template = "empty_1x1x1")
   public static void focusPrimalResearchAfterEldritchMinor(GameTestHelper helper) {
      FakePlayer player = player(helper);
      complete(player, "eldritch_minor");
      player.getInventory().setItem(0, new ItemStack(ModItems.INKWELL.get()));
      player.getInventory().setItem(1, new ItemStack(Items.PAPER));

      new PacketPlayerCompleteToServer("focus_primal", (byte)1).executeServer(player);
      int slot = ResearchManager.getResearchSlot(player, "focus_primal");
      helper.assertTrue(slot >= 0, "the book click must give a focus_primal note");

      ItemStack note = player.getInventory().getItem(slot);
      ResearchNoteData data = ResearchManager.getData(note);
      data.complete = true;
      ResearchManager.updateData(note, data);
      player.getInventory().setItem(slot, ItemStack.EMPTY);
      player.setItemInHand(InteractionHand.MAIN_HAND, note);
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      int permBefore = knowledge.getWarpPerm();
      int stickyBefore = knowledge.getWarpSticky();

      note.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

      helper.assertTrue(ResearchManager.isResearchComplete(player, "focus_primal"), "the solved note completes focus_primal");
      int perm = knowledge.getWarpPerm() - permBefore;
      int sticky = knowledge.getWarpSticky() - stickyBefore;
      helper.assertTrue(perm == 1 && sticky == 1, "warp 2 splits into 1 permanent + 1 sticky, got " + perm + " + " + sticky);
      helper.succeed();
   }

   /** The arcane recipe (charm, diamonds, quartz) is hidden until focus_primal is researched. */
   @GameTest(template = "empty_1x1x1")
   public static void focusPrimalRecipeNeedsTheResearch(GameTestHelper helper) {
      FakePlayer player = player(helper);
      ItemStack c = new ItemStack(Items.DIAMOND);
      ItemStack q = new ItemStack(Items.QUARTZ);
      ItemStack charm = new ItemStack(ModItems.PRIMAL_CHARM.get());
      NonNullList<ItemStack> grid = NonNullList.of(ItemStack.EMPTY, c, q, c.copy(), q.copy(), charm, q.copy(), c.copy(), q.copy(), c.copy());
      TransientCraftingContainer container = new TransientCraftingContainer(player.inventoryMenu, 3, 3, grid);

      IArcaneRecipe locked = ThaumcraftCraftingManager.findArcaneRecipe(container, helper.getLevel(), player);
      helper.assertTrue(locked == null, "no recipe without the research");

      complete(player, "focus_primal");
      IArcaneRecipe recipe = ThaumcraftCraftingManager.findArcaneRecipe(container, helper.getLevel(), player);
      helper.assertTrue(recipe instanceof ShapedArcaneRecipe, "the shaped arcane recipe must match");
      helper.assertTrue(recipe.getResultItem(helper.getLevel().registryAccess()).is(ModItems.FOCUS_PRIMAL.get()), "it crafts the primal focus");
      AspectList cost = ((ShapedArcaneRecipe)recipe).getAspects();
      for (Aspect aspect : Aspect.getPrimalAspects()) {
         helper.assertTrue(cost.getAmount(aspect) == 25, "25 of " + aspect.getTag());
      }
      helper.succeed();
   }

   /** The cast pays vis, launches the orb forward and the orb explodes on impact. */
   @GameTest(template = "empty_9x5x9", timeoutTicks = 100)
   public static void primalFocusOrbFliesAndExplodes(GameTestHelper helper) {
      FakePlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "tc_primal"));
      player.moveTo(helper.absoluteVec(new Vec3(4.5, 1.0, 1.5)), 0.0F, 0.0F);
      for (int x = 2; x <= 6; x++) {
         for (int y = 1; y <= 4; y++) {
            helper.setBlock(new BlockPos(x, y, 7), Blocks.STONE);
         }
      }
      Pig pig = helper.spawn(EntityType.PIG, new Vec3(4.5, 1.0, 6.5));
      pig.setNoAi(true);

      ItemWandCasting wandItem = (ItemWandCasting)ModItems.WAND_CASTING.get();
      ItemStack wand = ItemWandCasting.createWand(ModItems.WOOD_WAND_ROD.get(), ModItems.IRON_WAND_CAP.get());
      wandItem.setFocus(wand, new ItemStack(ModItems.FOCUS_PRIMAL.get()));
      AspectList full = new AspectList();
      for (Aspect aspect : Aspect.getPrimalAspects()) {
         full.add(aspect, wandItem.getMaxVis(wand));
      }
      wandItem.storeAllVis(wand, full);
      player.setItemInHand(InteractionHand.MAIN_HAND, wand);

      ((ItemFocusPrimal)ModItems.FOCUS_PRIMAL.get()).onFocusRightClick(wand, helper.getLevel(), player, InteractionHand.MAIN_HAND);

      AABB arena = new AABB(helper.absolutePos(BlockPos.ZERO)).expandTowards(9, 5, 9);
      List<EntityPrimalOrb> orbs = helper.getLevel().getEntitiesOfClass(EntityPrimalOrb.class, arena);
      helper.assertTrue(orbs.size() == 1, "the cast spawns one primal orb");
      helper.assertTrue(wandItem.getVis(wand, Aspect.FIRE) < wandItem.getMaxVis(wand), "the cast pays vis");
      Vec3 motion = orbs.get(0).getDeltaMovement();
      helper.assertTrue(motion.z > 0.1, "the orb must fly where the player looks, motion " + motion);

      helper.succeedWhen(() -> {
         helper.assertTrue(helper.getLevel().getEntitiesOfClass(EntityPrimalOrb.class, arena).isEmpty(), "orb still flying");
         helper.assertTrue(!pig.isAlive() || pig.getHealth() < pig.getMaxHealth(), "the impact explosion must hurt the pig");
      });
   }

   private static FakePlayer player(GameTestHelper helper) {
      FakePlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "tc_eldritch"));
      player.setPos(helper.absoluteVec(Vec3.ZERO));
      return player;
   }

   private static void complete(FakePlayer player, String key) {
      Thaumcraft.setup.getResearchManager().completeResearch(player, key);
   }

   private static ItemStack solvedNote(GameTestHelper helper, String key) {
      ItemStack note = ResearchManager.createNote(new ItemStack(ModItems.RESEARCH_NOTES.get()), key, helper.getLevel());
      ResearchNoteData data = ResearchManager.getData(note);
      data.complete = true;
      ResearchManager.updateData(note, data);
      return note;
   }
}
