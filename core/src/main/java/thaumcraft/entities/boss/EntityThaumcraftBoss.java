package thaumcraft.entities.boss;

import java.util.HashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.entities.IEldritchMob;

public class EntityThaumcraftBoss extends Monster {
   private static final EntityDataAccessor<BlockPos> HOME_POS = SynchedEntityData.defineId(EntityThaumcraftBoss.class, EntityDataSerializers.BLOCK_POS);
   private static final EntityDataAccessor<Integer> DATA_ANGER = SynchedEntityData.defineId(EntityThaumcraftBoss.class, EntityDataSerializers.INT);
   private final ServerBossEvent bossEvent = (ServerBossEvent)new ServerBossEvent(this.getDisplayName(), BossBarColor.PURPLE, BossBarOverlay.PROGRESS)
      .setDarkenScreen(true);
   HashMap<Integer, Integer> aggro = new HashMap<>();
   protected int spawnTimer = 0;

   public EntityThaumcraftBoss(EntityType<? extends Monster> entityType, Level level) {
      super(entityType, level);
      this.xpReward = 50;
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.KNOCKBACK_RESISTANCE, 0.95).add(Attributes.FOLLOW_RANGE, 40.0);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
      this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.8));
      this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
      this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
   }

   public void addAdditionalSaveData(@NotNull CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      BlockPos homePos = this.getHomePos();
      compound.putInt("HomePosX", homePos.getX());
      compound.putInt("HomePosY", homePos.getY());
      compound.putInt("HomePosZ", homePos.getZ());
   }

   public void readAdditionalSaveData(@NotNull CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      int i = compound.getInt("HomePosX");
      int j = compound.getInt("HomePosY");
      int k = compound.getInt("HomePosZ");
      this.setHomePos(new BlockPos(i, j, k));
   }

   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DATA_ANGER, 0);
      builder.define(HOME_POS, BlockPos.ZERO);
   }

   public void baseTick() {
      super.baseTick();
   }

   public void aiStep() {
      if (this.getSpawnTimer() == 0) {
         super.aiStep();
      }

      if (this.getTarget() != null && this.getTarget().isRemoved()) {
         this.setTarget(null);
      }
   }

   public void startSeenByPlayer(@NotNull ServerPlayer player) {
      super.startSeenByPlayer(player);
      this.bossEvent.addPlayer(player);
   }

   public void stopSeenByPlayer(@NotNull ServerPlayer player) {
      super.stopSeenByPlayer(player);
      this.bossEvent.removePlayer(player);
   }

   protected void customServerAiStep() {
      super.customServerAiStep();
      this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
   }

   public void setHomePos(BlockPos blockPos) {
      this.entityData.set(HOME_POS, blockPos);
   }

   public BlockPos getHomePos() {
      return (BlockPos)this.entityData.get(HOME_POS);
   }

   public int getAnger() {
      return (Integer)this.entityData.get(DATA_ANGER);
   }

   public void setAnger(int value) {
      this.entityData.set(DATA_ANGER, value);
   }

   public int getSpawnTimer() {
      return this.spawnTimer;
   }

   public void tick() {
      super.tick();
      if (this.getSpawnTimer() > 0) {
         this.spawnTimer--;
      }

      if (this.getAnger() > 0) {
         this.setAnger(this.getAnger() - 1);
      }

      Level level = this.level();
      if (level.isClientSide && this.random.nextInt(15) == 0 && this.getAnger() > 0) {
         double unused = this.random.nextGaussian() * 0.02;
         double _x = this.random.nextGaussian() * 0.02;
         double _xx = this.random.nextGaussian() * 0.02;
         level.addParticle(
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

   /**
    * Реакция на сильный урон — порт {@code EntityThaumcraftBoss.func_70097_a}
    * (1.7.10, строки 223-253).
    *
    * <p>Раньше метода не было: счётчик {@code anger} тикал вниз, но никто его не
    * выставлял и не давал боссу баффы. То есть удар тяжелее 35 ничем не
    * отличался от слабого, а обещанные 200 тиков ярости не наступали никогда.
    *
    * <p>Расстановка трёх эффектов восстановлена по 1.12.2-референсу того же
    * мода ({@code EntityThaumcraftBoss}, строки 141-143), где имена читаемые:
    * STRENGTH с усилением {@code damage/15}, RESISTANCE и SPEED с
    * {@code damage/40}. Значит в 1.7.10 {@code Potion.field_76428_l} — сила,
    * {@code field_76420_g} — сопротивление, {@code field_76424_c} — скорость.
    * Третий эффект — именно скорость, огнестойкости в 1.7.10 нет.
    *
    * <p>Карта {@code aggro} копит уже нанесённый урон по каждому атакующему и
    * складывается со следующим — в 1.7.10 она читалась при выборе цели.
    *
    * <p>Отдельно важно то, что происходит после ярости: 1.7.10:252 обрезает
    * урон до 35 и передаёт дальше уже обрезанным. То есть крит в 60 проходит
    * как 35, и босса нельзя убить одним сильным ударом — защита была не
    * косметической.
    */
   public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
      if (!this.level().isClientSide()) {
         if (source.getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker) {
            int targetId = attacker.getId();
            int total = (int)amount + this.aggro.getOrDefault(targetId, 0);
            this.aggro.put(targetId, total);
         }

         if (amount > 35.0F) {
            if (this.getAnger() == 0) {
               this.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                  net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 200, (int)(amount / 15.0F)));
               this.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                  net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 200, (int)(amount / 40.0F)));
               this.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                  net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 200, (int)(amount / 40.0F)));
               this.setAnger(200);

               // 1.7.10:245-249 — разбуженному атакующему игроку сообщают о ярости.
               if (source.getEntity() instanceof Player player) {
                  player.displayClientMessage(
                     net.minecraft.network.chat.Component.literal(this.getDisplayName().getString() + " ")
                        .append(net.minecraft.network.chat.Component.translatable("tc.boss.enrage")),
                     false
                  );
               }
            }

            amount = 35.0F;
         }
      }

      return super.hurt(source, amount);
   }

   public boolean isInvulnerable() {
      return super.isInvulnerable() || this.getSpawnTimer() > 0;
   }

   public boolean isPushable() {
      return super.isPushable() && !this.isInvulnerable();
   }

   protected int decreaseAirSupply(int air) {
      return air;
   }

   public boolean canPickUpLoot() {
      return false;
   }

   protected void populateDefaultEquipmentSlots(@NotNull RandomSource randomSource, @NotNull DifficultyInstance difficultyInstance) {
   }

   protected void populateDefaultEquipmentEnchantments(
      @NotNull ServerLevelAccessor level, @NotNull RandomSource randomSource, @NotNull DifficultyInstance difficultyInstance
   ) {
   }

   public boolean isPassengerOfSameVehicle(@NotNull Entity entity) {
      return entity instanceof IEldritchMob;
   }

   public void generateName() {
   }
}
