package thaumcraft.block.airy.gazy;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.Thaumcraft;

public class BlockGaseousLight extends BlockGas {
   public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
      return 13;
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (random.nextFloat() < 0.0075F) {
         Thaumcraft.setup.sparkle(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 1.0F, 1, random.nextFloat() / 2.0F);
      }
   }

   @Override
   public void placeParticle(Level world, int x, int y, int z) {
      Thaumcraft.setup.sparkle(x + 0.5F, y + 0.5F, z + 0.5F, 1);
   }
}
