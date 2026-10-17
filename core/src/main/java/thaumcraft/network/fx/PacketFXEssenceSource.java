package thaumcraft.network.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import thaumcraft.event.common.EssenceHandler;
import thaumcraft.network.BasePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketFXEssenceSource extends BasePacket {
   private final BlockPos blockPos;
   private final byte dx;
   private final byte dy;
   private final byte dz;
   private final int color;

   public PacketFXEssenceSource(BlockPos blockPos, byte dx, byte dy, byte dz, int color) {
      this.blockPos = blockPos;
      this.dx = dx;
      this.dy = dy;
      this.dz = dz;
      this.color = color;
   }

   public PacketFXEssenceSource(FriendlyByteBuf buf) {
      this.blockPos = buf.readBlockPos();
      this.color = buf.readInt();
      this.dx = buf.readByte();
      this.dy = buf.readByte();
      this.dz = buf.readByte();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeBlockPos(this.blockPos);
      buf.writeInt(this.color);
      buf.writeByte(this.dx);
      buf.writeByte(this.dy);
      buf.writeByte(this.dz);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      int n = this.blockPos.getX() - this.dx;
      int n2 = this.blockPos.getY() - this.dy;
      int n3 = this.blockPos.getZ() - this.dz;
      String string = this.blockPos.getX() + ":" + this.blockPos.getY() + ":" + this.blockPos.getZ() + ":" + n + ":" + n2 + ":" + n3 + ":" + this.color;
      if (EssenceHandler.sourceFX.containsKey(string)) {
         EssenceHandler.EssenceSourceFX essenceSourceFX = EssenceHandler.sourceFX.get(string);
         essenceSourceFX.ticks = 15;
         EssenceHandler.sourceFX.remove(string);
         EssenceHandler.sourceFX.put(string, essenceSourceFX);
      } else {
         EssenceHandler.sourceFX.put(string, new EssenceHandler.EssenceSourceFX(this.blockPos, new BlockPos(n, n2, n3), 15, this.color));
      }
   }
}
