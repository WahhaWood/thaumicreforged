package thaumcraft.network.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import thaumcraft.network.BasePacket;
import thaumcraft.particles.fx.other.FXShieldRunes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketFXShield extends BasePacket {
   private final int source;
   private final int target;

   public PacketFXShield(int source, int target) {
      this.source = source;
      this.target = target;
   }

   public PacketFXShield(FriendlyByteBuf buf) {
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
      ClientLevel level = minecraft.level;
      if (level != null) {
         Entity entityByID = level.getEntity(this.source);
         if (entityByID != null) {
            if (this.target >= 0) {
               Entity entityByID2 = level.getEntity(this.target);
               float yaw;
               float pitch;
               if (entityByID2 != null) {
                  double dx = entityByID.getX() - entityByID2.getX();
                  double dy = (entityByID.getBoundingBox().minY + entityByID.getBoundingBox().maxY) / 2.0
                     - (entityByID2.getBoundingBox().minY + entityByID2.getBoundingBox().maxY) / 2.0;
                  double dz = entityByID.getZ() - entityByID2.getZ();
                  double dist = Math.sqrt(dx * dx + dz * dz);
                  yaw = (float)(Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
                  pitch = (float)(-(Math.atan2(dy, dist) * 180.0 / Math.PI));
               } else {
                  pitch = 90.0F;
                  yaw = 0.0F;
               }

               minecraft.particleEngine.add(new FXShieldRunes(level, entityByID.getX(), entityByID.getY(), entityByID.getZ(), entityByID, 8, yaw, pitch));
            } else if (this.target == -1) {
               minecraft.particleEngine.add(new FXShieldRunes(level, entityByID.getX(), entityByID.getY(), entityByID.getZ(), entityByID, 8, 0.0F, 90.0F));
               minecraft.particleEngine.add(new FXShieldRunes(level, entityByID.getX(), entityByID.getY(), entityByID.getZ(), entityByID, 8, 0.0F, 270.0F));
            } else if (this.target == -2) {
               minecraft.particleEngine.add(new FXShieldRunes(level, entityByID.getX(), entityByID.getY(), entityByID.getZ(), entityByID, 8, 0.0F, 270.0F));
            } else if (this.target == -3) {
               minecraft.particleEngine.add(new FXShieldRunes(level, entityByID.getX(), entityByID.getY(), entityByID.getZ(), entityByID, 8, 0.0F, 90.0F));
            }
         }
      }
   }
}
