package thaumicenergistics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import thaumicenergistics.block.gearbox.entity.GearBoxBlockEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GearBoxModel {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      ResourceLocation.fromNamespaceAndPath("thaumicenergistics", "gearbox"), "main"
   );
   private static final float HALF_TURN = (float) (Math.PI / 2);
   private static final float QUARTER_TURN = (float) (Math.PI / 4);
   private static final float EDGE_DISTANCE = 3.12F;
   private static final int SIDE_COUNT = Direction.values().length;
   private final ModelPart transmission;
   private final ModelPart shaft;
   private final ModelPart spoke;
   private float rotationAngle = 0.0F;
   private final GearBoxModel.SideTransformation[] transformations;

   public GearBoxModel(ModelPart root) {
      this.transmission = root.getChild("transmission");
      this.shaft = root.getChild("shaft");
      this.spoke = root.getChild("spoke");
      this.transformations = new GearBoxModel.SideTransformation[Direction.values().length];
      this.transformations[Direction.EAST.ordinal()] = new GearBoxModel.SideTransformation(0.0F, 0.0F, 3.12F, 0.0F, 0.0F);
      this.transformations[Direction.WEST.ordinal()] = new GearBoxModel.SideTransformation(0.0F, 0.0F, -3.12F, 0.0F, 0.0F);
      this.transformations[Direction.UP.ordinal()] = new GearBoxModel.SideTransformation(0.0F, (float) (Math.PI / 2), 0.0F, 3.12F, 0.0F);
      this.transformations[Direction.DOWN.ordinal()] = new GearBoxModel.SideTransformation(0.0F, (float) (Math.PI / 2), 0.0F, -3.12F, 0.0F);
      this.transformations[Direction.SOUTH.ordinal()] = new GearBoxModel.SideTransformation((float) (Math.PI / 2), 0.0F, 0.0F, 0.0F, 3.12F);
      this.transformations[Direction.NORTH.ordinal()] = new GearBoxModel.SideTransformation((float) (Math.PI / 2), 0.0F, 0.0F, 0.0F, -3.12F);
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "transmission", CubeListBuilder.create().texOffs(1, 0).addBox(0.0F, 0.0F, 0.0F, 7.0F, 7.0F, 7.0F), PartPose.offset(-3.5F, -3.5F, -3.5F)
      );
      root.addOrReplaceChild("shaft", CubeListBuilder.create().texOffs(1, 15).addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F), PartPose.ZERO);
      root.addOrReplaceChild("spoke", CubeListBuilder.create().texOffs(1, 18).addBox(-0.5F, -3.5F, -0.5F, 1.0F, 7.0F, 1.0F), PartPose.ZERO);
      return LayerDefinition.create(mesh, 32, 32);
   }

   private void renderShaft(int side, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
      GearBoxModel.SideTransformation st = this.transformations[side];
      if (st.hasShaft) {
         poseStack.pushPose();
         poseStack.translate(st.shaftOffsetX / 16.0F, st.shaftOffsetY / 16.0F, st.shaftOffsetZ / 16.0F);
         poseStack.mulPose(Axis.ZP.rotation(st.rotateAngleZ));
         poseStack.mulPose(Axis.YP.rotation(st.rotateAngleY));
         poseStack.mulPose(Axis.XP.rotation(this.rotationAngle));
         boolean isDownSide = side == Direction.DOWN.ordinal();
         if (isDownSide && !st.hasGear) {
            poseStack.scale(2.3F, 1.0F, 1.0F);
         }

         this.shaft.render(poseStack, buffer, packedLight, packedOverlay);
         poseStack.popPose();
         if (st.hasGear) {
            for (int i = 0; i < 4; i++) {
               float a = this.rotationAngle + (float)i * (float) (Math.PI / 4);
               poseStack.pushPose();
               poseStack.translate(st.spokeOffsetX / 16.0F, st.spokeOffsetY / 16.0F, st.spokeOffsetZ / 16.0F + 0.001F);
               poseStack.mulPose(Axis.ZP.rotation(st.rotateAngleZ));
               poseStack.mulPose(Axis.YP.rotation(st.rotateAngleY));
               poseStack.mulPose(Axis.XP.rotation(a));
               this.spoke.render(poseStack, buffer, packedLight, packedOverlay);
               poseStack.popPose();
            }
         }
      }
   }

   public void render(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
      this.transmission.render(poseStack, buffer, packedLight, packedOverlay);

      for (int side = 0; side < SIDE_COUNT; side++) {
         this.renderShaft(side, poseStack, buffer, packedLight, packedOverlay);
      }
   }

   public void updateToTileEntity(GearBoxBlockEntity gearboxTile) {
      for (int side = 0; side < SIDE_COUNT; side++) {
         boolean withGear = !gearboxTile.sideIsFacingCrankable[side];
         this.transformations[side].setIsRendered(true, withGear);
      }

      this.rotationAngle = gearboxTile.shaftRotation;
   }

   private class SideTransformation {
      private static final float SPOKE_OFFSET = 1.2F;
      public final float rotateAngleY;
      public final float rotateAngleZ;
      public final float shaftOffsetX;
      public final float shaftOffsetY;
      public final float shaftOffsetZ;
      public final float spokeOffsetX;
      public final float spokeOffsetY;
      public final float spokeOffsetZ;
      boolean hasShaft = true;
      boolean hasGear = false;

      public SideTransformation(final float rotateAngleY, final float rotateAngleZ, final float offsetX, final float offsetY, final float offsetZ) {
         this.rotateAngleY = rotateAngleY;
         this.rotateAngleZ = rotateAngleZ;
         this.shaftOffsetX = offsetX;
         this.shaftOffsetY = offsetY;
         this.shaftOffsetZ = offsetZ;
         this.spokeOffsetX = offsetX * 1.2F;
         this.spokeOffsetY = offsetY * 1.2F;
         this.spokeOffsetZ = offsetZ * 1.2F;
      }

      public void setIsRendered(boolean shaft, boolean gear) {
         this.hasShaft = shaft;
         this.hasGear = gear;
      }
   }
}
