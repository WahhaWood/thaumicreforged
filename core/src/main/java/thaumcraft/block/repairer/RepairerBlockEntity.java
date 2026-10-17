package thaumcraft.block.repairer;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ITickable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssenceTransport;
import thaumcraft.api.blockentity.ThaumcraftSideInvBlockEntity;
import thaumcraft.init.registry.ModBlockEntities;

public class RepairerBlockEntity extends ThaumcraftSideInvBlockEntity implements ITickable, IAspectContainer, IEssenceTransport {
   private static final Map<Aspect, Integer> REPAIR_VALUES = new LinkedHashMap<>();
   public int ticksExisted;
   public boolean tookLastTick = true;
   private int damageLastTick;

   public RepairerBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType<?>)ModBlockEntities.REPAIRER.get(), blockPos, blockState, 1);
   }

   private boolean canRepairStack(ItemStack stack) {
      return !stack.isEmpty()
         && stack.isDamageableItem()
         && stack.getMaxDamage() > 0
         && !stack.hasCraftingRemainingItem()
         && stack.getCount() == 1
         && stack.getMaxStackSize() <= 1
         && !(stack.getItem() instanceof BlockItem);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound) {
      super.loadCustom(compound);
      this.loadRepairerData(compound);
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadCustom(compound, provider);
      this.loadRepairerData(compound);
   }

   private void loadRepairerData(@NotNull CompoundTag compound) {
      this.ticksExisted = compound.getInt("ticksExisted");
      this.tookLastTick = compound.getBoolean("tookLastTick");
      this.damageLastTick = compound.getInt("damageLastTick");
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound) {
      super.saveCustom(compound);
      this.saveRepairerData(compound);
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveCustom(compound, provider);
      this.saveRepairerData(compound);
   }

   private void saveRepairerData(@NotNull CompoundTag compound) {
      compound.putInt("ticksExisted", this.ticksExisted);
      compound.putBoolean("tookLastTick", this.tookLastTick);
      compound.putInt("damageLastTick", this.damageLastTick);
   }

   public void setChanged() {
      super.setChanged();
      if (this.level != null) {
         this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
      }
   }

   public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
      return index == 0 && this.canRepairStack(stack) && stack.getDamageValue() > 0;
   }

   public int getMaxStackSize() {
      return 1;
   }

   @Override
   public AspectList getAspects() {
      ItemStack stack = this.getItem(0);
      return stack.isEmpty() ? null : new AspectList().add(Aspect.ENTROPY, stack.getDamageValue());
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

   @Override
   public boolean isConnectable(Direction direction) {
      return direction == this.getHorizontalFacing();
   }

   @Override
   public boolean canInputFrom(Direction direction) {
      return false;
   }

   @Override
   public boolean canOutputTo(Direction direction) {
      return this.isConnectable(direction);
   }

   @Override
   public void setSuction(Aspect aspect, int amount) {
   }

   @Nullable
   @Override
   public Aspect getSuctionType(@Nullable Direction direction) {
      return direction == this.getHorizontalFacing() ? Aspect.TOOL : null;
   }

   @Override
   public int getSuctionAmount(@Nullable Direction direction) {
      return direction == this.getHorizontalFacing() ? 128 : 0;
   }

   @Override
   public int extractEssence(Aspect aspect, int amount, Direction direction) {
      return 0;
   }

   @Override
   public int injectEssence(Aspect aspect, int amount, Direction direction) {
      return 0;
   }

   @Nullable
   @Override
   public Aspect getEssenceType(@Nullable Direction direction) {
      return null;
   }

   @Override
   public int getEssenceAmount(Direction direction) {
      return 0;
   }

   @Override
   public int getMinimumSuction() {
      return 0;
   }

   @Override
   public boolean renderExtendedTube() {
      return false;
   }

   static {
      REPAIR_VALUES.put(Aspect.TOOL, 8);
      REPAIR_VALUES.put(Aspect.CRAFT, 5);
      REPAIR_VALUES.put(Aspect.ORDER, 3);
   }
}
