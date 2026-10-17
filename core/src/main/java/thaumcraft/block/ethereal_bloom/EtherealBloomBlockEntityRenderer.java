package thaumcraft.block.ethereal_bloom;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.CullStateShard;
import net.minecraft.client.renderer.RenderStateShard.DepthTestStateShard;
import net.minecraft.client.renderer.RenderStateShard.LightmapStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard;
import net.minecraft.client.renderer.RenderStateShard.WriteMaskStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import thaumcraft.util.UtilsFX;

public class EtherealBloomBlockEntityRenderer implements BlockEntityRenderer<EtherealBloomBlockEntity> {
   public static final ResourceLocation NODE_TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/nodes.png");
   public static final ResourceLocation CUBE_TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/crystalcapacitor.png");
   public static final ResourceLocation LEAVES_SPRITE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/purifier_leaves");
   public static final ResourceLocation STALK_SPRITE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/purifier_stalk");
   private static final ShaderStateShard SHADER = new ShaderStateShard(GameRenderer::getPositionColorTexLightmapShader);
   private static final TransparencyStateShard SRC_ALPHA_ONE = new TransparencyStateShard("thaumcraft_srcalpha_one", () -> {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
   }, () -> {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
   });
   private static final CullStateShard NO_CULL = new CullStateShard(false);
   private static final LightmapStateShard LIGHTMAP = new LightmapStateShard(true);
   private static final DepthTestStateShard LEQUAL = new DepthTestStateShard("<=", 515);
   private static final WriteMaskStateShard COLOR_DEPTH_WRITE = new WriteMaskStateShard(true, true);
   private static final Map<ResourceLocation, RenderType> ADDITIVE_DEPTH_CACHE = new ConcurrentHashMap<>();

   public EtherealBloomBlockEntityRenderer(Context ctx) {
   }

   public void render(EtherealBloomBlockEntity be, float partialTicks, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      float n5 = be.growthCounter + partialTicks;
      float n6 = be.growthCounter + partialTicks;
      float n7 = n5 - 33.0F;
      float n8 = n5 - 66.0F;
      if (n5 > 100.0F) {
         n5 = 100.0F;
      }

      if (n6 > 50.0F) {
         n6 = 50.0F;
      }

      if (n7 < 0.0F) {
         n7 = 0.0F;
      }

      if (n7 > 33.0F) {
         n7 = 33.0F;
      }

      if (n8 < 0.0F) {
         n8 = 0.0F;
      }

      if (n8 > 33.0F) {
         n8 = 33.0F;
      }

      float n9 = n5 / 100.0F;
      float n10 = n6 / 60.0F + 0.1666666F;
      float n11 = n7 / 33.0F;
      float n12 = n8 / 33.0F * 0.7F;
      int fullBright = 15728880;
      pose.pushPose();
      BlockPos bp = be.getBlockPos();
      int frame = be.counter % 32;
      pose.pushPose();
      pose.translate(-bp.getX(), -bp.getY(), -bp.getZ());
      UtilsFX.renderFacingStrip(
         buffer, pose, NODE_TEX, bp.getX() + 0.5, bp.getY() + n9, bp.getZ() + 0.5, 0.0F, n9, 1.0F, 32, 6, frame, partialTicks, 11197951, false, false
      );
      pose.popPose();
      pose.pushPose();
      pose.translate(0.5 - n12 / 8.0F, (double)n9 - n12 / 6.0F, 0.5 - n12 / 8.0F);
      pose.scale(n12 / 4.0F, n12 / 3.0F, n12 / 4.0F);
      drawTexturedCube(pose, buffer.getBuffer(additiveDepth(CUBE_TEX)), fullBright, 255, 255, 255, 255);
      pose.popPose();
      TextureAtlasSprite leaves = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(LEAVES_SPRITE);
      TextureAtlasSprite stalk = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(STALK_SPRITE);
      VertexConsumer atlas = buffer.getBuffer(additiveDepth(TextureAtlas.LOCATION_BLOCKS));
      pose.pushPose();
      pose.translate(0.5, 0.25, 0.5);
      pose.mulPose(Axis.XP.rotationDegrees(180.0F));

      for (int i = 0; i < 4; i++) {
         pose.pushPose();
         pose.scale(n11, n9, n11);
         pose.mulPose(Axis.YP.rotationDegrees(90.0F * i));
         drawCenteredQuad(pose, atlas, leaves, fullBright, 255, 255, 255, 255);
         pose.popPose();
      }

      pose.popPose();
      pose.pushPose();
      pose.translate(0.5, 0.6, 0.5);
      pose.mulPose(Axis.YP.rotationDegrees(45.0F));
      pose.mulPose(Axis.XP.rotationDegrees(180.0F));

      for (int i = 0; i < 4; i++) {
         pose.pushPose();
         pose.scale(n12, n9 * 0.7F, n12);
         pose.mulPose(Axis.YP.rotationDegrees(90.0F * i));
         drawCenteredQuad(pose, atlas, leaves, fullBright, 255, 255, 255, 255);
         pose.popPose();
      }

      pose.popPose();
      pose.pushPose();
      pose.translate(0.5, 0.5, 0.5);
      pose.mulPose(Axis.XP.rotationDegrees(180.0F));

      for (int i = 0; i < 4; i++) {
         pose.pushPose();
         pose.translate(0.0, (1.0F - n9) / 2.0F, 0.0);
         pose.scale(n10, n9, n10);
         pose.mulPose(Axis.YP.rotationDegrees(90.0F * i));
         drawCenteredQuad(pose, atlas, stalk, fullBright, 255, 255, 255, 255);
         pose.popPose();
      }

      pose.popPose();
      pose.popPose();
   }

   private static void drawCenteredQuad(PoseStack pose, VertexConsumer vc, TextureAtlasSprite spr, int light, int r, int g, int b, int a) {
      float u0 = spr.getU0();
      float u1 = spr.getU1();
      float v0 = spr.getV0();
      float v1 = spr.getV1();
      Matrix4f m = pose.last().pose();
      vc.addVertex(m, -0.5F, 0.0F, 0.0F).setColor(r, g, b, a).setUv(u0, v1).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, 0.5F, 0.0F, 0.0F).setColor(r, g, b, a).setUv(u1, v1).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, 0.5F, 1.0F, 0.0F).setColor(r, g, b, a).setUv(u1, v0).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, -0.5F, 1.0F, 0.0F).setColor(r, g, b, a).setUv(u0, v0).setUv2(light & 65535, light >> 16);
   }

   private static void drawTexturedCube(PoseStack pose, VertexConsumer vc, int light, int r, int g, int b, int a) {
      Matrix4f m = pose.last().pose();
      float x0 = 0.0F;
      float x1 = 1.0F;
      float y0 = 0.0F;
      float y1 = 1.0F;
      float z0 = 0.0F;
      float z1 = 1.0F;
      vc.addVertex(m, x0, y0, z1).setColor(r, g, b, a).setUv(0.0F, 1.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x1, y0, z1).setColor(r, g, b, a).setUv(1.0F, 1.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setUv(1.0F, 0.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x0, y1, z1).setColor(r, g, b, a).setUv(0.0F, 0.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x1, y0, z0).setColor(r, g, b, a).setUv(0.0F, 1.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x0, y0, z0).setColor(r, g, b, a).setUv(1.0F, 1.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x0, y1, z0).setColor(r, g, b, a).setUv(1.0F, 0.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x1, y1, z0).setColor(r, g, b, a).setUv(0.0F, 0.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x0, y0, z0).setColor(r, g, b, a).setUv(0.0F, 1.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x0, y0, z1).setColor(r, g, b, a).setUv(1.0F, 1.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x0, y1, z1).setColor(r, g, b, a).setUv(1.0F, 0.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x0, y1, z0).setColor(r, g, b, a).setUv(0.0F, 0.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x1, y0, z1).setColor(r, g, b, a).setUv(0.0F, 1.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x1, y0, z0).setColor(r, g, b, a).setUv(1.0F, 1.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x1, y1, z0).setColor(r, g, b, a).setUv(1.0F, 0.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setUv(0.0F, 0.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x0, y1, z1).setColor(r, g, b, a).setUv(0.0F, 1.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setUv(1.0F, 1.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x1, y1, z0).setColor(r, g, b, a).setUv(1.0F, 0.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x0, y1, z0).setColor(r, g, b, a).setUv(0.0F, 0.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x0, y0, z0).setColor(r, g, b, a).setUv(0.0F, 1.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x1, y0, z0).setColor(r, g, b, a).setUv(1.0F, 1.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x1, y0, z1).setColor(r, g, b, a).setUv(1.0F, 0.0F).setUv2(light & 65535, light >> 16);
      vc.addVertex(m, x0, y0, z1).setColor(r, g, b, a).setUv(0.0F, 0.0F).setUv2(light & 65535, light >> 16);
   }

   private static RenderType additiveDepth(ResourceLocation tex) {
      return ADDITIVE_DEPTH_CACHE.computeIfAbsent(
         tex,
         t -> {
            String key = t.toString().replace(':', '_').replace('/', '_');
            CompositeState state = CompositeState.builder()
               .setShaderState(SHADER)
               .setTextureState(new TextureStateShard(t, false, false))
               .setTransparencyState(SRC_ALPHA_ONE)
               .setCullState(NO_CULL)
               .setLightmapState(LIGHTMAP)
               .setDepthTestState(LEQUAL)
               .setWriteMaskState(COLOR_DEPTH_WRITE)
               .createCompositeState(false);
            return RenderType.create("thaumcraft_additive_depth_" + key, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, Mode.QUADS, 256, false, true, state);
         }
      );
   }
}
