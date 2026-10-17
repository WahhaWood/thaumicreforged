package thaumcraft.network.playerdata;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.network.BasePacket;
import thaumcraft.research.PlayerKnowledge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketSyncAspects extends BasePacket {
   protected AspectList data;

   public PacketSyncAspects(Player player) {
      this.data = new AspectList();
      this.data = PlayerKnowledge.of(player).getAspectsDiscovered();
   }

   public PacketSyncAspects(FriendlyByteBuf buf) {
      this.data = AspectList.fromNetwork(buf);
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      this.data.toNetwork(buf);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      this.data.forEach((aspect, integer) -> Thaumcraft.setup.getResearchManager().completeAspect(minecraft.player, aspect, integer.shortValue()));
   }
}
