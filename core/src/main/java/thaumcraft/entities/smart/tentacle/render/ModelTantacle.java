package thaumcraft.entities.smart.tentacle.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import thaumcraft.entities.smart.tentacle.EntityTantacle;

public class ModelTantacle extends HierarchicalModel<EntityTantacle> {
   public static final int LENGTH = 10;
   public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "taintacle"), "main");
   private final ModelPart root;
   private final ModelPart tentacle;
   private final ModelPart[] segs = new ModelPart[9];
   private final ModelPart orb;
   private final ModelPart head;

   public ModelTantacle(ModelPart root) {
      this.root = root;
      this.tentacle = root.getChild("tentacle");
      ModelPart cur = this.tentacle;

      for (int i = 0; i < 9; i++) {
         cur = cur.getChild("seg" + i);
         this.segs[i] = cur;
      }

      this.orb = this.segs[8].getChild("orb");
      this.head = this.segs[8].getChild("head");
      float s = 0.88F;

      for (int i = 0; i < 9; i++) {
         this.segs[i].xScale = s;
         this.segs[i].yScale = s;
         this.segs[i].zScale = s;
      }

      this.orb.xScale = s;
      this.orb.yScale = s;
      this.orb.zScale = s;
      this.head.xScale = s;
      this.head.yScale = s;
      this.head.zScale = s;
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition tentacle = root.addOrReplaceChild(
         "tentacle", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 12.0F, 0.0F)
      );
      PartDefinition prev = tentacle;

      for (int i = 0; i < 9; i++) {
         prev = prev.addOrReplaceChild(
            "seg" + i, CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, -8.0F, 0.0F)
         );
      }

      prev.addOrReplaceChild("orb", CubeListBuilder.create().texOffs(0, 56).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F), PartPose.offset(0.0F, -8.0F, 0.0F));
      prev.addOrReplaceChild(
         "head", CubeListBuilder.create().texOffs(0, 32).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F), PartPose.offset(0.0F, -8.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public ModelPart root() {
      return this.root;
   }

   public void setupAnim(EntityTantacle entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      boolean agitated = entity.getAgitationState();
      float flail = entity.flailIntensity;
      int hurtTime = entity.tcGetHurtTime();
      int attackTime = entity.tcGetAttackTime();
      float n6 = 0.0625F;
      float n7 = n6 * 0.2F;
      float n8 = agitated ? 3.0F : 1.0F + -n7;
      float n9 = flail + (hurtTime <= 0 && attackTime <= 0 ? -n7 : n7);
      this.tentacle.xRot = 0.0F;
      this.tentacle.yRot = 0.0F;
      this.tentacle.zRot = 0.0F;

      for (int i = 0; i < 9; i++) {
         ModelPart p = this.segs[i];
         p.yRot = 0.0F;
         p.xRot = 0.15F * n9 * Mth.sin(ageInTicks * 0.1F * n8 - i / 2.0F);
         p.zRot = 0.1F / n9 * Mth.sin(ageInTicks * 0.15F - i / 2.0F);
      }
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
      this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
   }
}
