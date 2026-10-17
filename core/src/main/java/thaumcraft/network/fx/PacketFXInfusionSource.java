package thaumcraft.network.fx;

import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import thaumcraft.block.arcane_pedestal.ArcanePedestalBlockEntity;
import thaumcraft.block.infusion_matrix.RunicMatrixBlockEntity;
import thaumcraft.network.BasePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketFXInfusionSource extends BasePacket {
   private final BlockPos blockPos;
   private final byte dx;
   private final byte dy;
   private final byte dz;
   private final int color;

   public PacketFXInfusionSource(BlockPos blockPos, byte dx, byte dy, byte dz, int color) {
      this.blockPos = blockPos;
      this.dx = dx;
      this.dy = dy;
      this.dz = dz;
      this.color = color;
   }

   public PacketFXInfusionSource(FriendlyByteBuf buf) {
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
      String string = n + ":" + n2 + ":" + n3 + ":" + this.color;
      if (minecraft.level.getBlockEntity(this.blockPos) instanceof RunicMatrixBlockEntity infusionMatrix) {
         BlockPos pedestalPos = this.blockPos.offset(-this.dx, -this.dy, -this.dz);
         int ticks = 15;
         if (minecraft.level.getBlockEntity(pedestalPos) instanceof ArcanePedestalBlockEntity) {
            ticks = 60;
         }

         if (infusionMatrix.sourceFX.containsKey(string)) {
            RunicMatrixBlockEntity.SourceFX sourceFX = infusionMatrix.sourceFX.get(string);
            sourceFX.ticks = ticks;
            infusionMatrix.sourceFX.put(string, sourceFX);
         } else {
            HashMap<String, RunicMatrixBlockEntity.SourceFX> sourceFX2 = infusionMatrix.sourceFX;
            sourceFX2.put(string, new RunicMatrixBlockEntity.SourceFX(pedestalPos, ticks, this.color));
         }
      }
   }
}
