package thaumicenergistics.client.render;

import appeng.api.client.AEKeyRenderHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import thaumicenergistics.ae2.storage.AEAspectKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.util.UtilsFX;

public class AspectKeyRenderHandler implements AEKeyRenderHandler<AEAspectKey> {
   public void drawInGui(Minecraft minecraft, GuiGraphics guiGraphics, int x, int y, AEAspectKey key) {
      Aspect aspect = key.getAspect();
      if (aspect != null) {
         UtilsFX.drawTag(guiGraphics, x, y, aspect, 0.0F, 0, 0.0F);
      }
   }

   public void drawOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, AEAspectKey key, float scale, int combinedLightIn, Level level) {
      Aspect aspect = key.getAspect();
      if (aspect != null) {
         ResourceLocation texture = aspect.getImage();
         VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent(texture));
         poseStack.pushPose();
         poseStack.translate(0.0F, 0.0F, 0.01F);
         scale -= 0.05F;
         float x0 = -scale / 2.0F;
         float y0 = scale / 2.0F;
         float x1 = scale / 2.0F;
         float y1 = -scale / 2.0F;
         int color = aspect.getColor();
         float r = (float)ARGB32.red(color) / 255.0F;
         float g = (float)ARGB32.green(color) / 255.0F;
         float b = (float)ARGB32.blue(color) / 255.0F;
         Matrix4f transform = poseStack.last().pose();
         buffer.addVertex(transform, x0, y1, 0.0F)
            .setColor(r, g, b, 1.0F)
            .setUv(0.0F, 1.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(combinedLightIn)
            .setNormal(0.0F, 0.0F, 1.0F);
         buffer.addVertex(transform, x1, y1, 0.0F)
            .setColor(r, g, b, 1.0F)
            .setUv(1.0F, 1.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(combinedLightIn)
            .setNormal(0.0F, 0.0F, 1.0F);
         buffer.addVertex(transform, x1, y0, 0.0F)
            .setColor(r, g, b, 1.0F)
            .setUv(1.0F, 0.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(combinedLightIn)
            .setNormal(0.0F, 0.0F, 1.0F);
         buffer.addVertex(transform, x0, y0, 0.0F)
            .setColor(r, g, b, 1.0F)
            .setUv(0.0F, 0.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(combinedLightIn)
            .setNormal(0.0F, 0.0F, 1.0F);
         poseStack.popPose();
      }
   }

   public Component getDisplayName(AEAspectKey key) {
      Aspect aspect = key.getAspect();
      return aspect != null ? aspect.getName() : Component.empty();
   }

   public List<Component> getTooltip(AEAspectKey key) {
      Aspect aspect = key.getAspect();
      if (aspect == null) {
         return List.of(Component.empty());
      } else {
         List<Component> tooltip = new ArrayList<>();
         tooltip.add(aspect.getName());
         tooltip.add(aspect.getLocalizedDescription());
         return tooltip;
      }
   }
}
