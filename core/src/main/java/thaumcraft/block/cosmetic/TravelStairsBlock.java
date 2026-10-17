package thaumcraft.block.cosmetic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import thaumcraft.init.registry.ModBlocks;

public class TravelStairsBlock extends StairBlock {
   public TravelStairsBlock() {
      super(((Block)ModBlocks.TRAVEL_STONE.get()).defaultBlockState(), Properties.of().strength(2.0F, 10.0F).lightLevel(s -> 9));
   }

   public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
      if (entity.isAlive() && entity instanceof LivingEntity living) {
         TravelStoneEffects.apply(level, pos, living);
      }

      super.stepOn(level, pos, state, entity);
   }
}
