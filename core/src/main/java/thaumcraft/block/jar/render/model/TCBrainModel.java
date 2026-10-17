package thaumcraft.block.jar.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public final class TCBrainModel {
   public static final ResourceLocation TEX_BRAIN = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/block/model/brain2.png");
   private final ModelPart shape1;
   private final ModelPart shape2;
   private final ModelPart shape3;

   @OnlyIn(Dist.CLIENT)
   private TCBrainModel(ModelPart root) {
      this.shape1 = root.getChild("shape1");
      this.shape2 = root.getChild("shape2");
      this.shape3 = root.getChild("shape3");
   }

   public static TCBrainModel create() {
      return new TCBrainModel(createLayer().bakeRoot());
   }

   public static LayerDefinition createLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "shape1", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 12.0F, 10.0F, 16.0F), PartPose.offset(-6.0F, 8.0F, -8.0F)
      );
      root.addOrReplaceChild("shape2", CubeListBuilder.create().texOffs(64, 0).addBox(0.0F, 0.0F, 0.0F, 8.0F, 3.0F, 7.0F), PartPose.offset(-4.0F, 18.0F, 0.0F));
      root.addOrReplaceChild(
         "shape3",
         CubeListBuilder.create().texOffs(0, 32).addBox(0.0F, 0.0F, 0.0F, 2.0F, 6.0F, 2.0F),
         PartPose.offsetAndRotation(-1.0F, 18.0F, -2.0F, 0.4089647F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 128, 64);
   }

   @OnlyIn(Dist.CLIENT)
   public void render(PoseStack poseStack, VertexConsumer vc, int light, int overlay) {
      this.shape1.render(poseStack, vc, light, overlay);
      this.shape2.render(poseStack, vc, light, overlay);
      this.shape3.render(poseStack, vc, light, overlay);
   }
}
