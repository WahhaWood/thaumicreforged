package thaumcraft.entities.smart.taint.spore.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.entities.smart.taint.spore.EntityTaintSwarm;

@OnlyIn(Dist.CLIENT)
public class RenderTaintSwarm extends EntityRenderer<EntityTaintSwarm> {
   public static final ResourceLocation STEVE = ResourceLocation.withDefaultNamespace("textures/entity/steve.png");

   public RenderTaintSwarm(Context ctx) {
      super(ctx);
   }

   @OnlyIn(Dist.CLIENT)
   public void render(EntityTaintSwarm entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
   }

   public ResourceLocation getTextureLocation(EntityTaintSwarm entity) {
      return STEVE;
   }
}
