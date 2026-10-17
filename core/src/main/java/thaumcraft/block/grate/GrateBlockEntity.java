package thaumcraft.block.grate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.entities.brainless.grate.GrateItemEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class GrateBlockEntity extends BlockEntity implements Container {
   private final IItemHandler itemHandler = new InvWrapper(this);

   public GrateBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType)ModBlockEntities.GRATE.get(), blockPos, blockState);
   }

   public int getContainerSize() {
      return 1;
   }

   public boolean isEmpty() {
      return true;
   }

   @NotNull
   public ItemStack getItem(int index) {
      return ItemStack.EMPTY;
   }

   @NotNull
   public ItemStack removeItem(int index, int amount) {
      return ItemStack.EMPTY;
   }

   @NotNull
   public ItemStack removeItemNoUpdate(int index) {
      return ItemStack.EMPTY;
   }

   public void setItem(int index, @NotNull ItemStack stack) {
      if (!this.level.isClientSide) {
         GrateItemEntity ei = new GrateItemEntity(this.level, this.worldPosition.getCenter(), stack.copy());
         ei.setDeltaMovement(0.0, -0.1, 0.0);
         this.level.addFreshEntity(ei);
      }
   }

   public boolean stillValid(@NotNull Player player) {
      return false;
   }

   public void clearContent() {
   }

   @NotNull
   public IItemHandler getItemHandler(@Nullable Direction side) {
      return this.itemHandler;
   }
}
