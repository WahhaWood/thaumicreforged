package thaumcraft.block.node_stabilizer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.ITickable;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;

public class NodeStabilizerBlockEntity extends ThaumcraftBlockEntity implements ITickable {
   public int count = 0;
   public int lock;

   public NodeStabilizerBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.NODE_STABILIZER.get(), blockPos, blockState);
      this.lock = blockState.is((Block)ModBlocks.NODE_STABILIZER.get()) ? 1 : 2;
   }

   @Override
   public void clientTick(Level level, BlockPos pos, BlockState state) {
      BlockPos nodePos = this.worldPosition.relative(state.getValue(NodeStabilizerBlock.FACING));
      if (NodeStabilizerBlock.isNode(this.level.getBlockState(nodePos)) && !this.level.hasNeighborSignal(this.worldPosition)) {
         if (this.count < 37) {
            this.count++;
         }
      } else if (this.count > 0) {
         this.count--;
      }
   }
}
