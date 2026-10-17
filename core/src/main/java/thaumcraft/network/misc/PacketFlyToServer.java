package thaumcraft.network.misc;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.item.armor.hover_harness.HoverLogic;
import thaumcraft.item.armor.hover_harness.ItemHoverHarness;
import thaumcraft.network.BasePacket;

public class PacketFlyToServer extends BasePacket {
   private final boolean hover;

   public PacketFlyToServer(boolean hover) {
      this.hover = hover;
   }

   public PacketFlyToServer(FriendlyByteBuf buf) {
      this.hover = buf.readBoolean();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeBoolean(this.hover);
   }

   /** The client toggles its own copy for instant feedback; the worn harness on the server is authoritative. */
   @Override
   public void executeServer(ServerPlayer player) {
      if (player != null) {
         ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
         if (chest.getItem() instanceof ItemHoverHarness) {
            // TC4 refuses to switch on without Potentia in the jar.
            HoverLogic.setActive(chest, this.hover && HoverLogic.checkFuel(chest).hasFuel());
         }
      }
   }
}
