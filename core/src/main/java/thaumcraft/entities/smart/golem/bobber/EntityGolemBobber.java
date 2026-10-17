package thaumcraft.entities.smart.golem.bobber;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.entities.smart.golem.EntityGolemBase;

public class EntityGolemBobber extends Projectile {
   private static final EntityDataAccessor<Integer> DATA_FISHER = SynchedEntityData.defineId(EntityGolemBobber.class, EntityDataSerializers.INT);
   @Nullable
   private EntityGolemBase cachedFisher;

   public EntityGolemBobber(EntityType<? extends EntityGolemBobber> type, Level level) {
      super(type, level);
   }

   public EntityGolemBobber(EntityType<? extends EntityGolemBobber> type, Level level, EntityGolemBase fisher) {
      this(type, level);
      this.setFisher(fisher);
      this.repositionToFisher();
      this.shootFromFisher();
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(DATA_FISHER, -1);
   }

   public void setFisher(EntityGolemBase fisher) {
      this.cachedFisher = fisher;
      this.entityData.set(DATA_FISHER, fisher == null ? -1 : fisher.getId());
      this.setOwner(fisher);
   }

   @Nullable
   public EntityGolemBase getFisher() {
      if (this.cachedFisher != null && !this.cachedFisher.isRemoved()) {
         return this.cachedFisher;
      } else {
         int id = (Integer)this.entityData.get(DATA_FISHER);
         if (id < 0) {
            return null;
         } else if (this.level().getEntity(id) instanceof EntityGolemBase g) {
            this.cachedFisher = g;
            return g;
         } else {
            return null;
         }
      }
   }

   private void repositionToFisher() {
      EntityGolemBase fisher = this.getFisher();
      if (fisher != null) {
         this.setPos(fisher.getX(), fisher.getEyeY() - 0.1, fisher.getZ());
         this.setYRot(fisher.getYRot());
         this.setXRot(fisher.getXRot());
      }
   }

   private void shootFromFisher() {
      EntityGolemBase fisher = this.getFisher();
      if (fisher != null) {
         float yaw = fisher.getYRot() * (float) Math.PI / 180.0F;
         float pitch = fisher.getXRot() * (float) Math.PI / 180.0F;
         double x = -Mth.sin(yaw) * Mth.cos(pitch);
         double y = -Mth.sin(pitch);
         double z = Mth.cos(yaw) * Mth.cos(pitch);
         Vec3 dir = new Vec3(x, y, z).normalize().scale(0.4);
         this.setDeltaMovement(dir);
      }
   }

   public void tick() {
      super.tick();
      Vec3 motion = this.getDeltaMovement();
      if (!this.isNoGravity()) {
         motion = motion.add(0.0, -0.03, 0.0);
      }

      this.setDeltaMovement(motion);
      this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
      float damp = this.isInWater() ? 0.9F : 0.92F;
      this.setDeltaMovement(this.getDeltaMovement().scale(damp));
      Vec3 dm = this.getDeltaMovement();
      if (dm.lengthSqr() > 1.0E-5) {
         float yaw = (float)(Mth.atan2(dm.x, dm.z) * (180.0 / Math.PI));
         float pitch = (float)(Mth.atan2(dm.y, dm.horizontalDistance()) * (180.0 / Math.PI));
         this.setYRot(yaw);
         this.setXRot(pitch);
      }
   }

   @NotNull
   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
      return super.getAddEntityPacket(entity);
   }
}
