package thaumcraft.network.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;
import thaumcraft.network.BasePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketFXWispZap extends BasePacket {
   private final int source;
   private final int target;

   public PacketFXWispZap(int source, int target) {
      this.source = source;
      this.target = target;
   }

   public PacketFXWispZap(FriendlyByteBuf buf) {
      this.source = buf.readInt();
      this.target = buf.readInt();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeInt(this.source);
      buf.writeInt(this.target);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      Level theWorld = minecraft.level;
      Entity entityByID = this.getEntityByID(this.source, minecraft, theWorld);
      Entity entityByID2 = this.getEntityByID(this.target, minecraft, theWorld);
      if (entityByID != null && entityByID2 != null) {
         Thaumcraft.setup.bolt(theWorld, entityByID, entityByID2);
      }
   }

   @OnlyIn(Dist.CLIENT)
   private Entity getEntityByID(int n, Minecraft minecraft, Level level) {
      return (Entity)(n == minecraft.player.getId() ? minecraft.player : level.getEntity(n));
   }
}
