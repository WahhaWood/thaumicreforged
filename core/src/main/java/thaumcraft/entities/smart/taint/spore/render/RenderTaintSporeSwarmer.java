package thaumcraft.entities.smart.taint.spore.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.entities.smart.taint.spore.EntityTaintSporeSwarmer;

@OnlyIn(Dist.CLIENT)
public class RenderTaintSporeSwarmer extends MobRenderer<EntityTaintSporeSwarmer, ModelTaintSporeSwarmer> {
   public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/taint_spore.png");

   public RenderTaintSporeSwarmer(Context ctx) {
      super(ctx, new ModelTaintSporeSwarmer(ctx.bakeLayer(ModelTaintSporeSwarmer.LAYER_LOCATION)), 0.25F);
   }

   protected float getFlipDegrees(EntityTaintSporeSwarmer entity) {
      return 0.0F;
   }

   public ResourceLocation getTextureLocation(EntityTaintSporeSwarmer entity) {
      return TEXTURE;
   }

   @OnlyIn(Dist.CLIENT)
   protected RenderType getRenderType(EntityTaintSporeSwarmer entity, boolean bodyVisible, boolean translucent, boolean glowing) {
      return RenderType.entityTranslucent(this.getTextureLocation(entity));
   }

   @OnlyIn(Dist.CLIENT)
   public void render(EntityTaintSporeSwarmer entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      ((ModelTaintSporeSwarmer)this.model).setEntity(entity);
      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
   }
}
