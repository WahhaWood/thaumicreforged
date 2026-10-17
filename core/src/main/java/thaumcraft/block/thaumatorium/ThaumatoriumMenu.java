package thaumcraft.block.thaumatorium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.init.registry.ModMenuTypes;
import thaumcraft.research.ResearchManager;

public class ThaumatoriumMenu extends AbstractContainerMenu {
   public final ThaumatoriumBlockEntity entity;
   public final Player player;
   private final List<RecipeHolder<CrucibleRecipe>> recipes = new ArrayList<>();

   public ThaumatoriumMenu(int windowsId, Inventory inventory, FriendlyByteBuf buf) {
      this(windowsId, inventory, (ThaumatoriumBlockEntity)inventory.player.level().getBlockEntity(buf.readBlockPos()));
   }

   public ThaumatoriumMenu(int windowsId, Inventory inventory, ThaumatoriumBlockEntity entity) {
      super((MenuType)ModMenuTypes.THAUMATORIUM.get(), windowsId);
      this.player = inventory.player;
      this.entity = entity;
      this.addSlot(new Slot(entity, 0, 48, 16) {
         public void setChanged() {
            super.setChanged();
            ThaumatoriumMenu.this.updateRecipes();
         }
      });

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
      }

      this.updateRecipes();
   }

   public void updateRecipes() {
      this.recipes.clear();
      ItemStack input = this.entity.getItem(0);
      if (!input.isEmpty() || this.entity.hasSelectedRecipes()) {
         for (RecipeHolder<CrucibleRecipe> holder : ThaumcraftApi.getRecipeManager().getAllRecipesFor(CrucibleRecipe.TYPE)) {
            CrucibleRecipe r = (CrucibleRecipe)holder.value();
            if (ResearchManager.isResearchComplete(this.player, r.researchKey) && r.catalystMatches(input)) {
               this.recipes.add(holder);
            } else if (this.entity.isRecipeSelected(holder.id())) {
               this.recipes.add(holder);
            }
         }
      }

      this.recipes.sort(Comparator.comparing(rx -> rx.id().toString()));
   }

   public boolean hasRecipes() {
      return !this.recipes.isEmpty();
   }

   public int getRecipeCount() {
      return this.recipes.size();
   }

   public CrucibleRecipe getRecipe(int index) {
      return (CrucibleRecipe)this.recipes.get(index).value();
   }

   public ResourceLocation getRecipeId(int index) {
      return this.recipes.get(index).id();
   }

   public List<RecipeHolder<CrucibleRecipe>> getRecipes() {
      return Collections.unmodifiableList(this.recipes);
   }

   /** TC4 ContainerThaumatorium.enchantItem: the screen sends the index of the shown recipe, the server toggles it. */
   @Override
   public boolean clickMenuButton(@NotNull Player player, int id) {
      this.updateRecipes();
      if (id >= 0 && id < this.recipes.size()) {
         this.entity.toggleRecipeSelection(this.recipes.get(id), player.getName().getString());
         return true;
      } else {
         return false;
      }
   }

   public boolean stillValid(@NotNull Player player) {
      return this.entity.stillValid(player);
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int index) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.getSlot(index);
      if (slot.hasItem()) {
         ItemStack stack = slot.getItem();
         int originalCount = stack.getCount();
         itemstack = stack.copy();
         if (index != 0) {
            if (!this.moveItemStackTo(stack, 0, 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(stack, 1, 37, false)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (stack.getCount() == originalCount) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, stack);
      }

      return itemstack;
   }
}
