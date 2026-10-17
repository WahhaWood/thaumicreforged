package thaumcraft.block.flux_scrubber;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;

public class FluxScrubberBlock extends TickableEntityBlock {
   public FluxScrubberBlock() {
      super(Properties.of().forceSolidOn().noOcclusion().forceSolidOn().sound(SoundType.STONE).strength(3.0F, 25.0F));
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP));
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{BlockStateProperties.FACING});
   }

   @Nullable
   public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(BlockStateProperties.FACING, context.getClickedFace());
   }

   @Nullable
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new FluxScrubberBlockEntity(blockPos, blockState);
   }
}
