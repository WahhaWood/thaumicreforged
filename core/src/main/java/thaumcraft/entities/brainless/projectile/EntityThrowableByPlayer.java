package thaumcraft.entities.brainless.projectile;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;

/**
 * Базовый «снаряд, брошенный существом». Наследники — алхимические/проекции-снаряды Thaumcraft.
 */
public class EntityThrowableByPlayer extends ThrowableProjectile {
   public EntityThrowableByPlayer(EntityType<? extends ThrowableProjectile> type, Level level) {
      super(type, level);
   }

   public EntityThrowableByPlayer(EntityType<? extends ThrowableProjectile> type, double x, double y, double z, Level level) {
      super(type, x, y, z, level);
   }

   public EntityThrowableByPlayer(EntityType<? extends ThrowableProjectile> type, Level level, LivingEntity thrower) {
      super(type, thrower, level);
      this.shootFromRotation(thrower, thrower.getXRot(), thrower.getYRot(), 0.0F, 1.0F, 1.0F);
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
   }
}
