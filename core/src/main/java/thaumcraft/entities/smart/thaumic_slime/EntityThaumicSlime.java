package thaumcraft.entities.smart.thaumic_slime;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Thaumcraft;
import thaumcraft.api.entities.ITaintedMob;

public class EntityThaumicSlime extends Monster implements ITaintedMob {
   private static final float[] spawnChances = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
   private static final EntityDataAccessor<Byte> DATA_SLIME_SIZE = SynchedEntityData.defineId(EntityThaumicSlime.class, EntityDataSerializers.BYTE);
   public float squishAmount;
   public float squishFactor;
   public float prevSquishFactor;
   private int slimeJumpDelay = 0;
   int launched = 10;
   int spitCounter = 100;
   private boolean splitDone;

   public EntityThaumicSlime(EntityType<? extends Monster> type, Level level) {
      super(type, level);
      int i = 1 << this.random.nextInt(3);
      this.slimeJumpDelay = this.random.nextInt(20) + 10;
      this.setSlimeSize(i);
   }

   public EntityThaumicSlime(Level level, LivingEntity par2EntityLiving, LivingEntity par3EntityLiving) {
      this((net.minecraft.world.entity.EntityType) par2EntityLiving.getType(), level);
      this.launched = 10;
      this.spitCounter = 100;
      this.setSlimeSize(1);
      this.setPos(this.getX(), (par2EntityLiving.getBoundingBox().minY + par2EntityLiving.getBoundingBox().maxY) / 2.0, this.getZ());
      double var6 = par3EntityLiving.getX() - par2EntityLiving.getX();
      double var8 = par3EntityLiving.getBoundingBox().minY + par3EntityLiving.getBbHeight() / 3.0F - this.getY();
      double var10 = par3EntityLiving.getZ() - par2EntityLiving.getZ();
      double var12 = Math.sqrt(var6 * var6 + var10 * var10);
      if (var12 >= 1.0E-7) {
         float var14 = (float)(Math.atan2(var10, var6) * 180.0 / Math.PI) - 90.0F;
         float var15 = (float)(-(Math.atan2(var8, var12) * 180.0 / Math.PI));
         double var16 = var6 / var12;
         double var18 = var10 / var12;
         this.moveTo(par2EntityLiving.getX() + var16, this.getY(), par2EntityLiving.getZ() + var18, var14, var15);
         float var20 = (float)var12 * 0.2F;
         this.setThrowableHeading(var6, var8 + var20, var10, 1.5F, 1.0F);
      }
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.MOVEMENT_SPEED, 0.23).add(Attributes.ATTACK_DAMAGE, 0.0);
   }

   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DATA_SLIME_SIZE, (byte)1);
   }

   public void setThrowableHeading(double par1, double par3, double par5, float par7, float par8) {
      double var9 = Math.sqrt(par1 * par1 + par3 * par3 + par5 * par5);
      par1 /= var9;
      par3 /= var9;
      par5 /= var9;
      par1 += this.random.nextGaussian() * 0.0075F * par8;
      par3 += this.random.nextGaussian() * 0.0075F * par8;
      par5 += this.random.nextGaussian() * 0.0075F * par8;
      par1 *= par7;
      par3 *= par7;
      par5 *= par7;
      this.setDeltaMovement(par1, par3, par5);
      double var10 = Math.sqrt(par1 * par1 + par5 * par5);
      float yaw = (float)(Math.atan2(par1, par5) * 180.0 / Math.PI);
      float pitch = (float)(Math.atan2(par3, var10) * 180.0 / Math.PI);
      this.setYRot(yaw);
      this.setXRot(pitch);
      this.yRotO = yaw;
      this.xRotO = pitch;
   }

   public void setSlimeSize(int par1) {
      this.entityData.set(DATA_SLIME_SIZE, (byte)par1);
      AttributeInstance mh = this.getAttribute(Attributes.MAX_HEALTH);
      if (mh != null) {
         mh.setBaseValue(par1);
      }

      this.setHealth(this.getMaxHealth());
      this.refreshDimensions();
   }

   public int getSlimeSize() {
      return (Byte)this.entityData.get(DATA_SLIME_SIZE);
   }

   protected int getAttackStrength() {
      return this.getSlimeSize();
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setSlimeSize(tag.getInt("Size") + 1);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("Size", this.getSlimeSize() - 1);
   }

   public void tick() {
      boolean wasOnGround = this.onGround();
      super.tick();
      this.squishFactor = this.squishFactor + (this.squishAmount - this.squishFactor) * 0.5F;
      this.prevSquishFactor = this.squishFactor;
      int unused = (int)Math.sqrt(this.getSlimeSize());
      if (this.launched > 0) {
         this.launched--;

         for (int _x = 0; _x < unused * (this.launched + 1); _x++) {
            Thaumcraft.setup.slimeJumpFX(this, unused);
         }
      }

      if (this.onGround() && !wasOnGround) {
         for (int var5 = 0; var5 < unused * 8; var5++) {
            Thaumcraft.setup.slimeJumpFX(this, unused);
         }

         this.squishAmount = -0.5F;
      } else if (!this.onGround() && wasOnGround) {
         this.squishAmount = 1.0F;
      }

      this.alterSquishAmount();
   }

   protected void customServerAiStep() {
      super.customServerAiStep();
   }

   protected EntityThaumicSlime getClosestMergableSlime() {
      EntityThaumicSlime closest = null;
      double distance = Double.MAX_VALUE;
      AABB box = new AABB(this.getX(), this.getY(), this.getZ(), this.getX(), this.getY(), this.getZ()).inflate(16.0, 8.0, 16.0);
      List<EntityThaumicSlime> ents = this.level().getEntitiesOfClass(EntityThaumicSlime.class, box);
      if (ents != null && !ents.isEmpty()) {
         for (EntityThaumicSlime slime : ents) {
            if (slime.getId() != this.getId() && slime.tickCount > 100 && slime.getSlimeSize() < 100 && this.distanceToSqr(slime) < distance) {
               closest = slime;
            }

            distance = this.distanceToSqr(slime);
         }
      }

      return closest;
   }

   private void applyJumpHorizontal(float moveStrafing, float moveForward) {
      float yawRad = this.getYRot() * (float) (Math.PI / 180.0);
      Vec3 forward = new Vec3(-Mth.sin(yawRad), 0.0, Mth.cos(yawRad));
      Vec3 right = new Vec3(Mth.cos(yawRad), 0.0, Mth.sin(yawRad));
      Vec3 dir = forward.scale(moveForward).add(right.scale(moveStrafing));
      if (!(dir.lengthSqr() < 1.0E-7)) {
         dir = dir.normalize();
         double speed = 0.2 * Math.sqrt(this.getSlimeSize());
         Vec3 dm = this.getDeltaMovement();
         this.setDeltaMovement(dm.add(dir.x * speed, 0.0, dir.z * speed));
      }
   }

   protected void alterSquishAmount() {
      this.squishAmount *= 0.6F;
   }

   protected int getJumpDelay() {
      return this.random.nextInt(16) + 8;
   }

   public void die(DamageSource source) {
      super.die(source);
   }

   public void playerTouch(Player player) {
      super.playerTouch(player);
   }

   protected boolean canDamagePlayer() {
      return this.getSlimeSize() > 0;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return this.getSlimeSize() > 3 ? SoundEvents.SLIME_HURT : SoundEvents.SLIME_HURT_SMALL;
   }

   protected SoundEvent getDeathSound() {
      return this.getSlimeSize() > 3 ? SoundEvents.SLIME_DEATH : SoundEvents.SLIME_DEATH_SMALL;
   }

   protected SoundEvent getJumpSound() {
      return this.getSlimeSize() > 3 ? SoundEvents.SLIME_JUMP : SoundEvents.SLIME_JUMP_SMALL;
   }

   protected float getSoundVolume() {
      return 0.1F * (float)Math.sqrt(this.getSlimeSize());
   }

   protected int getBaseExperienceReward() {
      return (int)Math.sqrt(this.getSlimeSize());
   }

   protected boolean makesSoundOnJump() {
      return this.getSlimeSize() > 3;
   }

   protected boolean makesSoundOnLand() {
      return this.getSlimeSize() > 5;
   }

   private void playGoreSound() {
      SoundEvent gore = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", "gore"));
      if (gore == null) {
         gore = SoundEvents.SLIME_SQUISH;
      }

      this.level()
         .playSound(
            null,
            this.getX(),
            this.getY(),
            this.getZ(),
            gore,
            SoundSource.HOSTILE,
            1.0F,
            ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 0.8F
         );
   }
}
