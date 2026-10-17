package thaumcraft.entities.smart.taint.cow;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.init.registry.ModEntities;

public class EntityTaintCow extends Cow implements ITaintedMob {
   public EntityTaintCow(EntityType<? extends Cow> entityType, Level level) {
      super(entityType, level);
   }

   public EntityTaintCow(Level level) {
      super((EntityType)ModEntities.TAINT_COW.get(), level);
   }

   public float getVoicePitch() {
      return 0.7F;
   }

   protected float getSoundVolume() {
      return 0.4F;
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide && this.tickCount < 5) {
         for (int unused = 0; unused < Thaumcraft.setup.particleCount(10); unused++) {
            Thaumcraft.setup.splooshFX(this);
         }
      }
   }
}
