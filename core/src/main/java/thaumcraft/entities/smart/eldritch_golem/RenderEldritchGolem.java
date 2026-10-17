package thaumcraft.entities.smart.eldritch_golem;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.entities.smart.eldritch_golem.render.ModelEldritchGolem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RenderEldritchGolem extends MobRenderer<EntityEldritchGolem, ModelEldritchGolem> {
   public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/eldritch_golem.png");

   public RenderEldritchGolem(Context ctx) {
      super(ctx, new ModelEldritchGolem(ctx.bakeLayer(ModelEldritchGolem.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(EntityEldritchGolem entity) {
      return TEXTURE;
   }

   @OnlyIn(Dist.CLIENT)
   protected void scale(EntityEldritchGolem entity, PoseStack poseStack, float partialTickTime) {
      poseStack.scale(2.15F, 2.15F, 2.15F);
   }

   @OnlyIn(Dist.CLIENT)
   protected RenderType getRenderType(EntityEldritchGolem entity, boolean bodyVisible, boolean translucent, boolean glowing) {
      return RenderType.entityTranslucent(this.getTextureLocation(entity));
   }
}
