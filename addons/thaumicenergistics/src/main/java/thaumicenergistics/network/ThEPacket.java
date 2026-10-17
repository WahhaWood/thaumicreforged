package thaumicenergistics.network;

import thaumicenergistics.ThaumicEnergistics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface ThEPacket extends CustomPacketPayload {
   Type<ThEPacket> TYPE = new Type(ThaumicEnergistics.location("packet"));

   void write(RegistryFriendlyByteBuf var1);

   void handle(IPayloadContext var1);

   default Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
