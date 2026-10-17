package thaumcraft.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ITickable {
   default void clientTick(Level level, BlockPos pos, BlockState state) {
   }

   default void serverTick(Level level, BlockPos pos, BlockState state) {
   }
}
