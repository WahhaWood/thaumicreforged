package thaumcraft.entities.smart.zombie;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import thaumcraft.init.registry.ModEntities;

public class EntityGiantBrainyZombie extends EntityBrainyZombie {
   private static final EntityDataAccessor<Float> ANGER = SynchedEntityData.defineId(EntityGiantBrainyZombie.class, EntityDataSerializers.FLOAT);
   private static final String RESOURCE_META_TAG = "tc_meta";

   public EntityGiantBrainyZombie(EntityType<? extends EntityGiantBrainyZombie> entityType, Level level) {
      super(entityType, level);
      this.refreshDimensions();
   }

   public EntityGiantBrainyZombie(Level level) {
      this((EntityType<? extends EntityGiantBrainyZombie>)ModEntities.GIANT_BRAINY_ZOMBIE.get(), level);
      this.xpReward = 15;
   }

   public static Builder createAttributes() {
      return Zombie.createAttributes().add(Attributes.MAX_HEALTH, 60.0).add(Attributes.ATTACK_DAMAGE, 7.0).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
   }

   @Override
   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(ANGER, 1.0F);
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
      super.onSyncedDataUpdated(key);
      if (ANGER.equals(key) && this.level().isClientSide) {
         this.refreshDimensions();
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putFloat("Anger", this.getAnger());
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.contains("Anger")) {
         this.setAnger(tag.getFloat("Anger"));
      }
   }

   public float getAnger() {
      return (Float)this.entityData.get(ANGER);
   }

   public void setAnger(float v) {
      float oldAnger = this.getAnger();
      float anger = Mth.clamp(v, 1.0F, 2.0F);
      if (!(Math.abs(oldAnger - anger) < 1.0E-4F)) {
         double x = this.getX();
         double y = this.getY();
         double z = this.getZ();
         float oldHeight = this.getBbHeight();
         this.entityData.set(ANGER, anger);
         this.refreshDimensions();
         if (!this.level().isClientSide && !this.noPhysics && !this.level().noCollision(this)) {
            int maxLift = Math.max(2, Mth.ceil(Math.max(0.0F, this.getBbHeight() - oldHeight)) + 1);

            for (int i = 1; i <= maxLift; i++) {
               this.setPos(x, y + i, z);
               if (this.level().noCollision(this)) {
                  return;
               }
            }

            this.setPos(x, y, z);
            this.entityData.set(ANGER, oldAnger);
            this.refreshDimensions();
         }
      }
   }

   public void aiStep() {
      super.aiStep();
   }

}
