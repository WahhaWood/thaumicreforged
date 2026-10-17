package thaumcraft.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ClickBehavior {
   protected Level level;
   protected BlockPos blockPos;
   protected BlockState state;
   private final boolean hasVisCost;

   public ClickBehavior() {
      this(false);
   }

   public ClickBehavior(boolean hasVisCost) {
      this.hasVisCost = hasVisCost;
   }

   public void init(Level level, BlockPos pos, BlockState state) {
      this.level = level;
      this.blockPos = pos;
      this.state = state;
   }

   public boolean hasVisCost() {
      return this.hasVisCost;
   }

   public int getComparatorOutput() {
      return 0;
   }

   public abstract boolean isValidForBlock();

   public void addInstability(int amount) {
   }
}
