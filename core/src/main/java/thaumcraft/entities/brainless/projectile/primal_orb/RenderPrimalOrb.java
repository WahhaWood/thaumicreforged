package thaumcraft.entities.brainless.projectile.primal_orb;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.CullStateShard;
import net.minecraft.client.renderer.RenderStateShard.DepthTestStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard;
import net.minecraft.client.renderer.RenderStateShard.WriteMaskStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RenderPrimalOrb extends EntityRenderer<EntityPrimalOrb> {
   public static final ResourceLocation PARTICLE_TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particles.png");
   private static final int[] PRIMAL_COLORS = new int[]{16777215, 16777086, 8323071, 8322942, 16744062, 8290047, 11184810};
   private static final ShaderStateShard POSITION_COLOR_SHADER = new ShaderStateShard(GameRenderer::getPositionColorShader);
   private static final ShaderStateShard POSITION_TEX_COLOR_SHADER = new ShaderStateShard(GameRenderer::getPositionTexColorShader);
   private static final TransparencyStateShard ADDITIVE_ALPHA_TRANSPARENCY = new TransparencyStateShard("thaumcraft_primal_orb_additive", () -> {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
   }, () -> {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
   });
   private static final CullStateShard NO_CULL = new CullStateShard(false);
   private static final DepthTestStateShard LEQUAL_DEPTH = new DepthTestStateShard("lequal", 515);
   private static final WriteMaskStateShard COLOR_WRITE_ONLY = new WriteMaskStateShard(true, false);
   private static final RenderType SPIKES = RenderType.create(
      "thaumcraft_primal_orb_spikes",
      DefaultVertexFormat.POSITION_COLOR,
      Mode.TRIANGLES,
      256,
      false,
      true,
      CompositeState.builder()
         .setShaderState(POSITION_COLOR_SHADER)
         .setTransparencyState(ADDITIVE_ALPHA_TRANSPARENCY)
         .setCullState(NO_CULL)
         .setDepthTestState(LEQUAL_DEPTH)
         .setWriteMaskState(COLOR_WRITE_ONLY)
         .createCompositeState(false)
   );
   private static final RenderType QUAD = RenderType.create(
      "thaumcraft_primal_orb_quad",
      DefaultVertexFormat.POSITION_TEX_COLOR,
      Mode.QUADS,
      256,
      false,
      true,
      CompositeState.builder()
         .setShaderState(POSITION_TEX_COLOR_SHADER)
         .setTextureState(new TextureStateShard(PARTICLE_TEX, false, false))
         .setTransparencyState(ADDITIVE_ALPHA_TRANSPARENCY)
         .setCullState(NO_CULL)
         .setDepthTestState(LEQUAL_DEPTH)
         .setWriteMaskState(COLOR_WRITE_ONLY)
         .createCompositeState(false)
   );

   public RenderPrimalOrb(Context ctx) {
      super(ctx);
      this.shadowRadius = 0.0F;
   }

   @OnlyIn(Dist.CLIENT)
   public void render(EntityPrimalOrb entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      float t = entity.tickCount + partialTick;
      float n6 = t / 80.0F;
      Random random = new Random(entity.getId());
      poseStack.pushPose();
      VertexConsumer spikes = buffer.getBuffer(SPIKES);

      for (int i = 0; i < 12; i++) {
         poseStack.pushPose();
         poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
         poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
         poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F));
         poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
         poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
         poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F + n6 * 360.0F));
         float n8 = random.nextFloat() * 20.0F + 5.0F;
         float n9 = random.nextFloat() * 2.0F + 1.0F;
         float scale = Math.min(entity.tickCount, 10) / 10.0F;
         float denom = 30.0F / (scale <= 0.0F ? 1.0E-4F : scale);
         float n10 = n8 / denom;
         float n11 = n9 / denom;
         int col = PRIMAL_COLORS[(i / 2 + 1) % PRIMAL_COLORS.length];
         int r = col >> 16 & 0xFF;
         int g = col >> 8 & 0xFF;
         int b = col & 0xFF;
         float ax = (float)(-0.866 * n11);
         float az = (float)(-0.5 * n11);
         float bx = (float)(0.866 * n11);
         float bz = (float)(-0.5 * n11);
         float cx = 0.0F;
         float cz = (float)(1.0 * n11);
         Matrix4f mat = poseStack.last().pose();
         tri(spikes, mat, 0.0F, 0.0F, 0.0F, 255, 255, 255, 255, ax, n10, az, r, g, b, 0, bx, n10, bz, r, g, b, 0);
         tri(spikes, mat, 0.0F, 0.0F, 0.0F, 255, 255, 255, 255, bx, n10, bz, r, g, b, 0, cx, n10, cz, r, g, b, 0);
         tri(spikes, mat, 0.0F, 0.0F, 0.0F, 255, 255, 255, 255, cx, n10, cz, r, g, b, 0, ax, n10, az, r, g, b, 0);
         poseStack.popPose();
      }

      poseStack.pushPose();
      poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
      poseStack.scale(0.5F, 0.5F, 0.5F);
      VertexConsumer quad = buffer.getBuffer(QUAD);
      float u0 = entity.tickCount % 13 / 16.0F;
      float u1 = u0 + 0.0624375F;
      float v0 = 0.125F;
      float v1 = v0 + 0.0624375F;
      Matrix4f qm = poseStack.last().pose();
      int a = 204;
      vtx(quad, qm, -0.5F, -0.5F, 0.0F, 255, 255, 255, a, u0, v1);
      vtx(quad, qm, 0.5F, -0.5F, 0.0F, 255, 255, 255, a, u1, v1);
      vtx(quad, qm, 0.5F, 0.5F, 0.0F, 255, 255, 255, a, u1, v0);
      vtx(quad, qm, -0.5F, 0.5F, 0.0F, 255, 255, 255, a, u0, v0);
      poseStack.popPose();
      poseStack.popPose();
      super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
   }

   public ResourceLocation getTextureLocation(EntityPrimalOrb entity) {
      return PARTICLE_TEX;
   }

   @OnlyIn(Dist.CLIENT)
   private static void tri(
      VertexConsumer vc,
      Matrix4f m,
      float x0,
      float y0,
      float z0,
      int r0,
      int g0,
      int b0,
      int a0,
      float x1,
      float y1,
      float z1,
      int r1,
      int g1,
      int b1,
      int a1,
      float x2,
      float y2,
      float z2,
      int r2,
      int g2,
      int b2,
      int a2
   ) {
      vc.addVertex(m, x0, y0, z0).setColor(r0, g0, b0, a0);
      vc.addVertex(m, x1, y1, z1).setColor(r1, g1, b1, a1);
      vc.addVertex(m, x2, y2, z2).setColor(r2, g2, b2, a2);
   }

   @OnlyIn(Dist.CLIENT)
   private static void vtx(VertexConsumer vc, Matrix4f m, float x, float y, float z, int r, int g, int b, int a, float u, float v) {
      vc.addVertex(m, x, y, z).setColor(r, g, b, a).setUv(u, v);
   }
}
