package thaumcraft.block.magnet;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.block.TickableEntityBlock;
import thaumcraft.item.wands.ItemWandCasting;

public class MagnetBlock extends TickableEntityBlock {
   public static final BooleanProperty PULLING = BooleanProperty.create("pulling");
   private static final VoxelShape SHAPE = box(1.0, 0.0, 1.0, 15.0, 2.0, 15.0);
   private final boolean mobMagnet;

   public MagnetBlock(boolean mobMagnet) {
      super(Properties.of().strength(1.7F, 1.0F).sound(SoundType.WOOD).noOcclusion().isRedstoneConductor((s, l, p) -> false));
      this.mobMagnet = mobMagnet;
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(PULLING, true));
   }

   @NotNull
   @Override
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return SHAPE;
   }

   @NotNull
   @Override
   public RenderShape getRenderShape(@NotNull BlockState blockState) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{PULLING});
   }

   @NotNull
   public ItemInteractionResult useItemOn(
      @NotNull ItemStack held,
      @NotNull BlockState state,
      @NotNull Level level,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hit
   ) {
      if (this.mobMagnet && held.getItem() instanceof ItemWandCasting && level.getBlockEntity(pos) instanceof MobMagnetBlockEntity mobMagnetBlockEntity) {
         if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(mobMagnetBlockEntity, buf -> buf.writeBlockPos(pos));
            level.playSound(null, pos, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 1.0F, 0.5F);
         }

         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      } else {
         if (!level.isClientSide) {
            level.setBlock(pos, (BlockState)state.cycle(PULLING), 3);
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, 0.5F);
         }

         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      }
   }

   public boolean isMobMagnet() {
      return this.mobMagnet;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return (BlockEntity)(this.mobMagnet ? new MobMagnetBlockEntity(pos, state) : new MagnetBlockEntity(pos, state));
   }
}
