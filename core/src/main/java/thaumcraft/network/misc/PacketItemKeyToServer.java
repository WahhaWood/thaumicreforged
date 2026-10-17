package thaumcraft.network.misc;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import thaumcraft.item.equipment.ItemElementalShovel;
import thaumcraft.item.golem_bell.ItemGolemBell;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.WandManager;
import thaumcraft.network.BasePacket;

/**
 * Клавиша «G» (misc-переключение предмета):
 * key 0 — колокол голема (сброс меток), key 1 — ориентация плоскости у Лопаты землероя
 * (порт {@code PacketItemKeyToServer} из TC4 1.7.10).
 */
public class PacketItemKeyToServer extends BasePacket {
   private final byte key;

   public PacketItemKeyToServer(int key) {
      this.key = (byte)key;
   }

   public PacketItemKeyToServer(FriendlyByteBuf buf) {
      this.key = buf.readByte();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeByte(this.key);
   }

   @Override
   public void executeServer(ServerPlayer player) {
      if (player == null) {
         return;
      }

      // Port of PacketItemKeyToServer.onMessage (TC4): F with a golem bell clears its markers,
      // G with a wand cycles the architect area, G with the elemental shovel rotates it.
      ItemStack held = player.getMainHandItem();
      if (this.key == 0 && held.getItem() instanceof ItemGolemBell) {
         ItemGolemBell.resetMarkers(held, player.level(), player);
      }

      if (this.key == 1 && held.getItem() instanceof ItemWandCasting) {
         WandManager.toggleMisc(held, player);
      }

      if (this.key == 1 && held.getItem() instanceof ItemElementalShovel) {
         ItemElementalShovel.setOrientation(held, ItemElementalShovel.getOrientation(held) + 1);
      }
   }
}
