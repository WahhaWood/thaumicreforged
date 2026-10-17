package thaumcraft.entities.smart.taint.spore.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import thaumcraft.entities.smart.taint.spore.EntityTaintSporeSwarmer;

public class ModelTaintSporeSwarmer extends EntityModel<EntityTaintSporeSwarmer> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      ResourceLocation.fromNamespaceAndPath("thaumcraft", "taint_spore_swarmer"), "main"
   );
   public final ModelPart cube;
   public final ModelPart cube2;
   private Entity entity;

   public ModelTaintSporeSwarmer(ModelPart root) {
      this.cube = root.getChild("cube");
      this.cube2 = root.getChild("cube2");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
      root.addOrReplaceChild(
         "cube2", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.offset(0.0F, 16.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public void setupAnim(EntityTaintSporeSwarmer entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      float n7 = 0.02F;
      if (entity.hurtTime > 0) {
         n7 = 0.04F;
      }

      this.cube.xRot = n7 * Mth.sin(ageInTicks * 0.05F);
      this.cube.zRot = n7 * Mth.sin(ageInTicks * 0.1F);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
      poseStack.pushPose();
      poseStack.pushPose();
      EntityTaintSporeSwarmer e = (EntityTaintSporeSwarmer)this.entity;
      float displaySize = e.displaySize;
      float n7 = -0.07F;
      float n8 = 0.025F * Mth.sin(e.tickCount * 0.075F);
      poseStack.translate(0.0F, 1.6F, 0.0F);
      poseStack.scale(n7 * displaySize - n8, n7 * displaySize + n8, n7 * displaySize - n8);
      poseStack.translate(0.0F, -(n7 * displaySize + n8) / 2.0F, 0.0F);
      this.cube.render(poseStack, buffer, 15728880, packedOverlay, color);
      poseStack.popPose();
      poseStack.pushPose();
      this.cube2.render(poseStack, buffer, packedLight, packedOverlay, color);
      poseStack.popPose();
      poseStack.popPose();
   }

   public void setEntity(EntityTaintSporeSwarmer e) {
      this.entity = e;
   }
}
