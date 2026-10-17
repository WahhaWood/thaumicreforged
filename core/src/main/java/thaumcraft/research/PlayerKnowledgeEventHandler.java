package thaumcraft.research;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;

public class PlayerKnowledgeEventHandler {
   @SubscribeEvent
   public void onPlayerClone(Clone event) {
      PlayerKnowledge.of(event.getEntity()).deserializeNBT(PlayerKnowledge.of(event.getOriginal()).serializeNBT());
      PlayerKnowledge.of(event.getEntity()).refreshProgressionState();
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         PlayerKnowledge.of(player).refreshProgressionState();
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onDatapackSync(OnDatapackSyncEvent event) {
      if (event.getPlayer() == null) {
         for (ServerPlayer player : event.getPlayerList().getPlayers()) {
            PlayerKnowledge.of(player).refreshProgressionState();
         }
      }
   }
}
