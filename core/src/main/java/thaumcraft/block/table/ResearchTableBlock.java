package thaumcraft.block.table;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;
import thaumcraft.init.registry.ModBlocks;

public class ResearchTableBlock extends TickableEntityBlock {
   private static final VoxelShape[] SHAPES = (VoxelShape[])Util.make(
      () -> {
         VoxelShape up = box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0);
         VoxelShape[] legs = new VoxelShape[]{
            box(10.0, 0.0, 10.0, 14.0, 12.0, 14.0),
            box(2.0, 0.0, 10.0, 6.0, 12.0, 14.0),
            box(10.0, 0.0, 2.0, 14.0, 12.0, 6.0),
            box(2.0, 0.0, 2.0, 6.0, 12.0, 6.0)
         };
         return new VoxelShape[]{
            Shapes.or(legs[0], new VoxelShape[]{legs[1], box(6.0, 2.0, 0.0, 10.0, 6.0, 12.0), up}),
            Shapes.or(legs[2], new VoxelShape[]{legs[3], box(6.0, 2.0, 4.0, 10.0, 6.0, 16.0), up}),
            Shapes.or(legs[2], new VoxelShape[]{legs[0], box(0.0, 2.0, 6.0, 12.0, 6.0, 10.0), up}),
            Shapes.or(legs[3], new VoxelShape[]{legs[1], box(4.0, 2.0, 6.0, 16.0, 6.0, 10.0), up})
         };
      }
   );
   public static final BooleanProperty MAIN = BooleanProperty.create("main");
   public static final BooleanProperty HAS_TOOLS = BooleanProperty.create("has_tools");
   public static final BooleanProperty HAS_NOTES = BooleanProperty.create("has_notes");

   public ResearchTableBlock() {
      super(Properties.of().destroyTime(2.5F).sound(SoundType.WOOD).pushReaction(PushReaction.BLOCK).noOcclusion().forceSolidOn());
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(MAIN, true)).setValue(HAS_TOOLS, false))
               .setValue(HAS_NOTES, false))
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
      );
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
      int index = ((Direction)blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)).ordinal() - 2;
      return index < 0 ? Shapes.block() : SHAPES[index];
   }

   @NotNull
   @Override
   public RenderShape getRenderShape(@NotNull BlockState blockState) {
      return blockState.getValue(MAIN) ? RenderShape.MODEL : RenderShape.INVISIBLE;
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{MAIN, HAS_TOOLS, HAS_NOTES, BlockStateProperties.HORIZONTAL_FACING});
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
      Direction dir = context.getHorizontalDirection();
      BlockPos partnerPos = context.getClickedPos().relative(dir);
      Level level = context.getLevel();
      if (level.getBlockState(partnerPos).canBeReplaced(context)) {
         return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, dir).setValue(MAIN, true);
      }
      return null;
   }

   @Override
   public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (!level.isClientSide && state.getValue(MAIN)) {
         Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
         BlockPos partner = pos.relative(dir);
         BlockState partnerState = this.defaultBlockState()
            .setValue(MAIN, false)
            .setValue(BlockStateProperties.HORIZONTAL_FACING, dir.getOpposite())
            .setValue(HAS_TOOLS, false)
            .setValue(HAS_NOTES, false);
         level.setBlock(partner, partnerState, 3);
      }
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return blockState.getValue(MAIN) ? new ResearchTableBlockEntity(blockPos, blockState) : null;
   }

   public void onNeighborChange(BlockState blockState, LevelReader levelReader, BlockPos blockPos, BlockPos neighbor) {
      if (levelReader instanceof Level level) {
         ;
      }
   }

   @NotNull
   @Override
   public ItemInteractionResult useItemOn(
      @NotNull ItemStack handStack,
      @NotNull BlockState blockState,
      @NotNull Level level,
      @NotNull BlockPos blockPos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult result
   ) {
      if (player.isShiftKeyDown()) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
      BlockPos targetPos = blockPos;
      if (!(Boolean)blockState.getValue(MAIN)) {
         targetPos = blockPos.relative((Direction)blockState.getValue(BlockStateProperties.HORIZONTAL_FACING));
      }

      if (level.getBlockEntity(targetPos) instanceof ResearchTableBlockEntity table) {
         if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
         } else if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            BlockPos openPos = targetPos;
            serverPlayer.openMenu(table, buf -> buf.writeBlockPos(openPos));
            return ItemInteractionResult.CONSUME;
         }
      }

      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   @NotNull
   @Override
   public InteractionResult useWithoutItem(
      @NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Player player, @NotNull BlockHitResult result
   ) {
      if (player.isShiftKeyDown()) {
         return InteractionResult.PASS;
      }
      BlockPos targetPos = blockPos;
      if (!(Boolean)blockState.getValue(MAIN)) {
         targetPos = blockPos.relative((Direction)blockState.getValue(BlockStateProperties.HORIZONTAL_FACING));
      }

      if (level.getBlockEntity(targetPos) instanceof ResearchTableBlockEntity table) {
         if (level.isClientSide) {
            return InteractionResult.SUCCESS;
         } else if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            BlockPos openPos = targetPos;
            serverPlayer.openMenu(table, buf -> buf.writeBlockPos(openPos));
            return InteractionResult.CONSUME;
         }
      }

      return InteractionResult.PASS;
   }

   @Override
   public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
      if (!state.is(newState.getBlock())) {
         if (!level.isClientSide) {
            BlockPos partnerPos = pos.relative((Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING));
            BlockState partnerState = level.getBlockState(partnerPos);
            if (partnerState.is((Block)ModBlocks.RESEARCH_TABLE.get())) {
               level.removeBlock(partnerPos, false);
               Block.popResource(level, partnerPos, new ItemStack((ItemLike)ModBlocks.TABLE.get()));
            }
         }

         super.onRemove(state, level, pos, newState, isMoving);
      }
   }
}
