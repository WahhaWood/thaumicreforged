package thaumcraft.block.hungry_chest.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ChestModel extends Model {
   private final ModelPart lid;
   private final ModelPart bottom;
   private final ModelPart lock;

   @OnlyIn(Dist.CLIENT)
   public ChestModel(ModelPart root) {
      super(RenderType::entityCutout);
      this.lid = root.getChild("lid");
      this.bottom = root.getChild("bottom");
      this.lock = root.getChild("lock");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      float pivotY = 10.0F;
      float pivotZ = 7.0F;
      partdefinition.addOrReplaceChild(
         "lid", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -5.0F, -14.0F, 14.0F, 5.0F, 14.0F), PartPose.offset(0.0F, pivotY, pivotZ)
      );
      partdefinition.addOrReplaceChild(
         "bottom", CubeListBuilder.create().texOffs(0, 19).addBox(-7.0F, 0.0F, -14.0F, 14.0F, 10.0F, 14.0F), PartPose.offset(0.0F, pivotY, pivotZ)
      );
      partdefinition.addOrReplaceChild(
         "lock", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -15.0F, 2.0F, 4.0F, 1.0F), PartPose.offset(0.0F, pivotY, pivotZ)
      );
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public void setLidAngle(float angle) {
      this.lid.xRot = -(angle * 1.570796F);
      this.lock.xRot = this.lid.xRot;
   }

   @OnlyIn(Dist.CLIENT)
   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
      this.lid.render(poseStack, buffer, packedLight, packedOverlay, color);
      this.lock.render(poseStack, buffer, packedLight, packedOverlay, color);
      this.bottom.render(poseStack, buffer, packedLight, packedOverlay, color);
   }
}
