package thaumcraft.block.tube.valve;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.block.tube.TubeBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class TubeValveBlockEntity extends TubeBlockEntity {
   public boolean allowFlow = true;
   private boolean wasPoweredLastTick = false;
   public float rotation = 0.0F;

   public TubeValveBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.TUBE_VALVE.get(), blockPos, blockState);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      super.loadCustom(compound);
      this.allowFlow = compound.getBoolean("flow");
      this.wasPoweredLastTick = compound.getBoolean("hadpower");
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      super.saveCustom(compound);
      compound.putBoolean("flow", this.allowFlow);
      compound.putBoolean("hadpower", this.wasPoweredLastTick);
   }

   /** Реакция на редстоун: питание закрывает поток (порт TileTubeValve). */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      super.serverTick(level, pos, state);
      if (level.getGameTime() % 5L != 0L) {
         return;
      }

      boolean powered = this.gettingPower();
      if (this.wasPoweredLastTick != powered) {
         this.wasPoweredLastTick = powered;
         this.allowFlow = !powered;
         this.setChanged();
         this.markForUpdate();
         level.playSound(null, pos, net.minecraft.sounds.SoundEvents.IRON_TRAPDOOR_CLOSE, net.minecraft.sounds.SoundSource.BLOCKS, 0.3F, powered ? 0.8F : 1.2F);
      }
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      if (!this.allowFlow && this.rotation < 360.0F) {
         this.rotation += 20.0F;
      } else if (this.allowFlow && this.rotation > 0.0F) {
         this.rotation -= 20.0F;
      }

      this.rotation = Mth.clamp(this.rotation, 0.0F, 360.0F);
      super.clientTick(level, pos, state);
   }

   @Override
   public boolean isConnectable(Direction face) {
      return face != this.facing && super.isConnectable(face);
   }

   @Override
   public void setSuction(Aspect aspect, int amount) {
      if (this.allowFlow) {
         super.setSuction(aspect, amount);
      } else {
         super.setSuction(null, 0);
      }
   }

   /** TC4 TileTubeValve: the handle turns to the next side with nothing to connect to. */
   @Override
   protected void rotate() {
      int a = this.facing.get3DDataValue();
      while (++a < 20) {
         Direction next = Direction.from3DDataValue(a % 6);
         if (!this.canConnectSide(next)) {
            this.setFacing(next);
            return;
         }
      }
   }

   public boolean gettingPower() {
      return this.level == null ? false : this.level.hasNeighborSignal(this.worldPosition);
   }

   public void toggleValve() {
      this.allowFlow = !this.allowFlow;
      this.setChanged();
      this.markForUpdate();
   }

   public float getRotation(float partialTick) {
      return this.rotation;
   }
}
