package thaumcraft.network.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import thaumcraft.event.client.ClientTickEventsFML;
import thaumcraft.event.client.RenderEventHandler;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.network.BasePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketMiscEvent extends BasePacket {
   private final PacketMiscEvent.Type type;

   public PacketMiscEvent(PacketMiscEvent.Type type) {
      this.type = type;
   }

   public PacketMiscEvent(FriendlyByteBuf buf) {
      this.type = (PacketMiscEvent.Type)buf.readEnum(PacketMiscEvent.Type.class);
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeEnum(this.type);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      LocalPlayer player = minecraft.player;
      switch (this.type) {
         case WARP:
            ClientTickEventsFML.warpVignette = 100;
            player.level()
               .playLocalSound(player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.HEARTBEAT.get(), SoundSource.AMBIENT, 1.0F, 1.0F, false);
            break;
         case MIST:
            RenderEventHandler.fogFiddled = true;
            RenderEventHandler.fogDuration = 2400;
            break;
         case MIST_SHORT:
            RenderEventHandler.fogFiddled = true;
            if (RenderEventHandler.fogDuration < 200) {
               RenderEventHandler.fogDuration = 200;
            }
      }
   }

   public static enum Type {
      WARP,
      MIST,
      MIST_SHORT;
   }
}
