package thaumcraft.entities.brainless.aspect_orb;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.init.registry.ModEntities;

public class AspectOrbEntity extends Entity implements IEntityWithComplexSpawn {
   public int orbAge = 0;
   public int orbMaxAge = 150;
   public int orbCooldown;
   private int orbHealth = 5;
   private Aspect aspect;
   private int aspectValue;
   private Player closestPlayer;

   public AspectOrbEntity(EntityType<? extends Entity> entityType, Level level) {
      super(entityType, level);
   }

   public AspectOrbEntity(Level level, Vec3 vec3, Aspect aspect, int aspectValue) {
      this(level, vec3.x, vec3.y, vec3.z, aspect, aspectValue);
   }

   public AspectOrbEntity(Level level, double x, double y, double z, Aspect aspect, int aspectValue) {
      super((EntityType)ModEntities.ASPECT_ORB.get(), level);
      this.setPos(x, y, z);
      this.setYRot((float)(Math.random() * 360.0));
      this.setDeltaMovement((Math.random() * 0.2F - 0.1F) * 2.0, Math.random() * 0.2 * 2.0, (Math.random() * 0.2F - 0.1F) * 2.0);
      this.aspectValue = aspectValue;
      this.setAspect(aspect);
   }

   public boolean isIgnoringBlockTriggers() {
      return true;
   }

   protected void defineSynchedData(Builder builder) {
   }

   public void tick() {
      super.tick();
      if (this.orbCooldown > 0) {
         this.orbCooldown--;
      }

      this.xo = this.getX();
      this.yo = this.getY();
      this.zo = this.getZ();
      Vec3 motion = this.getDeltaMovement();
      motion = motion.subtract(0.0, 0.03F, 0.0);
      if (this.isInLava()) {
         motion = new Vec3((this.random.nextFloat() - this.random.nextFloat()) * 0.2F, 0.2F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
         this.playSound(SoundEvents.LAVA_EXTINGUISH, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
      }

      this.setDeltaMovement(motion);
      this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
      if (this.closestPlayer != null) {
         Vec3 detVec = this.closestPlayer.getEyePosition().subtract(this.position());
         double d4 = detVec.length();
         double d5 = 1.0 - d4;
         if (d5 > 0.0) {
            d5 *= d5;
            this.setDeltaMovement(this.getDeltaMovement().add(detVec.x / d4 * d5 * 0.1, detVec.y / d4 * d5 * 0.1, detVec.z / d4 * d5 * 0.1));
         }
      }

      this.move(MoverType.SELF, this.getDeltaMovement());
      float f = 0.98F;
      if (this.onGround()) {
         f = 0.58800006F;
         BlockPos blockPos = this.blockPosition().below();
         BlockState state = this.level().getBlockState(blockPos);
         if (!state.isAir()) {
            f = state.getFriction(this.level(), blockPos, this) * 0.98F;
         }
      }

      motion = this.getDeltaMovement();
      motion = motion.multiply(f, 0.98F, f);
      if (this.onGround()) {
         motion = motion.multiply(1.0, -0.9F, 1.0);
      }

      this.setDeltaMovement(motion);
      this.orbAge++;
      if (this.orbAge >= this.orbMaxAge) {
         this.setRemoved(RemovalReason.KILLED);
      }
   }

   protected void addAdditionalSaveData(CompoundTag compound) {
      compound.putShort("Health", (byte)this.orbHealth);
      compound.putShort("Age", (short)this.orbAge);
      compound.putShort("Value", (short)this.aspectValue);
      compound.putString("Aspect", this.getAspect().getTag());
   }

   protected void readAdditionalSaveData(CompoundTag compound) {
      this.orbHealth = compound.getShort("Health") & 255;
      this.orbAge = compound.getShort("Age");
      this.aspectValue = compound.getShort("Value");
      this.setAspect(Aspect.getAspect(compound.getString("Aspect")));
   }

   public void playerTouch(@NotNull Player player) {
   }

   public boolean isAttackable() {
      return false;
   }

   public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
      if (this.aspect != null) {
         buffer.writeUtf(this.aspect.getTag());
      }
   }

   public void readSpawnData(RegistryFriendlyByteBuf buffer) {
      this.setAspect(Aspect.getAspect(buffer.readUtf()));
   }

   public int getAspectValue() {
      return this.aspectValue;
   }

   public Aspect getAspect() {
      return this.aspect != null ? this.aspect : Aspect.AIR;
   }

   public void setAspect(Aspect aspect) {
      this.aspect = aspect;
   }
}
