package thaumcraft.entities.smart.golem.handler;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class GolemFluidHandler implements IFluidHandler {
   private final FluidTank tank;
   private Runnable onChanged;

   public GolemFluidHandler() {
      this(1000);
   }

   public GolemFluidHandler(int capacity) {
      this.tank = new FluidTank(capacity) {
         protected void onContentsChanged() {
            if (GolemFluidHandler.this.onChanged != null) {
               GolemFluidHandler.this.onChanged.run();
            }
         }
      };
   }

   public GolemFluidHandler(int capacity, Predicate<FluidStack> validator) {
      this.tank = new FluidTank(capacity, validator) {
         protected void onContentsChanged() {
            if (GolemFluidHandler.this.onChanged != null) {
               GolemFluidHandler.this.onChanged.run();
            }
         }
      };
   }

   public void setOnChanged(Runnable callback) {
      this.onChanged = callback;
   }

   public void setCapacity(int capacity) {
      this.tank.setCapacity(capacity);
      this.notifyChanged();
   }

   public int getTanks() {
      return 1;
   }

   @Nonnull
   public FluidStack getFluidInTank(int tank) {
      return tank == 0 ? this.tank.getFluid() : FluidStack.EMPTY;
   }

   public int getTankCapacity(int tank) {
      return tank == 0 ? this.tank.getCapacity() : 0;
   }

   public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
      return tank == 0 && this.tank.isFluidValid(stack);
   }

   public int fill(FluidStack resource, FluidAction action) {
      return this.tank.fill(resource, action);
   }

   @Nonnull
   public FluidStack drain(FluidStack resource, FluidAction action) {
      return this.tank.drain(resource, action);
   }

   @Nonnull
   public FluidStack drain(int maxDrain, FluidAction action) {
      return this.tank.drain(maxDrain, action);
   }

   public FluidStack getCarriedFluid() {
      return this.tank.getFluid();
   }

   public void setCarriedFluid(FluidStack fluid) {
      this.tank.setFluid(fluid == null ? FluidStack.EMPTY : fluid.copy());
      this.notifyChanged();
   }

   public boolean hasFluid() {
      return !this.tank.isEmpty();
   }

   public boolean isEmpty() {
      return this.tank.isEmpty();
   }

   public boolean isFull() {
      return this.tank.getFluidAmount() >= this.tank.getCapacity();
   }

   public int getFluidAmount() {
      return this.tank.getFluidAmount();
   }

   public int getCapacity() {
      return this.tank.getCapacity();
   }

   public int getRemainingSpace() {
      return this.tank.getCapacity() - this.tank.getFluidAmount();
   }

   public boolean canAccept(FluidStack fluid) {
      if (fluid.isEmpty()) {
         return true;
      } else {
         return this.tank.isEmpty() ? this.tank.isFluidValid(fluid) : this.tank.getFluid().isFluidEqual(fluid) && this.tank.isFluidValid(fluid);
      }
   }

   public FluidStack clearFluid() {
      return this.tank.drain(Integer.MAX_VALUE, FluidAction.EXECUTE);
   }

   public CompoundTag serializeNBT() {
      return this.tank.writeToNBT(registryAccess(), new CompoundTag());
   }

   public void deserializeNBT(CompoundTag nbt) {
      this.tank.readFromNBT(registryAccess(), nbt);
      this.notifyChanged();
   }

   public FluidTank getTank() {
      return this.tank;
   }

   private void notifyChanged() {
      if (this.onChanged != null) {
         this.onChanged.run();
      }
   }

   private static Provider registryAccess() {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      return server == null ? RegistryAccess.EMPTY : server.registryAccess();
   }
}
