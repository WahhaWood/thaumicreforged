package thaumicenergistics.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import thaumicenergistics.item.ItemGolemWirelessBackpack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import thaumcraft.entities.smart.golem.EntityGolemBase;

@OnlyIn(Dist.CLIENT)
public class GolemBackpackRenderer {
   private static final ResourceLocation DEFAULT_TEXTURE = ItemGolemWirelessBackpack.BackpackSkins.Thaumium.getTextureLocation();

   public static void render(
      EntityGolemBase golem,
      WirelessGolemHandler.WirelessClientData wcd,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      float partialTick
   ) {
      boolean golemActive = wcd.isInRange && !golem.inactive;
      poseStack.pushPose();
      poseStack.translate(0.0F, 0.64F, 0.0F);
      float scale = 0.8F;
      poseStack.scale(scale, scale, scale);
      float golemYaw = 270.0F;
      golemYaw -= golem.yBodyRot * partialTick + golem.yBodyRotO * (1.0F - partialTick);
      poseStack.mulPose(Axis.YP.rotationDegrees(golemYaw));
      poseStack.translate(-0.16F, 0.0F, 0.0F);
      ResourceLocation texture = wcd.skin != null ? wcd.skin.getTextureLocation() : DEFAULT_TEXTURE;
      VertexConsumer vc = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
      float r = 1.0F;
      float g = 1.0F;
      float b = 1.0F;
      if (golem.hurtTime > 0) {
         r = 1.0F;
         g = 0.0F;
         b = 0.0F;
      } else if (golem.healing > 0) {
         r = 0.5F;
         g = 1.0F;
         b = 0.5F;
      }

      float f5 = 0.0625F;
      renderBox(poseStack, vc, packedLight, r, g, b, -1.0F * f5, -3.0F * f5, -3.0F * f5, 2.0F * f5, 6.0F * f5, 6.0F * f5, 0, 0, 16, 16);
      renderBox(poseStack, vc, packedLight, r, g, b, -1.5F * f5, -1.0F * f5, -2.0F * f5, 1.0F * f5, 2.0F * f5, 4.0F * f5, 2, 0, 16, 16);
      renderBox(poseStack, vc, packedLight, r, g, b, -0.5F * f5, -6.0F * f5, -0.5F * f5, 1.0F * f5, 3.0F * f5, 1.0F * f5, 10, 0, 16, 16);
      float pearlR = golemActive ? 1.0F : 1.0F;
      float pearlG = golemActive ? 1.0F : 0.0F;
      float pearlB = golemActive ? 1.0F : 0.0F;
      poseStack.pushPose();
      poseStack.translate(0.0F, -5.5F * f5, 0.0F);
      float pearlSize = 0.2F * f5;
      poseStack.scale(3.0F, 3.0F, 3.0F);
      poseStack.mulPose(Axis.YP.rotationDegrees(wcd.pearlRotation));
      renderPearl(poseStack, vc, packedLight, pearlR, pearlG, pearlB, pearlSize);
      poseStack.popPose();
      poseStack.popPose();
   }

   private static void renderPearl(PoseStack poseStack, VertexConsumer vc, int packedLight, float r, float g, float b, float size) {
      Matrix4f matrix = poseStack.last().pose();
      Matrix3f normal = poseStack.last().normal();
      float minU = 0.5F;
      float maxU = 0.84375F;
      float minV = 0.375F;
      float maxV = 0.71875F;
      float s = size * 5.0F;
      vc.addVertex(matrix, -s, -s, s)
         .setColor(r, g, b, 1.0F)
         .setUv(minU, maxV)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 0.0F, 1.0F);
      vc.addVertex(matrix, s, -s, s)
         .setColor(r, g, b, 1.0F)
         .setUv(maxU, maxV)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 0.0F, 1.0F);
      vc.addVertex(matrix, s, s, s)
         .setColor(r, g, b, 1.0F)
         .setUv(maxU, minV)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 0.0F, 1.0F);
      vc.addVertex(matrix, -s, s, s)
         .setColor(r, g, b, 1.0F)
         .setUv(minU, minV)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 0.0F, 1.0F);
      vc.addVertex(matrix, -s, s, -s)
         .setColor(r, g, b, 1.0F)
         .setUv(minU, minV)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 0.0F, -1.0F);
      vc.addVertex(matrix, s, s, -s)
         .setColor(r, g, b, 1.0F)
         .setUv(maxU, minV)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 0.0F, -1.0F);
      vc.addVertex(matrix, s, -s, -s)
         .setColor(r, g, b, 1.0F)
         .setUv(maxU, maxV)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 0.0F, -1.0F);
      vc.addVertex(matrix, -s, -s, -s)
         .setColor(r, g, b, 1.0F)
         .setUv(minU, maxV)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 0.0F, -1.0F);
      vc.addVertex(matrix, s, -s, s)
         .setColor(r, g, b, 1.0F)
         .setUv(minU, maxV)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(1.0F, 0.0F, 0.0F);
      vc.addVertex(matrix, s, -s, -s)
         .setColor(r, g, b, 1.0F)
         .setUv(maxU, maxV)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(1.0F, 0.0F, 0.0F);
      vc.addVertex(matrix, s, s, -s)
         .setColor(r, g, b, 1.0F)
         .setUv(maxU, minV)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(1.0F, 0.0F, 0.0F);
      vc.addVertex(matrix, s, s, s)
         .setColor(r, g, b, 1.0F)
         .setUv(minU, minV)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(1.0F, 0.0F, 0.0F);
      vc.addVertex(matrix, -s, s, s)
         .setColor(r, g, b, 1.0F)
         .setUv(minU, minV)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(-1.0F, 0.0F, 0.0F);
      vc.addVertex(matrix, -s, s, -s)
         .setColor(r, g, b, 1.0F)
         .setUv(maxU, minV)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(-1.0F, 0.0F, 0.0F);
      vc.addVertex(matrix, -s, -s, -s)
         .setColor(r, g, b, 1.0F)
         .setUv(maxU, maxV)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(-1.0F, 0.0F, 0.0F);
      vc.addVertex(matrix, -s, -s, s)
         .setColor(r, g, b, 1.0F)
         .setUv(minU, maxV)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(-1.0F, 0.0F, 0.0F);
   }

   private static void renderBox(
      PoseStack poseStack,
      VertexConsumer vc,
      int packedLight,
      float r,
      float g,
      float b,
      float x,
      float y,
      float z,
      float w,
      float h,
      float d,
      int texU,
      int texV,
      int texW,
      int texH
   ) {
      Matrix4f matrix = poseStack.last().pose();
      Matrix3f normal = poseStack.last().normal();
      float x2 = x + w;
      float y2 = y + h;
      float z2 = z + d;
      float u0 = (float)texU / (float)texW;
      float u1 = (float)(texU + 4) / (float)texW;
      float v0 = (float)texV / (float)texH;
      float v1 = (float)(texV + 4) / (float)texH;
      vc.addVertex(matrix, x, y, z2)
         .setColor(r, g, b, 1.0F)
         .setUv(u0, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 0.0F, 1.0F);
      vc.addVertex(matrix, x2, y, z2)
         .setColor(r, g, b, 1.0F)
         .setUv(u1, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 0.0F, 1.0F);
      vc.addVertex(matrix, x2, y2, z2)
         .setColor(r, g, b, 1.0F)
         .setUv(u1, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 0.0F, 1.0F);
      vc.addVertex(matrix, x, y2, z2)
         .setColor(r, g, b, 1.0F)
         .setUv(u0, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 0.0F, 1.0F);
      vc.addVertex(matrix, x, y2, z)
         .setColor(r, g, b, 1.0F)
         .setUv(u0, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 0.0F, -1.0F);
      vc.addVertex(matrix, x2, y2, z)
         .setColor(r, g, b, 1.0F)
         .setUv(u1, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 0.0F, -1.0F);
      vc.addVertex(matrix, x2, y, z)
         .setColor(r, g, b, 1.0F)
         .setUv(u1, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 0.0F, -1.0F);
      vc.addVertex(matrix, x, y, z)
         .setColor(r, g, b, 1.0F)
         .setUv(u0, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 0.0F, -1.0F);
      vc.addVertex(matrix, x, y2, z2)
         .setColor(r, g, b, 1.0F)
         .setUv(u0, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(matrix, x2, y2, z2)
         .setColor(r, g, b, 1.0F)
         .setUv(u1, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(matrix, x2, y2, z)
         .setColor(r, g, b, 1.0F)
         .setUv(u1, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(matrix, x, y2, z)
         .setColor(r, g, b, 1.0F)
         .setUv(u0, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(matrix, x, y, z)
         .setColor(r, g, b, 1.0F)
         .setUv(u0, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, -1.0F, 0.0F);
      vc.addVertex(matrix, x2, y, z)
         .setColor(r, g, b, 1.0F)
         .setUv(u1, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, -1.0F, 0.0F);
      vc.addVertex(matrix, x2, y, z2)
         .setColor(r, g, b, 1.0F)
         .setUv(u1, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, -1.0F, 0.0F);
      vc.addVertex(matrix, x, y, z2)
         .setColor(r, g, b, 1.0F)
         .setUv(u0, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, -1.0F, 0.0F);
      vc.addVertex(matrix, x2, y, z2)
         .setColor(r, g, b, 1.0F)
         .setUv(u0, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(1.0F, 0.0F, 0.0F);
      vc.addVertex(matrix, x2, y, z)
         .setColor(r, g, b, 1.0F)
         .setUv(u1, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(1.0F, 0.0F, 0.0F);
      vc.addVertex(matrix, x2, y2, z)
         .setColor(r, g, b, 1.0F)
         .setUv(u1, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(1.0F, 0.0F, 0.0F);
      vc.addVertex(matrix, x2, y2, z2)
         .setColor(r, g, b, 1.0F)
         .setUv(u0, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(1.0F, 0.0F, 0.0F);
      vc.addVertex(matrix, x, y, z)
         .setColor(r, g, b, 1.0F)
         .setUv(u0, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(-1.0F, 0.0F, 0.0F);
      vc.addVertex(matrix, x, y, z2)
         .setColor(r, g, b, 1.0F)
         .setUv(u1, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(-1.0F, 0.0F, 0.0F);
      vc.addVertex(matrix, x, y2, z2)
         .setColor(r, g, b, 1.0F)
         .setUv(u1, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(-1.0F, 0.0F, 0.0F);
      vc.addVertex(matrix, x, y2, z)
         .setColor(r, g, b, 1.0F)
         .setUv(u0, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(-1.0F, 0.0F, 0.0F);
   }
}
