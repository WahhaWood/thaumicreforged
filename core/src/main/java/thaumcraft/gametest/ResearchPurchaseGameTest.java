package thaumcraft.gametest;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.network.playerdata.PacketPlayerCompleteToServer;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.ResearchManager;

/** The server checks a research purchase instead of trusting the Thaumonomicon click. */
@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public class ResearchPurchaseGameTest {

   /** Enchanted Fabric is secondary: it costs 3 pannus + 3 praecantatio and needs all of it in the pool. */
   @GameTest(template = "empty_1x1x1")
   public static void secondaryResearchNeedsTheFullCost(GameTestHelper helper) {
      FakePlayer player = player(helper);
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      knowledge.addAspectPool(Aspect.getAspect("pannus"), (short)3);
      knowledge.addAspectPool(Aspect.getAspect("praecantatio"), (short)2);

      new PacketPlayerCompleteToServer("enchfabric", (byte)0).executeServer(player);
      helper.assertFalse(ResearchManager.isResearchComplete(player, "enchfabric"), "a short pool must not buy the research");

      knowledge.addAspectPool(Aspect.getAspect("praecantatio"), (short)1);
      new PacketPlayerCompleteToServer("enchfabric", (byte)0).executeServer(player);
      helper.assertTrue(ResearchManager.isResearchComplete(player, "enchfabric"), "the full cost buys it");
      helper.assertTrue(knowledge.getAspectPoolFor(Aspect.getAspect("pannus")) == 0, "the cost is paid");
      helper.succeed();
   }

   /** Bath Salts is a primary research: it needs a research note, aspects cannot buy it. */
   @GameTest(template = "empty_1x1x1")
   public static void primaryResearchCannotBeBought(GameTestHelper helper) {
      FakePlayer player = player(helper);
      PlayerKnowledge knowledge = PlayerKnowledge.of(player);
      for (String tag : new String[]{"cognitio", "auram", "ordo", "sano"}) {
         knowledge.addAspectPool(Aspect.getAspect(tag), (short)10);
      }

      new PacketPlayerCompleteToServer("bath_salts", (byte)0).executeServer(player);

      helper.assertFalse(ResearchManager.isResearchComplete(player, "bath_salts"), "primary research must go through notes");
      helper.succeed();
   }

   @GameTest(template = "empty_1x1x1")
   public static void debugCompletionNeedsOperator(GameTestHelper helper) {
      FakePlayer player = player(helper);

      new PacketPlayerCompleteToServer("enchfabric", (byte)2).executeServer(player);

      helper.assertFalse(ResearchManager.isResearchComplete(player, "enchfabric"), "debug completion is for operators only");
      helper.succeed();
   }

   private static FakePlayer player(GameTestHelper helper) {
      return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "tc_research"));
   }
}
