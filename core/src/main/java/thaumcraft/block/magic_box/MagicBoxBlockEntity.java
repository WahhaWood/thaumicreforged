package thaumcraft.block.magic_box;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ITickable;
import thaumcraft.init.registry.Config;
import thaumcraft.init.registry.ModBlockEntities;

public class MagicBoxBlockEntity extends BlockEntity implements MenuProvider, ITickable {
   private final MagicBoxBlockEntity.DynamicItemStackHandler itemHandler = new MagicBoxBlockEntity.DynamicItemStackHandler(27);
   private BlockPos masterPos = null;
   private byte sorting = -1;
   private int linkedBoxesCount = 0;
   private int linkTimer = 0;

   public MagicBoxBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.MAGIC_BOX.get(), pos, state);
   }

   @Nullable
   public IItemHandler getItemHandler(@Nullable Direction side) {
      if (!Config.removeBlockMagicBoxFromGame) {
         return (IItemHandler)(this.masterPos != null
               && !this.masterPos.equals(this.worldPosition)
               && this.level != null
               && this.level.getBlockEntity(this.masterPos) instanceof MagicBoxBlockEntity masterBox
            ? masterBox.getItemHandler(side)
            : this.itemHandler);
      } else {
         return null;
      }
   }

   public void invalidateCaps() {
      if (this.level != null) {
         this.level.invalidateCapabilities(this.worldPosition);
      }
   }

   public void setMaster(BlockPos master) {
      this.masterPos = master;
      this.setChanged();
   }

   public void loadAdditional(CompoundTag tag, Provider provider) {
      super.loadAdditional(tag, provider);
      this.itemHandler.deserializeNBT(provider, tag.getCompound("Inventory"));
      this.sorting = tag.getByte("sort");
      if (tag.contains("w_x")) {
         this.masterPos = new BlockPos(tag.getInt("w_x"), tag.getInt("w_y"), tag.getInt("w_z"));
      }
   }

   protected void saveAdditional(CompoundTag tag, Provider provider) {
      super.saveAdditional(tag, provider);
      tag.put("Inventory", this.itemHandler.serializeNBT(provider));
      tag.putByte("sort", this.sorting);
      if (this.masterPos != null) {
         tag.putInt("w_x", this.masterPos.getX());
         tag.putInt("w_y", this.masterPos.getY());
         tag.putInt("w_z", this.masterPos.getZ());
      }
   }

   public Component getDisplayName() {
      return Component.literal("Magic Box");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
      return (AbstractContainerMenu)(this.masterPos != null
            && !this.masterPos.equals(this.worldPosition)
            && this.level != null
            && this.level.getBlockEntity(this.masterPos) instanceof MagicBoxBlockEntity master
         ? master.createMenu(id, inventory, player)
         : new MagicBoxMenu(id, inventory, this.itemHandler, this));
   }

   private class DynamicItemStackHandler extends ItemStackHandler {
      public DynamicItemStackHandler(int size) {
         super(size);
      }

      public void resize(int newSize) {
         if (newSize != this.stacks.size()) {
            List<ItemStack> oldStacks = new ArrayList<>(this.stacks);
            this.stacks = NonNullList.withSize(newSize, ItemStack.EMPTY);

            for (int i = 0; i < Math.min(oldStacks.size(), newSize); i++) {
               this.stacks.set(i, oldStacks.get(i));
            }
         }
      }
   }
}
