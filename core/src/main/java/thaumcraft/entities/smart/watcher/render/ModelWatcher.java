package thaumcraft.entities.smart.watcher.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.entities.smart.watcher.EntityWatcher;

@OnlyIn(Dist.CLIENT)
public class ModelWatcher<T extends EntityWatcher> extends HierarchicalModel<T> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "watcher"), "main");
   private static final float DEG = (float) (Math.PI / 180.0);
   private static final float[] A1 = new float[]{1.75F, 0.25F, 0.0F, 0.0F, 0.5F, 0.5F, 0.5F, 0.5F, 1.25F, 0.75F, 0.0F, 0.0F};
   private static final float[] A2 = new float[]{0.0F, 0.0F, 0.0F, 0.0F, 0.25F, 1.75F, 1.25F, 0.75F, 0.0F, 0.0F, 0.0F, 0.0F};
   private static final float[] A3 = new float[]{0.0F, 0.0F, 0.25F, 1.75F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.75F, 1.25F};
   private static final float[] P4 = new float[]{0.0F, 0.0F, 8.0F, -8.0F, -8.0F, 8.0F, 8.0F, -8.0F, 0.0F, 0.0F, 8.0F, -8.0F};
   private static final float[] P5 = new float[]{-8.0F, -8.0F, -8.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.0F, 8.0F, 8.0F, 8.0F, 8.0F};
   private static final float[] P6 = new float[]{8.0F, -8.0F, 0.0F, 0.0F, -8.0F, -8.0F, 8.0F, 8.0F, 8.0F, -8.0F, 0.0F, 0.0F};
   private final ModelPart root;
   private final ModelPart body;
   private final ModelPart eye;
   private final ModelPart[] spines;
   private final ModelPart[] tail;

   public ModelWatcher(ModelPart root) {
      this.root = root;
      this.body = root.getChild("body");
      this.eye = this.body.getChild("eye");
      this.spines = new ModelPart[12];

      for (int i = 0; i < 12; i++) {
         this.spines[i] = this.body.getChild("spine" + i);
      }

      this.tail = new ModelPart[3];
      this.tail[0] = this.body.getChild("tail0");
      this.tail[1] = this.tail[0].getChild("tail1");
      this.tail[2] = this.tail[1].getChild("tail2");
   }

   public ModelPart root() {
      return this.root;
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition body = root.addOrReplaceChild(
         "body",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-6.0F, 10.0F, -8.0F, 12.0F, 12.0F, 16.0F)
            .texOffs(0, 28)
            .addBox(-8.0F, 10.0F, -6.0F, 2.0F, 12.0F, 12.0F)
            .texOffs(0, 28)
            .addBox(6.0F, 10.0F, -6.0F, 2.0F, 12.0F, 12.0F)
            .texOffs(16, 40)
            .addBox(-6.0F, 8.0F, -6.0F, 12.0F, 2.0F, 12.0F)
            .texOffs(16, 40)
            .addBox(-6.0F, 22.0F, -6.0F, 12.0F, 2.0F, 12.0F),
         PartPose.ZERO
      );

      for (int i = 0; i < 12; i++) {
         body.addOrReplaceChild("spine" + i, CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F), PartPose.ZERO);
      }

      body.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, 15.0F, 0.0F, 2.0F, 2.0F, 1.0F), PartPose.ZERO);
      PartDefinition tail0 = body.addOrReplaceChild(
         "tail0", CubeListBuilder.create().texOffs(40, 0).addBox(-2.0F, 14.0F, 7.0F, 4.0F, 4.0F, 8.0F), PartPose.ZERO
      );
      PartDefinition tail1 = tail0.addOrReplaceChild(
         "tail1", CubeListBuilder.create().texOffs(0, 54).addBox(0.0F, 14.0F, 0.0F, 3.0F, 3.0F, 7.0F), PartPose.ZERO
      );
      tail1.addOrReplaceChild(
         "tail2",
         CubeListBuilder.create().texOffs(41, 32).addBox(0.0F, 14.0F, 0.0F, 2.0F, 2.0F, 6.0F).texOffs(25, 19).addBox(1.0F, 10.5F, 3.0F, 1.0F, 9.0F, 9.0F),
         PartPose.ZERO
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      float pt = ageInTicks - entity.tickCount;
      this.body.yRot = netHeadYaw * (float) (Math.PI / 180.0);
      this.body.xRot = headPitch * (float) (Math.PI / 180.0);
      float spike = entity.getSpikesAnimation(pt);
      float n8 = (1.0F - spike) * 0.55F;

      for (int i = 0; i < 12; i++) {
         this.spines[i].xRot = (float) Math.PI * A1[i];
         this.spines[i].yRot = (float) Math.PI * A2[i];
         this.spines[i].zRot = (float) Math.PI * A3[i];
         float s = 1.0F + Mth.cos(ageInTicks * 1.5F + i) * 0.01F - n8;
         this.spines[i].setPos(P4[i] * s, 16.0F + P5[i] * s, P6[i] * s);
      }

      this.eye.visible = true;
      this.eye.xRot = 0.0F;
      this.eye.yRot = 0.0F;
      this.eye.zRot = 0.0F;
      this.eye.setPos(0.0F, 0.0F, -8.25F);
      LivingEntity lookTarget = Minecraft.getInstance().getCameraEntity() instanceof LivingEntity le ? le : null;
      if (entity.hasActiveAttackTarget()) {
         LivingEntity t = entity.getActiveAttackTarget();
         if (t != null) {
            lookTarget = t;
         }
      }

      if (lookTarget != null) {
         Vec3 tEyes = lookTarget.getEyePosition(pt);
         Vec3 mEyes = entity.getEyePosition(pt);
         this.eye.y = tEyes.y - mEyes.y > 0.0 ? 0.0F : 1.0F;
         Vec3 look = entity.getLookAngle();
         Vec3 lookXZ = new Vec3(look.x, 0.0, look.z);
         Vec3 dir = new Vec3(mEyes.x - tEyes.x, 0.0, mEyes.z - tEyes.z);
         if (dir.lengthSqr() > 1.0E-6) {
            Vec3 perp = dir.normalize().yRot((float) (Math.PI / 2));
            double dp = lookXZ.dot(perp);
            this.eye.x = Mth.sqrt((float)Math.abs(dp)) * 2.0F * (float)Math.signum(dp);
         } else {
            this.eye.x = 0.0F;
         }
      } else {
         this.eye.x = 0.0F;
         this.eye.y = 1.0F;
      }

      float tailAnim = entity.getTailAnimation(pt);
      float s0 = Mth.sin(tailAnim) * (float) Math.PI;
      this.tail[0].yRot = s0 * 0.05F;
      this.tail[1].yRot = s0 * 0.1F;
      this.tail[1].setPos(-1.5F, 0.5F, 14.0F);
      this.tail[2].yRot = s0 * 0.15F;
      this.tail[2].setPos(0.5F, 0.5F, 6.0F);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
      this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
   }
}
