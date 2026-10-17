package thaumcraft.network.playerdata;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import thaumcraft.Thaumcraft;
import thaumcraft.network.BasePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketRunicCharge extends BasePacket {
   private final UUID id;
   private final short amount;
   private final short max;

   public PacketRunicCharge(Player player, short amount, int max) {
      this.id = player.getUUID();
      this.amount = amount;
      this.max = (short)max;
   }

   public PacketRunicCharge(FriendlyByteBuf buf) {
      this.id = buf.readUUID();
      this.amount = buf.readShort();
      this.max = buf.readShort();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeUUID(this.id);
      buf.writeShort(this.amount);
      buf.writeShort(this.max);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      Thaumcraft.instance.runicEventHandler.runicCharge.put(this.id, Integer.valueOf(this.amount));
      if (this.max > 0) {
         Thaumcraft.instance.runicEventHandler.runicInfo.put(this.id, new Integer[]{Integer.valueOf(this.max), 0, 0, 0, 0});
      } else {
         Thaumcraft.instance.runicEventHandler.runicInfo.remove(this.id);
      }
   }
}
