package thaumcraft.network.misc;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.item.wands.WandManager;
import thaumcraft.network.BasePacket;

public class PacketFocusChangeToServer extends BasePacket {
   private String focus;

   public PacketFocusChangeToServer(String focus) {
      this.focus = focus;
   }

   public PacketFocusChangeToServer(FriendlyByteBuf buf) {
      this.focus = buf.readUtf();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeUtf(this.focus);
   }

   /** Port of PacketFocusChangeToServer.onMessage (TC4): only wands, never sceptres; main hand first. */
   @Override
   public void executeServer(ServerPlayer player) {
      if (player == null || this.focus == null) {
         return;
      }

      for (InteractionHand hand : InteractionHand.values()) {
         ItemStack held = player.getItemInHand(hand);
         if (held.getItem() instanceof ItemWandCasting wand && !wand.isSceptre(held)) {
            WandManager.changeFocus(held, player.level(), player, this.focus);
            return;
         }
      }
   }
}
