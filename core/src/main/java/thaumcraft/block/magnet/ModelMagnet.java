package thaumcraft.block.magnet;

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

public class ModelMagnet extends Model {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "magnet"), "main");
   private final ModelPart panel;
   private final ModelPart magnet;
   private final ModelPart box1;
   private final ModelPart box2;
   private final ModelPart box3;
   private final ModelPart box4;
   private final ModelPart box5;

   public ModelMagnet(ModelPart root) {
      super(RenderType::entityCutout);
      this.panel = root.getChild("panel");
      this.magnet = root.getChild("magnet");
      this.box1 = root.getChild("box1");
      this.box2 = root.getChild("box2");
      this.box3 = root.getChild("box3");
      this.box4 = root.getChild("box4");
      this.box5 = root.getChild("box5");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("panel", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 14.0F, 2.0F, 14.0F), PartPose.offset(-7.0F, 22.0F, -7.0F));
      root.addOrReplaceChild("magnet", CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 4.0F, 13.0F, 4.0F), PartPose.offset(-2.0F, 9.0F, -2.0F));
      root.addOrReplaceChild(
         "box1",
         CubeListBuilder.create().texOffs(28, 19).addBox(0.0F, -2.0F, 0.0F, 6.0F, 14.0F, 0.0F),
         PartPose.offsetAndRotation(3.0F, 10.0F, -3.0F, 0.0F, -1.570796F, 0.0F)
      );
      root.addOrReplaceChild(
         "box2",
         CubeListBuilder.create().texOffs(28, 33).addBox(0.0F, 0.0F, 0.0F, 6.0F, 14.0F, 0.0F),
         PartPose.offsetAndRotation(-3.0F, 8.0F, 3.0F, 0.0F, 1.570796F, 0.0F)
      );
      root.addOrReplaceChild(
         "box3",
         CubeListBuilder.create().texOffs(40, 19).addBox(0.0F, 0.0F, 0.0F, 6.0F, 14.0F, 0.0F),
         PartPose.offsetAndRotation(3.0F, 8.0F, 3.0F, 0.0F, 3.141593F, 0.0F)
      );
      root.addOrReplaceChild("box4", CubeListBuilder.create().texOffs(40, 33).addBox(0.0F, 0.0F, 0.0F, 6.0F, 14.0F, 0.0F), PartPose.offset(-3.0F, 8.0F, -3.0F));
      root.addOrReplaceChild("box5", CubeListBuilder.create().texOffs(28, 49).addBox(0.0F, 0.0F, 0.0F, 6.0F, 0.0F, 6.0F), PartPose.offset(-3.0F, 8.0F, -3.0F));
      return LayerDefinition.create(mesh, 64, 64);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color) {
      this.panel.render(poseStack, consumer, light, overlay, color);
      this.magnet.render(poseStack, consumer, light, overlay, color);
      this.box1.render(poseStack, consumer, light, overlay, color);
      this.box2.render(poseStack, consumer, light, overlay, color);
      this.box3.render(poseStack, consumer, light, overlay, color);
      this.box4.render(poseStack, consumer, light, overlay, color);
      this.box5.render(poseStack, consumer, light, overlay, color);
   }
}
