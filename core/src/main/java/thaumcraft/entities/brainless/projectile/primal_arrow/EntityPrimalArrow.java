package thaumcraft.entities.brainless.projectile.primal_arrow;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.init.registry.ModEntities;

public class EntityPrimalArrow extends AbstractArrow implements IEntityWithComplexSpawn {
   private final double damage;
   public int shootingEntityId;
   public int type;

   public EntityPrimalArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
      super(entityType, level);
      this.inGround = false;
      this.damage = 2.1;
      this.type = 0;
   }

   public EntityPrimalArrow(Level level, double x, double y, double z) {
      super((EntityType)ModEntities.PRIMAL_ARROW.get(), level);
      this.setPos(x, y, z);
      this.inGround = false;
      this.damage = 2.1;
      this.type = 0;
   }

   public EntityPrimalArrow(Level level, LivingEntity entity, float par3, int type) {
      super((EntityType)ModEntities.PRIMAL_ARROW.get(), level);
      this.setOwner(entity);
      this.setPos(entity.getX(), entity.getEyeY() - 0.1F, entity.getZ());
      this.inGround = false;
      this.damage = 2.1;
      this.type = type;
      this.pickup = Pickup.DISALLOWED;
      this.shootingEntityId = entity.getId();
   }

   public void writeSpawnData(RegistryFriendlyByteBuf buf) {
      Vec3 motion = this.getDeltaMovement();
      buf.writeDouble(motion.x);
      buf.writeDouble(motion.y);
      buf.writeDouble(motion.z);
      buf.writeFloat(this.getXRot());
      buf.writeFloat(this.getYRot());
      buf.writeByte(this.type);
      buf.writeInt(this.shootingEntityId);
   }

   public void readSpawnData(RegistryFriendlyByteBuf buf) {
      this.setDeltaMovement(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
      this.setXRot(buf.readFloat());
      this.setYRot(buf.readFloat());
      this.xRotO = this.getXRot();
      this.yRotO = this.getYRot();
      this.type = buf.readByte();
      this.shootingEntityId = buf.readInt();
   }

   public void playerTouch(@NotNull Player player) {
   }

   /**
    * Эффекты по типу стрелы — порт {@code EntityPrimalArrow.inflictDamage} (1.7.10,
    * строки 366-392).
    *
    * <p>В порте их не было: пять типов стрел различались только множителем урона
    * ({@code getBaseDamage}), то есть ледяная стрела не замедляла, стрела порядка
    * не давала эффекта, и тип 5 не работал вовсе. Урон и горение обрабатывает
    * {@code AbstractArrow}, здесь только наложение эффектов.
    *
    * <p>В оригинале {@code case} в 1.7.10 проваливаются: тип 2 получает
    * замедление и дополнительно эффект типа 5, тип 5 — свой эффект, оба затем
    * бьют обычной стрелой. Значения: тип 2 — slowness 200 тиков с усилением 4;
    * тип 4 — wither 200 тиков с усилением 4; тип 5 — wither 100 тиков без
    * усиления ({@code Potion.field_82731_v}).
    */
   @Override
   protected void doPostHurtEffects(net.minecraft.world.entity.LivingEntity target) {
      if (this.type == 2) {
         target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 200, 4));

         if (this.type == 5) {
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
               net.minecraft.world.effect.MobEffects.WITHER, 100, 0));
         }
      } else if (this.type == 5) {
         target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.WITHER, 100, 0));
      } else if (this.type == 4) {
         target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.WITHER, 200, 4));
      }
   }

   @NotNull
   protected ItemStack getPickupItem() {
      return ItemStack.EMPTY;
   }

   @NotNull
   protected ItemStack getDefaultPickupItem() {
      return ItemStack.EMPTY;
   }

   public double getBaseDamage() {
      return switch (this.type) {
         case 3 -> this.damage * 1.5;
         case 4, 5 -> this.damage * 0.8;
         default -> this.damage;
      };
   }

   public void addAdditionalSaveData(@NotNull CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putByte("type", (byte)this.type);
   }

   public void readAdditionalSaveData(@NotNull CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      this.type = compound.getByte("type");
   }
}
