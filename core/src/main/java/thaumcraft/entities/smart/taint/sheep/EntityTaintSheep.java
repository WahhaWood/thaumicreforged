package thaumcraft.entities.smart.taint.sheep;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.entities.smart.taint.ai.AIConvertGrass;
import thaumcraft.init.registry.ModEntities;

public class EntityTaintSheep extends Sheep implements ITaintedMob {
   private AIConvertGrass convertGrass;

   public EntityTaintSheep(EntityType<? extends Sheep> entityType, Level level) {
      super(entityType, level);
   }

   public EntityTaintSheep(Level level) {
      super((EntityType)ModEntities.TAINT_SHEEP.get(), level);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.convertGrass = new AIConvertGrass(this);
      // 1.7.10:21 — func_75248_a(7).
      this.goalSelector.addGoal(7, this.convertGrass);
   }

   public int getArmorValue() {
      return 1;
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
