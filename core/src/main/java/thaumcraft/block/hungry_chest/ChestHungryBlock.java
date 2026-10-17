package thaumcraft.block.hungry_chest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.block.TickableEntityBlock;

public class ChestHungryBlock extends TickableEntityBlock {
   protected static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

   public ChestHungryBlock() {
      super(Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2.5F));
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
   }

   public boolean triggerEvent(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
      BlockEntity be = level.getBlockEntity(pos);
      return be != null ? be.triggerEvent(id, param) : false;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{BlockStateProperties.HORIZONTAL_FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   @Override
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }

   @Override
   public void stepOn(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.Entity entity) {
      this.entityInside(state, level, pos, entity);
      super.stepOn(level, pos, state, entity);
   }

   @Override
   public void entityInside(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.Entity entity) {
      if (!level.isClientSide && entity instanceof net.minecraft.world.entity.item.ItemEntity itemEntity && entity.isAlive()) {
         if (level.getBlockEntity(pos) instanceof ChestHungryBlockEntity chest) {
            net.minecraft.world.item.ItemStack stack = itemEntity.getItem();
            net.minecraft.world.item.ItemStack remainder = chest.add(stack);
            if (remainder.getCount() != stack.getCount()) {
               level.playSound(null, pos, net.minecraft.sounds.SoundEvents.GENERIC_EAT, net.minecraft.sounds.SoundSource.BLOCKS, 0.25F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
               level.blockEvent(pos, this, 2, 10);
            }
            if (remainder.isEmpty()) {
               itemEntity.discard();
            } else {
               itemEntity.setItem(remainder);
            }
         }
      }
   }

   @Override
   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      if (!state.is(newState.getBlock())) {
         if (level.getBlockEntity(pos) instanceof ChestHungryBlockEntity chest) {
            net.minecraft.world.Containers.dropContents(level, pos, chest);
            level.updateNeighbourForOutputSignal(pos, this);
         }
         super.onRemove(state, level, pos, newState, isMoving);
      }
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   @Override
   protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
      return net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new ChestHungryBlockEntity(pos, state);
   }
}
