package thaumcraft.block.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class BlockTaintFibres extends Block {
   private static final boolean SPAWN_TAINT_SPORE = true;
   private final boolean mature;

   public BlockTaintFibres() {
      this(false);
   }

   public BlockTaintFibres(boolean mature) {
      super(Properties.of().strength(1.0F, 5.0F).sound(SoundType.ROOTS).noOcclusion().noCollission().randomTicks().lightLevel(s -> mature ? 10 : 0));
      this.mature = mature;
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      if (!this.canSurvive(state, level, pos)) {
         level.removeBlock(pos, false);
         return;
      }
      super.neighborChanged(state, level, pos, block, fromPos, isMoving);
   }

   public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
      return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP);
   }

   @Override
   public void randomTick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
      if (!level.getBiome(pos).is(thaumcraft.init.registry.ModBiomes.TAINT)) {
         level.removeBlock(pos, false);
         return;
      }

      if (!this.mature) {
         if (thaumcraft.init.registry.Config.spawnTaintSpore && random.nextInt(10) == 0 && level.isEmptyBlock(pos.above())) {
            level.setBlock(pos, ((Block)thaumcraft.init.registry.ModBlocks.MATURE_SPORE_STALK.get()).defaultBlockState(), 3);
            thaumcraft.entities.smart.taint.spore.EntityTaintSpore spore = new thaumcraft.entities.smart.taint.spore.EntityTaintSpore(level);
            spore.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0.0F, 0.0F);
            level.addFreshEntity(spore);
         }
      } else {
         java.util.List<thaumcraft.entities.smart.taint.spore.EntityTaintSpore> targets = level.getEntitiesOfClass(
            thaumcraft.entities.smart.taint.spore.EntityTaintSpore.class,
            new net.minecraft.world.phys.AABB(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1)
         );
         if (targets.isEmpty()) {
            level.setBlock(pos, ((Block)thaumcraft.init.registry.ModBlocks.SPORE_STALK.get()).defaultBlockState(), 3);
         }
      }
   }

   @Override
   public void entityInside(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.Entity entity) {
      if (!level.isClientSide && entity instanceof net.minecraft.world.entity.LivingEntity living && !living.isInvulnerable()) {
         if (entity instanceof net.minecraft.world.entity.player.Player) {
            if (level.random.nextInt(100) == 0) {
               living.addEffect(new net.minecraft.world.effect.MobEffectInstance(thaumcraft.init.registry.ModMobEffect.FLUX_TAINT, 80, 0, false, false));
            }
         } else if (level.random.nextInt(20) == 0) {
            living.addEffect(new net.minecraft.world.effect.MobEffectInstance(thaumcraft.init.registry.ModMobEffect.FLUX_TAINT, 160, 0, false, false));
         }
      }
      super.entityInside(state, level, pos, entity);
   }
}
