package thaumcraft.block.mnemonic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.block.thaumatorium.ThaumatoriumBlock;
import thaumcraft.init.registry.ModBlocks;

public class MnemonicMatrixBlock extends Block {
   private static final VoxelShape INSIDE = box(3.0, 3.0, 3.0, 13.0, 13.0, 13.0);

   public MnemonicMatrixBlock() {
      super(Properties.of().sound(SoundType.METAL).strength(3.0F, 17.0F));
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(BlockStateProperties.FACING_HOPPER, Direction.DOWN));
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{BlockStateProperties.FACING_HOPPER});
   }

   @NotNull
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
      return INSIDE;
   }

   @NotNull
   public VoxelShape getInteractionShape(@NotNull BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos blockPos) {
      return INSIDE;
   }

   @Nullable
   public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
      Level level = context.getLevel();
      Direction direction = context.getClickedFace().getOpposite();
      BlockState blockState = level.getBlockState(context.getClickedPos().relative(direction));
      if (blockState.is((Block)ModBlocks.THAUMATORIUM.get())) {
         ThaumatoriumBlock.Element element = (ThaumatoriumBlock.Element)blockState.getValue(ThaumatoriumBlock.ELEMENT);
         if ((element == ThaumatoriumBlock.Element.TOP || element == ThaumatoriumBlock.Element.CENTER)
            && blockState.getValue(BlockStateProperties.HORIZONTAL_FACING) != context.getClickedFace()) {
            return (BlockState)this.defaultBlockState().setValue(BlockStateProperties.FACING_HOPPER, direction);
         }
      }

      return null;
   }

   public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
      if (level instanceof ServerLevel serverLevel) {
         ;
      }
   }
}
