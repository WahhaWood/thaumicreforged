package thaumcraft.network.playerdata;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.network.BasePacket;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.util.PlayerNotifications;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketAspectPool extends BasePacket {
   private final String aspectKey;
   private final Short amount;
   private final Short total;
   private static long lastSound;

   public PacketAspectPool(String aspectKey, short amount, short total) {
      this.aspectKey = aspectKey;
      this.amount = amount;
      this.total = total;
   }

   public PacketAspectPool(FriendlyByteBuf buf) {
      this.aspectKey = buf.readUtf();
      this.amount = buf.readShort();
      this.total = buf.readShort();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeUtf(this.aspectKey);
      buf.writeShort(this.amount);
      buf.writeShort(this.total);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      Aspect aspect = Aspect.getAspect(this.aspectKey);
      if (aspect != null && this.amount >= 0) {
         PlayerKnowledge knowledge = PlayerKnowledge.of(minecraft.player);
         knowledge.setAspectPool(aspect, this.total);
         PlayerNotifications.addNotification(Component.translatable("tc.addaspectpool", new Object[]{this.amount, aspect.getName()}), aspect);

         for (short n = 0; n < this.amount; n++) {
            PlayerNotifications.addAspectNotification(aspect);
         }

         if (System.currentTimeMillis() > lastSound) {
            minecraft.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.1F, 0.9F + minecraft.player.level().random.nextFloat() * 0.2F);
            lastSound = System.currentTimeMillis() + 100L;
         }
      }
   }
}
