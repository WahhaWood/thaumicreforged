package thaumcraft.entities.brainless.projectile.frost_shard;

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
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class RenderFrostShard extends EntityRenderer<EntityFrostShard> {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/block/frostshard.png");

   public RenderFrostShard(Context ctx) {
      super(ctx);
      this.shadowRadius = 0.0F;
   }

   public ResourceLocation getTextureLocation(EntityFrostShard entity) {
      return TEX;
   }

   @OnlyIn(Dist.CLIENT)
   public void render(EntityFrostShard entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      poseStack.pushPose();
      float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
      float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
      poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
      poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
      RandomSource random = RandomSource.create(entity.getId());
      float s = entity.getDamage() * 0.1F;
      poseStack.scale(s + random.nextFloat() * 0.1F, s + random.nextFloat() * 0.1F, s + random.nextFloat() * 0.1F);
      VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEX));
      renderCube(poseStack, vc, packedLight);
      poseStack.popPose();
      super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
   }

   @OnlyIn(Dist.CLIENT)
   private static void renderCube(PoseStack poseStack, VertexConsumer vc, int light) {
      Pose p = poseStack.last();
      Matrix4f m = p.pose();
      Matrix3f n = p.normal();
      float min = -0.5F;
      float max = 0.5F;
      quad(vc, m, n, light, min, min, max, max, min, max, max, max, max, min, max, max, 0.0F, 0.0F, 1.0F);
      quad(vc, m, n, light, max, min, min, min, min, min, min, max, min, max, max, min, 0.0F, 0.0F, -1.0F);
      quad(vc, m, n, light, min, max, min, min, max, max, max, max, max, max, max, min, 0.0F, 1.0F, 0.0F);
      quad(vc, m, n, light, min, min, max, min, min, min, max, min, min, max, min, max, 0.0F, -1.0F, 0.0F);
      quad(vc, m, n, light, min, min, min, min, max, min, max, max, min, max, min, min, -1.0F, 0.0F, 0.0F);
      quad(vc, m, n, light, max, min, max, max, max, max, min, max, max, min, min, max, 1.0F, 0.0F, 0.0F);
   }

   @OnlyIn(Dist.CLIENT)
   private static void quad(
      VertexConsumer vc,
      Matrix4f m,
      Matrix3f n,
      int light,
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
      float nx,
      float ny,
      float nz
   ) {
      vertex(vc, m, n, light, x1, y1, z1, 0.0F, 1.0F, nx, ny, nz);
      vertex(vc, m, n, light, x2, y2, z2, 1.0F, 1.0F, nx, ny, nz);
      vertex(vc, m, n, light, x3, y3, z3, 1.0F, 0.0F, nx, ny, nz);
      vertex(vc, m, n, light, x4, y4, z4, 0.0F, 0.0F, nx, ny, nz);
   }

   @OnlyIn(Dist.CLIENT)
   private static void vertex(VertexConsumer vc, Matrix4f m, Matrix3f n, int light, float x, float y, float z, float u, float v, float nx, float ny, float nz) {
      vc.addVertex(m, x, y, z)
         .setColor(255, 255, 255, 255)
         .setUv(u, v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(light & 65535, light >> 16)
         .setNormal(nx, ny, nz);
   }
}
