package thaumcraft.block.lifter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.block.TickableEntityBlock;

public class LifterBlock extends TickableEntityBlock {
   public LifterBlock() {
      super(Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2.5F, 15.0F));
   }

   @OnlyIn(Dist.CLIENT)
   public void animateTick(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull RandomSource random) {
      if (level.getBlockEntity(blockPos) instanceof LifterBlockEntity lifter && !lifter.gettingPower() && lifter.rangeAbove > 0) {
         Thaumcraft.setup
            .sparkle(
               blockPos.getX() + 0.2F + random.nextFloat() * 0.6F, blockPos.getY() + 1, blockPos.getZ() + 0.2F + random.nextFloat() * 0.6F, 1.0F, 3, -0.3F
            );
      }
   }

   public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
      return direction != null && direction.compareTo(Direction.UP) > 0;
   }

   public void onPlace(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState1, boolean b) {
      super.onPlace(blockState, level, blockPos, blockState1, b);
   }

   @Override
   public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, boolean b) {
      super.onRemove(state, level, blockPos, blockState, b);
   }

   public void neighborChanged(
      @NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Block block, @NotNull BlockPos blockPos1, boolean b
   ) {
   }

   private void updateLifterStack(Level level, BlockPos blockPos) {
      for (int i = 1; level.getBlockEntity(blockPos.below(i)) instanceof LifterBlockEntity lifter; i++) {
         lifter.requiresUpdate = true;
      }
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new LifterBlockEntity(blockPos, blockState);
   }
}
