package thaumcraft.item.armor.cultist_robe;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
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
public class ModelRobe<T extends LivingEntity> extends HumanoidModel<T> {
   public static final ModelLayerLocation OUTER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "robe_outer"), "main");
   public static final ModelLayerLocation INNER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "robe_inner"), "main");
   private final ModelPart chestplate = this.body.getChild("chestplate");
   private final ModelPart chestthing = this.body.getChild("chestthing");
   private final ModelPart scroll = this.body.getChild("scroll");
   private final ModelPart backplate = this.body.getChild("backplate");
   private final ModelPart book = this.body.getChild("book");
   private final ModelPart clothchestL = this.body.getChild("clothchestL");
   private final ModelPart clothchestR = this.body.getChild("clothchestR");
   private final ModelPart frontclothR1 = this.body.getChild("frontclothR1");
   private final ModelPart frontclothR2 = this.body.getChild("frontclothR2");
   private final ModelPart frontclothL1 = this.body.getChild("frontclothL1");
   private final ModelPart frontclothL2 = this.body.getChild("frontclothL2");
   private final ModelPart clothBackR1 = this.body.getChild("clothBackR1");
   private final ModelPart clothBackR2 = this.body.getChild("clothBackR2");
   private final ModelPart clothBackR3 = this.body.getChild("clothBackR3");
   private final ModelPart clothBackL1 = this.body.getChild("clothBackL1");
   private final ModelPart clothBackL2 = this.body.getChild("clothBackL2");
   private final ModelPart clothBackL3 = this.body.getChild("clothBackL3");
   private final ModelPart focipouch = this.leftLeg.getChild("focipouch");

   public ModelRobe(ModelPart root) {
      super(root);
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
      head.addOrReplaceChild("hood1", CubeListBuilder.create().texOffs(16, 7).addBox(-4.5F, -9.0F, -4.6F, 9.0F, 9.0F, 9.0F), PartPose.ZERO);
      head.addOrReplaceChild(
         "hood2", CubeListBuilder.create().texOffs(52, 13).addBox(-4.0F, -9.7F, 2.0F, 8.0F, 9.0F, 3.0F), PartPose.rotation(-0.2268928F, 0.0F, 0.0F)
      );
      head.addOrReplaceChild(
         "hood3", CubeListBuilder.create().texOffs(52, 14).addBox(-3.5F, -10.0F, 3.5F, 7.0F, 8.0F, 3.0F), PartPose.rotation(-0.3490659F, 0.0F, 0.0F)
      );
      head.addOrReplaceChild(
         "hood4", CubeListBuilder.create().texOffs(53, 15).addBox(-3.0F, -10.7F, 3.5F, 6.0F, 7.0F, 3.0F), PartPose.rotation(-0.5759587F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(16, 55).addBox(-4.0F, 7.0F, -3.0F, 8.0F, 5.0F, 1.0F), PartPose.ZERO);
      body.addOrReplaceChild(
         "mbeltB", CubeListBuilder.create().texOffs(16, 55).addBox(-4.0F, 7.0F, -4.0F, 8.0F, 5.0F, 1.0F), PartPose.rotation(0.0F, 3.141593F, 0.0F)
      );
      body.addOrReplaceChild("mbeltL", CubeListBuilder.create().texOffs(16, 36).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO);
      body.addOrReplaceChild("mbeltR", CubeListBuilder.create().texOffs(16, 36).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO);
      body.addOrReplaceChild("chestthing", CubeListBuilder.create().texOffs(56, 50).addBox(-2.5F, 1.0F, -4.0F, 5.0F, 7.0F, 1.0F), PartPose.ZERO);
      body.addOrReplaceChild(
         "clothchestL", CubeListBuilder.create().texOffs(108, 38).mirror().addBox(2.1F, 0.5F, -3.5F, 2.0F, 8.0F, 1.0F).mirror(false), PartPose.ZERO
      );
      body.addOrReplaceChild("clothchestR", CubeListBuilder.create().texOffs(108, 38).addBox(-4.1F, 0.5F, -3.5F, 2.0F, 8.0F, 1.0F), PartPose.ZERO);
      body.addOrReplaceChild(
         "book", CubeListBuilder.create().texOffs(81, 16).addBox(1.0F, 0.0F, 4.0F, 5.0F, 7.0F, 2.0F), PartPose.rotation(0.0F, 0.0F, 0.7679449F)
      );
      body.addOrReplaceChild(
         "scroll", CubeListBuilder.create().texOffs(78, 25).addBox(-2.0F, 9.5F, 4.0F, 8.0F, 3.0F, 3.0F), PartPose.rotation(0.0F, 0.0F, 0.1919862F)
      );
      body.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 1.9F, 8.0F, 11.0F, 2.0F), PartPose.ZERO);
      body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(16, 25).addBox(-4.0F, 1.0F, -3.0F, 8.0F, 6.0F, 1.0F), PartPose.ZERO);
      body.addOrReplaceChild(
         "frontclothR1",
         CubeListBuilder.create().texOffs(108, 38).addBox(0.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F),
         PartPose.offsetAndRotation(-3.0F, 11.0F, -2.9F, -0.1047198F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "frontclothR2",
         CubeListBuilder.create().texOffs(108, 47).addBox(0.0F, 7.5F, 1.7F, 3.0F, 3.0F, 1.0F),
         PartPose.offsetAndRotation(-3.0F, 11.0F, -2.9F, -0.3316126F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "frontclothL1",
         CubeListBuilder.create().texOffs(108, 38).mirror().addBox(0.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F).mirror(false),
         PartPose.offsetAndRotation(0.0F, 11.0F, -2.9F, -0.1047198F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "frontclothL2",
         CubeListBuilder.create().texOffs(108, 47).mirror().addBox(0.0F, 7.5F, 1.7F, 3.0F, 3.0F, 1.0F).mirror(false),
         PartPose.offsetAndRotation(0.0F, 11.0F, -2.9F, -0.3316126F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "clothBackR1",
         CubeListBuilder.create().texOffs(118, 16).mirror().addBox(0.0F, 0.0F, 0.0F, 4.0F, 8.0F, 1.0F).mirror(false),
         PartPose.offsetAndRotation(-4.0F, 11.5F, 2.9F, 0.1047198F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "clothBackR2",
         CubeListBuilder.create().texOffs(123, 9).addBox(0.0F, 7.8F, -0.9F, 1.0F, 2.0F, 1.0F),
         PartPose.offsetAndRotation(-4.0F, 11.5F, 2.9F, 0.2268928F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "clothBackR3",
         CubeListBuilder.create().texOffs(120, 12).mirror().addBox(1.0F, 7.8F, -0.9F, 3.0F, 3.0F, 1.0F).mirror(false),
         PartPose.offsetAndRotation(-4.0F, 11.5F, 2.9F, 0.2268928F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "clothBackL1",
         CubeListBuilder.create().texOffs(118, 16).addBox(0.0F, 0.0F, 0.0F, 4.0F, 8.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, 11.5F, 2.9F, 0.1047198F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "clothBackL2",
         CubeListBuilder.create().texOffs(123, 9).mirror().addBox(3.0F, 7.8F, -0.9F, 1.0F, 2.0F, 1.0F).mirror(false),
         PartPose.offsetAndRotation(0.0F, 11.5F, 2.9F, 0.2268928F, 0.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "clothBackL3",
         CubeListBuilder.create().texOffs(120, 12).addBox(0.0F, 7.8F, -0.9F, 3.0F, 3.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, 11.5F, 2.9F, 0.2268928F, 0.0F, 0.0F)
      );
      rightArm.addOrReplaceChild(
         "shoulderR", CubeListBuilder.create().texOffs(16, 45).mirror().addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F).mirror(false), PartPose.ZERO
      );
      rightArm.addOrReplaceChild("rArm1", CubeListBuilder.create().texOffs(88, 39).addBox(-3.5F, 2.5F, -2.5F, 5.0F, 7.0F, 5.0F), PartPose.ZERO);
      rightArm.addOrReplaceChild("rArm2", CubeListBuilder.create().texOffs(76, 32).addBox(-3.0F, 5.5F, 2.5F, 4.0F, 4.0F, 2.0F), PartPose.ZERO);
      rightArm.addOrReplaceChild("rArm3", CubeListBuilder.create().texOffs(88, 32).addBox(-2.5F, 3.5F, 2.5F, 3.0F, 2.0F, 1.0F), PartPose.ZERO);
      rightArm.addOrReplaceChild(
         "shoulderplateTopR", CubeListBuilder.create().texOffs(56, 25).addBox(-5.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F), PartPose.rotation(0.0F, 0.0F, 0.4363323F)
      );
      rightArm.addOrReplaceChild(
         "shoulderplateR1", CubeListBuilder.create().texOffs(56, 33).addBox(-4.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F), PartPose.rotation(0.0F, 0.0F, 0.4363323F)
      );
      rightArm.addOrReplaceChild(
         "shoulderplateR2", CubeListBuilder.create().texOffs(40, 33).addBox(-3.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F), PartPose.rotation(0.0F, 0.0F, 0.4363323F)
      );
      rightArm.addOrReplaceChild(
         "shoulderplateR3", CubeListBuilder.create().texOffs(40, 33).addBox(-2.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F), PartPose.rotation(0.0F, 0.0F, 0.4363323F)
      );
      leftArm.addOrReplaceChild(
         "shoulderL", CubeListBuilder.create().texOffs(16, 45).mirror().addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F).mirror(false), PartPose.ZERO
      );
      leftArm.addOrReplaceChild(
         "lArm1", CubeListBuilder.create().texOffs(88, 39).mirror().addBox(-1.5F, 2.5F, -2.5F, 5.0F, 7.0F, 5.0F).mirror(false), PartPose.ZERO
      );
      leftArm.addOrReplaceChild("lArm2", CubeListBuilder.create().texOffs(76, 32).addBox(-1.0F, 5.5F, 2.5F, 4.0F, 4.0F, 2.0F), PartPose.ZERO);
      leftArm.addOrReplaceChild("lArm3", CubeListBuilder.create().texOffs(88, 32).addBox(-0.5F, 3.5F, 2.5F, 3.0F, 2.0F, 1.0F), PartPose.ZERO);
      leftArm.addOrReplaceChild(
         "shoulderplateTopL", CubeListBuilder.create().texOffs(56, 25).addBox(3.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F), PartPose.rotation(0.0F, 0.0F, -0.4363323F)
      );
      leftArm.addOrReplaceChild(
         "shoulderplateL1", CubeListBuilder.create().texOffs(56, 33).addBox(3.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F), PartPose.rotation(0.0F, 0.0F, -0.4363323F)
      );
      leftArm.addOrReplaceChild(
         "shoulderplateL2", CubeListBuilder.create().texOffs(40, 33).addBox(2.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F), PartPose.rotation(0.0F, 0.0F, -0.4363323F)
      );
      leftArm.addOrReplaceChild(
         "shoulderplateL3", CubeListBuilder.create().texOffs(40, 33).addBox(1.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F), PartPose.rotation(0.0F, 0.0F, -0.4363323F)
      );
      rightLeg.addOrReplaceChild(
         "legpanelR4", CubeListBuilder.create().texOffs(76, 38).addBox(-3.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F), PartPose.rotation(-0.4363323F, 0.0F, 0.0F)
      );
      rightLeg.addOrReplaceChild(
         "legpanelR5", CubeListBuilder.create().texOffs(76, 42).addBox(-3.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F), PartPose.rotation(-0.4363323F, 0.0F, 0.0F)
      );
      rightLeg.addOrReplaceChild(
         "legpanelR6", CubeListBuilder.create().texOffs(82, 38).addBox(-3.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F), PartPose.rotation(-0.4363323F, 0.0F, 0.0F)
      );
      rightLeg.addOrReplaceChild(
         "sidepanelR1",
         CubeListBuilder.create().texOffs(116, 25).mirror().addBox(-2.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F).mirror(false),
         PartPose.rotation(0.0F, 0.0F, 0.4363323F)
      );
      rightLeg.addOrReplaceChild(
         "sideclothR1", CubeListBuilder.create().texOffs(116, 42).addBox(-2.5F, 0.5F, -2.5F, 1.0F, 5.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, 0.122173F)
      );
      rightLeg.addOrReplaceChild(
         "sideclothR2", CubeListBuilder.create().texOffs(116, 34).addBox(-1.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, 0.296706F)
      );
      rightLeg.addOrReplaceChild(
         "sideclothR3",
         CubeListBuilder.create().texOffs(116, 1).addBox(0.4F, 8.4F, -2.5F, 1.0F, 3.0F, 5.0F),
         PartPose.rotation(0.0F, 0.0F, (float) (Math.PI / 6))
      );
      leftLeg.addOrReplaceChild(
         "legpanelL4",
         CubeListBuilder.create().texOffs(76, 38).mirror().addBox(1.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F).mirror(false),
         PartPose.rotation(-0.4363323F, 0.0F, 0.0F)
      );
      leftLeg.addOrReplaceChild(
         "legpanelL5",
         CubeListBuilder.create().texOffs(76, 42).mirror().addBox(1.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F).mirror(false),
         PartPose.rotation(-0.4363323F, 0.0F, 0.0F)
      );
      leftLeg.addOrReplaceChild(
         "legpanelL6",
         CubeListBuilder.create().texOffs(82, 38).mirror().addBox(1.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F).mirror(false),
         PartPose.rotation(-0.4363323F, 0.0F, 0.0F)
      );
      leftLeg.addOrReplaceChild(
         "sidepanelL1", CubeListBuilder.create().texOffs(116, 25).addBox(1.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, -0.4363323F)
      );
      leftLeg.addOrReplaceChild(
         "sideclothL1", CubeListBuilder.create().texOffs(116, 42).addBox(1.5F, 0.5F, -2.5F, 1.0F, 5.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, -0.122173F)
      );
      leftLeg.addOrReplaceChild(
         "sideclothL2", CubeListBuilder.create().texOffs(116, 34).addBox(0.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, -0.296706F)
      );
      leftLeg.addOrReplaceChild(
         "sideclothL3",
         CubeListBuilder.create().texOffs(116, 1).addBox(-1.4F, 8.4F, -2.5F, 1.0F, 3.0F, 5.0F),
         PartPose.rotation(0.0F, 0.0F, (float) (-Math.PI / 6))
      );
      leftLeg.addOrReplaceChild(
         "focipouch", CubeListBuilder.create().texOffs(100, 20).addBox(3.5F, 0.5F, -2.5F, 3.0F, 6.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, -0.122173F)
      );
      return LayerDefinition.create(mesh, 128, 64);
   }

   public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
      float a = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
      float b = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
      float min = Math.min(a, b);
      float v1 = min - 0.1047198F;
      this.frontclothR1.xRot = v1;
      this.frontclothL1.xRot = v1;
      float v2 = min - 0.3316126F;
      this.frontclothR2.xRot = v2;
      this.frontclothL2.xRot = v2;
      float v3 = -min + 0.1047198F;
      this.clothBackR1.xRot = v3;
      this.clothBackL1.xRot = v3;
      float v4 = -min + 0.2268928F;
      this.clothBackR2.xRot = v4;
      this.clothBackL2.xRot = v4;
      this.clothBackR3.xRot = v4;
      this.clothBackL3.xRot = v4;
   }

   public void setPlateSetVisible(boolean visible) {
      this.chestplate.visible = visible;
      this.chestthing.visible = visible;
      this.scroll.visible = visible;
      this.backplate.visible = visible;
      this.book.visible = visible;
      this.clothchestL.visible = visible;
      this.clothchestR.visible = visible;
   }

   public void setClothSetVisible(boolean visible) {
      this.frontclothR1.visible = visible;
      this.frontclothR2.visible = visible;
      this.frontclothL1.visible = visible;
      this.frontclothL2.visible = visible;
      this.clothBackR1.visible = visible;
      this.clothBackR2.visible = visible;
      this.clothBackR3.visible = visible;
      this.clothBackL1.visible = visible;
      this.clothBackL2.visible = visible;
      this.clothBackL3.visible = visible;
      this.focipouch.visible = visible;
   }

   public static void setSlotVisible(ModelRobe<?> model, EquipmentSlot slot) {
      model.setAllVisible(false);
      model.head.visible = slot == EquipmentSlot.HEAD;
      model.hat.visible = slot == EquipmentSlot.HEAD;
      model.body.visible = slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS;
      model.rightArm.visible = slot == EquipmentSlot.CHEST;
      model.leftArm.visible = slot == EquipmentSlot.CHEST;
      model.rightLeg.visible = slot == EquipmentSlot.LEGS;
      model.leftLeg.visible = slot == EquipmentSlot.LEGS;
      model.setPlateSetVisible(slot == EquipmentSlot.CHEST);
      model.setClothSetVisible(slot == EquipmentSlot.LEGS);
   }

   public static Model asGeneric(ModelRobe<?> model) {
      return model;
   }
}
