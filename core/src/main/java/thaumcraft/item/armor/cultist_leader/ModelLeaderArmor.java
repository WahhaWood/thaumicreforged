package thaumcraft.item.armor.cultist_leader;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;

public class ModelLeaderArmor extends HumanoidModel<LivingEntity> {
   public static final ModelLayerLocation LAYER_OUTER = new ModelLayerLocation(
      ResourceLocation.fromNamespaceAndPath("thaumcraft", "leader_armor_outer"), "main"
   );
   public static final ModelLayerLocation LAYER_INNER = new ModelLayerLocation(
      ResourceLocation.fromNamespaceAndPath("thaumcraft", "leader_armor_inner"), "main"
   );
   private final ModelPart legClothR = this.body.getChild("leg_cloth_r");
   private final ModelPart legClothL = this.body.getChild("leg_cloth_l");
   private final ModelPart cloak1 = this.body.getChild("cloak1");
   private final ModelPart cloak2 = this.body.getChild("cloak2");
   private final ModelPart cloak3 = this.body.getChild("cloak3");

   public ModelLeaderArmor(ModelPart root) {
      super(root);
   }

   public static LayerDefinition createBodyLayer(boolean outer) {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
      PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
      PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
      PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
      head.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(41, 8).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 9.0F, 9.0F), PartPose.ZERO);
      body.addOrReplaceChild("belt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO);
      body.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8.0F, 4.0F, 1.0F), PartPose.ZERO);
      body.addOrReplaceChild("mbelt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO);
      body.addOrReplaceChild("mbelt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO);
      body.addOrReplaceChild("belt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO);
      body.addOrReplaceChild(
         "cloak_tl",
         CubeListBuilder.create().texOffs(0, 43).addBox(2.5F, 1.0F, -1.0F, 2.0F, 1.0F, 3.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.1396263F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "cloak_tr",
         CubeListBuilder.create().texOffs(0, 43).addBox(-4.5F, 1.0F, -1.0F, 2.0F, 1.0F, 3.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.1396263F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "cloak1",
         CubeListBuilder.create().texOffs(0, 47).addBox(-4.5F, 2.0F, 1.0F, 9.0F, 12.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.1396263F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "cloak2",
         CubeListBuilder.create().texOffs(0, 59).addBox(-4.5F, 14.0F, -1.3F, 9.0F, 4.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.3069452F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "cloak3",
         CubeListBuilder.create().texOffs(0, 59).addBox(-4.5F, 17.0F, -3.7F, 9.0F, 4.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.4465716F, 0.0F, 0.0F)
      );
      CubeListBuilder outerOnly = outer ? CubeListBuilder.create() : CubeListBuilder.create();
      if (!outer) {
         outerOnly = CubeListBuilder.create();
      }

      if (outer) {
         body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -3.8F, 8.0F, 7.0F, 2.0F), PartPose.ZERO);
         body.addOrReplaceChild("chest_ornament", CubeListBuilder.create().texOffs(76, 53).addBox(-2.5F, 3.0F, -4.8F, 5.0F, 5.0F, 1.0F), PartPose.ZERO);
         body.addOrReplaceChild(
            "chest_cloth_l",
            CubeListBuilder.create().texOffs(20, 47).mirror().addBox(1.5F, 1.2F, -4.5F, 3.0F, 9.0F, 1.0F).mirror(false),
            PartPose.rotation(0.0663225F, 0.0F, 0.0F)
         );
         body.addOrReplaceChild(
            "chest_cloth_r", CubeListBuilder.create().texOffs(20, 47).addBox(-4.5F, 1.2F, -4.5F, 3.0F, 9.0F, 1.0F), PartPose.rotation(0.0663225F, 0.0F, 0.0F)
         );
         body.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8.0F, 11.0F, 2.0F), PartPose.ZERO);
         body.addOrReplaceChild(
            "collar_f",
            CubeListBuilder.create().texOffs(17, 31).addBox(-4.5F, -1.5F, -3.0F, 9.0F, 4.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2268928F, 0.0F, 0.0F)
         );
         body.addOrReplaceChild(
            "collar_b",
            CubeListBuilder.create().texOffs(17, 26).addBox(-4.5F, -1.5F, 7.0F, 9.0F, 4.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2268928F, 0.0F, 0.0F)
         );
         body.addOrReplaceChild(
            "collar_r",
            CubeListBuilder.create().texOffs(17, 11).addBox(-5.5F, -1.5F, -3.0F, 1.0F, 4.0F, 11.0F),
            PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2268928F, 0.0F, 0.0F)
         );
         body.addOrReplaceChild(
            "collar_l",
            CubeListBuilder.create().texOffs(17, 11).addBox(4.5F, -1.5F, -3.0F, 1.0F, 4.0F, 11.0F),
            PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2268928F, 0.0F, 0.0F)
         );
      }

      rightArm.addOrReplaceChild("gauntlet_r", CubeListBuilder.create().texOffs(100, 26).addBox(-3.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F), PartPose.ZERO);
      rightArm.addOrReplaceChild(
         "gauntlet_r2", CubeListBuilder.create().texOffs(102, 37).addBox(-5.0F, 3.5F, -2.0F, 1.0F, 5.0F, 4.0F), PartPose.rotation(0.0F, 0.0F, -0.1675516F)
      );
      rightArm.addOrReplaceChild("gauntletstrap_r1", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F), PartPose.ZERO);
      rightArm.addOrReplaceChild("gauntletstrap_r2", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F), PartPose.ZERO);
      rightArm.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(56, 35).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F), PartPose.ZERO);
      rightArm.addOrReplaceChild(
         "shoulder_r1",
         CubeListBuilder.create().texOffs(0, 0).addBox(-4.3F, -1.5F, -3.0F, 3.0F, 5.0F, 6.0F),
         PartPose.rotation(0.0F, 0.0F, (float) (Math.PI / 4))
      );
      rightArm.addOrReplaceChild(
         "shoulder_r2",
         CubeListBuilder.create().texOffs(0, 19).addBox(-3.3F, 3.5F, -2.5F, 1.0F, 1.0F, 5.0F),
         PartPose.rotation(0.0F, 0.0F, (float) (Math.PI / 4))
      );
      rightArm.addOrReplaceChild(
         "shoulder_r3",
         CubeListBuilder.create().texOffs(0, 11).addBox(-2.3F, 3.5F, -3.0F, 1.0F, 2.0F, 6.0F),
         PartPose.rotation(0.0F, 0.0F, (float) (Math.PI / 4))
      );
      rightArm.addOrReplaceChild(
         "shoulder_r4",
         CubeListBuilder.create().texOffs(18, 4).addBox(-2.3F, -1.5F, -4.0F, 1.0F, 6.0F, 1.0F),
         PartPose.rotation(0.0F, 0.0F, (float) (Math.PI / 4))
      );
      rightArm.addOrReplaceChild(
         "shoulder_r5",
         CubeListBuilder.create().texOffs(18, 4).addBox(-2.3F, -1.5F, 3.0F, 1.0F, 6.0F, 1.0F),
         PartPose.rotation(0.0F, 0.0F, (float) (Math.PI / 4))
      );
      leftArm.addOrReplaceChild("gauntlet_l", CubeListBuilder.create().texOffs(114, 26).addBox(1.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F), PartPose.ZERO);
      leftArm.addOrReplaceChild(
         "gauntlet_l2", CubeListBuilder.create().texOffs(102, 37).addBox(4.0F, 3.5F, -2.0F, 1.0F, 5.0F, 4.0F), PartPose.rotation(0.0F, 0.0F, 0.1675516F)
      );
      leftArm.addOrReplaceChild(
         "gauntletstrap_l1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F).mirror(false), PartPose.ZERO
      );
      leftArm.addOrReplaceChild(
         "gauntletstrap_l2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F).mirror(false), PartPose.ZERO
      );
      leftArm.addOrReplaceChild(
         "shoulder_l", CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F).mirror(false), PartPose.ZERO
      );
      leftArm.addOrReplaceChild(
         "shoulder_l1",
         CubeListBuilder.create().texOffs(0, 0).mirror().addBox(1.3F, -1.5F, -3.0F, 3.0F, 5.0F, 6.0F).mirror(false),
         PartPose.rotation(0.0F, 0.0F, (float) (-Math.PI / 4))
      );
      leftArm.addOrReplaceChild(
         "shoulder_l2",
         CubeListBuilder.create().texOffs(0, 19).mirror().addBox(2.3F, 3.5F, -2.5F, 1.0F, 1.0F, 5.0F).mirror(false),
         PartPose.rotation(0.0F, 0.0F, (float) (-Math.PI / 4))
      );
      leftArm.addOrReplaceChild(
         "shoulder_l3",
         CubeListBuilder.create().texOffs(0, 11).addBox(1.3F, 3.5F, -3.0F, 1.0F, 2.0F, 6.0F),
         PartPose.rotation(0.0F, 0.0F, (float) (-Math.PI / 4))
      );
      leftArm.addOrReplaceChild(
         "shoulder_l4",
         CubeListBuilder.create().texOffs(18, 4).addBox(1.3F, -1.5F, -4.0F, 1.0F, 6.0F, 1.0F),
         PartPose.rotation(0.0F, 0.0F, (float) (-Math.PI / 4))
      );
      leftArm.addOrReplaceChild(
         "shoulder_l5",
         CubeListBuilder.create().texOffs(18, 4).addBox(1.3F, -1.5F, 3.0F, 1.0F, 6.0F, 1.0F),
         PartPose.rotation(0.0F, 0.0F, (float) (-Math.PI / 4))
      );
      body.addOrReplaceChild(
         "leg_cloth_r",
         CubeListBuilder.create().texOffs(20, 55).addBox(0.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F),
         PartPose.offsetAndRotation(-4.5F, 10.4F, -3.9F, -0.0349066F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "leg_cloth_l",
         CubeListBuilder.create().texOffs(20, 55).mirror().addBox(0.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F).mirror(false),
         PartPose.offsetAndRotation(1.5F, 10.4F, -3.9F, -0.0349066F, 0.0F, 0.0F)
      );
      rightLeg.addOrReplaceChild(
         "backpanel_r1", CubeListBuilder.create().texOffs(0, 25).addBox(-3.0F, -0.5F, 2.5F, 5.0F, 7.0F, 1.0F), PartPose.rotation(0.0698132F, 0.0F, 0.0F)
      );
      rightLeg.addOrReplaceChild(
         "backpanel_r2", CubeListBuilder.create().texOffs(96, 14).addBox(-3.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, 0.1396263F)
      );
      rightLeg.addOrReplaceChild(
         "backpanel_r3", CubeListBuilder.create().texOffs(116, 13).addBox(-3.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, 0.1396263F)
      );
      rightLeg.addOrReplaceChild(
         "backpanel_r4",
         CubeListBuilder.create().texOffs(0, 25).mirror().addBox(-3.0F, -0.5F, -3.5F, 5.0F, 7.0F, 1.0F).mirror(false),
         PartPose.rotation(-0.0349066F, 0.0F, 0.0F)
      );
      leftLeg.addOrReplaceChild(
         "backpanel_l1", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, -0.5F, 2.5F, 5.0F, 7.0F, 1.0F), PartPose.rotation(0.0698132F, 0.0F, 0.0F)
      );
      leftLeg.addOrReplaceChild(
         "backpanel_l2", CubeListBuilder.create().texOffs(96, 14).addBox(-2.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, -0.1396263F)
      );
      leftLeg.addOrReplaceChild(
         "backpanel_l3", CubeListBuilder.create().texOffs(116, 13).addBox(2.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, -0.1396263F)
      );
      leftLeg.addOrReplaceChild(
         "backpanel_l4", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, -0.5F, -3.5F, 5.0F, 7.0F, 1.0F), PartPose.rotation(-0.0349066F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 128, 64);
   }

   public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
      float n7 = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
      float n8 = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
      float min = Math.min(n7, n8);
      this.legClothR.xRot = n7 - 0.1047198F;
      this.legClothL.xRot = n8 - 0.1047198F;
      this.cloak1.xRot = -min / 2.0F + 0.1396263F;
      this.cloak2.xRot = -min / 2.0F + 0.3069452F;
      this.cloak3.xRot = -min / 2.0F + 0.4465716F;
      if (entity instanceof Zombie || entity instanceof AbstractSkeleton) {
         float sin = Mth.sin(this.attackTime * (float) Math.PI);
         float sin2 = Mth.sin((1.0F - (1.0F - this.attackTime) * (1.0F - this.attackTime)) * (float) Math.PI);
         this.rightArm.zRot = 0.0F;
         this.leftArm.zRot = 0.0F;
         this.rightArm.yRot = -(0.1F - sin * 0.6F);
         this.leftArm.yRot = 0.1F - sin * 0.6F;
         this.rightArm.xRot = (float) (-Math.PI / 2);
         this.leftArm.xRot = (float) (-Math.PI / 2);
         this.rightArm.xRot -= sin * 1.2F - sin2 * 0.4F;
         this.leftArm.xRot -= sin * 1.2F - sin2 * 0.4F;
         this.rightArm.zRot = this.rightArm.zRot + (Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F);
         this.leftArm.zRot = this.leftArm.zRot - (Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F);
         this.rightArm.xRot = this.rightArm.xRot + Mth.sin(ageInTicks * 0.067F) * 0.05F;
         this.leftArm.xRot = this.leftArm.xRot - Mth.sin(ageInTicks * 0.067F) * 0.05F;
      }
   }
}
