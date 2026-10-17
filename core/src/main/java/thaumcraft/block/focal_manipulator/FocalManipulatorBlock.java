package thaumcraft.block.focal_manipulator;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ServerPlayer;
import thaumcraft.api.block.TickableEntityBlock;
import thaumcraft.research.ResearchManager;

public class FocalManipulatorBlock extends TickableEntityBlock {
   public FocalManipulatorBlock() {
      super(Properties.of().noOcclusion().forceSolidOn().sound(SoundType.STONE).strength(3.0F, 25.0F));
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new FocalManipulatorBlockEntity(pos, state);
   }

   @NotNull
   @Override
   public InteractionResult useWithoutItem(
      @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit
   ) {
      if (player.isShiftKeyDown()) {
         return InteractionResult.PASS;
      } else if (ResearchManager.isResearchComplete(player, "focal_manipulator")) {
         if (level.getBlockEntity(pos) instanceof FocalManipulatorBlockEntity be) {
            if (level.isClientSide) {
               return InteractionResult.SUCCESS;
            } else if (player instanceof ServerPlayer serverPlayer) {
               serverPlayer.openMenu(be, buf -> buf.writeBlockPos(pos));
               return InteractionResult.CONSUME;
            }
         }
         return InteractionResult.PASS;
      } else {
         if (!level.isClientSide) {
            player.sendSystemMessage(Component.translatable("tc.researchmissing").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   }
}
