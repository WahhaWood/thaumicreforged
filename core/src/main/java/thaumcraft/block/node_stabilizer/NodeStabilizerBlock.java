package thaumcraft.block.node_stabilizer;

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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;
import thaumcraft.init.registry.ModBlocks;

/** TC4 works only from below the node (facing up); facing down from above is our addition (E12). */
public class NodeStabilizerBlock extends TickableEntityBlock {
   public static final DirectionProperty FACING = BlockStateProperties.VERTICAL_DIRECTION;
   private static final VoxelShape SHAPE_UP = box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
   private static final VoxelShape SHAPE_DOWN = box(0.0, 4.0, 0.0, 16.0, 16.0, 16.0);

   public NodeStabilizerBlock() {
      super(Properties.of().forceSolidOn().noOcclusion().forceSolidOn().sound(SoundType.STONE).strength(3.0F, 25.0F));
      this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.UP));
   }

   @Override
   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   /** Faces the adjacent node; with no node next to it, hangs from a ceiling or stands on a floor. */
   @Override
   public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
      BlockPos pos = context.getClickedPos();
      Direction facing;
      if (isNode(context.getLevel().getBlockState(pos.above()))) {
         facing = Direction.UP;
      } else if (isNode(context.getLevel().getBlockState(pos.below()))) {
         facing = Direction.DOWN;
      } else {
         facing = context.getClickedFace() == Direction.DOWN ? Direction.DOWN : Direction.UP;
      }

      return this.defaultBlockState().setValue(FACING, facing);
   }

   public static boolean isNode(BlockState state) {
      return state.is(ModBlocks.AURA_NODE.get()) || state.is(ModBlocks.ENERGIZED_AURA_NODE.get());
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
      return blockState.getValue(FACING) == Direction.UP ? SHAPE_UP : SHAPE_DOWN;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new NodeStabilizerBlockEntity(blockPos, blockState);
   }
}
