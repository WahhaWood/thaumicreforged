package thaumcraft.item.sapling;

import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class ThaumcraftSaplingBlock extends SaplingBlock {
   private final int growChance;

   public ThaumcraftSaplingBlock(TreeGrower grower, int growChance, Properties props) {
      super(grower, props);
      this.growChance = Math.max(1, growChance);
   }
}
