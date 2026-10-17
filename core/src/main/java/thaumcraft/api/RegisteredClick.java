package thaumcraft.api;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.block.infusion_matrix.RunicMatrixBlockEntity;
import thaumcraft.init.registry.ModBlocks;

public class RegisteredClick {
   private static final List<ClickBehavior> clawBehaviors = new ArrayList<>();

   public static void init() {
      registerDefaultClawBehaviors();
   }

   private static void registerDefaultClawBehaviors() {
      registerClawClickBehavior(new ClickBehavior() {
         @Override
         public boolean isValidForBlock() {
            return this.state.is(Blocks.BOOKSHELF) || this.state.is(Blocks.CAULDRON);
         }
      });
      registerClawClickBehavior(new ClickBehavior(true) {
         private RunicMatrixBlockEntity matrix;

         @Override
         public boolean isValidForBlock() {
            if (this.state.is((Block)ModBlocks.RUNIC_MATRIX.get()) && this.level.getBlockEntity(this.blockPos) instanceof RunicMatrixBlockEntity tile) {
               this.matrix = tile;
               return true;
            } else {
               return false;
            }
         }

         @Override
         public void addInstability(int instability) {
            if (this.matrix != null) {
               this.matrix.setInstability(this.matrix.getInstability() + instability);
            }
         }

         @Override
         public int getComparatorOutput() {
            return this.matrix != null && this.matrix.isCrafting() ? 15 : 0;
         }
      });
   }

   public static void registerClawClickBehavior(ClickBehavior behavior) {
      clawBehaviors.add(behavior);
   }

   public static ClickBehavior getClawClickBehavior(Level level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      Block block = state.getBlock();

      for (ClickBehavior behavior : clawBehaviors) {
         behavior.init(level, pos, state);
         if (behavior.isValidForBlock()) {
            return behavior;
         }
      }

      return null;
   }
}
