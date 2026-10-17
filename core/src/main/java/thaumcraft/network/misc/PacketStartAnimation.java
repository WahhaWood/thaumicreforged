package thaumcraft.network.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import thaumcraft.block.infusion_claw.InfusionClawBlockEntity;
import thaumcraft.network.BasePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketStartAnimation extends BasePacket {
   private final byte type;
   private final BlockPos pos;

   public PacketStartAnimation(byte type, BlockPos pos) {
      this.type = type;
      this.pos = pos;
   }

   public PacketStartAnimation(byte type, int x, int y, int z) {
      this(type, new BlockPos(x, y, z));
   }

   public PacketStartAnimation(FriendlyByteBuf buf) {
      this.type = buf.readByte();
      this.pos = buf.readBlockPos();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeByte(this.type);
      buf.writeBlockPos(this.pos);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      if (minecraft.level != null) {
         if (minecraft.level.getBlockEntity(this.pos) instanceof InfusionClawBlockEntity claw && claw.animationStates != null) {
            claw.animationStates[8] = 1.0F;
            claw.animationStates[9] = 0.0F;
            claw.animationStates[10] = 0.0F;
            claw.animationStates[11] = 0.0F;
         }
      }
   }
}
