package thaumcraft.block.fluid;

import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.FlowingFluid;
import thaumcraft.ClientSetup;
import thaumcraft.Thaumcraft;
import thaumcraft.particles.fx.particles.FXBubble;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class BlockFluxGoo extends AbstractLiquidBlock {
   public static final ResourceLocation VIS_EXHAUST_RL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "vis_exhaust");
   public static final ResourceLocation SOUND_GORE_RL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "gore");
   public static final ResourceLocation TAINT_FIBRES_RL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "taint_fibres");

   public BlockFluxGoo(Supplier<? extends FlowingFluid> fluid, Properties properties) {
      super(fluid, properties);
   }

   public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
   }

   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      int amount = getFiniteAmount(level.getFluidState(pos));
      int intensity = Math.max(0, amount - 1);
      if (random.nextInt(50 - Thaumcraft.setup.particleCount(10)) <= intensity) {
         float surfaceY = amount / 8.0F;
         FXBubble fx = new FXBubble(
            (ClientLevel)level, pos.getX() + random.nextFloat(), pos.getY() + surfaceY, pos.getZ() + random.nextFloat(), 0.0, 0.0, 0.0, 0
         );
         ClientSetup.add(fx);
      }
   }
}
