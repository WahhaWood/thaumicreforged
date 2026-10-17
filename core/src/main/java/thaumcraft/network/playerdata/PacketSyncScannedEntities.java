package thaumcraft.network.playerdata;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import thaumcraft.Thaumcraft;
import thaumcraft.network.BasePacket;
import thaumcraft.research.PlayerKnowledge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketSyncScannedEntities extends BasePacket {
   protected Set<String> data = new HashSet<>();

   public PacketSyncScannedEntities(Player player) {
      this.data = PlayerKnowledge.of(player).getEntitiesScanned();
   }

   public PacketSyncScannedEntities(String key) {
      this.data.add(key);
   }

   public PacketSyncScannedEntities(FriendlyByteBuf buf) {
      this.data = (Set<String>)buf.readCollection(value -> new HashSet(), FriendlyByteBuf::readUtf);
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeCollection(this.data, FriendlyByteBuf::writeUtf);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      this.data.forEach(s -> Thaumcraft.setup.getResearchManager().completeScannedEntity(minecraft.player, s));
   }
}
