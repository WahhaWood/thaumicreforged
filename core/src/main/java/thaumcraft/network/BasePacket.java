package thaumcraft.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;

public abstract class BasePacket implements CustomPacketPayload {
   public static final Type<BasePacket> TYPE = new Type(Thaumcraft.location("packet"));

   public abstract void write(FriendlyByteBuf var1);

   public void handle(IPayloadContext context) {
      if (context.flow().isServerbound()) {
         context.enqueueWork(() -> this.executeServer(context.player() instanceof ServerPlayer serverPlayer ? serverPlayer : null));
      } else {
         context.enqueueWork((Runnable) this::executeClient);
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void executeClient() {
      this.executeClient(Minecraft.getInstance());
   }

   public void executeServer(ServerPlayer player) {
   }

   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
   }

   public Type<BasePacket> type() {
      return TYPE;
   }
}
