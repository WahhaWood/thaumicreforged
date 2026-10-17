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
import thaumcraft.entities.smart.taint.spore.EntityTaintSpore;

public class ModelTaintSpore extends EntityModel<EntityTaintSpore> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "taint_spore"), "main");
   private final ModelPart cube;

   public ModelTaintSpore(ModelPart root) {
      this.cube = root.getChild("cube");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "cube",
         CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 2.0F, -6.0F, 12.0F, 12.0F, 12.0F).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 16.0F, 16.0F),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public void setupAnim(EntityTaintSpore entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      float n7 = entity.hurtTime > 0 ? 0.04F : 0.02F;
      this.cube.xRot = n7 * Mth.sin(ageInTicks * 0.05F);
      this.cube.zRot = n7 * Mth.sin(ageInTicks * 0.1F);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
      this.cube.render(poseStack, buffer, packedLight, packedOverlay, color);
   }
}
