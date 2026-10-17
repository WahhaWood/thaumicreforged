package thaumcraft.block.thaumatorium;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;
import thaumcraft.block.crucible.CrucibleBlock;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.init.registry.ModItems;

public class ThaumatoriumBlock extends TickableEntityBlock {
   public static final EnumProperty<ThaumatoriumBlock.Element> ELEMENT = EnumProperty.create("element", ThaumatoriumBlock.Element.class);

   public ThaumatoriumBlock() {
      super(Properties.of().forceSolidOn().noOcclusion().sound(SoundType.METAL).strength(3.0F, 17.0F));
      this.registerDefaultState(
         (BlockState)((BlockState)this.defaultBlockState().setValue(ELEMENT, ThaumatoriumBlock.Element.CENTER))
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
      );
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
      builder.add(new Property[]{ELEMENT, BlockStateProperties.HORIZONTAL_FACING});
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      ThaumatoriumBlock.Element element = (ThaumatoriumBlock.Element)blockState.getValue(ELEMENT);
      return element != ThaumatoriumBlock.Element.TOP && element != ThaumatoriumBlock.Element.CENTER ? null : new ThaumatoriumBlockEntity(blockPos, blockState);
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
      return state.getValue(ELEMENT) == ThaumatoriumBlock.Element.BOTTOM ? CrucibleBlock.INSIDE : Shapes.block();
   }

   /**
    * Разборка тауматория.
    *
    * <p>В оригинале это {@code BlockMetalDevice.func_149695_a} (onNeighborChange): колонна md 10
    * проверяет, что сверху стоит md 11, а снизу — md 0 (основание), и иначе откатывается в
    * md 9, то есть в обычную алхимическую конструкцию; верхний md 11 проверяет md 10 под собой
    * и откатывается так же.
    *
    * <p>Основание в порте — тигель, а не металлический блок (см.
    * {@code WandManager.createThaumatorium}), поэтому проверяется CRUCIBLE под колонной.
    * Судьба деталей совпадает с оригиналом: обе половины возвращаются в ALCHEMICAL_CONSTRUCT.
    */
   @Override
   protected void neighborChanged(
      @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
      @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean movedByPiston
   ) {
      if (level.isClientSide() || checkStructure(state, level, pos)) {
         return;
      }

      restoreConstruct(level, pos);
   }

   /**
    * Ядро вынули или заменили — верхняя половина не должна остаться висеть одна.
    *
    * <p>Откатываем верх явно, а не каскадом соседей: в момент {@code onRemove} на месте
    * центра уже воздух, и верхний блок уведомления не получит — некому сообщить, что под
    * ним пусто. В оригинале ту же роль играет {@code func_149749_a} у md 11, который видит
    * разобранный md 10 и откатывается сам. Проверка на ALCHEMICAL_CONSTRUCT отсекает
    * собственный откат из {@link #restoreConstruct}, иначе разбор входил бы в себя рекурсивно.
    */
   @Override
   public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
      if (!state.is(newState.getBlock())
         && state.getValue(ELEMENT) == ThaumatoriumBlock.Element.CENTER
         && !newState.is(ModBlocks.ALCHEMICAL_CONSTRUCT.get())
         && !level.isClientSide()) {
         restoreConstruct(level, pos.above());
         restoreConstruct(level, pos);
      }

      super.onRemove(state, level, pos, newState, movedByPiston);
   }

   /** Половина колонны на месте, если у неё есть сосед по конструкции. */
   private static boolean checkStructure(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
      ThaumatoriumBlock.Element element = state.getValue(ELEMENT);

      if (element == ThaumatoriumBlock.Element.CENTER) {
         return level.getBlockState(pos.below()).is(ModBlocks.CRUCIBLE.get()) && isThaumatoriumPart(level, pos.above(), ThaumatoriumBlock.Element.TOP);
      }

      if (element == ThaumatoriumBlock.Element.TOP) {
         return isThaumatoriumPart(level, pos.below(), ThaumatoriumBlock.Element.CENTER);
      }

      return true;
   }

   private static boolean isThaumatoriumPart(@NotNull Level level, @NotNull BlockPos at, @NotNull ThaumatoriumBlock.Element element) {
      BlockState state = level.getBlockState(at);
      return state.getBlock() instanceof ThaumatoriumBlock && state.getValue(ELEMENT) == element;
   }

   /**
    * Откат в обычную алхимическую конструкцию — оригинальный {@code setBlock(this, 9, 3)}.
    *
    * <p>Вторую половину колонны убирать вручную не нужно: замена с {@code UPDATE_ALL}
    * будит соседа, тот не находит под собой (или над собой) тауматорий и откатывается сам.
    * На этом цепочка и обрывается — ALCHEMICAL_CONSTRUCT больше ничего не будит.
    */
   private static void restoreConstruct(@NotNull Level level, @NotNull BlockPos pos) {
      BlockState old = level.getBlockState(pos);

      if (!(old.getBlock() instanceof ThaumatoriumBlock)) {
         return;
      }

      BlockState construct = ModBlocks.ALCHEMICAL_CONSTRUCT.get().defaultBlockState();
      level.setBlock(pos, construct, Block.UPDATE_ALL);
      level.sendBlockUpdated(pos, old, construct, Block.UPDATE_ALL);
   }

   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
      return switch ((ThaumatoriumBlock.Element)state.getValue(ELEMENT)) {
         case BOTTOM -> new ItemStack((ItemLike)ModItems.CRUCIBLE.get());
         case CENTER, TOP -> new ItemStack((ItemLike)ModItems.ALCHEMICAL_CONSTRUCT.get());
      };
   }

   @NotNull
   public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      return List.of(switch ((ThaumatoriumBlock.Element)state.getValue(ELEMENT)) {
         case BOTTOM -> new ItemStack((ItemLike)ModItems.CRUCIBLE.get());
         case CENTER, TOP -> new ItemStack((ItemLike)ModItems.ALCHEMICAL_CONSTRUCT.get());
      });
   }

   @NotNull
   @Override
   public InteractionResult useWithoutItem(
      @NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Player player, @NotNull BlockHitResult hitResult
   ) {
      if (player.isShiftKeyDown()) {
         return InteractionResult.PASS;
      }
      ThaumatoriumBlock.Element element = (ThaumatoriumBlock.Element)blockState.getValue(ELEMENT);
      BlockPos targetPos = blockPos;
      if (element == ThaumatoriumBlock.Element.TOP) {
         targetPos = blockPos.below();
      } else if (element == ThaumatoriumBlock.Element.BOTTOM) {
         targetPos = blockPos.above();
      }

      if (level.getBlockEntity(targetPos) instanceof ThaumatoriumBlockEntity thaumatorium) {
         if (level.isClientSide) {
            return InteractionResult.SUCCESS;
         } else if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            BlockPos openPos = targetPos;
            serverPlayer.openMenu(thaumatorium, buf -> buf.writeBlockPos(openPos));
            return InteractionResult.CONSUME;
         }
      }

      return InteractionResult.PASS;
   }

   public static enum Element implements StringRepresentable {
      BOTTOM,
      CENTER,
      TOP;

      @NotNull
      public String getSerializedName() {
         return this.name().toLowerCase();
      }
   }
}
