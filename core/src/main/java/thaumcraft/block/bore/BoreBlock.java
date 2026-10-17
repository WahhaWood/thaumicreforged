package thaumcraft.block.bore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;
import thaumcraft.init.registry.ModBlocks;

public class BoreBlock extends TickableEntityBlock {
   public BoreBlock() {
      super(Properties.of().forceSolidOn().noOcclusion().sound(SoundType.WOOD).strength(2.5F, 10.0F));
      this.registerDefaultState(
         (BlockState)((BlockState)this.defaultBlockState().setValue(BlockStateProperties.VERTICAL_DIRECTION, Direction.UP))
            .setValue(BlockStateProperties.FACING, Direction.NORTH)
      );
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{BlockStateProperties.VERTICAL_DIRECTION, BlockStateProperties.FACING});
   }

   @Nullable
   public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
      Direction baseOrientation = context.getClickedFace().getOpposite();
      if (baseOrientation.getAxis().isHorizontal()) {
         return null;
      } else {
         BlockPos pos = context.getClickedPos();
         BlockPos basePos = pos.relative(baseOrientation);
         if (!context.getLevel().getBlockState(basePos).is((Block)ModBlocks.ARCANE_BORE_BASE.get())) {
            return null;
         } else {
            Direction facing = context.getHorizontalDirection().getOpposite();
            return (BlockState)((BlockState)this.defaultBlockState().setValue(BlockStateProperties.VERTICAL_DIRECTION, baseOrientation))
               .setValue(BlockStateProperties.FACING, facing);
         }
      }
   }

   @NotNull
   @Override
   public RenderShape getRenderShape(@NotNull BlockState blockState) {
      return RenderShape.INVISIBLE;
   }

   public void neighborChanged(
      @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean moving
   ) {
   }

   @Override
   public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity entity, @NotNull ItemStack stack) {
      if (entity != null) {
         ;
      }
   }

   public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
      return true;
   }

   @NotNull
   @Override
   public net.minecraft.world.InteractionResult useWithoutItem(
      @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull net.minecraft.world.entity.player.Player player, @NotNull net.minecraft.world.phys.BlockHitResult hit
   ) {
      if (player.isShiftKeyDown()) {
         return net.minecraft.world.InteractionResult.PASS;
      }
      if (level.getBlockEntity(pos) instanceof BoreBlockEntity bore) {
         if (level.isClientSide) {
            return net.minecraft.world.InteractionResult.SUCCESS;
         } else if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.openMenu(bore, buf -> buf.writeBlockPos(pos));
            return net.minecraft.world.InteractionResult.CONSUME;
         }
      }
      return net.minecraft.world.InteractionResult.PASS;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new BoreBlockEntity(blockPos, blockState);
   }
}
