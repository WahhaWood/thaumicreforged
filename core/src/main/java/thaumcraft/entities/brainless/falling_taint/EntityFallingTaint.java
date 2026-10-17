package thaumcraft.entities.brainless.falling_taint;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModEntities;

public class EntityFallingTaint extends Entity implements IEntityWithComplexSpawn {
   private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(EntityFallingTaint.class, EntityDataSerializers.INT);
   public int fallTime = 0;
   public int oldX;
   public int oldY;
   public int oldZ;

   public EntityFallingTaint(EntityType<? extends EntityFallingTaint> type, Level level) {
      super(type, level);
   }

   public EntityFallingTaint(Level level, double x, double y, double z, BlockState mimic, BlockPos oldPos) {
      this((EntityType<? extends EntityFallingTaint>)ModEntities.FALLING_TAINT.get(), level);
      this.blocksBuilding = true;
      this.setPos(x, y, z);
      this.setDeltaMovement(Vec3.ZERO);
      this.setMimicState(mimic);
      this.oldX = oldPos.getX();
      this.oldY = oldPos.getY();
      this.oldZ = oldPos.getZ();
      this.setBoundingBox(this.getBoundingBox().inflate(0.0));
      this.refreshDimensions();
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(DATA_STATE, Block.getId(Blocks.SAND.defaultBlockState()));
   }

   public void setMimicState(BlockState state) {
      this.entityData.set(DATA_STATE, Block.getId(state));
   }

   public BlockState getMimicState() {
      return Block.stateById((Integer)this.entityData.get(DATA_STATE));
   }

   public boolean isPickable() {
      return !this.isRemoved();
   }

   public void tick() {
      BlockState mimic = this.getMimicState();
      if (mimic != null && !mimic.isAir()) {
         this.xo = this.getX();
         this.yo = this.getY();
         this.zo = this.getZ();
         this.fallTime++;
         Vec3 v = this.getDeltaMovement().add(0.0, -0.04, 0.0);
         this.setDeltaMovement(v);
         this.move(MoverType.SELF, this.getDeltaMovement());
         Vec3 v2 = this.getDeltaMovement();
         this.setDeltaMovement(v2.x * 0.98, v2.y * 0.98, v2.z * 0.98);

         if (!this.level().isClientSide) {
            BlockPos currentPos = this.blockPosition();
            if (this.onGround()) {
               this.level().playSound(null, currentPos, (net.minecraft.sounds.SoundEvent)thaumcraft.init.registry.ModSounds.GORE.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
               if (this.canPlace(currentPos)) {
                  this.level().setBlock(currentPos, mimic, 3);
               }
               this.discard();
            } else if (this.fallTime > 100 && (currentPos.getY() <= this.level().getMinBuildHeight() || this.fallTime > 600)) {
               this.discard();
            }
         }
      } else {
         this.discard();
      }
   }

   private boolean canPlace(BlockPos pos) {
      BlockState at = this.level().getBlockState(pos);
      if (at.is((Block)ModBlocks.FIBROUS_TAINT.get()) || at.is((Block)ModBlocks.FLUX_GOO.get())) {
         return true;
      } else if (at.is((Block)ModBlocks.TAINTED_PLANT.get()) || at.is((Block)ModBlocks.TAINTED_GRASS.get())) {
         return true;
      } else {
         return !at.is((Block)ModBlocks.SPORE_STALK.get()) && !at.is((Block)ModBlocks.MATURE_SPORE_STALK.get()) ? at.canBeReplaced() : true;
      }
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      this.entityData.set(DATA_STATE, tag.getInt("State"));
      this.fallTime = tag.getInt("Time");
      this.oldX = tag.getInt("OldX");
      this.oldY = tag.getInt("OldY");
      this.oldZ = tag.getInt("OldZ");
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
      tag.putInt("State", (Integer)this.entityData.get(DATA_STATE));
      tag.putInt("Time", this.fallTime);
      tag.putInt("OldX", this.oldX);
      tag.putInt("OldY", this.oldY);
      tag.putInt("OldZ", this.oldZ);
   }

   public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
      buffer.writeVarInt((Integer)this.entityData.get(DATA_STATE));
      buffer.writeVarInt(this.oldX);
      buffer.writeVarInt(this.oldY);
      buffer.writeVarInt(this.oldZ);
   }

   public void readSpawnData(RegistryFriendlyByteBuf buffer) {
      this.entityData.set(DATA_STATE, buffer.readVarInt());
      this.oldX = buffer.readVarInt();
      this.oldY = buffer.readVarInt();
      this.oldZ = buffer.readVarInt();
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
      return super.getAddEntityPacket(entity);
   }

   public boolean displayFireAnimation() {
      return false;
   }
}
