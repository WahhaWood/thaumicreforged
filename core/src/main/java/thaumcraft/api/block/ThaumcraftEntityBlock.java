package thaumcraft.api.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.util.InventoryUtils;

public abstract class ThaumcraftEntityBlock extends BaseEntityBlock {
   public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

   public ThaumcraftEntityBlock(Properties properties) {
      super(properties);
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return null;
   }

   @NotNull
   public RenderShape getRenderShape(@NotNull BlockState blockState) {
      return RenderShape.MODEL;
   }

   @NotNull
   public VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
      return Shapes.block();
   }

   @NotNull
   protected InteractionResult useWithoutItem(
      @NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Player player, @NotNull BlockHitResult hitResult
   ) {
      if (!player.isShiftKeyDown() && level.getBlockEntity(blockPos) instanceof MenuProvider provider) {
         if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(provider, buf -> buf.writeBlockPos(blockPos));
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }

   @NotNull
   public InteractionResult use(
      @NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Player player, @NotNull BlockHitResult hitResult
   ) {
      return InteractionResult.PASS;
   }

   public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, boolean p_60519_) {
      InventoryUtils.dropItems(level, blockPos);
      super.onRemove(state, level, blockPos, blockState, p_60519_);
   }

   public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity entity, @NotNull ItemStack stack) {
   }
}
