package thaumcraft.gametest;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.research.ResearchApi;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.event.common.WarpEvents;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.equipment.blood_sword.ItemBloodSword;
import thaumcraft.item.mob_aspect.ItemMobAspect;
import thaumcraft.network.playerdata.PacketPlayerCompleteToServer;
import thaumcraft.recipe.ThaumcraftCraftingManager;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.ResearchManager;
import thaumcraft.research.ResearchNoteData;

/**
 * From-scratch progression audit: the whole research tree on a fresh player through the real unlock paths,
 * the awakened ichor armor chain through the real recipe lookups, and every recipe ingredient against the
 * stack the game actually produces.
 */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class ProgressionReachabilityGameTest {
   /** Clue triggers that only exist after a research (the item or the place it comes from). */
   private static final Map<String, String> CLUE_GATES = Map.of(
      "prim_pearl", "ender_outer",
      "outerrev", "ender_outer",
      "primal_staff_rod", "focus_primal"
   );

   /** Every research of the book, reached on a fresh player through notes, purchases, clues and warp events. */
   @GameTest(template = "empty_17x9x17", timeoutTicks = 400)
   public static void wholeResearchTreeFromScratch(GameTestHelper helper) {
      FakePlayer player = player(helper, "tc_progress");
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      knowledge.refreshProgressionState();
      List<ResearchItem> all = allResearch();

      // Crimson Rites drop from the cultists of the overworld eldritch rings.
      player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.CRIMSON_RITES.get()));
      player.getMainHandItem().getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

      int brains = 0;
      int warpBeforeMajor = -1;
      List<String> log = new ArrayList<>();
      boolean changed = true;
      int rounds = 0;
      while (changed && rounds++ < 200) {
         changed = false;
         for (ResearchItem ri : all) {
            if (done(player, ri.key)) {
               continue;
            }

            if ((ri.isHidden() || ri.isLost()) && !done(player, "@" + ri.key) && !giveClue(player, ri)) {
               continue;
            }

            if (ri.tags == null || ri.tags.size() == 0 || !ResearchManager.doesPlayerHaveRequisites(player, ri.key)) {
               continue;
            }

            if (ri.isSecondary()) {
               for (Aspect aspect : ri.tags.getAspects()) {
                  knowledge.setAspectPool(aspect, (short)Math.max(knowledge.getAspectPoolFor(aspect), ri.tags.getAmount(aspect)));
               }
               new PacketPlayerCompleteToServer(ri.key, (byte)0).executeServer(player);
            } else {
               ItemStack note = ResearchManager.createNote(new ItemStack(ModItems.RESEARCH_NOTES.get()), ri.key, helper.getLevel());
               ResearchNoteData data = ResearchManager.getData(note);
               data.complete = true;
               ResearchManager.updateData(note, data);
               player.setItemInHand(InteractionHand.MAIN_HAND, note);
               note.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            }

            if (done(player, ri.key)) {
               changed = true;
            } else {
               log.add("completion refused: " + ri.key);
            }
         }

         // Warp events: the real check, run once per round.
         int before = knowledge.getResearchCompleted().size();
         knowledge.setWarpCounter(10_000);
         WarpEvents.checkWarpEvent(player);
         changed |= knowledge.getResearchCompleted().size() != before;
         if (changed) {
            continue;
         }

         // Stalled. Research alone does not give enough warp for eldritch_major: eat zombie brains.
         if (!done(player, "eldritch_major")) {
            warpBeforeMajor = knowledge.getWarpPerm() + knowledge.getWarpSticky();
            while (knowledge.getWarpPerm() + knowledge.getWarpSticky() <= 50 && brains < 5000) {
               ItemStack brain = new ItemStack(ModItems.ZOMBIE_BRAIN.get());
               brain.getItem().finishUsingItem(brain, helper.getLevel(), player);
               brains++;
            }
            changed = true;
            continue;
         }

         // The eldritch eye opens the portal; entering the Outer Lands grants ender_outer.
         if (done(player, "oculus") && !done(player, "ender_outer")) {
            Thaumcraft.setup.getResearchManager().completeResearch(player, "ender_outer");
            changed = true;
         }

         // The primal charm whispers @focus_primal (covered by EldritchUnlockGameTest).
         if (done(player, "eldritch_minor") && !done(player, "@focus_primal") && !done(player, "focus_primal")) {
            Thaumcraft.setup.getResearchManager().completeResearch(player, "@focus_primal");
            changed = true;
         }
      }

      helper.getLevel().getEntities((Entity)null, new AABB(player.blockPosition()).inflate(64.0), e -> !(e instanceof Player)).forEach(Entity::discard);
      // A relog runs the knowledge hooks (pseudo research of addons catch up there).
      Set<String> beforeRelog = new LinkedHashSet<>(knowledge.getResearchCompleted());
      knowledge.refreshProgressionState();
      Set<String> onRelog = new LinkedHashSet<>(knowledge.getResearchCompleted());
      onRelog.removeAll(beforeRelog);
      log.add("completed only on relog: " + onRelog);
      List<String> missing = new ArrayList<>();
      for (ResearchItem ri : all) {
         if (!done(player, ri.key)) {
            missing.add(ri.key + (ResearchManager.doesPlayerHaveRequisites(player, ri.key) ? "" : "(requisites)"));
         }
      }

      Thaumcraft.LOGGER.info(
         "[progression-audit] research {}/{} rounds={} researchWarpBeforeMajor={} brainsEaten={} finalWarp perm={} sticky={} kamiIchorParents={}",
         all.size() - missing.size(), all.size(), rounds, warpBeforeMajor, brains, knowledge.getWarpPerm(), knowledge.getWarpSticky(),
         ResearchApi.getResearch("ichor").parentsHidden.length
      );
      log.forEach(line -> Thaumcraft.LOGGER.info("[progression-audit] {}", line));
      helper.assertTrue(missing.isEmpty(), "not reachable: " + missing);
      helper.assertTrue(onRelog.isEmpty(), "completed only on relog: " + onRelog);
      helper.succeed();
   }

   /** Souls only come from the blood sword: shift + use turns harvesting on, the kill drops the mob's three souls instead of its loot. */
   @GameTest(template = "empty_17x9x17")
   public static void bloodSwordReapsSouls(GameTestHelper helper) {
      FakePlayer player = player(helper, "tc_blood");
      player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.BLOOD_SWORD.get()));
      player.setShiftKeyDown(true);
      player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
      player.setShiftKeyDown(false);
      helper.assertTrue(ItemBloodSword.isActive(player.getMainHandItem()), "shift + use activates soul harvesting");

      Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(8, 1, 8));
      zombie.hurt(player.damageSources().playerAttack(player), 1000.0F);
      List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, zombie.getBoundingBox().inflate(4.0));
      List<Aspect> souls = drops.stream().map(ItemEntity::getItem).filter(s -> s.is(ModItems.MOB_ASPECT.get())).map(ItemMobAspect::getAspect).toList();
      helper.assertTrue(souls.size() == 3 && drops.size() == 3, "zombie drops exactly its three souls, got " + drops.stream().map(ItemEntity::getItem).toList());
      helper.assertTrue(souls.stream().filter(Aspect.FLESH::equals).count() == 2 && souls.contains(Aspect.UNDEAD), "zombie souls are flesh, flesh, undead: " + souls);
      drops.forEach(Entity::discard);
      helper.succeed();
   }

   /** KAMI pulls every artifice research in, so the ichor branch waits for the Outer Lands (infusion claw). */
   @GameTest(template = "empty_1x1x1")
   public static void kamiNeedsTheInfusionClaw(GameTestHelper helper) {
      Set<String> parents = Set.of(ResearchApi.getResearch("ichor").parentsHidden);
      helper.assertTrue(parents.contains("infusion_claw"), "ichor must require infusion_claw through the KAMI expansion");
      helper.assertTrue(!parents.contains("prim_pearl"), "eldritch research is not a direct KAMI parent");
      FakePlayer player = player(helper, "tc_kami");
      for (ResearchItem ri : allResearch()) {
         if (!ri.key.equals("infusion_claw") && !ri.category.equals("eldritch") && !(ri instanceof thaumcraft.api.research.type.KamiResearchItem)) {
            Thaumcraft.setup.getResearchManager().completeResearch(player, ri.key);
         }
      }
      helper.assertFalse(ResearchManager.doesPlayerHaveRequisites(player, "ichor"), "ichor stays locked without infusion_claw");
      Thaumcraft.setup.getResearchManager().completeResearch(player, "infusion_claw");
      helper.assertTrue(ResearchManager.doesPlayerHaveRequisites(player, "ichor"), "infusion_claw is the last missing KAMI parent");
      helper.succeed();
   }

   /** Every recipe on the road to the four awakened ichor pieces, found by the real lookups with natural stacks. */
   @GameTest(template = "empty_1x1x1")
   public static void awakenedIchorChainMatchesRealItems(GameTestHelper helper) {
      FakePlayer player = player(helper, "tc_ichor");
      for (ResearchItem ri : allResearch()) {
         Thaumcraft.setup.getResearchManager().completeResearch(player, ri.key);
      }

      Set<Item> chain = Set.of(
         ModItems.ENDER_SHARD.get(), ModItems.NETHER_SHARD.get(), ModItems.ICHOR.get(), ModItems.ICHOR_CLOTH.get(), ModItems.ICHORIUM.get(),
         ModItems.ICHOR_HELMET.get(), ModItems.ICHOR_CHEST.get(), ModItems.ICHOR_LEGGINGS.get(), ModItems.ICHOR_BOOTS.get(),
         ModItems.PRIMAL_CHARM.get(), ModItems.FOCUS_PRIMAL.get(), ModItems.FOCUS_DEFLECT.get(), ModItems.FOCUS_FLIGHT.get(),
         ModItems.FOCUS_SMELT.get(), ModItems.BRIGHT_NITOR.get(), ModItems.HOVER_HARNESS.get(), ModItems.ARCANE_BORE.get(),
         ModItems.ARCANE_LAMP_GROWTH.get(), ModItems.GOGGLES_REVEALING.get(), ModItems.INFUSION_CLAW.get(),
         ModItems.ADVANCED_ALCHEMICAL_CONSTRUCT.get(), ModItems.ELDRITCH_EYE.get(),
         ModItems.ICHOR_GEM_HELMET.get(), ModItems.ICHOR_GEM_CHEST.get(), ModItems.ICHOR_GEM_LEGGINGS.get(), ModItems.ICHOR_GEM_BOOTS.get()
      );

      Set<Item> found = new LinkedHashSet<>();
      List<String> failures = new ArrayList<>();
      for (RecipeHolder<?> holder : helper.getLevel().getRecipeManager().getRecipes()) {
         Recipe<?> recipe = holder.value();
         ItemStack result = recipe.getResultItem(helper.getLevel().registryAccess());
         if (result.isEmpty() || !chain.contains(result.getItem())) {
            continue;
         }

         boolean ok;
         if (recipe instanceof InfusionRecipe infusion) {
            List<ItemStack> comps = new ArrayList<>();
            infusion.components.forEach(i -> comps.add(natural(i)));
            InfusionRecipe hit = ThaumcraftCraftingManager.findInfusionRecipe(comps, natural(infusion.central), player);
            ok = hit != null && hit.getResultItem().is(result.getItem());
         } else if (recipe instanceof CrucibleRecipe crucible) {
            CrucibleRecipe hit = ThaumcraftCraftingManager.findCrucibleRecipe(crucible.aspects.copy(), natural(crucible.catalyst), player);
            ok = hit != null && hit.getResultItem(helper.getLevel().registryAccess()).is(result.getItem());
         } else if (recipe instanceof IArcaneRecipe && recipe instanceof ShapedRecipe shaped) {
            NonNullList<ItemStack> grid = NonNullList.withSize(9, ItemStack.EMPTY);
            for (int y = 0; y < shaped.getHeight(); y++) {
               for (int x = 0; x < shaped.getWidth(); x++) {
                  grid.set(y * 3 + x, natural(shaped.getIngredients().get(y * shaped.getWidth() + x)));
               }
            }
            IArcaneRecipe hit = ThaumcraftCraftingManager.findArcaneRecipe(
               new TransientCraftingContainer(player.inventoryMenu, 3, 3, grid), helper.getLevel(), player
            );
            ok = hit != null && hit.getResultItem(helper.getLevel().registryAccess()).is(result.getItem());
         } else {
            ok = recipe.getIngredients().stream().allMatch(i -> i.isEmpty() || i.test(natural(i)));
         }

         if (ok) {
            found.add(result.getItem());
         } else {
            failures.add(holder.id().toString());
         }
      }

      List<String> missing = new ArrayList<>();
      chain.stream().filter(i -> !found.contains(i)).forEach(i -> missing.add(i.toString()));
      Thaumcraft.LOGGER.info("[progression-audit] ichor chain: {} items craftable, failing recipes {}, no working recipe {}", found.size(), failures, missing);
      helper.assertTrue(missing.isEmpty(), "no working recipe for " + missing + " (failing: " + failures + ")");
      helper.succeed();
   }

   /** Every ingredient of every Thaumcraft recipe must accept the stack the game itself produces for that item. */
   @GameTest(template = "empty_1x1x1")
   public static void everyIngredientAcceptsNaturalStacks(GameTestHelper helper) {
      Map<String, String> broken = new TreeMap<>();
      for (RecipeHolder<?> holder : helper.getLevel().getRecipeManager().getRecipes()) {
         if (!holder.id().getNamespace().equals(Thaumcraft.MODID)) {
            continue;
         }

         Recipe<?> recipe = holder.value();
         List<Ingredient> ingredients = new ArrayList<>(recipe.getIngredients());
         if (recipe instanceof InfusionRecipe infusion) {
            ingredients.add(infusion.central);
            ingredients.addAll(infusion.components);
         } else if (recipe instanceof CrucibleRecipe crucible) {
            ingredients.add(crucible.catalyst);
         }

         for (Ingredient ingredient : ingredients) {
            if (ingredient.isEmpty()) {
               continue;
            }
            ItemStack stack = natural(ingredient);
            if (stack.isEmpty() || !ingredient.test(stack)) {
               broken.put(holder.id().toString(), stack.isEmpty() ? "no items" : describe(stack));
               break;
            }
         }
      }

      Thaumcraft.LOGGER.info("[progression-audit] {} recipes reject natural stacks", broken.size());
      broken.forEach((id, why) -> Thaumcraft.LOGGER.info("[progression-audit]   {} <- {}", id, why));
      helper.assertTrue(broken.isEmpty(), broken.size() + " recipes reject the stacks the game produces, e.g. " + broken.keySet().stream().limit(5).toList());
      helper.succeed();
   }

   /** The stack the game produces for the item an ingredient shows (jar-filled phial, mob drop, brewed potion). */
   private static ItemStack natural(Ingredient ingredient) {
      ItemStack[] items = ingredient.getItems();
      if (items.length == 0) {
         return ItemStack.EMPTY;
      }

      ItemStack shown = items[0];
      Item item = shown.getItem();
      if (item instanceof IEssenceContainerItem container && item == ModItems.PHIAL_ESSENCE.get()) {
         AspectList aspects = container.getAspects(shown);
         ItemStack phial = new ItemStack(item);
         if (aspects != null && aspects.size() > 0) {
            container.setAspects(phial, aspects);
         }
         return phial;
      }

      if (item == ModItems.MOB_ASPECT.get()) {
         Aspect aspect = ItemMobAspect.getAspect(shown);
         if (ItemMobAspect.isInfused(shown)) {
            return ItemMobAspect.getInfusedStackFromAspect(aspect);
         }
         return ItemMobAspect.isCondensed(shown) ? ItemMobAspect.getCondensedStackFromAspect(aspect) : ItemMobAspect.getStackFromAspect(aspect);
      }

      PotionContents contents = shown.get(DataComponents.POTION_CONTENTS);
      if (contents != null && contents.potion().isPresent() && (item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION)) {
         Holder<Potion> potion = contents.potion().get();
         return PotionContents.createItemStack(item, potion);
      }

      return shown.getComponentsPatch().isEmpty() ? new ItemStack(item) : shown.copy();
   }

   private static String describe(ItemStack stack) {
      ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
      return id + " " + stack.getComponentsPatch();
   }

   /** Scanning a trigger: the real clue roll, repeated as the player would scan more objects. */
   private static boolean giveClue(FakePlayer player, ResearchItem ri) {
      if (ri.tags == null || ri.tags.size() == 0) {
         return false;
      }
      String gate = CLUE_GATES.get(ri.key);
      if (gate != null && !done(player, gate)) {
         return false;
      }

      for (int attempt = 0; attempt < 64 && !done(player, "@" + ri.key); attempt++) {
         if (ri.getItemTriggers() != null) {
            for (Ingredient trigger : ri.getItemTriggers()) {
               if (trigger.getItems().length > 0) {
                  ResearchManager.createClue(player, trigger.getItems()[0].copy(), null);
               }
            }
         }
         if (ri.getEntityTriggers() != null) {
            for (String entity : ri.getEntityTriggers()) {
               ResearchManager.createClue(player, entity, null);
            }
         }
         if (ri.getAspectTriggers() != null) {
            for (Aspect aspect : ri.getAspectTriggers()) {
               ResearchManager.createClue(player, ItemStack.EMPTY, new AspectList().add(aspect, 1));
            }
         }
      }
      return done(player, "@" + ri.key);
   }

   private static List<ResearchItem> allResearch() {
      List<ResearchItem> out = new ArrayList<>();
      for (ResearchCategoryList category : ResearchApi.getResearchLists().values()) {
         out.addAll(category.research.values());
      }
      return out;
   }

   private static boolean done(Player player, String key) {
      return ResearchManager.isResearchComplete(player, key);
   }

   private static FakePlayer player(GameTestHelper helper, String name) {
      FakePlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
      player.setPos(helper.absoluteVec(Vec3.ZERO));
      return player;
   }
}
