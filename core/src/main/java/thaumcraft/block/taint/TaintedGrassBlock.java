package thaumcraft.block.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class TaintedGrassBlock extends Block {
   public TaintedGrassBlock() {
      super(Properties.of().strength(1.0F, 5.0F).sound(SoundType.ROOTS).noOcclusion().noCollission().randomTicks().lightLevel(s -> 8));
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      super.neighborChanged(state, level, pos, block, fromPos, isMoving);
   }
}
