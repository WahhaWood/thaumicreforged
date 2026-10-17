package thaumcraft.network.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import thaumcraft.network.BasePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketFXBlockDig extends BasePacket {
   private BlockPos blockPos;
   private int bi;
   private int md;
   private byte dx;
   private byte dy;
   private byte dz;

   public PacketFXBlockDig(BlockPos blockPos, byte dx, byte dy, byte dz, int bi, int md) {
      this.blockPos = blockPos;
      this.bi = bi;
      this.md = md;
      this.dx = dx;
      this.dy = dy;
      this.dz = dz;
   }

   public PacketFXBlockDig(FriendlyByteBuf buf) {
      this.blockPos = buf.readBlockPos();
      this.bi = buf.readInt();
      this.md = buf.readInt();
      this.dx = buf.readByte();
      this.dy = buf.readByte();
      this.dz = buf.readByte();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeBlockPos(this.blockPos);
      buf.writeInt(this.bi);
      buf.writeInt(this.md);
      buf.writeByte(this.dx);
      buf.writeByte(this.dy);
      buf.writeByte(this.dz);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
   }
}
