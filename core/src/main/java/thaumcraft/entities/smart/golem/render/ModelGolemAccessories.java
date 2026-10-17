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
import org.jetbrains.annotations.NotNull;
import thaumcraft.entities.smart.golem.EntityGolemBase;
import thaumcraft.item.golem_core.GolemCore;

public class ModelGolemAccessories extends HierarchicalModel<EntityGolemBase> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      ResourceLocation.fromNamespaceAndPath("thaumcraft", "golem_accessories"), "main"
   );
   private final ModelPart root;
   public final ModelPart golemHeadFez;
   public final ModelPart golemHeadGlasses;
   public final ModelPart golemHeadHat;
   public final ModelPart golemHeadHatRim;
   public final ModelPart golemBowtie;
   public final ModelPart golemVisor;
   public final ModelPart golemDartgun;
   public final ModelPart golemMace;
   public final ModelPart golemPlate;
   public final ModelPart golemPlateLeft;
   public final ModelPart golemPlateRight;
   public final ModelPart golemHeadJar;
   public final ModelPart golemHeadBrain;
   public final ModelPart golemEvilHead;
   private int actionTimer;
   private int leftArmTimer;
   private int rightArmTimer;
   private boolean carrying;
   private float partialTick;

   public ModelGolemAccessories(ModelPart root) {
      this.root = root;
      this.golemHeadFez = root.getChild("golemHeadFez");
      this.golemHeadGlasses = root.getChild("golemHeadGlasses");
      this.golemHeadHat = root.getChild("golemHeadHat");
      this.golemHeadHatRim = root.getChild("golemHeadHatRim");
      this.golemBowtie = root.getChild("golemBowtie");
      this.golemVisor = root.getChild("golemVisor");
      this.golemDartgun = root.getChild("golemDartgun");
      this.golemMace = root.getChild("golemMace");
      this.golemPlate = root.getChild("golemPlate");
      this.golemPlateLeft = root.getChild("golemPlateLeft");
      this.golemPlateRight = root.getChild("golemPlateRight");
      this.golemHeadJar = root.getChild("golemHeadJar");
      this.golemHeadBrain = root.getChild("golemHeadBrain");
      this.golemEvilHead = root.getChild("golemEvilHead");
   }

   public static LayerDefinition createBodyLayer() {
      float n = 0.0F;
      int texW = 128;
      int texH = 128;
      float yOff = 30.0F;
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "golemHeadFez",
         CubeListBuilder.create().texOffs(0, 94).addBox(-4.5F, -15.0F, -6.0F, 9.0F, 7.0F, 9.0F, new CubeDeformation(n)),
         PartPose.offset(0.0F, yOff, -2.0F)
      );
      root.addOrReplaceChild(
         "golemPlate",
         CubeListBuilder.create().texOffs(32, 40).addBox(-6.5F, -1.0F, -7.0F, 13.0F, 12.0F, 13.0F, new CubeDeformation(n)),
         PartPose.offset(0.0F, yOff, 0.0F)
      );
      root.addOrReplaceChild(
         "golemPlateLeft",
         CubeListBuilder.create().texOffs(0, 44).addBox(-8.5F, -4.0F, -6.5F, 3.0F, 6.0F, 12.0F, new CubeDeformation(n)),
         PartPose.offset(0.0F, yOff, 0.0F)
      );
      root.addOrReplaceChild(
         "golemPlateRight",
         CubeListBuilder.create().mirror().texOffs(0, 44).addBox(5.5F, -4.0F, -6.5F, 3.0F, 6.0F, 12.0F, new CubeDeformation(n)).mirror(false),
         PartPose.offset(0.0F, yOff, 0.0F)
      );
      root.addOrReplaceChild(
         "golemHeadHat",
         CubeListBuilder.create().texOffs(0, 110).addBox(-4.5F, -17.0F, -6.0F, 9.0F, 9.0F, 9.0F, new CubeDeformation(n)),
         PartPose.offset(0.0F, yOff, -2.0F)
      );
      root.addOrReplaceChild(
         "golemHeadGlasses",
         CubeListBuilder.create().texOffs(0, 80).addBox(-4.5F, -8.0F, -6.0F, 9.0F, 4.0F, 9.0F, new CubeDeformation(n)),
         PartPose.offset(0.0F, yOff, -2.0F)
      );
      root.addOrReplaceChild(
         "golemVisor",
         CubeListBuilder.create().texOffs(0, 70).addBox(-5.0F, -8.0F, -6.0F, 10.0F, 5.0F, 5.0F, new CubeDeformation(n)),
         PartPose.offset(0.0F, yOff, -2.0F)
      );
      root.addOrReplaceChild(
         "golemHeadHatRim",
         CubeListBuilder.create().texOffs(36, 114).addBox(-6.5F, -9.0F, -8.0F, 13.0F, 1.0F, 13.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, yOff, -2.0F)
      );
      root.addOrReplaceChild(
         "golemDartgun",
         CubeListBuilder.create().texOffs(80, 80).addBox(7.9F, 7.5F, -3.5F, 6.0F, 16.0F, 7.0F, new CubeDeformation(n)),
         PartPose.offset(0.0F, yOff, 0.0F)
      );
      root.addOrReplaceChild(
         "golemMace",
         CubeListBuilder.create().texOffs(80, 26).addBox(-13.0F, 15.0F, -5.0F, 6.0F, 8.0F, 10.0F, new CubeDeformation(n)),
         PartPose.offset(0.0F, yOff, 0.0F)
      );
      root.addOrReplaceChild(
         "golemBowtie",
         CubeListBuilder.create().texOffs(0, 0).addBox(-8.5F, -2.0F, -6.5F, 17.0F, 4.0F, 12.0F, new CubeDeformation(n)),
         PartPose.offset(0.0F, yOff, 0.0F)
      );
      root.addOrReplaceChild(
         "golemHeadJar",
         CubeListBuilder.create().texOffs(96, 56).addBox(-4.0F, -15.0F, -5.5F, 8.0F, 4.0F, 8.0F, new CubeDeformation(n)),
         PartPose.offset(0.0F, yOff, -2.0F)
      );
      root.addOrReplaceChild(
         "golemHeadBrain",
         CubeListBuilder.create().texOffs(96, 70).addBox(-3.5F, -14.0F, -5.0F, 7.0F, 3.0F, 7.0F, new CubeDeformation(n)),
         PartPose.offset(0.0F, yOff, -2.0F)
      );
      root.addOrReplaceChild(
         "golemEvilHead",
         CubeListBuilder.create().texOffs(64, 65).addBox(-4.0F, -9.0F, -5.5F, 8.0F, 7.0F, 8.0F, new CubeDeformation(n)),
         PartPose.offset(0.0F, yOff, -2.0F)
      );
      return LayerDefinition.create(mesh, texW, texH);
   }

   @NotNull
   public ModelPart root() {
      return this.root;
   }

   public void prepareMobModel(EntityGolemBase entity, float limbSwing, float limbSwingAmount, float partialTick) {
      this.actionTimer = entity.getActionTimer();
      this.leftArmTimer = entity.leftArm;
      this.rightArmTimer = entity.rightArm;
      this.carrying = !entity.getCarriedForDisplay().isEmpty();
      this.partialTick = partialTick;
   }

   public void setupAnim(EntityGolemBase entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      boolean inactive = entity.inactive;
      if (entity.getCore() == GolemCore.BLANK || entity.bootup < 0.0F) {
         this.golemHeadFez.yRot = 0.0F;
         this.golemHeadFez.xRot = 0.57595867F;
      } else if (inactive) {
         this.golemHeadFez.yRot = 0.0F;
         this.golemHeadFez.xRot = 0.57595867F;
      } else if (entity.bootup > 0.0F) {
         this.golemHeadFez.yRot = 0.0F;
         this.golemHeadFez.xRot = entity.bootup / (180.0F / (float)Math.PI);
      } else {
         this.golemHeadFez.yRot = netHeadYaw / (180.0F / (float)Math.PI);
         this.golemHeadFez.xRot = headPitch / (180.0F / (float)Math.PI);
      }

      float y = this.golemHeadFez.yRot;
      float x = this.golemHeadFez.xRot;
      this.golemHeadGlasses.yRot = y;
      this.golemHeadGlasses.xRot = x;
      this.golemHeadJar.yRot = y;
      this.golemHeadJar.xRot = x;
      this.golemHeadBrain.yRot = y;
      this.golemHeadBrain.xRot = x;
      this.golemEvilHead.yRot = y;
      this.golemEvilHead.xRot = x;
      this.golemVisor.yRot = y;
      this.golemVisor.xRot = x;
      this.golemHeadHat.yRot = y;
      this.golemHeadHat.xRot = x;
      this.golemHeadHatRim.yRot = y;
      this.golemHeadHatRim.xRot = x;
      if (this.actionTimer > 0) {
         this.golemDartgun.xRot = -2.0F + 1.5F * this.func_78172_a(this.actionTimer - this.partialTick, 5.0F);
         this.golemMace.xRot = -2.0F + 1.5F * this.func_78172_a(this.actionTimer - this.partialTick, 5.0F);
      } else if (this.leftArmTimer <= 0 && this.rightArmTimer <= 0) {
         if (this.carrying) {
            this.golemDartgun.xRot = -1.0F;
            this.golemMace.xRot = -1.0F;
         } else {
            this.golemDartgun.xRot = (-0.2F - 1.5F * this.func_78172_a(limbSwing, 13.0F)) * limbSwingAmount;
            this.golemMace.xRot = (-0.2F + 1.5F * this.func_78172_a(limbSwing, 13.0F)) * limbSwingAmount;
         }
      } else {
         if (this.leftArmTimer > 0) {
            this.golemMace.xRot = -2.0F + 1.5F * this.func_78172_a(this.leftArmTimer - this.partialTick, 20.0F);
         }

         if (this.rightArmTimer > 0) {
            this.golemDartgun.xRot = -2.0F + 1.5F * this.func_78172_a(this.rightArmTimer - this.partialTick, 20.0F);
         }
      }
   }

   private float func_78172_a(float n, float n2) {
      return (Math.abs(n % n2 - n2 * 0.5F) - n2 * 0.25F) / (n2 * 0.25F);
   }
}
