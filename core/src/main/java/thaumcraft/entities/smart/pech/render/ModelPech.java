package thaumcraft.entities.smart.pech.render;

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
import thaumcraft.entities.smart.pech.EntityPech;

public class ModelPech<T extends EntityPech> extends HierarchicalModel<T> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "pech"), "main");
   private final ModelPart root;
   private final ModelPart body;
   private final ModelPart rightLeg;
   private final ModelPart leftLeg;
   private final ModelPart head;
   private final ModelPart jowls;
   private final ModelPart lowerPack;
   private final ModelPart upperPack;
   public final ModelPart rightArm;
   private final ModelPart leftArm;

   public ModelPech(ModelPart root) {
      this.root = root;
      this.body = root.getChild("body");
      this.rightLeg = root.getChild("right_leg");
      this.leftLeg = root.getChild("left_leg");
      this.head = root.getChild("head");
      this.jowls = root.getChild("jowls");
      this.lowerPack = root.getChild("lower_pack");
      this.upperPack = root.getChild("upper_pack");
      this.rightArm = root.getChild("right_arm");
      this.leftArm = root.getChild("left_arm");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(34, 12).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 10.0F, 6.0F),
         PartPose.offsetAndRotation(0.0F, 9.0F, -3.0F, 0.3129957F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "right_leg",
         CubeListBuilder.create().texOffs(35, 1).mirror().addBox(-2.9F, 0.0F, 0.0F, 3.0F, 6.0F, 3.0F).mirror(false),
         PartPose.offset(0.0F, 18.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "left_leg", CubeListBuilder.create().texOffs(35, 1).addBox(-0.1F, 0.0F, 0.0F, 3.0F, 6.0F, 3.0F), PartPose.offset(0.0F, 18.0F, 0.0F)
      );
      root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(2, 11).addBox(-3.5F, -5.0F, -5.0F, 7.0F, 5.0F, 5.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
      root.addOrReplaceChild("jowls", CubeListBuilder.create().texOffs(1, 21).addBox(-4.0F, -1.0F, -6.0F, 8.0F, 3.0F, 5.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
      root.addOrReplaceChild(
         "lower_pack",
         CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, 0.0F, 10.0F, 5.0F, 5.0F),
         PartPose.offsetAndRotation(0.0F, 10.0F, 3.5F, 0.3013602F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "upper_pack",
         CubeListBuilder.create().texOffs(64, 1).addBox(-7.5F, -14.0F, 0.0F, 15.0F, 14.0F, 11.0F),
         PartPose.offsetAndRotation(0.0F, 10.0F, 3.0F, 0.4537856F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create().texOffs(52, 2).mirror().addBox(-2.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F).mirror(false),
         PartPose.offset(-3.0F, 10.0F, -1.0F)
      );
      root.addOrReplaceChild(
         "left_arm", CubeListBuilder.create().texOffs(52, 2).addBox(0.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F), PartPose.offset(3.0F, 10.0F, -1.0F)
      );
      return LayerDefinition.create(mesh, 128, 64);
   }

   public ModelPart root() {
      return this.root;
   }

   public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
      this.head.xRot = headPitch * (float) (Math.PI / 180.0);
      float mumble = entity.mumble;
      this.jowls.yRot = this.head.yRot;
      this.jowls.xRot = this.head.xRot
         + ((float) (Math.PI / 12) + Mth.cos(limbSwing * 0.6662F) * limbSwingAmount * 0.25F)
         + 0.34906587F * Mth.abs(Mth.sin(mumble));
      this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F;
      this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
      this.rightArm.zRot = 0.0F;
      this.leftArm.zRot = 0.0F;
      this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
      this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
      this.rightLeg.yRot = 0.0F;
      this.leftLeg.yRot = 0.0F;
      this.lowerPack.yRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.125F;
      this.lowerPack.zRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.125F;
      if (entity.isPassenger()) {
         this.rightArm.xRot -= (float) (Math.PI / 5);
         this.leftArm.xRot -= (float) (Math.PI / 5);
         this.rightLeg.xRot = (float) (-Math.PI * 2.0 / 5.0);
         this.leftLeg.xRot = (float) (-Math.PI * 2.0 / 5.0);
         this.rightLeg.yRot = (float) (Math.PI / 10);
         this.leftLeg.yRot = (float) (-Math.PI / 10);
      }

      float swing = entity.getAttackAnim(0.0F);
      if (swing > 0.0F) {
         float t = 1.0F - swing;
         float t2 = t * t;
         float w = Mth.sin((1.0F - t2 * t2) * (float) Math.PI);
         this.rightArm.xRot = this.rightArm.xRot - (w * 1.2F + Mth.sin(swing * (float) Math.PI) * -(this.head.xRot - 0.7F) * 0.75F);
         this.rightArm.yRot = this.rightArm.yRot + this.body.yRot * 2.0F;
         this.rightArm.zRot = Mth.sin(swing * (float) Math.PI) * -0.4F;
      }

      if (entity.isCrouching()) {
         this.rightArm.xRot += 0.4F;
         this.leftArm.xRot += 0.4F;
      }

      this.rightArm.zRot = this.rightArm.zRot + (Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F);
      this.leftArm.zRot = this.leftArm.zRot - (Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F);
      this.rightArm.xRot = this.rightArm.xRot + Mth.sin(ageInTicks * 0.067F) * 0.05F;
      this.leftArm.xRot = this.leftArm.xRot - Mth.sin(ageInTicks * 0.067F) * 0.05F;
   }

   public void renderToBuffer(PoseStack pose, VertexConsumer vb, int light, int overlay, int color) {
      this.root.render(pose, vb, light, overlay, color);
   }
}
