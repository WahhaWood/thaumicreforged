package thaumicenergistics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArcaneAssemblerModel {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      ResourceLocation.fromNamespaceAndPath("thaumicenergistics", "arcane_assembler"), "main"
   );
   public static final float MODEL_SCALE = 0.47058824F;
   private static final float HALF_TURN = (float) (Math.PI / 2);
   private static final float FULL_TURN = (float) Math.PI;
   private final ModelPart outerFrameTop;
   private final ModelPart outerFrameBottom;
   private final ModelPart outerFrameLeft;
   private final ModelPart outerFrameRight;
   private final ModelPart glass;
   private final ModelPart innerFrameTop;
   private final ModelPart innerFrameBottom;
   private final ModelPart innerFrameLeft;
   private final ModelPart innerFrameRight;

   public ArcaneAssemblerModel(ModelPart root) {
      this.outerFrameTop = root.getChild("outer_frame_top");
      this.outerFrameBottom = root.getChild("outer_frame_bottom");
      this.outerFrameLeft = root.getChild("outer_frame_left");
      this.outerFrameRight = root.getChild("outer_frame_right");
      this.glass = root.getChild("glass");
      this.innerFrameTop = root.getChild("inner_frame_top");
      this.innerFrameBottom = root.getChild("inner_frame_bottom");
      this.innerFrameLeft = root.getChild("inner_frame_left");
      this.innerFrameRight = root.getChild("inner_frame_right");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("outer_frame_top", CubeListBuilder.create().texOffs(0, 0).addBox(-17.0F, -17.0F, -15.0F, 34.0F, 2.0F, 2.0F), PartPose.ZERO);
      root.addOrReplaceChild("outer_frame_bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-17.0F, 15.0F, -15.0F, 34.0F, 2.0F, 2.0F), PartPose.ZERO);
      root.addOrReplaceChild(
         "outer_frame_left",
         CubeListBuilder.create().texOffs(0, 0).addBox(-17.0F, -17.0F, -15.0F, 34.0F, 2.0F, 2.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) (-Math.PI / 2))
      );
      root.addOrReplaceChild(
         "outer_frame_right",
         CubeListBuilder.create().texOffs(0, 0).addBox(-17.0F, -17.0F, -15.0F, 34.0F, 2.0F, 2.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) (Math.PI / 2))
      );
      root.addOrReplaceChild("glass", CubeListBuilder.create().texOffs(0, 37).addBox(-13.0F, -13.0F, -14.0F, 26.0F, 26.0F, 1.0F), PartPose.ZERO);
      root.addOrReplaceChild("inner_frame_top", CubeListBuilder.create().texOffs(0, 4).addBox(-15.0F, -15.0F, -15.0F, 30.0F, 2.0F, 2.0F), PartPose.ZERO);
      root.addOrReplaceChild("inner_frame_bottom", CubeListBuilder.create().texOffs(0, 4).addBox(-15.0F, 13.0F, -15.0F, 30.0F, 2.0F, 2.0F), PartPose.ZERO);
      root.addOrReplaceChild(
         "inner_frame_left",
         CubeListBuilder.create().texOffs(0, 4).addBox(-15.0F, -15.0F, -15.0F, 30.0F, 2.0F, 2.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) (-Math.PI / 2))
      );
      root.addOrReplaceChild(
         "inner_frame_right",
         CubeListBuilder.create().texOffs(0, 4).addBox(-15.0F, -15.0F, -15.0F, 30.0F, 2.0F, 2.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) (Math.PI / 2))
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public void renderFrame(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
      this.renderOuterFrameSide(0.0F, poseStack, buffer, packedLight, packedOverlay);
      this.renderOuterFrameSide((float) (Math.PI / 2), poseStack, buffer, packedLight, packedOverlay);
      this.renderOuterFrameSide((float) (-Math.PI / 2), poseStack, buffer, packedLight, packedOverlay);
      this.renderOuterFrameSide((float) Math.PI, poseStack, buffer, packedLight, packedOverlay);
      this.renderInnerFrameSide(0.0F, 0.0F, poseStack, buffer, packedLight, packedOverlay);
      this.renderInnerFrameSide((float) (Math.PI / 2), 0.0F, poseStack, buffer, packedLight, packedOverlay);
      this.renderInnerFrameSide((float) (-Math.PI / 2), 0.0F, poseStack, buffer, packedLight, packedOverlay);
      this.renderInnerFrameSide((float) Math.PI, 0.0F, poseStack, buffer, packedLight, packedOverlay);
      this.renderInnerFrameSide(0.0F, (float) (Math.PI / 2), poseStack, buffer, packedLight, packedOverlay);
      this.renderInnerFrameSide(0.0F, (float) (-Math.PI / 2), poseStack, buffer, packedLight, packedOverlay);
   }

   public void renderGlass(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
      this.renderGlassSide(0.0F, 0.0F, poseStack, buffer, packedLight, packedOverlay);
      this.renderGlassSide((float) (Math.PI / 2), 0.0F, poseStack, buffer, packedLight, packedOverlay);
      this.renderGlassSide((float) (-Math.PI / 2), 0.0F, poseStack, buffer, packedLight, packedOverlay);
      this.renderGlassSide((float) Math.PI, 0.0F, poseStack, buffer, packedLight, packedOverlay);
      this.renderGlassSide(0.0F, (float) (Math.PI / 2), poseStack, buffer, packedLight, packedOverlay);
      this.renderGlassSide(0.0F, (float) (-Math.PI / 2), poseStack, buffer, packedLight, packedOverlay);
   }

   private void renderGlassSide(float yRot, float xRot, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
      this.glass.xRot = xRot;
      this.glass.yRot = yRot;
      this.glass.render(poseStack, buffer, packedLight, packedOverlay);
   }

   private void renderInnerFrameSide(float yRot, float xRot, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
      this.innerFrameTop.yRot = yRot;
      this.innerFrameBottom.yRot = yRot;
      this.innerFrameLeft.yRot = yRot;
      this.innerFrameRight.yRot = yRot;
      this.innerFrameTop.xRot = xRot;
      this.innerFrameBottom.xRot = xRot;
      this.innerFrameLeft.xRot = xRot;
      this.innerFrameRight.xRot = xRot;
      this.innerFrameTop.render(poseStack, buffer, packedLight, packedOverlay);
      this.innerFrameBottom.render(poseStack, buffer, packedLight, packedOverlay);
      this.innerFrameLeft.render(poseStack, buffer, packedLight, packedOverlay);
      this.innerFrameRight.render(poseStack, buffer, packedLight, packedOverlay);
   }

   private void renderOuterFrameSide(float rotation, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
      this.outerFrameTop.yRot = rotation;
      this.outerFrameBottom.yRot = rotation;
      this.outerFrameLeft.xRot = -rotation;
      this.outerFrameRight.xRot = rotation;
      this.outerFrameTop.render(poseStack, buffer, packedLight, packedOverlay);
      this.outerFrameBottom.render(poseStack, buffer, packedLight, packedOverlay);
      this.outerFrameLeft.render(poseStack, buffer, packedLight, packedOverlay);
      this.outerFrameRight.render(poseStack, buffer, packedLight, packedOverlay);
   }
}
