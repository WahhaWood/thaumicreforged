package thaumcraft.block.repairer;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.ThaumcraftEntityBlock;
import thaumcraft.api.block.TickableEntityBlock;

public class RepairerBlock extends TickableEntityBlock {
   public static final MapCodec<RepairerBlock> CODEC = simpleCodec(props -> new RepairerBlock());
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

   public RepairerBlock() {
      super(Properties.of().strength(5.0F, 10.0F).noOcclusion());
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return SHAPE;
   }

   @NotNull
   @Override
   public RenderShape getRenderShape(@NotNull BlockState state) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }

   @Override
   protected MapCodec<? extends ThaumcraftEntityBlock> codec() {
      return CODEC;
   }

   @NotNull
   public ItemInteractionResult useItemOn(
      @NotNull ItemStack stack,
      @NotNull BlockState state,
      @NotNull Level level,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hit
   ) {
      if (level.getBlockEntity(pos) instanceof RepairerBlockEntity repairer) {
         ItemStack current = repairer.getItem(0);
         if (current.isEmpty()) {
            if (repairer.canPlaceItem(0, stack)) {
               repairer.setItem(0, stack.split(1));
               repairer.setChanged();
               repairer.markForUpdate();
               return ItemInteractionResult.sidedSuccess(level.isClientSide);
            } else {
               return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
         } else {
            if (!player.addItem(current.copy())) {
               Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, current.copy());
            }

            repairer.setItem(0, ItemStack.EMPTY);
            repairer.setChanged();
            repairer.markForUpdate();
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
         }
      } else {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
   }

   @Override
   public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
      if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof RepairerBlockEntity repairer) {
         Containers.dropContents(level, pos, repairer);
         level.updateNeighbourForOutputSignal(pos, this);
      }

      super.onRemove(state, level, pos, newState, isMoving);
   }

   public boolean isSignalSource(@NotNull BlockState state) {
      return true;
   }

   public int getSignal(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
      if (!(level.getBlockEntity(pos) instanceof RepairerBlockEntity repairer)) {
         return 0;
      } else {
         ItemStack stack = repairer.getItem(0);
         return !stack.isEmpty() && stack.getDamageValue() > 0 ? 15 : 0;
      }
   }

   public int getDirectSignal(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
      return this.getSignal(state, level, pos, direction);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new RepairerBlockEntity(pos, state);
   }
}
