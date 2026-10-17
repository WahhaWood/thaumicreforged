package thaumcraft.block.sensor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;

public class SensorBlock extends TickableEntityBlock {
   public SensorBlock() {
      super(Properties.of().forceSolidOn().noOcclusion().sound(SoundType.WOOD).strength(2.5F, 10.0F));
   }

   public void neighborChanged(
      @NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Block block, @NotNull BlockPos blockPos1, boolean b
   ) {
      if (!level.isClientSide && level.getBlockEntity(blockPos) instanceof SensorBlockEntity sensor) {
         sensor.updateTone();
      }
   }

   /** TC4 BlockWoodenDevice.onBlockActivated: a click raises the pitch by a semitone and plays it. */
   @NotNull
   @Override
   public InteractionResult useWithoutItem(
      @NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Player player, @NotNull BlockHitResult hitResult
   ) {
      if (!(level.getBlockEntity(blockPos) instanceof SensorBlockEntity sensor)) {
         return InteractionResult.PASS;
      }

      if (!level.isClientSide) {
         sensor.changePitch();
         sensor.triggerNote(level, blockPos, true);
      }

      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   public void onPlace(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState1, boolean p_60570_) {
      super.onPlace(blockState, level, blockPos, blockState1, p_60570_);
      if (!level.isClientSide && level.getBlockEntity(blockPos) instanceof SensorBlockEntity sensor) {
         sensor.updateTone();
      }
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new SensorBlockEntity(blockPos, blockState);
   }

   /** paramA is the instrument to play, or -1 (255 once it crossed the network as a byte) for the note particle alone. */
   public boolean triggerEvent(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, int paramA, int paramB) {
      int instrument = (byte)paramA;
      if (instrument < -1 || instrument >= NoteBlockInstrument.values().length) {
         return super.triggerEvent(blockState, level, blockPos, paramA, paramB);
      }

      if (instrument >= 0) {
         NoteBlockInstrument played = NoteBlockInstrument.values()[instrument];
         float pitch = played.isTunable() ? NoteBlock.getPitchFromNote(paramB) : 1.0F;
         level.playSeededSound(
            null, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, played.getSoundEvent(), SoundSource.RECORDS, 3.0F, pitch, level.random.nextLong()
         );
      }

      level.addParticle(ParticleTypes.NOTE, blockPos.getX() + 0.5, blockPos.getY() + 1.2, blockPos.getZ() + 0.5, paramB / 24.0, 0.0, 0.0);
      return true;
   }

   public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
      return true;
   }

   public int getSignal(@NotNull BlockState blockState, @NotNull BlockGetter getter, @NotNull BlockPos blockPos, @NotNull Direction direction) {
      return getter.getBlockEntity(blockPos) instanceof SensorBlockEntity sensor ? (sensor.redstoneSignal > 0 ? 15 : 0) : 0;
   }

   public int getDirectSignal(@NotNull BlockState blockState, @NotNull BlockGetter getter, @NotNull BlockPos blockPos, @NotNull Direction direction) {
      return getter.getBlockEntity(blockPos) instanceof SensorBlockEntity sensor ? (sensor.redstoneSignal > 0 ? 15 : 0) : 0;
   }

   public boolean isSignalSource(@NotNull BlockState blockState) {
      return true;
   }
}
