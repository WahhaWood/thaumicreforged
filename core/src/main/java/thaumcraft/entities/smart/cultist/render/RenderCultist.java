package thaumcraft.entities.smart.cultist.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.awt.Color;
import java.util.Random;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import thaumcraft.entities.smart.cultist.EntityCultistCleric;
import thaumcraft.entities.smart.cultist.EntityCultistLeader;
import thaumcraft.init.registry.Config;

@OnlyIn(Dist.CLIENT)
public class RenderCultist extends HumanoidMobRenderer<Mob, ModelCultist<Mob>> {
   public static final ResourceLocation SKIN = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/cultist.png");
   public static final ResourceLocation WISPY = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/wispy.png");

   @OnlyIn(Dist.CLIENT)
   public RenderCultist(Context context) {
      super(context, new ModelCultist(context.bakeLayer(ModelCultist.LAYER)), 0.5F);
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
      this.addLayer(
         new HumanoidArmorLayer(
            this,
            new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
            new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
            context.getModelManager()
         )
      );
   }

   public ResourceLocation getTextureLocation(Mob entity) {
      return SKIN;
   }

   @OnlyIn(Dist.CLIENT)
   protected void scale(Mob entity, PoseStack poseStack, float partialTickTime) {
      if (entity instanceof EntityCultistLeader) {
         poseStack.scale(1.25F, 1.25F, 1.25F);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void render(Mob entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      boolean ritual = entity instanceof EntityCultistCleric c && c.getIsRitualist() && c.hasValidRitualHome();
      float yOff = 0.0F;
      poseStack.pushPose();
      if (ritual) {
         int r = new Random(entity.getId()).nextInt(1000);
         yOff = Mth.sin((entity.tickCount + partialTicks + r) / 9.0F) * 0.1F + 0.21F;
         poseStack.translate(0.0, yOff, 0.0);
      }

      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
      if (ritual) {
         EntityCultistCleric cleric = (EntityCultistCleric)entity;
         BlockPos hp = cleric.getHomePosition();
         double bx = Mth.lerp(partialTicks, entity.xOld, entity.getX());
         double by = Mth.lerp(partialTicks, entity.yOld, entity.getY());
         double bz = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
         double sy = by + entity.getEyeHeight() * 1.2F;
         double hx = hp.getX() + 0.5;
         double hy = hp.getY() + 1.5 - yOff;
         double hz = hp.getZ() + 0.5;
         float n10 = Math.min(entity.tickCount, 10) / 10.0F;
         poseStack.pushPose();
         this.drawFloatyLine(poseStack, buffer, bx, by, bz, bx, sy, bz, hx, hy, hz, partialTicks, 1114129, -0.03F, n10, 0.25F);
         poseStack.popPose();
      }

      poseStack.popPose();
   }

   @OnlyIn(Dist.CLIENT)
   private void drawFloatyLine(
      PoseStack poseStack,
      MultiBufferSource buffer,
      double baseX,
      double baseY,
      double baseZ,
      double sx,
      double sy,
      double sz,
      double ex,
      double ey,
      double ez,
      float partialTicks,
      int rgb,
      float uSpeed,
      float lengthMul,
      float thickness
   ) {
      poseStack.translate(ex - baseX, ey - baseY, ez - baseZ);
      float time = (float)(System.nanoTime() / 30000000L);
      Color c = new Color(rgb);
      float r = c.getRed() / 255.0F;
      float g = c.getGreen() / 255.0F;
      float b = c.getBlue() / 255.0F;
      double dx = (float)(sx - ex);
      double dy = (float)(sy - ey);
      double dz = (float)(sz - ez);
      float dist = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
      float segF = Math.round(dist) * (Config.golemLinkQuality / 2.0F);
      if (!(segF <= 0.0F)) {
         int max = (int)(segF * lengthMul);
         float v0 = 0.0F;
         float v1 = 1.0F;
         VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(WISPY));
         Matrix4f mat = poseStack.last().pose();
         this.renderRibbonQuads(vc, mat, dx, dy, dz, dist, segF, max, time, uSpeed, thickness, r, g, b, true, v0, v1, sx, sy, sz);
         this.renderRibbonQuads(vc, mat, dx, dy, dz, dist, segF, max, time, uSpeed, thickness, r, g, b, false, v0, v1, sx, sy, sz);
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void renderRibbonQuads(
      VertexConsumer vc,
      Matrix4f mat,
      double dx,
      double dy,
      double dz,
      float dist,
      float segF,
      int max,
      float time,
      float uSpeed,
      float thickness,
      float r,
      float g,
      float b,
      boolean vertical,
      float v0,
      float v1,
      double sx,
      double sy,
      double sz
   ) {
      boolean havePrev = false;
      float prevU = 0.0F;
      float prevAx = 0.0F;
      float prevAy = 0.0F;
      float prevAz = 0.0F;
      float prevBx = 0.0F;
      float prevBy = 0.0F;
      float prevBz = 0.0F;
      int light = 15728880;
      int overlay = OverlayTexture.NO_OVERLAY;

      for (int i = 0; i <= max; i++) {
         float n26 = i / segF;
         float n27 = 1.0F - Math.abs(i - segF / 2.0F) / (segF / 2.0F);
         double n28 = dx + Mth.sin((float)((sz % 16.0 + dist * (1.0F - n26) * Config.golemLinkQuality / 2.0F - time % 32767.0F / 5.0F) / 4.0)) * 0.5F * n27;
         double n29 = dy + Mth.sin((float)((sx % 16.0 + dist * (1.0F - n26) * Config.golemLinkQuality / 2.0F - time % 32767.0F / 5.0F) / 3.0)) * 0.5F * n27;
         double n30 = dz + Mth.sin((float)((sy % 16.0 + dist * (1.0F - n26) * Config.golemLinkQuality / 2.0F - time % 32767.0F / 5.0F) / 2.0)) * 0.5F * n27;
         float u = (1.0F - n26) * dist - time * uSpeed;
         float ax;
         float ay;
         float az;
         float bx;
         float by;
         float bz;
         if (vertical) {
            ax = (float)(n28 * n26);
            ay = (float)(n29 * n26 - thickness);
            az = (float)(n30 * n26);
            bx = (float)(n28 * n26);
            by = (float)(n29 * n26 + thickness);
            bz = (float)(n30 * n26);
         } else {
            ax = (float)(n28 * n26 - thickness);
            ay = (float)(n29 * n26);
            az = (float)(n30 * n26);
            bx = (float)(n28 * n26 + thickness);
            by = (float)(n29 * n26);
            bz = (float)(n30 * n26);
         }

         if (havePrev) {
            vc.addVertex(mat, prevAx, prevAy, prevAz)
               .setColor(r, g, b, 0.8F)
               .setUv(prevU, v1)
               .setOverlay(overlay)
               .setUv2(light & 65535, light >> 16)
               .setNormal(0.0F, 1.0F, 0.0F);
            vc.addVertex(mat, prevBx, prevBy, prevBz)
               .setColor(r, g, b, 0.8F)
               .setUv(prevU, v0)
               .setOverlay(overlay)
               .setUv2(light & 65535, light >> 16)
               .setNormal(0.0F, 1.0F, 0.0F);
            vc.addVertex(mat, bx, by, bz)
               .setColor(r, g, b, 0.8F)
               .setUv(u, v0)
               .setOverlay(overlay)
               .setUv2(light & 65535, light >> 16)
               .setNormal(0.0F, 1.0F, 0.0F);
            vc.addVertex(mat, ax, ay, az)
               .setColor(r, g, b, 0.8F)
               .setUv(u, v1)
               .setOverlay(overlay)
               .setUv2(light & 65535, light >> 16)
               .setNormal(0.0F, 1.0F, 0.0F);
         }

         havePrev = true;
         prevU = u;
         prevAx = ax;
         prevAy = ay;
         prevAz = az;
         prevBx = bx;
         prevBy = by;
         prevBz = bz;
      }
   }
}
