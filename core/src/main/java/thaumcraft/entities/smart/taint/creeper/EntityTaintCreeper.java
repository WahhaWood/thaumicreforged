package thaumcraft.entities.smart.taint.creeper;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.init.registry.ModEntities;

public class EntityTaintCreeper extends Creeper implements ITaintedMob {
   public EntityTaintCreeper(EntityType<? extends Creeper> entityType, Level level) {
      super(entityType, level);
   }

   public EntityTaintCreeper(Level level) {
      super((EntityType)ModEntities.TAINT_CREEPER.get(), level);
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide && this.tickCount < 5) {
         for (int unused = 0; unused < Thaumcraft.setup.particleCount(10); unused++) {
            Thaumcraft.setup.splooshFX(this);
         }
      }
   }

   public float getVoicePitch() {
      return 0.7F;
   }
}
