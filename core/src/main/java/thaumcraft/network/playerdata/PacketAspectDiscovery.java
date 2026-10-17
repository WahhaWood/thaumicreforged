package thaumcraft.network.playerdata;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.item.relics.thaumonomicon.render.ResearchBrowserScreen;
import thaumcraft.network.BasePacket;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.util.PlayerNotifications;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketAspectDiscovery extends BasePacket {
   private final String aspectKey;

   public PacketAspectDiscovery(String aspectKey) {
      this.aspectKey = aspectKey;
   }

   public PacketAspectDiscovery(FriendlyByteBuf buf) {
      this.aspectKey = buf.readUtf();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeUtf(this.aspectKey);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      Aspect aspect = Aspect.getAspect(this.aspectKey);
      if (aspect != null) {
         PlayerKnowledge.of(minecraft.player).addDiscoveredAspect(aspect);
         PlayerNotifications.addNotification(Component.translatable("tc.addaspectdiscovery", new Object[]{aspect.getName()}), aspect);
         minecraft.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.2F, 0.5F + minecraft.level.random.nextFloat() * 0.2F);
         ResearchBrowserScreen.highlightedItem.add("ASPECTS");
         ResearchBrowserScreen.highlightedItem.add("BASICS");
      }
   }
}
