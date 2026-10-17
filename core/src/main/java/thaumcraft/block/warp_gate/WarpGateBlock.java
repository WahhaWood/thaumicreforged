package thaumcraft.block.warp_gate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;

public class WarpGateBlock extends TickableEntityBlock {
   public WarpGateBlock() {
      super(Properties.of().mapColor(MapColor.STONE).strength(5.0F, 2000.0F).sound(SoundType.STONE).noOcclusion());
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new WarpGateBlockEntity(pos, state);
   }
}
