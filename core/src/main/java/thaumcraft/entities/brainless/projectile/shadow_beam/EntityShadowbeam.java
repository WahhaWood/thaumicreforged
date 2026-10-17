package thaumcraft.entities.brainless.projectile.shadow_beam;

import thaumcraft.entities.brainless.projectile.EntityThrowableByPlayer;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.ModEntities;

public class EntityShadowbeam extends EntityThrowableByPlayer implements ItemSupplier {
   private static final int INITIAL_OFFSET = 2;
   private static final int LENGTH = 298;
   private static final int MAX_TICKS = 300;
   private static final int SIZE = 4;
   private int potency;
   private Vec3 movementVector;

   public EntityShadowbeam(EntityType<? extends ThrowableProjectile> entityType, Level level) {
      super(entityType, level);
      this.movementVector = Vec3.ZERO;
   }

   public EntityShadowbeam(Level level, LivingEntity thrower, int potency) {
      super((EntityType<? extends ThrowableProjectile>)ModEntities.SHADOW_BEAM.get(), level, thrower);
      this.potency = potency;
      Vec3 look = thrower.getLookAngle().scale(0.1);
      this.setDeltaMovement(look);
      this.movementVector = look;
      float f = Mth.sqrt((float)(look.x * look.x + look.z * look.z));
      this.setYRot((float)(Mth.atan2(look.x, look.z) * (180.0 / Math.PI)));
      this.setXRot((float)(Mth.atan2(look.y, f) * (180.0 / Math.PI)));
      this.yRotO = this.getYRot();
      this.xRotO = this.getXRot();
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
   }

   protected double getDefaultGravity() {
      return 0.0;
   }

   protected void onHitBlock(BlockHitResult result) {
      Direction dir = result.getDirection();
      Vec3 normal = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ());
      double dot = this.movementVector.dot(normal);
      this.movementVector = normal.scale(-2.0 * dot).add(this.movementVector);
      this.setDeltaMovement(this.movementVector);
   }

   /**
    * Порт ветки {@code func_70184_a} для попадания по сущности: урон 8 + potency
    * от имени владельца-игрока. Луч отскакивает от блоков, но не застревает.
    */
   @Override
   protected void onHitEntity(EntityHitResult result) {
      Entity target = result.getEntity();
      if (this.level() instanceof net.minecraft.server.level.ServerLevel level
         && this.getOwner() instanceof net.minecraft.server.level.ServerPlayer owner
         && target != owner) {
         boolean pvpAllowed = level.getServer() == null || level.getServer().isPvpAllowed();
         boolean isPlayerTarget = target instanceof net.minecraft.world.entity.player.Player;
         if (!isPlayerTarget || pvpAllowed) {
            target.hurt(level.damageSources().playerAttack(owner), 8.0F + this.potency);
         }
      }

      super.onHitEntity(result);
   }

   public void tick() {
      this.setDeltaMovement(this.movementVector);
      super.tick();
      if (this.tickCount > 2 && this.level().isClientSide) {
         Thaumcraft.setup.sparkle((float)this.getX(), (float)this.getY(), (float)this.getZ(), 4);
      }

      if (this.tickCount >= 300) {
         this.discard();
      }
   }

   public ItemStack getItem() {
      return ItemStack.EMPTY;
   }
}
