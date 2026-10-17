package thaumcraft.block.warp_gate;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import thaumcraft.init.registry.ModItems;
import thaumcraft.item.sky_pearl.ItemSkyPearl;
import thaumcraft.network.PacketHandler;
import thaumcraft.network.misc.PacketWarpGateTeleport;

public class WarpGateDestinationsScreen extends Screen {
   private static final ResourceLocation ENDER_FIELD_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/end_portal.png");
   private final WarpGateBlockEntity gate;
   private double mapX;
   private double mapZ;
   private double lastMouseX;
   private double lastMouseY;
   private int ticks;

   public WarpGateDestinationsScreen(WarpGateBlockEntity gate) {
      super(Component.translatable("ttmisc.destinations"));
      this.gate = gate;
   }

   protected void init() {
      super.init();
      this.resetView();
   }

   public void tick() {
      super.tick();
      this.ticks++;
      if (this.minecraft == null || this.minecraft.player == null || this.minecraft.player.isRemoved()) {
         this.onClose();
      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (button == 0) {
         this.mapX -= dragX;
         this.mapZ -= dragY;
         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 32) {
         this.resetView();
         return true;
      } else {
         int slotIndex = -1;
         if (keyCode >= 49 && keyCode <= 57) {
            slotIndex = keyCode - 49;
         } else if (keyCode == 48) {
            slotIndex = 9;
         }

         if (slotIndex >= 0) {
            if (this.gate.getLevel() == null) {
               return true;
            } else {
               ItemStack stack = this.gate.getItem(slotIndex);
               if (!stack.isEmpty()
                  && ItemSkyPearl.isAttuned(stack)
                  && ItemSkyPearl.getDim(stack).equals(this.gate.getLevel().dimension().location().toString())) {
                  this.mapX = ItemSkyPearl.getX(stack) - this.width / 2.0;
                  this.mapZ = ItemSkyPearl.getZ(stack) - this.height / 2.0;
               }

               return true;
            }
         } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
         }
      }
   }

   public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(guiGraphics);
      List<Component> tooltip = new ArrayList<>();
      int gateScreenX = (int)(this.gate.getBlockPos().getX() - this.mapX);
      int gateScreenZ = (int)(this.gate.getBlockPos().getZ() - this.mapZ);
      this.renderConnectionLines(guiGraphics, gateScreenX, gateScreenZ);
      guiGraphics.drawCenteredString(this.font, Component.translatable("ttmisc.numberKeys"), this.width / 2, 5, 16777215);
      guiGraphics.drawCenteredString(this.font, Component.translatable("ttmisc.spaceToReset"), this.width / 2, 16, 16777215);
      guiGraphics.drawString(this.font, Component.translatable("ttmisc.destinations").withStyle(ChatFormatting.UNDERLINE), 3, 40, 16777215, false);
      this.drawPearlAt(guiGraphics, -1, ItemStack.EMPTY, gateScreenX, gateScreenZ, mouseX, mouseY, tooltip);
      if (this.gate.getLevel() != null) {
         String currentDim = this.gate.getLevel().dimension().location().toString();

         for (int i = 0; i < this.gate.getContainerSize(); i++) {
            ItemStack stack = this.gate.getItem(i);
            if (!stack.isEmpty() && ItemSkyPearl.isAttuned(stack) && currentDim.equals(ItemSkyPearl.getDim(stack)) && ItemSkyPearl.getY(stack) != -1) {
               int destX = (int)(ItemSkyPearl.getX(stack) - this.mapX);
               int destZ = (int)(ItemSkyPearl.getZ(stack) - this.mapZ);
               this.drawPearlAt(guiGraphics, i, stack, destX, destZ, mouseX, mouseY, tooltip);
            }
         }
      }

      if (!tooltip.isEmpty()) {
         guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
      }
   }

   private void renderConnectionLines(GuiGraphics guiGraphics, int gateX, int gateY) {
      if (this.gate.getLevel() != null) {
         String currentDim = this.gate.getLevel().dimension().location().toString();
         BufferBuilder buffer = Tesselator.getInstance().begin(Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShader(GameRenderer::getPositionColorShader);
         RenderSystem.lineWidth(2.0F);
         float alpha = (float)((Math.sin(this.ticks / 10.0) + 1.0) / 4.0 + 0.25);

         for (int i = 0; i < this.gate.getContainerSize(); i++) {
            ItemStack stack = this.gate.getItem(i);
            if (!stack.isEmpty() && ItemSkyPearl.isAttuned(stack) && currentDim.equals(ItemSkyPearl.getDim(stack)) && ItemSkyPearl.getY(stack) != -1) {
               int destX = (int)(ItemSkyPearl.getX(stack) - this.mapX);
               int destZ = (int)(ItemSkyPearl.getZ(stack) - this.mapZ);
               buffer.addVertex(gateX, gateY, 0.0F).setColor(1.0F, 1.0F, 1.0F, alpha);
               buffer.addVertex(destX, destZ, 0.0F).setColor(1.0F, 1.0F, 1.0F, alpha);
            }
         }

         BufferUploader.drawWithShader(buffer.buildOrThrow());
         RenderSystem.disableBlend();
      }
   }

   private void drawPearlAt(GuiGraphics guiGraphics, int index, ItemStack stack, int x, int y, int mouseX, int mouseY, List<Component> tooltip) {
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate(x, y, 0.0F);
      guiGraphics.pose().scale(0.5F, 0.5F, 1.0F);
      ItemStack renderStack = stack.isEmpty() ? new ItemStack((ItemLike)ModItems.SKY_PEARL.get()) : stack;
      guiGraphics.renderItem(renderStack, -8, -8);
      guiGraphics.pose().popPose();
      if (index >= 0) {
         String destName = stack.has(DataComponents.CUSTOM_NAME) ? stack.getHoverName().getString() : Component.translatable("ttmisc.destination").getString();
         guiGraphics.drawString(this.font, index + 1 + ": " + destName, 5, 54 + index * 11, 16777215, false);
      }

      if (mouseX >= x - 4 && mouseX <= x + 4 && mouseY >= y - 4 && mouseY <= y + 4) {
         String nameKey = index == -1 ? "ttmisc.entrancePoint" : "ttmisc.destination";
         Component nameComp = (Component)(stack.has(DataComponents.CUSTOM_NAME) ? stack.getHoverName() : Component.translatable(nameKey));
         if (index >= 0) {
            tooltip.add(
               Component.translatable("ttmisc.destinationInd", new Object[]{index + 1})
                  .withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.AQUA})
                  .append(" ")
                  .append(nameComp)
            );
            tooltip.add(Component.literal("X: " + ItemSkyPearl.getX(stack)));
            tooltip.add(Component.literal("Y: " + ItemSkyPearl.getY(stack)));
            tooltip.add(Component.literal("Z: " + ItemSkyPearl.getZ(stack)));
            tooltip.add(Component.translatable("ttmisc.clickToTeleport").withStyle(ChatFormatting.GRAY));
         } else {
            tooltip.add(Component.translatable("ttmisc.entrancePoint").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.literal("X: " + this.gate.getBlockPos().getX()));
            tooltip.add(Component.literal("Z: " + this.gate.getBlockPos().getZ()));
         }
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0 && hasShiftDown()) {
         if (this.gate.getLevel() == null) {
            return false;
         }

         String currentDim = this.gate.getLevel().dimension().location().toString();

         for (int i = 0; i < this.gate.getContainerSize(); i++) {
            ItemStack stack = this.gate.getItem(i);
            if (!stack.isEmpty() && ItemSkyPearl.isAttuned(stack) && currentDim.equals(ItemSkyPearl.getDim(stack)) && ItemSkyPearl.getY(stack) != -1) {
               int destX = (int)(ItemSkyPearl.getX(stack) - this.mapX);
               int destZ = (int)(ItemSkyPearl.getZ(stack) - this.mapZ);
               if (mouseX >= destX - 4 && mouseX <= destX + 4 && mouseY >= destZ - 4 && mouseY <= destZ + 4) {
                  PacketHandler.sendToServer(new PacketWarpGateTeleport(this.gate.getBlockPos(), i));
                  this.onClose();
                  return true;
               }
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   private void renderBackground(GuiGraphics p_283688_) {
      BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      RenderSystem.setShaderTexture(0, ENDER_FIELD_TEXTURE);
      float hue = (float)(Math.sin(this.ticks / 150.0) + 1.0) / 2.0F;
      int colorParams = Color.HSBtoRGB(hue, 0.5F, 0.4F);
      int r = colorParams >> 16 & 0xFF;
      int g = colorParams >> 8 & 0xFF;
      int b = colorParams & 0xFF;
      float f = 256.0F;
      float uvWidth = this.width / f;
      float uvHeight = this.height / f;
      buffer.addVertex(0.0F, this.height, 0.0F).setUv(0.0F, uvHeight).setColor(r, g, b, 255);
      buffer.addVertex(this.width, this.height, 0.0F).setUv(uvWidth, uvHeight).setColor(r, g, b, 255);
      buffer.addVertex(this.width, 0.0F, 0.0F).setUv(uvWidth, 0.0F).setColor(r, g, b, 255);
      buffer.addVertex(0.0F, 0.0F, 0.0F).setUv(0.0F, 0.0F).setColor(r, g, b, 255);
      BufferUploader.drawWithShader(buffer.buildOrThrow());
   }

   private void resetView() {
      this.mapX = this.gate.getBlockPos().getX() - this.width / 2.0;
      this.mapZ = this.gate.getBlockPos().getZ() - this.height / 2.0;
   }
}
