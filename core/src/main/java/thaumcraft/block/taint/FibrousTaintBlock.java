package thaumcraft.block.taint;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import thaumcraft.init.registry.ModBiomes;

public class FibrousTaintBlock extends MultifaceBlock {
   public static final MapCodec<FibrousTaintBlock> CODEC = simpleCodec(props -> new FibrousTaintBlock());
   private static final int BIOME_SPREAD_RATE = 200;
   private final MultifaceSpreader spreader = new MultifaceSpreader(this);

   public FibrousTaintBlock() {
      super(Properties.of().strength(1.0F, 5.0F).noOcclusion().noCollission().randomTicks());
   }

   protected MapCodec<? extends MultifaceBlock> codec() {
      return CODEC;
   }

   public MultifaceSpreader getSpreader() {
      return this.spreader;
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      super.neighborChanged(state, level, pos, block, fromPos, isMoving);
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      return super.canSurvive(state, level, pos) && level.getBiome(pos).is(ModBiomes.TAINT);
   }

   @Override
   public void randomTick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
      if (!level.getBiome(pos).is(ModBiomes.TAINT)) {
         level.removeBlock(pos, false);
         return;
      }

      if (random.nextInt(BIOME_SPREAD_RATE) == 0 && thaumcraft.world.biomes.TaintHelper.countAdjacentTaint(level, pos) >= 2) {
         int bx = pos.getX() + random.nextInt(3) - 1;
         int bz = pos.getZ() + random.nextInt(3) - 1;
         BlockPos bpos = new BlockPos(bx, pos.getY(), bz);
         if (!level.getBiome(bpos).is(ModBiomes.TAINT)) {
            thaumcraft.util.Utils.setBiomeAt(level, bpos, ModBiomes.TAINT);
         }
      }

      this.spreader.spreadFromRandomFaceTowardRandomDirection(state, level, pos, random);

      int adjTaint = thaumcraft.world.biomes.TaintHelper.countAdjacentTaint(level, pos);
      if (adjTaint >= 2) {
         for (Direction d : Direction.values()) {
            BlockPos p = pos.relative(d);
            BlockState s = level.getBlockState(p);
            if (s.is(net.minecraft.tags.BlockTags.LOGS)) {
               level.setBlock(p, ((Block)thaumcraft.init.registry.ModBlocks.TAINT_CRUST.get()).defaultBlockState(), 3);
            } else if (s.is(net.minecraft.tags.BlockTags.DIRT) || s.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)) {
               level.setBlock(p, ((Block)thaumcraft.init.registry.ModBlocks.TAINT_SOIL.get()).defaultBlockState(), 3);
            }
         }
      }
   }

   @Override
   public void entityInside(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.Entity entity) {
      if (!level.isClientSide && entity instanceof net.minecraft.world.entity.LivingEntity living && !living.isInvulnerable()) {
         if (entity instanceof net.minecraft.world.entity.player.Player) {
            if (level.random.nextInt(1000) == 0) {
               living.addEffect(new net.minecraft.world.effect.MobEffectInstance(thaumcraft.init.registry.ModMobEffect.FLUX_TAINT, 80, 0, false, false));
            }
         } else if (level.random.nextInt(500) == 0) {
            living.addEffect(new net.minecraft.world.effect.MobEffectInstance(thaumcraft.init.registry.ModMobEffect.FLUX_TAINT, 160, 0, false, false));
         }
      }
      super.entityInside(state, level, pos, entity);
   }

   public static boolean hasAnyFace(BlockState state) {
      for (Direction d : Direction.values()) {
         BooleanProperty p = propertyFor(d);
         if (state.hasProperty(p) && (Boolean)state.getValue(p)) {
            return true;
         }
      }

      return false;
   }

   private static BooleanProperty propertyFor(Direction d) {
      return switch (d) {
         case NORTH -> BlockStateProperties.NORTH;
         case SOUTH -> BlockStateProperties.SOUTH;
         case WEST -> BlockStateProperties.WEST;
         case EAST -> BlockStateProperties.EAST;
         case UP -> BlockStateProperties.UP;
         case DOWN -> BlockStateProperties.DOWN;
         default -> throw new MatchException(null, null);
      };
   }

   private static boolean isAdjacentToSolidBlock(LevelAccessor level, BlockPos pos) {
      for (Direction d : Direction.values()) {
         BlockPos p = pos.relative(d);
         if (level.getBlockState(p).isFaceSturdy(level, p, d.getOpposite())) {
            return true;
         }
      }

      return false;
   }
}
