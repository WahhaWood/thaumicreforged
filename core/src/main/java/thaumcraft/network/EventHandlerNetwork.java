package thaumcraft.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.compat.kubejs.ThaumcraftKubeJSAspectBridge;
import thaumcraft.init.registry.Config;
import thaumcraft.item.relics.thaumonomicon.render.ResearchBrowserScreen;
import thaumcraft.network.misc.PacketConfig;
import thaumcraft.network.playerdata.PacketSyncAspectRegistry;
import thaumcraft.network.playerdata.PacketSyncResearchTree;
import thaumcraft.network.playerdata.PlayerKnowledgeSync;
import thaumcraft.research.ResearchManager;

public class EventHandlerNetwork {
   @SubscribeEvent
   public void playerLoggedInEvent(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         PacketHandler.sendTo(
            new PacketSyncAspectRegistry(
               ThaumcraftKubeJSAspectBridge.getCachedAspects(),
               ThaumcraftKubeJSAspectBridge.getCachedObjectTags(),
               ThaumcraftKubeJSAspectBridge.getCachedEntityTags()
            ),
            player
         );
         PacketHandler.sendTo(new PacketSyncResearchTree(ResearchManager.LOADER.getCachedCategories(), ResearchManager.LOADER.getCachedData()), player);
         PlayerKnowledgeSync.syncFull(player);
         PacketHandler.sendTo(new PacketConfig(), player);
      }
   }

   @SubscribeEvent
   public void onDatapackSync(OnDatapackSyncEvent event) {
      if (event.getPlayer() == null) {
         PacketSyncAspectRegistry aspectPacket = new PacketSyncAspectRegistry(
            ThaumcraftKubeJSAspectBridge.getCachedAspects(),
            ThaumcraftKubeJSAspectBridge.getCachedObjectTags(),
            ThaumcraftKubeJSAspectBridge.getCachedEntityTags()
         );
         PacketSyncResearchTree packet = new PacketSyncResearchTree(ResearchManager.LOADER.getCachedCategories(), ResearchManager.LOADER.getCachedData());

         for (ServerPlayer player : event.getPlayerList().getPlayers()) {
            PacketHandler.sendTo(aspectPacket, player);
            PacketHandler.sendTo(packet, player);
            PlayerKnowledgeSync.syncFull(player);
         }
      }
   }

   @SubscribeEvent
   public void clientLogsOut(LoggingOut event) {
      ThaumcraftKubeJSAspectBridge.clearRuntimeData();
      if (Thaumcraft.setup.getClientWorld() != null) {
         Config.wardedStone = Config.CwardedStone;
         Config.allowMirrors = Config.CallowMirrors;
         Config.enableKami = Config.CenableKami;
         Config.auraNodeDebug = Config.CauraNodeDebug;
         Config.hardNode = Config.ChardNode;
         Config.wuss = Config.Cwuss;
         Config.researchDifficulty = Config.CresearchDifficulty;
         Config.aspectTotalCap = Config.CaspectTotalCap;
         ResearchBrowserScreen.resetSelectedCategory();
         Thaumcraft.log.info("Restoring client configs.");
      }
   }
}
