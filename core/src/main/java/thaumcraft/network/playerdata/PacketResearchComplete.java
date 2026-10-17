package thaumcraft.network.playerdata;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import thaumcraft.api.research.ResearchApi;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchToast;
import thaumcraft.compat.jei.ThaumcraftJeiPlugin;
import thaumcraft.init.registry.ModSounds;
import thaumcraft.item.relics.thaumonomicon.render.ResearchBrowserScreen;
import thaumcraft.network.BasePacket;
import thaumcraft.util.PlayerNotifications;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PacketResearchComplete extends BasePacket {
   private String key;

   public PacketResearchComplete(String key) {
      this.key = key;
   }

   public PacketResearchComplete(FriendlyByteBuf buf) {
      this.key = buf.readUtf();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeUtf(this.key);
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public void executeClient(Minecraft minecraft) {
      if (this.key != null && this.key.length() > 0) {
         LocalPlayer player = minecraft.player;
         ResearchApi.completeResearch(player, this.key);
         if (this.key.startsWith("@")) {
            PlayerNotifications.addNotification(Component.translatable("tc.addclue"));
            player.playSound((SoundEvent)ModSounds.LEARN.get(), 0.2F, 1.0F + player.level().random.nextFloat() * 0.1F);
         } else {
            ResearchItem researchItem = ResearchApi.getResearch(this.key);
            if (researchItem != null && !researchItem.isVirtual()) {
               minecraft.getToasts().addToast(new ResearchToast(researchItem));
               ResearchBrowserScreen.highlightedItem.add(this.key);
               ResearchBrowserScreen.highlightedItem.add(ResearchApi.getResearch(this.key).category);
            }
         }

         if (minecraft.screen instanceof ResearchBrowserScreen browser) {
            browser.updateResearch();
         }

         ThaumcraftJeiPlugin.refreshRecipeVisibility();
      }
   }
}
