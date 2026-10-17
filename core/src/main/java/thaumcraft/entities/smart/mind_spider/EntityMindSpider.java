package thaumcraft.entities.smart.mind_spider;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.init.registry.ModEntities;

public class EntityMindSpider extends Spider {
   private static final EntityDataAccessor<Byte> DATA_HARMLESS = SynchedEntityData.defineId(EntityMindSpider.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<String> DATA_VIEWER = SynchedEntityData.defineId(EntityMindSpider.class, EntityDataSerializers.STRING);
   private int lifeSpan = Integer.MAX_VALUE;

   public EntityMindSpider(EntityType<? extends EntityMindSpider> type, Level level) {
      super(type, level);
      this.xpReward = 1;
   }

   public EntityMindSpider(Level level) {
      super((EntityType)ModEntities.MIND_SPIDER.get(), level);
      this.xpReward = 1;
   }

   public static Builder createAttributes() {
      return Spider.createAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.ATTACK_DAMAGE, 1.0).add(Attributes.FOLLOW_RANGE, 12.0);
   }

   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DATA_HARMLESS, (byte)0);
      builder.define(DATA_VIEWER, "");
   }

   public float getVoicePitch() {
      return 0.7F;
   }

   public String getViewer() {
      return (String)this.entityData.get(DATA_VIEWER);
   }

   public void setViewer(String player) {
      this.entityData.set(DATA_VIEWER, String.valueOf(player));
   }

   public boolean isHarmless() {
      return (Byte)this.entityData.get(DATA_HARMLESS) != 0;
   }

   public void setHarmless(boolean h) {
      if (h) {
         this.lifeSpan = 1200;
      }

      this.xpReward = h ? 0 : 1;
      this.entityData.set(DATA_HARMLESS, (byte)(h ? 1 : 0));
   }

   @OnlyIn(Dist.CLIENT)
   public float spiderScaleAmount() {
      return 0.3F;
   }

   public float getShadowSize() {
      return this.isHarmless() ? 0.0F : 0.1F;
   }

   public void tick() {
      super.tick();
   }

   public boolean isIgnoringBlockTriggers() {
      return true;
   }
}
