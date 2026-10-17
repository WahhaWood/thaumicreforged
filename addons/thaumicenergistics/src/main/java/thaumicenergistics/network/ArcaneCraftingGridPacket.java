package thaumicenergistics.network;

import appeng.api.stacks.AEItemKey;
import thaumicenergistics.menu.ArcaneCraftingTermMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ArcaneCraftingGridPacket implements ThEPacket {
   private static final int ITEM_GRID_SIZE = 9;
   private final AEItemKey[] gridItems;

   public ArcaneCraftingGridPacket(AEItemKey[] items) {
      this.gridItems = items;
   }

   public ArcaneCraftingGridPacket(RegistryFriendlyByteBuf buf) {
      this.gridItems = new AEItemKey[9];

      for (int i = 0; i < 9; i++) {
         if (buf.readBoolean()) {
            this.gridItems[i] = AEItemKey.fromPacket(buf);
         }
      }
   }

   @Override
   public void write(RegistryFriendlyByteBuf buf) {
      for (int i = 0; i < 9; i++) {
         AEItemKey slotItem = this.gridItems[i];
         boolean hasItem = slotItem != null;
         buf.writeBoolean(hasItem);
         if (hasItem) {
            slotItem.writeToPacket(buf);
         }
      }
   }

   @Override
   public void handle(IPayloadContext context) {
      context.enqueueWork(() -> {
         if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof ArcaneCraftingTermMenu menu && menu.stillValid(player)) {
            menu.fillCraftingGrid(player, this.gridItems);
         }
      });
   }

   public static void sendSetCraftingGrid(AEItemKey[] items) {
      ThEPacketHandler.sendToServer(new ArcaneCraftingGridPacket(items));
   }
}
