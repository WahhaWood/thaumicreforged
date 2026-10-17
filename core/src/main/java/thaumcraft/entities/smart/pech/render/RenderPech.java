package thaumcraft.entities.smart.pech.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import thaumcraft.entities.smart.pech.EntityPech;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RenderPech extends MobRenderer<EntityPech, ModelPech<EntityPech>> {
   public static final ResourceLocation[] SKIN = new ResourceLocation[]{
      ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/pech_forage.png"),
      ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/pech_thaum.png"),
      ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/pech_stalker.png")
   };

   public RenderPech(Context ctx) {
      super(ctx, new ModelPech(ctx.bakeLayer(ModelPech.LAYER_LOCATION)), 0.5F);
      this.addLayer(new RenderPech.PechHeldItemLayer(this, ctx.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(EntityPech entity) {
      int t = entity.getPechType();
      if (t < 0 || t >= SKIN.length) {
         t = 0;
      }

      return SKIN[t];
   }

   public class PechHeldItemLayer extends RenderLayer<EntityPech, ModelPech<EntityPech>> {
      private final ItemInHandRenderer handRenderer;

      public PechHeldItemLayer(RenderLayerParent<EntityPech, ModelPech<EntityPech>> parent, ItemInHandRenderer handRenderer) {
         super(parent);
         this.handRenderer = handRenderer;
      }

      @OnlyIn(Dist.CLIENT)
      public void render(
         PoseStack pose,
         MultiBufferSource buffer,
         int light,
         EntityPech entity,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         ItemStack held = entity.getMainHandItem();
         if (!held.isEmpty()) {
            pose.pushPose();
            if (entity.isBaby()) {
               pose.translate(0.0F, 0.625F, 0.0F);
               pose.mulPose(Axis.XN.rotationDegrees(20.0F));
               pose.scale(0.5F, 0.5F, 0.5F);
            }

            ((ModelPech)this.getParentModel()).rightArm.translateAndRotate(pose);
            pose.translate(-0.0625F, 0.375F, 0.0F);
            pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
            pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            pose.translate(0.0F, 0.125F, -0.125F);
            this.handRenderer.renderItem(entity, held, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, pose, buffer, light);
            pose.popPose();
         }
      }
   }
}
