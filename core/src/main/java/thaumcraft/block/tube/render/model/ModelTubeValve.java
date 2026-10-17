package thaumcraft.block.tube.render.model;

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

public class ModelTubeValve {
   public static final int TEX_W = 64;
   public static final int TEX_H = 32;
   private final ModelPart valveRod;
   public static final ModelLayerLocation TUBE_VALVE = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "tube_valve"), "main");

   public ModelTubeValve(ModelPart root) {
      this.valveRod = root.getChild("valve_rod");
   }

   public static LayerDefinition createLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("valve_rod", CubeListBuilder.create().texOffs(0, 10).mirror().addBox(-1.0F, 2.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.ZERO);
      return LayerDefinition.create(mesh, 64, 32);
   }

   public void render(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float r, float g, float b, float a) {
      poseStack.pushPose();
      poseStack.scale(0.0625F, 0.0625F, 0.0625F);
      this.valveRod.render(poseStack, buffer, packedLight, packedOverlay);
      poseStack.popPose();
   }
}
