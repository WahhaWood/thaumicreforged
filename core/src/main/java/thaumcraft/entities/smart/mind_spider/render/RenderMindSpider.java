package thaumcraft.entities.smart.mind_spider.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.entities.smart.mind_spider.EntityMindSpider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RenderMindSpider extends MobRenderer<EntityMindSpider, SpiderModel<EntityMindSpider>> {
   public static final ResourceLocation BODY = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/taint_spider.png");
   public static final ResourceLocation EYES = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/taint_spider_eyes.png");

   public RenderMindSpider(Context ctx) {
      super(ctx, new SpiderModel(ctx.bakeLayer(ModelLayers.SPIDER)), 0.0F);
   }

   public ResourceLocation getTextureLocation(EntityMindSpider entity) {
      return BODY;
   }

   protected float getFlipDegrees(EntityMindSpider entity) {
      return 180.0F;
   }

   @OnlyIn(Dist.CLIENT)
   protected void scale(EntityMindSpider entity, PoseStack poseStack, float partialTicks) {
      float s = entity.spiderScaleAmount();
      poseStack.scale(s, s, s);
   }

   @OnlyIn(Dist.CLIENT)
   public void render(EntityMindSpider entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      Minecraft mc = Minecraft.getInstance();
      String viewer = entity.getViewer();
      if (!viewer.isEmpty()) {
         if (mc.player == null) {
            return;
         }

         String me = mc.player.getGameProfile().getName();
         if (!viewer.equals(me)) {
            return;
         }
      }

      this.shadowRadius = entity.getShadowSize();
      poseStack.pushPose();
      ((SpiderModel)this.model).attackTime = this.getAttackAnim(entity, partialTicks);
      ((SpiderModel)this.model).riding = entity.isPassenger();
      ((SpiderModel)this.model).young = entity.isBaby();
      float bodyYaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
      float headYaw = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
      float netHeadYaw = headYaw - bodyYaw;
      if (((SpiderModel)this.model).riding && entity.getVehicle() instanceof LivingEntity living) {
         bodyYaw = Mth.rotLerp(partialTicks, living.yBodyRotO, living.yBodyRot);
         netHeadYaw = headYaw - bodyYaw;
         float f = Mth.wrapDegrees(netHeadYaw);
         if (f < -85.0F) {
            f = -85.0F;
         }

         if (f > 85.0F) {
            f = 85.0F;
         }

         bodyYaw = headYaw - f;
         if (f * f > 2500.0F) {
            bodyYaw += f * 0.2F;
         }

         netHeadYaw = headYaw - bodyYaw;
      }

      float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
      float bob = this.getBob(entity, partialTicks);
      this.setupRotations(entity, poseStack, bob, entityYaw, partialTicks, 1.0F);
      poseStack.scale(-1.0F, -1.0F, 1.0F);
      this.scale(entity, poseStack, partialTicks);
      poseStack.translate(0.0, -1.501, 0.0);
      float limbSwing = entity.walkAnimation.position(partialTicks);
      float limbSwingAmount = entity.walkAnimation.speed(partialTicks);
      if (entity.isBaby()) {
         limbSwing *= 3.0F;
      }

      if (limbSwingAmount > 1.0F) {
         limbSwingAmount = 1.0F;
      }

      ((SpiderModel)this.model).prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
      ((SpiderModel)this.model).setupAnim(entity, limbSwing, limbSwingAmount, bob, netHeadYaw, pitch);
      int overlay = LivingEntityRenderer.getOverlayCoords(entity, this.getWhiteOverlayProgress(entity, partialTicks));
      float alpha = Math.min(0.1F, (entity.tickCount + partialTicks) / 100.0F);
      VertexConsumer bodyConsumer = buffer.getBuffer(RenderType.entityTranslucent(BODY));
      int bodyColor = Mth.clamp((int)(alpha * 255.0F), 0, 255) << 24 | 16777215;
      ((SpiderModel)this.model).renderToBuffer(poseStack, bodyConsumer, packedLight, overlay, bodyColor);
      VertexConsumer eyesConsumer = buffer.getBuffer(RenderType.eyes(EYES));
      ((SpiderModel)this.model).renderToBuffer(poseStack, eyesConsumer, 15728880, overlay, -1);
      poseStack.popPose();
   }
}
