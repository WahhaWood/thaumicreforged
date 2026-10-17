package thaumcraft.block.crucible;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;
import thaumcraft.block.thaumatorium.ThaumatoriumBlockEntity;

public class CrucibleBlock extends TickableEntityBlock {
   public static final VoxelShape INSIDE = box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0);
   public static final VoxelShape SHAPE = Shapes.join(
      Shapes.block(),
      Shapes.or(box(0.0, 0.0, 3.0, 16.0, 3.0, 13.0), new VoxelShape[]{box(3.0, 0.0, 0.0, 13.0, 3.0, 16.0), box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0), INSIDE}),
      BooleanOp.ONLY_FIRST
   );
   /** Коллизия-чаша (дно + 4 стенки), как func_149743_a в 1.7.10: предметы падают внутрь. */
   private static final VoxelShape BOWL = Shapes.or(
      box(0.0, 0.0, 0.0, 16.0, 5.0, 16.0),
      box(0.0, 0.0, 0.0, 2.0, 13.6, 16.0),
      box(14.0, 0.0, 0.0, 16.0, 13.6, 16.0),
      box(0.0, 0.0, 0.0, 16.0, 13.6, 2.0),
      box(0.0, 0.0, 14.0, 16.0, 13.6, 16.0)
   );
   private int delay = 0;

   public CrucibleBlock() {
      super(Properties.of().randomTicks().sound(SoundType.METAL).strength(3.0F, 17.0F));
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
      return new CrucibleBlockEntity(blockPos, blockState);
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
      return SHAPE;
   }

   @NotNull
   public VoxelShape getInteractionShape(@NotNull BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos blockPos) {
      return INSIDE;
   }

   @NotNull
   @Override
   public VoxelShape getCollisionShape(
      @NotNull BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos blockPos, @NotNull CollisionContext context
   ) {
      return BOWL;
   }

   @NotNull
   public ItemInteractionResult useItemOn(
      @NotNull ItemStack stack,
      @NotNull BlockState blockState,
      @NotNull Level level,
      @NotNull BlockPos blockPos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hitResult
   ) {
      if (level.getBlockEntity(blockPos.above()) instanceof ThaumatoriumBlockEntity) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }

      if (!stack.isEmpty()
         && level.getBlockEntity(blockPos) instanceof CrucibleBlockEntity crucible
         && stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM) != null) {
         net.neoforged.neoforge.fluids.FluidActionResult result = net.neoforged.neoforge.fluids.FluidUtil
            .tryEmptyContainer(stack, crucible, Integer.MAX_VALUE, player, true);
         if (result.isSuccess()) {
            net.minecraft.world.item.ItemStack emptied = result.getResult();
            if (!player.isCreative()) {
               player.setItemInHand(hand, emptied);
            }

            level.playSound(
               null, blockPos, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY, net.minecraft.sounds.SoundSource.BLOCKS,
               0.33F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.3F
            );
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
         }
      }

      return ItemInteractionResult.sidedSuccess(level.isClientSide);
   }

   protected void entityInside(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Entity entity) {
      if (level.isClientSide || !entity.isAlive()) {
         return;
      }

      CrucibleBlockEntity crucible = level.getBlockEntity(blockPos) instanceof CrucibleBlockEntity tile ? tile : null;

      if (crucible != null
         && entity instanceof net.minecraft.world.entity.item.ItemEntity item
         && crucible.heat > 150
         && crucible.tank.getFluidAmount() > 0) {
         crucible.attemptSmelt(item);
         return;
      }

      // Оригинал (BlockMetalDevice.func_149670_a) живность в тигле жжёт не на каждом вызове,
      // а на каждом десятом: счётчик delay растёт и обнуляется через 10 вызовов. Без этого
      // тигель сжигал бы существо в 10 раз быстрее оригинала.
      this.delay++;

      if (this.delay < 10) {
         return;
      }

      this.delay = 0;

      if (entity instanceof net.minecraft.world.entity.LivingEntity living
         && crucible != null
         && crucible.heat > 150
         && crucible.tank.getFluidAmount() > 0
         && !living.fireImmune()) {
         living.hurt(level.damageSources().inFire(), 1.0F);
         level.playSound(
            null, blockPos, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, net.minecraft.sounds.SoundSource.BLOCKS,
            0.4F, 2.0F + level.random.nextFloat() * 0.4F
         );
      }
   }

   @Override
   public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, boolean p_60519_) {
      if (!level.isClientSide && level.getBlockEntity(blockPos) instanceof CrucibleBlockEntity crucible) {
         ;
      }

      super.onRemove(state, level, blockPos, blockState, p_60519_);
   }

   public void randomTick(@NotNull BlockState blockState, @NotNull ServerLevel level, @NotNull BlockPos blockPos, @NotNull RandomSource randomSource) {
      if (randomSource.nextInt(10) == 0
         && level.getBlockEntity(blockPos) instanceof CrucibleBlockEntity crucible
         && crucible.tank.getFluidAmount() > 0
         && crucible.heat > 150) {
         level.playSound(
            null, blockPos, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.1F + randomSource.nextFloat() * 0.1F, 1.2F + randomSource.nextFloat() * 0.2F
         );
      }
   }

   public void neighborChanged(
      @NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Block block, @NotNull BlockPos blockPos1, boolean p_60514_
   ) {
      if (!level.isClientSide && level.getBlockEntity(blockPos) instanceof CrucibleBlockEntity crucible) {
         ;
      }
   }

   public boolean hasAnalogOutputSignal(@NotNull BlockState blockState) {
      return true;
   }

   public int getAnalogOutputSignal(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos) {
      if (level.getBlockEntity(blockPos) instanceof CrucibleBlockEntity crucible) {
         float size = crucible.aspects.visSize();
         float r = size / 100.0F;
         return Mth.floor(r * 14.0F) + (size > 0.0F ? 1 : 0);
      } else {
         return super.getAnalogOutputSignal(blockState, level, blockPos);
      }
   }
}
