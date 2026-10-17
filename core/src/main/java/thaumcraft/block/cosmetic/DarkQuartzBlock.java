package thaumcraft.block.cosmetic;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class DarkQuartzBlock extends Block {
   public DarkQuartzBlock() {
      super(Properties.of().strength(0.8F, 10.0F).sound(SoundType.STONE));
   }
}
