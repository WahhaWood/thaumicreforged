package thaumcraft.entities.smart.travelling_trunk.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import thaumcraft.entities.smart.travelling_trunk.EntityTravelingTrunk;
import thaumcraft.entities.smart.travelling_trunk.inventory.MenuTravelingTrunk;
import thaumcraft.item.golem_upgrade.GolemUpgrade;

public class TravelingTrunkScreen extends AbstractContainerScreen<MenuTravelingTrunk> {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/screen/guitrunkbase.png");
   private static final int STAY_BUTTON_X = 112;
   private static final int STAY_BUTTON_Y = 0;
   private static final int STAY_BUTTON_SIZE = 10;
   private final EntityTravelingTrunk trunk;

   public TravelingTrunkScreen(MenuTravelingTrunk menu, Inventory inv, Component title) {
      super(menu, inv, title);
      this.trunk = menu.getTrunk();
      this.imageHeight = 200;
      this.imageWidth = 176;
   }

   protected void renderLabels(@NotNull GuiGraphics gg, int mouseX, int mouseY) {
      gg.pose().pushPose();
      gg.pose().scale(0.5F, 0.5F, 0.5F);
      Component ownerName = Component.literal(this.trunk.func_152113_b());
      Component guiName = Component.translatable("entity.trunk.guiname");
      gg.drawString(this.font, ownerName.copy().append(guiName), 8, 4, 12623984, false);
      gg.pose().popPose();
   }

   protected void renderBg(@NotNull GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
      if (this.trunk != null && this.trunk.isAlive()) {
         RenderSystem.enableBlend();
         gg.blit(TEX, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
         int healthWidth = Math.round(this.trunk.getHealth() / this.trunk.getMaxHealth() * 39.0F);
         gg.blit(TEX, this.leftPos + 134, this.topPos + 2, 176.0F, 16.0F, healthWidth, 6, 256, 256);
         if (this.trunk.hasUpgrade(GolemUpgrade.EARTH)) {
            gg.blit(TEX, this.leftPos, this.topPos + 80, 0.0F, 206.0F, this.imageWidth, 27, 256, 256);
         }

         if (this.trunk.getStay()) {
            gg.blit(TEX, this.leftPos + 112, this.topPos + 0, 176.0F, 0.0F, 10, 10, 256, 256);
         }

         RenderSystem.disableBlend();
      } else {
         this.closeScreen();
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return this.handleStayButtonClick(mouseX, mouseY) ? true : super.mouseClicked(mouseX, mouseY, button);
   }

   private boolean handleStayButtonClick(double mouseX, double mouseY) {
      int relX = (int)mouseX - (this.leftPos + 112);
      int relY = (int)mouseY - (this.topPos + 0);
      if (relX >= 0 && relY >= 0 && relX < 10 && relY <= 10) {
         if (this.minecraft != null && this.minecraft.player != null) {
            float pitch = 0.6F + (this.trunk.getStay() ? 0.0F : 0.2F);
            this.minecraft.player.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 0.3F, pitch);
            String messageKey = this.trunk.getStay() ? "entity.trunk.move" : "entity.trunk.stay";
            this.minecraft.player.displayClientMessage(Component.translatable(messageKey), false);
            if (this.minecraft.gameMode != null) {
               this.minecraft.gameMode.handleInventoryButtonClick(((MenuTravelingTrunk)this.menu).containerId, 1);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private void closeScreen() {
      if (this.minecraft != null && this.minecraft.player != null) {
         this.minecraft.player.closeContainer();
      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void removed() {
      if (this.trunk != null) {
         this.trunk.setOpen(false);
      }

      super.removed();
   }

   public void render(@NotNull GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(gg, mouseX, mouseY, partialTick);
      super.render(gg, mouseX, mouseY, partialTick);
      this.renderTooltip(gg, mouseX, mouseY);
   }
}
