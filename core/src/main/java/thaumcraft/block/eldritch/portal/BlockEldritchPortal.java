package thaumcraft.block.eldritch.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.block.eldritch.BlockEldritchEntityBase;

public class BlockEldritchPortal extends BlockEldritchEntityBase {
   private static final VoxelShape SHAPE = Shapes.empty();

   public BlockEldritchPortal() {
      super(Properties.of().noCollission().noOcclusion().strength(-1.0F, 200000.0F).lightLevel(s -> 15).noLootTable());
   }

   @Override
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.INVISIBLE;
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   @NotNull
   public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   @NotNull
   public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
      return SHAPE;
   }

   public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
      return true;
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean isMoving) {
      if (!level.isClientSide) {
      }

      super.neighborChanged(state, level, pos, neighborBlock, fromPos, isMoving);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new EldritchPortalBlockEntity(pos, state);
   }
}
