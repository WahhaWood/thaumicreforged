package thaumcraft.entities.smart.golem.render.screen;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;

public class SlotGhostFluid extends SlotGhost {
   public SlotGhostFluid(Container container, int index, int x, int y) {
      super(container, index, x, y, 1);
   }

   @Override
   public boolean accept(@NotNull ItemStack stack) {
      return FluidUtil.getFluidContained(stack).isPresent();
   }
}
