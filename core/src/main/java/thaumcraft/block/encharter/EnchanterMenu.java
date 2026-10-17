package thaumcraft.block.encharter;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.enchantment.EnchantmentManager;
import thaumcraft.init.registry.ModItems;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.item.wands.ItemWandCasting;

public class EnchanterMenu extends AbstractContainerMenu {
   private static final int START_BUTTON_ID = 0;
   public static final int MAX_ADD_CHOICES = 16;
   public static final int MAX_SELECTED_ENCHANTMENTS = 36;
   private static final int ADD_ENCHANT_BASE = 1;
   private static final int REMOVE_INDEX_BASE = 17;
   private static final int DEC_LEVEL_INDEX_BASE = 53;
   private static final int INC_LEVEL_INDEX_BASE = 89;
   private static final int INC_LEVEL_END_EXCLUSIVE = 125;
   public final EnchanterBlockEntity enchanter;

   public EnchanterMenu(int windowId, Inventory inventory, FriendlyByteBuf buf) {
      this(windowId, inventory, getEnchanter(inventory, buf));
   }

   private static EnchanterBlockEntity getEnchanter(Inventory inventory, FriendlyByteBuf buf) {
      BlockPos pos = buf.readBlockPos();
      if (inventory.player.level().getBlockEntity(pos) instanceof EnchanterBlockEntity enchanter) {
         return enchanter;
      } else {
         throw new IllegalStateException("Enchanter BE not found at " + pos);
      }
   }

   public EnchanterMenu(int windowId, Inventory inventory, final EnchanterBlockEntity enchanter) {
      super((MenuType)ModMenuTypes.ENCHANTER.get(), windowId);
      this.enchanter = enchanter;
      this.addSlot(new Slot(enchanter, 0, 6, 6) {
         public boolean mayPlace(@NotNull ItemStack stack) {
            return !enchanter.working && !stack.is((Item)ModItems.SPELL_CLOTH.get()) && stack.getItem().isEnchantable(stack) && stack.getCount() == 1;
         }

         public int getMaxStackSize() {
            return 1;
         }

         public boolean mayPickup(Player player) {
            return !enchanter.working;
         }
      });
      this.addSlot(
         new Slot(enchanter, 1, 6, 31) {
            public boolean mayPlace(@NotNull ItemStack stack) {
               return !enchanter.working && stack.getItem() instanceof ItemWandCasting wand && !wand.isStaff(stack)
                  ? wand.getCap(stack).getBasicDiscount() <= 1.0F
                  : false;
            }

            public int getMaxStackSize() {
               return 1;
            }
         }
      );

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
         }
      }

      for (int hotbar = 0; hotbar < 9; hotbar++) {
         this.addSlot(new Slot(inventory, hotbar, 8 + hotbar * 18, 142));
      }

      this.addEnchanterDataSlots();
   }

   private void addEnchanterDataSlots() {
      this.addDataSlot(new DataSlot() {
         public int get() {
            return EnchanterMenu.this.enchanter.working ? 1 : 0;
         }

         public void set(int value) {
            EnchanterMenu.this.enchanter.working = value != 0;
         }
      });

      for (Aspect aspect : Aspect.getPrimalAspects()) {
         this.addAspectDataSlot(aspect, true);
         this.addAspectDataSlot(aspect, false);
      }
   }

   private void addAspectDataSlot(final Aspect aspect, final boolean current) {
      this.addDataSlot(new DataSlot() {
         public int get() {
            return current ? EnchanterMenu.this.enchanter.currentAspects.getAmount(aspect) : EnchanterMenu.this.enchanter.totalAspects.getAmount(aspect);
         }

         public void set(int value) {
            if (current) {
               EnchanterMenu.this.enchanter.currentAspects.setAmount(aspect, value);
            } else {
               EnchanterMenu.this.enchanter.totalAspects.setAmount(aspect, value);
            }
         }
      });
   }

   private boolean removeEnchantAt(int index) {
      if (index >= 0 && index < this.enchanter.enchantments.size() && index < this.enchanter.levels.size()) {
         this.enchanter.enchantments.remove(index);
         this.enchanter.levels.remove(index);
         this.enchanter.updateAspectList();
         return true;
      } else {
         return false;
      }
   }

   private boolean setLevelAt(int index, int level, boolean removeWhenOne) {
      if (index >= 0 && index < this.enchanter.enchantments.size() && index < this.enchanter.levels.size()) {
         ResourceLocation enchantmentId = this.enchanter.enchantments.get(index);
         Holder<Enchantment> enchantment = this.getEnchantment(null, enchantmentId);
         if (enchantment == null) {
            return false;
         } else if (removeWhenOne && level < 1) {
            return this.removeEnchantAt(index);
         } else {
            int clamped = Math.max(1, Math.min(((Enchantment)enchantment.value()).getMaxLevel(), level));
            this.enchanter.levels.set(index, clamped);
            this.enchanter.updateAspectList();
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean stillValid(@NotNull Player player) {
      return this.enchanter.stillValid(player);
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int index) {
      ItemStack copy = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot.hasItem()) {
         ItemStack stack = slot.getItem();
         if (stack.is(Items.BOOK)) {
            return ItemStack.EMPTY;
         }

         copy = stack.copy();
         if (index < 2) {
            if (!this.moveItemStackTo(stack, 2, 38, false)) {
               return ItemStack.EMPTY;
            }
         } else {
            boolean isWand = ((Slot)this.slots.get(1)).mayPlace(stack);
            boolean isTool = ((Slot)this.slots.get(0)).mayPlace(stack);
            if (isWand) {
               if (!this.moveItemStackTo(stack, 1, 2, false)) {
                  return ItemStack.EMPTY;
               }
            } else {
               if (!isTool) {
                  return ItemStack.EMPTY;
               }

               if (!this.moveItemStackTo(stack, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            }
         }

         if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, stack);
      }

      return copy;
   }

   @Override
   public boolean clickMenuButton(@NotNull Player player, int id) {
      if (this.enchanter.working) {
         return false;
      }
      if (id == startButtonId()) {
         if (!this.enchanter.enchantments.isEmpty() && !this.enchanter.levels.isEmpty()) {
            this.enchanter.working = true;
            this.enchanter.markForUpdate();
            this.enchanter.setChanged();
            return true;
         }
         return false;
      }
      int addIndex = getAddEnchantIndex(id);
      if (addIndex >= 0) {
         List<ResourceLocation> choices = this.getAvailableEnchantmentChoices(player);
         if (addIndex < choices.size() && this.enchanter.enchantments.size() < 36) {
            ResourceLocation enchantmentId = choices.get(addIndex);
            if (!this.enchanter.enchantments.contains(enchantmentId)) {
               this.enchanter.enchantments.add(enchantmentId);
               this.enchanter.levels.add(1);
               this.enchanter.updateAspectList();
               return true;
            }
         }
         return false;
      }
      int removeIndex = getRemoveEnchantIndex(id);
      if (removeIndex >= 0) {
         return this.removeEnchantAt(removeIndex);
      }
      int lowerLevelIndex = getLowerLevelIndex(id);
      if (lowerLevelIndex >= 0) {
         if (lowerLevelIndex < this.enchanter.levels.size()) {
            return this.setLevelAt(lowerLevelIndex, this.enchanter.levels.get(lowerLevelIndex) - 1, true);
         }
         return false;
      }
      int raiseLevelIndex = getRaiseLevelIndex(id);
      if (raiseLevelIndex >= 0) {
         if (raiseLevelIndex < this.enchanter.levels.size()) {
            return this.setLevelAt(raiseLevelIndex, this.enchanter.levels.get(raiseLevelIndex) + 1, false);
         }
         return false;
      }
      return false;
   }

   public static int startButtonId() {
      return 0;
   }

   public static int addEnchantButtonId(int choiceIndex) {
      return choiceIndex >= 0 && choiceIndex < 16 ? 1 + choiceIndex : -1;
   }

   public static int removeEnchantButtonId(int index) {
      return index >= 0 && index < 36 ? 17 + index : -1;
   }

   public static int lowerLevelButtonId(int index) {
      return index >= 0 && index < 36 ? 53 + index : -1;
   }

   public static int raiseLevelButtonId(int index) {
      return index >= 0 && index < 36 ? 89 + index : -1;
   }

   public static int getAddEnchantIndex(int id) {
      return id >= 1 && id < 17 ? id - 1 : -1;
   }

   public static int getRemoveEnchantIndex(int id) {
      return id >= 17 && id < 53 ? id - 17 : -1;
   }

   public static int getLowerLevelIndex(int id) {
      return id >= 53 && id < 89 ? id - 53 : -1;
   }

   public static int getRaiseLevelIndex(int id) {
      return id >= 89 && id < 125 ? id - 89 : -1;
   }

   private List<ResourceLocation> getAvailableEnchantmentChoices(Player player) {
      ItemStack stack = this.enchanter.getItem(0);
      if (!isValidEnchantTarget(stack)) {
         return List.of();
      } else {
         List<ResourceLocation> choices = new ArrayList<>(16);

         for (ResourceLocation enchantmentId : EnchantmentManager.getSortedEnchantmentIds()) {
            Holder<Enchantment> enchantment = this.getEnchantment(player, enchantmentId);
            if (enchantment != null
               && EnchantmentManager.canApply(stack, enchantment, this.enchanter.enchantments, player.registryAccess())
               && EnchantmentManager.canEnchantmentBeUsed(player, enchantment)) {
               choices.add(enchantmentId);
               if (choices.size() >= 16) {
                  break;
               }
            }
         }

         return choices;
      }
   }

   private Holder<Enchantment> getEnchantment(Player player, ResourceLocation id) {
      if (player == null && this.enchanter.getLevel() == null) {
         return null;
      } else {
         RegistryAccess registries = player != null ? player.registryAccess() : this.enchanter.getLevel().registryAccess();
         ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);
         return (Holder<Enchantment>)registries.lookupOrThrow(Registries.ENCHANTMENT).get(key).orElse(null);
      }
   }

   private static boolean isValidEnchantTarget(ItemStack stack) {
      return !stack.isEmpty() && !stack.isEnchanted() && stack.getCount() == 1 && stack.getItem().isEnchantable(stack);
   }
}
