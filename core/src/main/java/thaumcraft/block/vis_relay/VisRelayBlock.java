package thaumcraft.block.vis_relay;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;
import thaumcraft.item.shard.ItemShard;

public class VisRelayBlock extends TickableEntityBlock {
   public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");
   private static final Map<Direction, VoxelShape> INSIDES = (Map<Direction, VoxelShape>)Util.make(() -> {
      Map<Direction, VoxelShape> map = new HashMap<>();
      map.put(Direction.DOWN, box(5.5, 0.0, 5.5, 10.5, 8.0, 10.5));
      map.put(Direction.UP, box(5.5, 8.0, 5.5, 10.5, 16.0, 10.5));
      map.put(Direction.NORTH, box(5.5, 5.5, 0.0, 10.5, 10.5, 8.0));
      map.put(Direction.SOUTH, box(5.5, 5.5, 8.0, 10.5, 10.5, 16.0));
      map.put(Direction.WEST, box(0.0, 5.5, 5.5, 8.0, 10.5, 10.5));
      map.put(Direction.EAST, box(8.0, 5.5, 5.5, 16.0, 10.5, 10.5));
      return map;
   });

   public VisRelayBlock() {
      super(
         Properties.of()
            .forceSolidOn()
            .noOcclusion()
            .lightLevel(state -> state.getValue(CONNECTED) ? 10 : 2)
            .sound(SoundType.METAL)
            .strength(3.0F, 17.0F)
      );
      this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH).setValue(CONNECTED, false));
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{BlockStateProperties.FACING, CONNECTED});
   }

   /** TC4 BlockMetalDevice.onNeighborBlockChange: the relay drops once the block it hangs on turns to air. */
   @NotNull
   @Override
   protected BlockState updateShape(
      @NotNull BlockState state,
      @NotNull Direction direction,
      @NotNull BlockState neighborState,
      @NotNull LevelAccessor level,
      @NotNull BlockPos pos,
      @NotNull BlockPos neighborPos
   ) {
      if (direction == state.getValue(BlockStateProperties.FACING) && neighborState.isAir()) {
         return Blocks.AIR.defaultBlockState();
      }

      return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
   }

   @NotNull
   @Override
   protected ItemInteractionResult useItemOn(
      @NotNull ItemStack stack,
      @NotNull BlockState state,
      @NotNull Level level,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hitResult
   ) {
      return tuneWithShard(stack, level, pos, player);
   }

   /**
    * TC4 BlockMetalDevice.onBlockActivated (relay and charge relay): a primal shard tunes the relay to its aspect,
    * the same shard again or a balanced shard clears the tuning. The shard is not used up.
    */
   public static ItemInteractionResult tuneWithShard(ItemStack stack, Level level, BlockPos pos, Player player) {
      if (player.isShiftKeyDown() || !(stack.getItem() instanceof ItemShard shard) || !(level.getBlockEntity(pos) instanceof VisRelayBlockEntity relay)) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }

      if (!level.isClientSide) {
         byte c = (byte)shard.getColor();
         relay.tune(c != relay.color && c < VisRelayBlockEntity.colors.length ? c : -1);
      }

      return ItemInteractionResult.sidedSuccess(level.isClientSide);
   }

   @Nullable
   public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(BlockStateProperties.FACING, context.getClickedFace().getOpposite());
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
      return INSIDES.get(blockState.getValue(BlockStateProperties.FACING));
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new VisRelayBlockEntity(blockPos, blockState);
   }
}
