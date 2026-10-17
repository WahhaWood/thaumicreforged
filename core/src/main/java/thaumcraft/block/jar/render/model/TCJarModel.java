package thaumcraft.block.jar.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public final class TCJarModel {
   public static final ResourceLocation TEX_JAR = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/block/model/jar.png");
   public static final ResourceLocation TEX_VOID_JAR = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/block/model/jar_void.png");
   public static final ResourceLocation TEX_BRINE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/block/model/jar_brain.png");
   private final ModelPart core;
   private final ModelPart brine;
   private final ModelPart lid;

   @OnlyIn(Dist.CLIENT)
   private TCJarModel(ModelPart root) {
      this.core = root.getChild("core");
      this.brine = root.getChild("brine");
      this.lid = root.getChild("lid");
   }

   public static TCJarModel create() {
      return new TCJarModel(createLayer().bakeRoot());
   }

   public static LayerDefinition createLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -12.0F, -5.0F, 10.0F, 12.0F, 10.0F), PartPose.ZERO);
      root.addOrReplaceChild("brine", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -11.0F, -4.0F, 8.0F, 10.0F, 8.0F), PartPose.ZERO);
      root.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 24).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 2.0F, 6.0F), PartPose.offset(0.0F, -14.0F, 0.0F));
      return LayerDefinition.create(mesh, 64, 32);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderAll(PoseStack poseStack, VertexConsumer vc, int light, int overlay) {
      this.renderLid(poseStack, vc, light, overlay);
      this.renderCore(poseStack, vc, light, overlay);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderCore(PoseStack poseStack, VertexConsumer vc, int light, int overlay) {
      this.core.render(poseStack, vc, light, overlay);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderLid(PoseStack poseStack, VertexConsumer vc, int light, int overlay) {
      this.lid.render(poseStack, vc, light, overlay);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderBrine(PoseStack poseStack, VertexConsumer vc, int light, int overlay) {
      this.brine.render(poseStack, vc, light, overlay);
   }
}
