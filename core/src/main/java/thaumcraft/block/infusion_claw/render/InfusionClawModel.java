package thaumcraft.block.infusion_claw.render;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class InfusionClawModel {
   public final ModelPart[] parts = new ModelPart[8];

   @OnlyIn(Dist.CLIENT)
   public InfusionClawModel(ModelPart root) {
      for (int i = 0; i < 8; i++) {
         this.parts[i] = root.getChild("part" + i);
      }
   }

   public static InfusionClawModel create() {
      LayerDefinition def = createLayer();
      return new InfusionClawModel(def.bakeRoot());
   }

   private static LayerDefinition createLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();

      for (int i = 0; i < 8; i++) {
         root.addOrReplaceChild("part" + i, CubeListBuilder.create().texOffs(0, 8 * i).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 2.0F, 6.0F), PartPose.ZERO);
      }

      return LayerDefinition.create(mesh, 32, 64);
   }
}
