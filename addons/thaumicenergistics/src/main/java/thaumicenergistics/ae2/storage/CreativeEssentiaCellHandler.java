package thaumicenergistics.ae2.storage;

import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import thaumicenergistics.item.ItemCreativeEssentiaCell;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CreativeEssentiaCellHandler implements ICellHandler {
   public static final CreativeEssentiaCellHandler INSTANCE = new CreativeEssentiaCellHandler();

   private CreativeEssentiaCellHandler() {
   }

   public boolean isCell(ItemStack is) {
      return is.getItem() instanceof ItemCreativeEssentiaCell;
   }

   @Nullable
   public StorageCell getCellInventory(ItemStack is, @Nullable ISaveProvider host) {
      return !(is.getItem() instanceof ItemCreativeEssentiaCell) ? null : new CreativeEssentiaCellInventory(is);
   }
}
