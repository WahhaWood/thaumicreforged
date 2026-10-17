package thaumcraft.block.wand_pedestal_focus;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.ThaumcraftEntityBlock;
import thaumcraft.block.wand_recharger.WandPedestalBlock;
import thaumcraft.block.wand_recharger.WandPedestalBlockEntity;

public class CompoundRechargeFocusBlock extends ThaumcraftEntityBlock {
   public CompoundRechargeFocusBlock() {
      super(Properties.of().forceSolidOn().sound(SoundType.STONE).strength(3.0F, 25.0F));
   }

   @Nullable
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return null;
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
      BlockState state = level.getBlockState(blockPos.below());
      return state.getBlock() instanceof WandPedestalBlock pedestal
         ? pedestal.useItemOn(stack, state, level, blockPos.below(), player, hand, hitResult)
         : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   @NotNull
   @Override
   public InteractionResult useWithoutItem(
      @NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Player player, @NotNull BlockHitResult hitResult
   ) {
      BlockState state = level.getBlockState(blockPos.below());
      return state.getBlock() instanceof WandPedestalBlock pedestal
         ? pedestal.useWithoutItem(state, level, blockPos.below(), player, hitResult)
         : InteractionResult.PASS;
   }

   public boolean canSurvive(@NotNull BlockState blockState, @NotNull LevelReader reader, @NotNull BlockPos blockPos) {
      return reader.getBlockEntity(blockPos.below()) instanceof WandPedestalBlockEntity;
   }
}
