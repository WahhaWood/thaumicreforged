package thaumcraft.entities.smart.golem.render;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.entities.smart.golem.EntityGolemBase;
import thaumcraft.item.golem_core.GolemCore;

public class ModelGolem extends HierarchicalModel<EntityGolemBase> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "golem"), "main");
   public int pass;
   private final ModelPart root;
   public final ModelPart golemHead;
   public final ModelPart golemBody;
   public final ModelPart golemRightArm;
   public final ModelPart golemLeftArm;
   public final ModelPart golemRightLeg;
   public final ModelPart golemLeftLeg;
   private float swayZ;
   private int actionTimer;
   private boolean carryPose;
   private int leftArmTimer;
   private int rightArmTimer;
   private float partialTick;

   public ModelGolem(ModelPart root) {
      this.root = root;
      this.golemHead = root.getChild("golemHead");
      this.golemBody = root.getChild("golemBody");
      this.golemRightArm = root.getChild("golemRightArm");
      this.golemLeftArm = root.getChild("golemLeftArm");
      this.golemRightLeg = root.getChild("golemRightLeg");
      this.golemLeftLeg = root.getChild("golemLeftLeg");
      this.pass = 0;
   }

   public static LayerDefinition createBodyLayer() {
      float n = 0.0F;
      int texW = 128;
      int texH = 128;
      float yOff = 30.0F;
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "golemHead",
         CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -11.0F, -5.5F, 8.0F, 9.0F, 8.0F, new CubeDeformation(n)),
         PartPose.offset(0.0F, yOff, -2.0F)
      );
      root.addOrReplaceChild(
         "golemBody",
         CubeListBuilder.create()
            .texOffs(0, 40)
            .addBox(-8.0F, -2.0F, -6.0F, 16.0F, 12.0F, 11.0F, new CubeDeformation(n))
            .texOffs(0, 70)
            .addBox(-4.5F, 10.0F, -3.0F, 9.0F, 5.0F, 6.0F, new CubeDeformation(n + 0.5F)),
         PartPose.offset(0.0F, yOff, 0.0F)
      );
      root.addOrReplaceChild(
         "golemRightArm",
         CubeListBuilder.create().texOffs(60, 21).addBox(-12.0F, -2.5F, -3.0F, 4.0F, 25.0F, 6.0F, new CubeDeformation(n)),
         PartPose.offset(0.0F, yOff, 0.0F)
      );
      root.addOrReplaceChild(
         "golemLeftArm",
         CubeListBuilder.create().mirror().texOffs(60, 21).addBox(8.0F, -2.5F, -3.0F, 4.0F, 25.0F, 6.0F, new CubeDeformation(n)).mirror(false),
         PartPose.offset(0.0F, yOff, 0.0F)
      );
      root.addOrReplaceChild(
         "golemRightLeg",
         CubeListBuilder.create().texOffs(37, 0).addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F, new CubeDeformation(n)),
         PartPose.offset(-4.0F, 18.0F + yOff, 0.0F)
      );
      root.addOrReplaceChild(
         "golemLeftLeg",
         CubeListBuilder.create().mirror().texOffs(37, 0).addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F, new CubeDeformation(n)).mirror(false),
         PartPose.offset(5.0F, 18.0F + yOff, 0.0F)
      );
      return LayerDefinition.create(mesh, texW, texH);
   }

   public ModelPart root() {
      return this.root;
   }

   public void prepareMobModel(EntityGolemBase entity, float limbSwing, float limbSwingAmount, float partialTick) {
      this.actionTimer = entity.getActionTimer();
      this.partialTick = partialTick;
      this.carryPose = !entity.getCarriedForDisplay().isEmpty() || entity.getCore() == GolemCore.LIQUID;
      this.leftArmTimer = entity.leftArm;
      this.rightArmTimer = entity.rightArm;
      this.swayZ = 0.0F;
      if (limbSwingAmount >= 0.01F) {
         float p = limbSwing - limbSwingAmount * (1.0F - partialTick) + 6.0F;
         float n4 = 13.0F;
         float f = (Mth.abs(p % n4 - n4 * 0.5F) - n4 * 0.25F) / (n4 * 0.25F);
         this.swayZ = 6.5F * f;
      }
   }

   public void setupAnim(EntityGolemBase entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      float bootup = entity.bootup;
      GolemCore core = entity.getCore();
      boolean inactive = entity.inactive;
      this.root.zRot = 0.0F;
      if (core != GolemCore.BLANK && !(bootup < 0.0F)) {
         if (inactive) {
            this.golemHead.yRot = 0.0F;
            this.golemHead.xRot = 0.57595867F;
         } else if (bootup > 0.0F) {
            this.golemHead.yRot = 0.0F;
            this.golemHead.xRot = bootup / (180.0F / (float)Math.PI);
         } else {
            this.golemHead.yRot = netHeadYaw / (180.0F / (float)Math.PI);
            this.golemHead.xRot = headPitch / (180.0F / (float)Math.PI);
         }

         this.golemRightLeg.xRot = -1.5F * this.func_78172_a(limbSwing, 13.0F) * limbSwingAmount;
         this.golemLeftLeg.xRot = 1.5F * this.func_78172_a(limbSwing, 13.0F) * limbSwingAmount;
         this.golemRightLeg.yRot = 0.0F;
         this.golemLeftLeg.yRot = 0.0F;
         this.golemRightArm.xRot = (-0.2F + 1.5F * this.func_78172_a(limbSwing, 13.0F)) * limbSwingAmount;
         this.golemLeftArm.xRot = (-0.2F - 1.5F * this.func_78172_a(limbSwing, 13.0F)) * limbSwingAmount;
         this.golemLeftArm.zRot = 0.0F;
         this.golemRightArm.zRot = 0.0F;
         if (core == GolemCore.ESSENCE) {
            float n8 = (1.0F - (0.5F + Math.min(64, entity.getCarryLimit()) / 128.0F)) * 25.0F;
            this.golemLeftArm.zRot = n8 / (180.0F / (float)Math.PI);
            this.golemRightArm.zRot = -n8 / (180.0F / (float)Math.PI);
         }
      } else {
         this.golemHead.yRot = 0.0F;
         this.golemHead.xRot = 0.57595867F;
         this.golemRightLeg.xRot = 0.0F;
         this.golemLeftLeg.xRot = 0.0F;
         this.golemRightArm.xRot = 0.0F;
         this.golemLeftArm.xRot = 0.0F;
         this.golemRightLeg.yRot = 0.0F;
         this.golemLeftLeg.yRot = 0.0F;
         this.golemLeftArm.zRot = 0.0F;
         this.golemRightArm.zRot = 0.0F;
      }

      if (this.actionTimer > 0) {
         this.golemRightArm.xRot = -2.0F + 1.5F * this.func_78172_a(this.actionTimer - this.partialTick, 5.0F);
         this.golemLeftArm.xRot = -2.0F + 1.5F * this.func_78172_a(this.actionTimer - this.partialTick, 5.0F);
      } else if (this.leftArmTimer <= 0 && this.rightArmTimer <= 0) {
         if (this.carryPose) {
            this.golemRightArm.xRot = -1.0F;
            this.golemLeftArm.xRot = -1.0F;
         }
      } else {
         if (this.leftArmTimer > 0) {
            this.golemLeftArm.xRot = -2.0F + 1.5F * this.func_78172_a(this.leftArmTimer - this.partialTick, 20.0F);
         }

         if (this.rightArmTimer > 0) {
            this.golemRightArm.xRot = -2.0F + 1.5F * this.func_78172_a(this.rightArmTimer - this.partialTick, 20.0F);
         }
      }
   }

   private float func_78172_a(float n, float n2) {
      return (Mth.abs(n % n2 - n2 * 0.5F) - n2 * 0.25F) / (n2 * 0.25F);
   }
}
