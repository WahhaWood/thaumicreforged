package thaumcraft.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import thaumcraft.Thaumcraft;
import thaumcraft.network.fx.PacketFXBeamPulse;
import thaumcraft.network.fx.PacketFXBeamPulseGolemBoss;
import thaumcraft.network.fx.PacketFXBlockArc;
import thaumcraft.network.fx.PacketFXBlockBubble;
import thaumcraft.network.fx.PacketFXBlockDig;
import thaumcraft.network.fx.PacketFXBlockSparkle;
import thaumcraft.network.fx.PacketFXBlockZap;
import thaumcraft.network.fx.PacketFXEssenceSource;
import thaumcraft.network.fx.PacketFXInfusionSource;
import thaumcraft.network.fx.PacketFXShield;
import thaumcraft.network.fx.PacketFXVisDrain;
import thaumcraft.network.fx.PacketFXWispZap;
import thaumcraft.network.fx.PacketFXZap;
import thaumcraft.network.misc.PacketBiomeChange;
import thaumcraft.network.misc.PacketBoreDig;
import thaumcraft.network.misc.PacketConfig;
import thaumcraft.network.misc.PacketFlyToServer;
import thaumcraft.network.misc.PacketFocusChangeToServer;
import thaumcraft.network.misc.PacketItemKeyToServer;
import thaumcraft.network.misc.PacketMiscEvent;
import thaumcraft.network.misc.PacketScanInventory;
import thaumcraft.network.misc.PacketStartAnimation;
import thaumcraft.network.misc.PacketSyncHungryChest;
import thaumcraft.network.misc.PacketWarpGateTeleport;
import thaumcraft.network.playerdata.PacketAspectCombinationToServer;
import thaumcraft.network.playerdata.PacketAspectDiscovery;
import thaumcraft.network.playerdata.PacketAspectPlaceToServer;
import thaumcraft.network.playerdata.PacketAspectPool;
import thaumcraft.network.playerdata.PacketPlayerCompleteToServer;
import thaumcraft.network.playerdata.PacketResearchComplete;
import thaumcraft.network.playerdata.PacketRunicCharge;
import thaumcraft.network.playerdata.PacketSyncAspectRegistry;
import thaumcraft.network.playerdata.PacketSyncAspects;
import thaumcraft.network.playerdata.PacketSyncResearch;
import thaumcraft.network.playerdata.PacketSyncResearchTree;
import thaumcraft.network.playerdata.PacketSyncScannedEntities;
import thaumcraft.network.playerdata.PacketSyncScannedItems;
import thaumcraft.network.playerdata.PacketSyncScannedPhenomena;
import thaumcraft.network.playerdata.PacketSyncWarp;
import thaumcraft.network.playerdata.PacketSyncSoulHearts;
import thaumcraft.network.playerdata.PacketSyncWipe;
import thaumcraft.network.playerdata.PacketWarpMessage;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketHandler {
   private static final String PROTOCOL_VERSION = "1.0";
   private static final ResourceLocation CHANNEL_ID = Thaumcraft.location("packet");
   private static final Map<ResourceLocation, Function<FriendlyByteBuf, ? extends BasePacket>> DECODERS = new HashMap<>();
   private static final Map<Class<? extends BasePacket>, ResourceLocation> IDS = new HashMap<>();
   private static final StreamCodec<FriendlyByteBuf, BasePacket> CODEC = StreamCodec.of(PacketHandler::encode, PacketHandler::decode);

   public static void init(RegisterPayloadHandlersEvent event) {
      event.registrar("1.0").commonBidirectional(BasePacket.TYPE, CODEC, PacketHandler::handle);
      register(PacketBiomeChange.class, Thaumcraft.location("biome_change"), PacketBiomeChange::new);
      register(PacketConfig.class, Thaumcraft.location("config"), PacketConfig::new);
      register(PacketMiscEvent.class, Thaumcraft.location("misc_event"), PacketMiscEvent::new);
      register(PacketSyncWipe.class, Thaumcraft.location("sync_wipe"), PacketSyncWipe::new);
      register(PacketSyncAspects.class, Thaumcraft.location("sync_aspects"), PacketSyncAspects::new);
      register(PacketSyncResearch.class, Thaumcraft.location("sync_research"), PacketSyncResearch::new);
      register(PacketSyncScannedItems.class, Thaumcraft.location("sync_scanned_items"), PacketSyncScannedItems::new);
      register(PacketSyncScannedEntities.class, Thaumcraft.location("sync_scanned_entities"), PacketSyncScannedEntities::new);
      register(PacketSyncScannedPhenomena.class, Thaumcraft.location("sync_scanned_phenomena"), PacketSyncScannedPhenomena::new);
      register(PacketResearchComplete.class, Thaumcraft.location("research_complete"), PacketResearchComplete::new);
      register(PacketAspectPool.class, Thaumcraft.location("aspect_pool"), PacketAspectPool::new);
      register(PacketAspectDiscovery.class, Thaumcraft.location("aspect_discovery"), PacketAspectDiscovery::new);
      register(PacketAspectCombinationToServer.class, Thaumcraft.location("aspect_combination"), PacketAspectCombinationToServer::new);
      register(PacketPlayerCompleteToServer.class, Thaumcraft.location("player_complete"), PacketPlayerCompleteToServer::new);
      register(PacketAspectPlaceToServer.class, Thaumcraft.location("aspect_place"), PacketAspectPlaceToServer::new);
      register(PacketRunicCharge.class, Thaumcraft.location("runic_charge"), PacketRunicCharge::new);
      register(PacketBoreDig.class, Thaumcraft.location("bore_dig"), PacketBoreDig::new);
      register(PacketSyncWarp.class, Thaumcraft.location("sync_warp"), PacketSyncWarp::new);
      register(PacketSyncSoulHearts.class, Thaumcraft.location("sync_soul_hearts"), PacketSyncSoulHearts::new);
      register(PacketWarpMessage.class, Thaumcraft.location("warp_message"), PacketWarpMessage::new);
      register(PacketItemKeyToServer.class, Thaumcraft.location("item_key"), PacketItemKeyToServer::new);
      register(PacketFocusChangeToServer.class, Thaumcraft.location("focus_change"), PacketFocusChangeToServer::new);
      register(PacketFlyToServer.class, Thaumcraft.location("fly"), PacketFlyToServer::new);
      register(PacketFXBlockBubble.class, Thaumcraft.location("fx_block_bubble"), PacketFXBlockBubble::new);
      register(PacketFXBlockDig.class, Thaumcraft.location("fx_block_dig"), PacketFXBlockDig::new);
      register(PacketFXBlockSparkle.class, Thaumcraft.location("fx_block_sparkle"), PacketFXBlockSparkle::new);
      register(PacketFXBlockArc.class, Thaumcraft.location("fx_block_arc"), PacketFXBlockArc::new);
      register(PacketFXBlockZap.class, Thaumcraft.location("fx_block_zap"), PacketFXBlockZap::new);
      register(PacketFXEssenceSource.class, Thaumcraft.location("fx_essence_source"), PacketFXEssenceSource::new);
      register(PacketFXInfusionSource.class, Thaumcraft.location("fx_infusion_source"), PacketFXInfusionSource::new);
      register(PacketFXShield.class, Thaumcraft.location("fx_shield"), PacketFXShield::new);
      register(PacketFXWispZap.class, Thaumcraft.location("fx_wisp_zap"), PacketFXWispZap::new);
      register(PacketFXZap.class, Thaumcraft.location("fx_zap"), PacketFXZap::new);
      register(PacketFXVisDrain.class, Thaumcraft.location("fx_vis_drain"), PacketFXVisDrain::new);
      register(PacketFXBeamPulse.class, Thaumcraft.location("fx_beam_pulse"), PacketFXBeamPulse::new);
      register(PacketFXBeamPulseGolemBoss.class, Thaumcraft.location("fx_beam_pulse_golem_boss"), PacketFXBeamPulseGolemBoss::new);
      register(PacketStartAnimation.class, Thaumcraft.location("start_animation"), PacketStartAnimation::new);
      register(PacketScanInventory.class, Thaumcraft.location("scan_inventory"), PacketScanInventory::new);
      register(PacketSyncAspectRegistry.class, Thaumcraft.location("sync_aspect_registry"), PacketSyncAspectRegistry::new);
      register(PacketSyncResearchTree.class, Thaumcraft.location("sync_research_tree"), PacketSyncResearchTree::new);
      register(PacketSyncHungryChest.class, Thaumcraft.location("sync_hungry_chest"), PacketSyncHungryChest::new);
      register(PacketWarpGateTeleport.class, Thaumcraft.location("warp_gate_teleport"), PacketWarpGateTeleport::new);
   }

   @OnlyIn(Dist.CLIENT)
   public static void sendToServer(BasePacket packet) {
      Connection connection = Minecraft.getInstance().getConnection().getConnection();
      connection.send(new ServerboundCustomPayloadPacket(packet));
   }

   public static void sendTo(BasePacket packet, ServerPlayer player) {
      player.connection.getConnection().send(new ClientboundCustomPayloadPacket(packet));
   }

   public static void sendToAll(BasePacket packet) {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      if (server != null) {
         ClientboundCustomPayloadPacket wrapped = new ClientboundCustomPayloadPacket(packet);

         for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.getConnection().send(wrapped);
         }
      }
   }

   public static void sendToAllAround(BasePacket packet, Level level, BlockPos blockPos, double radius) {
      sendToAllAround(packet, level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), radius);
   }

   public static void sendToAllAround(BasePacket packet, Level level, Vec3 vec3, double radius) {
      sendToAllAround(packet, level, vec3.x, vec3.y, vec3.z, radius);
   }

   public static void sendToAllAround(BasePacket packet, Level level, double x, double y, double z, double radius) {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      if (server != null) {
         double radiusSq = radius * radius;
         ClientboundCustomPayloadPacket wrapped = new ClientboundCustomPayloadPacket(packet);

         for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.level().dimension() == level.dimension() && player.distanceToSqr(x, y, z) <= radiusSq) {
               player.connection.getConnection().send(wrapped);
            }
         }
      }
   }

   private PacketHandler() {
   }

   private static <T extends BasePacket> void register(Class<T> type, ResourceLocation id, Function<FriendlyByteBuf, T> decoder) {
      IDS.put(type, id);
      DECODERS.put(id, decoder);
   }

   private static void handle(BasePacket packet, IPayloadContext context) {
      packet.handle(context);
   }

   private static void encode(FriendlyByteBuf buf, BasePacket packet) {
      ResourceLocation id = IDS.get(packet.getClass());
      if (id == null) {
         throw new IllegalStateException("Unregistered packet type: " + packet.getClass().getName());
      } else {
         buf.writeResourceLocation(id);
         packet.write(buf);
      }
   }

   private static BasePacket decode(FriendlyByteBuf buf) {
      ResourceLocation id = buf.readResourceLocation();
      Function<FriendlyByteBuf, ? extends BasePacket> decoder = DECODERS.get(id);
      if (decoder == null) {
         throw new IllegalStateException("Unknown packet id: " + id);
      } else {
         return decoder.apply(buf);
      }
   }
}
