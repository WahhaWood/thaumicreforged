package thaumcraft.entities.smart.taint.chicken;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.level.Level;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.init.registry.ModEntities;

public class EntityTaintChicken extends Chicken implements ITaintedMob {
   public EntityTaintChicken(EntityType<? extends Chicken> entityType, Level level) {
      super(entityType, level);
   }

   public EntityTaintChicken(Level level) {
      super((EntityType)ModEntities.TAINT_CHICKEN.get(), level);
   }

   protected int calculateFallDamage(float p_21237_, float p_21238_) {
      return 0;
   }

   public int getArmorValue() {
      return 2;
   }

   public float getVoicePitch() {
      return 0.7F;
   }
}
