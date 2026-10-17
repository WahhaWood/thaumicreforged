package thaumcraft.entities.brainless.projectile.golem_orb;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.ModEntities;

public class EntityGolemOrb extends ThrowableProjectile implements IEntityWithComplexSpawn {
   private int targetId = -1;
   private LivingEntity target;
   public boolean red;

   public EntityGolemOrb(EntityType<? extends EntityGolemOrb> entityType, Level level) {
      super(entityType, level);
   }

   public EntityGolemOrb(Level level) {
      super((EntityType)ModEntities.GOLEM_ORB.get(), level);
   }

   public EntityGolemOrb(Level level, LivingEntity owner, LivingEntity target, boolean red) {
      super((EntityType)ModEntities.GOLEM_ORB.get(), owner, level);
      this.target = target;
      this.targetId = target != null ? target.getId() : -1;
      this.red = red;
   }

   protected double getDefaultGravity() {
      return 0.0;
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         if (this.tickCount > (this.red ? 240 : 160)) {
            this.discard();
            return;
         }
      } else if (this.target == null && this.targetId >= 0 && this.level().getEntity(this.targetId) instanceof LivingEntity le) {
         this.target = le;
      }

      if (this.target != null && this.target.isAlive()) {
         double d = this.distanceToSqr(this.target);
         if (d < 1.0E-4) {
            d = 1.0E-4;
         }

         double dx = this.target.getX() - this.getX();
         double dy = this.target.getBoundingBox().minY + this.target.getBbHeight() * 0.6 - this.getY();
         double dz = this.target.getZ() - this.getZ();
         double accel = 0.2;
         dx /= d;
         dy /= d;
         dz /= d;
         Vec3 vel = this.getDeltaMovement().add(dx * accel, dy * accel, dz * accel);
         vel = new Vec3(Mth.clamp(vel.x, -0.25, 0.25), Mth.clamp(vel.y, -0.25, 0.25), Mth.clamp(vel.z, -0.25, 0.25));
         this.setDeltaMovement(vel);
      }
   }

   protected void onHitEntity(EntityHitResult hit) {
      super.onHitEntity(hit);
      if (!this.level().isClientSide) {
         if (this.getOwner() instanceof LivingEntity leOwner) {
            Entity targetEntity = hit.getEntity();
            float base = (float)leOwner.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float dmg = base * (this.red ? 1.0F : 0.6F);
            DamageSource src = this.level().damageSources().indirectMagic(this, leOwner);
            targetEntity.hurt(src, dmg);
            this.playModSound("shock", 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
            Thaumcraft.setup.burst(this.level(), this.getX(), this.getY(), this.getZ(), 1.0F);
            this.discard();
         } else {
            this.discard();
         }
      }
   }

   public boolean hurt(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else {
         this.markHurt();
         Entity attacker = source.getEntity();
         if (attacker != null) {
            Vec3 look = attacker.getLookAngle();
            this.setDeltaMovement(look.scale(0.9));
            this.playModSound("zap", 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
            return true;
         } else {
            return false;
         }
      }
   }

   protected void defineSynchedData(Builder builder) {
   }

   @NotNull
   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
      return super.getAddEntityPacket(entity);
   }

   public void writeSpawnData(RegistryFriendlyByteBuf buf) {
      int id = -1;
      if (this.target != null) {
         id = this.target.getId();
      }

      buf.writeInt(id);
      buf.writeBoolean(this.red);
   }

   public void readSpawnData(RegistryFriendlyByteBuf buf) {
      this.targetId = buf.readInt();
      this.red = buf.readBoolean();
      if (this.targetId >= 0 && this.level().getEntity(this.targetId) instanceof LivingEntity le) {
         this.target = le;
      }
   }

   private void playModSound(String soundId, float volume, float pitch) {
      SoundEvent ev = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", soundId));
      if (ev != null) {
         this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ev, SoundSource.HOSTILE, volume, pitch);
      }
   }
}
