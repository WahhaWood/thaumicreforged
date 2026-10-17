package thaumcraft.block.lamp.growth;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.block.lamp.ArcaneLampBlock;

public class ArcaneLampGrowthBlock extends ArcaneLampBlock {
   public ArcaneLampGrowthBlock() {
      super(8);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new ArcaneLampGrowthBlockEntity(blockPos, blockState);
   }
}
