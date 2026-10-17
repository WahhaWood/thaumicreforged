package thaumcraft.block.wand_recharger;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;

public class WandPedestalBlock extends TickableEntityBlock {
   public WandPedestalBlock() {
      super(Properties.of().forceSolidOn().sound(SoundType.STONE).strength(3.0F, 25.0F).noOcclusion());
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new WandPedestalBlockEntity(blockPos, blockState);
   }

   public boolean hasAnalogOutputSignal(@NotNull BlockState blockState) {
      return true;
   }

   @NotNull
   public ItemInteractionResult useItemOn(
      @NotNull ItemStack stack,
      @NotNull BlockState blockState,
      @NotNull Level level,
      @NotNull BlockPos blockPos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hitResult
   ) {
      if (level.getBlockEntity(blockPos) instanceof WandPedestalBlockEntity ped) {
         if (ped.getItem(0).isEmpty()) {
            if (ped.canPlaceItem(0, stack)) {
               if (!level.isClientSide) {
                  ItemStack placed = stack.copyWithCount(1);
                  stack.shrink(1);
                  ped.setItem(0, placed);
                  ped.setChanged();
                  ped.markForUpdate();
                  level.playSound(null, blockPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F, 1.6F);
               }
               return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
         } else {
            if (!level.isClientSide) {
               ItemStack item = ped.removeItem(0, 1);
               if (!player.getInventory().add(item)) {
                  Block.popResource(level, blockPos.above(), item);
               }
               ped.setChanged();
               ped.markForUpdate();
               level.playSound(null, blockPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F, 1.5F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
         }
      }
      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   @NotNull
   @Override
   public InteractionResult useWithoutItem(
      @NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Player player, @NotNull BlockHitResult hitResult
   ) {
      if (level.getBlockEntity(blockPos) instanceof WandPedestalBlockEntity ped) {
         if (!ped.getItem(0).isEmpty()) {
            if (!level.isClientSide) {
               ItemStack item = ped.removeItem(0, 1);
               if (!player.getInventory().add(item)) {
                  Block.popResource(level, blockPos.above(), item);
               }
               ped.setChanged();
               ped.markForUpdate();
               level.playSound(null, blockPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F, 1.5F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
         }
      }
      return InteractionResult.PASS;
   }

   @Override
   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      if (!state.is(newState.getBlock())) {
         if (level.getBlockEntity(pos) instanceof WandPedestalBlockEntity ped) {
            Containers.dropContents(level, pos, ped);
         }
         super.onRemove(state, level, pos, newState, isMoving);
      }
   }
}
