package thaumicenergistics.block.infusion_assembler;

import com.mojang.serialization.MapCodec;
import thaumicenergistics.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InfusionAssemblerBlock extends BaseEntityBlock implements EntityBlock {
   public static final MapCodec<InfusionAssemblerBlock> CODEC = simpleCodec(properties -> new InfusionAssemblerBlock());
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   private static final VoxelShape SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.85, 1.0);

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   public InfusionAssemblerBlock() {
      super(Properties.of().mapColor(MapColor.METAL).strength(1.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   @NotNull
   public VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
      return SHAPE;
   }

   @NotNull
   protected ItemInteractionResult useItemOn(
      ItemStack clickedStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
   ) {
      if (level.getBlockEntity(pos) instanceof InfusionAssemblerBlockEntity assembler) {
         if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(assembler, buf -> buf.writeBlockPos(pos));
         }

         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      }

      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof InfusionAssemblerBlockEntity assembler) {
         assembler.onBreak();
      }

      super.onRemove(state, level, pos, newState, isMoving);
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (level.getBlockEntity(pos) instanceof InfusionAssemblerBlockEntity assembler && placer instanceof Player player) {
         assembler.setOwner(player);
         int facing = Mth.floor((double)(placer.getYRot() * 4.0F / 360.0F) + 0.5) & 3;
         switch (facing) {
            case 0:
               assembler.setRotation(2);
               break;
            case 1:
               assembler.setRotation(5);
               break;
            case 2:
               assembler.setRotation(3);
               break;
            case 3:
               assembler.setRotation(4);
         }
      }
   }

   @NotNull
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new InfusionAssemblerBlockEntity(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      if (type != ModBlockEntities.INFUSION_ASSEMBLER.get()) {
         return null;
      } else {
         return level.isClientSide()
            ? (lvl, pos, st, be) -> InfusionAssemblerBlockEntity.clientTick(lvl, pos, st, (InfusionAssemblerBlockEntity)be)
            : (lvl, pos, st, be) -> InfusionAssemblerBlockEntity.serverTick(lvl, pos, st, (InfusionAssemblerBlockEntity)be);
      }
   }
}
