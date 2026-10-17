package thaumicenergistics.network;

import thaumicenergistics.client.screen.InfusionInscriberScreen;
import thaumicenergistics.menu.InfusionInscriberMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class InfusionInscriberSaveStatePacket implements ThEPacket {
   private static final InfusionInscriberMenu.SaveState[] SAVE_STATES = InfusionInscriberMenu.SaveState.values();
   private final InfusionInscriberMenu.SaveState saveState;
   private final boolean justSaved;

   public InfusionInscriberSaveStatePacket(InfusionInscriberMenu.SaveState saveState, boolean justSaved) {
      this.saveState = saveState;
      this.justSaved = justSaved;
   }

   public InfusionInscriberSaveStatePacket(RegistryFriendlyByteBuf buf) {
      int ordinal = buf.readVarInt();
      this.saveState = ordinal >= 0 && ordinal < SAVE_STATES.length ? SAVE_STATES[ordinal] : InfusionInscriberMenu.SaveState.DISABLED_MISSING_CORE;
      this.justSaved = buf.readBoolean();
   }

   @Override
   public void write(RegistryFriendlyByteBuf buf) {
      buf.writeVarInt(this.saveState.ordinal());
      buf.writeBoolean(this.justSaved);
   }

   @Override
   public void handle(IPayloadContext context) {
      // Server-to-client only: the channel is bidirectional, so a client could send it to the server.
      if (context.flow().isClientbound()) {
         context.enqueueWork(this::executeClient);
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void executeClient() {
      if (Minecraft.getInstance().screen instanceof InfusionInscriberScreen screen) {
         screen.onReceiveSaveState(this.saveState, this.justSaved);
      }
   }

   public static void sendToPlayer(ServerPlayer player, InfusionInscriberMenu.SaveState saveState, boolean justSaved) {
      ThEPacketHandler.sendToClient(new InfusionInscriberSaveStatePacket(saveState, justSaved), player);
   }
}
