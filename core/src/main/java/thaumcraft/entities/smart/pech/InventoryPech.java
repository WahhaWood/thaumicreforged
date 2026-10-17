package thaumcraft.entities.smart.pech;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class InventoryPech extends SimpleContainer {
   private final EntityPech pech;
   private final PechMenu menu;
   private final Player player;

   public InventoryPech(Player player, EntityPech pech, PechMenu menu) {
      super(5);
      this.player = player;
      this.pech = pech;
      this.menu = menu;
   }

   public void setChanged() {
      super.setChanged();
      if (this.menu != null) {
         this.menu.slotsChanged(this);
      }
   }

   public boolean stillValid(Player player) {
      return this.pech != null && this.pech.isAlive() && this.pech.isTamed() && player.distanceToSqr(this.pech) < 64.0;
   }

   public boolean canPlaceItem(int slot, ItemStack stack) {
      return slot == 0;
   }
}
