package thaumcraft.block.infusion_matrix;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.api.block.TickableEntityBlock;

public class RunicMatrixBlock extends TickableEntityBlock {
   public RunicMatrixBlock() {
      super(Properties.of().forceSolidOn().noOcclusion().lightLevel(value -> 10).forceSolidOn().sound(SoundType.STONE).strength(3.0F, 25.0F));
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(ACTIVE, false));
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{ACTIVE});
   }

   /**
    * Блок невидим: весь облик матрицы (кластер из 8 кубов) рисует
    * {@link RunicMatrixRenderer}. Это точная копия оригинала —
    * {@code BlockStoneDeviceRenderer} (meta 2) делегирует блок-рендер в
    * {@code TileRunicMatrixRenderer}, статической модели у блока нет ни в мире,
    * ни в инвентаре (в инвентаре предмет показывает модель из blockstate — тот же
    * кластер, поэтому {@code RenderShape.INVISIBLE} на предмет не влияет).
    *
    * <p>Ранние попытки возвращали {@code INVISIBLE} только при {@code ACTIVE},
    * из-за чего матрица «исчезала» при активации: TESR был приколочен к тому же
    * свойству. Теперь рендерер работает безусловно, и INVISIBLE безопасен.
    */
   @NotNull
   @Override
   public RenderShape getRenderShape(@NotNull BlockState blockState) {
      return RenderShape.INVISIBLE;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new RunicMatrixBlockEntity(blockPos, blockState);
   }

   public boolean triggerEvent(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, int par5, int par6) {
      if (par5 == 1) {
         Thaumcraft.setup.blockSparkle(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 11960575, 2);
         return true;
      } else if (par5 == 12) {
         // Фиолетовый взрыв завершения крафта: искры по объёму матрицы + расширяющееся гало.
         if (level.getBlockEntity(blockPos) instanceof RunicMatrixBlockEntity matrix) {
            matrix.startFinishFlash();
         }

         for (int a = 0; a < Thaumcraft.setup.particleCount(25); a++) {
            float dx = level.random.nextFloat();
            float dy = level.random.nextFloat();
            float dz = level.random.nextFloat();
            Thaumcraft.setup.blockSparkle(
               level,
               blockPos.getX() + (dx > 0.8F ? 1 : 0),
               blockPos.getY() + (dy > 0.8F ? 1 : 0),
               blockPos.getZ() + (dz > 0.8F ? 1 : 0),
               13369599,
               1
            );
         }

         return true;
      } else {
         return super.triggerEvent(blockState, level, blockPos, par5, par6);
      }
   }
}
