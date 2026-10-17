package thaumcraft.block.bore;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.ThaumcraftEntityBlock;

public class BoreBaseBlock extends ThaumcraftEntityBlock {
   public BoreBaseBlock() {
      super(Properties.of().noOcclusion().sound(SoundType.WOOD).strength(2.5F, 10.0F));
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{BlockStateProperties.HORIZONTAL_FACING});
   }

   @Nullable
   public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
   }

   public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
      return true;
   }

   @NotNull
   @Override
   public net.minecraft.world.InteractionResult useWithoutItem(
      @NotNull BlockState state, @NotNull net.minecraft.world.level.Level level, @NotNull BlockPos pos, @NotNull net.minecraft.world.entity.player.Player player, @NotNull net.minecraft.world.phys.BlockHitResult hit
   ) {
      if (player.isShiftKeyDown()) {
         return net.minecraft.world.InteractionResult.PASS;
      }
      for (Direction dir : Direction.values()) {
         BlockPos target = pos.relative(dir);
         if (level.getBlockEntity(target) instanceof BoreBlockEntity bore) {
            if (level.isClientSide) {
               return net.minecraft.world.InteractionResult.SUCCESS;
            } else if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
               serverPlayer.openMenu(bore, buf -> buf.writeBlockPos(target));
               return net.minecraft.world.InteractionResult.CONSUME;
            }
         }
      }
      return net.minecraft.world.InteractionResult.PASS;
   }

   @Nullable
   @Override
   public net.minecraft.world.MenuProvider getMenuProvider(BlockState state, net.minecraft.world.level.Level level, BlockPos pos) {
      for (Direction dir : Direction.values()) {
         BlockPos target = pos.relative(dir);
         if (level.getBlockEntity(target) instanceof BoreBlockEntity bore) {
            return bore;
         }
      }
      return null;
   }

   @Nullable
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new BoreBaseBlockEntity(blockPos, blockState);
   }
}
