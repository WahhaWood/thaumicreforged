package thaumcraft.network.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import thaumcraft.block.hungry_chest.ChestHungryBlockEntity;
import thaumcraft.network.BasePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketSyncHungryChest extends BasePacket {
   private BlockPos pos;
   private int id;
   private int value;

   public PacketSyncHungryChest(BlockPos pos, int id, int value) {
      this.pos = pos;
      this.id = id;
      this.value = value;
   }

   public PacketSyncHungryChest(FriendlyByteBuf buf) {
      this.pos = buf.readBlockPos();
      this.id = buf.readByte();
      this.value = buf.readInt();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeBlockPos(this.pos);
      buf.writeByte(this.id);
      buf.writeInt(this.value);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      if (minecraft.level != null && minecraft.level.getBlockEntity(this.pos) instanceof ChestHungryBlockEntity chest) {
         chest.triggerEvent(this.id, this.value);
      }
   }
}
