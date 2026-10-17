package thaumcraft.block.sensor;

import java.util.ArrayList;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ITickable;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class SensorBlockEntity extends ThaumcraftBlockEntity implements ITickable {
   public byte note = 0;
   public byte tone = 0;
   public int redstoneSignal = 0;
   public static WeakHashMap<ServerLevel, ArrayList<Integer[]>> noteBlockEvents = new WeakHashMap<>();

   public SensorBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.ARCANE_SENSOR.get(), blockPos, blockState);
   }

   @Override
   protected void saveAdditional(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveAdditional(compound, provider);
      compound.putByte("note", this.note);
      compound.putByte("tone", this.tone);
   }

   @Override
   public void loadAdditional(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadAdditional(compound, provider);
      this.note = compound.getByte("note");
      this.tone = compound.getByte("tone");
      if (this.note < 0) {
         this.note = 0;
      }

      if (this.note > 24) {
         this.note = 24;
      }
   }

   /** TC4 TileSensor.updateEntity: the signal fades after 10 ticks; a note block within 64 blocks playing our note and instrument starts it. */
   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (this.redstoneSignal > 0) {
         this.redstoneSignal--;
         if (this.redstoneSignal == 0) {
            this.updateSignal(level, pos, state);
         }
      }

      ArrayList<Integer[]> heard = noteBlockEvents.get(level);
      if (heard != null) {
         for (Integer[] dat : heard) {
            if (dat[3] == this.tone && dat[4] == this.note && pos.getCenter().distanceToSqr(dat[0] + 0.5, dat[1] + 0.5, dat[2] + 0.5) <= 4096.0) {
               this.triggerNote(level, pos, false);
               this.redstoneSignal = 10;
               this.updateSignal(level, pos, state);
               break;
            }
         }
      }
   }

   private void updateSignal(Level level, BlockPos pos, BlockState state) {
      level.updateNeighborsAt(pos, state.getBlock());
      level.updateNeighborsAt(pos.below(), state.getBlock());
      this.markForUpdate();
   }

   /** The instrument a note block would play here: TC4 matched the material below, today the block below names it. */
   public void updateTone() {
      NoteBlockInstrument instrument = this.level.getBlockState(this.worldPosition.below()).instrument();
      if (instrument.worksAboveNoteBlock()) {
         instrument = NoteBlockInstrument.HARP;
      }

      this.tone = (byte)instrument.ordinal();
      this.setChanged();
   }

   public void changePitch() {
      this.note = (byte)((this.note + 1) % 25);
      this.setChanged();
   }

   /** Shows the note (and plays it when {@code sound}) if there is air above, like a note block. */
   public void triggerNote(Level level, BlockPos pos, boolean sound) {
      if (level.getBlockState(pos.above()).isAir()) {
         level.blockEvent(pos, this.getBlockState().getBlock(), sound ? this.tone : -1, this.note);
      }
   }
}
