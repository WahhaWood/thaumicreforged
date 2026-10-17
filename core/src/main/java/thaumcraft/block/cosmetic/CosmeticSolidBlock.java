package thaumcraft.block.cosmetic;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class CosmeticSolidBlock extends Block {
   public CosmeticSolidBlock(Properties properties, boolean allowSpawning) {
      super(properties.isValidSpawn((state, level, pos, type) -> allowSpawning).strength(4.0F, 20.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());
   }
}
