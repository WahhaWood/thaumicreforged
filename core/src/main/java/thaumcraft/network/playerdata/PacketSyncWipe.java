package thaumcraft.network.playerdata;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import thaumcraft.compat.jei.ThaumcraftJeiPlugin;
import thaumcraft.network.BasePacket;
import thaumcraft.research.PlayerKnowledge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketSyncWipe extends BasePacket {
   public PacketSyncWipe() {
   }

   public PacketSyncWipe(FriendlyByteBuf buf) {
   }

   @Override
   public void write(FriendlyByteBuf buf) {
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      PlayerKnowledge.of(minecraft.player).clear();
      ThaumcraftJeiPlugin.refreshRecipeVisibility();
   }
}
