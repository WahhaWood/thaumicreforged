package thaumcraft.entities.smart.eldrich_warden.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.entities.smart.eldrich_guardian.render.EldritchGuardianModel;
import thaumcraft.entities.smart.eldrich_warden.EntityEldritchWarden;

public class EldritchWardenEyeLayer<T extends EntityEldritchWarden> extends RenderLayer<T, EldritchGuardianModel<T>> {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/eldritch_warden.png");

   public EldritchWardenEyeLayer(RenderLayerParent<T, EldritchGuardianModel<T>> parent) {
      super(parent);
   }

   public void render(
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      T entity,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      float pulse = 0.75F + 0.25F * Mth.sin((entity.tickCount + partialTicks) / 3.0F);
      poseStack.pushPose();
      poseStack.scale(1.01F, 1.01F, 1.01F);
      VertexConsumer vc = buffer.getBuffer(RenderType.eyes(TEX));
      int alpha = Mth.clamp((int)(pulse * 255.0F), 0, 255);
      ((EldritchGuardianModel)this.getParentModel()).hoodEye().render(poseStack, vc, 15728880, OverlayTexture.NO_OVERLAY, alpha << 24 | 16777215);
      poseStack.popPose();
   }
}
