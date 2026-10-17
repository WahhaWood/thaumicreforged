package thaumcraft.block.opaque;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;

public class AmberBricksBlock extends Block {
   public AmberBricksBlock() {
      super(
         Properties.of()
            .mapColor(MapColor.COLOR_ORANGE)
            .strength(1.5F, 5.0F)
            .sound(SoundType.STONE)
            .noOcclusion()
            .isSuffocating((s, l, p) -> false)
            .isViewBlocking((s, l, p) -> false)
            .lightLevel(value -> 3)
      );
   }
}
