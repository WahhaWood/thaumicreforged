package thaumcraft.entities.brainless.taint_bottle;

import thaumcraft.entities.brainless.projectile.EntityThrowableByPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.ModEntities;
import thaumcraft.init.registry.ModItems;

public class EntityBottleTaint extends EntityThrowableByPlayer implements ItemSupplier {
   public EntityBottleTaint(EntityType<? extends EntityBottleTaint> type, Level level) {
      super(type, level);
   }

   public EntityBottleTaint(Level level, LivingEntity thrower) {
      this((EntityType<? extends EntityBottleTaint>)ModEntities.BOTTLE_TAINT.get(), level);
      this.setOwner(thrower);
      this.setPos(thrower.getX(), thrower.getEyeY() - 0.1, thrower.getZ());
      this.shootFromRotation(thrower, thrower.getXRot(), thrower.getYRot(), -20.0F, 0.5F, 1.0F);
   }

   protected double getDefaultGravity() {
      return 0.05F;
   }

   protected void onHit(HitResult hitResult) {
      super.onHit(hitResult);
      Level level = this.level();
      if (this.level().isClientSide) {
         for (int unused = 0; unused < Thaumcraft.setup.particleCount(100); unused++) {
            Thaumcraft.setup.taintsplosionFX(this);
         }
      }

      Thaumcraft.setup.bottleTaintBreak(level, this.getX(), this.getY(), this.getZ());
   }

   @NotNull
   public ItemStack getItem() {
      return new ItemStack((ItemLike)ModItems.BOTTLE_TAINT.get());
   }
}
