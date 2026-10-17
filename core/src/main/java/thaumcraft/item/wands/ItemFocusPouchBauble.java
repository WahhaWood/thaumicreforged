package thaumcraft.item.wands;

import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemFocusPouchBauble extends ItemFocusPouch implements ICurioItem {
   public boolean canEquip(SlotContext slotContext, ItemStack stack) {
      return stack.getItem() == this && slotContext.identifier().equals("belt");
   }
}
