package thaumcraft.entities.smart.thaumic_slime;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class RenderThaumicSlime extends MobRenderer<EntityThaumicSlime, SlimeModel<EntityThaumicSlime>> {
   public static final ResourceLocation SLIME_TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/tslime.png");

   public RenderThaumicSlime(Context ctx) {
      super(ctx, new SlimeModel(ctx.bakeLayer(ModelLayers.SLIME)), 0.25F);
      this.addLayer(new RenderThaumicSlime.ThaumicSlimeOuterLayer(this, ctx));
   }

   @NotNull
   public ResourceLocation getTextureLocation(@NotNull EntityThaumicSlime entity) {
      return SLIME_TEXTURE;
   }

   @OnlyIn(Dist.CLIENT)
   protected void scale(EntityThaumicSlime slime, PoseStack poseStack, float partialTicks) {
      float f1 = Mth.sqrt(slime.getSlimeSize());
      float f2 = (slime.prevSquishFactor + (slime.squishFactor - slime.prevSquishFactor) * partialTicks) / (f1 * 0.25F + 1.0F);
      float f3 = 1.0F / (f2 + 1.0F);
      poseStack.scale(f3 * f1 + 0.1F, 1.0F / f3 * f1 + 0.1F, f3 * f1 + 0.1F);
      this.shadowRadius = 0.25F * f1;
   }

   public static class ThaumicSlimeOuterLayer extends RenderLayer<EntityThaumicSlime, SlimeModel<EntityThaumicSlime>> {
      private final SlimeModel<EntityThaumicSlime> model;

      public ThaumicSlimeOuterLayer(RenderLayerParent<EntityThaumicSlime, SlimeModel<EntityThaumicSlime>> parent, Context ctx) {
         super(parent);
         this.model = new SlimeModel(ctx.bakeLayer(ModelLayers.SLIME_OUTER));
      }

      @OnlyIn(Dist.CLIENT)
      public void render(
         @NotNull PoseStack poseStack,
         @NotNull MultiBufferSource buffer,
         int packedLight,
         EntityThaumicSlime entity,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         if (!entity.isInvisible()) {
            ((SlimeModel)this.getParentModel()).copyPropertiesTo(this.model);
            this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
            this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity)));
            this.model.renderToBuffer(poseStack, vc, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), -1);
         }
      }
   }
}
