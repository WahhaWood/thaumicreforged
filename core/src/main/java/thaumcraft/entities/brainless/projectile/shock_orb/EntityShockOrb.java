package thaumcraft.entities.brainless.projectile.shock_orb;

import thaumcraft.entities.brainless.projectile.EntityThrowableByPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import thaumcraft.init.registry.ModEntities;

public class EntityShockOrb extends EntityThrowableByPlayer {
   public int area = 4;
   public int damage = 5;

   public EntityShockOrb(EntityType<? extends ThrowableProjectile> entityType, Level level) {
      super(entityType, level);
   }

   public EntityShockOrb(Level level, LivingEntity thrower) {
      super((EntityType<? extends ThrowableProjectile>)ModEntities.SHOCK_ORB.get(), level, thrower);
   }

   protected double getDefaultGravity() {
      return 0.05;
   }

   protected void onHit(HitResult hitResult) {
      super.onHit(hitResult);
   }

   public void tick() {
      super.tick();
   }
}
