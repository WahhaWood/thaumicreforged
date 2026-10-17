package thaumcraft.entities.smart.taint.sheep;

import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderTaintSheep extends MobRenderer<EntityTaintSheep, SheepModel<EntityTaintSheep>> {
   public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/sheep.png");

   public RenderTaintSheep(Context ctx) {
      super(ctx, new SheepModel(ctx.bakeLayer(ModelLayers.SHEEP)), 0.7F);
      this.addLayer(new TaintSheepFurLayer(this, ctx.getModelSet()));
   }

   public ResourceLocation getTextureLocation(EntityTaintSheep entity) {
      return TEX;
   }
}
