package thaumcraft.block.airy.energized_aura;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;
import thaumcraft.block.node_stabilizer.NodeStabilizerBlock;
import thaumcraft.block.node_transducer.NodeTransducerBlockEntity;
import thaumcraft.init.registry.ModBlocks;

public class EnergizedAuraNodeBlock extends TickableEntityBlock {
   private final VoxelShape shape = box(4.8, 4.8, 4.8, 11.2, 11.2, 11.2);

   public EnergizedAuraNodeBlock() {
      super(Properties.of().sound(SoundType.WOOL).strength(2.0F, 200.0F).lightLevel(value -> 8).noCollission());
   }

   @NotNull
   @Override
   public RenderShape getRenderShape(@NotNull BlockState state) {
      return RenderShape.INVISIBLE;
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
      return this.shape;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new NodeEnergizedBlockEntity(pos, state);
   }

   /** TC4 BlockAiry.onNeighborBlockChange (meta 5): losing the stabilizer or the transducer blows the node up. */
   @Override
   protected void neighborChanged(
      @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean movedByPiston
   ) {
      super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
      if (!level.isClientSide && !isHeld(level, pos)) {
         explodify(level, pos);
      }
   }

   /** An unpowered stabilizer below facing up and a node transducer above. */
   public static boolean isHeld(Level level, BlockPos pos) {
      BlockPos below = pos.below();
      BlockState stabilizer = level.getBlockState(below);
      return stabilizer.getBlock() instanceof NodeStabilizerBlock
         && stabilizer.getValue(NodeStabilizerBlock.FACING) == Direction.UP
         && !level.hasNeighborSignal(below)
         && level.getBlockEntity(pos.above()) instanceof NodeTransducerBlockEntity;
   }

   /** TC4 BlockAiry.explodify: the node vanishes in a blast that spares blocks and spills flux, goo below it and gas above. */
   public static void explodify(Level level, BlockPos pos) {
      if (level.isClientSide) {
         return;
      }

      level.removeBlock(pos, false);
      level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0F, Level.ExplosionInteraction.NONE);
      RandomSource random = level.random;
      for (int a = 0; a < 50; a++) {
         BlockPos target = pos.offset(
            random.nextInt(8) - random.nextInt(8), random.nextInt(8) - random.nextInt(8), random.nextInt(8) - random.nextInt(8)
         );
         if (level.isEmptyBlock(target)) {
            Block flux = target.getY() < pos.getY() ? ModBlocks.FLUX_GOO.get() : ModBlocks.FLUX_GAS.get();
            level.setBlock(target, flux.defaultBlockState(), 3);
         }
      }
   }
}
