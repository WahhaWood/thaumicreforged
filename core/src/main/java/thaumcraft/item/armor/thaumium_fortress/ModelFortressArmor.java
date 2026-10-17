package thaumcraft.item.armor.thaumium_fortress;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.util.ItemStackData;

@OnlyIn(Dist.CLIENT)
public class ModelFortressArmor extends HumanoidModel<LivingEntity> {
   public static final ModelLayerLocation LAYER_OUTER = new ModelLayerLocation(
      ResourceLocation.fromNamespaceAndPath("thaumcraft", "fortress_armor_outer"), "main"
   );
   public static final ModelLayerLocation LAYER_INNER = new ModelLayerLocation(
      ResourceLocation.fromNamespaceAndPath("thaumcraft", "fortress_armor_inner"), "main"
   );
   private final ModelPart goggles = this.head.getChild("goggles");
   private final ModelPart[] mask = new ModelPart[3];
   private final ModelPart scroll;
   private final ModelPart book;
   private final ModelPart ornamentL;
   private final ModelPart ornamentL2;
   private final ModelPart ornamentR;
   private final ModelPart ornamentR2;
   private final ModelPart gemOrnament;
   private final ModelPart gem;
   private final ModelPart flapR;
   private final ModelPart flapL;
   private final ModelPart shoulderplateLtop;
   private final ModelPart shoulderplateL1;
   private final ModelPart shoulderplateL2;
   private final ModelPart shoulderplateL3;
   private final ModelPart shoulderplateRtop;
   private final ModelPart shoulderplateR1;
   private final ModelPart shoulderplateR2;
   private final ModelPart shoulderplateR3;
   private final ModelPart sidepanelR2;
   private final ModelPart sidepanelR3;
   private final ModelPart sidepanelL2;
   private final ModelPart sidepanelL3;

   public ModelFortressArmor(ModelPart root) {
      super(root);
      this.mask[0] = this.head.getChild("mask0");
      this.mask[1] = this.head.getChild("mask1");
      this.mask[2] = this.head.getChild("mask2");
      this.scroll = this.body.getChild("scroll");
      this.book = this.body.getChild("book");
      this.ornamentL = this.head.getChild("ornament_l");
      this.ornamentL2 = this.head.getChild("ornament_l2");
      this.ornamentR = this.head.getChild("ornament_r");
      this.ornamentR2 = this.head.getChild("ornament_r2");
      this.gemOrnament = this.head.getChild("gemornament");
      this.gem = this.head.getChild("gem");
      this.flapR = this.head.getChild("flap_r");
      this.flapL = this.head.getChild("flap_l");
      this.shoulderplateLtop = this.leftArm.getChild("shoulderplate_ltop");
      this.shoulderplateL1 = this.leftArm.getChild("shoulderplate_l1");
      this.shoulderplateL2 = this.leftArm.getChild("shoulderplate_l2");
      this.shoulderplateL3 = this.leftArm.getChild("shoulderplate_l3");
      this.shoulderplateRtop = this.rightArm.getChild("shoulderplate_rtop");
      this.shoulderplateR1 = this.rightArm.getChild("shoulderplate_r1");
      this.shoulderplateR2 = this.rightArm.getChild("shoulderplate_r2");
      this.shoulderplateR3 = this.rightArm.getChild("shoulderplate_r3");
      this.sidepanelR2 = this.rightLeg.getChild("sidepanel_r2");
      this.sidepanelR3 = this.rightLeg.getChild("sidepanel_r3");
      this.sidepanelL2 = this.leftLeg.getChild("sidepanel_l2");
      this.sidepanelL3 = this.leftLeg.getChild("sidepanel_l3");
   }

   public static LayerDefinition createBodyLayer(boolean outer) {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
      PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
      PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
      PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
      head.addOrReplaceChild("mask0", CubeListBuilder.create().texOffs(52, 2).addBox(-4.5F, -5.0F, -4.6F, 9.0F, 5.0F, 1.0F), PartPose.ZERO);
      head.addOrReplaceChild("mask1", CubeListBuilder.create().texOffs(76, 2).addBox(-4.5F, -5.0F, -4.6F, 9.0F, 5.0F, 1.0F), PartPose.ZERO);
      head.addOrReplaceChild("mask2", CubeListBuilder.create().texOffs(100, 2).addBox(-4.5F, -5.0F, -4.6F, 9.0F, 5.0F, 1.0F), PartPose.ZERO);
      head.addOrReplaceChild("goggles", CubeListBuilder.create().texOffs(100, 18).addBox(-4.5F, -5.0F, -4.25F, 9.0F, 5.0F, 1.0F), PartPose.ZERO);
      head.addOrReplaceChild(
         "ornament_l",
         CubeListBuilder.create().texOffs(78, 8).mirror().addBox(1.5F, -9.0F, -6.5F, 2.0F, 2.0F, 1.0F).mirror(false),
         PartPose.rotation(-0.1396263F, 0.0F, 0.0F)
      );
      head.addOrReplaceChild(
         "ornament_l2",
         CubeListBuilder.create().texOffs(78, 8).mirror().addBox(3.5F, -10.0F, -6.5F, 1.0F, 2.0F, 1.0F).mirror(false),
         PartPose.rotation(-0.1396263F, 0.0F, 0.0F)
      );
      head.addOrReplaceChild(
         "ornament_r", CubeListBuilder.create().texOffs(78, 8).addBox(-3.5F, -9.0F, -6.5F, 2.0F, 2.0F, 1.0F), PartPose.rotation(-0.1396263F, 0.0F, 0.0F)
      );
      head.addOrReplaceChild(
         "ornament_r2", CubeListBuilder.create().texOffs(78, 8).addBox(-4.5F, -10.0F, -6.5F, 1.0F, 2.0F, 1.0F), PartPose.rotation(-0.1396263F, 0.0F, 0.0F)
      );
      head.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(41, 8).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 4.0F, 9.0F), PartPose.ZERO);
      head.addOrReplaceChild(
         "helmet_r",
         CubeListBuilder.create().texOffs(21, 13).addBox(-6.5F, -3.0F, -4.5F, 1.0F, 5.0F, 9.0F),
         PartPose.rotation(0.0F, 0.0F, (float) (Math.PI / 6))
      );
      head.addOrReplaceChild(
         "helmet_l",
         CubeListBuilder.create().texOffs(21, 13).mirror().addBox(5.5F, -3.0F, -4.5F, 1.0F, 5.0F, 9.0F).mirror(false),
         PartPose.rotation(0.0F, 0.0F, (float) (-Math.PI / 6))
      );
      head.addOrReplaceChild(
         "helmet_b",
         CubeListBuilder.create().texOffs(41, 21).addBox(-4.5F, -3.0F, 5.5F, 9.0F, 5.0F, 1.0F),
         PartPose.rotation((float) (Math.PI / 6), 0.0F, 0.0F)
      );
      head.addOrReplaceChild("capsthingy", CubeListBuilder.create().texOffs(21, 0).addBox(-4.5F, -6.0F, -6.5F, 9.0F, 1.0F, 2.0F), PartPose.ZERO);
      head.addOrReplaceChild(
         "flap_r",
         CubeListBuilder.create().texOffs(59, 10).addBox(-10.0F, -2.0F, -1.0F, 3.0F, 3.0F, 1.0F),
         PartPose.rotation(0.0F, (float) (-Math.PI / 6), (float) (Math.PI / 6))
      );
      head.addOrReplaceChild(
         "flap_l",
         CubeListBuilder.create().texOffs(59, 10).mirror().addBox(7.0F, -2.0F, -1.0F, 3.0F, 3.0F, 1.0F).mirror(false),
         PartPose.rotation(0.0F, (float) (Math.PI / 6), (float) (-Math.PI / 6))
      );
      head.addOrReplaceChild(
         "gemornament", CubeListBuilder.create().texOffs(68, 11).addBox(-1.5F, -9.0F, -7.0F, 3.0F, 3.0F, 2.0F), PartPose.rotation(-0.1396263F, 0.0F, 0.0F)
      );
      head.addOrReplaceChild(
         "gem", CubeListBuilder.create().texOffs(72, 8).addBox(-1.0F, -8.5F, -7.5F, 2.0F, 2.0F, 1.0F), PartPose.rotation(-0.1396263F, 0.0F, 0.0F)
      );
      if (outer) {
         body.addOrReplaceChild("belt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO);
         body.addOrReplaceChild("belt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO);
         body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -4.0F, 8.0F, 7.0F, 2.0F), PartPose.ZERO);
         body.addOrReplaceChild(
            "scroll", CubeListBuilder.create().texOffs(34, 27).addBox(-2.0F, 9.5F, 4.0F, 8.0F, 3.0F, 3.0F), PartPose.rotation(0.0F, 0.0F, 0.1919862F)
         );
         body.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8.0F, 11.0F, 2.0F), PartPose.ZERO);
         body.addOrReplaceChild(
            "book", CubeListBuilder.create().texOffs(100, 8).addBox(1.0F, -0.3F, 4.0F, 5.0F, 7.0F, 2.0F), PartPose.rotation(0.0F, 0.0F, 0.7679449F)
         );
         body.addOrReplaceChild("mbelt", CubeListBuilder.create(), PartPose.ZERO);
         body.addOrReplaceChild("mbelt_l", CubeListBuilder.create(), PartPose.ZERO);
         body.addOrReplaceChild("mbelt_r", CubeListBuilder.create(), PartPose.ZERO);
      } else {
         body.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8.0F, 4.0F, 1.0F), PartPose.ZERO);
         body.addOrReplaceChild("mbelt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO);
         body.addOrReplaceChild("mbelt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO);
         body.addOrReplaceChild("belt_r", CubeListBuilder.create(), PartPose.ZERO);
         body.addOrReplaceChild("belt_l", CubeListBuilder.create(), PartPose.ZERO);
         body.addOrReplaceChild("chestplate", CubeListBuilder.create(), PartPose.ZERO);
         body.addOrReplaceChild("scroll", CubeListBuilder.create(), PartPose.ZERO);
         body.addOrReplaceChild("backplate", CubeListBuilder.create(), PartPose.ZERO);
         body.addOrReplaceChild("book", CubeListBuilder.create(), PartPose.ZERO);
      }

      rightArm.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(56, 35).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F), PartPose.ZERO);
      rightArm.addOrReplaceChild("gauntlet_r", CubeListBuilder.create().texOffs(100, 26).addBox(-3.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F), PartPose.ZERO);
      rightArm.addOrReplaceChild("gauntletstrap_r1", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F), PartPose.ZERO);
      rightArm.addOrReplaceChild("gauntletstrap_r2", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F), PartPose.ZERO);
      rightArm.addOrReplaceChild(
         "shoulderplate_rtop",
         CubeListBuilder.create().texOffs(110, 37).addBox(-5.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F),
         PartPose.rotation(0.0F, 0.0F, 0.4363323F)
      );
      rightArm.addOrReplaceChild(
         "shoulderplate_r1", CubeListBuilder.create().texOffs(110, 45).addBox(-4.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F), PartPose.rotation(0.0F, 0.0F, 0.4363323F)
      );
      rightArm.addOrReplaceChild(
         "shoulderplate_r2", CubeListBuilder.create().texOffs(94, 45).addBox(-3.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F), PartPose.rotation(0.0F, 0.0F, 0.4363323F)
      );
      rightArm.addOrReplaceChild(
         "shoulderplate_r3", CubeListBuilder.create().texOffs(94, 45).addBox(-2.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F), PartPose.rotation(0.0F, 0.0F, 0.4363323F)
      );
      leftArm.addOrReplaceChild(
         "shoulder_l", CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F).mirror(false), PartPose.ZERO
      );
      leftArm.addOrReplaceChild("gauntlet_l", CubeListBuilder.create().texOffs(114, 26).addBox(1.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F), PartPose.ZERO);
      leftArm.addOrReplaceChild(
         "gauntletstrap_l1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F).mirror(false), PartPose.ZERO
      );
      leftArm.addOrReplaceChild(
         "gauntletstrap_l2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F).mirror(false), PartPose.ZERO
      );
      leftArm.addOrReplaceChild(
         "shoulderplate_ltop",
         CubeListBuilder.create().texOffs(110, 37).mirror().addBox(3.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F).mirror(false),
         PartPose.rotation(0.0F, 0.0F, -0.4363323F)
      );
      leftArm.addOrReplaceChild(
         "shoulderplate_l1",
         CubeListBuilder.create().texOffs(110, 45).mirror().addBox(3.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F).mirror(false),
         PartPose.rotation(0.0F, 0.0F, -0.4363323F)
      );
      leftArm.addOrReplaceChild(
         "shoulderplate_l2",
         CubeListBuilder.create().texOffs(94, 45).mirror().addBox(2.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F).mirror(false),
         PartPose.rotation(0.0F, 0.0F, -0.4363323F)
      );
      leftArm.addOrReplaceChild(
         "shoulderplate_l3",
         CubeListBuilder.create().texOffs(94, 45).mirror().addBox(1.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F).mirror(false),
         PartPose.rotation(0.0F, 0.0F, -0.4363323F)
      );
      rightLeg.addOrReplaceChild(
         "legpanel_r1", CubeListBuilder.create().texOffs(0, 51).addBox(-1.0F, 0.5F, -3.5F, 3.0F, 4.0F, 1.0F), PartPose.rotation(-0.4363323F, 0.0F, 0.0F)
      );
      rightLeg.addOrReplaceChild(
         "legpanel_r2", CubeListBuilder.create().texOffs(8, 51).addBox(-1.0F, 3.5F, -2.5F, 3.0F, 4.0F, 1.0F), PartPose.rotation(-0.4363323F, 0.0F, 0.0F)
      );
      rightLeg.addOrReplaceChild(
         "legpanel_r3", CubeListBuilder.create().texOffs(0, 56).addBox(-1.0F, 6.5F, -1.5F, 3.0F, 3.0F, 1.0F), PartPose.rotation(-0.4363323F, 0.0F, 0.0F)
      );
      rightLeg.addOrReplaceChild(
         "legpanel_r4", CubeListBuilder.create().texOffs(0, 43).addBox(-3.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F), PartPose.rotation(-0.4363323F, 0.0F, 0.0F)
      );
      rightLeg.addOrReplaceChild(
         "legpanel_r5", CubeListBuilder.create().texOffs(0, 47).addBox(-3.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F), PartPose.rotation(-0.4363323F, 0.0F, 0.0F)
      );
      rightLeg.addOrReplaceChild(
         "legpanel_r6", CubeListBuilder.create().texOffs(6, 43).addBox(-3.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F), PartPose.rotation(-0.4363323F, 0.0F, 0.0F)
      );
      rightLeg.addOrReplaceChild(
         "sidepanel_r1", CubeListBuilder.create().texOffs(0, 22).addBox(-2.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, 0.4363323F)
      );
      rightLeg.addOrReplaceChild(
         "sidepanel_r2", CubeListBuilder.create().texOffs(0, 31).addBox(-1.5F, 3.5F, -2.5F, 1.0F, 3.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, 0.4363323F)
      );
      rightLeg.addOrReplaceChild(
         "sidepanel_r3", CubeListBuilder.create().texOffs(12, 31).addBox(-0.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, 0.4363323F)
      );
      rightLeg.addOrReplaceChild(
         "backpanel_r1", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F), PartPose.rotation(0.4363323F, 0.0F, 0.0F)
      );
      rightLeg.addOrReplaceChild(
         "backpanel_r2", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 2.5F, 1.5F, 5.0F, 3.0F, 1.0F), PartPose.rotation(0.4363323F, 0.0F, 0.0F)
      );
      rightLeg.addOrReplaceChild(
         "backpanel_r3", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 4.5F, 0.5F, 5.0F, 3.0F, 1.0F), PartPose.rotation(0.4363323F, 0.0F, 0.0F)
      );
      leftLeg.addOrReplaceChild(
         "backpanel_l3",
         CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 4.5F, 0.5F, 5.0F, 3.0F, 1.0F).mirror(false),
         PartPose.rotation(0.4363323F, 0.0F, 0.0F)
      );
      leftLeg.addOrReplaceChild(
         "legpanel_l1",
         CubeListBuilder.create().texOffs(0, 51).mirror().addBox(-2.0F, 0.5F, -3.5F, 3.0F, 4.0F, 1.0F).mirror(false),
         PartPose.rotation(-0.4363323F, 0.0F, 0.0F)
      );
      leftLeg.addOrReplaceChild(
         "legpanel_l2",
         CubeListBuilder.create().texOffs(8, 51).mirror().addBox(-2.0F, 3.5F, -2.5F, 3.0F, 4.0F, 1.0F).mirror(false),
         PartPose.rotation(-0.4363323F, 0.0F, 0.0F)
      );
      leftLeg.addOrReplaceChild(
         "legpanel_l3",
         CubeListBuilder.create().texOffs(0, 56).mirror().addBox(-2.0F, 6.5F, -1.5F, 3.0F, 3.0F, 1.0F).mirror(false),
         PartPose.rotation(-0.4363323F, 0.0F, 0.0F)
      );
      leftLeg.addOrReplaceChild(
         "legpanel_l4",
         CubeListBuilder.create().texOffs(0, 43).mirror().addBox(1.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F).mirror(false),
         PartPose.rotation(-0.4363323F, 0.0F, 0.0F)
      );
      leftLeg.addOrReplaceChild(
         "legpanel_l5",
         CubeListBuilder.create().texOffs(0, 47).mirror().addBox(1.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F).mirror(false),
         PartPose.rotation(-0.4363323F, 0.0F, 0.0F)
      );
      leftLeg.addOrReplaceChild(
         "legpanel_l6",
         CubeListBuilder.create().texOffs(6, 43).mirror().addBox(1.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F).mirror(false),
         PartPose.rotation(-0.4363323F, 0.0F, 0.0F)
      );
      leftLeg.addOrReplaceChild(
         "sidepanel_l1",
         CubeListBuilder.create().texOffs(0, 22).mirror().addBox(1.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F).mirror(false),
         PartPose.rotation(0.0F, 0.0F, -0.4363323F)
      );
      leftLeg.addOrReplaceChild(
         "sidepanel_l2",
         CubeListBuilder.create().texOffs(0, 31).mirror().addBox(0.5F, 3.5F, -2.5F, 1.0F, 3.0F, 5.0F).mirror(false),
         PartPose.rotation(0.0F, 0.0F, -0.4363323F)
      );
      leftLeg.addOrReplaceChild(
         "sidepanel_l3",
         CubeListBuilder.create().texOffs(12, 31).mirror().addBox(-0.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F).mirror(false),
         PartPose.rotation(0.0F, 0.0F, -0.4363323F)
      );
      leftLeg.addOrReplaceChild(
         "backpanel_l1",
         CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F).mirror(false),
         PartPose.rotation(0.4363323F, 0.0F, 0.0F)
      );
      leftLeg.addOrReplaceChild(
         "backpanel_l2",
         CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 2.5F, 1.5F, 5.0F, 3.0F, 1.0F).mirror(false),
         PartPose.rotation(0.4363323F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 128, 64);
   }

   public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
      this.applySetState(entity);
      if (entity instanceof Zombie || entity instanceof AbstractSkeleton) {
         float sin = Mth.sin(this.attackTime * (float) Math.PI);
         float sin2 = Mth.sin((1.0F - (1.0F - this.attackTime) * (1.0F - this.attackTime)) * (float) Math.PI);
         this.rightArm.zRot = 0.0F;
         this.leftArm.zRot = 0.0F;
         this.rightArm.yRot = -(0.1F - sin * 0.6F);
         this.leftArm.yRot = 0.1F - sin * 0.6F;
         this.rightArm.xRot = (float) (-Math.PI / 2);
         this.leftArm.xRot = (float) (-Math.PI / 2);
         this.rightArm.xRot -= sin * 1.2F - sin2 * 0.4F;
         this.leftArm.xRot -= sin * 1.2F - sin2 * 0.4F;
         this.rightArm.zRot = this.rightArm.zRot + (Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F);
         this.leftArm.zRot = this.leftArm.zRot - (Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F);
         this.rightArm.xRot = this.rightArm.xRot + Mth.sin(ageInTicks * 0.067F) * 0.05F;
         this.leftArm.xRot = this.leftArm.xRot - Mth.sin(ageInTicks * 0.067F) * 0.05F;
      }
   }

   private void applySetState(LivingEntity entity) {
      ItemStack headStack = entity.getItemBySlot(EquipmentSlot.HEAD);
      ItemStack chestStack = entity.getItemBySlot(EquipmentSlot.CHEST);
      ItemStack legsStack = entity.getItemBySlot(EquipmentSlot.LEGS);
      int set = 0;
      if (headStack.getItem() instanceof ItemFortressArmor) {
         set++;
      }

      if (chestStack.getItem() instanceof ItemFortressArmor) {
         set++;
      }

      if (legsStack.getItem() instanceof ItemFortressArmor) {
         set++;
      }

      boolean hasGoggles = false;
      int maskIdx = -1;
      if (headStack.getItem() instanceof ItemFortressArmor) {
         CompoundTag tag = ItemStackData.getTag(headStack);
         if (tag != null) {
            if (tag.contains("goggles")) {
               hasGoggles = true;
            }

            if (tag.contains("mask")) {
               maskIdx = tag.getInt("mask");
            }
         }
      }

      this.goggles.visible = hasGoggles;

      for (int i = 0; i < 3; i++) {
         this.mask[i].visible = maskIdx == i;
      }

      this.scroll.visible = set >= 3;
      this.book.visible = set >= 2;
      boolean s2 = set >= 2;
      boolean s3 = set >= 3;
      this.ornamentL.visible = s3;
      this.ornamentL2.visible = s3;
      this.ornamentR.visible = s3;
      this.ornamentR2.visible = s3;
      this.gemOrnament.visible = s3;
      this.gem.visible = s3;
      this.flapL.visible = s2;
      this.flapR.visible = s2;
      this.shoulderplateLtop.visible = s2;
      this.shoulderplateL1.visible = s2;
      this.shoulderplateL2.visible = s3;
      this.shoulderplateL3.visible = s3;
      this.shoulderplateRtop.visible = s2;
      this.shoulderplateR1.visible = s2;
      this.shoulderplateR2.visible = s3;
      this.shoulderplateR3.visible = s3;
      this.sidepanelR2.visible = s2;
      this.sidepanelL2.visible = s2;
      this.sidepanelR3.visible = s3;
      this.sidepanelL3.visible = s3;
   }
}
