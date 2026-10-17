package thaumcraft.item.focus_pouch;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;

@OnlyIn(Dist.CLIENT)
public class FocusPouchScreen extends AbstractContainerScreen<FocusPouchMenu> {
   private final ResourceLocation texture = Thaumcraft.location("textures/screen/focus_pouch.png");
   private final int blockSlot;

   public FocusPouchScreen(FocusPouchMenu menu, Inventory inventory, Component component) {
      super(menu, inventory, component);
      this.blockSlot = inventory.selected;
      this.imageWidth = 175;
      this.imageHeight = 232;
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float pt, int mx, int my) {
      if (this.minecraft.player.getInventory().getItem(this.blockSlot).isEmpty()) {
         this.minecraft.player.closeContainer();
      }

      RenderSystem.enableBlend();
      graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.blit(this.texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
      RenderSystem.disableBlend();
   }

   protected void renderLabels(@NotNull GuiGraphics graphics, int mx, int my) {
      super.renderLabels(graphics, mx, my);
      PoseStack pose = graphics.pose();
      pose.pushPose();
      pose.translate(0.0F, 0.0F, 200.0F);
      RenderSystem.enableBlend();
      graphics.blit(this.texture, this.blockSlot * 18, 209, 240, 0, 16, 16);
      RenderSystem.disableBlend();
      pose.popPose();
   }

   protected boolean checkHotbarKeyPressed(int p_97806_, int p_97807_) {
      return false;
   }
}
