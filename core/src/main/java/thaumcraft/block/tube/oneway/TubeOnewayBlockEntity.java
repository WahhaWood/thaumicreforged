package thaumcraft.block.tube.oneway;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.block.tube.TubeBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class TubeOnewayBlockEntity extends TubeBlockEntity {
   public TubeOnewayBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.TUBE_ONEWAY.get(), pos, state);
   }

   /** TC4 TileTubeOneway: suction only from the side behind its facing... */
   @Override
   protected void calculateSuction(@Nullable Aspect filter, boolean restrict, boolean directional) {
      super.calculateSuction(filter, restrict, true);
   }

   /** ...and essentia from every other side, so it flows one way only. */
   @Override
   protected void equalizeWithNeighbours(Level level, BlockPos pos, boolean directional) {
      super.equalizeWithNeighbours(level, pos, true);
   }
}
