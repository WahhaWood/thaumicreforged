package thaumicenergistics.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import thaumicenergistics.ThaumicEnergistics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ThEPacketHandler {
   private static final String PROTOCOL_VERSION = "1.0";
   private static final Map<ResourceLocation, Function<RegistryFriendlyByteBuf, ? extends ThEPacket>> DECODERS = new HashMap<>();
   private static final Map<Class<? extends ThEPacket>, ResourceLocation> IDS = new HashMap<>();
   private static final StreamCodec<FriendlyByteBuf, ThEPacket> CODEC = StreamCodec.of(ThEPacketHandler::encode, ThEPacketHandler::decode);

   private ThEPacketHandler() {
   }

   public static void init(RegisterPayloadHandlersEvent event) {
      register(InfusionInscriberActionPacket.class, ThaumicEnergistics.location("infusion_inscriber_action"), InfusionInscriberActionPacket::new);
      register(InfusionInscriberSaveStatePacket.class, ThaumicEnergistics.location("infusion_inscriber_save_state"), InfusionInscriberSaveStatePacket::new);
      register(KnowledgeInscriberActionPacket.class, ThaumicEnergistics.location("knowledge_inscriber_action"), KnowledgeInscriberActionPacket::new);
      register(KnowledgeInscriberSaveStatePacket.class, ThaumicEnergistics.location("knowledge_inscriber_save_state"), KnowledgeInscriberSaveStatePacket::new);
      register(ArcaneCraftingGridPacket.class, ThaumicEnergistics.location("arcane_crafting_grid"), ArcaneCraftingGridPacket::new);
      register(CrucibleInscriberActionPacket.class, ThaumicEnergistics.location("crucible_inscriber_action"), CrucibleInscriberActionPacket::new);
      register(CrucibleInscriberSaveStatePacket.class, ThaumicEnergistics.location("crucible_inscriber_save_state"), CrucibleInscriberSaveStatePacket::new);
      register(AssemblerTogglePacket.class, ThaumicEnergistics.location("assembler_toggle"), AssemblerTogglePacket::new);
      event.registrar("1.0").commonBidirectional(ThEPacket.TYPE, CODEC, ThEPacketHandler::handle);
   }

   public static void sendToServer(ThEPacket packet) {
      PacketDistributor.sendToServer(packet, new CustomPacketPayload[0]);
   }

   public static void sendToClient(ThEPacket packet, ServerPlayer player) {
      PacketDistributor.sendToPlayer(player, packet, new CustomPacketPayload[0]);
   }

   private static <T extends ThEPacket> void register(Class<T> type, ResourceLocation id, Function<RegistryFriendlyByteBuf, T> decoder) {
      IDS.put(type, id);
      DECODERS.put(id, decoder);
   }

   private static void handle(ThEPacket packet, IPayloadContext context) {
      packet.handle(context);
   }

   private static void encode(FriendlyByteBuf rawBuf, ThEPacket packet) {
      RegistryFriendlyByteBuf buf = (RegistryFriendlyByteBuf)rawBuf;
      ResourceLocation id = IDS.get(packet.getClass());
      if (id == null) {
         throw new IllegalStateException("Unregistered packet type: " + packet.getClass().getName());
      } else {
         buf.writeResourceLocation(id);
         packet.write(buf);
      }
   }

   private static ThEPacket decode(FriendlyByteBuf rawBuf) {
      RegistryFriendlyByteBuf buf = (RegistryFriendlyByteBuf)rawBuf;
      ResourceLocation id = buf.readResourceLocation();
      Function<RegistryFriendlyByteBuf, ? extends ThEPacket> decoder = DECODERS.get(id);
      if (decoder == null) {
         throw new IllegalStateException("Unknown packet id: " + id);
      } else {
         return decoder.apply(buf);
      }
   }
}
