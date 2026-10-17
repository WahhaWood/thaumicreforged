package thaumcraft.entities.smart.travelling_trunk.render;

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
import org.jetbrains.annotations.NotNull;
import thaumcraft.entities.smart.travelling_trunk.EntityTravelingTrunk;

public class ModelTrunk extends EntityModel<EntityTravelingTrunk> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "trunk"), "main");
   private final ModelPart root;
   public final ModelPart chestLid;
   public final ModelPart chestBelow;
   public final ModelPart chestKnob;

   public ModelTrunk(ModelPart root) {
      this.root = root;
      this.chestLid = root.getChild("chest_lid");
      this.chestBelow = root.getChild("chest_below");
      this.chestKnob = root.getChild("chest_knob");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "chest_lid", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -5.0F, -14.0F, 14.0F, 5.0F, 14.0F), PartPose.offset(1.0F, 7.0F, 15.0F)
      );
      root.addOrReplaceChild(
         "chest_knob", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -15.0F, 2.0F, 4.0F, 1.0F), PartPose.offset(8.0F, 7.0F, 15.0F)
      );
      root.addOrReplaceChild(
         "chest_below", CubeListBuilder.create().texOffs(0, 19).addBox(0.0F, 0.0F, 0.0F, 14.0F, 10.0F, 14.0F), PartPose.offset(1.0F, 6.0F, 1.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public void setupAnim(@NotNull EntityTravelingTrunk e, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      float pt = ageInTicks - e.tickCount;
      if (pt < 0.0F) {
         pt = 0.0F;
      }

      if (pt > 1.0F) {
         pt = 1.0F;
      }

      float lid = Mth.lerp(pt, e.lidrotO, e.lidrot);
      float n9 = 1.0F - lid;
      float x = -((1.0F - n9 * n9 * n9) * (float) (Math.PI / 2));
      this.chestLid.xRot = x;
      this.chestKnob.xRot = x;
   }

   public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vc, int packedLight, int packedOverlay, int color) {
      this.root.render(poseStack, vc, packedLight, packedOverlay, color);
   }
}
