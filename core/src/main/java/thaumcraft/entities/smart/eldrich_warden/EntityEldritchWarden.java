package thaumcraft.entities.smart.eldrich_warden;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.init.registry.ModEntities;
import thaumcraft.particles.fx.other.FXSonic;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class EntityEldritchWarden extends Monster implements RangedAttackMob, IEldritchMob {
   private static final String[] TITLES = new String[]{
      "Aphoom-Zhah",
      "Basatan",
      "Chaugnar Faugn",
      "Mnomquah",
      "Nyogtha",
      "Oorn",
      "Shaikorth",
      "Rhan-Tegoth",
      "Rhogog",
      "Shudde M'ell",
      "Vulthoom",
      "Yag-Kosha",
      "Yibb-Tstll",
      "Zathog",
      "Zushakon"
   };
   private static final EntityDataAccessor<Integer> DATA_TITLE = SynchedEntityData.defineId(EntityEldritchWarden.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_SPAWN_TIMER = SynchedEntityData.defineId(EntityEldritchWarden.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_FRENZY_COUNTER = SynchedEntityData.defineId(EntityEldritchWarden.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_SHIELD = SynchedEntityData.defineId(EntityEldritchWarden.class, EntityDataSerializers.INT);
   public static final int MAX_SHIELD = 100;
   private int lastHitTime = 0;
   public float armLiftL;
   public float armLiftR;
   private boolean lastBlast;
   private boolean fieldFrenzy;
   private final ServerBossEvent bossEvent = new ServerBossEvent(
      Component.translatable("entity.thaumcraft.eldritch_warden"), BossBarColor.PURPLE, BossBarOverlay.PROGRESS
   );
   private static final byte EVT_ARM_L = 15;
   private static final byte EVT_ARM_R = 16;
   private static final byte EVT_ARM_BOTH = 17;
   private static final byte EVT_SPAWN = 18;
   private static final byte EVT_SONIC = 19;
   public static final ResourceLocation OUTER_DIM = ResourceLocation.fromNamespaceAndPath("thaumcraft", "outer");
   private LongOpenHashSet tcSeenAiry;

   public EntityEldritchWarden(EntityType<? extends EntityEldritchWarden> type, Level level) {
      super(type, level);
      this.armLiftL = 0.0F;
      this.armLiftR = 0.0F;
      this.lastBlast = false;
      this.fieldFrenzy = false;
      this.xpReward = 50;
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
      this.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.RangedAttackGoal(this, 1.0, 30, 40.0F));
      this.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.0, false));
      this.goalSelector.addGoal(5, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(6, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 8.0F));
      this.goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.player.Player.class, true));
   }

   @Override
   protected net.minecraft.sounds.SoundEvent getAmbientSound() {
      return (net.minecraft.sounds.SoundEvent)thaumcraft.init.registry.ModSounds.EGIDLE.get();
   }

   @Override
   protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource damageSource) {
      return (net.minecraft.sounds.SoundEvent)thaumcraft.init.registry.ModSounds.EGATTACK.get();
   }

   @Override
   protected net.minecraft.sounds.SoundEvent getDeathSound() {
      return (net.minecraft.sounds.SoundEvent)thaumcraft.init.registry.ModSounds.EGDEATH.get();
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 200.0)
         .add(Attributes.MOVEMENT_SPEED, 0.33)
         .add(Attributes.ATTACK_DAMAGE, 10.0)
         .add(Attributes.FOLLOW_RANGE, 40.0)
         .add(Attributes.ARMOR, 4.0);
   }

   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DATA_TITLE, 0);
      builder.define(DATA_SPAWN_TIMER, 0);
      builder.define(DATA_FRENZY_COUNTER, 0);
      builder.define(DATA_SHIELD, MAX_SHIELD);
   }

   public int getRunicShield() {
      return (Integer)this.entityData.get(DATA_SHIELD);
   }

   public void setRunicShield(int v) {
      this.entityData.set(DATA_SHIELD, Mth.clamp(v, 0, MAX_SHIELD));
   }

   public int getSpawnTimer() {
      return (Integer)this.entityData.get(DATA_SPAWN_TIMER);
   }

   private void setSpawnTimer(int v) {
      this.entityData.set(DATA_SPAWN_TIMER, Math.max(0, v));
   }

   public int getFrenzyCounter() {
      return (Integer)this.entityData.get(DATA_FRENZY_COUNTER);
   }

   private void setFrenzyCounter(int v) {
      this.entityData.set(DATA_FRENZY_COUNTER, Math.max(0, v));
   }

   private boolean isInOuter() {
      return this.level().dimension().location().equals(OUTER_DIM);
   }

   public String getTitle() {
      int idx = Mth.clamp((Integer)this.entityData.get(DATA_TITLE), 0, TITLES.length - 1);
      return TITLES[idx];
   }

   private void setTitle(int idx) {
      this.entityData.set(DATA_TITLE, Mth.clamp(idx, 0, TITLES.length - 1));
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         if (this.tickCount - this.lastHitTime > 100 && this.getRunicShield() < MAX_SHIELD && this.tickCount % 2 == 0) {
            this.setRunicShield(this.getRunicShield() + 1);
            if (this.getRunicShield() % 25 == 0) {
               this.level().playSound(null, this.getX(), this.getY(), this.getZ(), (net.minecraft.sounds.SoundEvent)thaumcraft.init.registry.ModSounds.RUNIC_SHIELD_CHARGE.get(), net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F);
            }
         }
         this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
      }
      if (this.level().isClientSide) {
         if (this.armLiftL > 0.0F) {
            this.armLiftL = Math.max(0.0F, this.armLiftL - 0.05F);
         }

         if (this.armLiftR > 0.0F) {
            this.armLiftR = Math.max(0.0F, this.armLiftR - 0.05F);
         }

         float unused = (float)(this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
         float _x = (float)(this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
         Thaumcraft.setup.wispFXEG(this.level(), unused, (float)(this.getY() + 0.25 * this.getBbHeight()), _x, this);
         if (this.getSpawnTimer() > 0) {
            float _xx = Math.max(1.0F, this.getBbHeight() * (150 - this.getSpawnTimer()) / 150.0F);

            for (int _xxx = 0; _xxx < 33; _xxx++) {
               Thaumcraft.setup
                  .smokeSpiral(
                     this.level(),
                     this.getX(),
                     this.getBoundingBox().minY + _xx / 2.0F,
                     this.getZ(),
                     _xx,
                     this.random.nextInt(360),
                     Mth.floor(this.getBoundingBox().minY) - 1,
                     2232623
                  );
            }
         }

         this.clientFrenzyFxTick();
      }
   }

   private void placeAiryCross() {
      for (int l = 0; l < 4; l++) {
         int i = Mth.floor(this.getX() + (l % 2 * 2 - 1) * 0.25F);
         int j = Mth.floor(this.getY());
         int k = Mth.floor(this.getZ() + (l / 2 % 2 * 2 - 1) * 0.25F);
         BlockPos p = new BlockPos(i, j, k);
         if (this.level().isEmptyBlock(p)) {
         }
      }
   }

   private void clientFrenzyFxTick() {
      int counter = this.getFrenzyCounter();
      if (counter <= 0) {
         if (this.tcSeenAiry != null) {
            this.tcSeenAiry.clear();
         }
      } else {
         if (this.tcSeenAiry == null) {
            this.tcSeenAiry = new LongOpenHashSet();
         }

         if (counter < 121 && counter % 10 == 0) {
            double radius = (150 - counter) / 8.0;
            int d = 1 + counter / 8;
            int i = Mth.floor(this.getX());
            int j = Mth.floor(this.getY());
            int k = Mth.floor(this.getZ());

            for (int q = 0; q < 180 / d; q++) {
               double radians = Math.toRadians(q * 2.0 * d);
               int dx = (int)(radius * Math.cos(radians));
               int dz = (int)(radius * Math.sin(radians));
               BlockPos p = new BlockPos(i + dx, j, k + dz);
               long key = p.asLong();
               if (!this.tcSeenAiry.contains(key)) {
                  this.tcSeenAiry.add(key);
                  if (this.random.nextFloat() < 0.3F) {
                     Thaumcraft.setup
                        .arcLightning(
                           Thaumcraft.setup.getClientWorld(),
                           this.getX(),
                           this.getBoundingBox().minY + this.getBbHeight() / 2.0F,
                           this.getZ(),
                           p.getX() + 0.5,
                           p.getY() + 1,
                           p.getZ() + 0.5,
                           0.3F - this.level().random.nextFloat() * 0.1F,
                           0.0F,
                           0.5F + this.level().random.nextFloat() * 0.2F,
                           0.5F
                        );
                  } else {
                     Thaumcraft.setup.blockSparkle(Thaumcraft.setup.getClientWorld(), p.getX(), p.getY(), p.getZ(), 8388736, 7);
                  }
               }
            }
         }
      }
   }

   public boolean hurt(DamageSource source, float amount) {
      if (this.getFrenzyCounter() > 0) {
         return false;
      } else if (source == this.level().damageSources().drown() || source == this.level().damageSources().wither()) {
         return false;
      } else {
         this.lastHitTime = this.tickCount;
         if (!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_SHIELD) && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) {
            int shield = this.getRunicShield();
            if (shield > 0) {
               int absorbed = (int)Math.min(shield, amount);
               this.setRunicShield(shield - absorbed);
               amount -= absorbed;
               this.level().playSound(null, this.getX(), this.getY(), this.getZ(), (net.minecraft.sounds.SoundEvent)thaumcraft.init.registry.ModSounds.RUNIC_SHIELD_EFFECT.get(), net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F);
               if (!this.level().isClientSide) {
                  thaumcraft.network.PacketHandler.sendToAllAround(new thaumcraft.network.fx.PacketFXShield(this.getId(), -1), this.level(), this.getX(), this.getY(), this.getZ(), 32.0);
               }
               if (amount <= 0.0F) {
                  return false;
               }
            }
         }
         return super.hurt(source, amount);
      }
   }

   public void performRangedAttack(LivingEntity target, float f) {
      if (this.getFrenzyCounter() > 0) {
         return;
      }
      double dx = target.getX() - this.getX();
      double dy = target.getY(0.5) - (this.getY() + this.getBbHeight() * 0.7);
      double dz = target.getZ() - this.getZ();
      thaumcraft.entities.brainless.eldrich_orb.EntityEldritchOrb orb = new thaumcraft.entities.brainless.eldrich_orb.EntityEldritchOrb(this.level(), this);
      orb.setPos(this.getX(), this.getY() + this.getBbHeight() * 0.7, this.getZ());
      double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (dist > 0.001) {
         orb.shoot(dx, dy, dz, 1.2F, 1.0F);
      }
      this.level().addFreshEntity(orb);
      this.level().playSound(null, this.getX(), this.getY(), this.getZ(), (net.minecraft.sounds.SoundEvent)thaumcraft.init.registry.ModSounds.EGATTACK.get(), net.minecraft.sounds.SoundSource.HOSTILE, 1.5F, 0.8F + this.random.nextFloat() * 0.2F);
      this.level().broadcastEntityEvent(this, (byte)17);
   }

   @Override
   public void startSeenByPlayer(net.minecraft.server.level.ServerPlayer player) {
      super.startSeenByPlayer(player);
      this.bossEvent.addPlayer(player);
   }

   @Override
   public void stopSeenByPlayer(net.minecraft.server.level.ServerPlayer player) {
      super.stopSeenByPlayer(player);
      this.bossEvent.removePlayer(player);
   }

   @Override
   protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
      super.dropCustomDeathLoot(level, damageSource, recentlyHit);
      // Жемчуг идёт только из лут-таблицы (eldritch_warden.json): раньше он
      // падал и отсюда, и из таблицы — 2 штуки против 1 у голема/гиганта.
      this.spawnAtLocation(new net.minecraft.world.item.ItemStack((net.minecraft.world.level.ItemLike)thaumcraft.init.registry.ModItems.LOOT_BAG_RARE.get(), 1 + this.random.nextInt(2)));
   }

   @Override
   public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("runicShield", this.getRunicShield());
      tag.putInt("title", (Integer)this.entityData.get(DATA_TITLE));
   }

   @Override
   public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.contains("runicShield")) {
         this.setRunicShield(tag.getInt("runicShield"));
      }
      if (tag.contains("title")) {
         this.setTitle(tag.getInt("title"));
      }
   }

   public void handleEntityEvent(byte id) {
      if (id == 15 && this.level().isClientSide) {
         float unused = this.armLiftL = 0.5F;
      } else if (id == 16 && this.level().isClientSide) {
         float unused = this.armLiftR = 0.5F;
      } else if (id == 17 && this.level().isClientSide) {
         this.armLiftL = 0.9F;
         this.armLiftR = 0.9F;
      } else if (id == 18) {
         this.setSpawnTimer(150);
      } else if (id == 19 && this.level().isClientSide) {
         this.clientSpawnSonic();
      } else {
         super.handleEntityEvent(id);
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void clientSpawnSonic() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         mc.particleEngine.add(new FXSonic(mc.level, this.getX(), this.getY(), this.getZ(), this, 10));
      }
   }

   public boolean isAlliedTo(Entity other) {
      return other instanceof IEldritchMob || super.isAlliedTo(other);
   }

   protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
      return 3.1F;
   }

   public Component getName() {
      return Component.translatable("entity.thaumcraft.eldritch_warden").append(Component.literal(" " + this.getTitle()));
   }

   public boolean canAttackType(EntityType<?> type) {
      return type == ModEntities.ELDRITCH_GUARDIAN.get() ? false : super.canAttackType(type);
   }
}
