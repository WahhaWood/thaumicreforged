package thaumcraft.block.tube.filter;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.block.tube.TubeBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class TileTubeFilter extends TubeBlockEntity implements IAspectContainer {
   @Nullable
   public Aspect aspectFilter = null;

   public TileTubeFilter(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.TUBE_FILTER.get(), blockPos, blockState);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      super.loadCustom(compound);
      this.aspectFilter = Aspect.getAspect(compound.getString("AspectFilter"));
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      super.saveCustom(compound);
      if (this.aspectFilter != null) {
         compound.putString("AspectFilter", this.aspectFilter.getTag());
      }
   }

   /** TC4 TileTubeFilter: suction only for the filter's aspect. */
   @Override
   protected void calculateSuction(@Nullable Aspect filter, boolean restrict, boolean directional) {
      super.calculateSuction(this.aspectFilter, restrict, directional);
   }

   public void setFilter(@Nullable Aspect aspect) {
      this.aspectFilter = aspect;
      this.setChanged();
      this.markForUpdate();
   }

   public void clearFilter() {
      this.aspectFilter = null;
      this.setChanged();
      this.markForUpdate();
   }

   @Override
   public AspectList getAspects() {
      return this.aspectFilter != null ? new AspectList().add(this.aspectFilter, -1) : null;
   }

   @Override
   public void setAspects(AspectList aspectList) {
   }

   @Override
   public boolean doesContainerAccept(Aspect aspect) {
      return false;
   }

   @Override
   public int addToContainer(Aspect aspect, int amount) {
      return 0;
   }

   @Override
   public boolean takeFromContainer(Aspect aspect, int amount) {
      return false;
   }

   @Override
   public boolean takeFromContainer(AspectList aspectList) {
      return false;
   }

   @Override
   public boolean doesContainerContainAmount(Aspect aspect, int amount) {
      return false;
   }

   @Override
   public boolean doesContainerContain(AspectList aspectList) {
      return false;
   }

   @Override
   public int containerContains(Aspect aspect) {
      return 0;
   }
}
