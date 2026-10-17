package thaumcraft.entities.brainless.following;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.entities.brainless.special.SpecialItemEntity;
import thaumcraft.init.registry.ModEntities;

public class FollowingItemEntity extends SpecialItemEntity implements IEntityWithComplexSpawn {
   private Vec3 targetPos = Vec3.ZERO;
   public Entity target = null;
   private int type = 3;
   private int age = 20;

   public FollowingItemEntity(EntityType<? extends ItemEntity> entityType, Level level) {
      super(entityType, level);
   }

   public FollowingItemEntity(Level level, Vec3 pos, ItemStack stack) {
      super((EntityType<? extends ItemEntity>)ModEntities.FOLLOWING_ITEM.get(), level, pos, stack);
   }

   public FollowingItemEntity(Level level, Vec3 pos, ItemStack stack, Entity target, int type) {
      super((EntityType<? extends ItemEntity>)ModEntities.FOLLOWING_ITEM.get(), level, pos, stack);
      this.target = target;
      this.targetPos = new Vec3(target.getX(), target.getY(0.5), target.getZ());
      this.type = type;
      this.noPhysics = true;
      this.setNoGravity(true);
   }

   public FollowingItemEntity(Level level, double x, double y, double z, ItemStack stack, Entity target, int type) {
      this(level, new Vec3(x, y, z), stack, target, type);
   }

   public FollowingItemEntity(Level level, Vec3 pos, ItemStack stack, double tx, double ty, double tz) {
      super((EntityType<? extends ItemEntity>)ModEntities.FOLLOWING_ITEM.get(), level, pos, stack);
      this.targetPos = new Vec3(tx, ty, tz);
      this.noPhysics = true;
      this.setNoGravity(true);
   }

   @Override
   public void tick() {
      if (this.target != null) {
         this.targetPos = new Vec3(this.target.getX(), this.target.getY(0.5), this.target.getZ());
      }

      boolean following = this.target != null || this.targetPos.x != 0.0 || this.targetPos.y != 0.0 || this.targetPos.z != 0.0;
      if (following) {
         this.noPhysics = true;
         this.setNoGravity(true);
         Vec3 det = this.targetPos.subtract(this.position());
         if (this.age > 1) {
            this.age--;
         }

         double dist = det.length();
         if (dist > 0.5) {
            dist *= this.age;
            this.setDeltaMovement(det.x / dist, det.y / dist, det.z / dist);
         } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.1));
            this.targetPos = Vec3.ZERO;
            this.target = null;
            this.noPhysics = false;
            this.setNoGravity(false);
         }

         if (this.level().isClientSide) {
            if (this.type != 10) {
               Thaumcraft.setup
                  .sparkle(
                     (float)this.xo + (this.random.nextFloat() - this.random.nextFloat()) * 0.125F,
                     (float)this.yo + (this.random.nextFloat() - this.random.nextFloat()) * 0.125F,
                     (float)this.zo + (this.random.nextFloat() - this.random.nextFloat()) * 0.125F,
                     this.type
                  );
            } else {
               Thaumcraft.setup
                  .crucibleBubble(
                     this.level(),
                     (float)this.xo + (this.random.nextFloat() - this.random.nextFloat()) * 0.125F,
                     (float)this.yo + (this.random.nextFloat() - this.random.nextFloat()) * 0.125F,
                     (float)this.zo + (this.random.nextFloat() - this.random.nextFloat()) * 0.125F,
                     0.33F,
                     0.33F,
                     1.0F
                  );
            }
         }
      } else {
         this.noPhysics = false;
         this.setNoGravity(false);
      }

      super.tick();
   }

   public void addAdditionalSaveData(@NotNull CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putByte("type", (byte)this.type);
      tag.putByte("age", (byte)this.age);
   }

   public void readAdditionalSaveData(@NotNull CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.type = tag.getByte("type");
      if (tag.contains("age")) {
         this.age = tag.getByte("age");
      }
   }

   public void writeSpawnData(RegistryFriendlyByteBuf buf) {
      buf.writeInt(this.target == null ? -1 : this.target.getId());
      buf.writeDouble(this.targetPos.x);
      buf.writeDouble(this.targetPos.y);
      buf.writeDouble(this.targetPos.z);
      buf.writeByte(this.type);
      buf.writeByte(this.age);
   }

   public void readSpawnData(RegistryFriendlyByteBuf buf) {
      int ent = buf.readInt();
      if (ent > -1) {
         this.target = this.level().getEntity(ent);
      }

      this.targetPos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
      this.type = buf.readByte();
      this.age = buf.readByte();
   }
}
