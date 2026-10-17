package thaumicenergistics.item;

import appeng.items.storage.BasicStorageCell;
import thaumicenergistics.ae2.storage.AEAspectKeyType;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.ItemLike;

public class ItemEssentiaStorageCell extends BasicStorageCell {
   public ItemEssentiaStorageCell(ItemLike coreItem, ItemLike housingItem, double idleDrain, int kilobytes, int bytesPerType, int totalTypes) {
      super(new Properties().stacksTo(1), idleDrain, kilobytes, bytesPerType, totalTypes, AEAspectKeyType.INSTANCE);
   }
}
