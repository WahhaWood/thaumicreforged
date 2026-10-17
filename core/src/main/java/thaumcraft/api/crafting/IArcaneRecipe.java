package thaumcraft.api.crafting;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import thaumcraft.api.aspects.AspectList;

public interface IArcaneRecipe extends CraftingRecipe {
   default ItemStack getResultItem() {
      return this.getResultItem((Provider)null);
   }

   default ItemStack getResultItem(CraftingInput input, Provider access) {
      return this.assemble(input, access);
   }

   default ItemStack getResultItem(CraftingContainer container, Provider access) {
      return this.getResultItem(access);
   }

   AspectList getAspects();

   default AspectList getAspects(CraftingInput input) {
      return this.getAspects();
   }

   default AspectList getAspects(CraftingContainer container) {
      return this.getAspects();
   }

   String getResearchKey();
}
