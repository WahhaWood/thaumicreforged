package thaumicenergistics.block.industrial_alchemy_furnace;

import com.mojang.serialization.MapCodec;
import thaumicenergistics.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

public class IndustrialAlchemyFurnaceBlock extends BaseEntityBlock implements EntityBlock {
   public static final MapCodec<IndustrialAlchemyFurnaceBlock> CODEC = simpleCodec(properties -> new IndustrialAlchemyFurnaceBlock());
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   public IndustrialAlchemyFurnaceBlock() {
      super(Properties.of().mapColor(MapColor.METAL).strength(2.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (level.getBlockEntity(pos) instanceof IndustrialAlchemyFurnaceBlockEntity furnace) {
         furnace.syncToClient();
      }
   }

   @NotNull
   protected ItemInteractionResult useItemOn(
      ItemStack clickedStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
   ) {
      if (level.getBlockEntity(pos) instanceof IndustrialAlchemyFurnaceBlockEntity furnace) {
         if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(furnace, buf -> buf.writeBlockPos(pos));
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
      return new IndustrialAlchemyFurnaceBlockEntity(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
      return level instanceof ServerLevel
         ? createTickerHelper(
            blockEntityType, (BlockEntityType)ModBlockEntities.INDUSTRIAL_ALCHEMY_FURNACE.get(), IndustrialAlchemyFurnaceBlockEntity::serverTick
         )
         : null;
   }
}
