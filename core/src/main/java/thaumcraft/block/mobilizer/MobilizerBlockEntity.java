package thaumcraft.block.mobilizer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.ITickable;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;

public class MobilizerBlockEntity extends ThaumcraftBlockEntity implements ITickable {
   private static final String TAG_LINKED = "Linked";
   private static final String TAG_FIRST_RELAY_X = "FirstRelayX";
   private static final String TAG_FIRST_RELAY_Z = "FirstRelayZ";
   private static final String TAG_SECOND_RELAY_X = "SecondRelayX";
   private static final String TAG_SECOND_RELAY_Z = "SecondRelayZ";
   private static final String TAG_DIRECTION = "Direction";
   private static final String TAG_DEAD = "Dead";
   private boolean linked;
   private int firstRelayX;
   private int secondRelayX;
   private int firstRelayZ;
   private int secondRelayZ;
   private boolean dead;
   private Direction movementDirection = Direction.NORTH;

   public MobilizerBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.MOBILIZER.get(), pos, state);
   }

   @Override
   public void loadCustom(CompoundTag compound) {
      this.linked = compound.getBoolean("Linked");
      this.firstRelayX = compound.getInt("FirstRelayX");
      this.firstRelayZ = compound.getInt("FirstRelayZ");
      this.secondRelayX = compound.getInt("SecondRelayX");
      this.secondRelayZ = compound.getInt("SecondRelayZ");
      this.movementDirection = Direction.from3DDataValue(compound.getInt("Direction"));
      this.dead = compound.getBoolean("Dead");
   }

   @Override
   public void saveCustom(CompoundTag compound) {
      compound.putBoolean("Linked", this.linked);
      compound.putInt("FirstRelayX", this.firstRelayX);
      compound.putInt("FirstRelayZ", this.firstRelayZ);
      compound.putInt("SecondRelayX", this.secondRelayX);
      compound.putInt("SecondRelayZ", this.secondRelayZ);
      compound.putInt("Direction", this.movementDirection.get3DDataValue());
      compound.putBoolean("Dead", this.dead);
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (!this.dead) {
         this.verifyRelay();
         if (this.linked && !level.hasNeighborSignal(pos)) {
            if (level.getGameTime() % 100L == 0L) {
               this.updateDirectionBeforeMove();
            }

            if (level.getGameTime() % 100L == 1L) {
               this.moveOneStep();
            }
         }
      }
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
   }

   public void verifyRelay() {
      if (this.level != null) {
         BlockEntity relayTile = this.level.getBlockEntity(new BlockPos(this.firstRelayX, this.worldPosition.getY(), this.firstRelayZ));
         if (relayTile instanceof MobilizerRelayBlockEntity relay) {
            relay.verifyPartner();
         }

         boolean valid = this.linked && relayTile instanceof MobilizerRelayBlockEntity relay && this.relayIsConnectedToTarget(relay);
         if (!valid) {
            this.linked = false;
            this.setChanged();
         }
      }
   }

   private boolean relayIsConnectedToTarget(MobilizerRelayBlockEntity relay) {
      return relay.hasPartner() && relay.getPartnerX() == this.secondRelayX && relay.getPartnerZ() == this.secondRelayZ;
   }

   private void updateDirectionBeforeMove() {
      BlockPos targetPos = this.worldPosition.relative(this.movementDirection);
      if (!this.isMovementPathClear(targetPos)) {
         this.movementDirection = this.movementDirection.getOpposite();
         this.setChanged();
      }
   }

   private void moveOneStep() {
      if (this.level != null && !this.level.isClientSide) {
         BlockPos pos = this.worldPosition;
         BlockPos targetPos = pos.relative(this.movementDirection);
         BlockPos sourcePassengerPos = pos.above();
         BlockPos targetPassengerPos = targetPos.above();
         if (this.level.getBlockState(pos).is((Block)ModBlocks.MOBILIZER.get())) {
            if (this.isMovementPathClear(targetPos)) {
               if (!this.cantBreak(targetPos)) {
                  BlockEntity passengerEntity = this.level.getBlockEntity(sourcePassengerPos);
                  BlockState passengerState = this.level.getBlockState(sourcePassengerPos);
                  if (passengerEntity == null && !passengerState.isAir()) {
                     if (!passengerState.canSurvive(this.level, targetPassengerPos)) {
                        return;
                     }

                     if (this.cantBreak(targetPassengerPos)) {
                        return;
                     }

                     this.level.setBlock(targetPassengerPos, passengerState, 11);
                     if (!passengerState.isAir()) {
                        if (this.cantBreak(sourcePassengerPos)) {
                           return;
                        }

                        this.level.setBlock(sourcePassengerPos, Blocks.AIR.defaultBlockState(), 2);
                     }
                  }

                  BlockState mobilizerState = this.getBlockState();
                  this.level.setBlock(targetPos, mobilizerState, 3);
                  if (this.level.getBlockEntity(targetPos) instanceof MobilizerBlockEntity moved) {
                     moved.copyFrom(this);
                     moved.dead = false;
                     moved.setChanged();
                  }

                  this.dead = true;
                  this.level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
               }
            }
         }
      }
   }

   private boolean isMovementPathClear(BlockPos targetPos) {
      if (this.level == null) {
         return false;
      } else {
         BlockPos currentAbove = this.worldPosition.above();
         BlockPos targetAbove = targetPos.above();
         BlockState targetBase = this.level.getBlockState(targetPos);
         BlockState targetTop = this.level.getBlockState(targetAbove);
         BlockState currentTop = this.level.getBlockState(currentAbove);
         boolean targetBaseAir = targetBase.isAir();
         boolean targetTopAir = targetTop.isAir();
         boolean currentTopAir = currentTop.isAir();
         return targetBaseAir && (currentTopAir || targetTopAir);
      }
   }

   private void copyFrom(MobilizerBlockEntity source) {
      this.linked = source.linked;
      this.firstRelayX = source.firstRelayX;
      this.firstRelayZ = source.firstRelayZ;
      this.secondRelayX = source.secondRelayX;
      this.secondRelayZ = source.secondRelayZ;
      this.movementDirection = source.movementDirection;
   }

   public boolean isLinked() {
      return this.linked;
   }

   public void linkToRelays(int firstRelayX, int firstRelayZ, int secondRelayX, int secondRelayZ, Direction direction) {
      this.firstRelayX = firstRelayX;
      this.firstRelayZ = firstRelayZ;
      this.secondRelayX = secondRelayX;
      this.secondRelayZ = secondRelayZ;
      this.movementDirection = direction;
      this.linked = true;
      this.setChanged();
   }

   public void setDead(boolean dead) {
      this.dead = dead;
   }
}
