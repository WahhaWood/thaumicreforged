package thaumicenergistics.block.provider;

import thaumicenergistics.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class EssentiaProviderBlock extends AbstractProviderBlock<EssentiaProviderBlockEntity> {
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new EssentiaProviderBlockEntity(pos, state);
   }

   @Nullable
   @Override
   public <E extends BlockEntity> BlockEntityTicker<E> getTicker(Level level, BlockState state, BlockEntityType<E> type) {
      return !level.isClientSide()
         ? createTickerHelper(type, (BlockEntityType)ModBlockEntities.ESSENTIA_PROVIDER.get(), (lvl, pos, st, be) -> ((EssentiaProviderBlockEntity) be).serverTick())
         : null;
   }

   @Nullable
   private static <E extends BlockEntity, T extends BlockEntity> BlockEntityTicker<E> createTickerHelper(
      BlockEntityType<E> actualType, BlockEntityType<T> expectedType, BlockEntityTicker<T> ticker
   ) {
      return actualType == expectedType ? (BlockEntityTicker<E>) ticker : null;
   }
}
