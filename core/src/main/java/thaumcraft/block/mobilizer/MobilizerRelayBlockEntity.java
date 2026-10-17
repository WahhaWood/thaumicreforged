package thaumcraft.block.mobilizer;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ITickable;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class MobilizerRelayBlockEntity extends ThaumcraftBlockEntity implements ITickable {
   private static final int PARTNER_SCAN_RANGE = 32;
   private static final String TAG_HAS_PARTNER = "HasPartner";
   private static final String TAG_PARTNER_X = "PartnerX";
   private static final String TAG_PARTNER_Z = "PartnerZ";
   private boolean hasPartner;
   private int partnerX;
   private int partnerZ;

   public MobilizerRelayBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.MOBILIZER_RELAY.get(), pos, state);
   }

   @Override
   public void loadCustom(CompoundTag compound) {
      this.hasPartner = compound.getBoolean("HasPartner");
      this.partnerX = compound.getInt("PartnerX");
      this.partnerZ = compound.getInt("PartnerZ");
   }

   @Override
   public void saveCustom(CompoundTag compound) {
      compound.putBoolean("HasPartner", this.hasPartner);
      compound.putInt("PartnerX", this.partnerX);
      compound.putInt("PartnerZ", this.partnerZ);
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      this.verifyPartner();
      if (level.getGameTime() % 40L == 0L) {
         this.checkForPartner();
         if (this.hasPartner) {
            this.linkTrackMobilizers();
         }
      }
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      this.verifyPartner();
      if (this.hasPartner) {
         this.spawnTrackParticles();
      }
   }

   public void verifyPartner() {
      if (this.level != null) {
         boolean valid = this.hasPartner
            && this.level.getBlockEntity(new BlockPos(this.partnerX, this.worldPosition.getY(), this.partnerZ)) instanceof MobilizerRelayBlockEntity relay
            && relay.partnerX == this.worldPosition.getX()
            && relay.partnerZ == this.worldPosition.getZ();
         if (!valid) {
            this.hasPartner = false;
         }
      }
   }

   private void spawnTrackParticles() {
      if (this.level != null) {
         Direction direction = this.getTrackDirection();
         long phase = this.level.getGameTime() % 20L;
         this.forEachTrackPosition(trackPos -> {
            float xInc = direction.getStepX() == 0 ? 0.0F : 0.05F * direction.getStepX() * (float)phase;
            float zInc = direction.getStepZ() == 0 ? 0.0F : 0.05F * direction.getStepZ() * (float)phase;
            int color = direction.getStepX() >= 0 && direction.getStepZ() >= 0 ? 2 : 14;
            Thaumcraft.setup.sparkle(trackPos.getX() + 0.5F + xInc, this.worldPosition.getY() + 0.5F, trackPos.getZ() + 0.5F + zInc, color);
         });
      }
   }

   private void linkTrackMobilizers() {
      Direction movementDirection = this.partnerX != this.worldPosition.getX() ? Direction.EAST : Direction.NORTH;
      this.forEachTrackPosition(trackPos -> {
         if (this.level.getBlockEntity(trackPos) instanceof MobilizerBlockEntity mobilizer) {
            mobilizer.verifyRelay();
            if (!mobilizer.isLinked()) {
               mobilizer.linkToRelays(this.worldPosition.getX(), this.worldPosition.getZ(), this.partnerX, this.partnerZ, movementDirection);
            }
         }
      });
   }

   private void checkForPartner() {
      if (this.level != null && !this.hasPartner) {
         int y = this.worldPosition.getY();

         for (int i = -32; i < 32; i++) {
            if (i != 0) {
               this.trySetPartner(this.level.getBlockEntity(new BlockPos(this.worldPosition.getX(), y, this.worldPosition.getZ() + i)));
               if (this.hasPartner) {
                  return;
               }

               this.trySetPartner(this.level.getBlockEntity(new BlockPos(this.worldPosition.getX() + i, y, this.worldPosition.getZ())));
               if (this.hasPartner) {
                  return;
               }
            }
         }
      }
   }

   private void trySetPartner(BlockEntity blockEntity) {
      if (blockEntity instanceof MobilizerRelayBlockEntity relay) {
         relay.partnerX = this.worldPosition.getX();
         relay.partnerZ = this.worldPosition.getZ();
         relay.hasPartner = true;
         relay.setChanged();
         this.partnerX = relay.worldPosition.getX();
         this.partnerZ = relay.worldPosition.getZ();
         this.hasPartner = true;
         this.setChanged();
      }
   }

   private Direction getTrackDirection() {
      if (this.partnerX > this.worldPosition.getX()) {
         return Direction.EAST;
      } else if (this.partnerX < this.worldPosition.getX()) {
         return Direction.WEST;
      } else {
         return this.partnerZ > this.worldPosition.getZ() ? Direction.SOUTH : Direction.NORTH;
      }
   }

   private void forEachTrackPosition(Consumer<BlockPos> consumer) {
      int startX = this.worldPosition.getX();
      int startZ = this.worldPosition.getZ();
      int y = this.worldPosition.getY();
      int stepX = Integer.compare(this.partnerX, startX);
      int stepZ = Integer.compare(this.partnerZ, startZ);
      if (stepX != 0) {
         for (int x = startX; x != this.partnerX + stepX; x += stepX) {
            consumer.accept(new BlockPos(x, y, startZ));
         }
      } else if (stepZ != 0) {
         for (int z = startZ; z != this.partnerZ + stepZ; z += stepZ) {
            consumer.accept(new BlockPos(startX, y, z));
         }
      } else {
         consumer.accept(this.worldPosition);
      }
   }

   public boolean hasPartner() {
      return this.hasPartner;
   }

   public int getPartnerX() {
      return this.partnerX;
   }

   public int getPartnerZ() {
      return this.partnerZ;
   }
}
