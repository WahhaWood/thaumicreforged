package thaumcraft.entities.brainless.special;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.init.registry.ModEntities;

public class SpecialItemEntity extends ItemEntity {
   public SpecialItemEntity(EntityType<? extends ItemEntity> entityType, Level level) {
      super(entityType, level);
   }

   public SpecialItemEntity(EntityType<? extends ItemEntity> entityType, Level level, Vec3 vec3, ItemStack stack) {
      super(entityType, level);
      this.setPos(vec3.x, vec3.y, vec3.z);
      this.setDeltaMovement(level.random.nextDouble() * 0.2 - 0.1, 0.2, level.random.nextDouble() * 0.2 - 0.1);
      this.setItem(stack);
      this.lifespan = stack.getEntityLifespan(level);
   }

   public SpecialItemEntity(Level level, Vec3 vec3, ItemStack stack) {
      this(level, vec3.x, vec3.y, vec3.z, stack);
   }

   public SpecialItemEntity(Level level, double x, double y, double z, ItemStack stack) {
      super((EntityType)ModEntities.SPECIAL_ITEM.get(), level);
      this.setPos(x, y, z);
      this.setDeltaMovement(level.random.nextDouble() * 0.2 - 0.1, 0.2, level.random.nextDouble() * 0.2 - 0.1);
      this.setItem(stack);
      this.lifespan = stack.getEntityLifespan(level);
   }

   public void tick() {
      Vec3 vec3 = this.getDeltaMovement();
      if (vec3.y > 0.0) {
         vec3 = vec3.add(0.0, 0.9F, 0.0);
      }

      this.setDeltaMovement(vec3.add(0.0, 0.04F, 0.0));
      super.tick();
   }
}
