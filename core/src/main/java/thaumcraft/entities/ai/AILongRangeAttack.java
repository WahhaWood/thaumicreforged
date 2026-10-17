package thaumcraft.entities.ai;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.jetbrains.annotations.Nullable;

/** Дальний бой с каденцией по дистанции (порт AILongRangeAttack из 1.7.10). */
public class AILongRangeAttack extends Goal {
   private final Mob wielder;
   private final RangedAttackMob rangedWielder;
   private final double minDistance;
   private final double moveSpeed;
   private final int minAttackTime;
   private final int maxAttackTime;
   private final float maxRange;
   private final float maxRangeSquared;
   @Nullable
   private LivingEntity target;
   private int attackTime = -1;
   private int seeTime;

   public AILongRangeAttack(
      RangedAttackMob mob, double minDistance, double moveSpeed, int minAttackTime, int maxAttackTime, float maxRange
   ) {
      if (!(mob instanceof Mob living)) {
         throw new IllegalArgumentException("AILongRangeAttack requires a Mob");
      }

      this.rangedWielder = mob;
      this.wielder = living;
      this.minDistance = minDistance;
      this.moveSpeed = moveSpeed;
      this.minAttackTime = minAttackTime;
      this.maxAttackTime = maxAttackTime;
      this.maxRange = maxRange;
      this.maxRangeSquared = maxRange * maxRange;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   @Override
   public boolean canUse() {
      LivingEntity attackTarget = this.wielder.getTarget();
      if (attackTarget == null || !attackTarget.isAlive()) {
         return false;
      }

      this.target = attackTarget;
      double distanceSquared = this.wielder.distanceToSqr(attackTarget);
      return distanceSquared >= this.minDistance * this.minDistance;
   }

   @Override
   public boolean canContinueToUse() {
      return this.canUse() || !this.wielder.getNavigation().isDone();
   }

   @Override
   public void stop() {
      this.target = null;
      this.seeTime = 0;
      this.attackTime = -1;
   }

   @Override
   public void tick() {
      if (this.target == null) {
         return;
      }

      double distanceSquared = this.wielder.distanceToSqr(this.target);
      boolean canSee = this.wielder.getSensing().hasLineOfSight(this.target);
      if (canSee) {
         this.seeTime++;
      } else {
         this.seeTime = 0;
      }

      if (distanceSquared <= this.maxRangeSquared && this.seeTime >= 20) {
         this.wielder.getNavigation().stop();
      } else {
         this.wielder.getNavigation().moveTo(this.target, this.moveSpeed);
      }

      this.wielder.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
      if (--this.attackTime == 0) {
         if (distanceSquared > this.maxRangeSquared || !canSee) {
            return;
         }

         float distanceRatio = (float)(Math.sqrt(distanceSquared) / this.maxRange);
         this.rangedWielder.performRangedAttack(this.target, Mth.clamp(distanceRatio, 0.1F, 1.0F));
         this.attackTime = Mth.floor(distanceRatio * (this.maxAttackTime - this.minAttackTime) + this.minAttackTime);
      } else if (this.attackTime < 0) {
         float distanceRatio = (float)(Math.sqrt(distanceSquared) / this.maxRange);
         this.attackTime = Mth.floor(distanceRatio * (this.maxAttackTime - this.minAttackTime) + this.minAttackTime);
      }
   }
}
