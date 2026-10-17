package thaumcraft.block.arcane_door;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.block.opaque.OwnedBlockEntity;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModBlocks;

public class ArcaneDoorBlock extends DoorBlock implements EntityBlock {
   public ArcaneDoorBlock() {
      super(
         BlockSetType.IRON, Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).noOcclusion().strength(Config.wardedStone ? -1.0F : 15.0F, 999.0F)
      );
   }

   private static BlockPos getBasePos(BlockPos pos, BlockState state) {
      return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
   }

   public void setPlacedBy(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (!level.isClientSide && placer instanceof Player player) {
         ;
      }
   }

   @NotNull
   public InteractionResult useWithoutItem(
      @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit
   ) {
      BlockPos basePos = getBasePos(pos, state);
      if (!(level.getBlockEntity(basePos) instanceof OwnedBlockEntity owned)) {
         return InteractionResult.PASS;
      } else {
         String name = player.getScoreboardName();
         boolean allowed = player.isCreative() || name.equals(owned.owner) || owned.accessList.contains("0" + name) || owned.accessList.contains("1" + name);
         if (allowed) {
            if (!level.isClientSide) {
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
         } else {
            if (!level.isClientSide) {
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
         }
      }
   }

   public void neighborChanged(
      @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos fromPos, boolean isMoving
   ) {
      if (state.getValue(HALF) != DoubleBlockHalf.UPPER) {
         BlockState plate = level.getBlockState(fromPos);
         if (plate.is((Block)ModBlocks.ARCANE_PRESSURE_PLATE.get()) && !level.isClientSide) {
         }
      }
   }

   @NotNull
   public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull Builder builder) {
      return (List<ItemStack>)(Config.wardedStone ? new ArrayList<>() : super.getDrops(state, builder));
   }

   @Nullable
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return state.getValue(HALF) == DoubleBlockHalf.UPPER ? null : new ArcaneDoorBlockEntity(pos, state);
   }

   public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
      if (!Config.wardedStone) {
         super.onBlockExploded(state, level, pos, explosion);
      }
   }
}
