package thaumcraft.entities.brainless.permanent;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PermanentItemEntity extends ItemEntity {
   public PermanentItemEntity(EntityType<? extends ItemEntity> entityType, Level level) {
      super(entityType, level);
   }

   public PermanentItemEntity(Level level, double x, double y, double z, ItemStack stack) {
      super(level, x, y, z, stack);
      this.setYRot((float)(Math.random() * 360.0));
      double motionX = Math.random() * 0.2 - 0.1;
      double motionY = 0.2;
      double motionZ = Math.random() * 0.2 - 0.1;
      this.setDeltaMovement(motionX, motionY, motionZ);
   }

   public void tick() {
      super.tick();
   }

   public boolean fireImmune() {
      return true;
   }
}
