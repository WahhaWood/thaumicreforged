package thaumcraft.block.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public abstract class BlockEldritchStructureCoreBase extends BlockEldritchEntityBase {
   private static final ThreadLocal<Boolean> TC_ELDRITCH_REMOVING = ThreadLocal.withInitial(() -> false);

   protected BlockEldritchStructureCoreBase(Properties properties) {
      super(properties);
   }

   @Override
   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      if (state.getBlock() != newState.getBlock() && !TC_ELDRITCH_REMOVING.get()) {
      }

      super.onRemove(state, level, pos, newState, isMoving);
   }
}
