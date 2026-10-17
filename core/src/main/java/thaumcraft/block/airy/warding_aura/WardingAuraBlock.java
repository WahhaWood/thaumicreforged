package thaumcraft.block.airy.warding_aura;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import thaumcraft.block.airy.AbstractAiryInvisibleBlock;

public class WardingAuraBlock extends AbstractAiryInvisibleBlock {
   private static final TagKey<Block> BASE_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("thaumcraft", "warding_aura_base"));
   private static final VoxelShape FULL = Shapes.block();

   public WardingAuraBlock() {
      super(Properties.of().sound(SoundType.WOOL).noCollission().replaceable());
   }

   private static BlockPos getBasePos(Level level, BlockPos pos) {
      BlockPos below = pos.below();
      return level.getBlockState(below).is(BASE_TAG) ? below : below.below();
   }

   private static boolean isActiveBarrier(Level level, BlockPos pos) {
      BlockPos base = getBasePos(level, pos);
      return !level.hasNeighborSignal(base);
   }

   @NotNull
   @Override
   public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
      if (level instanceof Level lvl) {
         if (ctx instanceof EntityCollisionContext ecc) {
            Entity e = ecc.getEntity();
            if (e instanceof LivingEntity && !(e instanceof Player)) {
               return isActiveBarrier(lvl, pos) ? FULL : EMPTY;
            } else {
               return EMPTY;
            }
         } else {
            return EMPTY;
         }
      } else {
         return EMPTY;
      }
   }

   public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource rand) {
   }
}
