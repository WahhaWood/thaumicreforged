package thaumcraft.item.armor.hover_harness;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class HoverHarnessScreen extends AbstractContainerScreen<HoverHarnessMenu> {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/screen/hover_harness.png");
   private final int blockedSlotIndex;

   public HoverHarnessScreen(HoverHarnessMenu menu, Inventory inv, Component title) {
      super(menu, inv, Component.empty());
      this.blockedSlotIndex = inv.selected;
      this.imageWidth = 176;
      this.imageHeight = 166;
   }

   protected boolean checkHotbarKeyPressed(int keyCode, int scanCode) {
      return this.minecraft != null && this.minecraft.options.keyHotbarSlots[this.blockedSlotIndex].matches(keyCode, scanCode)
         ? true
         : super.checkHotbarKeyPressed(keyCode, scanCode);
   }

   public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(gg, mouseX, mouseY, partialTick);
      super.render(gg, mouseX, mouseY, partialTick);
      RenderSystem.enableBlend();
      gg.blit(TEX, this.leftPos + 8 + this.blockedSlotIndex * 18, this.topPos + 142, 240.0F, 0.0F, 16, 16, 256, 256);
      RenderSystem.disableBlend();
      this.renderTooltip(gg, mouseX, mouseY);
   }

   protected void renderBg(GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      gg.blit(TEX, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
   }
}
