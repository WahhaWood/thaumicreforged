package thaumicenergistics.block.arcane_assembler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class AdvancedArcaneAssemblerBlock extends ArcaneAssemblerBlock {
   private final int tier;

   public AdvancedArcaneAssemblerBlock(int tier) {
      this.tier = Math.max(1, Math.min(3, tier));
   }

   public int getTier() {
      return this.tier;
   }

   @NotNull
   @Override
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return ArcaneAssemblerBlockEntityFactory.create(pos, state);
   }
}
