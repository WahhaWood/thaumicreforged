package thaumcraft.block.tube.restrict;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.block.tube.TubeBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class TileTubeRestrict extends TubeBlockEntity {
   public TileTubeRestrict(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.TUBE_RESTRICT.get(), blockPos, blockState);
   }

   /** TC4 TileTubeRestrict: passes on half the suction instead of one less. */
   @Override
   protected void calculateSuction(@Nullable Aspect filter, boolean restrict, boolean directional) {
      super.calculateSuction(filter, true, directional);
   }
}
