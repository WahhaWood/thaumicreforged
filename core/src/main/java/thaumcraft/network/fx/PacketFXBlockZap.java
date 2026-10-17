package thaumcraft.network.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.network.BasePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketFXBlockZap extends BasePacket {
   private final float x;
   private final float y;
   private final float z;
   private final float dx;
   private final float dy;
   private final float dz;

   public PacketFXBlockZap(float x, float y, float z, float dx, float dy, float dz) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.dx = dx;
      this.dy = dy;
      this.dz = dz;
   }

   public PacketFXBlockZap(FriendlyByteBuf buf) {
      this.x = buf.readFloat();
      this.y = buf.readFloat();
      this.z = buf.readFloat();
      this.dx = buf.readFloat();
      this.dy = buf.readFloat();
      this.dz = buf.readFloat();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeFloat(this.x);
      buf.writeFloat(this.y);
      buf.writeFloat(this.z);
      buf.writeFloat(this.dx);
      buf.writeFloat(this.dy);
      buf.writeFloat(this.dz);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      Level level = minecraft.level;
      Thaumcraft.setup.nodeBolt(minecraft.level, this.x, this.y, this.z, this.dx, this.dy, this.dz);
      minecraft.level
         .playLocalSound(this.x, this.y, this.z, (SoundEvent)ModSounds.ZAP.get(), SoundSource.BLOCKS, 0.1F, 1.0F + level.random.nextFloat() * 0.2F, false);
   }
}
