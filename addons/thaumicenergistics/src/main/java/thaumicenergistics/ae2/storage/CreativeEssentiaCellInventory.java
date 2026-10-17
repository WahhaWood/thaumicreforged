package thaumicenergistics.ae2.storage;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import appeng.util.ConfigInventory;
import thaumicenergistics.item.ItemCreativeEssentiaCell;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class CreativeEssentiaCellInventory implements StorageCell {
   private static final long REPORTED_AMOUNT = 100000000000L;
   private final ItemStack cell;
   private final ItemCreativeEssentiaCell cellItem;

   public CreativeEssentiaCellInventory(ItemStack cell) {
      this.cell = cell;
      this.cellItem = (ItemCreativeEssentiaCell)cell.getItem();
   }

   public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
      MEStorage.checkPreconditions(what, amount, mode, source);
      return amount != 0L && this.hasConfiguredAspect(what) ? amount : 0L;
   }

   public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
      MEStorage.checkPreconditions(what, amount, mode, source);
      return amount != 0L && this.hasConfiguredAspect(what) ? Math.min(amount, 100000000000L) : 0L;
   }

   public void getAvailableStacks(KeyCounter out) {
      for (AEKey configuredKey : this.getConfigInventory().keySet()) {
         out.add(configuredKey, 100000000000L);
      }
   }

   public Component getDescription() {
      return this.cell.getHoverName();
   }

   public CellState getStatus() {
      return this.getConfigInventory().isEmpty() ? CellState.EMPTY : CellState.NOT_EMPTY;
   }

   public double getIdleDrain() {
      return this.cellItem.getIdleDrain();
   }

   public boolean canFitInsideCell() {
      return this.getConfigInventory().isEmpty();
   }

   public void persist() {
   }

   private boolean hasConfiguredAspect(AEKey key) {
      return AEAspectKeyType.INSTANCE.contains(key) && this.getConfigInventory().keySet().contains(key);
   }

   private ConfigInventory getConfigInventory() {
      return this.cellItem.getConfigInventory(this.cell);
   }
}
