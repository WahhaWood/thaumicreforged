package thaumicenergistics.block.provider;

import appeng.api.util.AEColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractProviderBlock<T extends ProviderBaseBlockEntity> extends Block implements EntityBlock {
   public static final DirectionProperty FACING = BlockStateProperties.FACING;

   protected AbstractProviderBlock() {
      super(Properties.of().mapColor(MapColor.METAL).strength(1.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.DOWN));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction attachmentSide = context.getClickedFace().getOpposite();
      return (BlockState)this.defaultBlockState().setValue(FACING, attachmentSide);
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (level.getBlockEntity(pos) instanceof ProviderBaseBlockEntity provider) {
         int attachmentOrdinal = ((Direction)state.getValue(FACING)).ordinal();
         provider.setupProvider(attachmentOrdinal);
         if (placer instanceof Player player) {
            provider.setOwner(player);
         }
      }
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ProviderBaseBlockEntity provider) {
         provider.onBreakBlock();
      }

      super.onRemove(state, level, pos, newState, isMoving);
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      super.neighborChanged(state, level, pos, block, fromPos, isMoving);
      if (level.getBlockEntity(pos) instanceof ProviderBaseBlockEntity provider) {
         provider.checkGridConnectionColor();
      }
   }

   public boolean recolorBlock(Level level, BlockPos pos, Direction side, int colorIndex) {
      if (level.getBlockEntity(pos) instanceof ProviderBaseBlockEntity provider) {
         AEColor color = AEColor.values()[Mth.clamp(colorIndex, 0, AEColor.values().length - 1)];
         return provider.recolourBlock(side, color, null);
      } else {
         return false;
      }
   }

   public abstract BlockEntity newBlockEntity(BlockPos var1, BlockState var2);

   @Nullable
   public abstract <E extends BlockEntity> BlockEntityTicker<E> getTicker(Level var1, BlockState var2, BlockEntityType<E> var3);
}
