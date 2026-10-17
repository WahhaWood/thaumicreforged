package thaumcraft.event.common;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import thaumcraft.network.playerdata.PlayerKnowledgeSync;
import thaumcraft.research.ResearchManager;

public class ThaumcraftCommonEvents {
   @SubscribeEvent
   public void addReloadListener(AddReloadListenerEvent event) {
      event.addListener(ResearchManager.LOADER);
   }

   @SubscribeEvent
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         PlayerKnowledgeSync.syncFull(player);
      }
   }

   @SubscribeEvent
   public void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         PlayerKnowledgeSync.syncFull(player);
      }
   }
}
