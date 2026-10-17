package thaumcraft.block.eldritch.lock;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.ClientSetup;
import thaumcraft.block.eldritch.BlockEldritchEntityBase;
import thaumcraft.init.registry.ModItems;
import thaumcraft.particles.fx.particles.FXSpark;

public class BlockEldritchLock extends BlockEldritchEntityBase {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

   public BlockEldritchLock() {
      super(
         Properties.of()
            .strength(-1.0F, 3600000.0F)
            .sound(SoundType.STONE)
            .noOcclusion()
            .isSuffocating((s, g, p) -> false)
            .isViewBlocking((s, g, p) -> false)
            .isValidSpawn((s, g, p, e) -> false)
            .lightLevel(s -> 5)
      );
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new EldritchLockBlockEntity(pos, state);
   }

   @NotNull
   public net.minecraft.world.ItemInteractionResult useItemOn(
      @NotNull ItemStack held,
      @NotNull BlockState state,
      @NotNull Level level,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hit
   ) {
      if (!held.isEmpty() && held.is((net.minecraft.world.item.Item)ModItems.RUNE_TABLET.get())) {
         if (level.getBlockEntity(pos) instanceof EldritchLockBlockEntity lock && lock.count < 0) {
            if (!level.isClientSide) {
               lock.count = 0;
               lock.setChanged();
               level.sendBlockUpdated(pos, state, state, 3);
               if (!player.getAbilities().instabuild) {
                  held.shrink(1);
               }
               level.playSound(null, pos, (net.minecraft.sounds.SoundEvent)thaumcraft.init.registry.ModSounds.RUNIC_SHIELD_CHARGE.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
         }
      }
      return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   @Override
   public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      return InteractionResult.PASS;
   }

   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (level instanceof ClientLevel clientLevel) {
         if (clientLevel.getBlockEntity(pos) instanceof EldritchLockBlockEntity lock) {
            if (lock.count >= 0) {
               FXSpark fx = new FXSpark(
                  clientLevel,
                  pos.getX() + clientLevel.random.nextFloat(),
                  pos.getY() + clientLevel.random.nextFloat(),
                  pos.getZ() + clientLevel.random.nextFloat(),
                  0.5F
               );
               fx.setColor(0.65F + clientLevel.random.nextFloat() * 0.1F, 1.0F, 1.0F);
               ClientSetup.add(fx);
            }
         }
      }
   }
}
