package thaumcraft.entities.smart.edlritch_crab;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ModelEldritchCrab extends EntityModel<EntityEldritchCrab> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "eldritch_crab"), "main");
   private final ModelPart root;
   private final ModelPart tailHelm;
   private final ModelPart tailBare;
   private final ModelPart rFLeg1;
   private final ModelPart rClaw1;
   private final ModelPart head1;
   private final ModelPart rClaw0;
   private final ModelPart rClaw2;
   private final ModelPart lClaw2;
   private final ModelPart lClaw1;
   private final ModelPart rArm;
   private final ModelPart torso;
   private final ModelPart rRLeg1;
   private final ModelPart head0;
   private final ModelPart lRLeg1;
   private final ModelPart lFLeg1;
   private final ModelPart rRLeg0;
   private final ModelPart rFLeg0;
   private final ModelPart lFLeg0;
   private final ModelPart lRLeg0;
   private final ModelPart lClaw0;
   private final ModelPart lArm;

   public ModelEldritchCrab(ModelPart root) {
      this.root = root;
      this.tailHelm = root.getChild("tailHelm");
      this.tailBare = root.getChild("tailBare");
      this.rFLeg1 = root.getChild("rFLeg1");
      this.rClaw1 = root.getChild("rClaw1");
      this.head1 = root.getChild("head1");
      this.rClaw0 = root.getChild("rClaw0");
      this.rClaw2 = root.getChild("rClaw2");
      this.lClaw2 = root.getChild("lClaw2");
      this.lClaw1 = root.getChild("lClaw1");
      this.rArm = root.getChild("rArm");
      this.torso = root.getChild("torso");
      this.rRLeg1 = root.getChild("rRLeg1");
      this.head0 = root.getChild("head0");
      this.lRLeg1 = root.getChild("lRLeg1");
      this.lFLeg1 = root.getChild("lFLeg1");
      this.rRLeg0 = root.getChild("rRLeg0");
      this.rFLeg0 = root.getChild("rFLeg0");
      this.lFLeg0 = root.getChild("lFLeg0");
      this.lRLeg0 = root.getChild("lRLeg0");
      this.lClaw0 = root.getChild("lClaw0");
      this.lArm = root.getChild("lArm");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      CubeDeformation def = CubeDeformation.NONE;
      root.addOrReplaceChild(
         "tailHelm",
         CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -4.5F, -0.4F, 9.0F, 9.0F, 9.0F, def),
         PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.1047198F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "tailBare",
         CubeListBuilder.create().texOffs(64, 0).addBox(-4.0F, -4.0F, -0.4F, 8.0F, 8.0F, 8.0F, def),
         PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.1047198F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "rClaw1", CubeListBuilder.create().texOffs(0, 47).addBox(-2.0F, -1.0F, -5.066667F, 4.0F, 3.0F, 5.0F, def), PartPose.offset(-6.0F, 15.5F, -10.0F)
      );
      root.addOrReplaceChild(
         "head1", CubeListBuilder.create().texOffs(0, 38).addBox(-2.0F, -1.5F, -9.066667F, 4.0F, 4.0F, 1.0F, def), PartPose.offset(0.0F, 18.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "rClaw0", CubeListBuilder.create().texOffs(0, 55).addBox(-2.0F, -2.5F, -3.066667F, 4.0F, 5.0F, 3.0F, def), PartPose.offset(-6.0F, 17.0F, -7.0F)
      );
      root.addOrReplaceChild(
         "rClaw2",
         CubeListBuilder.create().texOffs(14, 54).addBox(-1.5F, -1.0F, -4.066667F, 3.0F, 2.0F, 5.0F, def),
         PartPose.offsetAndRotation(-6.0F, 18.5F, -10.0F, 0.3141593F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "rArm",
         CubeListBuilder.create().texOffs(44, 4).addBox(-1.0F, -1.0F, -5.066667F, 2.0F, 2.0F, 6.0F, def),
         PartPose.offsetAndRotation(-3.0F, 17.0F, -4.0F, 0.0F, 0.7504916F, 0.0F)
      );
      root.addOrReplaceChild(
         "lClaw2",
         CubeListBuilder.create().texOffs(14, 54).addBox(-1.5F, -1.0F, -4.066667F, 3.0F, 2.0F, 5.0F, def),
         PartPose.offsetAndRotation(6.0F, 18.5F, -10.0F, 0.3141593F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "lClaw1",
         CubeListBuilder.create().texOffs(0, 47).mirror().addBox(-2.0F, -1.0F, -5.066667F, 4.0F, 3.0F, 5.0F, def).mirror(false),
         PartPose.offset(6.0F, 15.5F, -10.0F)
      );
      root.addOrReplaceChild(
         "lClaw0",
         CubeListBuilder.create().texOffs(0, 55).mirror().addBox(-2.0F, -2.5F, -3.066667F, 4.0F, 5.0F, 3.0F, def).mirror(false),
         PartPose.offset(6.0F, 17.0F, -7.0F)
      );
      root.addOrReplaceChild(
         "lArm",
         CubeListBuilder.create().texOffs(44, 4).addBox(-1.0F, -1.0F, -4.066667F, 2.0F, 2.0F, 6.0F, def),
         PartPose.offsetAndRotation(4.0F, 17.0F, -5.0F, 0.0F, -0.7504916F, 0.0F)
      );
      root.addOrReplaceChild(
         "torso",
         CubeListBuilder.create().texOffs(0, 18).addBox(-3.5F, -3.5F, -6.066667F, 7.0F, 7.0F, 6.0F, def),
         PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.0523599F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "head0", CubeListBuilder.create().texOffs(0, 31).addBox(-2.5F, -2.0F, -8.066667F, 5.0F, 5.0F, 2.0F, def), PartPose.offset(0.0F, 18.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "rRLeg1", CubeListBuilder.create().texOffs(36, 4).addBox(-4.5F, 1.0F, -0.9F, 2.0F, 5.0F, 2.0F, def), PartPose.offset(-4.0F, 20.0F, -1.5F)
      );
      root.addOrReplaceChild(
         "rFLeg1", CubeListBuilder.create().texOffs(36, 4).addBox(-5.0F, 1.0F, -1.066667F, 2.0F, 5.0F, 2.0F, def), PartPose.offset(-4.0F, 20.0F, -3.5F)
      );
      root.addOrReplaceChild(
         "lRLeg1", CubeListBuilder.create().texOffs(36, 4).addBox(2.5F, 1.0F, -0.9F, 2.0F, 5.0F, 2.0F, def), PartPose.offset(4.0F, 20.0F, -1.5F)
      );
      root.addOrReplaceChild(
         "lFLeg1", CubeListBuilder.create().texOffs(36, 4).addBox(3.0F, 1.0F, -1.066667F, 2.0F, 5.0F, 2.0F, def), PartPose.offset(4.0F, 20.0F, -3.5F)
      );
      root.addOrReplaceChild(
         "rRLeg0", CubeListBuilder.create().texOffs(36, 0).addBox(-4.5F, -1.0F, -0.9F, 6.0F, 2.0F, 2.0F, def), PartPose.offset(-4.0F, 20.0F, -1.5F)
      );
      root.addOrReplaceChild(
         "rFLeg0", CubeListBuilder.create().texOffs(36, 0).addBox(-5.0F, -1.0F, -1.066667F, 6.0F, 2.0F, 2.0F, def), PartPose.offset(-4.0F, 20.0F, -3.5F)
      );
      root.addOrReplaceChild(
         "lFLeg0", CubeListBuilder.create().texOffs(36, 0).addBox(-1.0F, -1.0F, -1.066667F, 6.0F, 2.0F, 2.0F, def), PartPose.offset(4.0F, 20.0F, -3.5F)
      );
      root.addOrReplaceChild(
         "lRLeg0", CubeListBuilder.create().texOffs(36, 0).addBox(-1.5F, -1.0F, -0.9F, 6.0F, 2.0F, 2.0F, def), PartPose.offset(4.0F, 20.0F, -1.5F)
      );
      return LayerDefinition.create(mesh, 128, 64);
   }

   public void setupAnim(EntityEldritchCrab entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.tailHelm.visible = entity.hasHelm();
      this.tailBare.visible = !entity.hasHelm();
      setRot(this.rRLeg1, 0.0F, 0.2094395F, 0.4363323F);
      setRot(this.rFLeg1, 0.0F, -0.2094395F, 0.4363323F);
      setRot(this.lRLeg1, 0.0F, -0.2094395F, -0.4363323F);
      setRot(this.lFLeg1, 0.0F, 0.2094395F, -0.4363323F);
      setRot(this.rRLeg0, 0.0F, 0.2094395F, 0.4363323F);
      setRot(this.rFLeg0, 0.0F, -0.2094395F, 0.4363323F);
      setRot(this.lFLeg0, 0.0F, 0.2094395F, -0.4363323F);
      setRot(this.lRLeg0, 0.0F, -0.2094395F, -0.4363323F);
      float n7 = -(Mth.cos(limbSwing * 0.6662F * 2.0F + 0.0F) * 0.4F) * limbSwingAmount;
      float n8 = -(Mth.cos(limbSwing * 0.6662F * 2.0F + (float) Math.PI) * 0.4F) * limbSwingAmount;
      this.rRLeg1.yRot += n7;
      this.rRLeg0.yRot += n7;
      this.lRLeg1.yRot += -n7;
      this.lRLeg0.yRot += -n7;
      this.rFLeg1.yRot += n8;
      this.rFLeg0.yRot += n8;
      this.lFLeg1.yRot += -n8;
      this.lFLeg0.yRot += -n8;
      this.rRLeg1.zRot += n7;
      this.rRLeg0.zRot += n7;
      this.lRLeg1.zRot += -n7;
      this.lRLeg0.zRot += -n7;
      this.rFLeg1.zRot += n8;
      this.rFLeg0.zRot += n8;
      this.lFLeg1.zRot += -n8;
      this.lFLeg0.zRot += -n8;
      float n9 = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.125F;
      this.tailHelm.yRot = n9;
      this.tailBare.yRot = n9;
      float n10 = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount * 0.125F;
      this.tailHelm.zRot = n10;
      this.tailBare.zRot = n10;
      this.rClaw2.xRot = 0.3141593F - Mth.sin(ageInTicks / 4.0F) * 0.25F;
      this.lClaw2.xRot = 0.3141593F + Mth.sin(ageInTicks / 4.1F) * 0.25F;
      this.rClaw1.xRot = Mth.sin(ageInTicks / 4.0F) * 0.125F;
      this.lClaw1.xRot = -Mth.sin(ageInTicks / 4.1F) * 0.125F;
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
      this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
   }

   private static void setRot(ModelPart p, float x, float y, float z) {
      p.xRot = x;
      p.yRot = y;
      p.zRot = z;
   }
}
