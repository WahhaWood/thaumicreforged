package thaumcraft.entities.smart.pech;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.util.ItemStackData;

public class PechMenu extends AbstractContainerMenu {
   private final EntityPech pech;
   private final InventoryPech inventory;
   private final Player player;
   private final Level level;

   public PechMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
      this(containerId, playerInv, getPech(playerInv.player.level(), buf.readInt()));
   }

   public PechMenu(int containerId, Inventory playerInv, EntityPech pech) {
      super((MenuType)ModMenuTypes.PECH.get(), containerId);
      this.pech = pech;
      this.level = playerInv.player.level();
      this.player = playerInv.player;
      this.inventory = new InventoryPech(playerInv.player, pech, this);
      if (this.pech != null) {
         this.pech.trading = true;
      }

      this.addSlot(new Slot(this.inventory, 0, 36, 29));

      for (int i = 0; i < 2; i++) {
         for (int j = 0; j < 2; j++) {
            this.addSlot(new SlotOutput(this.inventory, 1 + j + i * 2, 106 + 18 * j, 20 + 18 * i));
         }
      }

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.addSlot(new Slot(playerInv, i, 8 + i * 18, 142));
      }
   }

   public InventoryPech getMerchantInventory() {
      return this.inventory;
   }

   public boolean stillValid(Player player) {
      return this.pech != null && this.pech.isAlive() && this.pech.isTamed() && this.pech.distanceTo(player) < 8.0F;
   }

   public boolean isReadyToTrade() {
      if (this.pech == null) {
         return false;
      } else {
         ItemStack in = this.inventory.getItem(0);
         if (in.isEmpty()) {
            return false;
         } else if (!this.pech.isValued(in)) {
            return false;
         } else {
            for (int i = 1; i <= 4; i++) {
               if (!this.inventory.getItem(i).isEmpty()) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   /** TC4 ContainerPech.enchantItem: button 0 is the trade button. */
   @Override
   public boolean clickMenuButton(Player player, int id) {
      if (id == 0) {
         this.generateContents();
         return true;
      } else {
         return super.clickMenuButton(player, id);
      }
   }

   /**
    * TC4 ContainerPech.generateContents: the offered item's value is spent on goods from the pech's trade list,
    * single items from its pack and rare dungeon loot. The pech may lose interest (untame) with value/2 percent.
    */
   private void generateContents() {
      if (this.level.isClientSide || !this.isReadyToTrade()) {
         return;
      }

      RandomSource random = this.level.random;
      int value = this.pech.getValue(this.inventory.getItem(0));
      if (random.nextInt(100) <= value / 2) {
         this.pech.setTamed(false);
         this.pech.updateAINextTick = true;
         this.pech.playSound(ModSounds.PECH_TRADE.get(), 0.4F, 1.0F);
      }

      if (random.nextInt(5) == 0) {
         value += random.nextInt(3);
      } else if (random.nextBoolean()) {
         value -= random.nextInt(3);
      }

      List<List<?>> trades = EntityPech.tradeInventory.get(this.pech.getPechType());
      while (value > 0) {
         int am = Math.min(5, Math.max((value + 1) / 2, random.nextInt(value) + 1));
         value -= am;
         if (am == 1 && random.nextBoolean() && this.hasStuffInPack()) {
            List<Integer> loot = new ArrayList<>();
            for (int a = 0; a < this.pech.loot.length; a++) {
               if (this.pech.loot[a] != null && !this.pech.loot[a].isEmpty()) {
                  loot.add(a);
               }
            }

            int r = loot.get(random.nextInt(loot.size()));
            this.moveTradeStackToOutputs(this.pech.loot[r].copyWithCount(1));
            this.pech.loot[r].shrink(1);
            if (this.pech.loot[r].isEmpty()) {
               this.pech.loot[r] = ItemStack.EMPTY;
            }
         } else if (am >= 4 && random.nextBoolean()) {
            ItemStack rare = this.rareDungeonLoot();
            if (rare.isEmpty()) {
               value += am;
            } else {
               rare.onCraftedBy(this.level, this.player, 0);
               this.moveTradeStackToOutputs(rare);
            }
         } else {
            int price = am;
            List<List<?>> offers = trades.stream().filter(t -> (Integer)t.get(0) == price).toList();
            if (offers.isEmpty()) {
               continue;
            }

            ItemStack is = ((ItemStack)offers.get(random.nextInt(offers.size())).get(1)).copy();
            is.onCraftedBy(this.level, this.player, 0);
            this.moveTradeStackToOutputs(is);
         }
      }

      this.inventory.removeItem(0, 1);
   }

   /**
    * TC4 drew a dungeonChest entry of weight 5 or less and at most one item (records, horse armour, enchanted books).
    * 1.21 has no weighted chest list, so roll the simple dungeon loot table and keep an unstackable item.
    */
   private ItemStack rareDungeonLoot() {
      if (!(this.level instanceof ServerLevel serverLevel)) {
         return ItemStack.EMPTY;
      }

      LootTable table = serverLevel.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
      LootParams params = new LootParams.Builder(serverLevel)
         .withParameter(LootContextParams.ORIGIN, this.pech.position())
         .create(LootContextParamSets.CHEST);
      List<ItemStack> rare = table.getRandomItems(params).stream().filter(st -> st.getMaxStackSize() == 1).toList();
      return rare.isEmpty() ? ItemStack.EMPTY : rare.get(this.level.random.nextInt(rare.size())).copy();
   }

   private boolean hasStuffInPack() {
      if (this.pech == null) {
         return false;
      } else {
         for (ItemStack stack : this.pech.loot) {
            if (stack != null && !stack.isEmpty()) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean moveTradeStackToOutputs(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         for (int i = 1; i <= 4; i++) {
            Slot slot = (Slot)this.slots.get(i);
            ItemStack existing = slot.getItem();
            if (existing.isEmpty()) {
               slot.set(stack.copy());
               slot.setChanged();
               stack.setCount(0);
               return true;
            }

            if (ItemStackData.sameItemSameTags(existing, stack)) {
               int maxStackSize = Math.min(slot.getMaxStackSize(stack), stack.getMaxStackSize());
               int space = maxStackSize - existing.getCount();
               if (space > 0) {
                  int moved = Math.min(space, stack.getCount());
                  existing.grow(moved);
                  stack.shrink(moved);
                  slot.setChanged();
                  if (stack.isEmpty()) {
                     return true;
                  }
               }
            }
         }

         return stack.isEmpty();
      }
   }

   public ItemStack quickMoveStack(Player player, int index) {
      ItemStack ret = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot.hasItem()) {
         ItemStack stack = slot.getItem();
         ret = stack.copy();
         if (index == 0) {
            if (!this.moveItemStackTo(stack, 5, 41, true)) {
               return ItemStack.EMPTY;
            }
         } else if (index >= 1 && index < 5) {
            if (!this.moveItemStackTo(stack, 5, 41, true)) {
               return ItemStack.EMPTY;
            }
         } else if (index >= 5 && index < 41 && !this.moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (stack.getCount() == ret.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, stack);
      }

      return ret;
   }

   public void removed(Player player) {
      super.removed(player);
      if (this.pech != null) {
         this.pech.trading = false;
      }

      if (!this.level.isClientSide) {
         for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
               ItemEntity dropped = player.drop(stack, false);
               if (dropped != null) {
                  dropped.getPersistentData().putBoolean("thaumcraft.pech_drop", true);
               }
            }
         }
      }
   }

   private static EntityPech getPech(Level level, int id) {
      return level.getEntity(id) instanceof EntityPech pech ? pech : null;
   }
}
