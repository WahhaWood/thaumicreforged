package thaumcraft.entities.smart.tentacle;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.init.registry.ModDamageType;
import thaumcraft.init.registry.ModEntities;
import thaumcraft.init.registry.ModSounds;

public class EntityTantacle extends Monster implements ITaintedMob {
   public static final TagKey<Block> TAINT_SPAWNABLE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("thaumcraft", "taint_spawnable"));
   public float flailIntensity = 1.0F;
   private int summonCooldown = 0;

   public EntityTantacle(EntityType<? extends Monster> entityType, Level level) {
      super(entityType, level);
      this.xpReward = 10;
   }

   public EntityTantacle(Level level) {
      this((EntityType<? extends Monster>)ModEntities.TENTACLE.get(), level);
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 30.0)
         .add(Attributes.ARMOR, 4.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.75)
         .add(Attributes.FOLLOW_RANGE, 24.0)
         .add(Attributes.MOVEMENT_SPEED, 0.0)
         .add(Attributes.ATTACK_DAMAGE, 6.0);
   }

   public static boolean checkTaintacleSpawnRules(
      EntityType<EntityTantacle> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random
   ) {
      if (!Monster.checkMonsterSpawnRules(type, level, reason, pos, random)) {
         return false;
      } else {
         int light = level.getRawBrightness(pos, 0);
         if (light > 7) {
            return false;
         } else {
            BlockState below = level.getBlockState(pos.below());
            if (!below.is(TAINT_SPAWNABLE)) {
               return false;
            } else {
               AABB box = new AABB(pos).inflate(24.0, 8.0, 24.0);
               List<EntityTantacle> nearby = level.getEntitiesOfClass(EntityTantacle.class, box);
               return nearby.isEmpty();
            }
         }
      }
   }

   public float getShadowSize() {
      return 0.25F;
   }

   public boolean canBeCollidedWith() {
      return true;
   }

   public boolean isPushable() {
      return true;
   }

   public void move(@NotNull MoverType type, @NotNull Vec3 vec3) {
      super.move(type, new Vec3(0.0, vec3.y > 0.0 ? 0.0 : vec3.y, 0.0));
   }

   public void travel(@NotNull Vec3 travelVector) {
      super.travel(Vec3.ZERO);
   }

   protected void updateNoActionTime() {
      super.updateNoActionTime();
      LivingEntity t = this.getTarget();
      if (t != null) {
         this.faceEntity(t, 5.0F);
      }
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide) {
         LivingEntity unused = this.getTarget();
         float _x = this.getBbHeight();
         if (this.tickCount > _x * 10.0F && (this.hurtTime > 0 || this.swingTime > 0 || unused != null && unused.distanceTo(this) < _x)) {
            if (this.flailIntensity < 3.0F) {
               this.flailIntensity += 0.2F;
            }
         } else if (this.flailIntensity > 1.0F) {
            this.flailIntensity -= 0.2F;
         }

         if (this.tickCount < _x * 10.0F && this.onGround()) {
            Thaumcraft.setup.tentacleAriseFX(this);
         }
      }
   }

   public boolean getAgitationState() {
      LivingEntity t = this.getTarget();
      if (t == null) {
         return false;
      } else {
         float h = this.getBbHeight();
         float r = h * 7.0F;
         return t.distanceToSqr(this) < r * r;
      }
   }

   public int tcGetHurtTime() {
      return this.hurtTime;
   }

   public int tcGetAttackTime() {
      return this.swingTime;
   }

   protected void attackEntity(Entity entity, float distance) {
      if (this.swingTime <= 0) {
         float height = this.getBbHeight();
      }
   }

   public boolean doHurtTarget(@NotNull Entity entity) {
      float damage = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
      // 1.7.10:167-175 — урон += 3<<усиление при сопротивлении и -= 2<<усиление
      // при wither. Эффекты в оригинале: Potion.field_76420_g и
      // Potion.field_76437_t; расставка восстановлена по EntityEldritchWarden
      // (строка 370, field_76437_t на 400 тиков) и EntityTaintSwarm (строка 212,
      // то же на 100) — в 1.12.2 это WITHER, — и по совпадению порядка и
      // амплификаторов в EntityThaumcraftBoss (строки 236-238).
      // Здесь стояли DAMAGE_BOOST и WEAKNESS — то есть урон считался не по тем
      // эффектам, которые накладывает сам тайнтакл и ретрансляторы.
      MobEffectInstance effect = this.getEffect(MobEffects.DAMAGE_RESISTANCE);
      if (effect != null) {
         damage += 3 << effect.getAmplifier();
      }

      effect = this.getEffect(MobEffects.WITHER);
      if (effect != null) {
         damage -= 2 << effect.getAmplifier();
      }

      DamageSource source = this.level().damageSources().source(ModDamageType.TENTACLE, this);
      int knock = 0;
      if (entity instanceof LivingEntity && this.level() instanceof ServerLevel serverLevel) {
         ItemStack weapon = this.getMainHandItem();
         damage = EnchantmentHelper.modifyDamage(serverLevel, weapon, entity, source, damage);
         knock += (int)EnchantmentHelper.modifyKnockback(serverLevel, weapon, entity, source, 0.0F);
      }

      boolean hit = entity.hurt(source, damage);
      if (hit) {
         if (knock > 0) {
            float yRot = this.getYRot();
            entity.push(-Mth.sin(yRot * (float) (Math.PI / 180.0)) * knock * 0.5F, 0.1, Mth.cos(yRot * (float) (Math.PI / 180.0)) * knock * 0.5F);
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 0.0, 0.6));
         }

         int fire = this.getFireAspectLevel();
         if (fire > 0) {
            entity.igniteForSeconds(fire * 4);
         }

         if (this.level() instanceof ServerLevel serverLevel) {
            EnchantmentHelper.doPostAttackEffects(serverLevel, entity, source);
         }
      }

      return hit;
   }

   private int getFireAspectLevel() {
      return this.registryAccess()
         .lookupOrThrow(Registries.ENCHANTMENT)
         .get(Enchantments.FIRE_ASPECT)
         .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, this.getMainHandItem()))
         .orElse(0);
   }

   public boolean hurt(@NotNull DamageSource damageSource, float damage) {
      return super.hurt(damageSource, damage);
   }

   public void faceEntity(Entity entity, float maxTurn) {
      double d0 = entity.getX() - this.getX();
      double d1 = entity.getZ() - this.getZ();
      float yaw = (float)(Math.atan2(d1, d0) * 180.0 / Math.PI) - 90.0F;
      this.setYRot(this.updateRotation(this.getYRot(), yaw, maxTurn));
   }

   protected float updateRotation(float current, float target, float max) {
      float delta = Mth.wrapDegrees(target - current);
      if (delta > max) {
         delta = max;
      }

      if (delta < -max) {
         delta = -max;
      }

      return current + delta;
   }

   public int getTalkInterval() {
      return 200;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return (SoundEvent)ModSounds.ROOTS.get();
   }

   protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
      return (SoundEvent)ModSounds.TENTACLE.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)ModSounds.TENTACLE.get();
   }

   public float getVoicePitch() {
      return 1.3F - this.getBbHeight() / 10.0F;
   }

   protected float getSoundVolume() {
      return this.getBbHeight() / 8.0F;
   }

   public static class TaintacleAttackGoal extends Goal {
      private final EntityTantacle mob;

      public TaintacleAttackGoal(EntityTantacle mob) {
         this.mob = mob;
         this.setFlags(EnumSet.of(Flag.LOOK, Flag.TARGET));
      }

      public boolean canUse() {
         LivingEntity t = this.mob.getTarget();
         return t != null && t.isAlive() && !(t instanceof ITaintedMob);
      }

      public boolean canContinueToUse() {
         return this.canUse();
      }

      public void tick() {
         LivingEntity t = this.mob.getTarget();
         if (t != null) {
            this.mob.getLookControl().setLookAt(t, 30.0F, 30.0F);
            float dist = this.mob.distanceTo(t);
            if (this.mob.hasLineOfSight(t)) {
               this.mob.attackEntity(t, dist);
            }
         }
      }
   }
}
