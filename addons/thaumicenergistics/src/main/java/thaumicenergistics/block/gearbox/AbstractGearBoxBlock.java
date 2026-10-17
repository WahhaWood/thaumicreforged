package thaumicenergistics.block.gearbox;

import thaumicenergistics.block.gearbox.entity.GearBoxBlockEntity;
import thaumicenergistics.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractGearBoxBlock extends BaseEntityBlock implements EntityBlock {
   private static final VoxelShape SHAPE = Shapes.box(0.23, 0.23, 0.23, 0.77, 0.77, 0.77);
   private final boolean allowGolemInteraction;

   protected AbstractGearBoxBlock(boolean allowGolemInteraction) {
      super(Properties.of().mapColor(MapColor.METAL).strength(0.6F).sound(SoundType.STONE).noOcclusion());
      this.allowGolemInteraction = allowGolemInteraction;
   }

   @NotNull
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return SHAPE;
   }

   @NotNull
   public RenderShape getRenderShape(@NotNull BlockState state) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }

   @NotNull
   protected ItemInteractionResult useItemOn(
      @NotNull ItemStack clickedStack,
      @NotNull BlockState state,
      @NotNull Level level,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hitResult
   ) {
      if (!this.canPlayerInteract(player)) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      } else {
         Object var10000 = null;
         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      }
   }

   private boolean canPlayerInteract(Player player) {
      if (!(player instanceof FakePlayer)) {
         return true;
      } else {
         return !this.allowGolemInteraction ? false : "FakeThaumcraftGolem".equals(player.getName().getString());
      }
   }

   public void neighborChanged(
      @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving
   ) {
      super.neighborChanged(state, level, pos, block, fromPos, isMoving);
      if (level.getBlockEntity(pos) instanceof GearBoxBlockEntity gearBox) {
         gearBox.updateCrankables();
         gearBox.requestSync();
      }
   }

   public boolean isSignalSource(@NotNull BlockState state) {
      return false;
   }

   public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
      return false;
   }

   @Nullable
   public abstract BlockEntity newBlockEntity(@NotNull BlockPos var1, @NotNull BlockState var2);

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
      return level.isClientSide
         ? createTickerHelper(type, (BlockEntityType)ModBlockEntities.GEAR_BOX.get(), GearBoxBlockEntity::clientTick)
         : createTickerHelper(type, (BlockEntityType)ModBlockEntities.GEAR_BOX.get(), GearBoxBlockEntity::serverTick);
   }
}
