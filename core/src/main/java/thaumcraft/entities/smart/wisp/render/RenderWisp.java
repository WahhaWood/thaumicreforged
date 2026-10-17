package thaumcraft.entities.smart.wisp.render;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.awt.Color;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.entities.smart.wisp.EntityWisp;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RenderWisp extends EntityRenderer<EntityWisp> {
   private static final Color DEFAULT_COLOR = new Color(0);
   public static final ResourceLocation WISP_TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/wisp.png");
   public static final ResourceLocation PARTICLE_TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particles.png");
   private static final TransparencyStateShard TC_ADDITIVE = new TransparencyStateShard("thaumcraft_additive", () -> {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
   }, () -> {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
   });
   private static final ShaderStateShard SHADER = new ShaderStateShard(GameRenderer::getPositionColorTexLightmapShader);
   private static final TextureStateShard TEX_WISP = new TextureStateShard(WISP_TEX, false, false);
   private static final TextureStateShard TEX_PART = new TextureStateShard(PARTICLE_TEX, false, false);
   private static final LightmapStateShard LIGHTMAP_ON = new LightmapStateShard(true);
   private static final OverlayStateShard OVERLAY_OFF = new OverlayStateShard(false);
   private static final CullStateShard NO_CULL = new CullStateShard(false);
   private static final DepthTestStateShard LEQUAL = new DepthTestStateShard("<=", 515);
   private static final WriteMaskStateShard COLOR_WRITE_ONLY = new WriteMaskStateShard(true, false);
   private static final RenderType LAYER_WISP = RenderType.create(
      "thaumcraft_wisp",
      DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
      Mode.QUADS,
      256,
      false,
      true,
      CompositeState.builder()
         .setShaderState(SHADER)
         .setTextureState(TEX_WISP)
         .setTransparencyState(TC_ADDITIVE)
         .setWriteMaskState(COLOR_WRITE_ONLY)
         .setCullState(NO_CULL)
         .setDepthTestState(LEQUAL)
         .setLightmapState(LIGHTMAP_ON)
         .setOverlayState(OVERLAY_OFF)
         .createCompositeState(true)
   );
   private static final RenderType LAYER_PARTICLES = RenderType.create(
      "thaumcraft_wisp_particles",
      DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
      Mode.QUADS,
      256,
      false,
      true,
      CompositeState.builder()
         .setShaderState(SHADER)
         .setTextureState(TEX_PART)
         .setTransparencyState(TC_ADDITIVE)
         .setWriteMaskState(COLOR_WRITE_ONLY)
         .setCullState(NO_CULL)
         .setDepthTestState(LEQUAL)
         .setLightmapState(LIGHTMAP_ON)
         .setOverlayState(OVERLAY_OFF)
         .createCompositeState(true)
   );

   public RenderWisp(Context ctx) {
      super(ctx);
      this.shadowRadius = 0.0F;
   }

   @OnlyIn(Dist.CLIENT)
   public void render(
      @NotNull EntityWisp entity, float entityYaw, float partialTicks, @NotNull PoseStack pose, @NotNull MultiBufferSource buffer, int packedLight
   ) {
      if (!(entity.getHealth() <= 0.0F)) {
         Color c = DEFAULT_COLOR;
         Aspect a = Aspect.getAspect(entity.getAspectTag());
         if (a != null) {
            c = new Color(a.getColor());
         }

         float r = c.getRed() / 255.0F;
         float g = c.getGreen() / 255.0F;
         float b = c.getBlue() / 255.0F;
         if (entity.hurtTime > 0) {
            r = 1.0F;
            g = c.getGreen() / 300.0F;
            b = c.getBlue() / 300.0F;
         }

         int fullBright = 15728880;
         int frame = entity.tickCount & 15;
         int fx = frame & 3;
         int fy = frame >> 2 & 3;
         float cell = 0.25F;
         float eps = 5.0E-4F;
         float u0 = fx * cell + eps;
         float v0 = fy * cell + eps;
         float u1 = (fx + 1) * cell - eps;
         float v1 = (fy + 1) * cell - eps;
         pose.pushPose();
         pose.translate(0.0, 0.45, 0.0);
         pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
         VertexConsumer vc = buffer.getBuffer(LAYER_WISP);
         emitQuad(pose, vc, r, g, b, 1.0F, u0, v0, u1, v1, fullBright);
         pose.popPose();
         frame = entity.tickCount & 15;
         float u0x = frame / 16.0F;
         float u1x = (frame + 1) / 16.0F;
         cell = 0.3125F;
         eps = 0.375F;
         u0 = Mth.sin((entity.tickCount + partialTicks) / 10.0F) * 0.1F;
         v0 = 0.4F + u0;
         pose.pushPose();
         pose.translate(0.0, 0.45, 0.0);
         pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
         pose.scale(v0, v0, v0);
         VertexConsumer vcx = buffer.getBuffer(LAYER_PARTICLES);
         emitQuad(pose, vcx, 1.0F, 1.0F, 1.0F, 1.0F, u0x, cell, u1x, eps, fullBright);
         pose.popPose();
         super.render(entity, entityYaw, partialTicks, pose, buffer, packedLight);
      }
   }

   @OnlyIn(Dist.CLIENT)
   private static void emitQuad(PoseStack pose, VertexConsumer vc, float r, float g, float b, float a, float u0, float v0, float u1, float v1, int light) {
      Matrix4f mat = pose.last().pose();
      vc.addVertex(mat, -1.0F, -1.0F, 0.0F).setColor(r, g, b, a).setUv(u1, v1).setUv2(light & 65535, light >> 16);
      vc.addVertex(mat, -1.0F, 1.0F, 0.0F).setColor(r, g, b, a).setUv(u1, v0).setUv2(light & 65535, light >> 16);
      vc.addVertex(mat, 1.0F, 1.0F, 0.0F).setColor(r, g, b, a).setUv(u0, v0).setUv2(light & 65535, light >> 16);
      vc.addVertex(mat, 1.0F, -1.0F, 0.0F).setColor(r, g, b, a).setUv(u0, v1).setUv2(light & 65535, light >> 16);
   }

   @NotNull
   public ResourceLocation getTextureLocation(@NotNull EntityWisp entity) {
      return WISP_TEX;
   }
}
