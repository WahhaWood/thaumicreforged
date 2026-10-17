package thaumcraft.entities.smart.edlritch_crab;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class EldritchCrabRenderer extends MobRenderer<EntityEldritchCrab, ModelEldritchCrab> {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/crab.png");
   public static final ResourceLocation OVERLAY = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/craboverlay.png");

   public EldritchCrabRenderer(Context ctx) {
      super(ctx, new ModelEldritchCrab(ctx.bakeLayer(ModelEldritchCrab.LAYER_LOCATION)), 1.0F);
      this.addLayer(new EldritchCrabRenderer.OverlayLayer(this));
   }

   public ResourceLocation getTextureLocation(EntityEldritchCrab entity) {
      return TEX;
   }

   public static final class OverlayLayer extends RenderLayer<EntityEldritchCrab, ModelEldritchCrab> {
      public OverlayLayer(EldritchCrabRenderer parent) {
         super(parent);
      }

      public void render(
         PoseStack poseStack,
         MultiBufferSource buffer,
         int packedLight,
         EntityEldritchCrab entity,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         boolean inv = entity.isInvisible();
         RenderSystem.depthMask(!inv);
         VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(EldritchCrabRenderer.OVERLAY));
         ((ModelEldritchCrab)this.getParentModel()).renderToBuffer(poseStack, vc, 15728880, OverlayTexture.NO_OVERLAY, -1);
         RenderSystem.depthMask(true);
      }
   }
}
