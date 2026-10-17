package thaumicenergistics.menu.slot;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.helpers.ICraftingGridMenu;
import appeng.helpers.InventoryAction;
import appeng.menu.slot.CraftingTermSlot;
import appeng.util.Platform;
import appeng.util.inv.CarriedItemInventory;
import appeng.util.inv.PlayerInternalInventory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.menu.ArcaneCraftingTermMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.CraftingInput.Positioned;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.util.ItemStackData;

public class ArcaneCraftingTermSlot extends CraftingTermSlot {
   private final InternalInventory wandInv;
   private final ArcaneCraftingTermMenu arcaneMenu;
   private final InternalInventory craftInv;
   private final InternalInventory pattern;
   private final IActionSource mySrc;
   private final IEnergySource energySrc;
   private final MEStorage storage;
   private final ICraftingGridMenu menu;
   private final Player player;

   public ArcaneCraftingTermSlot(
      Player player,
      IActionSource mySrc,
      IEnergySource energySrc,
      MEStorage storage,
      InternalInventory cMatrix,
      InternalInventory secondMatrix,
      InternalInventory wandInv,
      ICraftingGridMenu ccp
   ) {
      super(player, mySrc, energySrc, storage, cMatrix, secondMatrix, ccp);
      this.player = player;
      this.wandInv = wandInv;
      this.arcaneMenu = ccp instanceof ArcaneCraftingTermMenu ? (ArcaneCraftingTermMenu)ccp : null;
      this.craftInv = secondMatrix;
      this.pattern = cMatrix;
      this.mySrc = mySrc;
      this.energySrc = energySrc;
      this.storage = storage;
      this.menu = ccp;
   }

   protected RecipeHolder<CraftingRecipe> findRecipe(CraftingInput ic, Level level) {
      if (this.arcaneMenu != null && this.arcaneMenu.getArcaneRecipe() != null) {
         IArcaneRecipe arcaneRecipe = this.arcaneMenu.getArcaneRecipe();
         if (arcaneRecipe.matches(ic, level)) {
            return new RecipeHolder(ThaumicEnergistics.location("arcane_terminal_dynamic"), arcaneRecipe);
         }
      }

      return (RecipeHolder<CraftingRecipe>)level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, ic, level).orElse(null);
   }

   protected NonNullList<ItemStack> getRemainingItems(CraftingInput ic, Level level) {
      RecipeHolder<CraftingRecipe> recipe = this.findRecipe(ic, level);
      return recipe != null && ((CraftingRecipe)recipe.value()).matches(ic, level)
         ? ((CraftingRecipe)recipe.value()).getRemainingItems(ic)
         : super.getRemainingItems(ic, level);
   }

   public void doClick(InventoryAction action, Player who) {
      if (!this.getItem().isEmpty()) {
         if (!this.isRemote()) {
            int howManyPerCraft = this.getItem().getCount();
            int maxTimesToCraft;
            InternalInventory target;
            if (action == InventoryAction.CRAFT_SHIFT) {
               target = new PlayerInternalInventory(who.getInventory());
               maxTimesToCraft = (int)Math.floor((double)this.getItem().getMaxStackSize() / (double)howManyPerCraft);
            } else if (action == InventoryAction.CRAFT_STACK) {
               target = new CarriedItemInventory(this.getMenu());
               maxTimesToCraft = (int)Math.floor((double)this.getItem().getMaxStackSize() / (double)howManyPerCraft);
            } else {
               if (this.getMenu().getCarried().isEmpty()) {
                  this.getMenu().setCarried(this.craftItem(who, this.storage, this.storage.getAvailableStacks()));
                  return;
               }

               target = new CarriedItemInventory(this.getMenu());
               maxTimesToCraft = 1;
            }

            ItemStack itemAtStart = this.getItem().copy();
            if (!itemAtStart.isEmpty()) {
               for (int x = 0; x < maxTimesToCraft; x++) {
                  if (!ItemStackData.sameItemSameTags(itemAtStart, this.getItem())) {
                     return;
                  }

                  if (!target.simulateAdd(itemAtStart).isEmpty()) {
                     return;
                  }

                  if (!this.canConsumeVisForNextCraft(who)) {
                     return;
                  }

                  KeyCounter all = this.storage.getAvailableStacks();
                  ItemStack crafted = this.craftItem(who, this.storage, all);
                  if (crafted.isEmpty()) {
                     return;
                  }

                  ItemStack extra = target.addItems(crafted);
                  if (!extra.isEmpty()) {
                     Platform.spawnDrops(who.level(), who.blockPosition(), List.of(extra));
                     return;
                  }
               }
            }
         }
      }
   }

   private ItemStack craftItem(Player p, MEStorage inv, KeyCounter all) {
      ItemStack is = this.getItem().copy();
      if (is.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack[] set = new ItemStack[this.pattern.size()];
         Arrays.fill(set, ItemStack.EMPTY);
         Level level = p.level();
         if (!level.isClientSide()) {
            CraftingInput ic = createCraftingInput(this.pattern);
            RecipeHolder<CraftingRecipe> r = this.findRecipe(ic, level);
            if (r == null) {
               Item target = is.getItem();
               if (is.isDamageableItem() && target.isValidRepairItem(is, is)) {
                  boolean isBad = false;

                  for (int x = 0; x < ic.size(); x++) {
                     ItemStack pis = ic.getItem(x);
                     if (!pis.isEmpty() && pis.getItem() != target) {
                        isBad = true;
                     }
                  }

                  if (!isBad) {
                     this.makeItem(p, is);
                     p.containerMenu.slotsChanged(this.craftInv.toContainer());
                     return is;
                  }
               }

               return ItemStack.EMPTY;
            }

            is = ((CraftingRecipe)r.value()).assemble(ic, level.registryAccess());
            if (!this.consumeVisForRecipe(p, (CraftingRecipe)r.value())) {
               return ItemStack.EMPTY;
            }

            if (inv != null) {
               for (int xx = 0; xx < this.pattern.size(); xx++) {
                  if (!this.pattern.getStackInSlot(xx).isEmpty()) {
                     ItemStack template = this.pattern.getStackInSlot(xx);
                     AEItemKey key = AEItemKey.of(template);
                     if (key != null && inv.extract(key, 1L, Actionable.MODULATE, this.mySrc) > 0L) {
                        set[xx] = template.copyWithCount(1);
                     }
                  }
               }
            }
         }

         this.makeItem(p, is);
         this.postCraft(p, inv, set);
         p.containerMenu.slotsChanged(this.craftInv.toContainer());
         return is;
      }
   }

   private void makeItem(Player p, ItemStack is) {
      super.onQuickCraft(is, is.getCount());
      NonNullList<ItemStack> items = NonNullList.withSize(this.pattern.size(), ItemStack.EMPTY);

      for (int i = 0; i < this.pattern.size(); i++) {
         items.set(i, this.pattern.getStackInSlot(i));
      }

      Positioned positioned = CraftingInput.ofPositioned(3, 3, items);
      CommonHooks.setCraftingPlayer(this.player);
      NonNullList<ItemStack> remainingItems = this.getRemainingItems(positioned.input(), this.player.level());
      CommonHooks.setCraftingPlayer((Player)null);

      for (int y = 0; y < 3; y++) {
         for (int x = 0; x < 3; x++) {
            int slotIdx = y * 3 + x;
            int remainderIdx = (y - positioned.top()) * 3 + (x - positioned.left());
            this.pattern.extractItem(slotIdx, 1, false);
            if (remainderIdx >= 0 && remainderIdx < remainingItems.size()) {
               ItemStack remainingInSlot = (ItemStack)remainingItems.get(remainderIdx);
               if (!remainingInSlot.isEmpty()) {
                  if (this.pattern.getStackInSlot(slotIdx).isEmpty()) {
                     this.pattern.setItemDirect(slotIdx, remainingInSlot);
                  } else if (!this.player.getInventory().add(remainingInSlot)) {
                     this.player.drop(remainingInSlot, false);
                  }
               }
            }
         }
      }
   }

   private void postCraft(Player p, MEStorage inv, ItemStack[] set) {
      List<ItemStack> drops = new ArrayList<>();
      if (!p.getCommandSenderWorld().isClientSide()) {
         for (int x = 0; x < this.craftInv.size(); x++) {
            if (this.craftInv.getStackInSlot(x).isEmpty()) {
               this.craftInv.setItemDirect(x, set[x]);
            } else if (!set[x].isEmpty()) {
               AEItemKey what = AEItemKey.of(set[x]);
               int amount = set[x].getCount();
               long inserted = inv.insert(what, (long)amount, Actionable.MODULATE, this.mySrc);
               if (inserted < (long)amount) {
                  drops.add(what.toStack((int)((long)amount - inserted)));
               }
            }
         }
      }

      if (!drops.isEmpty()) {
         Platform.spawnDrops(p.level(), new BlockPos((int)p.getX(), (int)p.getY(), (int)p.getZ()), drops);
      }
   }

   private boolean canConsumeVisForNextCraft(Player player) {
      if (this.arcaneMenu == null) {
         return true;
      } else {
         IArcaneRecipe recipe = this.arcaneMenu.getArcaneRecipe();
         if (recipe == null) {
            return true;
         } else {
            AspectList aspects = recipe.getAspects();
            if (aspects != null && aspects.size() != 0) {
               ItemStack wandStack = this.wandInv.getStackInSlot(0);
               if (wandStack.getItem() instanceof ItemWandCasting wand && !wand.isStaff(wandStack)) {
                  return wand.consumeAllVisCrafting(wandStack, player, aspects, false);
               }

               return false;
            } else {
               return true;
            }
         }
      }
   }

   private boolean consumeVisForRecipe(Player player, CraftingRecipe recipe) {
      if (recipe instanceof IArcaneRecipe arcaneRecipe) {
         AspectList aspects = arcaneRecipe.getAspects();
         if (aspects != null && aspects.size() != 0) {
            ItemStack wandStack = this.wandInv.getStackInSlot(0);
            if (!(wandStack.getItem() instanceof ItemWandCasting wand) || wand.isStaff(wandStack)) {
               return false;
            }

            return !wand.consumeAllVisCrafting(wandStack, player, aspects, false) ? false : wand.consumeAllVisCrafting(wandStack, player, aspects, true);
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   private static CraftingInput createCraftingInput(InternalInventory inventory) {
      List<ItemStack> items = new ArrayList<>(9);

      for (int i = 0; i < 9; i++) {
         items.add(inventory.getStackInSlot(i));
      }

      return CraftingInput.of(3, 3, items);
   }
}
