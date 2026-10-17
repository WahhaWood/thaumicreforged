package thaumcraft.block.eldritch.doorway;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;

public class AncientDoorwayBlock extends Block {
   public AncientDoorwayBlock() {
      super(
         Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .strength(-1.0F, 3600000.0F)
            .sound(SoundType.STONE)
            .lightLevel(state -> 12)
            .noOcclusion()
            .isValidSpawn((state, level, pos, entityType) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)
            .noLootTable()
      );
   }
}
