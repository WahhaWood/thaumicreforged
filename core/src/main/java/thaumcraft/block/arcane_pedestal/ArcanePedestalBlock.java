package thaumcraft.block.arcane_pedestal;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.ThaumcraftEntityBlock;

/**
 * Магический пьедестал (порт {@code TilePedestal} / BlockStoneDevice meta 1 из 1.7.10).
 *
 * <p>Каноническое взаимодействие:
 * <ul>
 *   <li>ПКМ предметом по пустому пьедесталу — кладём 1 предмет;</li>
 *   <li>ПКМ по занятому пьедесталу — забираем предмет в инвентарь (или выбрасываем под ноги, если места нет).</li>
 * </ul>
 */
public class ArcanePedestalBlock extends ThaumcraftEntityBlock {
   private static final VoxelShape SHAPE = Shapes.or(
      Shapes.box(0.0, 0.0, 0.0, 1.0, 0.25, 1.0),
      new VoxelShape[]{Shapes.box(0.25, 0.25, 0.25, 0.75, 0.75, 0.75), Shapes.box(0.125, 0.75, 0.125, 0.875, 1.0, 0.875)}
   );

   public ArcanePedestalBlock() {
      super(Properties.of().forceSolidOn().sound(SoundType.STONE).strength(3.0F, 25.0F));
   }

   @Nullable
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new ArcanePedestalBlockEntity(blockPos, blockState);
   }

   public boolean hasAnalogOutputSignal(@NotNull BlockState blockState) {
      return true;
   }

   public int getAnalogOutputSignal(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos) {
      return level.getBlockEntity(blockPos) instanceof ArcanePedestalBlockEntity pedestal ? AbstractContainerMenu.getRedstoneSignalFromContainer(pedestal) : 0;
   }

   @NotNull
   @Override
   public ItemInteractionResult useItemOn(
      @NotNull ItemStack stack,
      @NotNull BlockState state,
      @NotNull Level level,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hit
   ) {
      if (!(level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity pedestal)) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }

      if (!pedestal.getItem(0).isEmpty()) {
         // ПКМ по занятому пьедесталу — забираем предмет (порт 1.7.10 BlockStoneDevice#func_149727_a).
         takeItem(level, pos, player, pedestal);
         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      }

      if (!stack.isEmpty()) {
         // ПКМ предметом по пустому пьедесталу — кладём ровно 1 штуку.
         if (!level.isClientSide) {
            ItemStack placed = stack.copyWithCount(1);
            stack.shrink(1);
            pedestal.setItem(0, placed);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F, 1.6F);
         }
         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      }

      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   @NotNull
   @Override
   public InteractionResult useWithoutItem(
      @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit
   ) {
      if (level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity pedestal && !pedestal.getItem(0).isEmpty()) {
         takeItem(level, pos, player, pedestal);
         return InteractionResult.sidedSuccess(level.isClientSide);
      }
      return InteractionResult.PASS;
   }

   private static void takeItem(Level level, BlockPos pos, Player player, ArcanePedestalBlockEntity pedestal) {
      if (!level.isClientSide) {
         ItemStack stored = pedestal.getItem(0);
         // Забираем весь стек (результат инфузии может быть стаком, например 8 ихора).
         ItemStack taken = pedestal.removeItem(0, Math.max(1, stored.getCount()));
         if (!player.getInventory().add(taken)) {
            player.drop(taken, false);
         }
         level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F, 1.5F);
      }
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter getter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
      return SHAPE;
   }
}
