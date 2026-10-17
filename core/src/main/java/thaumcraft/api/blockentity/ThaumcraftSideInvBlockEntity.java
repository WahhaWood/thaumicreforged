package thaumcraft.api.blockentity;

import java.util.EnumMap;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThaumcraftSideInvBlockEntity extends ThaumcraftInvBlockEntity implements WorldlyContainer {
   private final IItemHandler unsidedWrapper = new SidedInvWrapper(this, null);
   private final EnumMap<Direction, IItemHandler> sidedWrappers = new EnumMap<>(Direction.class);
   protected int[] slotsForFace;

   public ThaumcraftSideInvBlockEntity(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState, int containerSize) {
      super(type, blockPos, blockState, containerSize);
      this.slotsForFace = IntStream.range(0, containerSize).toArray();
   }

   @NotNull
   public int[] getSlotsForFace(@NotNull Direction direction) {
      return this.slotsForFace;
   }

   public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction direction) {
      return this.canPlaceItem(index, stack);
   }

   public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
      return true;
   }

   @NotNull
   public IItemHandler getItemHandler(@Nullable Direction side) {
      return side == null ? this.unsidedWrapper : this.sidedWrappers.computeIfAbsent(side, dir -> new SidedInvWrapper(this, dir));
   }

   public void invalidateCaps() {
      this.sidedWrappers.clear();
      if (this.level != null) {
         this.level.invalidateCapabilities(this.worldPosition);
      }
   }
}
