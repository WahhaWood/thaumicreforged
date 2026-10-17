package thaumcraft.block.eldritch;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;

public class EndritchBlock extends Block {
   public EndritchBlock() {
      super(
         Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .sound(SoundType.STONE)
            .strength(2.0F, 30.0F)
            .requiresCorrectToolForDrops()
            .lightLevel(state -> 12)
            .isValidSpawn((state, level, pos, type) -> false)
      );
   }
}
