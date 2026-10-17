package thaumicenergistics.block.crucible_inscriber;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrucibleInscriberBlock extends BaseEntityBlock implements EntityBlock {
   public static final MapCodec<CrucibleInscriberBlock> CODEC = simpleCodec(properties -> new CrucibleInscriberBlock());
   public static final DirectionProperty FACING = BlockStateProperties.FACING;

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   public CrucibleInscriberBlock() {
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

   @NotNull
   protected ItemInteractionResult useItemOn(
      ItemStack clickedStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
   ) {
      if (level.getBlockEntity(pos) instanceof CrucibleInscriberBlockEntity inscriber) {
         if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(inscriber, buf -> buf.writeBlockPos(pos));
         }

         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      }

      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   @NotNull
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new CrucibleInscriberBlockEntity(pos, state);
   }
}
