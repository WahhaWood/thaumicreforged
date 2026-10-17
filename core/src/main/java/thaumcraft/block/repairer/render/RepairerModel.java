package thaumcraft.block.repairer.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RepairerModel extends Model {
   public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "repairer"), "main");
   private final ModelPart solid;
   private final ModelPart glass;

   @OnlyIn(Dist.CLIENT)
   public RepairerModel(ModelPart root) {
      super(RenderType::entityTranslucent);
      this.solid = root.getChild("solid");
      this.glass = root.getChild("glass");
   }

   public static LayerDefinition createLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition solid = root.addOrReplaceChild("solid", CubeListBuilder.create(), PartPose.ZERO);
      solid.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 1.0F, 16.0F), PartPose.offset(0.0F, 23.0F, 0.0F));
      solid.addOrReplaceChild(
         "support1", CubeListBuilder.create().texOffs(0, 17).addBox(-8.0F, -14.0F, 6.0F, 2.0F, 14.0F, 2.0F), PartPose.offset(0.0F, 23.0F, 0.0F)
      );
      solid.addOrReplaceChild(
         "support2", CubeListBuilder.create().texOffs(0, 17).addBox(6.0F, -14.0F, 6.0F, 2.0F, 14.0F, 2.0F), PartPose.offset(0.0F, 23.0F, 0.0F)
      );
      solid.addOrReplaceChild(
         "support3", CubeListBuilder.create().texOffs(0, 17).addBox(-8.0F, -14.0F, -8.0F, 2.0F, 14.0F, 2.0F), PartPose.offset(0.0F, 23.0F, 0.0F)
      );
      solid.addOrReplaceChild(
         "support4", CubeListBuilder.create().texOffs(0, 17).addBox(6.0F, -14.0F, -8.0F, 2.0F, 14.0F, 2.0F), PartPose.offset(0.0F, 23.0F, 0.0F)
      );
      solid.addOrReplaceChild(
         "top1", CubeListBuilder.create().texOffs(11, 19).addBox(-8.0F, -15.0F, -8.0F, 16.0F, 1.0F, 2.0F), PartPose.offset(0.0F, 23.0F, 0.0F)
      );
      solid.addOrReplaceChild(
         "top2", CubeListBuilder.create().texOffs(11, 19).addBox(-8.0F, -15.0F, 6.0F, 16.0F, 1.0F, 2.0F), PartPose.offset(0.0F, 23.0F, 0.0F)
      );
      solid.addOrReplaceChild(
         "top3", CubeListBuilder.create().texOffs(11, 23).addBox(6.0F, -15.0F, -6.0F, 2.0F, 1.0F, 12.0F), PartPose.offset(0.0F, 23.0F, 0.0F)
      );
      solid.addOrReplaceChild(
         "top4", CubeListBuilder.create().texOffs(11, 23).addBox(-8.0F, -15.0F, -6.0F, 2.0F, 1.0F, 12.0F), PartPose.offset(0.0F, 23.0F, 0.0F)
      );
      solid.addOrReplaceChild(
         "opening1", CubeListBuilder.create().texOffs(48, 30).addBox(-2.5F, -9.0F, 7.0F, 5.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 23.0F, 0.0F)
      );
      solid.addOrReplaceChild(
         "opening2", CubeListBuilder.create().texOffs(48, 30).addBox(-2.5F, -6.0F, 7.0F, 5.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 23.0F, 0.0F)
      );
      solid.addOrReplaceChild(
         "opening3", CubeListBuilder.create().texOffs(48, 24).addBox(-2.0F, -8.5F, 7.0F, 1.0F, 3.0F, 1.0F), PartPose.offset(0.0F, 23.0F, 0.0F)
      );
      solid.addOrReplaceChild(
         "opening4", CubeListBuilder.create().texOffs(48, 24).addBox(1.0F, -8.5F, 7.0F, 1.0F, 3.0F, 1.0F), PartPose.offset(0.0F, 23.0F, 0.0F)
      );
      PartDefinition glass = root.addOrReplaceChild("glass", CubeListBuilder.create(), PartPose.ZERO);
      glass.addOrReplaceChild(
         "glass1", CubeListBuilder.create().texOffs(0, 37).addBox(-6.0F, -14.5F, -6.0F, 12.0F, 0.0F, 12.0F), PartPose.offset(0.0F, 23.0F, 0.0F)
      );
      glass.addOrReplaceChild(
         "glass2", CubeListBuilder.create().texOffs(1, 38).addBox(7.5F, -14.0F, -6.0F, 0.0F, 14.0F, 12.0F), PartPose.offset(0.0F, 23.0F, 0.0F)
      );
      glass.addOrReplaceChild(
         "glass3", CubeListBuilder.create().texOffs(1, 38).addBox(-7.5F, -14.0F, -6.0F, 0.0F, 14.0F, 12.0F), PartPose.offset(0.0F, 23.0F, 0.0F)
      );
      glass.addOrReplaceChild(
         "glass4", CubeListBuilder.create().texOffs(40, 34).addBox(-6.0F, -14.0F, -7.5F, 12.0F, 14.0F, 0.0F), PartPose.offset(0.0F, 23.0F, 0.0F)
      );
      glass.addOrReplaceChild(
         "glass5", CubeListBuilder.create().texOffs(33, 50).addBox(-6.0F, -14.0F, 7.5F, 12.0F, 14.0F, 0.0F), PartPose.offset(0.0F, 23.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
      this.solid.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderSolid(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
      this.solid.render(poseStack, vertexConsumer, packedLight, packedOverlay, -1);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderGlass(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
      this.glass.render(poseStack, vertexConsumer, packedLight, packedOverlay, -1493172225);
   }
}
