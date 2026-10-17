package thaumcraft.event.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import thaumcraft.network.playerdata.PacketSyncSoulHearts;

/**
 * Отрисовка «душевных сердец» рядом с полосой голода (порт {@code SoulHeartClientHandler} из TT2).
 * Текстура 16x9: слева полное сердце 9x9, справа половина.
 */
@OnlyIn(Dist.CLIENT)
public class SoulHeartClientHandler {
   private static final ResourceLocation SOUL_HEARTS = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/gui/soul_hearts.png");

   @SubscribeEvent
   public void onRenderGui(RenderGuiEvent.Post event) {
      int hearts = PacketSyncSoulHearts.clientHearts;
      Minecraft minecraft = Minecraft.getInstance();
      if (hearts <= 0 || minecraft.player == null || minecraft.options.hideGui) {
         return;
      }

      int x = event.getGuiGraphics().guiWidth() / 2 + 10;
      int y = event.getGuiGraphics().guiHeight() - 29;
      int index = 0;

      for (int i = 0; i < hearts; i++) {
         boolean half = i == hearts - 1 && hearts % 2 != 0;
         if (half || i % 2 == 0) {
            event.getGuiGraphics()
               .blit(
                  SOUL_HEARTS,
                  x + index * 8,
                  y,
                  half ? 9 : 0,
                  0,
                  half ? 7 : 9,
                  9,
                  16,
                  9
               );
            index++;
         }
      }
   }
}
