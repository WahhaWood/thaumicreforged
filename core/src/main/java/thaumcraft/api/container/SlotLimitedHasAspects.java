package thaumcraft.api.container;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.recipe.ThaumcraftCraftingManager;

public class SlotLimitedHasAspects extends Slot {
   public SlotLimitedHasAspects(Container container, int index, int x, int y) {
      super(container, index, x, y);
   }

   public boolean mayPlace(@NotNull ItemStack stack) {
      AspectList bonusTags = ThaumcraftCraftingManager.getBonusTags(stack, ThaumcraftCraftingManager.getObjectTags(stack));
      return bonusTags != null && bonusTags.size() > 0;
   }
}
