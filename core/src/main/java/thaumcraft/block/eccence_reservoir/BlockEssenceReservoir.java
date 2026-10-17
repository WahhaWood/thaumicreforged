package thaumcraft.block.eccence_reservoir;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;

public class BlockEssenceReservoir extends TickableEntityBlock {
   public BlockEssenceReservoir() {
      super(Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 17.0F).noOcclusion());
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.DOWN));
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{BlockStateProperties.FACING});
   }

   @Nullable
   public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(BlockStateProperties.FACING, context.getNearestLookingDirection().getOpposite());
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new EssenceReservoirBlockEntity(blockPos, blockState);
   }

   public boolean hasAnalogOutputSignal(@NotNull BlockState blockState) {
      return true;
   }

   public int getAnalogOutputSignal(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos) {
      if (level.getBlockEntity(blockPos) instanceof EssenceReservoirBlockEntity reservoir) {
         float r = (float)reservoir.essence.visSize() / reservoir.maxAmount;
         return Mth.floor(r * 14.0F) + (reservoir.essence.visSize() > 0 ? 1 : 0);
      } else {
         return 0;
      }
   }

   @Override
   public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
      if (!state.is(newState.getBlock())) {
         if (level.getBlockEntity(pos) instanceof EssenceReservoirBlockEntity reservoir) {
            ;
         }

         super.onRemove(state, level, pos, newState, isMoving);
      }
   }
}
