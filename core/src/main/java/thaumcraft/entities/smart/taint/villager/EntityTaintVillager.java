package thaumcraft.entities.smart.taint.villager;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.api.entities.ITaintedMob;

public class EntityTaintVillager extends Zombie implements ITaintedMob {
   public EntityTaintVillager(EntityType<? extends Zombie> type, Level level) {
      super(type, level);
   }

   public static Builder createAttributes() {
      return Zombie.createAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.23).add(Attributes.ATTACK_DAMAGE, 4.0);
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide && this.tickCount < 5) {
         int unused = Thaumcraft.setup.particleCount(10);

         for (int _x = 0; _x < unused; _x++) {
            Thaumcraft.setup.splooshFX(this);
         }
      }
   }

   protected boolean isSunSensitive() {
      return false;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.VILLAGER_AMBIENT;
   }

   protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
      return SoundEvents.VILLAGER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.VILLAGER_DEATH;
   }

   public float getVoicePitch() {
      return 0.7F;
   }
}
