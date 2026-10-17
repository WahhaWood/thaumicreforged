package thaumcraft.entities.smart.tentacle.boss;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.entities.smart.tentacle.EntityTantacle;
import thaumcraft.init.registry.ModEntities;

public class EntityTantacleGiant extends EntityTantacle implements ITaintedMob {
   private static final EntityDataAccessor<Integer> DATA_ANGER = SynchedEntityData.defineId(EntityTantacleGiant.class, EntityDataSerializers.INT);
   private ServerBossEvent bossEvent;

   public EntityTantacleGiant(EntityType<? extends EntityTantacleGiant> entityType, Level level) {
      super(entityType, level);
      this.xpReward = 20;
   }

   public EntityTantacleGiant(Level level) {
      this((EntityType<? extends EntityTantacleGiant>)ModEntities.TENTACLE_GIANT.get(), level);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.level().isClientSide && this.random.nextInt(15) == 0 && this.getAnger() > 0) {
         double unused = this.random.nextGaussian() * 0.02;
         double _x = this.random.nextGaussian() * 0.02;
         double _xx = this.random.nextGaussian() * 0.02;
         this.level()
            .addParticle(
               ParticleTypes.ANGRY_VILLAGER,
               this.getX() + this.random.nextFloat() * this.getBbWidth() - this.getBbWidth() / 2.0,
               this.getBoundingBox().minY + this.getBbHeight() + this.random.nextFloat() * 0.5,
               this.getZ() + this.random.nextFloat() * this.getBbWidth() - this.getBbWidth() / 2.0,
               unused,
               _x,
               _xx
            );
      }
   }

   protected void customServerAiStep() {
      super.customServerAiStep();
      if (this.bossEvent != null) {
         this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
         this.bossEvent.setName(this.getDisplayName());
      }
   }

   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DATA_ANGER, 0);
   }

   public int getAnger() {
      return (Integer)this.entityData.get(DATA_ANGER);
   }

   public void setAnger(int value) {
      this.entityData.set(DATA_ANGER, value);
   }

   public void addAdditionalSaveData(@NotNull CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("Anger", this.getAnger());
   }

   public void readAdditionalSaveData(@NotNull CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.contains("Anger")) {
         this.setAnger(tag.getInt("Anger"));
      }

      if (this.bossEvent != null) {
         this.bossEvent.setName(this.getDisplayName());
      }
   }

   public void setCustomName(@Nullable Component name) {
      super.setCustomName(name);
      if (this.bossEvent != null) {
         this.bossEvent.setName(this.getDisplayName());
      }
   }

   protected int decreaseAirSupply(int air) {
      return air;
   }

   @Override
   public boolean hurt(@NotNull DamageSource damageSource, float damage) {
      if (!this.level().isClientSide && damage > 35.0F) {
         damage = 35.0F;
      }

      return super.hurt(damageSource, damage);
   }
}
