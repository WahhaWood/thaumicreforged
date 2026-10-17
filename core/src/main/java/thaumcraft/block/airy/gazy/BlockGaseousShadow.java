package thaumcraft.block.airy.gazy;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.Thaumcraft;

public class BlockGaseousShadow extends BlockGas {
   public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
      return 215;
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (random.nextFloat() < 0.0075F) {
         Thaumcraft.setup.wispFX2(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.125F, 5, true, true, -0.02F);
      }
   }

   @Override
   public void placeParticle(Level world, int x, int y, int z) {
      Thaumcraft.setup.wispFX2(world, x + 0.5, y + 0.5, z + 0.5, 0.125F, 5, true, true, -0.02F);
   }
}
