package thaumcraft.entities.smart.cultist_portal.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import thaumcraft.entities.smart.cultist_portal.EntityCultistPortal;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RenderCultistPortal extends EntityRenderer<EntityCultistPortal> {
   public static final ResourceLocation portaltex = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/cultist_portal.png");

   public RenderCultistPortal(Context ctx) {
      super(ctx);
      this.shadowRadius = 0.1F;
      this.shadowStrength = 0.5F;
   }

   @OnlyIn(Dist.CLIENT)
   public void render(EntityCultistPortal e, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      if (Minecraft.getInstance().getCameraEntity() instanceof Player) {
         long n6 = System.nanoTime() / 50000000L;
         float n7 = 1.5F;
         int n8 = (int)Math.min(50.0F, e.tickCount + partialTick);
         if (e.hurtTime > 0) {
            double sin = Math.sin(e.hurtTime * 72.0 * Math.PI / 180.0);
            n7 -= (float)(sin / 4.0);
            n8 += (int)(6.0 * sin);
         }

         if (e.pulse > 0) {
            double sin2 = Math.sin(e.pulse * 36.0 * Math.PI / 180.0);
            n7 += (float)(sin2 / 4.0);
            n8 += (int)(12.0 * sin2);
         }

         float n9 = n8 / 50.0F * 1.3F;
         float n10 = (1.0F - e.getHealth() / e.getMaxHealth()) / 3.0F;
         float n11 = Mth.sin(e.tickCount / (5.0F - 12.0F * n10)) * n10 + n10;
         float n12 = Mth.sin(e.tickCount / (6.0F - 15.0F * n10)) * n10 + n10;
         float n13 = 1.0F - n11;
         float n14 = n7 - n11 / 4.0F;
         float n15 = n9 - n12 / 3.0F;
         float u0 = (15 - (int)n6 % 16) / 16.0F;
         float u1 = u0 + 0.0625F;
         float v0 = 0.0F;
         float v1 = 1.0F;
         poseStack.pushPose();
         poseStack.translate(0.0, e.getBbHeight() / 2.0F, 0.0);
         poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
         poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
         VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(portaltex));
         int fullBright = 15728880;
         vc.addVertex(poseStack.last().pose(), -n15, -n14, 0.0F)
            .setColor(1.0F, 1.0F, 1.0F, n13)
            .setUv(u1, v0)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(fullBright)
            .setNormal(0.0F, 0.0F, -1.0F);
         vc.addVertex(poseStack.last().pose(), -n15, n14, 0.0F)
            .setColor(1.0F, 1.0F, 1.0F, n13)
            .setUv(u1, v1)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(fullBright)
            .setNormal(0.0F, 0.0F, -1.0F);
         vc.addVertex(poseStack.last().pose(), n15, n14, 0.0F)
            .setColor(1.0F, 1.0F, 1.0F, n13)
            .setUv(u0, v1)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(fullBright)
            .setNormal(0.0F, 0.0F, -1.0F);
         vc.addVertex(poseStack.last().pose(), n15, -n14, 0.0F)
            .setColor(1.0F, 1.0F, 1.0F, n13)
            .setUv(u0, v0)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(fullBright)
            .setNormal(0.0F, 0.0F, -1.0F);
         poseStack.popPose();
         super.render(e, entityYaw, partialTick, poseStack, buffer, packedLight);
      }
   }

   public ResourceLocation getTextureLocation(EntityCultistPortal entity) {
      return portaltex;
   }
}
