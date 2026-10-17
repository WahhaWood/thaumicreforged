package thaumcraft.block.fluid;

import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.FlowingFluid;
import thaumcraft.ClientSetup;
import thaumcraft.particles.fx.particles.FXSlimyBubble;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class BlockFluidDeath extends AbstractLiquidBlock {
   public BlockFluidDeath(Supplier<? extends FlowingFluid> source, Properties properties) {
      super(source, properties);
   }

   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      int levelAmount = this.getFluidLevel(level, pos);
      float surfaceY = levelAmount / 8.0F;
      FXSlimyBubble fx = new FXSlimyBubble(
         (ClientLevel)level, pos.getX() + random.nextFloat(), pos.getY() + surfaceY, pos.getZ() + random.nextFloat(), 0.075F + random.nextFloat() * 0.075F
      );
      fx.setAlpha(0.8F);
      fx.setColor(0.3F - random.nextFloat() * 0.1F, 0.0F, 0.4F + random.nextFloat() * 0.1F);
      ClientSetup.add(fx);
      if (random.nextInt(50) == 0) {
         double sx = pos.getX() + random.nextFloat();
         double sy = pos.getY() + 1.0;
         double sz = pos.getZ() + random.nextFloat();
         level.playLocalSound(sx, sy, sz, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.1F + random.nextFloat() * 0.1F, 0.9F + random.nextFloat() * 0.15F, false);
      }

      super.animateTick(state, level, pos, random);
   }
}
