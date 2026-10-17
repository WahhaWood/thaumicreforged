package thaumcraft.network.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import thaumcraft.block.warp_gate.WarpGateBlockEntity;
import thaumcraft.network.BasePacket;

public class PacketWarpGateTeleport extends BasePacket {
   private static final double MAX_DISTANCE_SQR = 4.0;
   private final BlockPos gatePos;
   private final int index;

   public PacketWarpGateTeleport(BlockPos gatePos, int index) {
      this.gatePos = gatePos;
      this.index = index;
   }

   public PacketWarpGateTeleport(FriendlyByteBuf buf) {
      this.gatePos = buf.readBlockPos();
      this.index = buf.readVarInt();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeBlockPos(this.gatePos);
      buf.writeVarInt(this.index);
   }

   /** The destination screen opens while sneaking on top of the gate, so the player must still be there. */
   @Override
   public void executeServer(ServerPlayer player) {
      if (player != null && isStandingOnGate(player, this.gatePos)
         && player.level().getBlockEntity(this.gatePos) instanceof WarpGateBlockEntity gate) {
         gate.teleportPlayer(player, this.index);
      }
   }

   public static boolean isStandingOnGate(Player player, BlockPos gatePos) {
      return player.level().isLoaded(gatePos) && player.position().distanceToSqr(Vec3.atBottomCenterOf(gatePos.above())) <= MAX_DISTANCE_SQR;
   }
}
