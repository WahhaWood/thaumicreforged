package thaumcraft.entities.brainless.eldrich_orb.render;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import thaumcraft.util.Utils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RenderEldritchOrb extends EntityRenderer<Entity> {
   public RenderEldritchOrb(Context ctx) {
      super(ctx);
      this.shadowRadius = 0.0F;
   }

   @OnlyIn(Dist.CLIENT)
   public void render(Entity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      Tesselator tess = Tesselator.getInstance();
      float t = (entity.tickCount + partialTicks) / 80.0F;
      float scaleIn = Math.min(entity.tickCount, 10) / 10.0F;
      int col = 15650047;
      int cr = col >> 16 & 0xFF;
      int cg = col >> 8 & 0xFF;
      int cb = col & 0xFF;
      Random rnd = new Random(entity.getId());
      poseStack.pushPose();
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
      RenderSystem.depthMask(false);
      RenderSystem.enableCull();
      poseStack.pushPose();

      for (int i = 0; i < 12; i++) {
         poseStack.mulPose(Axis.XP.rotationDegrees(rnd.nextFloat() * 360.0F));
         poseStack.mulPose(Axis.YP.rotationDegrees(rnd.nextFloat() * 360.0F));
         poseStack.mulPose(Axis.ZP.rotationDegrees(rnd.nextFloat() * 360.0F));
         poseStack.mulPose(Axis.XP.rotationDegrees(rnd.nextFloat() * 360.0F));
         poseStack.mulPose(Axis.YP.rotationDegrees(rnd.nextFloat() * 360.0F));
         poseStack.mulPose(Axis.ZP.rotationDegrees(rnd.nextFloat() * 360.0F + t * 360.0F));
         float n8 = rnd.nextFloat() * 20.0F + 5.0F;
         float n9 = rnd.nextFloat() * 2.0F + 1.0F;
         float n10 = n8 / 30.0F * scaleIn;
         float n11 = n9 / 30.0F * scaleIn;
         Matrix4f m = poseStack.last().pose();
         BufferBuilder bb = tess.begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
         bb.addVertex(m, 0.0F, 0.0F, 0.0F).setColor(255, 255, 255, 255);
         bb.addVertex(m, (float)(-0.866 * n11), n10, -0.5F * n11).setColor(cr, cg, cb, 0);
         bb.addVertex(m, (float)(0.866 * n11), n10, -0.5F * n11).setColor(cr, cg, cb, 0);
         bb.addVertex(m, 0.0F, n10, 1.0F * n11).setColor(cr, cg, cb, 0);
         bb.addVertex(m, (float)(-0.866 * n11), n10, -0.5F * n11).setColor(cr, cg, cb, 0);
         BufferUploader.drawWithShader(bb.buildOrThrow());
      }

      poseStack.popPose();
      RenderSystem.depthMask(true);
      RenderSystem.disableCull();
      RenderSystem.disableBlend();
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, Utils.getParticles());
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      float u0 = entity.tickCount % 13 / 16.0F;
      float u1 = u0 + 0.0624375F;
      float v0 = 0.1875F;
      float v1 = v0 + 0.0624375F;
      poseStack.pushPose();
      poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
      Matrix4f m2 = poseStack.last().pose();
      BufferBuilder bb = tess.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      bb.addVertex(m2, -0.5F, -0.5F, 0.0F).setUv(u0, v1);
      bb.addVertex(m2, 0.5F, -0.5F, 0.0F).setUv(u1, v1);
      bb.addVertex(m2, 0.5F, 0.5F, 0.0F).setUv(u1, v0);
      bb.addVertex(m2, -0.5F, 0.5F, 0.0F).setUv(u0, v0);
      BufferUploader.drawWithShader(bb.buildOrThrow());
      poseStack.popPose();
      RenderSystem.disableBlend();
      poseStack.popPose();
      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
   }

   public ResourceLocation getTextureLocation(Entity entity) {
      return Utils.getParticles();
   }
}
