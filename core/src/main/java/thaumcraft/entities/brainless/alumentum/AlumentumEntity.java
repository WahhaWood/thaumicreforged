package thaumcraft.entities.brainless.alumentum;

import thaumcraft.entities.brainless.projectile.EntityThrowableByPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import thaumcraft.init.registry.ModEntities;
import thaumcraft.init.registry.ModItems;

public class AlumentumEntity extends EntityThrowableByPlayer implements ItemSupplier {
   private boolean exploded;

   public AlumentumEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
      super(entityType, level);
   }

   public AlumentumEntity(Level level, double x, double y, double z) {
      super((EntityType<? extends ThrowableProjectile>)ModEntities.ALUMENTUM.get(), x, y, z, level);
   }

   public AlumentumEntity(LivingEntity shooter, Level level) {
      super((EntityType<? extends ThrowableProjectile>)ModEntities.ALUMENTUM.get(), shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ(), level);
      this.setOwner(shooter);
   }

   @NotNull
   public ItemStack getItem() {
      return ((Item)ModItems.ALUMENTUM.get()).getDefaultInstance();
   }

   public void tick() {
      super.tick();
      Level level = this.level();
      if (level.isClientSide) {
         ;
      }
   }

   protected boolean canHitEntity(Entity entity) {
      Entity owner = this.getOwner();
      return owner != null && entity == owner && this.tickCount < 5 ? false : super.canHitEntity(entity);
   }
}
