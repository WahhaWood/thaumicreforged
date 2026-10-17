package thaumicenergistics.block.crucible_assembler;

import com.mojang.serialization.MapCodec;
import thaumicenergistics.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrucibleAssemblerBlock extends BaseEntityBlock implements EntityBlock {
   public static final MapCodec<CrucibleAssemblerBlock> CODEC = simpleCodec(properties -> new CrucibleAssemblerBlock());

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   public CrucibleAssemblerBlock() {
      super(Properties.of().mapColor(MapColor.METAL).strength(1.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());
   }

   @NotNull
   protected ItemInteractionResult useItemOn(
      ItemStack clickedStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
   ) {
      if (level.getBlockEntity(pos) instanceof CrucibleAssemblerBlockEntity assembler) {
         if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(assembler, buf -> buf.writeBlockPos(pos));
         }

         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      }

      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (level.getBlockEntity(pos) instanceof CrucibleAssemblerBlockEntity assembler && placer instanceof Player player) {
         assembler.setOwner(player);
      }
   }

   @NotNull
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new CrucibleAssemblerBlockEntity(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      if (type != ModBlockEntities.CRUCIBLE_ASSEMBLER.get()) {
         return null;
      } else {
         return level.isClientSide()
            ? (lvl, pos, st, be) -> CrucibleAssemblerBlockEntity.clientTick(lvl, pos, st, (CrucibleAssemblerBlockEntity)be)
            : (lvl, pos, st, be) -> CrucibleAssemblerBlockEntity.serverTick(lvl, pos, st, (CrucibleAssemblerBlockEntity)be);
      }
   }
}
