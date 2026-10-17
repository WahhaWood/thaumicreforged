package thaumcraft.entities.smart.taint.spider;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.init.registry.ModEntities;

public class EntityTaintSpider extends Spider implements ITaintedMob {
   public EntityTaintSpider(EntityType<? extends Spider> entityType, Level level) {
      super(entityType, level);
   }

   public EntityTaintSpider(Level level) {
      super((EntityType)ModEntities.TAINT_SPIDER.get(), level);
      this.xpReward = 2;
   }

   public float getVoicePitch() {
      return 0.7F;
   }

   public float getScale() {
      return 0.4F;
   }
}
