package thaumcraft.block.jar.render.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public final class JarRenderUtil {
   public static final ResourceLocation LIQUID_SPRITE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/animatedglow");

   private JarRenderUtil() {
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderFluidBox(
      PoseStack poseStack,
      MultiBufferSource buffers,
      float x0,
      float y0,
      float z0,
      float x1,
      float y1,
      float z1,
      int color,
      float alpha,
      int light,
      int overlay
   ) {
      TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(LIQUID_SPRITE);
      VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      renderBox(poseStack, consumer, sprite, x0, y0, z0, x1, y1, z1, r, g, b, alpha, light, overlay);
   }

   @OnlyIn(Dist.CLIENT)
   private static void renderBox(
      PoseStack pose,
      VertexConsumer vc,
      TextureAtlasSprite sprite,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float r,
      float g,
      float b,
      float a,
      int light,
      int overlay
   ) {
      float u0 = sprite.getU0();
      float u1 = sprite.getU1();
      float v0 = sprite.getV0();
      float v1 = sprite.getV1();
      renderBoxUV(pose, vc, x1, y1, z1, x2, y2, z2, u0, u1, v0, v1, r, g, b, a, light, overlay);
   }

   @OnlyIn(Dist.CLIENT)
   private static void renderBoxUV(
      PoseStack pose,
      VertexConsumer vc,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float u0,
      float u1,
      float v0,
      float v1,
      float r,
      float g,
      float b,
      float a,
      int light,
      int overlay
   ) {
      Pose last = pose.last();
      Matrix4f m = last.pose();
      Matrix3f n = last.normal();
      quad(vc, m, n, x1, y1, z2, x2, y1, z2, x2, y1, z1, x1, y1, z1, u0, v0, u1, v1, r, g, b, a, light, overlay, 0.0F, -1.0F, 0.0F);
      quad(vc, m, n, x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, u0, v0, u1, v1, r, g, b, a, light, overlay, 0.0F, 1.0F, 0.0F);
      quad(vc, m, n, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, u0, v0, u1, v1, r, g, b, a, light, overlay, 0.0F, 0.0F, -1.0F);
      quad(vc, m, n, x2, y1, z2, x1, y1, z2, x1, y2, z2, x2, y2, z2, u0, v0, u1, v1, r, g, b, a, light, overlay, 0.0F, 0.0F, 1.0F);
      quad(vc, m, n, x1, y1, z2, x1, y1, z1, x1, y2, z1, x1, y2, z2, u0, v0, u1, v1, r, g, b, a, light, overlay, -1.0F, 0.0F, 0.0F);
      quad(vc, m, n, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1, u0, v0, u1, v1, r, g, b, a, light, overlay, 1.0F, 0.0F, 0.0F);
   }

   @OnlyIn(Dist.CLIENT)
   private static void quad(
      VertexConsumer vc,
      Matrix4f m,
      Matrix3f n,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      float u0,
      float v0,
      float u1,
      float v1,
      float r,
      float g,
      float b,
      float a,
      int light,
      int overlay,
      float nx,
      float ny,
      float nz
   ) {
      vert(vc, m, n, x1, y1, z1, u0, v1, r, g, b, a, light, overlay, nx, ny, nz);
      vert(vc, m, n, x2, y2, z2, u1, v1, r, g, b, a, light, overlay, nx, ny, nz);
      vert(vc, m, n, x3, y3, z3, u1, v0, r, g, b, a, light, overlay, nx, ny, nz);
      vert(vc, m, n, x4, y4, z4, u0, v0, r, g, b, a, light, overlay, nx, ny, nz);
   }

   @OnlyIn(Dist.CLIENT)
   public static void quad(
      PoseStack poseStack,
      VertexConsumer vc,
      float x0,
      float y0,
      float z0,
      float x1,
      float y1,
      float z1,
      float u0,
      float v0,
      float u1,
      float v1,
      int light,
      int overlay,
      float r,
      float g,
      float b,
      float a,
      float nx,
      float ny,
      float nz
   ) {
      Pose pose = poseStack.last();
      Matrix4f mat = pose.pose();
      Matrix3f nmat = pose.normal();
      vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setUv2(light & 65535, light >> 16).setNormal(nx, ny, nz);
      vc.addVertex(mat, x1, y0, z0).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setUv2(light & 65535, light >> 16).setNormal(nx, ny, nz);
      vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setUv2(light & 65535, light >> 16).setNormal(nx, ny, nz);
      vc.addVertex(mat, x0, y1, z1).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setUv2(light & 65535, light >> 16).setNormal(nx, ny, nz);
   }

   @OnlyIn(Dist.CLIENT)
   private static void vert(
      VertexConsumer vc,
      Matrix4f m,
      Matrix3f n,
      float x,
      float y,
      float z,
      float u,
      float v,
      float r,
      float g,
      float b,
      float a,
      int light,
      int overlay,
      float nx,
      float ny,
      float nz
   ) {
      vc.addVertex(m, x, y, z).setColor(r, g, b, a).setUv(u, v).setOverlay(overlay).setUv2(light & 65535, light >> 16).setNormal(nx, ny, nz);
   }
}
