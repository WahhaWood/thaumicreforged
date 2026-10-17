package thaumcraft.block.mobilizer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;

public class MobilizerBlock extends TickableEntityBlock {
   public MobilizerBlock() {
      super(Properties.of().strength(3.0F, 10.0F).sound(SoundType.METAL));
   }

   @Override
   public void onRemove(@NotNull BlockState oldState, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean moving) {
      if (!oldState.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MobilizerBlockEntity mobilizer) {
         mobilizer.setDead(true);
      }

      super.onRemove(oldState, level, pos, newState, moving);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new MobilizerBlockEntity(pos, state);
   }
}
