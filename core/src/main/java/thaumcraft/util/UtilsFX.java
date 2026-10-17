package thaumcraft.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.CullStateShard;
import net.minecraft.client.renderer.RenderStateShard.DepthTestStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard;
import net.minecraft.client.renderer.RenderStateShard.WriteMaskStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.RenderType.CompositeState.CompositeStateBuilder;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.init.registry.Config;
import thaumcraft.particles.fx.ParticleUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class UtilsFX {
   public static String[] colorNames = new String[]{
      "White", "Orange", "Magenta", "Light Blue", "Yellow", "Lime", "Pink", "Gray", "Light Gray", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black"
   };
   public static String[] colorCodes = new String[]{"§f", "§6", "§d", "§9", "§e", "§a", "§d", "§8", "§7", "§b", "§5", "§9", "§4", "§2", "§c", "§8"};
   public static int[] colors = new int[]{
      15790320, 15435844, 12801229, 6719955, 14602026, 4312372, 14188952, 4408131, 10526880, 2651799, 8073150, 2437522, 5320730, 3887386, 11743532, 1973019
   };
   public static int[] connectedTextureRefByID;
   public static float[] lightBrightnessTable = null;
   private static Map textureSizeCache = new HashMap();
   static Map<String, ResourceLocation> boundTextures = new HashMap<>();
   static DecimalFormat myFormatter = new DecimalFormat("#######.##");
   private static final Map<ResourceLocation, int[]> TEX_SIZE_CACHE = new HashMap<>();
   public static final DepthTestStateShard TC_ALWAYS_DEPTH = new DepthTestStateShard("tc_always", 519) {
      public void setupRenderState() {
         RenderSystem.enableDepthTest();
         RenderSystem.depthFunc(519);
      }

      public void clearRenderState() {
         RenderSystem.depthFunc(515);
      }
   };
   public static final RenderType SCAN_XRAY_LINES = RenderType.create(
      "thaumcraft_scan_xray_lines",
      DefaultVertexFormat.POSITION_COLOR_NORMAL,
      Mode.LINES,
      256,
      false,
      false,
      CompositeState.builder()
         .setShaderState(new ShaderStateShard(GameRenderer::getRendertypeLinesShader))
         .setTransparencyState(new TransparencyStateShard("tc_translucent", () -> {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
         }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
         }))
         .setCullState(new CullStateShard(false))
         .setWriteMaskState(new WriteMaskStateShard(true, false))
         .setDepthTestState(TC_ALWAYS_DEPTH)
         .createCompositeState(false)
   );

   public static float getBrightnessFromLight(int n) {
      if (lightBrightnessTable == null) {
         lightBrightnessTable = new float[16];
         float n2 = 0.0F;

         for (int i = 0; i <= 15; i++) {
            float n3 = 1.0F - i / 15.0F;
            lightBrightnessTable[i] = (1.0F - n3) / (n3 * 3.0F + 1.0F) * (1.0F - n2) + n2;
         }
      }

      return lightBrightnessTable[n];
   }

   public static void infusedStoneSparkle(Level level, BlockPos blockPos, int n4) {
      if (level.isClientSide) {
         switch (n4) {
            case 1:
               boolean var9 = true;
               break;
            case 2:
               byte var8 = 4;
               break;
            case 3:
               byte var7 = 2;
               break;
            case 4:
               byte var6 = 3;
               break;
            case 5:
               byte var5 = 6;
               break;
            case 6:
               byte var4 = 5;
               break;
            default:
               boolean var10000 = false;
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderAnimatedQuadStripGui(
      PoseStack poseStack, ResourceLocation texture, float size, float alpha, int grid, int row, int col, float z, int rgb
   ) {
      float u0 = (float)col / grid;
      float u1 = (float)(col + 1) / grid;
      float v0 = (float)row / grid;
      float v1 = (float)(row + 1) / grid;
      int r = rgb >> 16 & 0xFF;
      int g = rgb >> 8 & 0xFF;
      int b = rgb & 0xFF;
      int a = Mth.clamp((int)(alpha * 255.0F), 0, 255);
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      RenderSystem.setShaderTexture(0, texture);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      Matrix4f m = poseStack.last().pose();
      BufferBuilder bb = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      float hs = 0.5F * size;
      bb.addVertex(m, -hs, hs, z).setColor(r, g, b, a).setUv(u0, v1);
      bb.addVertex(m, hs, hs, z).setColor(r, g, b, a).setUv(u1, v1);
      bb.addVertex(m, hs, -hs, z).setColor(r, g, b, a).setUv(u1, v0);
      bb.addVertex(m, -hs, -hs, z).setColor(r, g, b, a).setUv(u0, v0);
      BufferUploader.drawWithShader(bb.buildOrThrow());
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderFacingQuad(
      MultiBufferSource source,
      PoseStack pose,
      ResourceLocation location,
      double x,
      double y,
      double z,
      float scale,
      float alpha,
      int stepTex,
      int posTex,
      float pt,
      int color
   ) {
      if (Minecraft.getInstance().cameraEntity instanceof Player player) {
         int r = ARGB32.red(color);
         int g = ARGB32.green(color);
         int b = ARGB32.blue(color);
         int a = (int)(alpha * 255.0F);
         rotateQuadToCamera(player.getEyePosition(pt), pose, x, y, z);
         pose.scale(scale, scale, scale);
         float u0 = (float)posTex / stepTex;
         float u1 = (float)(posTex + 1) / stepTex;
         float v0 = 0.0F;
         float v1 = 1.0F;
         Matrix4f matrix4f = pose.last().pose();
         VertexConsumer consumer = source.getBuffer(createRenderType(location, DestFactor.ONE));
         consumer.addVertex(matrix4f, 0.5F, 0.5F, 0.0F).setUv(u0, v1).setColor(r, g, b, a).setNormal(0.0F, 0.0F, -1.0F);
         consumer.addVertex(matrix4f, 0.5F, -0.5F, 0.0F).setUv(u1, v1).setColor(r, g, b, a).setNormal(0.0F, 0.0F, -1.0F);
         consumer.addVertex(matrix4f, -0.5F, -0.5F, 0.0F).setUv(u1, v0).setColor(r, g, b, a).setNormal(0.0F, 0.0F, -1.0F);
         consumer.addVertex(matrix4f, -0.5F, 0.5F, 0.0F).setUv(u0, v0).setColor(r, g, b, a).setNormal(0.0F, 0.0F, -1.0F);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderFacingStrip(
      MultiBufferSource source,
      PoseStack pose,
      ResourceLocation location,
      double x,
      double y,
      double z,
      float offY,
      float scale,
      float alpha,
      int stepTex,
      int ty,
      int tx,
      float pt,
      int color,
      boolean dark,
      boolean disableDepth
   ) {
      if (Minecraft.getInstance().cameraEntity instanceof Player player) {
         int var32 = ARGB32.red(color);
         int g = ARGB32.green(color);
         int b = ARGB32.blue(color);
         int a = Mth.clamp((int)(alpha * 255.0F), 0, 255);
         pose.pushPose();
         pose.translate(0.5F, 0.5F, 0.5F);
         rotateQuadToCamera(player.getEyePosition(pt), pose, x, y, z);
         pose.mulPose(Axis.ZP.rotation(offY));
         pose.scale(scale, scale, scale);
         float u0 = (float)tx / stepTex;
         float u1 = (float)(tx + 1) / stepTex;
         float v0 = (float)ty / stepTex;
         float v1 = (ty + 1.0F) / stepTex;
         Pose last = pose.last();
         Matrix4f poseMatrix = last.pose();
         Matrix3f normalMatrix = last.normal();
         VertexConsumer consumer = source.getBuffer(ThaumcraftRenderTypes.node(location, dark, disableDepth));
         consumer.addVertex(poseMatrix, -0.5F, 0.5F, 0.0F)
            .setColor(var32, g, b, a)
            .setUv(u0, v1)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setUv2(240, 240)
            .setNormal(0.0F, 0.0F, 1.0F);
         consumer.addVertex(poseMatrix, -0.5F, -0.5F, 0.0F)
            .setColor(var32, g, b, a)
            .setUv(u0, v0)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setUv2(240, 240)
            .setNormal(0.0F, 0.0F, 1.0F);
         consumer.addVertex(poseMatrix, 0.5F, -0.5F, 0.0F)
            .setColor(var32, g, b, a)
            .setUv(u1, v0)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setUv2(240, 240)
            .setNormal(0.0F, 0.0F, 1.0F);
         consumer.addVertex(poseMatrix, 0.5F, 0.5F, 0.0F)
            .setColor(var32, g, b, a)
            .setUv(u1, v1)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setUv2(240, 240)
            .setNormal(0.0F, 0.0F, 1.0F);
         pose.popPose();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static void rotateQuadToCamera(Vec3 pos, PoseStack pose, double x, double y, double z) {
      pos = pos.subtract(x, y, z);
      float yaw = (float)Math.toDegrees(Math.atan2(pos.x, pos.z)) - 180.0F;
      float distanceXZ = (float)Math.sqrt(pos.x * pos.x + pos.z * pos.z);
      float pitch = (float)Math.toDegrees(Math.atan2(pos.y, distanceXZ));
      pose.mulPose(Axis.YP.rotationDegrees(yaw));
      pose.mulPose(Axis.XP.rotationDegrees(pitch));
   }

   @OnlyIn(Dist.CLIENT)
   private static int[] getTextureSize(ResourceLocation tex) {
      return TEX_SIZE_CACHE.computeIfAbsent(tex, t -> {
         try {
            Optional<Resource> opt = Minecraft.getInstance().getResourceManager().getResource(t);
            if (opt.isPresent()) {
               int[] var6;
               try (InputStream in = opt.get().open()) {
                  NativeImage img = NativeImage.read(in);
                  int w = Math.max(1, img.getWidth());
                  int h = Math.max(1, img.getHeight());
                  img.close();
                  var6 = new int[]{w, h};
               }

               return var6;
            }
         } catch (Exception var9) {
         }

         return new int[]{256, 256};
      });
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderAnimatedQuadStrip(GuiGraphics graphics, ResourceLocation texture, float n, float n2, int n3, int n4, int n5, float n6, int n7) {
      if (Minecraft.getInstance().cameraEntity instanceof Player) {
         float n8 = (float)n5 / n3;
         float n9 = (float)(n5 + 1) / n3;
         float n10 = (float)n4 / n3;
         float n11 = (float)(n4 + 1) / n3;
         int color = n7 | (int)(n2 * 255.0F) << 24;
         RenderSystem.setShaderTexture(0, texture);
         RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
         Matrix4f matrix4f = graphics.pose().last().pose();
         BufferBuilder builder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
         builder.addVertex(matrix4f, -0.5F * n, 0.5F * n, 0.0F).setUv(n8, n11).setColor(color).setNormal(0.0F, 0.0F, -1.0F);
         builder.addVertex(matrix4f, 0.5F * n, 0.5F * n, 0.0F).setUv(n9, n11).setColor(color).setNormal(0.0F, 0.0F, -1.0F);
         builder.addVertex(matrix4f, 0.5F * n, -0.5F * n, 0.0F).setUv(n9, n10).setColor(color).setNormal(0.0F, 0.0F, -1.0F);
         builder.addVertex(matrix4f, -0.5F * n, -0.5F * n, 0.0F).setUv(n8, n10).setColor(color).setNormal(0.0F, 0.0F, -1.0F);
         BufferUploader.drawWithShader(builder.buildOrThrow());
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static void drawTexturedQuad(GuiGraphics graphics, ResourceLocation location, int x, int y, int u, int v, int width, int height, int z) {
      graphics.blit(location, x, y, z, u, v, width, height, 256, 256);
   }

   @OnlyIn(Dist.CLIENT)
   public static void drawTexturedQuad(GuiGraphics graphics, ResourceLocation location, int x, int y, int u, int v, int width, int height, float z) {
      PoseStack pose = graphics.pose();
      pose.pushPose();
      pose.translate(0.0F, 0.0F, z);
      graphics.blit(location, x, y, 0, u, v, width, height, 256, 256);
      pose.popPose();
   }

   @OnlyIn(Dist.CLIENT)
   public static void drawTexturedQuadFull(GuiGraphics graphics, ResourceLocation location, int x, int y, int z) {
      graphics.pose().pushPose();
      graphics.pose().translate(0.0F, 0.0F, z);
      graphics.blit(location, x, y, 0.0F, 0.0F, 16, 16, 16, 16);
      graphics.pose().popPose();
   }

   public static void renderQuad(String s) {
      renderQuad(s, 1, 0.66F);
   }

   public static void renderQuad(String s, int oneFunc, float alpha) {
      renderQuad(s, oneFunc, alpha, 1.0F, 1.0F, 1.0F);
   }

   public static void renderQuad(String s, int oneFunc, float alpha, float red, float green, float blue) {
      bindTexture(s);
      BufferBuilder builder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, oneFunc);
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      builder.addVertex(0.0F, 1.0F, 0.0F).setUv(0.0F, 1.0F).setColor(red, green, blue, alpha).setNormal(0.0F, 0.0F, -1.0F);
      builder.addVertex(1.0F, 1.0F, 0.0F).setUv(1.0F, 1.0F).setColor(red, green, blue, alpha).setNormal(0.0F, 0.0F, -1.0F);
      builder.addVertex(1.0F, 0.0F, 0.0F).setUv(1.0F, 0.0F).setColor(red, green, blue, alpha).setNormal(0.0F, 0.0F, -1.0F);
      builder.addVertex(0.0F, 0.0F, 0.0F).setUv(0.0F, 0.0F).setColor(red, green, blue, alpha).setNormal(0.0F, 0.0F, -1.0F);
      BufferUploader.drawWithShader(builder.buildOrThrow());
      RenderSystem.disableBlend();
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderQuadCenteredFromTexture(
      PoseStack stack, String s, float scale, float r, float g, float b, int brightness, int destFunc, float alpha
   ) {
      bindTexture(s);
      renderQuadCenteredFromTexture(stack, scale, r, g, b, brightness, destFunc, alpha);
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderQuadCenteredFromTexture(
      PoseStack stack, ResourceLocation resourceLocation, float n, float n2, float n3, float n4, int n5, int n6, float n7
   ) {
      bindTexture(resourceLocation);
      renderQuadCenteredFromTexture(stack, n, n2, n3, n4, n5, n6, n7);
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderQuadCenteredFromTexture(PoseStack stack, float scale, float r, float g, float b, int brightness, int destFunc, float alpha) {
      BufferBuilder builder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      stack.scale(scale, scale, scale);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, destFunc);
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      Matrix4f matrix4f = stack.last().pose();
      builder.addVertex(matrix4f, -0.5F, 0.5F, 0.0F).setUv(0.0F, 1.0F).setColor(r, g, b, alpha);
      builder.addVertex(matrix4f, 0.5F, 0.5F, 0.0F).setUv(1.0F, 1.0F).setColor(r, g, b, alpha);
      builder.addVertex(matrix4f, 0.5F, -0.5F, 0.0F).setUv(1.0F, 0.0F).setColor(r, g, b, alpha);
      builder.addVertex(matrix4f, -0.5F, -0.5F, 0.0F).setUv(0.0F, 0.0F).setColor(r, g, b, alpha);
      BufferUploader.drawWithShader(builder.buildOrThrow());
      RenderSystem.disableBlend();
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderQuadCenteredFromTextureLevel(
      MultiBufferSource source,
      PoseStack pose,
      ResourceLocation location,
      float scale,
      float r,
      float g,
      float b,
      int brightness,
      DestFactor destFunc,
      float alpha
   ) {
      pose.scale(scale, scale, scale);
      Matrix4f matrix4f = pose.last().pose();
      VertexConsumer consumer = source.getBuffer(createRenderType(location, destFunc));
      consumer.addVertex(matrix4f, -0.5F, 0.5F, 0.0F).setUv(0.0F, 1.0F).setColor(r, g, b, alpha).setNormal(0.0F, 0.0F, -1.0F);
      consumer.addVertex(matrix4f, 0.5F, 0.5F, 0.0F).setUv(1.0F, 1.0F).setColor(r, g, b, alpha).setNormal(0.0F, 0.0F, -1.0F);
      consumer.addVertex(matrix4f, 0.5F, -0.5F, 0.0F).setUv(1.0F, 0.0F).setColor(r, g, b, alpha).setNormal(0.0F, 0.0F, -1.0F);
      consumer.addVertex(matrix4f, -0.5F, -0.5F, 0.0F).setUv(0.0F, 0.0F).setColor(r, g, b, alpha).setNormal(0.0F, 0.0F, -1.0F);
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderQuadCenteredFromTextureLevelFlat(
      MultiBufferSource source, PoseStack pose, ResourceLocation location, float scale, float r, float g, float b, DestFactor destFunc, float alpha
   ) {
      pose.scale(scale, scale, scale);
      Matrix4f matrix4f = pose.last().pose();
      VertexConsumer consumer = source.getBuffer(createFlatRenderType(location, destFunc));
      consumer.addVertex(matrix4f, -0.5F, 0.5F, 0.0F).setUv(0.0F, 1.0F).setColor(r, g, b, alpha);
      consumer.addVertex(matrix4f, 0.5F, 0.5F, 0.0F).setUv(1.0F, 1.0F).setColor(r, g, b, alpha);
      consumer.addVertex(matrix4f, 0.5F, -0.5F, 0.0F).setUv(1.0F, 0.0F).setColor(r, g, b, alpha);
      consumer.addVertex(matrix4f, -0.5F, -0.5F, 0.0F).setUv(0.0F, 0.0F).setColor(r, g, b, alpha);
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderQuadCenteredFromTextureLevelFlatOverlay(
      MultiBufferSource source, PoseStack pose, ResourceLocation location, float scale, float r, float g, float b, DestFactor destFunc, float alpha
   ) {
      pose.scale(scale, scale, scale);
      Matrix4f matrix4f = pose.last().pose();
      VertexConsumer consumer = source.getBuffer(createFlatOverlayRenderType(location, destFunc));
      consumer.addVertex(matrix4f, -0.5F, 0.5F, 0.0F).setUv(0.0F, 1.0F).setColor(r, g, b, alpha);
      consumer.addVertex(matrix4f, 0.5F, 0.5F, 0.0F).setUv(1.0F, 1.0F).setColor(r, g, b, alpha);
      consumer.addVertex(matrix4f, 0.5F, -0.5F, 0.0F).setUv(1.0F, 0.0F).setColor(r, g, b, alpha);
      consumer.addVertex(matrix4f, -0.5F, -0.5F, 0.0F).setUv(0.0F, 0.0F).setColor(r, g, b, alpha);
   }

   @OnlyIn(Dist.CLIENT)
   public static void drawQuadSpriteIconStyle(
      PoseStack poseStack, VertexConsumer vc, int packedLight, TextureAtlasSprite sprite, float size, float r, float g, float b, float a
   ) {
      float h = size * 0.5F;
      Matrix4f pose = poseStack.last().pose();
      Matrix3f normal = poseStack.last().normal();
      float u0 = sprite.getU0();
      float u1 = sprite.getU1();
      float v0 = sprite.getV0();
      float v1 = sprite.getV1();
      vc.addVertex(pose, -h, h, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u1, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(packedLight & 65535, packedLight >> 16)
         .setNormal(0.0F, 0.0F, 1.0F);
      vc.addVertex(pose, h, h, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u0, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(packedLight & 65535, packedLight >> 16)
         .setNormal(0.0F, 0.0F, 1.0F);
      vc.addVertex(pose, h, -h, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u0, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(packedLight & 65535, packedLight >> 16)
         .setNormal(0.0F, 0.0F, 1.0F);
      vc.addVertex(pose, -h, -h, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u1, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(packedLight & 65535, packedLight >> 16)
         .setNormal(0.0F, 0.0F, 1.0F);
   }

   @OnlyIn(Dist.CLIENT)
   public static int getTextureAnimationSize(String s) {
      if (textureSizeCache.get(s) != null) {
         return (Integer)textureSizeCache.get(s);
      } else {
         try {
            InputStream inputStream = ((Resource)Minecraft.getInstance().getResourceManager().getResource(Thaumcraft.location(s)).get()).open();
            if (inputStream == null) {
               throw new Exception("Image not found: " + s);
            } else {
               BufferedImage read = ImageIO.read(inputStream);
               int n = read.getWidth() / read.getHeight();
               textureSizeCache.put(s, n);
               return n;
            }
         } catch (Exception var4) {
            var4.printStackTrace();
            return 16;
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static int getTextureSize(String s, int n) {
      if (textureSizeCache.get(Arrays.asList(s, n)) != null) {
         return (Integer)textureSizeCache.get(Arrays.asList(s, n));
      } else {
         try {
            InputStream inputStream = ((Resource)Minecraft.getInstance().getResourceManager().getResource(Thaumcraft.location(s)).get()).open();
            if (inputStream == null) {
               throw new Exception("Image not found: " + s);
            } else {
               int n2 = ImageIO.read(inputStream).getWidth() / n;
               textureSizeCache.put(Arrays.asList(s, n), n2);
               return n2;
            }
         } catch (Exception var4) {
            var4.printStackTrace();
            return 16;
         }
      }
   }

   public static int getBrightnessForRender(Entity entity, double n, double n2) {
      int floor_double = Mth.floor(n);
      int floor_double2 = Mth.floor(n2);
      if (entity.level().isLoaded(new BlockPos(floor_double, 0, floor_double2))) {
         BlockPos blockPos = new BlockPos(
            floor_double, Mth.floor(entity.getY() - (entity.getBoundingBox().maxY - entity.getBoundingBox().minY) * 0.66), floor_double2
         );
         return entity.level().getBrightness(LightLayer.SKY, blockPos);
      } else {
         return 0;
      }
   }

   public static void bindTexture(String s) {
      ResourceLocation resourceLocation;
      if (boundTextures.containsKey(s)) {
         resourceLocation = boundTextures.get(s);
      } else {
         resourceLocation = Thaumcraft.location(s);
      }

      bindTexture(resourceLocation);
   }

   public static void bindTexture(String s, String s2) {
      ResourceLocation resourceLocation;
      if (boundTextures.containsKey(s + ":" + s2)) {
         resourceLocation = boundTextures.get(s + ":" + s2);
      } else {
         resourceLocation = ResourceLocation.fromNamespaceAndPath(s, s2);
      }

      bindTexture(resourceLocation);
   }

   public static void bindTexture(ResourceLocation resourceLocation) {
      RenderSystem.setShaderTexture(0, resourceLocation);
   }

   @OnlyIn(Dist.CLIENT)
   public static void drawTag(GuiGraphics graphics, int x, int y, Aspect aspect, float amount, int particle, float z, int dstFunc, float opaque) {
      drawTag(graphics, x, y, aspect, amount, particle, z, dstFunc, opaque, false);
   }

   @OnlyIn(Dist.CLIENT)
   public static void drawTag(GuiGraphics graphics, int x, int y, Aspect aspect, float amount, int particle, float z) {
      drawTag(graphics, x, y, aspect, amount, particle, z, 771, 1.0F, false);
   }

   @OnlyIn(Dist.CLIENT)
   public static void drawTag(GuiGraphics graphics, int x, int y, Aspect aspect) {
      drawTag(graphics, x, y, aspect, 0.0F, 0, 0.0F, 771, 1.0F, true);
   }

   @OnlyIn(Dist.CLIENT)
   public static void drawTag(GuiGraphics graphics, int x, int y, Aspect aspect, float amount, int particle, float z, int dstFunc, float opaque, boolean b) {
      drawTag(graphics, (float)x, (float)y, aspect, amount, particle, z, dstFunc, opaque, b);
   }

   public static void drawTag(
      GuiGraphics graphics, float x, float y, Aspect aspect, float amount, int particle, float z, int dstFunc, float opaque, boolean gray
   ) {
      if (aspect != null) {
         float red;
         float green;
         float blue;
         float alpha;
         if (gray) {
            red = 0.1F;
            green = 0.1F;
            blue = 0.1F;
            alpha = opaque * 0.8F;
         } else {
            int color = aspect.getColor();
            red = ARGB32.red(color) / 255.0F;
            green = ARGB32.green(color) / 255.0F;
            blue = ARGB32.blue(color) / 255.0F;
            alpha = opaque;
         }

         Font font = Minecraft.getInstance().font;
         PoseStack pose = graphics.pose();
         pose.pushPose();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, dstFunc);
         pose.pushPose();
         RenderSystem.setShaderTexture(0, aspect.getImage());
         RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
         BufferBuilder builder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
         Matrix4f matrix4f = pose.last().pose();
         builder.addVertex(matrix4f, x + 0.0F, y + 16.0F, z).setUv(0.0F, 1.0F).setColor(red, green, blue, alpha);
         builder.addVertex(matrix4f, x + 16.0F, y + 16.0F, z).setUv(1.0F, 1.0F).setColor(red, green, blue, alpha);
         builder.addVertex(matrix4f, x + 16.0F, y + 0.0F, z).setUv(1.0F, 0.0F).setColor(red, green, blue, alpha);
         builder.addVertex(matrix4f, x + 0.0F, y + 0.0F, z).setUv(0.0F, 0.0F).setColor(red, green, blue, alpha);
         BufferUploader.drawWithShader(builder.buildOrThrow());
         pose.popPose();
         if (amount > 0.0F) {
            pose.pushPose();
            pose.scale(0.5F, 0.5F, 0.5F);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            String format = myFormatter.format(amount);
            int stringWidth = font.width(format);
            if (dstFunc > 1) {
               for (int i = -1; i <= 1; i++) {
                  for (int j = -1; j <= 1; j++) {
                     if ((i == 0 || j == 0) && (i != 0 || j != 0)) {
                        graphics.drawString(font, format, i + 32 - stringWidth + (int)x * 2, j + 32 - 9 + (int)y * 2, 0, false);
                     }
                  }
               }
            }

            graphics.drawString(font, format, 32 - stringWidth + (int)x * 2, 32 - 9 + (int)y * 2, 16777215, false);
            pose.popPose();
         }

         if (particle > 0) {
            pose.pushPose();
            RenderSystem.enableBlend();
            drawTexturedQuad(
               graphics, ParticleUtils.PARTICLE_TEXTURE, (int)x - 4, (int)y - 4, 16 * (Minecraft.getInstance().player.tickCount % 16), 80, 16, 16, z
            );
            if (particle > 1) {
               pose.scale(0.5F, 0.5F, 0.5F);
               String string = particle + "";
               int n8 = font.width(string) / 2;
               if (dstFunc > 1) {
                  for (int k = -1; k <= 1; k++) {
                     for (int l = -1; l <= 1; l++) {
                        if ((k == 0 || l == 0) && (k != 0 || l != 0)) {
                           graphics.drawString(font, string, 8 - n8 + k + (int)x * 2, 15 + l - 9 + (int)y * 2, 0, false);
                        }
                     }
                  }
               }

               graphics.drawString(font, string, 8 - n8 + (int)x * 2, 15 - 9 + (int)y * 2, 16777215, false);
            }

            pose.popPose();
         }

         RenderSystem.disableBlend();
         pose.popPose();
      }
   }

   public static boolean isVisibleTo(float n, Entity entity, BlockPos pos) {
      return isVisibleTo(n, entity, pos.getX(), pos.getY(), pos.getZ());
   }

   @OnlyIn(Dist.CLIENT)
   public static boolean isVisibleTo(float n, Entity entity, double n2, double n3, double n4) {
      double distance = entity.distanceToSqr(n2, n3, n4);
      if (distance < 4.0) {
         return true;
      } else {
         Minecraft client = Minecraft.getInstance();
         double n5 = n + ((Integer)client.options.fov().get()).intValue() / 2.0F;
         int n6 = 512;
         if (n6 > 400) {
            n6 = 400;
         }

         double n7 = n6;
         float cos = Mth.cos(-entity.yRotO * 0.01745329F - 3.141593F);
         float sin = Mth.sin(-entity.yRotO * 0.01745329F - 3.141593F);
         float n8 = -Mth.cos(-entity.xRotO * 0.01745329F);
         float sin2 = Mth.sin(-entity.xRotO * 0.01745329F);
         double n9 = sin * n8;
         double n10 = sin2;
         double n11 = cos * n8;
         double n12 = n2 + 0.5 - entity.getX();
         double n13 = n3 + 0.5 - entity.getY() - entity.getEyeHeight();
         double n14 = n4 + 0.5 - entity.getZ();
         double sqrt = Math.sqrt(n12 * n12 + n13 * n13 + n14 * n14);
         return Math.acos(n12 / sqrt * n9 + n13 / sqrt * n10 + n14 / sqrt * n11) < n5 && client.options.getCameraType().isFirstPerson() && distance < n7
            || !client.options.getCameraType().isFirstPerson() && distance < n7;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static void drawFloatyLine(
      PoseStack pose,
      MultiBufferSource bufferSource,
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      float partialTicks,
      int color,
      ResourceLocation texture,
      float speed,
      float quality
   ) {
      drawFloatyLine(pose, bufferSource, x1, y1, z1, x2, y2, z2, partialTicks, color, texture, speed, quality, 0.15F);
   }

   @OnlyIn(Dist.CLIENT)
   public static void drawFloatyLine(
      PoseStack pose,
      MultiBufferSource bufferSource,
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      float partialTicks,
      int color,
      ResourceLocation texture,
      float speed,
      float quality,
      float thickness
   ) {
      drawFloatyLine(pose, bufferSource, x1, y1, z1, x2, y2, z2, partialTicks, color, texture, speed, quality, thickness, false);
   }

   @OnlyIn(Dist.CLIENT)
   public static void drawFloatyLine(
      PoseStack pose,
      MultiBufferSource bufferSource,
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      float partialTicks,
      int color,
      ResourceLocation texture,
      float speed,
      float quality,
      float thickness,
      boolean disableDepth
   ) {
      float time = (float)(System.nanoTime() / 30000000L);
      int r = ARGB32.red(color);
      int g = ARGB32.green(color);
      int b = ARGB32.blue(color);
      double dx = x1 - x2;
      double dy = y1 - y2;
      double dz = z1 - z2;
      float len = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (!(len <= 1.0E-4F)) {
         float base = Math.round(len) * (Config.golemLinkQuality / 2.0F);
         if (!(base <= 0.0F)) {
            int steps = Math.max(1, (int)(base * quality));
            pose.pushPose();
            pose.translate(x2, y2, z2);
            Matrix4f mat = pose.last().pose();
            VertexConsumer consumer = bufferSource.getBuffer(ThaumcraftRenderTypes.getFloatyLineRenderType(texture, disableDepth));

            for (int i = 0; i <= steps; i++) {
               float t = (float)i / steps;
               float a = 1.0F - Math.abs(t - 0.5F) / 0.5F;
               a = Mth.clamp(a, 0.0F, 1.0F);
               double wx = Mth.sin((float)((z1 % 16.0 + len * (1.0F - t) * Config.golemLinkQuality / 2.0F - time % 32767.0F / 5.0F) / 4.0)) * 0.5F * a;
               double wy = Mth.sin((float)((x1 % 16.0 + len * (1.0F - t) * Config.golemLinkQuality / 2.0F - time % 32767.0F / 5.0F) / 3.0)) * 0.5F * a;
               double wz = Mth.sin((float)((y1 % 16.0 + len * (1.0F - t) * Config.golemLinkQuality / 2.0F - time % 32767.0F / 5.0F) / 2.0)) * 0.5F * a;
               float u = (1.0F - t) * len - time * speed;
               int a255 = Mth.clamp((int)(a * 255.0F), 0, 255);
               float px = (float)((dx + wx) * t);
               float py = (float)((dy + wy) * t);
               float pz = (float)((dz + wz) * t);
               consumer.addVertex(mat, px, py - thickness, pz)
                  .setColor(r, g, b, a255)
                  .setUv(u, 1.0F)
                  .setOverlay(OverlayTexture.NO_OVERLAY)
                  .setUv2(240, 240)
                  .setNormal(0.0F, 1.0F, 0.0F);
               consumer.addVertex(mat, px, py + thickness, pz)
                  .setColor(r, g, b, a255)
                  .setUv(u, 0.0F)
                  .setOverlay(OverlayTexture.NO_OVERLAY)
                  .setUv2(240, 240)
                  .setNormal(0.0F, 1.0F, 0.0F);
            }

            for (int i = 0; i <= steps; i++) {
               float t = i / base;
               float a = 1.0F - Math.abs(i - base / 2.0F) / (base / 2.0F);
               if (a < 0.0F) {
                  a = 0.0F;
               }

               double wave = len * (1.0F - t) * Config.golemLinkQuality / 2.0F - time % 32767.0F / 5.0F;
               double wx = Mth.sin((float)((z1 % 16.0 + wave) / 4.0)) * 0.5F * a;
               double wy = Mth.sin((float)((x1 % 16.0 + wave) / 3.0)) * 0.5F * a;
               double wz = Mth.sin((float)((y1 % 16.0 + wave) / 2.0)) * 0.5F * a;
               float u = (1.0F - t) * len - time * speed;
               int a255 = Mth.clamp((int)(a * 255.0F), 0, 255);
               float px = (float)((dx + wx) * t);
               float py = (float)((dy + wy) * t);
               float pz = (float)((dz + wz) * t);
               consumer.addVertex(mat, px - thickness, py, pz)
                  .setColor(r, g, b, a255)
                  .setUv(u, 1.0F)
                  .setOverlay(OverlayTexture.NO_OVERLAY)
                  .setUv2(240, 240)
                  .setNormal(1.0F, 0.0F, 0.0F);
               consumer.addVertex(mat, px + thickness, py, pz)
                  .setColor(r, g, b, a255)
                  .setUv(u, 0.0F)
                  .setOverlay(OverlayTexture.NO_OVERLAY)
                  .setUv2(240, 240)
                  .setNormal(1.0F, 0.0F, 0.0F);
            }

            pose.popPose();
         }
      }
   }

   public static void drawFloatyLine(double n, double n2, double n3, double n4, double n5, double n6, float n7, int n8, ResourceLocation s, float n9, float n10) {
      drawFloatyLine(n, n2, n3, n4, n5, n6, n7, n8, s, n9, n10, 0.15F);
   }

   @OnlyIn(Dist.CLIENT)
   public static void drawFloatyLine(
      double n, double n2, double n3, double n4, double n5, double n6, float n7, int n8, ResourceLocation tex, float n9, float n10, float n11
   ) {
      Minecraft mc = Minecraft.getInstance();
      Entity camEnt = mc.getCameraEntity();
      if (camEnt != null) {
         Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
         double cx = Mth.lerp(n7, camEnt.xo, camEnt.getX());
         double cy = Mth.lerp(n7, camEnt.yo, camEnt.getY());
         double cz = Mth.lerp(n7, camEnt.zo, camEnt.getZ());
         Matrix4f mv = new Matrix4f(RenderSystem.getModelViewMatrix());
         float mvTx = mv.m30();
         float mvTy = mv.m31();
         float mvTz = mv.m32();
         boolean hasCam = Math.abs(mvTx + (float)camPos.x) < 0.1F && Math.abs(mvTy + (float)camPos.y) < 0.1F && Math.abs(mvTz + (float)camPos.z) < 0.1F;
         if (hasCam) {
            mv.translate((float)n4, (float)n5, (float)n6);
         } else {
            mv.translate((float)(n4 - cx), (float)(n5 - cy), (float)(n6 - cz));
         }

         float time = (float)(System.nanoTime() / 30000000L);
         Color c = new Color(n8);
         int r = c.getRed();
         int g = c.getGreen();
         int b = c.getBlue();
         double dx = n - n4;
         double dy = n2 - n5;
         double dz = n3 - n6;
         float len = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (!(len <= 1.0E-4F)) {
            float base = Math.round(len) * (Config.golemLinkQuality / 2.0F);
            if (!(base <= 0.0F)) {
               int steps = (int)(base * n10);
               if (steps > 0) {
                  RenderSystem.depthMask(false);
                  RenderSystem.enableBlend();
                  RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
                  RenderSystem.disableCull();
                  RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
                  RenderSystem.setShaderTexture(0, tex);
                  BufferBuilder bb = Tesselator.getInstance().begin(Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX_COLOR);

                  for (int i = 0; i <= steps; i++) {
                     float t = i / base;
                     float a = 1.0F - Math.abs(i - base / 2.0F) / (base / 2.0F);
                     if (a < 0.0F) {
                        a = 0.0F;
                     }

                     double wx = Mth.sin((float)((n3 % 16.0 + len * (1.0F - t) * Config.golemLinkQuality / 2.0F - time % 32767.0F / 5.0F) / 4.0)) * 0.5F * a;
                     double wy = Mth.sin((float)((n % 16.0 + len * (1.0F - t) * Config.golemLinkQuality / 2.0F - time % 32767.0F / 5.0F) / 3.0)) * 0.5F * a;
                     double wz = Mth.sin((float)((n2 % 16.0 + len * (1.0F - t) * Config.golemLinkQuality / 2.0F - time % 32767.0F / 5.0F) / 2.0)) * 0.5F * a;
                     float u = (1.0F - t) * len - time * n9;
                     int a255 = Mth.clamp((int)(a * 255.0F), 0, 255);
                     float px = (float)((dx + wx) * t);
                     float py = (float)((dy + wy) * t);
                     float pz = (float)((dz + wz) * t);
                     bb.addVertex(mv, px, py - n11, pz).setColor(r, g, b, a255).setUv(u, 1.0F);
                     bb.addVertex(mv, px, py + n11, pz).setColor(r, g, b, a255).setUv(u, 0.0F);
                  }

                  BufferUploader.drawWithShader(bb.buildOrThrow());
                  bb = Tesselator.getInstance().begin(Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX_COLOR);

                  for (int i = 0; i <= steps; i++) {
                     float t = i / base;
                     float a = 1.0F - Math.abs(i - base / 2.0F) / (base / 2.0F);
                     if (a < 0.0F) {
                        a = 0.0F;
                     }

                     double wx = Mth.sin((float)((n3 % 16.0 + len * (1.0F - t) * Config.golemLinkQuality / 2.0F - time % 32767.0F / 5.0F) / 4.0)) * 0.5F * a;
                     double wy = Mth.sin((float)((n % 16.0 + len * (1.0F - t) * Config.golemLinkQuality / 2.0F - time % 32767.0F / 5.0F) / 3.0)) * 0.5F * a;
                     double wz = Mth.sin((float)((n2 % 16.0 + len * (1.0F - t) * Config.golemLinkQuality / 2.0F - time % 32767.0F / 5.0F) / 2.0)) * 0.5F * a;
                     float u = (1.0F - t) * len - time * n9;
                     int a255 = Mth.clamp((int)(a * 255.0F), 0, 255);
                     float px = (float)((dx + wx) * t);
                     float py = (float)((dy + wy) * t);
                     float pz = (float)((dz + wz) * t);
                     bb.addVertex(mv, px - n11, py, pz).setColor(r, g, b, a255).setUv(u, 1.0F);
                     bb.addVertex(mv, px + n11, py, pz).setColor(r, g, b, a255).setUv(u, 0.0F);
                  }

                  BufferUploader.drawWithShader(bb.buildOrThrow());
                  RenderSystem.enableCull();
                  RenderSystem.disableBlend();
                  RenderSystem.depthMask(true);
               }
            }
         }
      }
   }

   public static void drawFloatyGUILine(double n, double n2, double n3, double n4, float n5, int n6, String s, float n7, float n8) {
      ResourceLocation tex = ResourceLocation.tryParse(s);
      if (tex == null) {
         tex = ResourceLocation.fromNamespaceAndPath("thaumcraft", s);
      }

      Color c = new Color(n6);
      int r = c.getRed();
      int g = c.getGreen();
      int b = c.getBlue();
      double dx = n - n3;
      double dy = n2 - n4;
      float len = (float)Math.sqrt(dx * dx + dy * dy);
      if (!(len <= 1.0E-4F)) {
         float stepsF = Math.round(len) * n8;
         int steps = (int)stepsF;
         if (steps > 0) {
            float angleDeg = (float)(-(Math.atan2(dx, dy) * 180.0 / Math.PI) + 90.0);
            Matrix4f mv = new Matrix4f(RenderSystem.getModelViewMatrix());
            mv.translate((float)n3, (float)n4, 0.0F);
            mv.rotateZ((float)(angleDeg * (Math.PI / 180.0)));
            float invSteps = 1.0F / stepsF;
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, tex);
            BufferBuilder bb = Tesselator.getInstance().begin(Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX_COLOR);
            double nx = dx / len;

            for (int i = 0; i <= steps; i++) {
               float t = i / stepsF;
               float u1 = (1.0F - t) * stepsF;
               float u2 = (1.0F - t) * stepsF + invSteps;
               float x = (float)(nx * i);
               int a = 255;
               bb.addVertex(mv, x, -1.0F, 0.0F).setColor(r, g, b, a).setUv(u1, 1.0F);
               bb.addVertex(mv, x, 1.0F, 0.0F).setColor(r, g, b, a).setUv(u2, 0.0F);
            }

            BufferUploader.drawWithShader(bb.buildOrThrow());
            RenderSystem.disableBlend();
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderFluidFace(MultiBufferSource source, PoseStack pose, TextureAtlasSprite sprite, float scale, int color, int packedLight) {
      pose.pushPose();
      pose.scale(scale, scale, scale);
      float r = ARGB32.red(color) / 255.0F;
      float g = ARGB32.green(color) / 255.0F;
      float b = ARGB32.blue(color) / 255.0F;
      float a = ARGB32.alpha(color) / 255.0F;
      float u0 = sprite.getU0();
      float u1 = sprite.getU1();
      float v0 = sprite.getV0();
      float v1 = sprite.getV1();
      Matrix4f matrix4f = pose.last().pose();
      VertexConsumer consumer = source.getBuffer(RenderType.translucent());
      consumer.addVertex(matrix4f, -0.5F, 0.5F, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u0, v1)
         .setUv2(packedLight & 65535, packedLight >> 16)
         .setNormal(0.0F, 0.0F, 1.0F);
      consumer.addVertex(matrix4f, 0.5F, 0.5F, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u1, v1)
         .setUv2(packedLight & 65535, packedLight >> 16)
         .setNormal(0.0F, 0.0F, 1.0F);
      consumer.addVertex(matrix4f, 0.5F, -0.5F, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u1, v0)
         .setUv2(packedLight & 65535, packedLight >> 16)
         .setNormal(0.0F, 0.0F, 1.0F);
      consumer.addVertex(matrix4f, -0.5F, -0.5F, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u0, v0)
         .setUv2(packedLight & 65535, packedLight >> 16)
         .setNormal(0.0F, 0.0F, 1.0F);
      consumer.addVertex(matrix4f, -0.5F, -0.5F, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u0, v0)
         .setUv2(packedLight & 65535, packedLight >> 16)
         .setNormal(0.0F, 0.0F, -1.0F);
      consumer.addVertex(matrix4f, 0.5F, -0.5F, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u1, v0)
         .setUv2(packedLight & 65535, packedLight >> 16)
         .setNormal(0.0F, 0.0F, -1.0F);
      consumer.addVertex(matrix4f, 0.5F, 0.5F, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u1, v1)
         .setUv2(packedLight & 65535, packedLight >> 16)
         .setNormal(0.0F, 0.0F, -1.0F);
      consumer.addVertex(matrix4f, -0.5F, 0.5F, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u0, v1)
         .setUv2(packedLight & 65535, packedLight >> 16)
         .setNormal(0.0F, 0.0F, -1.0F);
      pose.popPose();
   }

   @OnlyIn(Dist.CLIENT)
   private static RenderType createRenderType(String suffix, DestFactor factor) {
      return RenderType.create(
         "thaumcraft_" + suffix,
         DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL,
         Mode.QUADS,
         256,
         false,
         false,
         CompositeState.builder()
            .setShaderState(new ShaderStateShard(GameRenderer::getPositionTexColorShader))
            .setDepthTestState(new DepthTestStateShard("always", 519))
            .setTransparencyState(new TransparencyStateShard("lightning_transparency", () -> {
               RenderSystem.enableBlend();
               RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, factor);
            }, () -> {
               RenderSystem.disableBlend();
               RenderSystem.defaultBlendFunc();
            }))
            .setWriteMaskState(new WriteMaskStateShard(true, false))
            .createCompositeState(false)
      );
   }

   @OnlyIn(Dist.CLIENT)
   private static RenderType createRenderType(ResourceLocation location, DestFactor factor) {
      return createRenderType(location, factor, builder -> builder);
   }

   @OnlyIn(Dist.CLIENT)
   private static RenderType createFlatRenderType(ResourceLocation location, DestFactor factor) {
      return RenderType.create(
         "thaumcraft_flat_" + location.getPath(),
         DefaultVertexFormat.POSITION_TEX_COLOR,
         Mode.QUADS,
         256,
         false,
         false,
         CompositeState.builder()
            .setTextureState(new TextureStateShard(location, false, false))
            .setShaderState(new ShaderStateShard(GameRenderer::getPositionTexColorShader))
            .setTransparencyState(new TransparencyStateShard("lightning_transparency", () -> {
               RenderSystem.enableBlend();
               RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, factor);
            }, () -> {
               RenderSystem.disableBlend();
               RenderSystem.defaultBlendFunc();
            }))
            .setCullState(RenderStateShard.NO_CULL)
            .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
            .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
            .createCompositeState(false)
      );
   }

   @OnlyIn(Dist.CLIENT)
   private static RenderType createFlatOverlayRenderType(ResourceLocation location, DestFactor factor) {
      return RenderType.create(
         "thaumcraft_flat_overlay_" + location.getPath(),
         DefaultVertexFormat.POSITION_TEX_COLOR,
         Mode.QUADS,
         256,
         false,
         false,
         CompositeState.builder()
            .setTextureState(new TextureStateShard(location, false, false))
            .setShaderState(new ShaderStateShard(GameRenderer::getPositionTexColorShader))
            .setTransparencyState(new TransparencyStateShard("lightning_transparency", () -> {
               RenderSystem.enableBlend();
               RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, factor);
            }, () -> {
               RenderSystem.disableBlend();
               RenderSystem.defaultBlendFunc();
            }))
            .setCullState(RenderStateShard.NO_CULL)
            .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .createCompositeState(false)
      );
   }

   @OnlyIn(Dist.CLIENT)
   private static RenderType createRenderType(ResourceLocation location, DestFactor factor, Function<CompositeStateBuilder, CompositeStateBuilder> function) {
      return RenderType.create(
         "thaumcraft_" + location.getPath(),
         DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL,
         Mode.QUADS,
         256,
         false,
         false,
         function.apply(
               CompositeState.builder()
                  .setTextureState(new TextureStateShard(location, false, false))
                  .setShaderState(new ShaderStateShard(GameRenderer::getPositionTexColorShader))
                  .setTransparencyState(new TransparencyStateShard("lightning_transparency", () -> {
                     RenderSystem.enableBlend();
                     RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, factor);
                  }, () -> {
                     RenderSystem.disableBlend();
                     RenderSystem.defaultBlendFunc();
                  }))
                  .setWriteMaskState(new WriteMaskStateShard(true, false))
            )
            .createCompositeState(false)
      );
   }
}
