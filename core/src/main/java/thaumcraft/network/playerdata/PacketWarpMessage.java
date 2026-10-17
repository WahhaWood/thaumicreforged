package thaumcraft.network.playerdata;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.network.BasePacket;
import thaumcraft.util.PlayerNotifications;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketWarpMessage extends BasePacket {
   protected int data;
   protected byte type;

   public PacketWarpMessage(byte type, int data) {
      this.data = data;
      this.type = type;
   }

   public PacketWarpMessage(FriendlyByteBuf buf) {
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
      if (this.data != 0) {
         if (this.type == 0 && this.data > 0) {
            if (this.data < 0) {
               PlayerNotifications.addNotification(Component.translatable("tc.removewarp"));
            } else {
               PlayerNotifications.addNotification(Component.translatable("tc.addwarp"));
               minecraft.player.playSound((SoundEvent)ModSounds.WHISPERS.get(), 0.5F, 1.0F);
            }
         } else if (this.type == 1) {
            if (this.data < 0) {
               PlayerNotifications.addNotification(Component.translatable("tc.removewarpsticky"));
            } else {
               PlayerNotifications.addNotification(Component.translatable("tc.addwarpsticky"));
               minecraft.player.playSound((SoundEvent)ModSounds.WHISPERS.get(), 0.5F, 1.0F);
            }
         } else {
            PlayerNotifications.addNotification(Component.translatable(this.data < 0 ? "tc.removewarptemp" : "tc.addwarptemp"));
         }
      }
   }
}
