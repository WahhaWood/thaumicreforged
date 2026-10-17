package thaumcraft.block.infusion_claw.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ColorHelper;
import thaumcraft.block.infusion_claw.InfusionClawBlockEntity;
import thaumcraft.block.infusion_claw.InfusionClawMenu;

public class InfusionClawScreen extends AbstractContainerScreen<InfusionClawMenu> {
   public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/screen/infusion_claw.png");

   public InfusionClawScreen(InfusionClawMenu menu, Inventory inv, Component title) {
      super(menu, inv, title);
      this.imageWidth = 176;
      this.imageHeight = 166;
   }

   protected void renderLabels(@NotNull GuiGraphics gg, int mouseX, int mouseY) {
      InfusionClawBlockEntity tile = ((InfusionClawMenu)this.menu).getClaw();
      if (tile.isRemoved()) {
         Minecraft.getInstance().setScreen(null);
      } else {
         ItemStack carried = ((InfusionClawMenu)this.menu).getCarried();
         if (tile.isRunning() || !carried.isEmpty() && !tile.isItemValidForSlot(0, carried)) {
            RenderSystem.enableBlend();
            gg.setColor(1.0F, 1.0F, 1.0F, 0.5F);
            gg.blit(BACKGROUND, 80, 32, 240, 0, 16, 16);
            gg.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
         }
      }
   }

   protected void renderBg(@NotNull GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
      gg.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
      int baseX = this.leftPos;
      int baseY = this.topPos;
      gg.blit(BACKGROUND, baseX + 114, baseY + 36, 244, 19, 8, 8);
      boolean locked = ((InfusionClawMenu)this.menu).getClaw().isLocked();
      if (locked) {
         gg.blit(BACKGROUND, baseX + 114, baseY + 36, 244, 27, 8, 8);
      }

      gg.pose().pushPose();
      gg.pose().translate(baseX + 124, baseY + 38, 0.0F);
      gg.pose().scale(0.5F, 0.5F, 1.0F);
      gg.drawString(this.font, I18n.get("crew.info.InfusionClaw.lock", new Object[0]), 0, 0, ColorHelper.toHex(locked ? Color.WHITE : Color.GRAY), false);
      gg.pose().popPose();
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      boolean ret = super.mouseClicked(mouseX, mouseY, button);
      int baseX = (this.width - this.imageWidth) / 2;
      int baseY = (this.height - this.imageHeight) / 2;
      int checkX = (int)mouseX - baseX - 114;
      int checkY = (int)mouseY - baseY - 36;
      if (checkX >= 0 && checkX < 8 && checkY >= 0 && checkY < 8) {
         InfusionClawBlockEntity tile = ((InfusionClawMenu)this.menu).getClaw();
         int value = tile.isLocked() ? 0 : 1;
         Minecraft.getInstance().gameMode.handleInventoryButtonClick(((InfusionClawMenu)this.menu).containerId, value);
         tile.setIsLocked(!tile.isLocked());
         return true;
      } else {
         return ret;
      }
   }
}
