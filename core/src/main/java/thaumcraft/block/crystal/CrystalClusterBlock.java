package thaumcraft.block.crystal;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.api.crafting.IInfusionStabiliser;
import thaumcraft.api.render.ITIntColor;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModSounds;

public class CrystalClusterBlock extends DirectionalBlock implements IInfusionStabiliser, EntityBlock, ITIntColor {
   public static final MapCodec<CrystalClusterBlock> CODEC = simpleCodec(props -> new CrystalClusterBlock());
   public static final DeferredSoundType SOUND_TYPE = new DeferredSoundType(
      1.0F,
      1.0F,
      () -> SoundEvents.GLASS_BREAK,
      ModSounds.CRYSTAL::get,
      () -> SoundEvents.GLASS_PLACE,
      () -> SoundEvents.GLASS_HIT,
      () -> SoundEvents.GLASS_FALL
   );

   public CrystalClusterBlock() {
      super(Properties.of().sound(SOUND_TYPE).noOcclusion().forceSolidOn().strength(0.7F, 1.0F).lightLevel(value -> 8));
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(FACING, Direction.DOWN));
   }

   protected MapCodec<? extends DirectionalBlock> codec() {
      return CODEC;
   }

   public int getColor() {
      int meta = 0;
      if (ModBlocks.FIRE_CRYSTAL_CLUSTER.get() == this) {
         meta = 1;
      }

      if (ModBlocks.WATER_CRYSTAL_CLUSTER.get() == this) {
         meta = 2;
      }

      if (ModBlocks.EARTH_CRYSTAL_CLUSTER.get() == this) {
         meta = 3;
      }

      if (ModBlocks.ORDER_CRYSTAL_CLUSTER.get() == this) {
         meta = 4;
      }

      if (ModBlocks.ENTROPY_CRYSTAL_CLUSTER.get() == this) {
         meta = 5;
      }

      return this.getColor(meta);
   }

   /**
    * Цвет аспекта блока: используется и BER ({@code getColor(0..5)}), и тинтом
    * статической модели ({@code ITIntColor}, tint 0). Остальные тинты не красим.
    */
   @Override
   public int getColor(int meta) {
      if (meta < 0 || meta > 5) {
         return -1;
      }

      return switch (meta) {
         case 1 -> 16727041;
         case 2 -> 37119;
         case 3 -> 40960;
         case 4 -> 15650047;
         case 5 -> 5592439;
         default -> 16777086;
      };
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      // В 1.7.10 мета ориентации равна номеру кликнутой грани (onBlockPlaced:
      // side 0 — потолок, 1 — пол), а FACING означает направление роста.
      // Здесь стояло getOpposite — все ручные установки выходили вверх ногами.
      //
      // NB: BlockPlaceContext.getClickedPos() для некликабельных блоков уже
      // сдвинут на ЦЕЛЕВУЮ клетку (relativePos), поэтому опора — это цель,
      // смещённая против направления роста, а не getClickedPos() сам.
      Direction facing = context.getClickedFace();
      BlockPos support = context.getClickedPos().relative(facing.getOpposite());
      if (!context.getLevel().getBlockState(support).isFaceSturdy(context.getLevel(), support, facing)) {
         return null;
      }

      return (BlockState)this.defaultBlockState().setValue(FACING, facing);
   }

   /**
    * Проверка опоры — порт {@code BlockCrystal} (1.7.10, строки 143-165):
    * кристалл держится только за грань, противоположную направлению роста.
    * Без этого кристаллы, чью опору позже сносил проход соседней ячейки,
    * оставались висеть в воздухе (в оригинале они отваливаются).
    */
   public boolean canSurvive(@NotNull BlockState state, @NotNull net.minecraft.world.level.LevelReader level, @NotNull BlockPos pos) {
      Direction facing = (Direction)state.getValue(FACING);
      BlockPos support = pos.relative(facing.getOpposite());
      return level.getBlockState(support).isFaceSturdy(level, support, facing);
   }

   public void neighborChanged(
      @NotNull BlockState state,
      @NotNull net.minecraft.world.level.Level level,
      @NotNull BlockPos pos,
      @NotNull Block blockIn,
      @NotNull BlockPos fromPos,
      boolean isMoving
   ) {
      // В оригинале отвал с дропом, а не тихая замена на воздух, поэтому
      // updateShape не трогаем: destroyBlock роняет предмет игроку.
      if (!level.isClientSide && !this.canSurvive(state, level, pos)) {
         level.destroyBlock(pos, true);
      }
   }

   @Nullable
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new CrystalClusterBlock.CrystalClusterBlockEntity(blockPos, blockState);
   }

   public static class CrystalClusterBlockEntity extends ThaumcraftBlockEntity {
      public CrystalClusterBlockEntity(BlockPos blockPos, BlockState blockState) {
         super((BlockEntityType<?>)ModBlockEntities.CRYSTAL_CLUSTER.get(), blockPos, blockState);
      }
   }
}
