package thaumcraft.block.alchemy_furnace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;

public class AlchemyFurnaceBlock extends TickableEntityBlock {
   public static final BooleanProperty FILLED = BooleanProperty.create("filled");

   public AlchemyFurnaceBlock() {
      super(Properties.of().sound(SoundType.METAL).strength(3.0F, 17.0F));
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH))
               .setValue(ACTIVE, false))
            .setValue(FILLED, false)
      );
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{BlockStateProperties.HORIZONTAL_FACING, ACTIVE, FILLED});
   }

   @Nullable
   public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
   }

   public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (player.isShiftKeyDown()) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   @NotNull
   @Override
   public net.minecraft.world.InteractionResult useWithoutItem(
      @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit
   ) {
      if (player.isShiftKeyDown()) {
         return net.minecraft.world.InteractionResult.PASS;
      }
      if (level.getBlockEntity(pos) instanceof AlchemyFurnaceBlockEntity furnace) {
         if (level.isClientSide) {
            return net.minecraft.world.InteractionResult.SUCCESS;
         } else if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.openMenu(furnace, buf -> buf.writeBlockPos(pos));
            return net.minecraft.world.InteractionResult.CONSUME;
         }
      }
      return net.minecraft.world.InteractionResult.PASS;
   }

   @Override
   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      if (!state.is(newState.getBlock())) {
         if (level.getBlockEntity(pos) instanceof AlchemyFurnaceBlockEntity furnace) {
            ;
         }

         super.onRemove(state, level, pos, newState, isMoving);
      }
   }

   public boolean isRandomlyTicking(@NotNull BlockState state) {
      return true;
   }

   public void animateTick(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull RandomSource random) {
      if ((Boolean)blockState.getValue(ACTIVE)) {
         float f = blockPos.getX() + 0.5F;
         float f1 = blockPos.getY() + 0.2F + random.nextFloat() * 5.0F / 16.0F;
         float f2 = blockPos.getZ() + 0.5F;
         float f3 = 0.52F;
         float f4 = random.nextFloat() * 0.5F - 0.25F;
         level.addParticle(ParticleTypes.SMOKE, f - f3, f1, f2 + f4, 0.0, 0.0, 0.0);
         level.addParticle(ParticleTypes.FLAME, f - f3, f1, f2 + f4, 0.0, 0.0, 0.0);
         level.addParticle(ParticleTypes.SMOKE, f + f3, f1, f2 + f4, 0.0, 0.0, 0.0);
         level.addParticle(ParticleTypes.FLAME, f + f3, f1, f2 + f4, 0.0, 0.0, 0.0);
         level.addParticle(ParticleTypes.SMOKE, f + f4, f1, f2 - f3, 0.0, 0.0, 0.0);
         level.addParticle(ParticleTypes.FLAME, f + f4, f1, f2 - f3, 0.0, 0.0, 0.0);
         level.addParticle(ParticleTypes.SMOKE, f + f4, f1, f2 + f3, 0.0, 0.0, 0.0);
         level.addParticle(ParticleTypes.FLAME, f + f4, f1, f2 + f3, 0.0, 0.0, 0.0);
      }
   }

   public int getLightEmission(BlockState blockState, BlockGetter level, BlockPos pos) {
      return blockState.getValue(ACTIVE) ? 12 : super.getLightEmission(blockState, level, pos);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new AlchemyFurnaceBlockEntity(blockPos, blockState);
   }

   public boolean hasAnalogOutputSignal(@NotNull BlockState blockState) {
      return true;
   }

   public int getAnalogOutputSignal(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos) {
      return level.getBlockEntity(blockPos) instanceof AlchemyFurnaceBlockEntity furnace ? AbstractContainerMenu.getRedstoneSignalFromContainer(furnace) : 0;
   }

   public void neighborChanged(
      @NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Block block, @NotNull BlockPos blockPos1, boolean p_60514_
   ) {
      if (level.getBlockEntity(blockPos) instanceof AlchemyFurnaceBlockEntity furnace) {
         ;
      }
   }
}
