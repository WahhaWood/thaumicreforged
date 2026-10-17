package thaumcraft.item.armor.hover_harness;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.block.jar.standart.JarBlockItem;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.util.ItemStackData;

public class HoverHarnessMenu extends AbstractContainerMenu {
   private static final String NBT_KEY_UID = "UID";
   private static final String NBT_KEY_JAR = "jar";
   public final SimpleContainer input = new SimpleContainer(1) {
      public void setChanged() {
         super.setChanged();
         HoverHarnessMenu.this.slotsChanged(this);
      }
   };
   private final ItemStack armor;
   private final int blockedHandSlot;

   public HoverHarnessMenu(int containerId, Inventory inv, FriendlyByteBuf buf) {
      this(containerId, inv, inv.player.level(), buf.readBlockPos());
   }

   public HoverHarnessMenu(int containerId, Inventory inv, Level level, BlockPos pos) {
      super((MenuType)ModMenuTypes.HOVER_HARNESS.get(), containerId);
      this.blockedHandSlot = inv.selected;
      this.armor = inv.getSelected();
      if (!this.armor.isEmpty() && this.armor.getItem() instanceof ItemHoverHarness) {
         CompoundTag nbt = ItemStackData.getOrCreateTag(this.armor);
         if (!nbt.contains("UID")) {
            nbt.putString("UID", UUID.randomUUID().toString());
            ItemStackData.setTag(this.armor, nbt);
         }

         if (nbt.contains("jar")) {
            this.input.setItem(0, ItemStackData.loadStack(nbt.getCompound("jar")));
         }
      }

      this.addSlot(new Slot(this.input, 0, 80, 32) {
         public boolean mayPlace(ItemStack stack) {
            return HoverHarnessMenu.isValidJar(stack);
         }

         public int getMaxStackSize() {
            return 1;
         }
      });
      this.bindPlayerInventory(inv);
   }

   public static boolean isValidJar(ItemStack stack) {
      if (stack.isEmpty() || !(stack.getItem() instanceof JarBlockItem)) {
         return false;
      } else if (!ItemStackData.hasTag(stack)) {
         return false;
      } else {
         AspectList aspects = ((JarBlockItem)stack.getItem()).getAspects(stack);
         return aspects != null && aspects.size() > 0 && aspects.getAmount(Aspect.ENERGY) > 0;
      }
   }

   public void slotsChanged(Container container) {
      if (container == this.input && !this.armor.isEmpty()) {
         ItemStack jar = this.input.getItem(0);
         new CompoundTag();
         if (!jar.isEmpty()) {
            CompoundTag jarTag = ItemStackData.saveStack(jar);
            CompoundTag tag = ItemStackData.getOrCreateTag(this.armor);
            tag.put("jar", jarTag);
            ItemStackData.setTag(this.armor, tag);
         } else {
            CompoundTag tag = ItemStackData.getOrCreateTag(this.armor);
            tag.remove("jar");
            ItemStackData.setTag(this.armor, tag);
         }
      }
   }

   protected void bindPlayerInventory(Inventory inv) {
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         if (i == this.blockedHandSlot) {
            this.addSlot(new Slot(inv, i, 8 + i * 18, 142) {
               public boolean mayPickup(Player player) {
                  return false;
               }

               public boolean mayPlace(ItemStack stack) {
                  return false;
               }
            });
         } else {
            this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
         }
      }
   }

   public boolean stillValid(Player player) {
      return player.isAlive() && this.blockedHandSlot == player.getInventory().selected && isSameItemInventory(this.armor, player.getInventory().getSelected());
   }

   private static boolean isSameItemInventory(ItemStack base, ItemStack comparison) {
      if (!base.isEmpty() && !comparison.isEmpty()) {
         if (base.getItem() != comparison.getItem()) {
            return false;
         } else if (ItemStackData.sameItemSameTags(base, comparison)) {
            return true;
         } else {
            CompoundTag baseTag = ItemStackData.getTag(base);
            CompoundTag compTag = ItemStackData.getTag(comparison);
            if (baseTag != null && compTag != null) {
               String baseUID = baseTag.getString("UID");
               String compUID = compTag.getString("UID");
               return !baseUID.isEmpty() && baseUID.equals(compUID);
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   public ItemStack quickMoveStack(Player player, int index) {
      ItemStack copy = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack stackInSlot = slot.getItem();
         copy = stackInSlot.copy();
         if (index == 0) {
            if (!this.moveItemStackTo(stackInSlot, 1, 37, true)) {
               return ItemStack.EMPTY;
            }
         } else if (isValidJar(stackInSlot)) {
            if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (index < 28) {
            if (!this.moveItemStackTo(stackInSlot, 28, 37, false)) {
               return ItemStack.EMPTY;
            }
         } else if (index < 37 && !this.moveItemStackTo(stackInSlot, 1, 28, false)) {
            return ItemStack.EMPTY;
         }

         if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (stackInSlot.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, stackInSlot);
      }

      return copy;
   }
}
