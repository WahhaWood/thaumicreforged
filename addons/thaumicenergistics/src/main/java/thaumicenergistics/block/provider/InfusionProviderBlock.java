package thaumicenergistics.block.provider;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class InfusionProviderBlock extends AbstractProviderBlock<InfusionProviderBlockEntity> {
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new InfusionProviderBlockEntity(pos, state);
   }

   @Nullable
   @Override
   public <E extends BlockEntity> BlockEntityTicker<E> getTicker(Level level, BlockState state, BlockEntityType<E> type) {
      return null;
   }
}
