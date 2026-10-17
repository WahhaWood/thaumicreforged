package thaumcraft.block.eldritch.capstone;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.NotNull;

/**
 * Капитель элдричей — {@code blockEldritch:3} из 1.7.10 ({@code TileEldritchCap}).
 *
 * <p>Восемь капителей стоят по кольцу радиуса 3 вокруг алтаря элдричей
 * ({@code WorldGenEldritchRing}). В порте их не было: кольцо ставило
 * {@code eldritch_obelisk_part}, а тот отдаёт {@code RenderShape.INVISIBLE}
 * (это структурная заглушка, которую рисует рендерер ядра обелиска) — восемь
 * блоков вокруг алтаря выглядели прозрачными.
 *
 * <p>Модель — та же OBJ-капитель, что использует рендерер обелиска
 * ({@code models/obj/obelisk.obj} с текстурой {@code obelisk_cap}).
 */
public class EldritchCapstoneBlock extends Block {
   public EldritchCapstoneBlock() {
      super(Properties.of().strength(50.0F, 20000.0F).sound(SoundType.STONE).noOcclusion().noLootTable());
   }

   @NotNull
   @Override
   public RenderShape getRenderShape(@NotNull BlockState state) {
      return RenderShape.MODEL;
   }

   /**
    * Снег на капители не держится.
    *
    * <p>Капитель — не полный блок, но погода ставит снежный слой по позиции
    * блока, а не по его форме, и в снежных биомах капители зарастали снегом.
    * Как только сверху появляется снежный слой — убираем его. Проверка одна
    * и дешёвая, вызывается из обычного обновления соседей.
    */
   @Override
   public void neighborChanged(
      @NotNull BlockState state, @NotNull net.minecraft.world.level.Level level, @NotNull net.minecraft.core.BlockPos pos,
      @NotNull net.minecraft.world.level.block.Block neighborBlock, net.minecraft.core.BlockPos neighborPos,
      boolean movedByPiston
   ) {
      if (!level.isClientSide
         && level.getBlockState(pos.above()).is(net.minecraft.world.level.block.Blocks.SNOW)) {
         level.removeBlock(pos.above(), false);
      }

      super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
   }
}
