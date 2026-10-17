package thaumcraft.item.armor.ichor_gem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class ModelWings<T extends LivingEntity> extends HumanoidModel<T> {
   public final ModelPart wing1 = this.body.getChild("wing1");
   public final ModelPart wing2 = this.body.getChild("wing2");
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      ResourceLocation.fromNamespaceAndPath("thaumcraft", "ichor_gem_wings"), "main"
   );

   public ModelWings(ModelPart root) {
      super(root);
   }

   public static LayerDefinition createLayerDefinition() {
      MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(1.0F), 0.0F);
      PartDefinition root = mesh.getRoot();
      PartDefinition body = root.getChild("body");
      body.addOrReplaceChild(
         "wing1",
         CubeListBuilder.create().texOffs(16, -12).addBox(0.0F, 0.0F, 0.0F, 0.0F, 7.0F, 12.0F),
         PartPose.offsetAndRotation(-2.0F, 1.0F, 2.0F, 0.0F, -0.6108652F, 0.0F)
      );
      body.addOrReplaceChild(
         "wing2",
         CubeListBuilder.create().texOffs(16, -12).addBox(0.1F, 0.0F, 0.0F, 0.0F, 7.0F, 12.0F),
         PartPose.offsetAndRotation(2.0F, 1.0F, 2.0F, 0.0F, 0.4468043F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 32);
   }

   public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
      this.applyWingAnim(entity);
   }

   public void applyWingAnim(LivingEntity living) {
      this.wing1.yRot = -0.6108652F;
      this.wing2.yRot = 0.4468043F;
      if (living instanceof Player p && p.getAbilities().flying) {
         float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
         float t = living.tickCount + partial;
         float ry = (Mth.sin(t) + 1.0F) * (float) (Math.PI / 180.0) * 15.0F - 0.6108652F;
         this.wing1.yRot = ry;
         this.wing2.yRot = -ry;
      }
   }
}
