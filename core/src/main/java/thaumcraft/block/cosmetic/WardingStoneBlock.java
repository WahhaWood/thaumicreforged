package thaumcraft.block.cosmetic;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.AABB;
import thaumcraft.Thaumcraft;
import thaumcraft.api.block.TickableEntityBlock;

public class WardingStoneBlock extends TickableEntityBlock {
   public WardingStoneBlock() {
      super(Properties.of().strength(2.0F, 10.0F).sound(SoundType.STONE).randomTicks().isValidSpawn((state, level, pos, type) -> false));
   }

   @Override
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new WardingStoneBlockEntity(pos, state);
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (level.isClientSide) {
         if (level.hasNeighborSignal(pos)) {
            for (int a = 0; a < Thaumcraft.setup.particleCount(2); a++) {
               Thaumcraft.setup
                  .blockRunes(
                     level,
                     pos.getX(),
                     pos.getY() + 0.7F,
                     pos.getZ(),
                     0.2F + level.random.nextFloat() * 0.4F,
                     level.random.nextFloat() * 0.3F,
                     0.8F + level.random.nextFloat() * 0.2F,
                     20,
                     -0.02F
                  );
            }
         } else {
            boolean blocked = !level.getBlockState(pos.above()).isAir() && level.getBlockState(pos.above()).blocksMotion()
               || !level.getBlockState(pos.above(2)).isAir() && level.getBlockState(pos.above(2)).blocksMotion();
            if (!blocked) {
               AABB box = new AABB(pos).inflate(1.0, 1.0, 1.0);
               List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, box, ex -> !(ex instanceof Player));
               if (!list.isEmpty()) {
                  LivingEntity e = list.get(0);
                  Thaumcraft.setup
                     .blockRunes(
                        level,
                        pos.getX(),
                        pos.getY() + 0.6F + level.random.nextFloat() * Math.max(0.8F, e.getEyeHeight()),
                        pos.getZ(),
                        0.6F + level.random.nextFloat() * 0.4F,
                        0.0F,
                        0.3F + level.random.nextFloat() * 0.7F,
                        20,
                        0.0F
                     );
               }
            } else {
               for (int a = 0; a < Thaumcraft.setup.particleCount(3); a++) {
                  Thaumcraft.setup
                     .blockRunes(
                        level,
                        pos.getX(),
                        pos.getY() + 0.7F,
                        pos.getZ(),
                        0.9F + level.random.nextFloat() * 0.1F,
                        level.random.nextFloat() * 0.3F,
                        level.random.nextFloat() * 0.3F,
                        24,
                        -0.02F
                     );
               }
            }
         }
      }
   }
}
