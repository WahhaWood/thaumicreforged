package thaumcraft.network.playerdata;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import thaumcraft.network.BasePacket;
import thaumcraft.research.PlayerKnowledge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketSyncWarp extends BasePacket {
   protected int data;
   protected byte type;

   public PacketSyncWarp(Player player, byte type) {
      this.data = 0;
      this.type = 0;
      if (type == 0) {
         this.data = PlayerKnowledge.of(player).getWarpPerm();
      }

      if (type == 1) {
         this.data = PlayerKnowledge.of(player).getWarpSticky();
      }

      if (type == 2) {
         this.data = PlayerKnowledge.of(player).getWarpTemp();
      }

      this.type = type;
   }

   public PacketSyncWarp(FriendlyByteBuf buf) {
      this.data = buf.readInt();
      this.type = buf.readByte();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeInt(this.data);
      buf.writeByte(this.type);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      if (this.type == 0) {
         PlayerKnowledge.of(minecraft.player).setWarpPerm(this.data);
      } else if (this.type == 1) {
         PlayerKnowledge.of(minecraft.player).setWarpSticky(this.data);
      } else {
         PlayerKnowledge.of(minecraft.player).setWarpTemp(this.data);
      }
   }
}
