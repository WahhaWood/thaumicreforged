package thaumcraft.entities.smart.zombie;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

public class EntityBrainyZombie extends Zombie {
   private static final EntityDataAccessor<Boolean> VILLAGER = SynchedEntityData.defineId(EntityBrainyZombie.class, EntityDataSerializers.BOOLEAN);

   public EntityBrainyZombie(EntityType<? extends Zombie> type, Level level) {
      super(type, level);
   }

   public static Builder createAttributes() {
      return Zombie.createAttributes().add(Attributes.MAX_HEALTH, 25.0).add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
   }

   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(VILLAGER, Boolean.FALSE);
   }

   public boolean isVillager() {
      return (Boolean)this.entityData.get(VILLAGER);
   }

   public void setVillager(boolean villager) {
      this.entityData.set(VILLAGER, villager);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("Villager", this.isVillager());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setVillager(tag.getBoolean("Villager"));
   }

   public int getArmorValue() {
      int v = super.getArmorValue() + 3;
      return Math.min(v, 20);
   }

}
