package thaumcraft.block.magnet;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.init.registry.ModBlockEntities;
import thaumcraft.item.soul_mould.ItemSoulMould;
import thaumcraft.util.BlockEntityMenuValidator;

public class MobMagnetBlockEntity extends MagnetBlockEntity implements Container, MenuProvider {
   private static final String TAG_ADULT = "adult";
   private final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
   private boolean adult = true;

   public MobMagnetBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ModBlockEntities.MOB_MAGNET.get(), pos, state);
   }

   public boolean isAdultMode() {
      return this.adult;
   }

   public void setAdultMode(boolean adult) {
      this.adult = adult;
      this.markForUpdate();
      this.setChanged();
   }

   @Override
   protected Predicate<Entity> getEntitySelector() {
      return entity -> {
         if (entity instanceof LivingEntity living && !(entity instanceof Player)) {
            boolean canPass = !(living instanceof AgeableMob ageable && this.adult == ageable.isBaby());
            if (!canPass) {
               return false;
            } else {
               ItemStack filterStack = (ItemStack)this.inventory.get(0);
               if (filterStack.isEmpty()) {
                  return true;
               } else {
                  String pattern = ItemSoulMould.getPatternName(filterStack);
                  return pattern.isEmpty() ? true : entity.getType().builtInRegistryHolder().key().location().toString().equals(pattern);
               }
            }
         } else {
            return false;
         }
      };
   }

   @Override
   public void loadCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.loadCustom(compound, provider);
      this.adult = compound.getBoolean("adult");
      ContainerHelper.loadAllItems(compound, this.inventory, provider);
   }

   @Override
   public void saveCustom(@NotNull CompoundTag compound, @NotNull Provider provider) {
      super.saveCustom(compound, provider);
      compound.putBoolean("adult", this.adult);
      ContainerHelper.saveAllItems(compound, this.inventory, provider);
   }

   public int getContainerSize() {
      return this.inventory.size();
   }

   public boolean isEmpty() {
      for (ItemStack stack : this.inventory) {
         if (!stack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   @NotNull
   public ItemStack getItem(int slot) {
      return (ItemStack)this.inventory.get(slot);
   }

   @NotNull
   public ItemStack removeItem(int slot, int amount) {
      ItemStack stack = ContainerHelper.removeItem(this.inventory, slot, amount);
      if (!stack.isEmpty()) {
         this.setChanged();
      }

      return stack;
   }

   @NotNull
   public ItemStack removeItemNoUpdate(int slot) {
      return ContainerHelper.takeItem(this.inventory, slot);
   }

   public void setItem(int slot, @NotNull ItemStack stack) {
      this.inventory.set(slot, stack);
      if (stack.getCount() > this.getMaxStackSize()) {
         stack.setCount(this.getMaxStackSize());
      }

      this.setChanged();
   }

   public boolean stillValid(@NotNull Player player) {
      return BlockEntityMenuValidator.isValid(player, this);
   }

   public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
      return stack.getItem() instanceof ItemSoulMould;
   }

   public void clearContent() {
      for (int i = 0; i < this.inventory.size(); i++) {
         this.inventory.set(i, ItemStack.EMPTY);
      }
   }

   @NotNull
   public Component getDisplayName() {
      return Component.translatable("block.thaumcraft.mob_magnet");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player player) {
      return new MobMagnetMenu(id, playerInventory, this);
   }
}
