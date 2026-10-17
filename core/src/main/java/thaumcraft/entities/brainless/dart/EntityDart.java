package thaumcraft.entities.brainless.dart;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

public class EntityDart extends AbstractArrow implements IEntityWithComplexSpawn {
   private boolean first = true;

   public EntityDart(EntityType<? extends EntityDart> type, Level level) {
      super(type, level);
      this.setBaseDamage(1.0);
   }

   public EntityDart(EntityType<? extends EntityDart> type, Level level, LivingEntity shooter, LivingEntity target, float velocity, float inaccuracy) {
      this(type, level);
      this.setOwner(shooter);
      double startY = shooter.getEyeY() - 0.1F;
      double dx = target.getX() - shooter.getX();
      double dy = target.getEyeY() - 0.7F - startY;
      double dz = target.getZ() - shooter.getZ();
      double horiz = Math.sqrt(dx * dx + dz * dz);
      if (horiz >= 1.0E-7) {
         float yaw = (float)(Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
         float pitch = (float)(-(Math.atan2(dy, horiz) * 180.0 / Math.PI));
         double nx = dx / horiz;
         double nz = dz / horiz;
         this.moveTo(shooter.getX() + nx / 5.0, startY, shooter.getZ() + nz / 5.0, yaw, pitch);
         float lift = (float)horiz * 0.2F;
         this.shoot(dx, dy + lift, dz, velocity, inaccuracy);
      }
   }

   public void tick() {
      if (this.first && this.level().isClientSide) {
         this.first = false;
         Vec3 unused = this.getDeltaMovement();
         double _x = unused.x;
         double _xx = unused.y;
         double _xxx = unused.z;

         for (int _xxxx = 0; _xxxx < 5; _xxxx++) {
            this.level()
               .addParticle(
                  ParticleTypes.SMOKE,
                  this.getX() - _x / 1.5,
                  this.getY() - _xx / 1.5,
                  this.getZ() - _xxx / 1.5,
                  _x / 9.0 + this.random.nextGaussian() * 0.01,
                  _xx / 9.0 + this.random.nextGaussian() * 0.01,
                  _xxx / 9.0 + this.random.nextGaussian() * 0.01
               );
         }
      }

      super.tick();
   }

   protected ItemStack getPickupItem() {
      return ItemStack.EMPTY;
   }

   protected ItemStack getDefaultPickupItem() {
      return ItemStack.EMPTY;
   }

   public void writeSpawnData(RegistryFriendlyByteBuf data) {
      Vec3 v = this.getDeltaMovement();
      data.writeDouble(v.x);
      data.writeDouble(v.y);
      data.writeDouble(v.z);
      data.writeFloat(this.getYRot());
      data.writeFloat(this.getXRot());
   }

   public void readSpawnData(RegistryFriendlyByteBuf data) {
      double mx = data.readDouble();
      double my = data.readDouble();
      double mz = data.readDouble();
      this.setDeltaMovement(mx, my, mz);
      float yaw = data.readFloat();
      float pitch = data.readFloat();
      this.setYRot(yaw);
      this.setXRot(pitch);
      this.setYHeadRot(yaw);
   }
}
