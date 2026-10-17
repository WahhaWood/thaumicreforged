package thaumcraft.network.playerdata;

import java.util.HashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import thaumcraft.api.research.ResearchApi;
import thaumcraft.compat.jei.ThaumcraftJeiPlugin;
import thaumcraft.network.BasePacket;
import thaumcraft.research.ResearchManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketSyncResearch extends BasePacket {
   protected HashSet<String> data;

   public PacketSyncResearch(Player player) {
      this.data = ResearchManager.getResearchForPlayer(player);
   }

   public PacketSyncResearch(FriendlyByteBuf buf) {
      this.data = new HashSet<>(buf.readList(FriendlyByteBuf::readUtf));
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeCollection(this.data, FriendlyByteBuf::writeUtf);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      this.data.forEach(s -> ResearchApi.completeResearch(minecraft.player, s));
      ThaumcraftJeiPlugin.refreshRecipeVisibility();
   }
}
