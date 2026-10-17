package thaumcraft.block.lamp.fertility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.block.lamp.ArcaneLampBlock;

public class ArcaneLampFertilityBlock extends ArcaneLampBlock {
   public ArcaneLampFertilityBlock() {
      super(8);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new ArcaneLampFertilityBlockEntity(blockPos, blockState);
   }
}
