package thaumcraft.network.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import thaumcraft.network.BasePacket;
import thaumcraft.util.Utils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketBiomeChange extends BasePacket {
   private final BlockPos pos;
   private final ResourceLocation biomeId;

   public PacketBiomeChange(BlockPos pos, ResourceKey<Biome> biomeKey) {
      this.pos = pos;
      this.biomeId = biomeKey.location();
   }

   public PacketBiomeChange(FriendlyByteBuf buf) {
      this.pos = buf.readBlockPos();
      this.biomeId = buf.readResourceLocation();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeBlockPos(this.pos);
      buf.writeResourceLocation(this.biomeId);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      if (minecraft.level != null) {
         ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, this.biomeId);
         Utils.setBiomeAt(minecraft.level, this.pos, key);
         minecraft.levelRenderer.setSectionDirty(this.pos.getX() >> 4, this.pos.getY() >> 4, this.pos.getZ() >> 4);
      }
   }
}
