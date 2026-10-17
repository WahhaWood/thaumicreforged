package thaumcraft.block.tube.centrifuge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;

public class CentrifugeBlock extends TickableEntityBlock {
   private static final VoxelShape INSIDE = box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

   public CentrifugeBlock() {
      super(Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(0.5F, 5.0F));
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new CentrifugeBlockEntity(blockPos, blockState);
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
      return INSIDE;
   }
}
