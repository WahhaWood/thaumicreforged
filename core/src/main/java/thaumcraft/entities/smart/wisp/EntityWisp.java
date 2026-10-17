package thaumcraft.entities.smart.wisp;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.init.registry.ModEntities;
import thaumcraft.init.registry.ModSounds;

public class EntityWisp extends FlyingMob {
   private static final EntityDataAccessor<String> DATA_ASPECT = SynchedEntityData.defineId(EntityWisp.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Integer> DATA_ZAP_TARGET = SynchedEntityData.defineId(EntityWisp.class, EntityDataSerializers.INT);
   private static final byte EVT_ZAP = 77;
   @Nullable
   private LivingEntity targetedEntity;
   public double waypointX;
   public double waypointY;
   public double waypointZ;
   private int aggroCooldown;
   public int prevAttackCounter;
   public int attackCounter;
   public int courseChangeCooldown;

   public EntityWisp(EntityType<? extends FlyingMob> type, Level level) {
      super(type, level);
      this.setNoGravity(true);
   }

   public EntityWisp(Level level) {
      this((EntityType<? extends FlyingMob>)ModEntities.WISP.get(), level);
      this.courseChangeCooldown = 0;
      this.targetedEntity = null;
      this.aggroCooldown = 0;
      this.prevAttackCounter = 0;
      this.attackCounter = 0;
      this.xpReward = 5;
      this.waypointX = this.getX();
      this.waypointY = this.getY();
      this.waypointZ = this.getZ();
   }

   public static Builder createAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MAX_HEALTH, 22.0)
         .add(Attributes.ATTACK_DAMAGE, 3.0)
         .add(Attributes.FOLLOW_RANGE, 16.0)
         .add(Attributes.MOVEMENT_SPEED, 0.35)
         .add(Attributes.FLYING_SPEED, 0.45);
   }

   public boolean isIgnoringBlockTriggers() {
      return true;
   }

   protected int decreaseAirSupply(int air) {
      return air;
   }

   public boolean isNoGravity() {
      return true;
   }

   public boolean hurt(@NotNull DamageSource damageSource, float damage) {
      return super.hurt(damageSource, damage);
   }

   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DATA_ASPECT, "");
      builder.define(DATA_ZAP_TARGET, -1);
   }

   public String getAspectTag() {
      return (String)this.entityData.get(DATA_ASPECT);
   }

   public void setAspectTag(String t) {
      this.entityData.set(DATA_ASPECT, t == null ? "" : t);
   }

   public void die(@NotNull DamageSource damageSource) {
      super.die(damageSource);
      if (this.level().isClientSide) {
         Thaumcraft.setup.burst(this.level(), this.getX(), this.getY() + 0.45, this.getZ(), 1.0F);
      }
   }

   public void tick() {
      this.setNoGravity(true);
      super.tick();
      if (this.level().isClientSide) {
         if (this.tickCount <= 1) {
            Thaumcraft.setup.burst(this.level(), this.getX(), this.getY() + 0.45, this.getZ(), 1.0F);
         }

         Aspect unused = Aspect.getAspect(this.getAspectTag());
         if (this.random.nextBoolean() && unused != null) {
            Color _x = new Color(unused.getColor());
            Thaumcraft.setup
               .wispFX(
                  this.level(),
                  this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 0.7F,
                  this.getY() + 0.45 + (this.random.nextFloat() - this.random.nextFloat()) * 0.7F,
                  this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 0.7F,
                  0.1F,
                  _x.getRed() / 255.0F,
                  _x.getGreen() / 255.0F,
                  _x.getBlue() / 255.0F
               );
         }
      }
   }

   public void handleEntityEvent(byte id) {
      if (id == 77) {
         if (this.level().isClientSide) {
            int unused = (Integer)this.entityData.get(DATA_ZAP_TARGET);
            Entity _x = this.level().getEntity(unused);
            if (_x != null) {
               Thaumcraft.setup.bolt(this.level(), this, _x);
            }
         }
      } else {
         super.handleEntityEvent(id);
      }
   }

   protected void customServerAiStep() {
      super.customServerAiStep();
      this.updateEntityActionState();
   }

   protected void updateEntityActionState() {
   }

   private boolean isCourseTraversable(double x, double y, double z, double dist) {
      if (dist <= 1.0E-4) {
         return false;
      } else {
         double stepX = (x - this.getX()) / dist;
         double stepY = (y - this.getY()) / dist;
         double stepZ = (z - this.getZ()) / dist;
         AABB bb = this.getBoundingBox();

         for (int i = 1; i < (int)dist; i++) {
            bb = bb.move(stepX, stepY, stepZ);
            if (!this.level().noCollision(this, bb)) {
               return false;
            }
         }

         BlockPos wp = BlockPos.containing(x, y, z);
         if (!this.level().getFluidState(wp).isEmpty()) {
            return false;
         } else {
            for (int a = 0; a < 11; a++) {
               if (!this.level().isEmptyBlock(wp.below(a))) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   private void ensureAspect(Level level, BlockPos pos) {
      if (!level.isClientSide) {
         String tag = this.getAspectTag();
         if (tag.isEmpty() || Aspect.getAspect(tag) == null) {
            this.setAspectTag(this.pickRandomAspect(level, pos));
         }
      }
   }

   private String pickRandomAspect(Level level, BlockPos pos) {
      boolean eerie = false;
      Optional<ResourceKey<Biome>> key = level.getBiome(pos).unwrapKey();
      if (key.isPresent()) {
         ResourceLocation rl = key.get().location();
         if ("thaumcraft".equals(rl.getNamespace()) && rl.getPath().contains("eerie")) {
            eerie = true;
         }
      }

      if (eerie) {
         Aspect[] pool = new Aspect[]{Aspect.DARKNESS, Aspect.UNDEAD, Aspect.ENTROPY, Aspect.ELDRITCH, Aspect.POISON, Aspect.DEATH};
         return pool[this.random.nextInt(pool.length)].getTag();
      } else {
         if (this.random.nextInt(10) != 0) {
            ArrayList<Aspect> primals = Aspect.getPrimalAspects();
            if (!primals.isEmpty()) {
               return primals.get(this.random.nextInt(primals.size())).getTag();
            }
         } else {
            ArrayList<Aspect> comps = Aspect.getCompoundAspects();
            if (!comps.isEmpty()) {
               return comps.get(this.random.nextInt(comps.size())).getTag();
            }
         }

         return Aspect.AIR.getTag();
      }
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return (SoundEvent)ModSounds.WISP_LIVE.get();
   }

   @Nullable
   protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
      return SoundEvents.GENERIC_HURT;
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)ModSounds.WISP_DEAD.get();
   }

   protected float getSoundVolume() {
      return 0.25F;
   }

   protected boolean shouldDespawnInPeaceful() {
      return true;
   }

   public void addAdditionalSaveData(@NotNull CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putString("Type", this.getAspectTag());
   }

   public void readAdditionalSaveData(@NotNull CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setAspectTag(tag.getString("Type"));
      this.setNoGravity(true);
   }

   public int getMaxSpawnClusterSize() {
      return 2;
   }
}
