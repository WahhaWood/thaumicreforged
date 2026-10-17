package thaumcraft.network.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import thaumcraft.Thaumcraft;
import thaumcraft.network.BasePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketFXBlockSparkle extends BasePacket {
   private final BlockPos blockPos;
   private final int color;

   public PacketFXBlockSparkle(BlockPos blockPos, int color) {
      this.blockPos = blockPos;
      this.color = color;
   }

   public PacketFXBlockSparkle(FriendlyByteBuf buf) {
      this.blockPos = buf.readBlockPos();
      this.color = buf.readInt();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeBlockPos(this.blockPos);
      buf.writeInt(this.color);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      Thaumcraft.setup.blockSparkle(Thaumcraft.setup.getClientWorld(), this.blockPos.getX(), this.blockPos.getY(), this.blockPos.getZ(), this.color, 7);
   }
}
