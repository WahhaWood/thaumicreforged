package thaumcraft.entities.smart.tentacle.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.entities.smart.tentacle.EntityTantacle;

public class TantacleRenderer extends EntityRenderer<EntityTantacle> {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/taintacle.png");
   private final ModelTantacle model;

   public TantacleRenderer(Context ctx) {
      super(ctx);
      this.model = new ModelTantacle(ctx.bakeLayer(ModelTantacle.LAYER));
      this.shadowRadius = 0.25F;
   }

   public void render(EntityTantacle entity, float entityYaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight) {
      float h = entity.getBbHeight();
      this.shadowRadius = h / 8.0F;
      float age = entity.tickCount + partialTick;
      float t = h * 10.0F;
      float rise = 0.0F;
      if (age < t) {
         rise = (t - age) / t * h;
      }

      float baseY = (h == 3.0F ? 0.6F : 1.2F) + rise;
      float s = h / 3.0F;
      pose.pushPose();
      pose.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
      pose.scale(-1.0F, -1.0F, 1.0F);
      pose.translate(0.0, -1.5, 0.0);
      pose.translate(0.0, baseY, 0.0);
      pose.scale(s, s, s);
      this.model.setupAnim(entity, 0.0F, 0.0F, age, 0.0F, 0.0F);
      VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity)));
      this.model.renderToBuffer(pose, vc, 15728880, OverlayTexture.NO_OVERLAY, -1);
      pose.popPose();
      super.render(entity, entityYaw, partialTick, pose, buffer, packedLight);
   }

   public ResourceLocation getTextureLocation(EntityTantacle entity) {
      return TEX;
   }
}
