package thaumcraft.entities.smart.taint.spore.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.entities.smart.taint.spore.EntityTaintSpore;

@OnlyIn(Dist.CLIENT)
public class RenderTaintSpore extends MobRenderer<EntityTaintSpore, ModelTaintSpore> {
   public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/taint_spore.png");

   public RenderTaintSpore(Context ctx) {
      super(ctx, new ModelTaintSpore(ctx.bakeLayer(ModelTaintSpore.LAYER_LOCATION)), 0.25F);
   }

   public ResourceLocation getTextureLocation(EntityTaintSpore entity) {
      return TEXTURE;
   }

   @OnlyIn(Dist.CLIENT)
   protected RenderType getRenderType(EntityTaintSpore entity, boolean bodyVisible, boolean translucent, boolean glowing) {
      return RenderType.entityTranslucent(this.getTextureLocation(entity));
   }

   @OnlyIn(Dist.CLIENT)
   protected void scale(EntityTaintSpore entity, PoseStack poseStack, float partialTickTime) {
      float displaySize = entity.displaySize;
      if (entity.displaySize < entity.getSporeSize()) {
         displaySize += 0.02F * partialTickTime;
      }

      float n2 = -0.12F;
      float n3 = 0.025F * Mth.sin(entity.tickCount * 0.075F);
      poseStack.scale(n2 * displaySize - n3, n2 * displaySize + n3, n2 * displaySize - n3);
   }

   protected float getFlipDegrees(EntityTaintSpore entity) {
      return 0.0F;
   }
}
