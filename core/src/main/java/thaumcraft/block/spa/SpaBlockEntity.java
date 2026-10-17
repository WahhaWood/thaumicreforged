package thaumcraft.block.spa;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ITickable;
import thaumcraft.api.blockentity.ThaumcraftSideInvBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.item.bath_salts.ItemBathSalts;

public class SpaBlockEntity extends ThaumcraftSideInvBlockEntity implements IFluidHandler, MenuProvider, ITickable {
   private boolean mix = true;
   private int counter = 0;
   public final FluidTank tank = new FluidTank(5000);

   public SpaBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.ARCANE_SPA.get(), blockPos, blockState, 1);
   }

   public void toggleMix() {
      this.mix = !this.mix;
      this.markForUpdate();
      this.setChanged();
   }

   public boolean getMix() {
      return this.mix;
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound, Provider provider) {
      super.loadCustom(compound, provider);
      this.mix = compound.getBoolean("mix");
      this.tank.setFluid(FluidStack.parseOptional(provider, compound.getCompound("tank")));
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound, Provider provider) {
      super.saveCustom(compound, provider);
      compound.putBoolean("mix", this.mix);
      if (!this.tank.getFluid().isEmpty()) {
         compound.put("tank", this.tank.getFluid().save(provider));
      }
   }

   @Override
   public void loadAdditional(@NotNull CompoundTag compound, Provider provider) {
      super.loadAdditional(compound, provider);
   }

   @Override
   protected void saveAdditional(@NotNull CompoundTag compound, Provider provider) {
      super.saveAdditional(compound, provider);
   }

   public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
      return stack.getItem() instanceof ItemBathSalts;
   }

   @NotNull
   @Override
   public int[] getSlotsForFace(@NotNull Direction direction) {
      return direction != Direction.UP ? super.getSlotsForFace(direction) : new int[0];
   }

   @Override
   public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction direction) {
      return direction != Direction.UP && super.canPlaceItem(index, stack);
   }

   @Override
   public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
      return direction != Direction.UP;
   }

   @Nullable
   private BlockState getTargetPlaceState() {
      if (this.mix) {
         return ((LiquidBlock)ModBlocks.FLUID_PURE.get()).defaultBlockState();
      } else {
         FluidStack fs = this.tank.getFluid();
         if (!fs.isEmpty() && this.tank.getFluidAmount() >= 1000) {
            Fluid fluid = fs.getFluid();
            BlockState st = fluid.defaultFluidState().createLegacyBlock();
            return st.isAir() ? null : st;
         } else {
            return null;
         }
      }
   }

   private boolean hasIngredients() {
      FluidStack fs = this.tank.getFluid();
      if (!this.mix) {
         if (!fs.isEmpty() && this.tank.getFluidAmount() >= 1000) {
            BlockState place = fs.getFluid().defaultFluidState().createLegacyBlock();
            return !place.isAir();
         } else {
            return false;
         }
      } else if (!fs.isEmpty() && fs.getFluid() == Fluids.WATER && this.tank.getFluidAmount() >= 1000) {
         ItemStack in = this.getItem(0);
         return !in.isEmpty() && in.getItem() instanceof ItemBathSalts;
      } else {
         return false;
      }
   }

   private boolean isValidLocation(BlockPos pos, boolean mustBeAdjacent, Block targetBlock) {
      Level level = this.level;
      if (level == null) {
         return false;
      } else if (targetBlock == Blocks.WATER && level.dimensionType().ultraWarm()) {
         return false;
      } else {
         BlockState state = level.getBlockState(pos);
         BlockState below = level.getBlockState(pos.below());
         if (!below.isFaceSturdy(level, pos.below(), Direction.UP)) {
            return false;
         } else if (!state.canBeReplaced()) {
            return false;
         } else {
            return state.getBlock() == targetBlock && level.getFluidState(pos).isSource()
               ? false
               : !mustBeAdjacent || this.isBlockTouchingTargetSource(pos, targetBlock);
         }
      }
   }

   private boolean isBlockTouchingTargetSource(BlockPos pos, Block targetBlock) {
      Level level = this.level;
      if (level == null) {
         return false;
      } else {
         for (Direction d : Direction.values()) {
            BlockPos p = pos.relative(d);
            BlockState st = level.getBlockState(p);
            if (st.getBlock() == targetBlock && level.getFluidState(p).isSource()) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public void serverTick(Level level, BlockPos pos, BlockState state) {
      if (this.counter++ % 40 == 0 && !level.hasNeighborSignal(pos) && this.hasIngredients()) {
         BlockState targetState = this.getTargetPlaceState();
         if (targetState == null) {
            return;
         }
         Block targetBlock = targetState.getBlock();
         BlockPos above = pos.above();
         BlockState currentAbove = level.getBlockState(above);

         if (currentAbove.getBlock() == targetBlock && level.getFluidState(above).isSource()) {
            for (int xx = -2; xx <= 2; xx++) {
               for (int zz = -2; zz <= 2; zz++) {
                  BlockPos checkPos = above.offset(xx, 0, zz);
                  if (this.isValidLocation(checkPos, true, targetBlock)) {
                     this.consumeIngredients();
                     level.setBlockAndUpdate(checkPos, targetState);
                     return;
                  }
               }
            }
         } else if (this.isValidLocation(above, false, targetBlock)) {
            this.consumeIngredients();
            level.setBlockAndUpdate(above, targetState);
         }
      }
   }

   private void consumeIngredients() {
      if (this.mix) {
         this.removeItem(0, 1);
      }
      this.drain(1000, FluidAction.EXECUTE);
   }

   public int getTanks() {
      return this.tank.getTanks();
   }

   @NotNull
   public FluidStack getFluidInTank(int tank) {
      return this.tank.getFluidInTank(tank);
   }

   public int getTankCapacity(int tank) {
      return this.tank.getTankCapacity(tank);
   }

   public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
      return this.tank.isFluidValid(tank, stack);
   }

   public int fill(FluidStack resource, FluidAction action) {
      if (resource != null && !resource.isEmpty()) {
         int df = this.tank.fill(resource, action);
         if (df > 0 && action == FluidAction.EXECUTE) {
            this.markForUpdate();
            this.setChanged();
         }

         return df;
      } else {
         return 0;
      }
   }

   @NotNull
   public FluidStack drain(FluidStack resource, FluidAction action) {
      if (resource == null || resource.isEmpty()) {
         return FluidStack.EMPTY;
      } else if (!FluidStack.isSameFluidSameComponents(resource, this.tank.getFluid())) {
         return FluidStack.EMPTY;
      } else {
         FluidStack result = this.tank.drain(resource, action);
         if (!result.isEmpty() && action == FluidAction.EXECUTE) {
            this.markForUpdate();
            this.setChanged();
         }

         return result;
      }
   }

   @NotNull
   public FluidStack drain(int maxDrain, FluidAction action) {
      if (maxDrain <= 0) {
         return FluidStack.EMPTY;
      } else {
         FluidStack fs = this.tank.drain(maxDrain, action);
         if (!fs.isEmpty() && action == FluidAction.EXECUTE) {
            this.markForUpdate();
            this.setChanged();
         }

         return fs;
      }
   }

   @Nullable
   public IFluidHandler getFluidHandler(@Nullable Direction side) {
      return side != Direction.UP ? this : null;
   }

   @Override
   public void invalidateCaps() {
      super.invalidateCaps();
   }

   @NotNull
   public Component getDisplayName() {
      return Component.empty();
   }

   @Nullable
   public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory inventory, @NotNull Player player) {
      return new SpaMenu(windowId, inventory, this);
   }
}
