package thaumcraft.block.lamp;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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

public class ArcaneLampBlock extends TickableEntityBlock {
   public ArcaneLampBlock() {
      this(15);
   }

   public ArcaneLampBlock(int light) {
      super(Properties.of().lightLevel(value -> light).sound(SoundType.METAL).strength(3.0F, 17.0F));
      this.registerDefaultState(
         (BlockState)((BlockState)this.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH)).setValue(ACTIVE, false)
      );
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter getter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
      return Shapes.create(0.25, 0.125, 0.25, 0.75, 0.875, 0.75);
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{ACTIVE, BlockStateProperties.FACING});
   }

   @Nullable
   public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(BlockStateProperties.FACING, context.getClickedFace().getOpposite());
   }

   @Override
   public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, boolean p_60519_) {
      if (level.getBlockEntity(blockPos) instanceof ArcaneLampBlockEntity arcaneLamp) {
         ;
      }

      super.onRemove(state, level, blockPos, blockState, p_60519_);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new ArcaneLampBlockEntity(blockPos, blockState);
   }

   public void neighborChanged(
      @NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Block block, @NotNull BlockPos blockPos1, boolean p_60514_
   ) {
      if (level.getBlockEntity(blockPos) instanceof ArcaneLampBlockEntity lamp) {
         ;
      }
   }
}
