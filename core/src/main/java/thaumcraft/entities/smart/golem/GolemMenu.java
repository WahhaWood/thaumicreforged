package thaumcraft.entities.smart.golem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.entities.smart.golem.handler.GolemItemHandler;
import thaumcraft.entities.smart.golem.render.screen.SlotGhost;
import thaumcraft.entities.smart.golem.render.screen.SlotGhostFluid;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.item.golem_core.GolemCore;

public class GolemMenu extends AbstractContainerMenu {
   public final EntityGolemBase golem;
   public final GolemItemHandler inventory;
   public int currentScroll = 0;
   public int maxScroll;
   private final GolemMenu.GolemInventoryView golemView;

   public GolemMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
      this(containerId, playerInv, resolveGolem(playerInv.player.level(), buf.readInt()));
   }

   public GolemMenu(int containerId, Inventory playerInv, EntityGolemBase golem) {
      super((MenuType)ModMenuTypes.GOLEM.get(), containerId);
      this.golem = golem;
      this.golem.setupGolemInventory();
      this.inventory = golem.getInventory();
      this.golemView = new GolemMenu.GolemInventoryView();
      if (!this.golem.level().isClientSide) {
         this.golem.paused = true;
      }

      this.maxScroll = Math.max(0, this.inventory.getSlotCount() / 6 - 1);
      this.bindGolemInventory();
      this.bindPlayerInventory(playerInv);
   }

   private static EntityGolemBase resolveGolem(Level level, int entityId) {
      Entity e = level.getEntity(entityId);
      return (EntityGolemBase)e;
   }

   protected void bindGolemInventory() {
      if (this.golem.getCore() != GolemCore.BLANK && this.golem.getCore().hasInventory()) {
         int slots = this.inventory.getSlotCount();

         for (int a = 0; a < Math.min(6, slots); a++) {
            int x = 100 + a / 2 * 28;
            int y = 16 + a % 2 * 31;
            if (this.golem.getCore() == GolemCore.FILL) {
               this.addSlot(new SlotGhost(this.golemView, a, x, y));
            } else if (this.golem.getCore() == GolemCore.LIQUID) {
               this.addSlot(new SlotGhostFluid(this.golemView, a, x, y));
            } else {
               this.addSlot(new SlotGhost(this.golemView, a, x, y, 1));
            }
         }
      }
   }

   protected void bindPlayerInventory(Inventory inv) {
      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         this.addSlot(new Slot(inv, col, 8 + col * 18, 142));
      }
   }

   public void clicked(int index, int button, ClickType clickType, Player player) {
      if (index >= 0 && index < this.slots.size()) {
         Slot slot = this.getSlot(index);
         if (slot instanceof SlotGhost ghost) {
            ItemStack carried = this.getCarried();
            if (carried.isEmpty()) {
               slot.set(ItemStack.EMPTY);
            } else if (ghost.accept(carried)) {
               slot.set(carried);
            }

            this.broadcastChanges();
            return;
         }
      }

      super.clicked(index, button, clickType, player);
   }

   /**
    * TC4 ContainerGolem.enchantItem: 66/67 page the ghost slots, 50-57 flip a toggle, 0..slots-1 step a slot's
    * marker colour down and slots..2*slots-1 step it up (-1 is "any colour").
    */
   @Override
   public boolean clickMenuButton(Player player, int button) {
      if (button == 66 && this.currentScroll > 0) {
         this.currentScroll--;
      }

      if (button == 67 && this.currentScroll < this.maxScroll) {
         this.currentScroll++;
      }

      if (button >= 50 && button <= 57) {
         this.golem.setToggle(button - 50, !this.golem.getToggles()[button - 50]);
      }

      int slots = this.inventory.getSlotCount();
      if (button >= 0 && button < slots) {
         int c = this.golem.getColors(button) - 1;
         if (c < -1) {
            c = 15;
         }

         this.golem.setColors(button, c);
      }

      if (button >= slots && button < slots * 2) {
         int c = this.golem.getColors(button - slots) + 1;
         if (c > 15) {
            c = -1;
         }

         this.golem.setColors(button - slots, c);
      }

      this.golem.level().playSound(null, this.golem.getX(), this.golem.getY(), this.golem.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.NEUTRAL, 0.2F, 0.8F);
      return true;
   }

   public ItemStack quickMoveStack(Player player, int index) {
      boolean changed = false;
      if (index >= 0 && index < this.slots.size()) {
         Slot slot = this.getSlot(index);
         if (slot.container == player.getInventory()) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
               for (Slot candidate : this.slots) {
                  if (candidate instanceof SlotGhost ghost && !ghost.hasItem() && ghost.accept(stack)) {
                     candidate.set(stack);
                     changed = true;
                     break;
                  }
               }
            }
         } else if (slot instanceof SlotGhost) {
            slot.set(ItemStack.EMPTY);
            changed = true;
         }
      }

      if (changed) {
         this.broadcastChanges();
      }

      return ItemStack.EMPTY;
   }

   public boolean stillValid(Player player) {
      if (this.golem != null && this.golem.isAlive()) {
         return !this.golem.getOwnerName().equals(player.getName().getString())
            ? false
            : player.level() == this.golem.level() && player.distanceToSqr(this.golem) <= 64.0;
      } else {
         return false;
      }
   }

   public void removed(Player player) {
      super.removed(player);
      if (!player.level().isClientSide) {
         this.golem.paused = false;
      }
   }

   public int getSlotCount() {
      return this.inventory.getSlotCount();
   }

   public final class GolemInventoryView implements Container {
      public int map(int viewIndex) {
         return viewIndex + GolemMenu.this.currentScroll * 6;
      }

      public int getContainerSize() {
         return Math.min(6, GolemMenu.this.inventory.getSlotCount());
      }

      public boolean isEmpty() {
         for (int i = 0; i < this.getContainerSize(); i++) {
            if (!this.getItem(i).isEmpty()) {
               return false;
            }
         }

         return true;
      }

      public ItemStack getItem(int slot) {
         int idx = this.map(slot);
         return idx >= 0 && idx < GolemMenu.this.inventory.getSlotCount() ? GolemMenu.this.inventory.getStackInSlot(idx) : ItemStack.EMPTY;
      }

      public ItemStack removeItem(int slot, int amount) {
         int idx = this.map(slot);
         return idx >= 0 && idx < GolemMenu.this.inventory.getSlotCount() ? GolemMenu.this.inventory.extractItem(idx, amount, false) : ItemStack.EMPTY;
      }

      public ItemStack removeItemNoUpdate(int slot) {
         int idx = this.map(slot);
         if (idx >= 0 && idx < GolemMenu.this.inventory.getSlotCount()) {
            ItemStack cur = GolemMenu.this.inventory.getStackInSlot(idx);
            GolemMenu.this.inventory.setStackInSlot(idx, ItemStack.EMPTY);
            return cur;
         } else {
            return ItemStack.EMPTY;
         }
      }

      public void setItem(int slot, ItemStack stack) {
         int idx = this.map(slot);
         if (idx >= 0 && idx < GolemMenu.this.inventory.getSlotCount()) {
            GolemMenu.this.inventory.setStackInSlot(idx, stack);
         }
      }

      public void setChanged() {
      }

      public boolean stillValid(Player player) {
         return GolemMenu.this.stillValid(player);
      }

      public void clearContent() {
         for (int i = 0; i < this.getContainerSize(); i++) {
            int idx = this.map(i);
            if (idx >= 0 && idx < GolemMenu.this.inventory.getSlotCount()) {
               GolemMenu.this.inventory.setStackInSlot(idx, ItemStack.EMPTY);
            }
         }
      }
   }
}
