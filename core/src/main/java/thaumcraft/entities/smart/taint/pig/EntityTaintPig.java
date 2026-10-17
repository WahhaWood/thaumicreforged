package thaumcraft.entities.smart.taint.pig;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.init.registry.ModEntities;

public class EntityTaintPig extends Pig implements ITaintedMob {
   public EntityTaintPig(EntityType<? extends Pig> entityType, Level level) {
      super(entityType, level);
   }

   public EntityTaintPig(Level level) {
      super((EntityType)ModEntities.TAINT_PIG.get(), level);
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide && this.tickCount < 5) {
         for (int unused = 0; unused < Thaumcraft.setup.particleCount(10); unused++) {
            Thaumcraft.setup.splooshFX(this);
         }
      }
   }

   public int getArmorValue() {
      return 2;
   }

   public float getVoicePitch() {
      return 0.7F;
   }
}
