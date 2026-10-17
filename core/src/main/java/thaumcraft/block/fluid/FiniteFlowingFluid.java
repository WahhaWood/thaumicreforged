package thaumcraft.block.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties;

public final class FiniteFlowingFluid {
   private FiniteFlowingFluid() {
   }

   public static class Flowing extends net.neoforged.neoforge.fluids.BaseFlowingFluid.Flowing {
      public Flowing(Properties properties) {
         super(properties);
      }

      protected void spread(Level level, BlockPos pos, FluidState fluidState) {
      }

      protected FluidState getNewLiquid(Level level, BlockPos pos, BlockState blockState) {
         FluidState current = level.getFluidState(pos);
         FluidState target = super.getNewLiquid(level, pos, blockState);
         int currentAmount = current.getAmount();
         if (target.isEmpty()) {
            return currentAmount > 1 ? this.getFlowing(currentAmount - 1, (Boolean)current.getValue(FALLING)) : current;
         } else {
            int targetAmount = target.getAmount();
            return targetAmount < currentAmount ? this.getFlowing(currentAmount - 1, (Boolean)target.getValue(FALLING)) : target;
         }
      }
   }

   public static class Source extends net.neoforged.neoforge.fluids.BaseFlowingFluid.Source {
      public Source(Properties properties) {
         super(properties);
      }

      protected void spread(Level level, BlockPos pos, FluidState fluidState) {
      }
   }
}
