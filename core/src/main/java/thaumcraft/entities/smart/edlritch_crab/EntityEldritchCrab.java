package thaumcraft.entities.smart.edlritch_crab;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import thaumcraft.init.registry.ModEntities;

public class EntityEldritchCrab extends Monster {
   private static final EntityDataAccessor<Byte> FLAGS = SynchedEntityData.defineId(EntityEldritchCrab.class, EntityDataSerializers.BYTE);
   private int rideAttackCooldown = 0;

   public EntityEldritchCrab(EntityType<? extends EntityEldritchCrab> entityType, Level level) {
      super(entityType, level);
   }

   public EntityEldritchCrab(Level level) {
      super((EntityType)ModEntities.ELDRITCH_CRAB.get(), level);
      this.xpReward = 6;
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.MOVEMENT_SPEED, 0.3);
   }

   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(FLAGS, (byte)0);
   }

   public void tick() {
      super.tick();
      if (this.tickCount < 20) {
         this.fallDistance = 0.0F;
      }
   }

   public boolean hasHelm() {
      return ((Byte)this.entityData.get(FLAGS) & 1) != 0;
   }

   public void setHelm(boolean value) {
      byte f = (Byte)this.entityData.get(FLAGS);
      f = value ? (byte)(f | 1) : (byte)(f & -2);
      this.entityData.set(FLAGS, f);
      AttributeInstance ms = this.getAttribute(Attributes.MOVEMENT_SPEED);
      if (ms != null) {
         ms.setBaseValue(value ? 0.275 : 0.3);
      }
   }

   public boolean hurt(DamageSource source, float damage) {
      boolean b = super.hurt(source, damage);
      if (b && !this.level().isClientSide) {
      }

      return b;
   }

   public boolean doHurtTarget(Entity target) {
      boolean ok = super.doHurtTarget(target);
      if (ok) {
         this.playSound(lookupSound("crabclaw"), 1.0F, 0.9F + this.level().random.nextFloat() * 0.2F);
      }

      return ok;
   }

   public int getAmbientSoundInterval() {
      return 160;
   }

   protected SoundEvent getAmbientSound() {
      return lookupSound("crabtalk");
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return lookupSoundOr("hostilehurt", SoundEvents.GENERIC_HURT);
   }

   protected SoundEvent getDeathSound() {
      return lookupSound("crabdeath");
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
   }

   public boolean canBeAffected(MobEffectInstance effect) {
      return effect.getEffect() != MobEffects.POISON && super.canBeAffected(effect);
   }

   public boolean isAlliedTo(Entity other) {
      return other instanceof EntityEldritchCrab || super.isAlliedTo(other);
   }

   private void spawnBrokenItemParticles(ItemStack stack) {
      if (!stack.isEmpty()) {
         if (this.level() instanceof ServerLevel sl) {
            Vec3 p = this.position().add(0.0, 0.5, 0.0);
            sl.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack), p.x, p.y, p.z, 8, 0.25, 0.25, 0.25, 0.05);
         }
      }
   }

   private static SoundEvent lookupSound(String path) {
      SoundEvent se = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", path));
      return se != null ? se : SoundEvents.SPIDER_AMBIENT;
   }

   private static SoundEvent lookupSoundOr(String path, SoundEvent fallback) {
      SoundEvent se = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", path));
      return se != null ? se : fallback;
   }
}
