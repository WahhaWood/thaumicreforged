package thaumicenergistics.block.arcane_assembler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public final class ArcaneAssemblerBlockEntityFactory {
   private ArcaneAssemblerBlockEntityFactory() {
   }

   @NotNull
   public static ArcaneAssemblerBlockEntity create(BlockPos pos, BlockState state) {
      if (state.getBlock() instanceof AdvancedArcaneAssemblerBlock advancedBlock) {
         return (ArcaneAssemblerBlockEntity)(switch (advancedBlock.getTier()) {
            case 1 -> new ArcaneAssemblerTier1BlockEntity(pos, state);
            case 2 -> new ArcaneAssemblerTier2BlockEntity(pos, state);
            default -> new ArcaneAssemblerTier3BlockEntity(pos, state);
         });
      } else {
         return new ArcaneAssemblerBlockEntity(pos, state);
      }
   }
}
