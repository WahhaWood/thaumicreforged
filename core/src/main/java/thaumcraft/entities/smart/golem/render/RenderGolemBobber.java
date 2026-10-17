package thaumcraft.entities.smart.golem.render;

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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import thaumcraft.entities.smart.golem.EntityGolemBase;
import thaumcraft.entities.smart.golem.bobber.EntityGolemBobber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RenderGolemBobber extends EntityRenderer<EntityGolemBobber> {
   public static final ResourceLocation TEX = ResourceLocation.withDefaultNamespace("textures/misc/particles.png");

   public RenderGolemBobber(Context ctx) {
      super(ctx);
   }

   @OnlyIn(Dist.CLIENT)
   public void render(
      @NotNull EntityGolemBobber entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight
   ) {
      poseStack.pushPose();
      poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
      poseStack.scale(0.5F, 0.5F, 0.5F);
      int u = 1;
      int v = 2;
      float u0 = u * 8 / 128.0F;
      float u1 = (u * 8 + 8) / 128.0F;
      float v0 = v * 8 / 128.0F;
      float v1 = (v * 8 + 8) / 128.0F;
      VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(TEX));
      Pose last = poseStack.last();
      vc.addVertex(last.pose(), -0.5F, -0.5F, 0.0F)
         .setColor(255, 255, 255, 255)
         .setUv(u0, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(last.pose(), 0.5F, -0.5F, 0.0F)
         .setColor(255, 255, 255, 255)
         .setUv(u1, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(last.pose(), 0.5F, 0.5F, 0.0F)
         .setColor(255, 255, 255, 255)
         .setUv(u1, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(last.pose(), -0.5F, 0.5F, 0.0F)
         .setColor(255, 255, 255, 255)
         .setUv(u0, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(packedLight)
         .setNormal(0.0F, 1.0F, 0.0F);
      poseStack.popPose();
      EntityGolemBase fisher = entity.getFisher();
      if (fisher != null) {
         double bx = Mth.lerp(partialTick, entity.xo, entity.getX());
         double by = Mth.lerp(partialTick, entity.yo, entity.getY()) + 0.25;
         double bz = Mth.lerp(partialTick, entity.zo, entity.getZ());
         float arm = fisher.rightArm / 3.0F;
         float sin = Mth.sin(Mth.sqrt(arm) * (float) Math.PI);
         Vec3 vec = new Vec3(-0.5, 0.03, 0.8);
         float pitch = Mth.lerp(partialTick, fisher.xRotO, fisher.getXRot());
         float yaw = Mth.lerp(partialTick, fisher.yRotO, fisher.getYRot());
         vec = vec.xRot(-pitch * (float) (Math.PI / 180.0));
         vec = vec.yRot(-yaw * (float) (Math.PI / 180.0));
         vec = vec.yRot(sin * 0.5F);
         vec = vec.xRot(-sin * 0.7F);
         float bodyYaw = Mth.lerp(partialTick, fisher.yBodyRotO, fisher.yBodyRot) * (float) (Math.PI / 180.0);
         double sy = Mth.sin(bodyYaw);
         double cy = Mth.cos(bodyYaw);
         double hx = Mth.lerp(partialTick, fisher.xo, fisher.getX()) - cy * 0.25 - sy * 0.7;
         double hy = Mth.lerp(partialTick, fisher.yo, fisher.getY()) - 0.4;
         double hz = Mth.lerp(partialTick, fisher.zo, fisher.getZ()) - sy * 0.25 + cy * 0.7;
         double dx = hx - bx;
         double dy = hy - by;
         double dz = hz - bz;
         VertexConsumer line = buffer.getBuffer(RenderType.lineStrip());
         Pose pose = poseStack.last();

         for (int i = 0; i <= 16; i++) {
            float t = i / 16.0F;
            float px = (float)(dx * t);
            float py = (float)(dy * (t * t + t) * 0.5 + 0.25);
            float pz = (float)(dz * t);
            line.addVertex(pose.pose(), px, py, pz).setColor(0, 0, 0, 255).setNormal(0.0F, 1.0F, 0.0F);
         }
      }

      super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
   }

   @NotNull
   public ResourceLocation getTextureLocation(@NotNull EntityGolemBobber entity) {
      return TEX;
   }
}
