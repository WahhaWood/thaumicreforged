package thaumcraft.entities.smart.pech_blast;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RenderPechBlast extends EntityRenderer<EntityPechBlast> {
   public static final ResourceLocation STEVE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/model/steve.png");

   public RenderPechBlast(Context ctx) {
      super(ctx);
      this.shadowRadius = 0.1F;
   }

   @OnlyIn(Dist.CLIENT)
   public void render(EntityPechBlast entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
   }

   public ResourceLocation getTextureLocation(EntityPechBlast entity) {
      return STEVE;
   }
}
