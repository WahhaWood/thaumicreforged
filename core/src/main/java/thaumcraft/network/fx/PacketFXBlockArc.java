package thaumcraft.network.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;
import thaumcraft.entities.smart.cultist_portal.EntityCultistPortal;
import thaumcraft.network.BasePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketFXBlockArc extends BasePacket {
   private final BlockPos blockPos;
   private final int source;

   public PacketFXBlockArc(BlockPos blockPos, int source) {
      this.blockPos = blockPos;
      this.source = source;
   }

   public PacketFXBlockArc(FriendlyByteBuf buf) {
      this.blockPos = buf.readBlockPos();
      this.source = buf.readInt();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeBlockPos(this.blockPos);
      buf.writeInt(this.source);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      Level level = minecraft.level;
      Entity entityByID = level.getEntity(this.source);
      if (entityByID != null) {
         float n = 0.3F - level.random.nextFloat() * 0.1F;
         float n2 = 0.0F;
         float n3 = 0.5F + level.random.nextFloat() * 0.2F;
         if (entityByID instanceof EntityCultistPortal) {
            n = 0.5F + level.random.nextFloat() * 0.2F;
            n2 = 0.0F;
            n3 = 0.0F;
         }

         Thaumcraft.setup
            .arcLightning(
               Thaumcraft.setup.getClientWorld(),
               entityByID.getX(),
               entityByID.getBoundingBox().minY + entityByID.getBbHeight() / 2.0F,
               entityByID.getZ(),
               this.blockPos.getX() + 0.5,
               this.blockPos.getY() + 1,
               this.blockPos.getZ() + 0.5,
               n,
               n2,
               n3,
               0.5F
            );
      }
   }
}
