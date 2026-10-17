package thaumcraft.entities.smart.taint.spider;

import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TaintSpiderEyesLayer extends EyesLayer<EntityTaintSpider, SpiderModel<EntityTaintSpider>> {
   private static final RenderType EYES = RenderType.eyes(ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/taint_spider_eyes.png"));

   public TaintSpiderEyesLayer(RenderLayerParent<EntityTaintSpider, SpiderModel<EntityTaintSpider>> parent) {
      super(parent);
   }

   public RenderType renderType() {
      return EYES;
   }
}
