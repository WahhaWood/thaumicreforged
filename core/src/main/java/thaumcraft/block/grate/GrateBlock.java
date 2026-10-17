package thaumcraft.block.grate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.ThaumcraftEntityBlock;
import thaumcraft.init.registry.ModBlocks;

public class GrateBlock extends ThaumcraftEntityBlock {
   private static final VoxelShape INSIDE = box(0.0, 13.0, 0.0, 16.0, 16.0, 16.0);
   private static final BooleanProperty CLOSE = BooleanProperty.create("close");

   public GrateBlock() {
      super(Properties.of().sound(SoundType.METAL).strength(3.0F, 17.0F));
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(CLOSE, false));
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{CLOSE});
   }

   @NotNull
   @Override
   public VoxelShape getShape(
      @NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext collisionContext
   ) {
      return INSIDE;
   }

   @NotNull
   public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
      return context instanceof EntityCollisionContext cc && cc.getEntity() instanceof ItemEntity && !isClose(state)
         ? Shapes.empty()
         : super.getCollisionShape(state, getter, blockPos, context);
   }

   @Nullable
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new GrateBlockEntity(blockPos, blockState);
   }

   public static boolean isClose(BlockState blockState) {
      return blockState.is((Block)ModBlocks.GRATE.get()) && (Boolean)blockState.getValue(CLOSE);
   }
}
