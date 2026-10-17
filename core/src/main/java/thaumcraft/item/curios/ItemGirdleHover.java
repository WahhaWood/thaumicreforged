package thaumcraft.item.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import thaumcraft.api.IRunicArmor;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemGirdleHover extends Item implements ICurioItem, IRunicArmor {
   public ItemGirdleHover() {
      super(new Properties().stacksTo(1).setNoRepair().rarity(Rarity.RARE));
   }

   public void curioTick(SlotContext slotContext, ItemStack stack) {
      LivingEntity entity = slotContext.entity();
      if (entity != null && entity.fallDistance > 0.0F) {
         entity.fallDistance -= 0.33F;
      }
   }

   @Override
   public int getRunicCharge(ItemStack stack) {
      return 0;
   }
}
