package thaumcraft.block.infusion_claw;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ClickBehavior;
import thaumcraft.api.RegisteredClick;
import thaumcraft.api.block.TickableEntityBlock;

public class InfusionClawBlock extends TickableEntityBlock {
   public InfusionClawBlock() {
      super(Properties.of().strength(3.5F).sound(SoundType.STONE).noOcclusion());
      RegisteredClick.init();
   }

   @NotNull
   @Override
   public RenderShape getRenderShape(@NotNull BlockState state) {
      return RenderShape.INVISIBLE;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new InfusionClawBlockEntity(pos, state);
   }

   public void neighborChanged(
      @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving
   ) {
      if (!level.isClientSide && level.getBlockEntity(pos) instanceof InfusionClawBlockEntity claw) {
         ;
      }

      super.neighborChanged(state, level, pos, block, fromPos, isMoving);
   }

   @Override
   public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
      if (!level.isClientSide && placer instanceof Player player && level.getBlockEntity(pos) instanceof InfusionClawBlockEntity claw) {
         boolean var10000 = false;
      }

      super.setPlacedBy(level, pos, state, placer, stack);
   }

   @NotNull
   @Override
   public InteractionResult useWithoutItem(
      @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit
   ) {
      if (!level.isClientSide && level.getBlockEntity(pos) instanceof InfusionClawBlockEntity claw) {
         ;
      }

      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   @Override
   public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
      if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof InfusionClawBlockEntity claw) {
         ;
      }

      super.onRemove(state, level, pos, newState, isMoving);
   }

   public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
      return true;
   }

   public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
      if (pos.getY() > level.getMinBuildHeight()) {
         ClickBehavior behavior = RegisteredClick.getClawClickBehavior(level, pos.below());
         if (behavior != null) {
            return behavior.getComparatorOutput();
         }
      }

      return 0;
   }
}
