package thaumcraft.entities.smart.pech.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.entities.smart.pech.PechMenu;

public class PechScreen extends AbstractContainerScreen<PechMenu> {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/screen/pech.png");

   public PechScreen(PechMenu menu, Inventory inv, Component title) {
      super(menu, inv, title);
      this.imageWidth = 175;
      this.imageHeight = 232;
   }

   protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
      int x = this.leftPos;
      int y = this.topPos;
      g.blit(TEX, x, y, 0, 0, this.imageWidth, this.imageHeight);
      if (((PechMenu)this.menu).isReadyToTrade()) {
         g.blit(TEX, x + 67, y + 24, 176, 0, 25, 25);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      int x = this.leftPos;
      int y = this.topPos;
      double rx = mouseX - (x + 67);
      double ry = mouseY - (y + 24);
      if (rx >= 0.0 && ry >= 0.0 && rx < 25.0 && ry < 25.0 && ((PechMenu)this.menu).isReadyToTrade()) {
         Minecraft mc = this.minecraft;
         if (mc != null && mc.gameMode != null) {
            mc.gameMode.handleInventoryButtonClick(((PechMenu)this.menu).containerId, 0);
            this.playButton();
            return true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   private void playButton() {
      SoundEvent s = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", "pech_dice"));
      if (s != null) {
         Minecraft mc = this.minecraft;
         if (mc != null && mc.player != null) {
            mc.player
               .level()
               .playLocalSound(
                  mc.player.getX(),
                  mc.player.getY(),
                  mc.player.getZ(),
                  s,
                  SoundSource.PLAYERS,
                  0.5F,
                  0.95F + mc.player.level().getRandom().nextFloat() * 0.1F,
                  false
               );
         }
      }
   }
}
