package thaumcraft.block.cosmetic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class TravelStoneBlock extends CosmeticSolidBlock {
   public TravelStoneBlock() {
      super(Properties.of().strength(2.0F, 10.0F).lightLevel(s -> 9), false);
   }

   public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
      if (entity.isAlive() && entity instanceof LivingEntity living) {
         TravelStoneEffects.apply(level, pos, living);
      }

      super.stepOn(level, pos, state, entity);
   }
}
