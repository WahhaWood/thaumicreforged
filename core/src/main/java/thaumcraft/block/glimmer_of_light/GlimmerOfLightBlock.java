package thaumcraft.block.glimmer_of_light;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;

public class GlimmerOfLightBlock extends TickableEntityBlock {
   private static final VoxelShape EMPTY = Shapes.empty();

   public GlimmerOfLightBlock() {
      super(
         Properties.of()
            .air()
            .replaceable()
            .noCollission()
            .noOcclusion()
            .noLootTable()
            .sound(SoundType.WOOL)
            .strength(-1.0F, 3600000.0F)
            .lightLevel(state -> 15)
      );
   }

   @NotNull
   @Override
   public RenderShape getRenderShape(@NotNull BlockState state) {
      return RenderShape.INVISIBLE;
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return EMPTY;
   }

   @NotNull
   public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return EMPTY;
   }

   @NotNull
   public VoxelShape getVisualShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return EMPTY;
   }

   public boolean canBeReplaced(@NotNull BlockState state, @NotNull BlockPlaceContext context) {
      return true;
   }

   @NotNull
   public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull Builder builder) {
      return Collections.emptyList();
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new LightSourceTile(pos, state);
   }

   protected boolean isAir(@NotNull BlockState state) {
      return true;
   }

   public boolean isPathfindable(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull PathComputationType type) {
      return true;
   }

   public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource rand) {
      if (rand.nextInt(500) == 0) {
         double x = pos.getX() + 0.5 + (rand.nextDouble() - rand.nextDouble()) / 3.0;
         double y = pos.getY() + 0.5 + (rand.nextDouble() - rand.nextDouble()) / 3.0;
         double z = pos.getZ() + 0.5 + (rand.nextDouble() - rand.nextDouble()) / 3.0;
         double mx = (rand.nextDouble() - rand.nextDouble()) * 0.01;
         double my = 0.01 + rand.nextDouble() * 0.01;
         double mz = (rand.nextDouble() - rand.nextDouble()) * 0.01;
         level.addParticle(ParticleTypes.END_ROD, x, y, z, mx, my, mz);
      }
   }
}
