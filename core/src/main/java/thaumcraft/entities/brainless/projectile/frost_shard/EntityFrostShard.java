package thaumcraft.entities.brainless.projectile.frost_shard;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class EntityFrostShard extends ThrowableItemProjectile {
   private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(EntityFrostShard.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Byte> DATA_FROSTY = SynchedEntityData.defineId(EntityFrostShard.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Float> DATA_BOUNCE = SynchedEntityData.defineId(EntityFrostShard.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> DATA_BOUNCE_LIMIT = SynchedEntityData.defineId(EntityFrostShard.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_FRAGILE = SynchedEntityData.defineId(EntityFrostShard.class, EntityDataSerializers.BOOLEAN);
   public double bounce = 0.5;
   public int bounceLimit = 3;
   public boolean fragile = false;

   public EntityFrostShard(EntityType<? extends EntityFrostShard> type, Level level) {
      super(type, level);
   }

   public EntityFrostShard(EntityType<? extends EntityFrostShard> type, Level level, LivingEntity owner, float scatter) {
      super(type, owner, level);
      this.shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0.0F, this.getLaunchVelocity(), scatter);
   }

   protected float getLaunchVelocity() {
      return 1.5F;
   }

   protected Item getDefaultItem() {
      return Items.SNOWBALL;
   }

   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DATA_DAMAGE, 0.0F);
      builder.define(DATA_FROSTY, (byte)0);
      builder.define(DATA_BOUNCE, 0.5F);
      builder.define(DATA_BOUNCE_LIMIT, 3);
      builder.define(DATA_FRAGILE, false);
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
      return super.getAddEntityPacket(entity);
   }

   protected double getDefaultGravity() {
      return this.isFragile() ? 0.015F : 0.05F;
   }

   public EntityDimensions getDimensions(Pose pose) {
      float d = this.getDamage();
      float s = 0.15F + d * 0.15F;
      return EntityDimensions.scalable(s, s);
   }

   public void tick() {
      super.tick();
      this.mirrorFieldsToData();
      if (this.getFrosty() > 0 && this.level().isClientSide) {
         float unused = this.getDamage() / 10.0F;

         for (int _x = 0; _x < this.getFrosty(); _x++) {
            double _xx = this.getX() - unused + this.random.nextFloat() * unused * 2.0F;
            double _xxx = this.getY() - unused + this.random.nextFloat() * unused * 2.0F;
            double _xxxx = this.getZ() - unused + this.random.nextFloat() * unused * 2.0F;
            this.level().addParticle(ParticleTypes.SNOWFLAKE, _xx, _xxx, _xxxx, 0.0, 0.0, 0.0);
         }
      }

      Vec3 m = this.getDeltaMovement();
      double var20 = Math.sqrt(m.x * m.x + m.z * m.z);
      float yaw = (float)(Math.atan2(m.x, m.z) * 180.0 / Math.PI);
      float pitch = (float)(Math.atan2(m.y, var20) * 180.0 / Math.PI);

      while (pitch - this.xRotO < -180.0F) {
         this.xRotO -= 360.0F;
      }

      while (pitch - this.xRotO >= 180.0F) {
         this.xRotO += 360.0F;
      }

      while (yaw - this.yRotO < -180.0F) {
         this.yRotO -= 360.0F;
      }

      while (yaw - this.yRotO >= 180.0F) {
         this.yRotO += 360.0F;
      }

      this.setXRot(this.xRotO + (pitch - this.xRotO) * 0.2F);
      this.setYRot(this.yRotO + (yaw - this.yRotO) * 0.2F);
   }

   protected void onHitEntity(EntityHitResult hit) {
      Entity target = hit.getEntity();
      int ox = Mth.floor(this.getX()) - Mth.floor(target.getX());
      int oy = Mth.floor(this.getY()) - Mth.floor(target.getY());
      int oz = Mth.floor(this.getZ()) - Mth.floor(target.getZ());
      Vec3 m = this.getDeltaMovement();
      double mx = m.x;
      double my = m.y;
      double mz = m.z;
      if (oz != 0) {
         mz *= -1.0;
      }

      if (ox != 0) {
         mx *= -1.0;
      }

      if (oy != 0) {
         my *= -0.9;
      }

      mx *= 0.66;
      my *= 0.66;
      mz *= 0.66;
      int dmgInt = (int)this.getDamage();
      this.spawnImpactParticles(Blocks.ICE.defaultBlockState(), dmgInt);

      // 1.7.10:135-137 — примороженный осколок замедляет жертву: slowness на
      // 200 тиков с усилением getFrosty()-1. Эффекта не было вовсе, то есть
      // «морозилка» просто наносила урон без замедления.
      if (target instanceof LivingEntity living && this.getFrosty() > 0) {
         living.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 200, this.getFrosty() - 1));
      }

      this.applyBounceAndBackoff(mx, my, mz);
      if (!this.isRemoved()) {
         if (this.bounceLimit-- <= 0) {
            this.killWithIceBurst();
         }

         this.mirrorFieldsToData();
      }
   }

   protected void onHitBlock(BlockHitResult hit) {
      Direction dir = hit.getDirection();
      Vec3 m = this.getDeltaMovement();
      double mx = m.x;
      double my = m.y;
      double mz = m.z;
      if (dir.getStepZ() != 0) {
         mz *= -1.0;
      }

      if (dir.getStepX() != 0) {
         mx *= -1.0;
      }

      if (dir.getStepY() != 0) {
         my *= -0.9;
      }

      BlockPos bp = hit.getBlockPos();
      BlockState state = this.level().getBlockState(bp);
      SoundType st = state.getSoundType();
      this.playSound(st.getBreakSound(), 0.3F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
      int dmgInt = (int)this.getDamage();
      this.spawnImpactParticles(state, dmgInt);
      this.applyBounceAndBackoff(mx, my, mz);
      if (this.bounceLimit-- <= 0) {
         this.killWithIceBurst();
      }

      this.mirrorFieldsToData();
   }

   private void applyBounceAndBackoff(double mx, double my, double mz) {
      mx *= this.bounce;
      my *= this.bounce;
      mz *= this.bounce;
      this.setDeltaMovement(mx, my, mz);
      double var20 = Math.sqrt(mx * mx + my * my + mz * mz);
      if (var20 > 0.0) {
         this.setPos(this.getX() - mx / var20 * 0.05F, this.getY() - my / var20 * 0.05F, this.getZ() - mz / var20 * 0.05F);
      }

      this.markHurt();
   }

   private void spawnImpactParticles(BlockState state, int count) {
      if (count > 0) {
         BlockParticleOption opt = new BlockParticleOption(ParticleTypes.BLOCK, state);
         if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(opt, this.getX(), this.getY(), this.getZ(), count, 0.0, 0.0, 0.0, 0.15);
         } else {
            for (int a = 0; a < count; a++) {
               double vx = 4.0 * (this.random.nextFloat() - 0.5);
               double vy = 0.5;
               double vz = (this.random.nextFloat() - 0.5) * 4.0;
               this.level().addParticle(opt, this.getX(), this.getY(), this.getZ(), vx, vy, vz);
            }
         }
      }
   }

   private void killWithIceBurst() {
      this.discard();
      this.playSound(SoundEvents.GLASS_BREAK, 0.3F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
      int count = (int)(8.0F * this.getDamage());
      this.spawnImpactParticles(Blocks.ICE.defaultBlockState(), count);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putFloat("damage", this.getDamage());
      tag.putBoolean("fragile", this.fragile);
      tag.putInt("frost", this.getFrosty());
      tag.putDouble("bounce", this.bounce);
      tag.putInt("bounceLimit", this.bounceLimit);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setDamage(tag.getFloat("damage"));
      this.fragile = tag.getBoolean("fragile");
      this.setFrosty(tag.getInt("frost"));
      this.bounce = tag.getDouble("bounce");
      this.bounceLimit = tag.getInt("bounceLimit");
      this.mirrorFieldsToData();
   }

   public void setDamage(float damage) {
      this.entityData.set(DATA_DAMAGE, damage);
      this.refreshDimensions();
   }

   public float getDamage() {
      return (Float)this.entityData.get(DATA_DAMAGE);
   }

   public void setFrosty(int frosty) {
      this.entityData.set(DATA_FROSTY, (byte)frosty);
   }

   public int getFrosty() {
      return (Byte)this.entityData.get(DATA_FROSTY);
   }

   public boolean isFragile() {
      return this.fragile;
   }

   private void mirrorFieldsToData() {
   }
}
