package thaumcraft.network.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.network.BasePacket;
import thaumcraft.particles.fx.ParticleUtils;
import thaumcraft.particles.fx.particles.FXVisSparkle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketFXVisDrain extends BasePacket {
   private final BlockPos blockPos;
   private final BlockPos blockPosD;
   private int color;

   public PacketFXVisDrain(BlockPos blockPos, BlockPos blockPosD, int color) {
      this.blockPos = blockPos;
      this.blockPosD = blockPosD.subtract(blockPos);
      this.color = color;
   }

   public PacketFXVisDrain(FriendlyByteBuf buf) {
      this.blockPos = buf.readBlockPos();
      this.blockPosD = buf.readBlockPos().offset(this.blockPos);
      this.color = Aspect.getPrimalAspects().get(buf.readByte()).getColor();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeBlockPos(this.blockPos);
      buf.writeBlockPos(this.blockPosD);
      buf.writeByte(this.color);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      ClientLevel clientWorld = Minecraft.getInstance().level;
      if (clientWorld != null) {
         double startX = this.blockPosD.getX() + 0.4 + clientWorld.random.nextFloat() * 0.2F;
         double startY = this.blockPosD.getY() + 0.4 + clientWorld.random.nextFloat() * 0.2F;
         double startZ = this.blockPosD.getZ() + 0.4 + clientWorld.random.nextFloat() * 0.2F;
         double targetX = this.blockPos.getX() + clientWorld.random.nextFloat();
         double targetY = this.blockPos.getY() + clientWorld.random.nextFloat();
         double targetZ = this.blockPos.getZ() + clientWorld.random.nextFloat();
         FXVisSparkle fxVisSparkle = new FXVisSparkle(clientWorld, startX, startY, startZ, targetX, targetY, targetZ);
         float r = (this.color >> 16 & 0xFF) / 255.0F;
         float g = (this.color >> 8 & 0xFF) / 255.0F;
         float b = (this.color & 0xFF) / 255.0F;
         fxVisSparkle.setColor(r, g, b);
         ParticleUtils.addEffect(fxVisSparkle);
      }
   }
}
