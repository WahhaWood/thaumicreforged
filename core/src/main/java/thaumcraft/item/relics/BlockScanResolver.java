package thaumcraft.item.relics;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.research.ScanResult;
import thaumcraft.util.ItemStackData;

final class BlockScanResolver {
   private BlockScanResolver() {
   }

   static ScanResult resolveScanResult(Level level, BlockPos blockPos, BlockState blockState, BlockHitResult hitResult, Player player) {
      if (ThaumcraftApi.exists(blockState)) {
         return ScanResult.ofBlock(blockState.getBlock());
      } else {
         ItemStack itemBackedScanStack = resolveItemBackedScanStack(level, blockPos, blockState, hitResult, player);
         return !itemBackedScanStack.isEmpty() ? ScanResult.ofItem(itemBackedScanStack) : resolveNoItemBlockScan(blockState);
      }
   }

   @NotNull
   private static ItemStack resolveItemBackedScanStack(Level level, BlockPos blockPos, BlockState blockState, BlockHitResult hitResult, Player player) {
      ItemStack pickedStack = blockState.getCloneItemStack(hitResult, level, blockPos, player);
      if (pickedStack.isEmpty()) {
         pickedStack = blockState.getBlock().getCloneItemStack(level, blockPos, blockState);
      }

      if (pickedStack.isEmpty()) {
         pickedStack = new ItemStack(blockState.getBlock());
      }

      if (pickedStack.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack normalizedStack = pickedStack.copy();
         normalizedStack.setCount(1);
         stripVolatileBlockScanData(normalizedStack);
         return normalizedStack;
      }
   }

   private static void stripVolatileBlockScanData(ItemStack stack) {
      if (ItemStackData.hasTag(stack)) {
         CompoundTag tag = ItemStackData.getTag(stack);
         if (tag != null) {
            CompoundTag sanitized = tag.copy();
            sanitized.remove("BlockEntityTag");
            sanitized.remove("BlockStateTag");
            ItemStackData.setTag(stack, sanitized.isEmpty() ? null : sanitized);
         }
      }
   }

   private static ScanResult resolveNoItemBlockScan(BlockState blockState) {
      return ScanResult.ofBlock(blockState.getBlock());
   }
}
