package thaumcraft.item.ichor_pouch;

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
public class IchorPouchScreen extends AbstractContainerScreen<IchorPouchMenu> {
   private final ResourceLocation texture = Thaumcraft.location("textures/screen/ichor_pouch.png");

   public IchorPouchScreen(IchorPouchMenu menu, Inventory inventory, Component component) {
      super(menu, inventory, component);
      this.imageWidth = 256;
      this.imageHeight = 256;
   }

   protected void renderBg(@NotNull GuiGraphics graphics, float pt, int mx, int my) {
      if (!((IchorPouchMenu)this.menu).stillValid(this.minecraft.player)) {
         this.minecraft.player.closeContainer();
      } else {
         RenderSystem.enableBlend();
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         graphics.blit(this.texture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
         RenderSystem.disableBlend();
      }
   }

   protected void renderLabels(@NotNull GuiGraphics graphics, int mx, int my) {
      PoseStack pose = graphics.pose();
      pose.pushPose();
      pose.translate(0.0F, 0.0F, 200.0F);
      RenderSystem.enableBlend();
      int hotbarIndex = ((IchorPouchMenu)this.menu).getBlockedHotbarIndex();
      int hotbarX = 48 + hotbarIndex * 18;
      int hotbarY = 235;
      graphics.fill(hotbarX, hotbarY, hotbarX + 16, hotbarY + 16, -2130771968);
      RenderSystem.disableBlend();
      pose.popPose();
   }

   protected boolean checkHotbarKeyPressed(int keyCode, int scanCode) {
      return false;
   }
}
