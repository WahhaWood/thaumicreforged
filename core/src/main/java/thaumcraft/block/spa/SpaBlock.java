package thaumcraft.block.spa;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;

public class SpaBlock extends TickableEntityBlock {
   public SpaBlock() {
      super(Properties.of().sound(SoundType.STONE).strength(3.0F, 25.0F).noOcclusion().isRedstoneConductor((s, l, p) -> false));
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new SpaBlockEntity(pos, state);
   }

   @NotNull
   public ItemInteractionResult useItemOn(
      @NotNull ItemStack held,
      @NotNull BlockState state,
      @NotNull Level level,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hit
   ) {
      if (player.isShiftKeyDown()) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      } else if (level.getBlockEntity(pos) instanceof SpaBlockEntity spa) {
         if (FluidUtil.interactWithFluidHandler(player, hand, spa)) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
         }
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      } else {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
   }

   @NotNull
   @Override
   public InteractionResult useWithoutItem(
      @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit
   ) {
      if (player.isShiftKeyDown()) {
         return InteractionResult.PASS;
      }
      if (level.getBlockEntity(pos) instanceof SpaBlockEntity spa) {
         if (level.isClientSide) {
            return InteractionResult.SUCCESS;
         } else if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(spa, buf -> buf.writeBlockPos(pos));
            return InteractionResult.CONSUME;
         }
      }
      return InteractionResult.PASS;
   }
}
