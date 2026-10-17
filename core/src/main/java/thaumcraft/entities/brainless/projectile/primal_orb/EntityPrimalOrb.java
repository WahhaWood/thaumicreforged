package thaumcraft.entities.brainless.projectile.primal_orb;

import thaumcraft.entities.brainless.projectile.EntityThrowableByPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.ModEntities;

public class EntityPrimalOrb extends EntityThrowableByPlayer implements IEntityWithComplexSpawn {
   private static final String TAG_REAL_TICKS_EXISTED = "RealTicksExisted";
   private int realTicksExisted;
   int count = 0;
   boolean seeker = false;
   int oi = 0;

   public EntityPrimalOrb(EntityType<? extends ThrowableProjectile> entityType, Level level) {
      super(entityType, level);
   }

   public EntityPrimalOrb(Level level, LivingEntity shooter, boolean seeker) {
      super((EntityType<? extends ThrowableProjectile>)ModEntities.PRIMAL_ORB.get(), level, shooter);
      this.seeker = seeker;
      this.oi = shooter.getId();
   }

   public void addAdditionalSaveData(@NotNull CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      if (this.realTicksExisted > 0) {
         compound.putInt("RealTicksExisted", this.realTicksExisted);
      }
   }

   public void readAdditionalSaveData(@NotNull CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      this.realTicksExisted = compound.getInt("RealTicksExisted");
   }

   public void writeSpawnData(RegistryFriendlyByteBuf data) {
      data.writeBoolean(this.seeker);
      data.writeInt(this.oi);
   }

   public void readSpawnData(RegistryFriendlyByteBuf data) {
      this.seeker = data.readBoolean();
      this.oi = data.readInt();
   }

   protected double getDefaultGravity() {
      return 0.001F;
   }

   protected float func_70182_d() {
      return 0.5F;
   }

   public void tick() {
      this.count++;
      Level level = this.level();
      if (this.level().isClientSide) {
         for (int unused = 0; unused < 6; unused++) {
            Thaumcraft.setup
               .wispFX4(
                  level,
                  (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                  (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                  (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                  this,
                  unused,
                  true,
                  0.0F
               );
         }

         Thaumcraft.setup
            .wispFX2(
               level,
               this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
               this.getY() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
               this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
               0.1F,
               this.random.nextInt(6),
               true,
               true,
               0.0F
            );
      }

      super.tick();
   }

   protected void onHit(@NotNull HitResult result) {
      Level level = this.level();
      // Серверный эффект: первородный взрыв. Раньше был только клиентский FX —
      // шар фокуса primal бился и исчезал без урона.
      if (!level.isClientSide) {
         level.explode(
            this, this.getX(), this.getY(), this.getZ(), 1.5F, false, net.minecraft.world.level.Level.ExplosionInteraction.MOB
         );
         this.discard();
         return;
      }
      // Дальше только клиентский FX разлёта.
      for (int unused = 0; unused < 6; unused++) {
         for (int _x = 0; _x < 6; _x++) {
            float _xx = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
            float _xxx = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
            float _xxxx = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
            Thaumcraft.setup
               .wispFX3(
                  level,
                  this.getX() + _xx,
                  this.getY() + _xxx,
                  this.getZ() + _xxxx,
                  this.getX() + _xx * 10.0F,
                  this.getY() + _xxx * 10.0F,
                  this.getZ() + _xxxx * 10.0F,
                  0.4F,
                  _x,
                  true,
                  0.05F
               );
         }
      }
   }

   public void taintSplosion() {
   }
}
