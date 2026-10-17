package thaumcraft.block.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import thaumcraft.api.block.TickableEntityBlock;

public abstract class BlockEldritchEntityBase extends TickableEntityBlock {
   protected BlockEldritchEntityBase(Properties properties) {
      super(properties);
   }

   @Override
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
      return 0;
   }

   public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
      return true;
   }
}
