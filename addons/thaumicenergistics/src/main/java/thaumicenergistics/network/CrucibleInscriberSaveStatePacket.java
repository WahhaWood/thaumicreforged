package thaumicenergistics.network;

import thaumicenergistics.client.screen.CrucibleInscriberScreen;
import thaumicenergistics.menu.CrucibleInscriberMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CrucibleInscriberSaveStatePacket implements ThEPacket {
   private static final CrucibleInscriberMenu.SaveState[] SAVE_STATES = CrucibleInscriberMenu.SaveState.values();
   private final CrucibleInscriberMenu.SaveState saveState;
   private final boolean justSaved;
   private final boolean canBack;
   private final boolean canNext;

   public CrucibleInscriberSaveStatePacket(CrucibleInscriberMenu.SaveState saveState, boolean justSaved, boolean canBack, boolean canNext) {
      this.saveState = saveState;
      this.justSaved = justSaved;
      this.canBack = canBack;
      this.canNext = canNext;
   }

   public CrucibleInscriberSaveStatePacket(RegistryFriendlyByteBuf buf) {
      int ordinal = buf.readVarInt();
      this.saveState = ordinal >= 0 && ordinal < SAVE_STATES.length ? SAVE_STATES[ordinal] : CrucibleInscriberMenu.SaveState.DISABLED_MISSING_CORE;
      this.justSaved = buf.readBoolean();
      this.canBack = buf.readBoolean();
      this.canNext = buf.readBoolean();
   }

   @Override
   public void write(RegistryFriendlyByteBuf buf) {
      buf.writeVarInt(this.saveState.ordinal());
      buf.writeBoolean(this.justSaved);
      buf.writeBoolean(this.canBack);
      buf.writeBoolean(this.canNext);
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
      if (Minecraft.getInstance().screen instanceof CrucibleInscriberScreen screen) {
         screen.onReceiveSaveState(this.saveState, this.justSaved, this.canBack, this.canNext);
      }
   }

   public static void sendToPlayer(ServerPlayer player, CrucibleInscriberMenu.SaveState saveState, boolean justSaved, boolean canBack, boolean canNext) {
      ThEPacketHandler.sendToClient(new CrucibleInscriberSaveStatePacket(saveState, justSaved, canBack, canNext), player);
   }
}
