package thaumcraft.entities.smart.taint.spore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.entities.ITaintedMob;

public class EntityTaintSwarm extends Monster implements ITaintedMob {
   private BlockPos currentFlightTarget;
   public int damBonus;
   public final List<Object> swarm = new ArrayList<>();
   private int attackTime;
   private Entity entityToAttack;
   private static final EntityDataAccessor<Byte> FLAGS = SynchedEntityData.defineId(EntityTaintSwarm.class, EntityDataSerializers.BYTE);

   public EntityTaintSwarm(EntityType<? extends EntityTaintSwarm> type, Level level) {
      super(type, level);
      this.damBonus = 0;
      this.attackTime = 0;
      this.entityToAttack = null;
      this.currentFlightTarget = null;
      this.xpReward = 5;
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 30.0)
         .add(Attributes.ATTACK_DAMAGE, 2.0)
         .add(Attributes.MOVEMENT_SPEED, 0.25)
         .add(Attributes.FOLLOW_RANGE, 32.0);
   }

   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(FLAGS, (byte)0);
   }

   @NotNull
   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
      return super.getAddEntityPacket(entity);
   }

   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      return true;
   }

   public boolean isPushable() {
      return false;
   }

   protected float getSoundVolume() {
      return 0.1F;
   }

   protected SoundEvent getAmbientSound() {
      return null;
   }

   @NotNull
   protected SoundEvent getHurtSound(DamageSource source) {
      return getThaumcraftSound("swarmattack");
   }

   @NotNull
   protected SoundEvent getDeathSound() {
      return getThaumcraftSound("swarmattack");
   }

   public boolean getIsSummoned() {
      return ((Byte)this.entityData.get(FLAGS) & 2) != 0;
   }

   public void setIsSummoned(boolean summoned) {
      byte b = (Byte)this.entityData.get(FLAGS);
      if (summoned) {
         this.entityData.set(FLAGS, (byte)(b | 2));
      } else {
         this.entityData.set(FLAGS, (byte)(b & -3));
      }
   }

   public void tick() {
      super.tick();
      this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
      if (this.level().isClientSide) {
         Iterator unused = this.swarm.iterator();

         while (unused.hasNext()) {
            Object _x = unused.next();
            if (_x == null) {
               unused.remove();
            } else if (_x instanceof Entity _xx && _xx.isRemoved()) {
               unused.remove();
            }
         }

         if (this.swarm.size() < Math.max(Thaumcraft.setup.particleCount(25), 10)) {
            this.swarm.add(Thaumcraft.setup.swarmParticleFX(this.level(), this, 0.22F, 15.0F, 0.08F));
         }
      }

      if (this.attackTime > 0) {
         this.attackTime--;
      }
   }

   public static boolean checkTaintSwarmSpawnRules(
      EntityType<? extends EntityTaintSwarm> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
   ) {
      return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
   }

   private static SoundEvent getThaumcraftSound(String path) {
      return (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", path));
   }

   private static void playThaumcraftSound(Entity e, String path, float volume, float pitch) {
      SoundEvent se = getThaumcraftSound(path);
      if (se != null) {
         e.level().playSound(null, e.blockPosition(), se, SoundSource.HOSTILE, volume, pitch);
      }
   }
}
