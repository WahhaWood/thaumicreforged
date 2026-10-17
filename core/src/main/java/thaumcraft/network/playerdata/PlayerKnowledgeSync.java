package thaumcraft.network.playerdata;

import net.minecraft.server.level.ServerPlayer;
import thaumcraft.network.PacketHandler;

public final class PlayerKnowledgeSync {
   private PlayerKnowledgeSync() {
   }

   public static void syncFull(ServerPlayer player) {
      PacketHandler.sendTo(new PacketSyncWipe(), player);
      PacketHandler.sendTo(new PacketSyncResearch(player), player);
      PacketHandler.sendTo(new PacketSyncScannedItems(player), player);
      PacketHandler.sendTo(new PacketSyncScannedEntities(player), player);
      PacketHandler.sendTo(new PacketSyncScannedPhenomena(player), player);
      PacketHandler.sendTo(new PacketSyncAspects(player), player);
      PacketHandler.sendTo(new PacketSyncWarp(player, (byte)0), player);
      PacketHandler.sendTo(new PacketSyncWarp(player, (byte)1), player);
      PacketHandler.sendTo(new PacketSyncWarp(player, (byte)2), player);
   }
}
