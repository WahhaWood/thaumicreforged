package thaumcraft.entities.brainless.projectile.ember;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import thaumcraft.init.registry.ModEntities;

public class EntityEmber extends ThrowableProjectile {
   private static final EntityDataAccessor<Integer> DATA_DURATION = SynchedEntityData.defineId(EntityEmber.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_FIREY = SynchedEntityData.defineId(EntityEmber.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(EntityEmber.class, EntityDataSerializers.FLOAT);
   public int duration = 20;
   public int firey = 0;
   public float damage = 1.0F;

   public EntityEmber(EntityType<? extends EntityEmber> type, Level level) {
      super(type, level);
   }

   public EntityEmber(Level level) {
      this((EntityType<? extends EntityEmber>)ModEntities.EMBER.get(), level);
   }

   public EntityEmber(Level level, LivingEntity owner, float scatter) {
      this((EntityType<? extends EntityEmber>)ModEntities.EMBER.get(), level);
      this.setOwner(owner);
      this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
      this.shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0.0F, 1.0F, scatter);
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(DATA_DURATION, 20);
      builder.define(DATA_FIREY, 0);
      builder.define(DATA_DAMAGE, 1.0F);
   }

   private void syncToData() {
      this.entityData.set(DATA_DURATION, this.duration);
      this.entityData.set(DATA_FIREY, this.firey);
      this.entityData.set(DATA_DAMAGE, this.damage);
   }

   private void syncFromData() {
      this.duration = (Integer)this.entityData.get(DATA_DURATION);
      this.firey = (Integer)this.entityData.get(DATA_FIREY);
      this.damage = (Float)this.entityData.get(DATA_DAMAGE);
   }

   public void tick() {
      if (this.level().isClientSide) {
         this.syncFromData();
      }

      double f = this.duration <= 20 ? 0.95 : 0.975;
      this.setDeltaMovement(this.getDeltaMovement().multiply(f, f, f));
      if (this.onGround()) {
         this.setDeltaMovement(this.getDeltaMovement().multiply(0.66, 0.66, 0.66));
      }

      super.tick();
   }

   protected double getDefaultGravity() {
      return 0.0;
   }

   public boolean isPickable() {
      return false;
   }

   public boolean hurt(DamageSource source, float amount) {
      return false;
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putFloat("damage", this.damage);
      tag.putInt("firey", this.firey);
      tag.putInt("duration", this.duration);
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.damage = tag.getFloat("damage");
      this.firey = tag.getInt("firey");
      this.duration = tag.getInt("duration");
      this.syncToData();
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
      this.syncToData();
      return super.getAddEntityPacket(entity);
   }
}
