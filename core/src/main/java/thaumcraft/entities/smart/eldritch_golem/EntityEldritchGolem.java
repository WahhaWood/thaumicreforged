package thaumcraft.entities.smart.eldritch_golem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Thaumcraft;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.entities.ai.AILongRangeAttack;
import thaumcraft.entities.boss.EntityThaumcraftBoss;
import thaumcraft.entities.smart.modifiers.ChampionModifier;
import thaumcraft.util.EntityUtils;

public class EntityEldritchGolem extends EntityThaumcraftBoss implements IEldritchMob, RangedAttackMob {
   private static final EntityDataAccessor<Boolean> HEADLESS = SynchedEntityData.defineId(EntityEldritchGolem.class, EntityDataSerializers.BOOLEAN);
   private boolean chargingBeam;
   private int beamCharge = 0;
   private int arcing;
   private int ax;
   private int ay;
   private int az;
   private int attackTimer;
   private boolean rangedGoalAdded;
   private AILongRangeAttack rangedGoal;

   public EntityEldritchGolem(EntityType<? extends EntityEldritchGolem> type, Level level) {
      super(type, level);
      this.chargingBeam = false;
      this.arcing = 0;
      this.ax = 0;
      this.ay = 0;
      this.az = 0;
      this.attackTimer = 0;
   }

   public static Builder createAttributes() {
      return AttributeSupplier.builder()
         .add(Attributes.MOVEMENT_SPEED, 0.3)
         .add(Attributes.ATTACK_DAMAGE, 10.0)
         .add(Attributes.MAX_HEALTH, 250.0)
         .add(Attributes.ARMOR, 6.0);
   }

   @Override
   public void generateName() {
      int t = (int)this.getAttributeValue(Holder.direct(EntityUtils.CHAMPION_MOD));
      if (t >= 0 && t < ChampionModifier.mods.length) {
         this.setCustomName(Component.translatable("entity.Thaumcraft.EldritchGolem.name", new Object[]{ChampionModifier.mods[t].getModName()}));
      }
   }

   @Override
   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(HEADLESS, false);
   }

   public boolean isHeadless() {
      return (Boolean)this.entityData.get(HEADLESS);
   }

   public void setHeadless(boolean v) {
      boolean prev = this.isHeadless();
      this.entityData.set(HEADLESS, v);
      if (v && !prev && !this.level().isClientSide && !this.rangedGoalAdded) {
         this.rangedGoal = new AILongRangeAttack(this, 3.0, 1.0, 5, 5, 24.0F);
         this.goalSelector.addGoal(2, this.rangedGoal);
         this.rangedGoalAdded = true;
      }
   }

   protected float getStandingEyeHeight(Pose pose, EntityDimensions size) {
      return this.isHeadless() ? 3.33F : 3.0F;
   }

   public boolean fireImmune() {
      return true;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.IRON_GOLEM_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.IRON_GOLEM_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.attackTimer > 0) {
         this.attackTimer--;
      }

      if (this.level().isClientSide) {
         Vec3 unused = this.getDeltaMovement();
         if (unused.x * unused.x + unused.z * unused.z > 2.5000003E-7F && this.random.nextInt(5) == 0) {
            int _x = Mth.floor(this.getX());
            int _xx = Mth.floor(this.getY() - 0.2);
            int _xxx = Mth.floor(this.getZ());
            BlockPos _xxxx = new BlockPos(_x, _xx, _xxx);
            BlockState _xxxxx = this.level().getBlockState(_xxxx);
            if (!_xxxxx.isAir()) {
               this.level()
                  .addParticle(
                     new BlockParticleOption(ParticleTypes.BLOCK, _xxxxx),
                     this.getX() + (this.random.nextFloat() - 0.5) * this.getBbWidth(),
                     this.getBoundingBox().minY + 0.1,
                     this.getZ() + (this.random.nextFloat() - 0.5) * this.getBbWidth(),
                     4.0 * (this.random.nextFloat() - 0.5),
                     0.5,
                     (this.random.nextFloat() - 0.5) * 4.0
                  );
            }
         }
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (this.level().isClientSide && this.isHeadless()) {
         this.setXRot(0.0F);
         float unused = Mth.cos(-this.yBodyRot * (float) (Math.PI / 180.0) - (float) Math.PI);
         float _x = Mth.sin(-this.yBodyRot * (float) (Math.PI / 180.0) - (float) Math.PI);
         float _xx = -Mth.cos(-this.getXRot() * (float) (Math.PI / 180.0));
         float _xxx = Mth.sin(-this.getXRot() * (float) (Math.PI / 180.0));
         Vec3 _xxxx = new Vec3(_x * _xx, _xxx, unused * _xx);
         if (this.random.nextInt(20) == 0) {
            float _xxxxx = (this.random.nextFloat() - this.random.nextFloat()) / 2.0F;
            float _xxxxxx = (this.random.nextFloat() - this.random.nextFloat()) / 2.0F;
            Thaumcraft.setup
               .spark(
                  (float)(this.getX() + _xxxx.x + _xxxxx),
                  (float)this.getY() + this.getEyeHeight() - 0.25F,
                  (float)(this.getZ() + _xxxx.z + _xxxxxx),
                  0.3F,
                  0.65F + this.random.nextFloat() * 0.1F,
                  1.0F,
                  1.0F,
                  0.8F
               );
         }

         Thaumcraft.setup
            .drawVentParticles(
               this.level(),
               (float)(this.getX() + _xxxx.x * 0.66),
               (float)(this.getY() + this.getEyeHeight() - 0.75),
               (float)(this.getZ() + _xxxx.z * 0.66),
               0.0,
               0.001,
               0.0,
               5592405,
               4.0F
            );
         if (this.arcing > 0) {
            Thaumcraft.setup
               .arcLightning(
                  this.level(),
                  this.getX(),
                  this.getY() + this.getBbHeight() * 0.5,
                  this.getZ(),
                  this.ax + 0.5,
                  this.ay + 1,
                  this.az + 0.5,
                  0.65F + this.random.nextFloat() * 0.1F,
                  1.0F,
                  1.0F,
                  1.0F - this.arcing / 10.0F
               );
            this.arcing--;
         }
      }
   }

   public void handleEntityEvent(byte id) {
      if (id == 4) {
         this.attackTimer = 10;
         this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
      } else if (id == 18) {
         this.spawnTimer = 150;
      } else if (id != 19) {
         super.handleEntityEvent(id);
      } else {
         if (this.level().isClientSide && this.arcing == 0) {
            float unused = 2.0F + this.random.nextFloat() * 2.0F;
            double _x = Math.toRadians(this.random.nextInt(360));
            double _xx = unused * Math.cos(_x);
            double _xxx = unused * Math.sin(_x);
            int _xxxx = Mth.floor(this.getX() + _xx);
            int _xxxxx = Mth.floor(this.getY());
            int _xxxxxx = Mth.floor(this.getZ() + _xxx);

            for (int _xxxxxxx = 0; _xxxxxxx < 5 && this.level().getBlockState(new BlockPos(_xxxx, _xxxxx, _xxxxxx)).isAir(); _xxxxx--) {
               _xxxxxxx++;
            }

            BlockPos _xxxxxxx = new BlockPos(_xxxx, _xxxxx, _xxxxxx);
            BlockPos _xxxxxxxx = _xxxxxxx.above();
            if (this.level().getBlockState(_xxxxxxxx).isAir() && !this.level().getBlockState(_xxxxxxx).isAir()) {
               this.ax = _xxxx;
               this.ay = _xxxxx;
               this.az = _xxxxxx;
               this.arcing = 8 + this.random.nextInt(5);
               this.playModSound("jacobs", 0.8F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
            }
         }
      }
   }

   public int getAttackTimer() {
      return this.attackTimer;
   }

   private void playModSound(String soundId, float volume, float pitch) {
      SoundEvent ev = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", soundId));
      if (ev != null) {
         this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ev, SoundSource.HOSTILE, volume, pitch);
      }
   }

   @Override
   public void performRangedAttack(net.minecraft.world.entity.LivingEntity target, float distanceFactor) {
   }

}
