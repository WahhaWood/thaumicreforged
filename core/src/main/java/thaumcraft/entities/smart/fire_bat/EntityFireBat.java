package thaumcraft.entities.smart.fire_bat;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import thaumcraft.init.registry.ModDamageType;

/**
 * Огненная летучая мышь (порт {@code thaumcraft.common.entities.monster.EntityFireBat} из TC4).
 *
 * <p>Оригинальные (1.7.10) особенности, воспроизведённые здесь:
 * <ul>
 *   <li>полёт задаётся вручную полями {@code motionX/Y/Z} с инерцией 0.1f
 *       (обычная навигация и {@code travel} отключены);</li>
 *   <li>режим «висит» (hanging) — обнуление движения и проверка опоры сверху;</li>
 *   <li>приоритетные цели: рукотворные летучие мыши атакуют назначенную цель
 *       (поэтому {@link #getAttackTarget()} у призыванных всегда задан), дикие ищут игроков;</li>
 *   <li>собственный урон ({@code func_70785_a}): взрыв при контакте, вампиризм, отбрасывание;</li>
 *   <li>шанс деспавна зависит от освещённости ({@code func_70601_bi}).</li>
 * </ul>
 */
public class EntityFireBat extends Monster {
   private static final int FLAG_HANGING = 0;
   private static final int FLAG_SUMMONED = 1;
   private static final int FLAG_EXPLOSIVE = 2;
   private static final int FLAG_DEVIL = 3;
   private static final int FLAG_VAMPIRE = 4;
   private byte batFlags;
   public int damBonus;
   @Nullable
   private UUID ownerUUID;
   @Nullable
   private BlockPos currentFlightTarget;
   private int attackCooldown;

   public EntityFireBat(EntityType<? extends Monster> type, Level level) {
      super(type, level);
      // func_70105_a(0.5F, 0.9F) + field_70178_ae = true (не падает под гравитацией)
      this.setNoGravity(true);
      this.xpReward = 3;
      this.setPathfindingMalus(PathType.WATER, -1.0F);
      this.setIsBatHanging(true);
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.ATTACK_DAMAGE, 1.0).add(Attributes.FOLLOW_RANGE, 16.0);
   }

   public boolean fireImmune() {
      return true;
   }

   public boolean isPushable() {
      return false;
   }

   protected void doPush(Entity pEntity) {
   }

   public boolean isIgnoringBlockTriggers() {
      return true;
   }

   public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
      return false;
   }

   protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
   }

   /** func_70041_e_ — не деспавнится в мирном режиме (мыши нужны как существа Thaumcraft). */
   @Override
   protected boolean shouldDespawnInPeaceful() {
      return false;
   }

   /** func_70601_bi — деспавн в зависимости от света: чем светлее, тем реже. */
   @Override
   public void checkDespawn() {
      int light = this.level().getMaxLocalRawBrightness(this.blockPosition());
      int threshold = 7;
      if (light <= this.random.nextInt(threshold)) {
         super.checkDespawn();
      }
   }

   @Override
   public boolean canPickUpLoot() {
      return true;
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      }
      // Портировано 1:1: огонь/взрывы мышь не бьют, остальное — да, и мышь отлипает от потолка.
      if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION)) {
         return false;
      }

      if (!this.level().isClientSide && this.getIsBatHanging()) {
         this.setIsBatHanging(false);
      }

      return super.hurt(source, amount);
   }

   @Nullable
   @Override
   protected SoundEvent getAmbientSound() {
      return this.getIsBatHanging() && this.random.nextInt(4) != 0 ? null : SoundEvents.BAT_AMBIENT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.BAT_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.BAT_DEATH;
   }

   @Override
   protected float getSoundVolume() {
      return 0.1F;
   }

   /** func_70636_d — поджог приводит к урону. */
   @Override
   protected void tickDeath() {
      if (this.isOnFire()) {
         this.hurt(this.level().damageSources().onFire(), 1.0F);
      }

      super.tickDeath();
   }

   // ------------------------------------------------------------------ полёт

   /**
    * func_70064_a / func_70069_a — мышь не участвует в стандартной наземной физике:
    * гравитация, трение и ходьба отключены, движение целиком ручное (см. {@link #aiStep()}).
    * Ванильный {@code travel()} — это как раз наземная физика, поэтому он пустой,
    * а применение motion делаем сами.
    */
   @Override
   public void travel(Vec3 pTravelVector) {
   }

   @Override
   public void tick() {
      if (this.level().isClientSide) {
         if (this.getIsExplosive()) {
            this.level().addParticle(
               ParticleTypes.END_ROD,
               this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F,
               this.getY() + this.getBbHeight() * 0.5 + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F,
               this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F,
               0.0, 0.0, 0.0
            );
         }

         if (!this.getIsVampire()) {
            double px = this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
            double py = this.getY() + this.getBbHeight() * 0.5 + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
            double pz = this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
            this.level().addParticle(ParticleTypes.SMOKE, px, py, pz, 0.0, 0.0, 0.0);
            this.level().addParticle(ParticleTypes.FLAME, px, py, pz, 0.0, 0.0, 0.0);
         }
      }

      super.tick();

      // func_70071_h_: вис — обнуляем движение, иначе гасим вертикальную скорость.
      if (this.getIsBatHanging()) {
         this.setDeltaMovement(0.0, 0.0, 0.0);
      } else {
         double dy = this.getDeltaMovement().y * 0.6F;
         this.setDeltaMovement(this.getDeltaMovement().x, dy, this.getDeltaMovement().z);
      }

      // func_70724_aR (перезарядка удара) тикает вручную: отдельного aiStepAttack в 1.21 нет.
      if (this.attackCooldown > 0) {
         this.attackCooldown--;
      }
   }

   /** func_70626_be — управление полётом и реакция на попадание урона. */
   @Override
   public void aiStep() {
      super.aiStep();

      if (this.level().isClientSide) {
         return;
      }

      if (this.getIsBatHanging()) {
         // func_147445_c(x, y+1, z, false) — полноценный блок над мышью пропал, отлипаем
         BlockPos above = this.blockPosition().above();
         if (!this.level().getBlockState(above).isCollisionShapeFullBlock(this.level(), above)) {
            this.setIsBatHanging(false);
            this.level().levelEvent(1015, this.blockPosition(), 0);
         } else if (this.random.nextInt(200) == 0) {
            this.setYRot(this.random.nextInt(360));
         }

         // func_72890_a(this, 4.0) — игрок рядом сбивает мышь с потолка
         if (!this.level().getEntitiesOfClass(
            Player.class, this.getBoundingBox().inflate(4.0), p -> true
         ).isEmpty()) {
            this.setIsBatHanging(false);
            this.level().levelEvent(1015, this.blockPosition(), 0);
         }
      } else {
         LivingEntity target = this.getTarget();
         if (target == null) {
            if (this.getIsSummoned()) {
               // func_70097_a(DamageSource.outOfWorld, 2.0) — потерявшая цель рукотворная мышь умирает
               this.hurt(this.level().damageSources().fellOutOfWorld(), 2.0F);
            }

            if (this.currentFlightTarget != null
               && (!this.level().isLoaded(this.currentFlightTarget) || this.currentFlightTarget.getY() < 1)) {
               this.currentFlightTarget = null;
            }

            if (this.currentFlightTarget == null
               || this.random.nextInt(30) == 0
               || this.currentFlightTarget.distToCenterSqr(
                  this.getX(), this.getY(), this.getZ()
               ) < 4.0F) {
               this.currentFlightTarget = new BlockPos(
                  Mth.floor(this.getX()) + this.random.nextInt(7) - this.random.nextInt(7),
                  Mth.floor(this.getY()) + this.random.nextInt(6) - 2,
                  Mth.floor(this.getZ()) + this.random.nextInt(7) - this.random.nextInt(7)
               );
            }

            double dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
            double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
            double dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
            this.steerFlight(dx, dy, dz);

            if (this.random.nextInt(100) == 0
               && this.level().getBlockState(this.blockPosition().above())
                  .isCollisionShapeFullBlock(this.level(), this.blockPosition().above())) {
               this.setIsBatHanging(true);
            }
         } else {
            double dx = target.getX() - this.getX();
            double dy = target.getY() + target.getBbHeight() * 0.66 - this.getY();
            double dz = target.getZ() - this.getZ();
            this.steerFlight(dx, dy, dz);
         }

         // func_82165_m — творческий игрок отпускает цель
         if (target instanceof Player && ((Player)target).isCreative()) {
            this.setTarget(null);
         }

         // func_70071_h_ применяет motion в tick(); так как travel() отключён,
         // применяем перемещение сами — иначе мышь висит в воздухе.
         this.move(MoverType.SELF, this.getDeltaMovement());
      }
   }

   /** Общий кусок руления полётом: инерция 0.1f с шагом 0.5/0.7/0.5 по осям. */
   private void steerFlight(double dx, double dy, double dz) {
      Vec3 motion = this.getDeltaMovement();
      double mx = motion.x + (Math.signum(dx) * 0.5 - motion.x) * 0.1F;
      double my = motion.y + (Math.signum(dy) * 0.7 - motion.y) * 0.1F;
      double mz = motion.z + (Math.signum(dz) * 0.5 - motion.z) * 0.1F;
      this.setDeltaMovement(mx, my, mz);
      float yaw = (float)(Mth.atan2(mz, mx) * 180.0 / Math.PI) - 90.0F;
      this.yHeadRot = 0.5F;
      this.setYRot(this.getYRot() + Mth.wrapDegrees(yaw - this.getYRot()));
   }

   // ------------------------------------------------------------------- цели

   /** func_70782_k — призыванные мыши не ищут цели сами, дикие ищут игрока в радиусе 12. */
   @Override
   @Nullable
   public LivingEntity getTarget() {
      if (this.getIsSummoned()) {
         return super.getTarget();
      }

      Player nearest = this.level().getNearestPlayer(this, 12.0);
      return nearest != null ? nearest : super.getTarget();
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
      this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1.0));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
      this.targetSelector.addGoal(
         2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, e -> !this.getIsSummoned())
      );
   }

   /** func_70785_a — удар мыши: взрыв / лечение вампира / отбрасывание. */
   @Override
   public boolean doHurtTarget(Entity target) {
      if (this.attackCooldown <= 0 && target instanceof LivingEntity living) {
         // func_70785_a: дистанция + пересечение AABB по вертикали (по Y)
         net.minecraft.world.phys.AABB targetBox = living.getBoundingBox();
         net.minecraft.world.phys.AABB selfBox = this.getBoundingBox();
         if (this.distanceToSqr(living) < Math.max(2.5F, living.getBbWidth() * 1.1F)
            && targetBox.maxY > selfBox.minY
            && targetBox.minY < selfBox.maxY) {
            if (this.getIsSummoned()) {
               thaumcraft.util.EntityUtils.setRecentlyHit(living, 100);
            }

            if (this.getIsVampire()) {
               Player owner = this.getOwnerPlayer();
               // 1.7.10:340-342 — Potion.field_76428_l на 26 тиков с усилением 1.
               // Это СИЛА, а не DIG_SPEED: расставка полей 1.7.10 восстановлена
               // по EntityThaumcraftBoss (строки 236-238), где field_76428_l с
               // усилением damage/15 в 1.12.2 читается как STRENGTH.
               // В 1.21.1 MobEffects.DIG_SPEED — это haste (скорость копания),
               // то есть бат давал владельцу не то.
               if (owner != null && !owner.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST)) {
                  owner.addEffect(
                     new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 26, 1
                     )
                  );
               }

               this.heal(1.0F);
            }

            this.attackCooldown = 20;
            if ((this.getIsExplosive() || this.random.nextInt(10) == 0)
               && !this.level().isClientSide && !this.getIsDevil()) {
               // func_70172_ad = 0 — цель не отбрасывается взрывом
               living.setDeltaMovement(0.0, 0.0, 0.0);
               this.level().explode(
                  this, this.getX(), this.getY(), this.getZ(),
                  1.5F + (this.getIsExplosive() ? this.damBonus * 0.33F : 0.0F),
                  false, net.minecraft.world.level.Level.ExplosionInteraction.NONE
               );
               this.discard();
            } else if (!this.getIsVampire() && !this.random.nextBoolean()) {
               // Укус на 4 (призыванная) / 2 (дикая) HP через нормальный DamageSource с мышью
               // как атакующим: работают киллфид, могилы, тотемы и ивенты урона. Броня по-прежнему
               // игнорируется — через тег bypasses_armor у thaumcraft:firebat (стиль TC4).
               if (!this.level().isClientSide) {
                  living.hurt(
                     this.level().damageSources().source(ModDamageType.FIREBAT, this),
                     this.getIsSummoned() ? 4.0F : 2.0F
                  );
               }
               living.hasImpulse = false;
            } else {
               // func_70652_k — ванильный удар оружием, но импульс мыши и цели обнуляется
               double mx = living.getDeltaMovement().x;
               double my = living.getDeltaMovement().y;
               double mz = living.getDeltaMovement().z;
               super.doHurtTarget(living);
               living.setDeltaMovement(mx, my, mz);
               living.hasImpulse = false;
            }

            this.level().playSound(
               null, this.getX(), this.getY(), this.getZ(),
               SoundEvents.BAT_HURT, SoundSource.NEUTRAL, 0.5F, 0.9F + this.random.nextFloat() * 0.2F
            );
         }
      }

      return true;
   }

   /** func_146068_u — дроп пороха только с диких мышей, призыванные исчезают ни с чем. */
   @Override
   protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, DamageSource source, boolean looting) {
      if (!this.getIsSummoned()) {
         this.spawnAtLocation(new ItemStack(net.minecraft.world.item.Items.GUNPOWDER));
      }
   }

   // ------------------------------------------------------------------ флаги

   public boolean getIsBatHanging() {
      return this.getFlag(0);
   }

   public void setIsBatHanging(boolean v) {
      this.setFlag(0, v);
   }

   public boolean getIsSummoned() {
      return this.getFlag(1);
   }

   public void setIsSummoned(boolean v) {
      this.setFlag(1, v);
      this.refreshCombatAttributes();
   }

   public boolean getIsExplosive() {
      return this.getFlag(2);
   }

   public void setIsExplosive(boolean v) {
      this.setFlag(2, v);
   }

   public boolean getIsDevil() {
      return this.getFlag(3);
   }

   public void setIsDevil(boolean v) {
      this.setFlag(3, v);
      this.refreshCombatAttributes();
   }

   public boolean getIsVampire() {
      return this.getFlag(4);
   }

   public void setIsVampire(boolean v) {
      this.setFlag(4, v);
   }

   public int getDamBonus() {
      return this.damBonus;
   }

   public void setDamBonus(int damBonus) {
      this.damBonus = damBonus;
      this.refreshCombatAttributes();
   }

   @Nullable
   public Player getOwnerPlayer() {
      return this.ownerUUID == null ? null : this.level().getPlayerByUUID(this.ownerUUID);
   }

   public void setOwner(@Nullable Player player) {
      this.ownerUUID = player == null ? null : player.getUUID();
   }

   @Nullable
   public BlockPos getCurrentFlightTarget() {
      return this.currentFlightTarget;
   }

   private boolean getFlag(int bit) {
      return (this.batFlags & 1 << bit) != 0;
   }

   private void setFlag(int bit, boolean v) {
      byte b = this.batFlags;
      this.batFlags = v ? (byte)(b | 1 << bit) : (byte)(b & ~(1 << bit));
   }

   /** func_110147_ax — пересчёт боевых характеристик из флагов. */
   private void refreshCombatAttributes() {
      AttributeInstance max = this.getAttribute(Attributes.MAX_HEALTH);
      if (max != null) {
         double v = this.getIsDevil() ? 15.0 : 5.0;
         max.setBaseValue(v);
         if (this.getHealth() > v) {
            this.setHealth((float)v);
         }
      }

      AttributeInstance dmg = this.getAttribute(Attributes.ATTACK_DAMAGE);
      if (dmg != null) {
         double v = this.getIsSummoned() ? (this.getIsDevil() ? 3.0 : 2.0) + this.getDamBonus() : 1.0;
         dmg.setBaseValue(v);
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putByte("BatFlags", this.batFlags);
      tag.putByte("damBonus", (byte)this.damBonus);
      if (this.ownerUUID != null) {
         tag.putUUID("Owner", this.ownerUUID);
      }
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.batFlags = tag.getByte("BatFlags");
      this.damBonus = tag.getByte("damBonus");
      if (tag.hasUUID("Owner")) {
         this.ownerUUID = tag.getUUID("Owner");
      }
   }
}