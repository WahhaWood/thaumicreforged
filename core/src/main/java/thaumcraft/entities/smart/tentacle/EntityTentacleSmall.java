package thaumcraft.entities.smart.tentacle;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.init.registry.ModEntities;

public class EntityTentacleSmall extends EntityTantacle implements ITaintedMob {
   private int lifetime = 200;

   public EntityTentacleSmall(EntityType<? extends Monster> entityType, Level level) {
      super(entityType, level);
      this.xpReward = 0;
   }

   public EntityTentacleSmall(Level level) {
      super((EntityType<? extends Monster>)ModEntities.TENTACLE_SMALL.get(), level);
      this.xpReward = 0;
   }

   @Override
   public void tick() {
      super.tick();
   }
}
