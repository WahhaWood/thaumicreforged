package thaumcraft.block.airy.static_field;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.NotNull;
import thaumcraft.block.airy.AbstractAiryInvisibleBlock;

public class StaticFieldBlock extends AbstractAiryInvisibleBlock {
   public StaticFieldBlock() {
      super(Properties.of().sound(SoundType.WOOL).noCollission().replaceable().air().lightLevel(s -> 8).strength(100.0F, 50.0F).randomTicks());
   }

   public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource rand) {
      level.removeBlock(pos, false);
   }

   public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource rand) {
   }
}
