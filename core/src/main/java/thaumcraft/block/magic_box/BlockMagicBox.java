package thaumcraft.block.magic_box;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.api.block.TickableEntityBlock;
import thaumcraft.init.registry.Config;

public class BlockMagicBox extends TickableEntityBlock {
   public BlockMagicBox() {
      super(Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).noOcclusion());
   }

   @Override
   public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (player.isShiftKeyDown()) {
         return InteractionResult.PASS;
      }
      if (level.getBlockEntity(pos) instanceof MagicBoxBlockEntity box) {
         if (level.isClientSide) {
            return InteractionResult.SUCCESS;
         } else if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.openMenu(box, buf -> buf.writeBlockPos(pos));
            return InteractionResult.CONSUME;
         }
      }
      return InteractionResult.PASS;
   }

   @Override
   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      if (state.getBlock() != newState.getBlock()) {
         if (!level.isClientSide && !Config.removeBlockMagicBoxFromGame) {
         }

         super.onRemove(state, level, pos, newState, isMoving);
      }
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new MagicBoxBlockEntity(pos, state);
   }

   @Override
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }
}
