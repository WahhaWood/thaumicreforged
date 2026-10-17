package thaumcraft.entities.brainless.dart;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class RenderDart<T extends AbstractArrow> extends EntityRenderer<T> {
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/projectiles/arrow.png");
   private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE);
   private static final float SCALE = 0.025625F;
   private static final float SCALE_X = 0.01921875F;

   public RenderDart(Context context) {
      super(context);
      this.shadowRadius = 0.0F;
   }

   @OnlyIn(Dist.CLIENT)
   public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      poseStack.pushPose();
      float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
      float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
      poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 90.0F));
      poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
      float shake = entity.shakeTime - partialTicks;
      if (shake > 0.0F) {
         poseStack.mulPose(Axis.ZP.rotationDegrees(-Mth.sin(shake * 3.0F) * shake));
      }

      int i = 0;
      float u0 = 0.0F;
      float u1 = 0.5F;
      float v0 = (0.0F + i * 10.0F) / 32.0F;
      float v1 = (5.0F + i * 10.0F) / 32.0F;
      float u2 = 0.0F;
      float u3 = 0.15625F;
      float v2 = (5.0F + i * 10.0F) / 32.0F;
      float v3 = (10.0F + i * 10.0F) / 32.0F;
      poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
      poseStack.scale(0.01921875F, 0.025625F, 0.025625F);
      poseStack.translate(-4.0F, 0.0F, 0.0F);
      VertexConsumer vc = buffer.getBuffer(RENDER_TYPE);
      int r = 128;
      int g = 128;
      int b = 153;
      int a = 255;
      Pose pose = poseStack.last();
      Matrix4f mat = pose.pose();
      Matrix3f nmat = pose.normal();
      vertex(mat, nmat, vc, -7.0F, -2.0F, -2.0F, u2, v2, r, g, b, a, packedLight, 1.0F, 0.0F, 0.0F);
      vertex(mat, nmat, vc, -7.0F, -2.0F, 2.0F, u3, v2, r, g, b, a, packedLight, 1.0F, 0.0F, 0.0F);
      vertex(mat, nmat, vc, -7.0F, 2.0F, 2.0F, u3, v3, r, g, b, a, packedLight, 1.0F, 0.0F, 0.0F);
      vertex(mat, nmat, vc, -7.0F, 2.0F, -2.0F, u2, v3, r, g, b, a, packedLight, 1.0F, 0.0F, 0.0F);
      vertex(mat, nmat, vc, -7.0F, 2.0F, -2.0F, u2, v2, r, g, b, a, packedLight, -1.0F, 0.0F, 0.0F);
      vertex(mat, nmat, vc, -7.0F, 2.0F, 2.0F, u3, v2, r, g, b, a, packedLight, -1.0F, 0.0F, 0.0F);
      vertex(mat, nmat, vc, -7.0F, -2.0F, 2.0F, u3, v3, r, g, b, a, packedLight, -1.0F, 0.0F, 0.0F);
      vertex(mat, nmat, vc, -7.0F, -2.0F, -2.0F, u2, v3, r, g, b, a, packedLight, -1.0F, 0.0F, 0.0F);

      for (int j = 0; j < 4; j++) {
         poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
         Pose p = poseStack.last();
         Matrix4f m = p.pose();
         Matrix3f nm = p.normal();
         vertex(m, nm, vc, -8.0F, -2.0F, 0.0F, u0, v0, r, g, b, a, packedLight, 0.0F, 0.0F, 1.0F);
         vertex(m, nm, vc, 8.0F, -2.0F, 0.0F, u1, v0, r, g, b, a, packedLight, 0.0F, 0.0F, 1.0F);
         vertex(m, nm, vc, 8.0F, 2.0F, 0.0F, u1, v1, r, g, b, a, packedLight, 0.0F, 0.0F, 1.0F);
         vertex(m, nm, vc, -8.0F, 2.0F, 0.0F, u0, v1, r, g, b, a, packedLight, 0.0F, 0.0F, 1.0F);
      }

      poseStack.popPose();
      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
   }

   @OnlyIn(Dist.CLIENT)
   private static void vertex(
      Matrix4f mat,
      Matrix3f nmat,
      VertexConsumer vc,
      float x,
      float y,
      float z,
      float u,
      float v,
      int r,
      int g,
      int b,
      int a,
      int packedLight,
      float nx,
      float ny,
      float nz
   ) {
      vc.addVertex(mat, x, y, z).setColor(r, g, b, a).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(nx, ny, nz);
   }

   public ResourceLocation getTextureLocation(T entity) {
      return TEXTURE;
   }
}
