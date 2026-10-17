package thaumcraft.network.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.Config;
import thaumcraft.network.BasePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketConfig extends BasePacket {
   public PacketConfig() {
   }

   public PacketConfig(FriendlyByteBuf buf) {
      Config.wardedStone = buf.readBoolean();
      Config.allowMirrors = buf.readBoolean();
      Config.enableKami = buf.readBoolean();
      Config.auraNodeDebug = buf.readBoolean();
      Config.hardNode = buf.readBoolean();
      Config.wuss = buf.readBoolean();
      Config.researchDifficulty = buf.readByte();
      Config.aspectTotalCap = buf.readInt();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeBoolean(Config.wardedStone);
      buf.writeBoolean(Config.allowMirrors);
      buf.writeBoolean(Config.enableKami);
      buf.writeBoolean(Config.auraNodeDebug);
      buf.writeBoolean(Config.hardNode);
      buf.writeBoolean(Config.wuss);
      buf.writeByte(Config.researchDifficulty);
      buf.writeInt(Config.aspectTotalCap);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      Thaumcraft.log.info("Client received server config settings.");
      Thaumcraft.log
         .info(
            "WARDED_STONE["
               + Config.wardedStone
               + "], MIRRORS["
               + Config.allowMirrors
               + "], KAMI["
               + Config.enableKami
               + "], AURA_NODE_DEBUG["
               + Config.auraNodeDebug
               + "], HARD_NODES["
               + Config.hardNode
               + "], WUSS_MODE["
               + Config.wuss
               + "], RESEARCH_DIFFICULTY["
               + Config.researchDifficulty
               + "], ASPECT_TOTAL_CAP["
               + Config.aspectTotalCap
         );
   }
}
