package thaumcraft.block.fluid;

import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.FlowingFluid;
import thaumcraft.ClientSetup;
import thaumcraft.particles.fx.particles.FXBubble;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class BlockFluidPure extends AbstractLiquidBlock {
   public static final ResourceLocation WARP_WARD_RL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "warp_ward");

   public BlockFluidPure(Supplier<? extends FlowingFluid> fluid, Properties properties) {
      super(fluid, properties);
   }

   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
      int amount = level.getFluidState(pos).getAmount();
      float surfaceY = amount / 8.0F;
      FXBubble fb = new FXBubble((ClientLevel)level, pos.getX() + rand.nextFloat(), pos.getY() + surfaceY, pos.getZ() + rand.nextFloat(), 0.0, 0.0, 0.0, 0);
      fb.setRGB(1.0F, 1.0F, 1.0F);
      ClientSetup.add(fb);
      if (rand.nextInt(25) == 0) {
         double sx = pos.getX() + rand.nextFloat();
         double sy = pos.getY() + surfaceY;
         double sz = pos.getZ() + rand.nextFloat();
         level.playLocalSound(sx, sy, sz, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.1F + rand.nextFloat() * 0.1F, 0.9F + rand.nextFloat() * 0.15F, false);
      }
   }
}
