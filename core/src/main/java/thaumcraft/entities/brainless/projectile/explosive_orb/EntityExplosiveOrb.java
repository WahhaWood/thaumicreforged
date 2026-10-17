package thaumcraft.entities.brainless.projectile.explosive_orb;

import thaumcraft.entities.brainless.projectile.EntityThrowableByPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.ModEntities;

public class EntityExplosiveOrb extends EntityThrowableByPlayer {
   public float strength = 1.0F;
   public boolean onFire = false;

   public EntityExplosiveOrb(EntityType<? extends ThrowableProjectile> entityType, Level level) {
      super(entityType, level);
   }

   public EntityExplosiveOrb(Level level, LivingEntity shooter) {
      super((EntityType<? extends ThrowableProjectile>)ModEntities.EXPLOSIVE_ORB.get(), level, shooter);
   }

   protected double getDefaultGravity() {
      return 0.01F;
   }

   protected void onHit(@NotNull HitResult result) {
      // Взрыв при попадании (сущность или блок): урон strength + поджог.
      // Раньше метод был пустым — фаербол фокуса огня долетал и исчезал без эффекта.
      if (!this.level().isClientSide) {
         this.level()
            .explode(
               this, this.getX(), this.getY(), this.getZ(), this.strength, this.onFire, net.minecraft.world.level.Level.ExplosionInteraction.MOB
            );
         this.discard();
      }
   }

   public void tick() {
      super.tick();
      // Самоликвидация в воздухе через 6 с, чтобы не висели вечные снаряды.
      if (!this.level().isClientSide && this.tickCount > 120) {
         this.level()
            .explode(
               this, this.getX(), this.getY(), this.getZ(), this.strength, this.onFire, net.minecraft.world.level.Level.ExplosionInteraction.MOB
            );
         this.discard();
         return;
      }
      Thaumcraft.setup
         .drawGenericParticles(
            this.level(),
            this.xo + (this.random.nextFloat() - this.random.nextFloat()) * 0.3F,
            this.yo + (this.random.nextFloat() - this.random.nextFloat()) * 0.3F,
            this.zo + (this.random.nextFloat() - this.random.nextFloat()) * 0.3F,
            0.0,
            0.0,
            0.0,
            1.0F,
            1.0F,
            1.0F,
            0.8F,
            false,
            151,
            9,
            1,
            7 + this.random.nextInt(5),
            0,
            2.0F + this.random.nextFloat()
         );
   }
}
