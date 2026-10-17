package thaumcraft.block.bellows;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;

public class BellowsBlock extends TickableEntityBlock {
   public BellowsBlock() {
      super(Properties.of().forceSolidOn().noOcclusion().sound(SoundType.WOOD).strength(2.5F, 10.0F));
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP));
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{BlockStateProperties.FACING});
   }

   @Nullable
   public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(BlockStateProperties.FACING, context.getClickedFace().getOpposite());
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
      return Shapes.create(0.1F, 0.0, 0.1F, 0.9F, 1.0, 0.9F);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new BellowsBlockEntity(blockPos, blockState);
   }
}
