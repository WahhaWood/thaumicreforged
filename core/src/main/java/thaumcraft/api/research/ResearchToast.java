package thaumcraft.api.research;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.Toast.Visibility;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ScreenApi;
import thaumcraft.util.InventoryUtils;
import thaumcraft.util.UtilsFX;

@OnlyIn(Dist.CLIENT)
public class ResearchToast implements Toast {
   public static final int DISPLAY_TIME = 3000;
   private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/toasts.png");
   private final ResearchItem researchItem;

   public ResearchToast(ResearchItem researchItem) {
      this.researchItem = researchItem;
   }

   @NotNull
   @OnlyIn(Dist.CLIENT)
   public Visibility render(@NotNull GuiGraphics graphics, @NotNull ToastComponent toast, long timeWork) {
      Component name = this.researchItem.getName();
      Minecraft minecraft = toast.getMinecraft();
      Font font = minecraft.font;
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      graphics.blit(TEXTURE, 0, 0, 0, 0, this.width(), this.height());
      graphics.drawString(font, Component.translatable("tc.overlay.info.research_completed"), 30, 7, -256);
      int stringWidth = font.width(name);
      if (stringWidth <= 125) {
         graphics.drawString(font, name, 30, 18, -1);
      } else {
         float scaled = 125.0F / stringWidth;
         PoseStack pose = graphics.pose();
         pose.pushPose();
         pose.translate(30.0F, 16.0F + 2.0F / scaled, 0.0F);
         pose.scale(scaled, scaled, scaled);
         graphics.drawString(font, name, 0, 0, -1);
         pose.popPose();
      }

      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      if (this.researchItem.icon instanceof Ingredient ingredient) {
         graphics.renderFakeItem(InventoryUtils.cycleItemStack(ingredient), 8, 8);
      } else if (this.researchItem.icon instanceof Block block) {
         ScreenApi.renderBlock(graphics, block.defaultBlockState(), 8, 8);
      } else if (this.researchItem.icon instanceof ResourceLocation resourceLocation) {
         UtilsFX.drawTexturedQuadFull(graphics, resourceLocation, 8, 8, 0);
      }

      return timeWork >= 3000.0 * toast.getNotificationDisplayTimeMultiplier() ? Visibility.HIDE : Visibility.SHOW;
   }
}
