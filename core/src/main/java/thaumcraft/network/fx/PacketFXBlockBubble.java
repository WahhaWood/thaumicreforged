package thaumcraft.network.fx;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;
import thaumcraft.network.BasePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketFXBlockBubble extends BasePacket {
   private final BlockPos blockPos;
   private final int color;

   public PacketFXBlockBubble(BlockPos blockPos, int color) {
      this.blockPos = blockPos;
      this.color = color;
   }

   public PacketFXBlockBubble(FriendlyByteBuf buf) {
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
      Level level = minecraft.level;
      RandomSource rand = level.random;
      int x = this.blockPos.getX();
      int y = this.blockPos.getY();
      int z = this.blockPos.getZ();
      Color color = new Color(this.color);
      float n = color.getRed() / 255.0F;
      float n2 = color.getGreen() / 255.0F;
      float n3 = color.getBlue() / 255.0F;

      for (int i = 0; i < Thaumcraft.setup.particleCount(1); i++) {
         Thaumcraft.setup.crucibleBubble(level, x, y + rand.nextFloat(), z + rand.nextFloat(), n, n2, n3);
         Thaumcraft.setup.crucibleBubble(level, x + 1, y + rand.nextFloat(), z + rand.nextFloat(), n, n2, n3);
         Thaumcraft.setup.crucibleBubble(level, x + rand.nextFloat(), y + rand.nextFloat(), z, n, n2, n3);
         Thaumcraft.setup.crucibleBubble(level, x + rand.nextFloat(), y + rand.nextFloat(), z + 1, n, n2, n3);
      }
   }
}
