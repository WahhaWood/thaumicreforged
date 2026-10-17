package thaumicenergistics.util;

import appeng.api.inventories.InternalInventory;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.IWarpingGear;
import thaumcraft.api.aspects.Aspect;

public final class VisCraftingHelper {
   public static final VisCraftingHelper INSTANCE = new VisCraftingHelper();
   private static final float SCEPTER_MODIFIER = 0.9F;

   private VisCraftingHelper() {
   }

   public float getScepterVisModifier(Aspect aspect) {
      return 0.9F;
   }

   public float calculateArmorDiscount(InternalInventory armorInventory, int startSlot, int endSlot, Aspect aspect) {
      int totalPercent = 0;

      for (int i = startSlot; i < endSlot; i++) {
         ItemStack stack = armorInventory.getStackInSlot(i);
         if (!stack.isEmpty() && stack.getItem() instanceof IVisDiscountGear gear) {
            totalPercent += gear.getVisDiscount(stack, null, aspect);
         }
      }

      return (float)totalPercent / 100.0F;
   }

   public int calculateArmorWarp(InternalInventory armorInventory, int startSlot, int endSlot) {
      int totalWarp = 0;

      for (int i = startSlot; i < endSlot; i++) {
         ItemStack stack = armorInventory.getStackInSlot(i);
         if (!stack.isEmpty() && stack.getItem() instanceof IWarpingGear gear) {
            totalWarp += gear.getWarp(stack, null);
         }
      }

      return totalWarp;
   }
}
