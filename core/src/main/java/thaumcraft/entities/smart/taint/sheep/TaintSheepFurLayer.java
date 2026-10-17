package thaumcraft.entities.smart.taint.sheep;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TaintSheepFurLayer extends RenderLayer<EntityTaintSheep, SheepModel<EntityTaintSheep>> {
   public static final ResourceLocation FUR_TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/sheep_fur.png");
   private final SheepFurModel<EntityTaintSheep> furModel;

   public TaintSheepFurLayer(RenderLayerParent<EntityTaintSheep, SheepModel<EntityTaintSheep>> parent, EntityModelSet modelSet) {
      super(parent);
      this.furModel = new SheepFurModel(modelSet.bakeLayer(ModelLayers.SHEEP_FUR));
   }

   public void render(
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      EntityTaintSheep entity,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      if (!entity.isSheared() && !entity.isInvisible()) {
         ((SheepModel)this.getParentModel()).copyPropertiesTo(this.furModel);
         this.furModel.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
         this.furModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
         VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(FUR_TEX));
         this.furModel.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, -1);
      }
   }
}
