package thaumicenergistics.block.arcane_assembler;

import com.mojang.serialization.MapCodec;
import thaumicenergistics.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
import thaumcraft.util.ItemStackData;

public class ArcaneAssemblerBlock extends BaseEntityBlock implements EntityBlock {
   public static final MapCodec<ArcaneAssemblerBlock> CODEC = simpleCodec(properties -> new ArcaneAssemblerBlock());
   public static final int MAX_SPEED_UPGRADES = 4;

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   public ArcaneAssemblerBlock() {
      super(Properties.of().mapColor(MapColor.METAL).strength(1.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());
   }

   @NotNull
   protected ItemInteractionResult useItemOn(
      ItemStack clickedStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
   ) {
      if (level.getBlockEntity(pos) instanceof ArcaneAssemblerBlockEntity assembler) {
         if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(assembler, buf -> buf.writeBlockPos(pos));
         }

         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      }

      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (level.getBlockEntity(pos) instanceof ArcaneAssemblerBlockEntity assembler) {
         CompoundTag tag = ItemStackData.getTag(stack);
         if (tag != null) {
            assembler.readVisLevelsFromNBT(tag);
         }

         if (placer instanceof Player player) {
            assembler.setOwner(player);
         }
      }
   }

   @NotNull
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new ArcaneAssemblerBlockEntity(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      if (type != ModBlockEntities.ARCANE_ASSEMBLER.get()) {
         return null;
      } else {
         return level.isClientSide()
            ? (lvl, pos, st, be) -> ArcaneAssemblerBlockEntity.clientTick(lvl, pos, st, (ArcaneAssemblerBlockEntity)be)
            : (lvl, pos, st, be) -> ArcaneAssemblerBlockEntity.serverTick(lvl, pos, st, (ArcaneAssemblerBlockEntity)be);
      }
   }
}
