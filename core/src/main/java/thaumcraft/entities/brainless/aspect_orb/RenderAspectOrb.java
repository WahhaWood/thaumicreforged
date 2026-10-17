package thaumcraft.entities.brainless.aspect_orb;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.CullStateShard;
import net.minecraft.client.renderer.RenderStateShard.DepthTestStateShard;
import net.minecraft.client.renderer.RenderStateShard.LightmapStateShard;
import net.minecraft.client.renderer.RenderStateShard.OverlayStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard;
import net.minecraft.client.renderer.RenderStateShard.WriteMaskStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import thaumcraft.util.Utils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RenderAspectOrb extends EntityRenderer<AspectOrbEntity> {
   private static final Map<Integer, RenderType> ORB_TYPES = new ConcurrentHashMap<>();

   public RenderAspectOrb(Context context) {
      super(context);
      this.shadowRadius = 0.1F;
      this.shadowStrength = 0.5F;
   }

   @OnlyIn(Dist.CLIENT)
   public void render(AspectOrbEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      int destBlend = 1;
      int color = 16777215;
      float a = 1.0F;
      if (entity.getAspect() != null) {
         destBlend = entity.getAspect().getBlend();
         color = entity.getAspect().getColor();
         a = 0.5019608F;
      }

      float u0 = (int)(System.nanoTime() / 25000000L % 16L) / 16.0F;
      float u1 = u0 + 0.0625F;
      float v0 = 0.5F;
      float v1 = 0.5625F;
      float x0 = -0.5F;
      float x1 = 0.5F;
      float y0 = -0.25F;
      float y1 = 0.75F;
      float scale = 0.1F + 0.3F * ((float)(entity.orbMaxAge - entity.orbAge) / entity.orbMaxAge);
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      poseStack.pushPose();
      poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
      poseStack.scale(scale, scale, scale);
      RenderType type = ORB_TYPES.computeIfAbsent(destBlend, RenderAspectOrb::createOrbType);
      VertexConsumer vc = buffer.getBuffer(type);
      Pose last = poseStack.last();
      Matrix4f mat = last.pose();
      Matrix3f nmat = last.normal();
      vc.addVertex(mat, x0, y0, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u0, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(mat, x1, y0, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u1, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(mat, x1, y1, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u1, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(mat, x0, y1, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u0, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 1.0F, 0.0F);
      poseStack.popPose();
      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
   }

   public ResourceLocation getTextureLocation(AspectOrbEntity entity) {
      return Utils.getParticles();
   }

   @OnlyIn(Dist.CLIENT)
   private static RenderType createOrbType(int destBlend) {
      DestFactor dst = mapDestFactor(destBlend);
      TransparencyStateShard transparency = new TransparencyStateShard("thaumcraft_orb_blend_" + destBlend, () -> {
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, dst);
      }, () -> {
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
      });
      CompositeState state = CompositeState.builder()
         .setShaderState(new ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentShader))
         .setTextureState(new TextureStateShard(Utils.getParticles(), false, false))
         .setTransparencyState(transparency)
         .setDepthTestState(new DepthTestStateShard("lequal", 515))
         .setCullState(new CullStateShard(false))
         .setLightmapState(new LightmapStateShard(true))
         .setOverlayState(new OverlayStateShard(true))
         .setWriteMaskState(new WriteMaskStateShard(true, true))
         .createCompositeState(true);
      return RenderType.create("thaumcraft_aspect_orb_" + destBlend, DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, false, true, state);
   }

   private static DestFactor mapDestFactor(int glConst) {
      return switch (glConst) {
         case 0 -> DestFactor.ZERO;
         case 1 -> DestFactor.ONE;
         case 768 -> DestFactor.SRC_COLOR;
         case 769 -> DestFactor.ONE_MINUS_SRC_COLOR;
         case 770 -> DestFactor.SRC_ALPHA;
         case 771 -> DestFactor.ONE_MINUS_SRC_ALPHA;
         case 772 -> DestFactor.DST_ALPHA;
         case 773 -> DestFactor.ONE_MINUS_DST_ALPHA;
         case 774 -> DestFactor.DST_COLOR;
         case 775 -> DestFactor.ONE_MINUS_DST_COLOR;
         default -> DestFactor.ONE;
      };
   }
}
