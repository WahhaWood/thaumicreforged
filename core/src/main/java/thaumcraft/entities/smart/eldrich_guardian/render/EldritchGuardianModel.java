package thaumcraft.entities.smart.eldrich_guardian.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
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
import thaumcraft.entities.smart.eldrich_guardian.EntityEldritchGuardian;
import thaumcraft.entities.smart.eldrich_warden.EntityEldritchWarden;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class EldritchGuardianModel<T extends LivingEntity> extends HierarchicalModel<T> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      ResourceLocation.fromNamespaceAndPath("thaumcraft", "eldritch_guardian"), "main"
   );
   private final ModelPart root;
   private final ModelPart beltR;
   private final ModelPart mbelt;
   private final ModelPart mbeltL;
   private final ModelPart mbeltR;
   private final ModelPart beltL;
   private final ModelPart cloak1;
   private final ModelPart cloak2;
   private final ModelPart cloak3;
   private final ModelPart chestplate;
   private final ModelPart backplate;
   private final ModelPart hoodEye;
   private final ModelPart hood1;
   private final ModelPart hood2;
   private final ModelPart hood3;
   private final ModelPart hood4;
   private final ModelPart shoulderplateTopR;
   private final ModelPart shoulderplateR1;
   private final ModelPart shoulderplateR2;
   private final ModelPart shoulderplateR3;
   private final ModelPart shoulderR;
   private final ModelPart armL1;
   private final ModelPart armL2;
   private final ModelPart armL3;
   private final ModelPart armR1;
   private final ModelPart armR2;
   private final ModelPart armR3;
   private final ModelPart shoulderL;
   private final ModelPart shoulderplateLtop;
   private final ModelPart shoulderplateL1;
   private final ModelPart shoulderplateL2;
   private final ModelPart shoulderplateL3;
   private final ModelPart legpanelR4;
   private final ModelPart legpanelR5;
   private final ModelPart legpanelR6;
   private final ModelPart backpanelR1;
   private final ModelPart backpanelR2;
   private final ModelPart backpanelR3;
   private final ModelPart backpanelL3;
   private final ModelPart legpanelL4;
   private final ModelPart legpanelL5;
   private final ModelPart legpanelL6;
   private final ModelPart backpanelL1;
   private final ModelPart backpanelL2;
   private final ModelPart sidepanelR1;
   private final ModelPart sidepanelL1;
   private final ModelPart sidepanelR2;
   private final ModelPart sidepanelR3;
   private final ModelPart sidepanelR4;
   private final ModelPart sidepanelL2;
   private final ModelPart sidepanelL3;
   private final ModelPart sidepanelL4;
   private final ModelPart legpanelC1;
   private final ModelPart legpanelC2;
   private final ModelPart legpanelC3;
   private float renderAlpha = 1.0F;

   @OnlyIn(Dist.CLIENT)
   public EldritchGuardianModel(ModelPart root) {
      this.root = root;
      this.beltR = root.getChild("beltR");
      this.mbelt = root.getChild("mbelt");
      this.mbeltL = root.getChild("mbeltL");
      this.mbeltR = root.getChild("mbeltR");
      this.beltL = root.getChild("beltL");
      this.chestplate = root.getChild("chestplate");
      this.backplate = root.getChild("backplate");
      this.hoodEye = root.getChild("hoodEye");
      this.hood1 = root.getChild("hood1");
      this.hood2 = this.hood1.getChild("hood2");
      this.hood3 = this.hood1.getChild("hood3");
      this.hood4 = this.hood1.getChild("hood4");
      this.shoulderplateTopR = root.getChild("shoulderplateTopR");
      this.shoulderplateR1 = root.getChild("shoulderplateR1");
      this.shoulderplateR2 = root.getChild("shoulderplateR2");
      this.shoulderplateR3 = root.getChild("shoulderplateR3");
      this.shoulderR = root.getChild("shoulderR");
      this.armL1 = root.getChild("armL1");
      this.armL2 = this.armL1.getChild("armL2");
      this.armL3 = this.armL1.getChild("armL3");
      this.armR1 = root.getChild("armR1");
      this.armR2 = this.armR1.getChild("armR2");
      this.armR3 = this.armR1.getChild("armR3");
      this.shoulderL = root.getChild("shoulderL");
      this.shoulderplateLtop = root.getChild("shoulderplateLtop");
      this.shoulderplateL1 = root.getChild("shoulderplateL1");
      this.shoulderplateL2 = root.getChild("shoulderplateL2");
      this.shoulderplateL3 = root.getChild("shoulderplateL3");
      this.legpanelR4 = root.getChild("legpanelR4");
      this.legpanelR5 = root.getChild("legpanelR5");
      this.legpanelR6 = root.getChild("legpanelR6");
      this.backpanelR1 = root.getChild("backpanelR1");
      this.backpanelR2 = root.getChild("backpanelR2");
      this.backpanelR3 = root.getChild("backpanelR3");
      this.backpanelL3 = root.getChild("backpanelL3");
      this.legpanelL4 = root.getChild("legpanelL4");
      this.legpanelL5 = root.getChild("legpanelL5");
      this.legpanelL6 = root.getChild("legpanelL6");
      this.backpanelL1 = root.getChild("backpanelL1");
      this.backpanelL2 = root.getChild("backpanelL2");
      this.sidepanelR1 = root.getChild("sidepanelR1");
      this.sidepanelL1 = root.getChild("sidepanelL1");
      this.sidepanelR2 = root.getChild("sidepanelR2");
      this.sidepanelR3 = this.sidepanelR2.getChild("sidepanelR3");
      this.sidepanelR4 = this.sidepanelR3.getChild("sidepanelR4");
      this.sidepanelL2 = root.getChild("sidepanelL2");
      this.sidepanelL3 = this.sidepanelL2.getChild("sidepanelL3");
      this.sidepanelL4 = this.sidepanelL3.getChild("sidepanelL4");
      this.legpanelC1 = root.getChild("legpanelC1");
      this.legpanelC2 = this.legpanelC1.getChild("legpanelC2");
      this.legpanelC3 = this.legpanelC2.getChild("legpanelC3");
      this.cloak1 = root.getChild("cloak1");
      this.cloak2 = this.cloak1.getChild("cloak2");
      this.cloak3 = this.cloak2.getChild("cloak3");
   }

   @OnlyIn(Dist.CLIENT)
   public ModelPart hoodEye() {
      return this.hoodEye;
   }

   public void setRenderAlpha(float renderAlpha) {
      this.renderAlpha = Mth.clamp(renderAlpha, 0.0F, 1.0F);
   }

   @OnlyIn(Dist.CLIENT)
   public ModelPart root() {
      return this.root;
   }

   public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.hood1.yRot = netHeadYaw * (float) (Math.PI / 180.0);
      this.hood1.xRot = headPitch * (float) (Math.PI / 180.0);
      this.hoodEye.xRot = this.hood1.xRot;
      this.hoodEye.yRot = this.hood1.yRot;
      float armR = 0.0F;
      float armL = 0.0F;
      if (entity instanceof EntityEldritchGuardian g) {
         armR = g.armLiftR;
         armL = g.armLiftL;
      } else if (entity instanceof EntityEldritchWarden w) {
         armR = w.armLiftR;
         armL = w.armLiftL;
      }

      this.armL1.xRot = -1.0F - armL + Mth.sin((ageInTicks + 20.0F) / 10.0F) * 0.08F;
      this.armR1.xRot = -1.0F - armR + Mth.sin(ageInTicks / 10.0F) * 0.08F;
      this.legpanelC1.xRot = -0.15F + Mth.sin(ageInTicks / 8.0F) * 0.12F;
      this.legpanelC2.xRot = Mth.sin((ageInTicks - 5.0F) / 8.0F) * 0.13F;
      this.legpanelC3.xRot = Mth.sin((ageInTicks - 10.0F) / 8.0F) * 0.14F;
      this.cloak1.xRot = 0.2F + Mth.sin(ageInTicks / 7.0F) * 0.08F;
      this.cloak2.xRot = Mth.sin((ageInTicks - 5.0F) / 7.0F) * 0.1F;
      this.cloak3.xRot = Mth.sin((ageInTicks - 10.0F) / 7.0F) * 0.12F;
      this.sidepanelL2.zRot = -0.2F + Mth.sin((ageInTicks + 10.0F) / 8.0F) * 0.12F;
      this.sidepanelL3.zRot = Mth.sin((ageInTicks + 5.0F) / 8.0F) * 0.13F;
      this.sidepanelL4.zRot = Mth.sin(ageInTicks / 8.0F) * 0.14F;
      this.sidepanelR2.zRot = 0.2F + Mth.sin((ageInTicks - 5.0F) / 8.0F) * 0.12F;
      this.sidepanelR3.zRot = Mth.sin((ageInTicks - 10.0F) / 8.0F) * 0.13F;
      this.sidepanelR4.zRot = Mth.sin((ageInTicks - 15.0F) / 8.0F) * 0.14F;
   }

   @OnlyIn(Dist.CLIENT)
   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
      int alpha = (int)((color >>> 24 & 0xFF) * this.renderAlpha);
      this.root.render(poseStack, buffer, packedLight, packedOverlay, color & 16777215 | alpha << 24);
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "beltR", CubeListBuilder.create().texOffs(76, 44).mirror().addBox(-5.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.offset(0.0F, -6.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "mbelt", CubeListBuilder.create().texOffs(56, 55).mirror().addBox(-4.0F, 8.0F, -3.0F, 8.0F, 4.0F, 1.0F), PartPose.offset(0.0F, -6.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "mbeltL", CubeListBuilder.create().texOffs(76, 44).mirror().addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.offset(0.0F, -6.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "mbeltR", CubeListBuilder.create().texOffs(76, 44).mirror().addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.offset(0.0F, -6.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "beltL", CubeListBuilder.create().texOffs(76, 44).mirror().addBox(4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.offset(0.0F, -6.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "chestplate", CubeListBuilder.create().texOffs(56, 45).mirror().addBox(-4.0F, 1.0F, -4.0F, 8.0F, 7.0F, 2.0F), PartPose.offset(0.0F, -6.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "hoodEye", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, -6.0F, 0.0F)
      );
      PartDefinition hood1 = root.addOrReplaceChild(
         "hood1", CubeListBuilder.create().texOffs(40, 12).mirror().addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, -6.0F, 0.0F)
      );
      hood1.addOrReplaceChild(
         "hood2",
         CubeListBuilder.create().texOffs(36, 28).mirror().addBox(-3.5F, -8.7F, 2.0F, 7.0F, 7.0F, 3.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2268928F, 0.0F, 0.0F)
      );
      hood1.addOrReplaceChild(
         "hood3",
         CubeListBuilder.create().texOffs(22, 19).mirror().addBox(-3.0F, -9.0F, 2.5F, 6.0F, 6.0F, 3.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3490659F, 0.0F, 0.0F)
      );
      hood1.addOrReplaceChild(
         "hood4",
         CubeListBuilder.create().texOffs(40, 4).mirror().addBox(-2.5F, -9.7F, 3.5F, 5.0F, 5.0F, 3.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5759587F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "backplate", CubeListBuilder.create().texOffs(36, 45).mirror().addBox(-4.0F, 1.0F, 2.0F, 8.0F, 11.0F, 2.0F), PartPose.offset(0.0F, -6.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "shoulderplateTopR",
         CubeListBuilder.create().texOffs(110, 37).mirror().addBox(-5.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F),
         PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.3665191F, 0.3141593F, 0.4363323F)
      );
      root.addOrReplaceChild(
         "shoulderplateR1",
         CubeListBuilder.create().texOffs(110, 45).mirror().addBox(3.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F),
         PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.3665191F, -0.3141593F, -0.4363323F)
      );
      root.addOrReplaceChild(
         "shoulderplateR2",
         CubeListBuilder.create().texOffs(94, 45).mirror().addBox(-3.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F),
         PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.3665191F, 0.3141593F, 0.4363323F)
      );
      root.addOrReplaceChild(
         "shoulderplateR3",
         CubeListBuilder.create().texOffs(94, 45).mirror().addBox(-2.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F),
         PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.3665191F, 0.3141593F, 0.4363323F)
      );
      root.addOrReplaceChild(
         "shoulderR",
         CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F),
         PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.3665191F, 0.122173F, 0.0349066F)
      );
      PartDefinition armL1 = root.addOrReplaceChild(
         "armL1",
         CubeListBuilder.create().texOffs(72, 8).mirror().addBox(-1.0F, 2.5F, -1.5F, 4.0F, 10.0F, 5.0F),
         PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.9599311F, -0.1047198F, -0.1919862F)
      );
      armL1.addOrReplaceChild("armL2", CubeListBuilder.create().texOffs(76, 28).mirror().addBox(-1.0F, 9.5F, 3.5F, 4.0F, 3.0F, 3.0F), PartPose.ZERO);
      armL1.addOrReplaceChild("armL3", CubeListBuilder.create().texOffs(76, 23).mirror().addBox(-1.0F, 6.5F, 3.5F, 4.0F, 3.0F, 2.0F), PartPose.ZERO);
      PartDefinition armR1 = root.addOrReplaceChild(
         "armR1",
         CubeListBuilder.create().texOffs(72, 8).mirror().addBox(-3.0F, 2.5F, -1.5F, 4.0F, 10.0F, 5.0F),
         PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.9599311F, 0.1047198F, 0.1919862F)
      );
      armR1.addOrReplaceChild("armR2", CubeListBuilder.create().texOffs(76, 28).mirror().addBox(-3.0F, 9.5F, 3.5F, 4.0F, 3.0F, 3.0F), PartPose.ZERO);
      armR1.addOrReplaceChild("armR3", CubeListBuilder.create().texOffs(76, 23).mirror().addBox(-3.0F, 6.5F, 3.5F, 4.0F, 3.0F, 2.0F), PartPose.ZERO);
      root.addOrReplaceChild(
         "shoulderL",
         CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F),
         PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.3665191F, -0.122173F, -0.0349066F)
      );
      root.addOrReplaceChild(
         "shoulderplateLtop",
         CubeListBuilder.create().texOffs(110, 37).addBox(3.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F),
         PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.3665191F, -0.3141593F, -0.4363323F)
      );
      root.addOrReplaceChild(
         "shoulderplateL1",
         CubeListBuilder.create().texOffs(110, 45).mirror().addBox(-4.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F),
         PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.3665191F, 0.3141593F, 0.4363323F)
      );
      root.addOrReplaceChild(
         "shoulderplateL2",
         CubeListBuilder.create().texOffs(94, 45).addBox(2.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F),
         PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.3665191F, -0.3141593F, -0.4363323F)
      );
      root.addOrReplaceChild(
         "shoulderplateL3",
         CubeListBuilder.create().texOffs(94, 45).addBox(1.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F),
         PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.3665191F, -0.3141593F, -0.4363323F)
      );
      root.addOrReplaceChild(
         "legpanelR4",
         CubeListBuilder.create().texOffs(0, 43).mirror().addBox(-3.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F),
         PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "legpanelR5",
         CubeListBuilder.create().texOffs(0, 47).mirror().addBox(-3.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F),
         PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "legpanelR6",
         CubeListBuilder.create().texOffs(6, 43).mirror().addBox(-3.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F),
         PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "backpanelR1",
         CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-3.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F),
         PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "backpanelR2",
         CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-3.0F, 2.5F, 1.5F, 5.0F, 3.0F, 1.0F),
         PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "backpanelR3",
         CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-3.0F, 4.5F, 0.5F, 5.0F, 3.0F, 1.0F),
         PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "backpanelL3",
         CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 4.5F, 0.5F, 5.0F, 3.0F, 1.0F),
         PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "legpanelL4",
         CubeListBuilder.create().texOffs(0, 43).addBox(1.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F),
         PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "legpanelL5",
         CubeListBuilder.create().texOffs(0, 47).addBox(1.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F),
         PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "legpanelL6",
         CubeListBuilder.create().texOffs(6, 43).addBox(1.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F),
         PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, -0.4363323F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "backpanelL1",
         CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F),
         PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "backpanelL2",
         CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 2.5F, 1.5F, 5.0F, 3.0F, 1.0F),
         PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, 0.4363323F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "sidepanelL1",
         CubeListBuilder.create().texOffs(0, 22).mirror().addBox(1.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F),
         PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, 0.0F, 0.0F, -0.4363323F)
      );
      root.addOrReplaceChild(
         "sidepanelR1",
         CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F),
         PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, 0.0F, 0.0F, 0.4363323F)
      );
      PartDefinition sidepanelR2 = root.addOrReplaceChild(
         "sidepanelR2",
         CubeListBuilder.create().texOffs(0, 54).mirror().addBox(0.0F, 0.0F, -0.5F, 1.0F, 5.0F, 5.0F),
         PartPose.offsetAndRotation(-4.5F, 9.5F, -2.0F, 0.0F, 0.0F, 0.122173F)
      );
      PartDefinition sidepanelR3 = sidepanelR2.addOrReplaceChild(
         "sidepanelR3",
         CubeListBuilder.create().texOffs(0, 35).mirror().addBox(0.0F, 0.0F, -0.5F, 1.0F, 3.0F, 5.0F),
         PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, 0.0F, 0.296706F)
      );
      sidepanelR3.addOrReplaceChild(
         "sidepanelR4",
         CubeListBuilder.create().texOffs(24, 35).mirror().addBox(0.0F, 0.0F, -0.5F, 1.0F, 3.0F, 5.0F),
         PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0F, (float) (Math.PI / 6))
      );
      PartDefinition sidepanelL2 = root.addOrReplaceChild(
         "sidepanelL2",
         CubeListBuilder.create().texOffs(0, 54).mirror().addBox(0.0F, 0.0F, -0.5F, 1.0F, 5.0F, 5.0F),
         PartPose.offsetAndRotation(4.5F, 9.5F, -2.0F, 0.0F, 0.0F, -0.122173F)
      );
      PartDefinition sidepanelL3 = sidepanelL2.addOrReplaceChild(
         "sidepanelL3",
         CubeListBuilder.create().texOffs(0, 35).mirror().addBox(0.0F, 0.0F, -0.5F, 1.0F, 3.0F, 5.0F),
         PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, 0.0F, -0.296706F)
      );
      sidepanelL3.addOrReplaceChild(
         "sidepanelL4",
         CubeListBuilder.create().texOffs(24, 35).mirror().addBox(0.0F, 0.0F, -0.5F, 1.0F, 3.0F, 5.0F),
         PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0F, (float) (-Math.PI / 6))
      );
      PartDefinition legpanelC1 = root.addOrReplaceChild(
         "legpanelC1", CubeListBuilder.create().texOffs(16, 45).mirror().addBox(-3.0F, 0.0F, -0.5F, 6.0F, 8.0F, 1.0F), PartPose.offset(0.0F, 5.5F, -3.0F)
      );
      PartDefinition legpanelC2 = legpanelC1.addOrReplaceChild(
         "legpanelC2", CubeListBuilder.create().texOffs(16, 54).mirror().addBox(-3.0F, 0.0F, -0.5F, 6.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F)
      );
      legpanelC2.addOrReplaceChild(
         "legpanelC3", CubeListBuilder.create().texOffs(32, 59).mirror().addBox(-3.0F, 0.0F, -0.5F, 6.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 4.0F, 0.0F)
      );
      PartDefinition cloak1 = root.addOrReplaceChild(
         "cloak1", CubeListBuilder.create().texOffs(106, 0).mirror().addBox(0.0F, 0.0F, -0.5F, 10.0F, 18.0F, 1.0F), PartPose.offset(-5.0F, -6.0F, 4.0F)
      );
      PartDefinition cloak2 = cloak1.addOrReplaceChild(
         "cloak2", CubeListBuilder.create().texOffs(106, 19).mirror().addBox(0.0F, 0.0F, -0.5F, 10.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 18.0F, 0.0F)
      );
      cloak2.addOrReplaceChild(
         "cloak3", CubeListBuilder.create().texOffs(106, 24).mirror().addBox(0.0F, 0.0F, -0.5F, 10.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 4.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 128, 64);
   }
}
