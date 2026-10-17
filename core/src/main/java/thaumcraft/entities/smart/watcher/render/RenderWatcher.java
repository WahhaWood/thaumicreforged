package thaumcraft.entities.smart.watcher.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import thaumcraft.Thaumcraft;
import thaumcraft.entities.smart.watcher.EntityWatcher;
import thaumcraft.util.ThaumcraftRenderTypes;

@OnlyIn(Dist.CLIENT)
public class RenderWatcher extends MobRenderer<EntityWatcher, ModelWatcher<EntityWatcher>> {
   public static final ResourceLocation WATCHER_TEX = Thaumcraft.location("textures/model/watcher.png");
   public static final ResourceLocation BEAM_TEX = Thaumcraft.location("textures/model/watcher_beam.png");

   public RenderWatcher(Context ctx) {
      super(ctx, new ModelWatcher(ctx.bakeLayer(ModelWatcher.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(EntityWatcher entity) {
      return WATCHER_TEX;
   }

   @OnlyIn(Dist.CLIENT)
   public void render(EntityWatcher entity, float entityYaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int packedLight) {
      super.render(entity, entityYaw, partialTicks, pose, buffers, packedLight);
      LivingEntity target = entity.getActiveAttackTarget();
      if (target != null) {
         float prog = entity.getAttackAnimationScale(partialTicks);
         if (!(prog <= 0.0F)) {
            renderBeam(entity, target, prog, partialTicks, pose, buffers);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private static void renderBeam(EntityWatcher watcher, LivingEntity target, float prog, float partialTicks, PoseStack pose, MultiBufferSource buffers) {
      Vec3 watcherPos = watcher.getPosition(partialTicks);
      Vec3 watcherEye = watcherPos.add(0.0, watcher.getEyeHeight(), 0.0);
      Vec3 targetPos = target.getPosition(partialTicks).add(0.0, target.getBbHeight() * 0.5, 0.0);
      Vec3 delta = targetPos.subtract(watcherEye);
      double len = delta.length() + 1.0;
      if (!(len <= 1.0E-6)) {
         Vec3 n = delta.normalize();
         float yawDeg = (float)(((Math.PI / 2) - Math.atan2(n.z, n.x)) * (180.0 / Math.PI));
         float pitchDeg = (float)(Math.acos(Mth.clamp(n.y, -1.0, 1.0)) * (180.0 / Math.PI));
         float f = prog * prog;
         int r = 64 + (int)(f * 240.0F);
         int g = 32 + (int)(f * 192.0F);
         int b = 128 - (int)(f * 64.0F);
         int a = 255;
         float age = watcher.tickCount + partialTicks;
         float vScroll = age * 0.5F % 1.0F;
         pose.pushPose();
         pose.translate(0.0, watcher.getEyeHeight(), 0.0);
         pose.mulPose(Axis.YP.rotationDegrees(yawDeg));
         pose.mulPose(Axis.XP.rotationDegrees(pitchDeg));
         Pose last = pose.last();
         Matrix4f mat = last.pose();
         VertexConsumer vc = buffers.getBuffer(ThaumcraftRenderTypes.watcherBeam(BEAM_TEX));
         double n14 = 0.2;
         double n15 = n14 * 1.41;
         double n12 = age * 0.05 * -1.5;
         double n16 = Math.cos(n12 + (Math.PI * 3.0 / 4.0)) * n15;
         double n17 = Math.sin(n12 + (Math.PI * 3.0 / 4.0)) * n15;
         double n18 = Math.cos(n12 + (Math.PI / 4)) * n15;
         double n19 = Math.sin(n12 + (Math.PI / 4)) * n15;
         double n20 = Math.cos(n12 + (Math.PI * 5.0 / 4.0)) * n15;
         double n21 = Math.sin(n12 + (Math.PI * 5.0 / 4.0)) * n15;
         double n22 = Math.cos(n12 + (Math.PI * 7.0 / 4.0)) * n15;
         double n23 = Math.sin(n12 + (Math.PI * 7.0 / 4.0)) * n15;
         double n24 = Math.cos(n12 + Math.PI) * n14;
         double n25 = Math.sin(n12 + Math.PI) * n14;
         double n26 = Math.cos(n12) * n14;
         double n27 = Math.sin(n12) * n14;
         double n28 = Math.cos(n12 + (Math.PI / 2)) * n14;
         double n29 = Math.sin(n12 + (Math.PI / 2)) * n14;
         double n30 = Math.cos(n12 + (Math.PI * 3.0 / 2.0)) * n14;
         double n31 = Math.sin(n12 + (Math.PI * 3.0 / 2.0)) * n14;
         double u0 = 0.0;
         double u1 = 0.4999;
         double v0 = -1.0 + vScroll;
         double v1 = len * (0.5 / n14) + v0;
         int light = 15728880;
         v(vc, mat, (float)n24, (float)len, (float)n25, r, g, b, a, (float)u1, (float)v1, light);
         v(vc, mat, (float)n24, 0.0F, (float)n25, r, g, b, a, (float)u1, (float)v0, light);
         v(vc, mat, (float)n26, 0.0F, (float)n27, r, g, b, a, (float)u0, (float)v0, light);
         v(vc, mat, (float)n26, (float)len, (float)n27, r, g, b, a, (float)u0, (float)v1, light);
         v(vc, mat, (float)n28, (float)len, (float)n29, r, g, b, a, (float)u1, (float)v1, light);
         v(vc, mat, (float)n28, 0.0F, (float)n29, r, g, b, a, (float)u1, (float)v0, light);
         v(vc, mat, (float)n30, 0.0F, (float)n31, r, g, b, a, (float)u0, (float)v0, light);
         v(vc, mat, (float)n30, (float)len, (float)n31, r, g, b, a, (float)u0, (float)v1, light);
         float capV = watcher.tickCount % 2 == 0 ? 0.5F : 0.0F;
         v(vc, mat, (float)n16, (float)len, (float)n17, r, g, b, a, 0.5F, capV + 0.5F, light);
         v(vc, mat, (float)n18, (float)len, (float)n19, r, g, b, a, 1.0F, capV + 0.5F, light);
         v(vc, mat, (float)n22, (float)len, (float)n23, r, g, b, a, 1.0F, capV, light);
         v(vc, mat, (float)n20, (float)len, (float)n21, r, g, b, a, 0.5F, capV, light);
         pose.popPose();
      }
   }

   @OnlyIn(Dist.CLIENT)
   private static void v(VertexConsumer vc, Matrix4f mat, float x, float y, float z, int r, int g, int b, int a, float u, float v, int light) {
      vc.addVertex(mat, x, y, z).setColor(r, g, b, a).setUv(u, v).setUv2(light & 65535, light >> 16).setOverlay(OverlayTexture.NO_OVERLAY);
   }
}
