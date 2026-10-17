package thaumcraft.block.mana_pod;

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

public class ModelManaPod {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "mana_pod"), "main");
   public final ModelPart pod0;
   public final ModelPart pod1;
   public final ModelPart pod2;

   public ModelManaPod(ModelPart root) {
      this.pod0 = root.getChild("pod0");
      this.pod1 = root.getChild("pod1");
      this.pod2 = root.getChild("pod2");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("pod0", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F), PartPose.ZERO);
      root.addOrReplaceChild("pod1", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 7.0F, 6.0F), PartPose.ZERO);
      root.addOrReplaceChild("pod2", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 9.0F, 7.0F), PartPose.ZERO);
      return LayerDefinition.create(mesh, 32, 32);
   }

   private static int color(float r, float g, float b, float a) {
      int ia = Math.round(a * 255.0F) & 0xFF;
      int ir = Math.round(r * 255.0F) & 0xFF;
      int ig = Math.round(g * 255.0F) & 0xFF;
      int ib = Math.round(b * 255.0F) & 0xFF;
      return ia << 24 | ir << 16 | ig << 8 | ib;
   }

   public void renderPod0(PoseStack pose, VertexConsumer vc, int packedLight, int packedOverlay, float r, float g, float b, float a) {
      this.renderPod0(pose, vc, packedLight, packedOverlay, color(r, g, b, a));
   }

   public void renderPod0(PoseStack pose, VertexConsumer vc, int packedLight, int packedOverlay, int color) {
      this.pod0.render(pose, vc, packedLight, packedOverlay, color);
   }

   public void renderPod1(PoseStack pose, VertexConsumer vc, int packedLight, int packedOverlay, float r, float g, float b, float a) {
      this.renderPod1(pose, vc, packedLight, packedOverlay, color(r, g, b, a));
   }

   public void renderPod1(PoseStack pose, VertexConsumer vc, int packedLight, int packedOverlay, int color) {
      this.pod1.render(pose, vc, packedLight, packedOverlay, color);
   }

   public void renderPod2(PoseStack pose, VertexConsumer vc, int packedLight, int packedOverlay, float r, float g, float b, float a) {
      this.renderPod2(pose, vc, packedLight, packedOverlay, color(r, g, b, a));
   }

   public void renderPod2(PoseStack pose, VertexConsumer vc, int packedLight, int packedOverlay, int color) {
      this.pod2.render(pose, vc, packedLight, packedOverlay, color);
   }
}
