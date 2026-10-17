package thaumcraft.entities.brainless.projectile.ember;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.CullStateShard;
import net.minecraft.client.renderer.RenderStateShard.LightmapStateShard;
import net.minecraft.client.renderer.RenderStateShard.OverlayStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RenderEmber extends EntityRenderer<EntityEmber> {
   public static final ResourceLocation PARTICLE_TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particles.png");
   private static final TransparencyStateShard ADDITIVE_ALPHA_TRANSPARENCY = new TransparencyStateShard("thaumcraft_ember_additive_alpha", () -> {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
   }, () -> {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
   });
   private static final RenderType EMBER = RenderType.create(
      "thaumcraft_ember",
      DefaultVertexFormat.NEW_ENTITY,
      Mode.QUADS,
      256,
      false,
      true,
      CompositeState.builder()
         .setShaderState(new ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentShader))
         .setTextureState(new TextureStateShard(PARTICLE_TEXTURE, false, false))
         .setTransparencyState(ADDITIVE_ALPHA_TRANSPARENCY)
         .setCullState(new CullStateShard(false))
         .setLightmapState(new LightmapStateShard(true))
         .setOverlayState(new OverlayStateShard(true))
         .createCompositeState(true)
   );

   public RenderEmber(Context ctx) {
      super(ctx);
      this.shadowRadius = 0.0F;
   }

   @OnlyIn(Dist.CLIENT)
   public void render(EntityEmber e, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      poseStack.pushPose();
      int dur = Math.max(e.duration, 1);
      float progress = (e.tickCount + partialTick) / dur;
      progress = Mth.clamp(progress, 0.0F, 1.0F);
      float u0 = (7.0F + (int)(8.0F * progress)) / 16.0F;
      float u1 = u0 + 0.0625F;
      float v0 = 0.5625F;
      float v1 = v0 + 0.0625F;
      float s = 0.25F + progress;
      poseStack.scale(s, s, s);
      poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
      Pose last = poseStack.last();
      VertexConsumer vc = buffer.getBuffer(EMBER);
      int light = 15728880;
      float a = 0.9F;
      vc.addVertex(last.pose(), -0.5F, -0.5F, 0.0F)
         .setColor(1.0F, 1.0F, 1.0F, a)
         .setUv(u0, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(light & 65535, light >> 16)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(last.pose(), 0.5F, -0.5F, 0.0F)
         .setColor(1.0F, 1.0F, 1.0F, a)
         .setUv(u1, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(light & 65535, light >> 16)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(last.pose(), 0.5F, 0.5F, 0.0F)
         .setColor(1.0F, 1.0F, 1.0F, a)
         .setUv(u1, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(light & 65535, light >> 16)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(last.pose(), -0.5F, 0.5F, 0.0F)
         .setColor(1.0F, 1.0F, 1.0F, a)
         .setUv(u0, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(light & 65535, light >> 16)
         .setNormal(0.0F, 1.0F, 0.0F);
      poseStack.popPose();
      super.render(e, entityYaw, partialTick, poseStack, buffer, packedLight);
   }

   public ResourceLocation getTextureLocation(EntityEmber entity) {
      return PARTICLE_TEXTURE;
   }
}
