package thaumicenergistics.block.infusion_inscriber;

import appeng.api.inventories.InternalInventory;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import thaumicenergistics.init.ModBlockEntities;
import thaumicenergistics.item.ItemBrainCore;
import thaumicenergistics.menu.InfusionInscriberMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class InfusionInscriberBlockEntity extends AEBaseInvBlockEntity implements MenuProvider {
   private static final int SLOT_BRAIN_CORE = 0;
   private final AppEngInternalInventory inventory = new AppEngInternalInventory(this, 1);

   public InfusionInscriberBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.INFUSION_INSCRIBER.get(), pos, state);
      this.inventory.setMaxStackSize(0, 1);
      this.inventory.setFilter(new IAEItemFilter() {
         public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return stack.getItem() instanceof ItemBrainCore;
         }
      });
   }

   public InternalInventory getInternalInventory() {
      return this.inventory;
   }

   public void onChangeInventory(InternalInventory inv, int slot) {
      this.setChanged();
   }

   public boolean hasBrainCore() {
      return !this.inventory.getStackInSlot(0).isEmpty();
   }

   public ItemStack removeBrainCore() {
      return this.inventory.extractItem(0, 1, false);
   }

   public boolean insertBrainCore(ItemStack stack) {
      if (this.inventory.isItemValid(0, stack) && !this.hasBrainCore()) {
         ItemStack single = stack.copy();
         single.setCount(1);
         this.inventory.setItemDirect(0, single);
         return true;
      } else {
         return false;
      }
   }

   public Component getDisplayName() {
      return Component.translatable("block.thaumicenergistics.infusion_inscriber");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
      return new InfusionInscriberMenu(containerId, inventory, this);
   }
}
