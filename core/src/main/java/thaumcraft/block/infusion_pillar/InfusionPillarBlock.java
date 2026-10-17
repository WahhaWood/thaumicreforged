package thaumcraft.block.infusion_pillar;

import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.ThaumcraftEntityBlock;
import thaumcraft.block.infusion_matrix.RunicMatrixBlockEntity;

public class InfusionPillarBlock extends ThaumcraftEntityBlock {
   public static final BooleanProperty TOP = BooleanProperty.create("top");
   public static final IntegerProperty UPGRADE_TIER = IntegerProperty.create("upgrade_tier", 0, 4);

   public InfusionPillarBlock() {
      super(Properties.of().forceSolidOn().sound(SoundType.STONE).strength(3.0F, 25.0F));
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(TOP, false)).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH))
            .setValue(UPGRADE_TIER, 0)
      );
   }

   @NotNull
   @Override
   public RenderShape getRenderShape(@NotNull BlockState blockState) {
      return blockState.getValue(TOP) ? RenderShape.INVISIBLE : RenderShape.MODEL;
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{TOP, BlockStateProperties.HORIZONTAL_FACING, UPGRADE_TIER});
   }

   @Nullable
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return blockState.getValue(TOP) ? null : new InfusionPillarBlockEntity(blockPos, blockState);
   }

   /**
    * Разрушение колонны ломает весь алтарь: матрица гаснет, а остальные колонны
    * и постройка возвращаются к обычным блокам.
    *
    * <p>В TC4 такого нет — при сломанной колонне просто срывается крафт, а блоки
    * остаются. Здесь поведение запрошено пользователем, поэтому это расширение,
    * а не паритет.
    *
    * <p>Хук на {@code onRemove}, а не на {@code playerWillDestroy}: первый
    * срабатывает и от взрывов, и от поршней, и его же зовёт тестовый
    * {@code /break} (через {@code destroyBlock}). Повторный вход во время самого
    * возврата блокируется флагом {@code REVERTING}.
    */
   @Override
   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
      if (!level.isClientSide && !state.is(newState.getBlock()) && !RunicMatrixBlockEntity.isReverting()) {
         BlockPos matrixPos = RunicMatrixBlockEntity.findMatrixNear(level, pos);
         if (matrixPos != null) {
            RunicMatrixBlockEntity.revertAltar(level, matrixPos);
         }
      }

      super.onRemove(state, level, pos, newState, movedByPiston);
   }

}
