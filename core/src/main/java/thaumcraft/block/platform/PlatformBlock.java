package thaumcraft.block.platform;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlatformBlock extends BaseEntityBlock {
   public static final MapCodec<PlatformBlock> CODEC = simpleCodec(props -> new PlatformBlock());
   private static final VoxelShape SOLID = Shapes.block();
   private static final VoxelShape EMPTY = Shapes.empty();

   public PlatformBlock() {
      super(
         Properties.of()
            .mapColor(MapColor.WOOD)
            .strength(2.0F, 5.0F)
            .sound(SoundType.WOOD)
            .noOcclusion()
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)
      );
   }

   @NotNull
   public RenderShape getRenderShape(@NotNull BlockState state) {
      return RenderShape.MODEL;
   }

   @Nullable
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new PlatformBlockEntity(pos, state);
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   @NotNull
   public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      if (context instanceof EntityCollisionContext entityContext) {
         if (entityContext.getEntity() instanceof Player player && player.isShiftKeyDown()) {
            return EMPTY;
         } else {
            return entityContext.isAbove(SOLID, pos, true) ? SOLID : EMPTY;
         }
      } else {
         return SOLID;
      }
   }

   @NotNull
   public ItemInteractionResult useItemOn(
      @NotNull ItemStack stack,
      @NotNull BlockState state,
      @NotNull Level level,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hit
   ) {
      if (level.getBlockEntity(pos) instanceof PlatformBlockEntity platform) {
         BlockState mimic = PlatformBlock.BlockStateUtils.resolveMimic(stack, player, hand, hit);
         if (mimic == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
         } else {
            if (!level.isClientSide) {
               platform.setMimic(mimic);
            }

            return ItemInteractionResult.sidedSuccess(level.isClientSide);
         }
      } else {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
   }

   private static final class BlockStateUtils {
      @Nullable
      private static BlockState resolveMimic(ItemStack held, Player player, InteractionHand hand, BlockHitResult hit) {
         if (held.isEmpty()) {
            return Blocks.AIR.defaultBlockState();
         } else if (held.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof PlatformBlock) {
               return null;
            } else {
               BlockPlaceContext placeContext = new BlockPlaceContext(new UseOnContext(player, hand, hit));
               BlockState placementState = block.getStateForPlacement(placeContext);
               BlockState mimicState = placementState == null ? block.defaultBlockState() : placementState;
               return !mimicState.isAir() && mimicState.getRenderShape() == RenderShape.MODEL ? mimicState : null;
            }
         } else {
            return null;
         }
      }
   }
}
