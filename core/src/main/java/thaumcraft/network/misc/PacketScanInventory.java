package thaumcraft.network.misc;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.research.ScanResult;
import thaumcraft.item.relics.ItemThaumometer;
import thaumcraft.network.BasePacket;
import thaumcraft.research.ScanManager;

public class PacketScanInventory extends BasePacket {
   private final int slotId;

   public PacketScanInventory(int slotId) {
      this.slotId = slotId;
   }

   public PacketScanInventory(FriendlyByteBuf buf) {
      this.slotId = buf.readInt();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeInt(this.slotId);
   }

   @Override
   public void executeServer(ServerPlayer player) {
      if (player == null) {
         return;
      }

      // На курсоре должен лежать таумометр, иначе пакет поддельный.
      if (!(player.containerMenu.getCarried().getItem() instanceof ItemThaumometer)) {
         return;
      }

      if (this.slotId < 0 || this.slotId >= player.containerMenu.slots.size()) {
         return;
      }

      Slot slot = player.containerMenu.getSlot(this.slotId);
      ItemStack stack = slot.getItem();
      if (stack.isEmpty()) {
         return;
      }

      ScanResult scan = ScanResult.ofItem(stack);
      if (ScanManager.isValidScanTarget(player, scan, "@")) {
         ScanManager.completeScan(player, scan, "@");
      }
   }
}
