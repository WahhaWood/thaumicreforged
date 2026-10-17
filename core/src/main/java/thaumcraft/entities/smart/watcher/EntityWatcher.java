package thaumcraft.entities.smart.watcher;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

public class EntityWatcher extends Guardian {
   protected static final EntityDataAccessor<Byte> DATA_FLAGS = SynchedEntityData.defineId(EntityWatcher.class, EntityDataSerializers.BYTE);
   protected static final int FLAG_ELDER = 4;
   private static final String TAG_FLAGS = "WatcherFlags";

   public EntityWatcher(EntityType<? extends Guardian> type, Level level) {
      super(type, level);
      this.setPathfindingMalus(PathType.WATER, 0.0F);
      this.xpReward = 10;
   }

   public static Builder createAttributes() {
      return Guardian.createAttributes()
         .add(Attributes.ATTACK_DAMAGE, 6.0)
         .add(Attributes.MOVEMENT_SPEED, 0.5)
         .add(Attributes.FOLLOW_RANGE, 16.0)
         .add(Attributes.MAX_HEALTH, 30.0);
   }

   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DATA_FLAGS, (byte)0);
   }

   private boolean getFlag(int mask) {
      return ((Byte)this.entityData.get(DATA_FLAGS) & mask) != 0;
   }

   private void setFlag(int mask, boolean value) {
      byte b = (Byte)this.entityData.get(DATA_FLAGS);
      if (value) {
         b = (byte)(b | mask);
      } else {
         b = (byte)(b & ~mask);
      }

      this.entityData.set(DATA_FLAGS, b);
   }

   public boolean isElderWatcher() {
      return this.getFlag(4);
   }

   public void setElderWatcher(boolean v) {
      this.setFlag(4, v);
      this.refreshDimensions();
   }

   public int getAttackDuration() {
      return this.isElderWatcher() ? 60 : 80;
   }

   public void setActiveAttackTarget(int entityId) {
      if (entityId == 0) {
         this.setTarget(null);
      } else {
         if (this.level().getEntity(entityId) instanceof LivingEntity living) {
            this.setTarget(living);
         }
      }
   }

   protected float getStandingEyeHeight(Pose pose, EntityDimensions size) {
      return size.height() * 0.5F;
   }

   public float getWalkTargetValue(BlockPos pos, LevelReader level) {
      return level.getFluidState(pos).is(FluidTags.WATER) ? 10.0F + level.getPathfindingCostFromLightLevels(pos) : super.getWalkTargetValue(pos, level);
   }

   public static boolean checkWatcherSpawnRules(EntityType<?> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
      return (random.nextInt(20) == 0 || !level.canSeeSkyFromBelowWater(pos))
         && level.getDifficulty() != Difficulty.PEACEFUL
         && (spawnType == MobSpawnType.SPAWNER || level.getFluidState(pos).is(FluidTags.WATER))
         && level.getFluidState(pos.below()).is(FluidTags.WATER);
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide && this.isElderWatcher() && !this.hasRestriction()) {
         this.restrictTo(this.blockPosition(), 16);
      }
   }

   public static class WatcherAttackGoal extends Goal {
      private final EntityWatcher mob;
      private int charge;

      public WatcherAttackGoal(EntityWatcher mob) {
         this.mob = mob;
         this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      }

      public boolean canUse() {
         LivingEntity target = this.mob.getTarget();
         return target != null && target.isAlive();
      }

      public boolean canContinueToUse() {
         return this.canUse();
      }

      public void start() {
         this.charge = -10;
      }

      public void tick() {
         LivingEntity t = this.mob.getTarget();
         if (t != null && t.isAlive()) {
            this.mob.getNavigation().stop();
            this.mob.getLookControl().setLookAt(t, 90.0F, 90.0F);
            if (!this.mob.getSensing().hasLineOfSight(t)) {
               this.mob.setTarget(null);
               this.mob.setActiveAttackTarget(0);
            } else {
               this.charge++;
               if (this.charge == 0) {
                  this.mob.setActiveAttackTarget(t.getId());
                  if (!this.mob.isSilent()) {
                     this.mob.level().broadcastEntityEvent(this.mob, (byte)21);
                  }
               } else {
                  if (this.charge >= this.mob.getAttackDuration()) {
                     float dmg = 1.0F;
                     Difficulty d = this.mob.level().getDifficulty();
                     if (d == Difficulty.HARD) {
                        dmg += 2.0F;
                     }

                     if (this.mob.isElderWatcher()) {
                        dmg += 2.0F;
                     }

                     t.hurt(this.mob.damageSources().indirectMagic(this.mob, this.mob), dmg);
                     double ad = this.mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
                     if (ad > 0.0) {
                        t.hurt(this.mob.damageSources().mobAttack(this.mob), (float)ad);
                     }

                     this.mob.setTarget(null);
                     this.mob.setActiveAttackTarget(0);
                     this.charge = -10;
                  }
               }
            }
         }
      }
   }

   public static class WatcherAttackSelector implements Predicate<LivingEntity> {
      private final EntityWatcher watcher;

      public WatcherAttackSelector(EntityWatcher watcher) {
         this.watcher = watcher;
      }

      public boolean test(@Nullable LivingEntity entity) {
         return entity != null
            && (entity instanceof Player || entity instanceof Squid || entity instanceof Axolotl)
            && entity.distanceToSqr(this.watcher) > 9.0;
      }
   }
}
