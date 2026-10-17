package thaumcraft.entities.smart.taint.spider;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderTaintSpider extends MobRenderer<EntityTaintSpider, SpiderModel<EntityTaintSpider>> {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/taint_spider.png");

   public RenderTaintSpider(Context ctx) {
      super(ctx, new SpiderModel(ctx.bakeLayer(ModelLayers.SPIDER)), 0.5F);
      this.addLayer(new TaintSpiderEyesLayer(this));
   }

   public ResourceLocation getTextureLocation(EntityTaintSpider entity) {
      return TEX;
   }

   @OnlyIn(Dist.CLIENT)
   protected void scale(EntityTaintSpider entity, PoseStack poseStack, float partialTick) {
      float s = entity.getScale();
      poseStack.scale(s, s * 1.25F, s);
   }

   protected float getFlipDegrees(EntityTaintSpider entity) {
      return 180.0F;
   }
}
