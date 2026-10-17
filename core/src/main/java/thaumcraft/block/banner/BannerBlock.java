package thaumcraft.block.banner;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.item.phial_essence.ItemPhialEssence;
import thaumcraft.util.ItemStackData;

public class BannerBlock extends AbstractBannerBlock {
   public static final MapCodec<BannerBlock> CODEC = simpleCodec(props -> new BannerBlock(null));
   public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
   private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

   public BannerBlock(DyeColor color) {
      super(
         color,
         Properties.of()
            .mapColor(MapColor.WOOD)
            .forceSolidOn()
            .instrument(NoteBlockInstrument.BASS)
            .noCollission()
            .strength(1.0F)
            .sound(SoundType.WOOD)
            .ignitedByLava()
      );
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(ROTATION, 0));
   }

   protected MapCodec<? extends AbstractBannerBlock> codec() {
      return CODEC;
   }

   public boolean canSurvive(@NotNull BlockState state, LevelReader reader, BlockPos pos) {
      return reader.getBlockState(pos.below()).isSolid();
   }

   @NotNull
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return SHAPE;
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(ROTATION, RotationSegment.convertToSegment(context.getRotation() + 180.0F));
   }

   @NotNull
   public BlockState updateShape(
      @NotNull BlockState state,
      @NotNull Direction direction,
      @NotNull BlockState blockState,
      @NotNull LevelAccessor accessor,
      @NotNull BlockPos pos,
      @NotNull BlockPos blockPos
   ) {
      return direction == Direction.DOWN && !state.canSurvive(accessor, pos)
         ? Blocks.AIR.defaultBlockState()
         : super.updateShape(state, direction, blockState, accessor, pos, blockPos);
   }

   @NotNull
   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(ROTATION, rotation.rotate((Integer)state.getValue(ROTATION), 16));
   }

   @NotNull
   public BlockState mirror(BlockState state, Mirror mirror) {
      return (BlockState)state.setValue(ROTATION, mirror.mirror((Integer)state.getValue(ROTATION), 16));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{ROTATION});
   }

   @NotNull
   public ItemInteractionResult useItemOn(
      @NotNull ItemStack stack,
      @NotNull BlockState state,
      @NotNull Level level,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hitResult
   ) {
      if ((player.isShiftKeyDown() || stack.getItem() instanceof ItemPhialEssence)
         && level.getBlockEntity(pos) instanceof BannerBlockEntity banner
         && banner.getColor() != null) {
         if (!level.isClientSide) {
         }

         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
   }

   @NotNull
   public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      List<ItemStack> drops = new ArrayList<>();
      BlockEntity blockEntity = (BlockEntity)builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
      if (blockEntity instanceof BannerBlockEntity banner) {
         DyeColor color = banner.getColor();
         ItemStack drop = new ItemStack(color != null ? (ItemLike)ModBlocks.BANNER.get(color).get() : (ItemLike)ModBlocks.CRIMSON_CULT_BANNER.get());
         if (banner.getAspect() != null) {
            CompoundTag compound = ItemStackData.getOrCreateTag(drop);
            if (banner.getAspect() != null) {
               compound.putString("aspect", banner.getAspect().getTag());
            }
         }

         drops.add(drop);
      }

      return drops;
   }

   @Nullable
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new BannerBlockEntity(blockPos, blockState);
   }
}
