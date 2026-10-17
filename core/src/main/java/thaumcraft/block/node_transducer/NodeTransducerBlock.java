package thaumcraft.block.node_transducer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;

public class NodeTransducerBlock extends TickableEntityBlock {
   private static final VoxelShape INSIDE = box(0.0, 4.0, 0.0, 16.0, 16.0, 16.0);

   public NodeTransducerBlock() {
      super(Properties.of().forceSolidOn().noOcclusion().sound(SoundType.STONE).strength(3.0F, 25.0F));
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
      return INSIDE;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new NodeTransducerBlockEntity(blockPos, blockState);
   }

   @Override
   public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean movedByPiston) {
      super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
      if (!level.isClientSide && level.getBlockEntity(pos) instanceof NodeTransducerBlockEntity transducer) {
         transducer.checkStatus(level, pos);
      }
   }
}
