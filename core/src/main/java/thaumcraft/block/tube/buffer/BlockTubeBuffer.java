package thaumcraft.block.tube.buffer;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.block.tube.BlockTube;

public class BlockTubeBuffer extends BlockTube {
   public BlockTubeBuffer(Properties properties) {
      super(properties);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new TubeBufferBlockEntity(pos, state);
   }

   public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
      return true;
   }

   public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
      if (level.getBlockEntity(pos) instanceof TubeBufferBlockEntity buffer) {
         int aspectCount = buffer.aspects.visSize();
         float capacity = 8.0F;
         float signal = aspectCount / capacity * 14.0F;
         return Mth.floor(signal) + (aspectCount > 0 ? 1 : 0);
      } else {
         return 0;
      }
   }
}
