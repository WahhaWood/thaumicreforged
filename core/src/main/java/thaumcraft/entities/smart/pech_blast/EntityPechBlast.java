package thaumcraft.entities.smart.pech_blast;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.ModEntities;

public class EntityPechBlast extends ThrowableProjectile {
   private static final EntityDataAccessor<Integer> STRENGTH = SynchedEntityData.defineId(EntityPechBlast.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(EntityPechBlast.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> NIGHTSHADE = SynchedEntityData.defineId(EntityPechBlast.class, EntityDataSerializers.BOOLEAN);

   public EntityPechBlast(EntityType<? extends EntityPechBlast> type, Level level) {
      super(type, level);
   }

   public EntityPechBlast(Level level) {
      super((EntityType)ModEntities.PECH_BLAST.get(), level);
   }

   public EntityPechBlast(Level level, LivingEntity thrower, int strength, int duration, boolean nightshade) {
      super((EntityType)ModEntities.PECH_BLAST.get(), level);
      this.setOwner(thrower);
      this.setPos(thrower.getX(), thrower.getEyeY() - 0.1, thrower.getZ());
      this.setStrength(strength);
      this.setDuration(duration);
      this.setNightshade(nightshade);
      // super(World, EntityLivingBase) в 1.7.10 вызывает
      // func_70080_a(getLookX(), getLookY(), getLookZ(), 1.5F), то есть
      // нормализованный вектор взгляда × func_70182_d() (1.5F).
      Vec3 look = thrower.getLookAngle().normalize().scale(1.5D);
      this.setDeltaMovement(look.x, look.y, look.z);
      this.setYRot(thrower.getYRot());
      this.setXRot(thrower.getXRot());
   }

   public EntityPechBlast(Level level, double x, double y, double z, int strength, int duration, boolean nightshade) {
      super((EntityType)ModEntities.PECH_BLAST.get(), level);
      this.setPos(x, y, z);
      this.setStrength(strength);
      this.setDuration(duration);
      this.setNightshade(nightshade);
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(STRENGTH, 0);
      builder.define(DURATION, 0);
      builder.define(NIGHTSHADE, false);
   }

   public int getStrength() {
      return (Integer)this.entityData.get(STRENGTH);
   }

   public void setStrength(int v) {
      this.entityData.set(STRENGTH, v);
   }

   public int getDuration() {
      return (Integer)this.entityData.get(DURATION);
   }

   public void setDuration(int v) {
      this.entityData.set(DURATION, v);
   }

   public boolean isNightshade() {
      return (Boolean)this.entityData.get(NIGHTSHADE);
   }

   public void setNightshade(boolean v) {
      this.entityData.set(NIGHTSHADE, v);
   }

   protected double getDefaultGravity() {
      return 0.025F;
   }

   public void tick() {
      if (this.level().isClientSide) {
         for (int unused = 0; unused < 3; unused++) {
            double _x = this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
            double _xx = this.getY() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
            double _xxx = this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
            Thaumcraft.setup.wispFX2(this.level(), _x, _xx, _xxx, 0.3F, 3, true, true, 0.02F);
            double _xxxx = (this.getX() + this.xo) / 2.0 + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
            double _xxxxx = (this.getY() + this.yo) / 2.0 + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
            double _xxxxxx = (this.getZ() + this.zo) / 2.0 + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
            Thaumcraft.setup.wispFX2(this.level(), _xxxx, _xxxxx, _xxxxxx, 0.3F, 2, true, true, 0.02F);
            float _xxxxxxx = (float)this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F;
            float _xxxxxxxx = (float)this.getY() + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F;
            float _xxxxxxxxx = (float)this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F;
            Thaumcraft.setup.sparkle(_xxxxxxx, _xxxxxxxx, _xxxxxxxxx, 5);
         }
      }

      super.tick();
      // func_70071_h_: func_70173_aa > 500 → func_70106_y() (исчезновение)
      if (this.tickCount > 500) {
         this.discard();
      }
   }

   protected void onHit(HitResult hit) {
      if (this.level().isClientSide) {
         for (int unused = 0; unused < 9; unused++) {
            float _x = (this.random.nextFloat() - this.random.nextFloat()) * 0.3F;
            float _xx = (this.random.nextFloat() - this.random.nextFloat()) * 0.3F;
            float _xxx = (this.random.nextFloat() - this.random.nextFloat()) * 0.3F;
            Thaumcraft.setup
               .wispFX3(
                  this.level(),
                  this.getX() + _x,
                  this.getY() + _xx,
                  this.getZ() + _xxx,
                  this.getX() + _x * 8.0F,
                  this.getY() + _xx * 8.0F,
                  this.getZ() + _xxx * 8.0F,
                  0.3F,
                  3,
                  true,
                  0.02F
               );
            _x = (this.random.nextFloat() - this.random.nextFloat()) * 0.3F;
            _xx = (this.random.nextFloat() - this.random.nextFloat()) * 0.3F;
            _xxx = (this.random.nextFloat() - this.random.nextFloat()) * 0.3F;
            Thaumcraft.setup
               .wispFX3(
                  this.level(),
                  this.getX() + _x,
                  this.getY() + _xx,
                  this.getZ() + _xxx,
                  this.getX() + _x * 8.0F,
                  this.getY() + _xx * 8.0F,
                  this.getZ() + _xxx * 8.0F,
                  0.3F,
                  2,
                  true,
                  0.02F
               );
            _x = (this.random.nextFloat() - this.random.nextFloat()) * 0.3F;
            _xx = (this.random.nextFloat() - this.random.nextFloat()) * 0.3F;
            _xxx = (this.random.nextFloat() - this.random.nextFloat()) * 0.3F;
            Thaumcraft.setup
               .wispFX3(
                  this.level(),
                  this.getX() + _x,
                  this.getY() + _xx,
                  this.getZ() + _xxx,
                  this.getX() + _x * 8.0F,
                  this.getY() + _xx * 8.0F,
                  this.getZ() + _xxx * 8.0F,
                  0.3F,
                  0,
                  true,
                  0.02F
               );
         }
      }

      super.onHit(hit);
   }

   @NotNull
   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
      return super.getAddEntityPacket(entity);
   }
}
