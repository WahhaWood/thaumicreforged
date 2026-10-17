package thaumcraft.block.cosmetic;

import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class DarkQuartzPillarBlock extends RotatedPillarBlock {
   public DarkQuartzPillarBlock() {
      super(Properties.of().strength(0.8F, 10.0F).sound(SoundType.STONE));
   }
}
