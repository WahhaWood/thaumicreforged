package thaumcraft.network.playerdata;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.network.BasePacket;

/** Синхронизация количества «душевных сердец» (Soul Hearts) с клиентом (порт PacketSoulHearts из TT2). */
public class PacketSyncSoulHearts extends BasePacket {
   public static int clientHearts = 0;
   private final int hearts;

   public PacketSyncSoulHearts(int hearts) {
      this.hearts = Math.max(0, hearts);
   }

   public PacketSyncSoulHearts(FriendlyByteBuf buf) {
      this.hearts = buf.readInt();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeInt(this.hearts);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      clientHearts = this.hearts;
   }
}
