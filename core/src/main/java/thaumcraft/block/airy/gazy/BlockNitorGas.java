package thaumcraft.block.airy.gazy;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import thaumcraft.Thaumcraft;

public class BlockNitorGas extends BlockGas {
   public static final BooleanProperty STRONG = BooleanProperty.create("strong");

   public BlockNitorGas() {
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(STRONG, false)).setValue(LEVEL, 0));
   }

   public int tickRate(Level level) {
      return level.dimension() == Level.NETHER ? 60 : 20;
   }


   public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
      return state.getValue(STRONG) ? 15 : 12;
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{STRONG});
   }

   public BlockState stateForBrightNitor() {
      return (BlockState)((BlockState)this.defaultBlockState().setValue(STRONG, false)).setValue(LEVEL, 0);
   }
}
