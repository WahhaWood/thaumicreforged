package thaumcraft.network.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import thaumcraft.block.bore.BoreBlockEntity;
import thaumcraft.network.BasePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketBoreDig extends BasePacket {
   private final BlockPos blockPos;
   private final int digloc;
   private final float speedyTime;

   public PacketBoreDig(BlockPos blockPos, int digloc, float speedyTime) {
      this.blockPos = blockPos;
      this.digloc = digloc;
      this.speedyTime = speedyTime;
   }

   public PacketBoreDig(FriendlyByteBuf buf) {
      this.blockPos = buf.readBlockPos();
      this.digloc = buf.readInt();
      this.speedyTime = buf.readFloat();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeBlockPos(this.blockPos);
      buf.writeInt(this.digloc);
      buf.writeFloat(this.speedyTime);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      if (minecraft.level.getBlockEntity(this.blockPos) instanceof BoreBlockEntity bore) {
         bore.getDigEvent(this.digloc, this.speedyTime);
      }
   }
}
