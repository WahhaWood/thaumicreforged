package thaumcraft.entities.smart.fire_bat.render;

import net.minecraft.client.model.HierarchicalModel;
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
import thaumcraft.entities.smart.fire_bat.EntityFireBat;

public class ModelFireBat<T extends EntityFireBat> extends HierarchicalModel<T> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "fire_bat"), "main");
   private final ModelPart root;
   private final ModelPart head;
   private final ModelPart body;
   private final ModelPart rightWing;
   private final ModelPart leftWing;
   private final ModelPart outerRightWing;
   private final ModelPart outerLeftWing;

   public ModelFireBat(ModelPart root) {
      this.root = root;
      this.head = root.getChild("head");
      this.body = root.getChild("body");
      this.rightWing = this.body.getChild("right_wing");
      this.leftWing = this.body.getChild("left_wing");
      this.outerRightWing = this.rightWing.getChild("outer_right_wing");
      this.outerLeftWing = this.leftWing.getChild("outer_left_wing");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.ZERO);
      head.addOrReplaceChild("ear_right", CubeListBuilder.create().texOffs(24, 0).addBox(-4.0F, -6.0F, -2.0F, 3.0F, 4.0F, 1.0F), PartPose.ZERO);
      head.addOrReplaceChild(
         "ear_left", CubeListBuilder.create().texOffs(24, 0).mirror().addBox(1.0F, -6.0F, -2.0F, 3.0F, 4.0F, 1.0F).mirror(false), PartPose.ZERO
      );
      PartDefinition body = root.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 4.0F, -3.0F, 6.0F, 12.0F, 6.0F).texOffs(0, 34).addBox(-5.0F, 16.0F, 0.0F, 10.0F, 6.0F, 1.0F),
         PartPose.ZERO
      );
      PartDefinition rightWing = body.addOrReplaceChild(
         "right_wing", CubeListBuilder.create().texOffs(42, 0).addBox(-12.0F, 1.0F, 1.5F, 10.0F, 16.0F, 1.0F), PartPose.ZERO
      );
      rightWing.addOrReplaceChild(
         "outer_right_wing", CubeListBuilder.create().texOffs(24, 16).addBox(-8.0F, 1.0F, 0.0F, 8.0F, 12.0F, 1.0F), PartPose.offset(-12.0F, 1.0F, 1.5F)
      );
      PartDefinition leftWing = body.addOrReplaceChild(
         "left_wing", CubeListBuilder.create().texOffs(42, 0).mirror().addBox(2.0F, 1.0F, 1.5F, 10.0F, 16.0F, 1.0F).mirror(false), PartPose.ZERO
      );
      leftWing.addOrReplaceChild(
         "outer_left_wing",
         CubeListBuilder.create().texOffs(24, 16).mirror().addBox(0.0F, 1.0F, 0.0F, 8.0F, 12.0F, 1.0F).mirror(false),
         PartPose.offset(12.0F, 1.0F, 1.5F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      if (entity.getIsBatHanging()) {
         this.head.xRot = headPitch * (float) (Math.PI / 180.0);
         this.head.yRot = (float) Math.PI - netHeadYaw * (float) (Math.PI / 180.0);
         this.head.zRot = (float) Math.PI;
         this.head.setPos(0.0F, -2.0F, 0.0F);
         this.rightWing.setPos(-3.0F, 0.0F, 3.0F);
         this.leftWing.setPos(3.0F, 0.0F, 3.0F);
         this.body.xRot = (float) Math.PI;
         this.rightWing.xRot = (float) (-Math.PI / 20);
         this.rightWing.yRot = (float) (-Math.PI * 2.0 / 5.0);
         this.outerRightWing.yRot = -1.7278761F;
         this.leftWing.xRot = this.rightWing.xRot;
         this.leftWing.yRot = -this.rightWing.yRot;
         this.outerLeftWing.yRot = -this.outerRightWing.yRot;
      } else {
         this.head.xRot = headPitch * (float) (Math.PI / 180.0);
         this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
         this.head.zRot = 0.0F;
         this.head.setPos(0.0F, 0.0F, 0.0F);
         this.rightWing.setPos(0.0F, 0.0F, 0.0F);
         this.leftWing.setPos(0.0F, 0.0F, 0.0F);
         this.body.xRot = (float) (Math.PI / 4) + Mth.cos(ageInTicks * 0.1F) * 0.15F;
         this.body.yRot = 0.0F;
         this.rightWing.yRot = Mth.cos(ageInTicks * 1.3F) * (float) Math.PI * 0.25F;
         this.leftWing.yRot = -this.rightWing.yRot;
         this.outerRightWing.yRot = this.rightWing.yRot * 0.5F;
         this.outerLeftWing.yRot = -this.rightWing.yRot * 0.5F;
      }
   }

   @NotNull
   public ModelPart root() {
      return this.root;
   }
}
