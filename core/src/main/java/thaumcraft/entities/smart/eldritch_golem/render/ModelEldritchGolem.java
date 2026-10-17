package thaumcraft.entities.smart.eldritch_golem.render;

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
import thaumcraft.entities.smart.eldritch_golem.EntityEldritchGolem;

public class ModelEldritchGolem extends EntityModel<EntityEldritchGolem> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "eldritch_golem"), "main");
   private final ModelPart root;
   private final ModelPart Frontcloth1;
   private final ModelPart CollarL;
   private final ModelPart Cloak1;
   private final ModelPart CloakCL;
   private final ModelPart CloakCR;
   private final ModelPart Cloak3;
   private final ModelPart Cloak2;
   private final ModelPart Head;
   private final ModelPart Head2;
   private final ModelPart Frontcloth0;
   private final ModelPart CollarB;
   private final ModelPart Torso;
   private final ModelPart CollarR;
   private final ModelPart CollarF;
   private final ModelPart CollarBlack;
   private final ModelPart ArmL;
   private final ModelPart ArmR;
   private final ModelPart BackpanelR1;
   private final ModelPart WaistR1;
   private final ModelPart WaistR2;
   private final ModelPart WaistR3;
   private final ModelPart LegR;
   private final ModelPart WaistL1;
   private final ModelPart WaistL2;
   private final ModelPart WaistL3;
   private final ModelPart Frontcloth2;
   private final ModelPart BackpanelL1;
   private final ModelPart LegL;

   public ModelEldritchGolem(ModelPart root) {
      this.root = root;
      this.Frontcloth1 = root.getChild("Frontcloth1");
      this.CollarL = root.getChild("CollarL");
      this.Cloak1 = root.getChild("Cloak1");
      this.CloakCL = root.getChild("CloakCL");
      this.CloakCR = root.getChild("CloakCR");
      this.Cloak3 = root.getChild("Cloak3");
      this.Cloak2 = root.getChild("Cloak2");
      this.Head = root.getChild("Head");
      this.Head2 = root.getChild("Head2");
      this.Frontcloth0 = root.getChild("Frontcloth0");
      this.CollarB = root.getChild("CollarB");
      this.Torso = root.getChild("Torso");
      this.CollarR = root.getChild("CollarR");
      this.CollarF = root.getChild("CollarF");
      this.CollarBlack = root.getChild("CollarBlack");
      this.ArmL = root.getChild("ArmL");
      this.ArmR = root.getChild("ArmR");
      this.BackpanelR1 = root.getChild("BackpanelR1");
      this.WaistR1 = root.getChild("WaistR1");
      this.WaistR2 = root.getChild("WaistR2");
      this.WaistR3 = root.getChild("WaistR3");
      this.LegR = root.getChild("LegR");
      this.WaistL1 = root.getChild("WaistL1");
      this.WaistL2 = root.getChild("WaistL2");
      this.WaistL3 = root.getChild("WaistL3");
      this.Frontcloth2 = root.getChild("Frontcloth2");
      this.BackpanelL1 = root.getChild("BackpanelL1");
      this.LegL = root.getChild("LegL");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "Cloak1",
         CubeListBuilder.create().texOffs(0, 47).addBox(-5.0F, 1.5F, 4.0F, 10.0F, 12.0F, 1.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1396263F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "Cloak3",
         CubeListBuilder.create().texOffs(0, 37).addBox(-5.0F, 17.5F, -0.8F, 10.0F, 4.0F, 1.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.4465716F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "Cloak2",
         CubeListBuilder.create().texOffs(0, 59).addBox(-5.0F, 13.5F, 1.7F, 10.0F, 4.0F, 1.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.3069452F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "CloakCL",
         CubeListBuilder.create().texOffs(0, 43).addBox(3.0F, 0.5F, 2.0F, 2.0F, 1.0F, 3.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1396263F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "CloakCR",
         CubeListBuilder.create().texOffs(0, 43).addBox(-5.0F, 0.5F, 2.0F, 2.0F, 1.0F, 3.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1396263F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "Head",
         CubeListBuilder.create().texOffs(47, 12).addBox(-3.5F, -6.0F, -2.5F, 7.0F, 7.0F, 5.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(0.0F, 4.5F, -3.8F, -0.1047198F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "Head2",
         CubeListBuilder.create().texOffs(26, 16).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(0.0F, 0.0F, -5.0F, -0.1047198F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "CollarL",
         CubeListBuilder.create().texOffs(75, 50).addBox(3.5F, -0.5F, -7.0F, 1.0F, 4.0F, 10.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.837758F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "CollarR",
         CubeListBuilder.create().texOffs(67, 50).addBox(-4.5F, -0.5F, -7.0F, 1.0F, 4.0F, 10.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.837758F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "CollarB",
         CubeListBuilder.create().texOffs(77, 59).addBox(-3.5F, -0.5F, 2.0F, 7.0F, 4.0F, 1.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.837758F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "CollarF",
         CubeListBuilder.create().texOffs(77, 59).addBox(-3.5F, -0.5F, -7.0F, 7.0F, 4.0F, 1.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.837758F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "CollarBlack",
         CubeListBuilder.create().texOffs(22, 0).addBox(-3.5F, 0.0F, -6.0F, 7.0F, 1.0F, 8.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.837758F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "Frontcloth0",
         CubeListBuilder.create().texOffs(114, 52).addBox(-3.0F, 3.2F, -3.5F, 6.0F, 10.0F, 1.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1745329F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "Frontcloth1",
         CubeListBuilder.create().texOffs(114, 39).addBox(-1.0F, 1.5F, -3.5F, 6.0F, 6.0F, 1.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, -0.1047198F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "Frontcloth2",
         CubeListBuilder.create().texOffs(114, 47).addBox(-1.0F, 8.5F, -1.5F, 6.0F, 3.0F, 1.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(-2.0F, 11.0F, 0.0F, -0.3316126F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "Torso",
         CubeListBuilder.create().texOffs(34, 45).addBox(-5.0F, 2.5F, -3.0F, 10.0F, 10.0F, 6.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1745329F, 0.0F, 0.0F)
      );
      PartDefinition armR = root.addOrReplaceChild(
         "ArmR",
         CubeListBuilder.create().texOffs(78, 32).addBox(-3.5F, 1.5F, -2.0F, 4.0F, 13.0F, 5.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(-5.0F, 3.0F, -2.0F, 0.0F, 0.0F, 0.1047198F)
      );
      armR.addOrReplaceChild(
         "ShoulderR1",
         CubeListBuilder.create().texOffs(0, 23).addBox(-3.3F, 4.0F, -2.5F, 1.0F, 2.0F, 6.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.186824F)
      );
      armR.addOrReplaceChild(
         "ShoulderR",
         CubeListBuilder.create().texOffs(0, 0).addBox(-4.3F, -1.0F, -3.0F, 4.0F, 5.0F, 7.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.186824F)
      );
      armR.addOrReplaceChild(
         "ShoulderR2",
         CubeListBuilder.create().texOffs(0, 12).addBox(-2.3F, 4.0F, -3.0F, 2.0F, 3.0F, 7.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.186824F)
      );
      armR.addOrReplaceChild(
         "ShoulderR0",
         CubeListBuilder.create().texOffs(56, 31).addBox(-4.5F, -1.5F, -2.5F, 5.0F, 6.0F, 6.0F, CubeDeformation.NONE),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition armL = root.addOrReplaceChild(
         "ArmL",
         CubeListBuilder.create().texOffs(78, 32).mirror().addBox(-0.5F, 1.5F, -2.0F, 4.0F, 13.0F, 5.0F, CubeDeformation.NONE).mirror(false),
         PartPose.offsetAndRotation(5.0F, 3.0F, -2.0F, 0.0F, 0.0F, -0.1047198F)
      );
      armL.addOrReplaceChild(
         "ShoulderL1",
         CubeListBuilder.create().texOffs(0, 23).mirror().addBox(2.3F, 4.0F, -2.5F, 1.0F, 2.0F, 6.0F, CubeDeformation.NONE).mirror(false),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.186824F)
      );
      armL.addOrReplaceChild(
         "ShoulderL0",
         CubeListBuilder.create().texOffs(56, 31).mirror().addBox(-0.5F, -1.5F, -2.5F, 5.0F, 6.0F, 6.0F, CubeDeformation.NONE).mirror(false),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      armL.addOrReplaceChild(
         "ShoulderL",
         CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.3F, -1.0F, -3.0F, 4.0F, 5.0F, 7.0F, CubeDeformation.NONE).mirror(false),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.186824F)
      );
      armL.addOrReplaceChild(
         "ShoulderL2",
         CubeListBuilder.create().texOffs(0, 12).mirror().addBox(0.3F, 4.0F, -3.0F, 2.0F, 3.0F, 7.0F, CubeDeformation.NONE).mirror(false),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.186824F)
      );
      root.addOrReplaceChild(
         "BackpanelR1",
         CubeListBuilder.create().texOffs(96, 7).addBox(0.0F, 2.5F, -2.5F, 2.0F, 2.0F, 5.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1396263F)
      );
      root.addOrReplaceChild(
         "WaistR1",
         CubeListBuilder.create().texOffs(96, 14).addBox(-3.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1396263F)
      );
      root.addOrReplaceChild(
         "WaistR2",
         CubeListBuilder.create().texOffs(116, 13).addBox(-3.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1396263F)
      );
      root.addOrReplaceChild(
         "WaistR3",
         CubeListBuilder.create().texOffs(114, 5).mirror().addBox(-2.0F, 2.5F, -2.5F, 2.0F, 3.0F, 5.0F, CubeDeformation.NONE).mirror(false),
         PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1396263F)
      );
      root.addOrReplaceChild(
         "LegR",
         CubeListBuilder.create().texOffs(79, 19).addBox(-2.5F, 2.5F, -2.0F, 4.0F, 9.0F, 4.0F, CubeDeformation.NONE),
         PartPose.offset(-2.0F, 12.5F, 0.0F)
      );
      root.addOrReplaceChild(
         "WaistL1",
         CubeListBuilder.create().texOffs(96, 14).mirror().addBox(-2.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F, CubeDeformation.NONE).mirror(false),
         PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.1396263F)
      );
      root.addOrReplaceChild(
         "WaistL2",
         CubeListBuilder.create().texOffs(116, 13).mirror().addBox(2.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F, CubeDeformation.NONE).mirror(false),
         PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.1396263F)
      );
      root.addOrReplaceChild(
         "WaistL3",
         CubeListBuilder.create().texOffs(114, 5).addBox(0.0F, 2.5F, -2.5F, 2.0F, 3.0F, 5.0F, CubeDeformation.NONE),
         PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.1396263F)
      );
      root.addOrReplaceChild(
         "BackpanelL1",
         CubeListBuilder.create().texOffs(96, 7).mirror().addBox(-2.0F, 2.5F, -2.5F, 2.0F, 2.0F, 5.0F, CubeDeformation.NONE).mirror(false),
         PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.1396263F)
      );
      root.addOrReplaceChild(
         "LegL",
         CubeListBuilder.create().texOffs(79, 19).mirror().addBox(-1.5F, 2.5F, -2.0F, 4.0F, 9.0F, 4.0F, CubeDeformation.NONE).mirror(false),
         PartPose.offset(2.0F, 12.5F, 0.0F)
      );
      return LayerDefinition.create(mesh, 128, 64);
   }

   public void prepareMobModel(EntityEldritchGolem entity, float limbSwing, float limbSwingAmount, float partialTick) {
      int attackTimer = entity.getAttackTimer();
      if (attackTimer > 0) {
         float v = this.doAbs(attackTimer - partialTick, 10.0F);
         this.ArmR.xRot = -2.0F + 1.5F * v;
         this.ArmL.xRot = -2.0F + 1.5F * v;
      } else {
         this.ArmR.xRot = Mth.cos(limbSwing * 0.4F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F;
         this.ArmL.xRot = Mth.cos(limbSwing * 0.4F) * 2.0F * limbSwingAmount * 0.5F;
      }
   }

   public void setupAnim(EntityEldritchGolem entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      boolean headless = entity.isHeadless();
      this.Head.visible = !headless;
      this.Head2.visible = headless;
      int spawnTimer = entity.getSpawnTimer();
      if (spawnTimer > 0) {
         this.Head.yRot = 0.0F;
         this.Head.xRot = spawnTimer * 0.5F * (float) (Math.PI / 180.0);
      } else {
         this.Head.yRot = netHeadYaw * 0.25F * (float) (Math.PI / 180.0);
         this.Head.xRot = headPitch * 0.5F * (float) (Math.PI / 180.0);
         this.Head2.yRot = netHeadYaw * (float) (Math.PI / 180.0);
         this.Head2.xRot = headPitch * (float) (Math.PI / 180.0);
      }

      this.LegR.xRot = Mth.cos(limbSwing * 0.4662F) * 1.4F * limbSwingAmount;
      this.LegL.xRot = Mth.cos(limbSwing * 0.4662F + (float) Math.PI) * 1.4F * limbSwingAmount;
      float a = Mth.cos(limbSwing * 0.44F) * 1.4F * limbSwingAmount;
      float b = Mth.cos(limbSwing * 0.44F + (float) Math.PI) * 1.4F * limbSwingAmount;
      float min = Math.min(a, b);
      this.Frontcloth1.xRot = min - 0.1047198F;
      this.Frontcloth2.xRot = min - 0.3316126F;
      this.Cloak1.xRot = -min / 3.0F + 0.1396263F;
      this.Cloak2.xRot = -min / 3.0F + 0.3069452F;
      this.Cloak3.xRot = -min / 3.0F + 0.4465716F;
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
      this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
   }

   private float doAbs(float v, float mod) {
      return (Math.abs(v % mod - mod * 0.5F) - mod * 0.25F) / (mod * 0.25F);
   }
}
