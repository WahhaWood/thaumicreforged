package thaumcraft.block.transvector;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;

public class TransvectorInterfaceBlock extends TickableEntityBlock {
   public TransvectorInterfaceBlock() {
      super(Properties.of().strength(3.0F, 10.0F).sound(SoundType.METAL));
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new TransvectorInterfaceBlockEntity(pos, state);
   }
}
