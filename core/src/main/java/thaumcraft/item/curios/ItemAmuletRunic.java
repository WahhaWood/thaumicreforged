package thaumcraft.item.curios;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import thaumcraft.Thaumcraft;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.ItemRunic;
import thaumcraft.util.CuriosSlotUtils;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemAmuletRunic extends ItemRunic implements ICurioItem, IRunicArmor {
   public ItemAmuletRunic(int charge) {
      super(charge, new Properties().stacksTo(1).setNoRepair().rarity(Rarity.RARE));
   }

   public boolean canEquip(SlotContext slotContext, ItemStack stack) {
      return stack.getItem() == this && CuriosSlotUtils.isAmuletSlot(slotContext.identifier());
   }

   public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
      return true;
   }

   public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
      Thaumcraft.instance.runicEventHandler.isDirty = true;
   }

   public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
      Thaumcraft.instance.runicEventHandler.isDirty = true;
   }
}
