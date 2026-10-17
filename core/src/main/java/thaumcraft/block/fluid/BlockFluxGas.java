package thaumcraft.block.fluid;

import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.FlowingFluid;

public class BlockFluxGas extends AbstractLiquidBlock {
   public static final ResourceLocation VIS_EXHAUST_RL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "vis_exhaust");

   public BlockFluxGas(Supplier<? extends FlowingFluid> fluid, Properties properties) {
      super(fluid, properties);
   }
}
