package thaumcraft.block.eldritch.lock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelCube {
   private final ModelPart root;

   public ModelCube() {
      this.root = createLayer64x32().bakeRoot();
   }

   public ModelCube(int vOff) {
      this.root = createLayer64x64(vOff).bakeRoot();
   }

   public void render(PoseStack poseStack, VertexConsumer vc, int packedLight, int packedOverlay) {
      this.root.render(poseStack, vc, packedLight, packedOverlay);
   }

   private static LayerDefinition createLayer64x32() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.offset(8.0F, 8.0F, 8.0F));
      return LayerDefinition.create(mesh, 64, 32);
   }

   private static LayerDefinition createLayer64x64(int vOff) {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, vOff).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.ZERO);
      return LayerDefinition.create(mesh, 64, 64);
   }
}
