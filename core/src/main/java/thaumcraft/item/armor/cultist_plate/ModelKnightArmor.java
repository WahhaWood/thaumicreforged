package thaumcraft.item.armor.cultist_plate;

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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelKnightArmor<T extends LivingEntity> extends HumanoidModel<T> {
   public static final ModelLayerLocation OUTER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "knight_armor_outer"), "main");
   public static final ModelLayerLocation INNER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "knight_armor_inner"), "main");
   private final ModelPart frontcloth1;
   private final ModelPart frontcloth2;
   private final ModelPart cloak1;
   private final ModelPart cloak2;
   private final ModelPart cloak3;
   private final ModelPart chestplate;
   private final ModelPart tabbard;
   private final ModelPart backplate;
   private final ModelPart cloakAtL;
   private final ModelPart cloakAtR;

   public ModelKnightArmor(ModelPart root) {
      super(root);
      ModelPart body = root.getChild("body");
      this.chestplate = body.getChild("chestplate");
      this.tabbard = body.getChild("tabbard");
      this.backplate = body.getChild("backplate");
      this.frontcloth1 = body.getChild("frontcloth1");
      this.frontcloth2 = body.getChild("frontcloth2");
      this.cloak1 = body.getChild("cloak1");
      this.cloak2 = body.getChild("cloak2");
      this.cloak3 = body.getChild("cloak3");
      this.cloakAtL = body.getChild("cloakAtL");
      this.cloakAtR = body.getChild("cloakAtR");
   }

   public static LayerDefinition createLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
      root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
      PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
      PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
      PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
      PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
      PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
      head.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(41, 8).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 9.0F, 9.0F), PartPose.ZERO);
      body.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8.0F, 4.0F, 1.0F), PartPose.ZERO);
      body.addOrReplaceChild("mbeltL", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO);
      body.addOrReplaceChild("mbeltR", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO);
      body.addOrReplaceChild("tabbard", CubeListBuilder.create().texOffs(114, 52).addBox(-3.0F, 1.2F, -3.5F, 6.0F, 10.0F, 1.0F), PartPose.ZERO);
      body.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8.0F, 11.0F, 2.0F), PartPose.ZERO);
      body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -3.0F, 8.0F, 7.0F, 1.0F), PartPose.ZERO);
      body.addOrReplaceChild(
         "cloakAtL", CubeListBuilder.create().texOffs(0, 43).addBox(2.5F, 1.0F, 2.0F, 2.0F, 1.0F, 3.0F), PartPose.rotation(0.1396263F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "cloakAtR", CubeListBuilder.create().texOffs(0, 43).addBox(-4.5F, 1.0F, 2.0F, 2.0F, 1.0F, 3.0F), PartPose.rotation(0.1396263F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "cloak1",
         CubeListBuilder.create().texOffs(0, 47).addBox(0.0F, 0.0F, 0.0F, 9.0F, 12.0F, 1.0F),
         PartPose.offsetAndRotation(-4.5F, 1.3F, 4.2F, 0.1396263F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "cloak2",
         CubeListBuilder.create().texOffs(0, 59).addBox(0.0F, 11.7F, -2.0F, 9.0F, 4.0F, 1.0F),
         PartPose.offsetAndRotation(-4.5F, 1.3F, 4.2F, 0.3069452F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "cloak3",
         CubeListBuilder.create().texOffs(0, 59).addBox(0.0F, 15.2F, -4.2F, 9.0F, 4.0F, 1.0F),
         PartPose.offsetAndRotation(-4.5F, 1.3F, 4.2F, 0.4465716F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "frontcloth1",
         CubeListBuilder.create().texOffs(120, 39).addBox(0.0F, 0.0F, 0.0F, 6.0F, 8.0F, 1.0F),
         PartPose.offsetAndRotation(-3.0F, 11.0F, -3.5F, -0.1047198F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "frontcloth2",
         CubeListBuilder.create().texOffs(100, 37).addBox(0.0F, 7.5F, 1.8F, 6.0F, 3.0F, 1.0F),
         PartPose.offsetAndRotation(-3.0F, 11.0F, -3.5F, -0.3316126F, 0.0F, 0.0F)
      );
      rightArm.addOrReplaceChild(
         "shoulderR", CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F).mirror(false), PartPose.ZERO
      );
      rightArm.addOrReplaceChild(
         "shoulderR0",
         CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-4.3F, -1.5F, -3.0F, 3.0F, 5.0F, 6.0F).mirror(false),
         PartPose.rotation(0.0F, 0.0F, (float) (Math.PI / 4))
      );
      rightArm.addOrReplaceChild(
         "shoulderR1",
         CubeListBuilder.create().texOffs(0, 19).addBox(-3.3F, 3.5F, -2.5F, 1.0F, 1.0F, 5.0F),
         PartPose.rotation(0.0F, 0.0F, (float) (Math.PI / 4))
      );
      rightArm.addOrReplaceChild(
         "shoulderR2",
         CubeListBuilder.create().texOffs(0, 11).mirror().addBox(-2.3F, 3.5F, -3.0F, 1.0F, 2.0F, 6.0F).mirror(false),
         PartPose.rotation(0.0F, 0.0F, (float) (Math.PI / 4))
      );
      rightArm.addOrReplaceChild(
         "gauntletR", CubeListBuilder.create().texOffs(100, 26).mirror().addBox(-3.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F).mirror(false), PartPose.ZERO
      );
      rightArm.addOrReplaceChild(
         "gauntletstrapR1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F).mirror(false), PartPose.ZERO
      );
      rightArm.addOrReplaceChild(
         "gauntletstrapR2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F).mirror(false), PartPose.ZERO
      );
      leftArm.addOrReplaceChild("shoulderL", CubeListBuilder.create().texOffs(56, 35).addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F), PartPose.ZERO);
      leftArm.addOrReplaceChild(
         "shoulderL0",
         CubeListBuilder.create().texOffs(0, 0).addBox(1.3F, -1.5F, -3.0F, 3.0F, 5.0F, 6.0F),
         PartPose.rotation(0.0F, 0.0F, (float) (-Math.PI / 4))
      );
      leftArm.addOrReplaceChild(
         "shoulderL1",
         CubeListBuilder.create().texOffs(0, 19).mirror().addBox(2.3F, 3.5F, -2.5F, 1.0F, 1.0F, 5.0F).mirror(false),
         PartPose.rotation(0.0F, 0.0F, (float) (-Math.PI / 4))
      );
      leftArm.addOrReplaceChild(
         "shoulderL2",
         CubeListBuilder.create().texOffs(0, 11).addBox(1.3F, 3.5F, -3.0F, 1.0F, 2.0F, 6.0F),
         PartPose.rotation(0.0F, 0.0F, (float) (-Math.PI / 4))
      );
      leftArm.addOrReplaceChild("gauntletL", CubeListBuilder.create().texOffs(114, 26).addBox(1.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F), PartPose.ZERO);
      leftArm.addOrReplaceChild(
         "gauntletstrapL1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F).mirror(false), PartPose.ZERO
      );
      leftArm.addOrReplaceChild(
         "gauntletstrapL2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F).mirror(false), PartPose.ZERO
      );
      rightLeg.addOrReplaceChild(
         "sidepanelR0", CubeListBuilder.create().texOffs(96, 14).addBox(-3.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, 0.1396263F)
      );
      rightLeg.addOrReplaceChild(
         "sidepanelR1",
         CubeListBuilder.create().texOffs(96, 7).mirror().addBox(0.0F, 2.5F, -2.5F, 2.0F, 2.0F, 5.0F).mirror(false),
         PartPose.rotation(0.0F, 0.0F, 0.1396263F)
      );
      rightLeg.addOrReplaceChild(
         "sidepanelR2",
         CubeListBuilder.create().texOffs(114, 5).mirror().addBox(-2.0F, 2.5F, -2.5F, 2.0F, 3.0F, 5.0F).mirror(false),
         PartPose.rotation(0.0F, 0.0F, 0.1396263F)
      );
      rightLeg.addOrReplaceChild(
         "sidepanelR3", CubeListBuilder.create().texOffs(116, 13).addBox(-3.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, 0.1396263F)
      );
      leftLeg.addOrReplaceChild(
         "sidepanelL0", CubeListBuilder.create().texOffs(96, 14).addBox(-2.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, -0.1396263F)
      );
      leftLeg.addOrReplaceChild(
         "sidepanelL1", CubeListBuilder.create().texOffs(96, 7).addBox(-2.0F, 2.5F, -2.5F, 2.0F, 2.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, -0.1396263F)
      );
      leftLeg.addOrReplaceChild(
         "sidepanelL2", CubeListBuilder.create().texOffs(114, 5).addBox(0.0F, 2.5F, -2.5F, 2.0F, 3.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, -0.1396263F)
      );
      leftLeg.addOrReplaceChild(
         "sidepanelL3", CubeListBuilder.create().texOffs(116, 13).addBox(2.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, -0.1396263F)
      );
      return LayerDefinition.create(mesh, 128, 64);
   }

   public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
      float a = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
      float b = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
      float min = Math.min(a, b);
      this.frontcloth1.xRot = min - 0.1047198F;
      this.frontcloth2.xRot = min - 0.3316126F;
      this.cloak1.xRot = -min / 2.0F + 0.1396263F;
      this.cloak2.xRot = -min / 2.0F + 0.3069452F;
      this.cloak3.xRot = -min / 2.0F + 0.4465716F;
   }

   public void setOuterAddonsVisible(boolean visible) {
      this.chestplate.visible = visible;
      this.tabbard.visible = visible;
      this.backplate.visible = visible;
      this.frontcloth1.visible = visible;
      this.frontcloth2.visible = visible;
      this.cloak1.visible = visible;
      this.cloak2.visible = visible;
      this.cloak3.visible = visible;
      this.cloakAtL.visible = visible;
      this.cloakAtR.visible = visible;
   }

   public static void setSlotVisible(HumanoidModel<?> model, EquipmentSlot slot) {
      model.setAllVisible(false);
      model.head.visible = slot == EquipmentSlot.HEAD;
      model.hat.visible = slot == EquipmentSlot.HEAD;
      model.body.visible = slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS;
      model.rightArm.visible = slot == EquipmentSlot.CHEST;
      model.leftArm.visible = slot == EquipmentSlot.CHEST;
      model.rightLeg.visible = slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
      model.leftLeg.visible = slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
      if (model instanceof ModelKnightArmor<?> mk) {
         mk.setOuterAddonsVisible(slot == EquipmentSlot.CHEST);
      }
   }
}
