package thaumcraft.network.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import thaumcraft.network.BasePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketFXBeamPulse extends BasePacket {
   private final int source;
   private final int target;
   private final int color;

   public PacketFXBeamPulse(int source, int target, int color) {
      this.source = source;
      this.target = target;
      this.color = color;
   }

   public PacketFXBeamPulse(FriendlyByteBuf buf) {
      this.source = buf.readInt();
      this.target = buf.readInt();
      this.color = buf.readInt();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeInt(this.source);
      buf.writeInt(this.target);
      buf.writeInt(this.color);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
   }

   @OnlyIn(Dist.CLIENT)
   private Entity getEntityByID(int n, Minecraft minecraft, Level level) {
      return (Entity)(n == minecraft.player.getId() ? minecraft.player : level.getEntity(n));
   }
}
