package thaumcraft.entities.brainless.eldrich_orb;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.ModEntities;

public class EntityEldritchOrb extends ThrowableItemProjectile {
   public EntityEldritchOrb(EntityType<? extends EntityEldritchOrb> type, Level level) {
      super(type, level);
      this.setNoGravity(true);
   }

   public EntityEldritchOrb(Level level, LivingEntity owner) {
      super((EntityType)ModEntities.ELDRITCH_ORB.get(), owner, level);
      this.setNoGravity(true);
   }

   protected Item getDefaultItem() {
      return Items.AIR;
   }

   protected double getDefaultGravity() {
      return 0.0;
   }

   public void tick() {
      super.tick();
      if (this.tickCount > 100 && !this.level().isClientSide) {
         this.discard();
      }
   }

   @Override
   protected void onHitEntity(net.minecraft.world.phys.EntityHitResult result) {
      super.onHitEntity(result);
      if (!this.level().isClientSide) {
         net.minecraft.world.entity.Entity target = result.getEntity();
         net.minecraft.world.entity.Entity owner = this.getOwner();
         if (target != owner && !(target instanceof thaumcraft.api.entities.IEldritchMob)) {
            target.hurt(this.damageSources().indirectMagic(this, owner instanceof LivingEntity living ? living : null), 6.0F);
            if (target instanceof LivingEntity living) {
               // 1.7.10:77 — Potion.field_76437_t (wither) на 160 тиков без
               // усиления. Ослаблен под сервер: 120 тиков (6 с).
               living.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER, 120, 0));
            }
         }
         this.level().broadcastEntityEvent(this, (byte)16);
         this.discard();
      }
   }

   @Override
   protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
      super.onHitBlock(result);
      if (!this.level().isClientSide) {
         this.level().broadcastEntityEvent(this, (byte)16);
         this.discard();
      }
   }

   @NotNull
   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
      return super.getAddEntityPacket(entity);
   }

   public void handleEntityEvent(byte id) {
      if (id == 16 && this.level().isClientSide) {
         for (int unused = 0; unused < 30; unused++) {
            float _x = (this.random.nextFloat() - this.random.nextFloat()) * 0.3F;
            float _xx = (this.random.nextFloat() - this.random.nextFloat()) * 0.3F;
            float _xxx = (this.random.nextFloat() - this.random.nextFloat()) * 0.3F;
            Thaumcraft.setup
               .wispFX3(
                  this.level(),
                  this.getX() + _x,
                  this.getY() + _xx,
                  this.getZ() + _xxx,
                  this.getX() + _x * 8.0F,
                  this.getY() + _xx * 8.0F,
                  this.getZ() + _xxx * 8.0F,
                  0.3F,
                  5,
                  true,
                  0.02F
               );
         }
      } else {
         super.handleEntityEvent(id);
      }
   }
}
