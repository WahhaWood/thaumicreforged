package thaumcraft.block.fluid;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

public abstract class AbstractLiquidBlock extends LiquidBlock {
   public static final int MIN_FINITE_AMOUNT = 1;
   public static final int MAX_FINITE_AMOUNT = 7;

   protected AbstractLiquidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties) {
      super(fluid.get(), properties);
   }

   protected int getFluidLevel(Level level, BlockPos pos) {
      return level.getFluidState(pos).getAmount();
   }

   protected BlockState createFiniteBlockState(int amount) {
      return createFiniteBlockState(this.fluid, amount);
   }

   public static BlockState createFiniteBlockState(FlowingFluid fluid, int amount) {
      return fluid.getFlowing(Mth.clamp(amount, 1, 7), false).createLegacyBlock();
   }

   public static BlockState createMinFiniteBlockState(FlowingFluid fluid) {
      return createFiniteBlockState(fluid, 1);
   }

   public static BlockState createMaxFiniteBlockState(FlowingFluid fluid) {
      return createFiniteBlockState(fluid, 7);
   }

   public static int getFiniteAmount(FluidState fluidState) {
      return fluidState.isEmpty() ? 0 : Mth.clamp(fluidState.getAmount(), 1, 7);
   }

   public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
      return true;
   }
}
