package thaumcraft.block.tube.filter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssenceContainerItem;
import thaumcraft.block.tube.BlockTube;
import thaumcraft.init.registry.ModItems;
import thaumcraft.init.registry.ModSounds;

public class BlockTubeFiltered extends BlockTube {
   public BlockTubeFiltered(Properties properties) {
      super(properties);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new TileTubeFilter(pos, state);
   }

   /**
    * TC4 BlockTube.onBlockActivated (filter): sneaking takes the filter out as a blank label; a label with an aspect sets an
    * empty filter to that aspect and is used up.
    */
   public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (!(level.getBlockEntity(pos) instanceof TileTubeFilter filterTile)) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }

      if (player.isShiftKeyDown() && filterTile.aspectFilter != null) {
         if (level.isClientSide) {
            level.playLocalSound(pos, ModSounds.PAGE.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
         } else {
            filterTile.clearFilter();
            Direction side = hit.getDirection();
            level.addFreshEntity(new ItemEntity(
               level, pos.getX() + 0.5 + side.getStepX() / 3.0, pos.getY() + 0.5, pos.getZ() + 0.5 + side.getStepZ() / 3.0, new ItemStack(ModItems.JAR_LABEL.get())
            ));
         }

         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      }

      if (filterTile.aspectFilter == null && stack.is(ModItems.JAR_LABEL.get()) && stack.getItem() instanceof IEssenceContainerItem label) {
         AspectList aspects = label.getAspects(stack);
         if (aspects != null && aspects.size() > 0) {
            if (level.isClientSide) {
               level.playLocalSound(pos, ModSounds.PAGE.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
            } else {
               filterTile.setFilter(aspects.getAspects()[0]);
               stack.shrink(1);
            }
         }

         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      }

      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   @Override
   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      if (!state.is(newState.getBlock())) {
      }

      super.onRemove(state, level, pos, newState, isMoving);
   }
}
