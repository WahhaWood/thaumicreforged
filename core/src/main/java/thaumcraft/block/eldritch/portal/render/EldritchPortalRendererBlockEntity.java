package thaumcraft.block.eldritch.portal.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.block.eldritch.portal.EldritchPortalBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class EldritchPortalRendererBlockEntity implements BlockEntityRenderer<EldritchPortalBlockEntity> {
   public static final ResourceLocation portaltex = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/eldritch_portal.png");

   public EldritchPortalRendererBlockEntity(Context ctx) {
   }

   @OnlyIn(Dist.CLIENT)
   public void render(EldritchPortalBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      if (Minecraft.getInstance().getCameraEntity() instanceof Player) {
         long t = System.nanoTime() / 50000000L;
         int n6 = (int)Math.min(30.0F, be.opencount + partialTick);
         float n7 = (int)Math.min(5.0F, be.opencount + partialTick) / 5.0F;
         float n8 = n6 / 30.0F;
         float u0 = (int)t % 16 / 16.0F;
         float u1 = u0 + 0.0625F;
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.depthMask(false);
         VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(portaltex));
         // Билборд развёрнут на камеру: вплотную он закрывает весь экран, и
         // виден только непрозрачный чёрный центр вихря — «экран чернеет».
         // Раньше я просто не рисовал портал ближе 2.5 блока, но тогда он
         // пропадал прямо перед игроком. Теперь портал виден всегда, а
         // непрозрачность плавно падает у самой камеры: издалека обычный,
         // вплотную — полупрозрачный, экран не заливает.
         Camera closeCam = Minecraft.getInstance().gameRenderer.getMainCamera();
         BlockPos self = be.getBlockPos();
         float camDist = (float)Math.sqrt(
            closeCam.getPosition().distanceToSqr(self.getX() + 0.5, self.getY() + 0.5, self.getZ() + 0.5));
         float opacity = Mth.clamp((camDist - 1.2F) / 1.8F, 0.0F, 1.0F);
         int alpha = (int)(opacity * 255.0F);

         poseStack.pushPose();
         // Поза блочного рендерера в 1.21 начинается от УГЛА блока, а в 1.7.10
         // рендерер сам прибавлял 0.5 (px = x + 0.5) — поэтому сдвиг нужен,
         // иначе портал уезжает на полблока и стоит не по центру алтаря.
         poseStack.translate(0.5, 0.5, 0.5);
         Camera cam = Minecraft.getInstance().gameRenderer.getMainCamera();
         Quaternionf q = new Quaternionf(cam.rotation());
         q.mul(new Quaternionf().rotationZ((float)Math.toRadians(90.0)));
         Vector3f v1 = new Vector3f(-1.0F, -1.0F, 0.0F).rotate(q).mul(n7, n8, n7);
         Vector3f v2 = new Vector3f(1.0F, -1.0F, 0.0F).rotate(q).mul(n7, n8, n7);
         Vector3f v3 = new Vector3f(1.0F, 1.0F, 0.0F).rotate(q).mul(n7, n8, n7);
         Vector3f v4 = new Vector3f(-1.0F, 1.0F, 0.0F).rotate(q).mul(n7, n8, n7);
         Pose last = poseStack.last();
         Matrix4f mat = last.pose();
         Matrix3f nmat = last.normal();
         int fullBright = 15728880;
         vc.addVertex(mat, v1.x(), v1.y(), v1.z())
            .setColor(255, 0, 255, alpha)
            .setUv(u0, 1.0F)
            .setOverlay(packedOverlay)
            .setUv2(fullBright & 65535, fullBright >> 16)
            .setNormal(0.0F, 0.0F, -1.0F);
         vc.addVertex(mat, v2.x(), v2.y(), v2.z())
            .setColor(255, 0, 255, alpha)
            .setUv(u1, 1.0F)
            .setOverlay(packedOverlay)
            .setUv2(fullBright & 65535, fullBright >> 16)
            .setNormal(0.0F, 0.0F, -1.0F);
         vc.addVertex(mat, v3.x(), v3.y(), v3.z())
            .setColor(255, 0, 255, alpha)
            .setUv(u1, 0.0F)
            .setOverlay(packedOverlay)
            .setUv2(fullBright & 65535, fullBright >> 16)
            .setNormal(0.0F, 0.0F, -1.0F);
         vc.addVertex(mat, v4.x(), v4.y(), v4.z())
            .setColor(255, 0, 255, alpha)
            .setUv(u0, 0.0F)
            .setOverlay(packedOverlay)
            .setUv2(fullBright & 65535, fullBright >> 16)
            .setNormal(0.0F, 0.0F, -1.0F);
         poseStack.popPose();
         RenderSystem.depthMask(true);
         RenderSystem.disableBlend();
      }
   }

   public int getViewDistance() {
      return 96;
   }

   /** The block entity's own box: NeoForge 21.1 asks the renderer, not the block entity. */
   @Override
   public AABB getRenderBoundingBox(EldritchPortalBlockEntity blockEntity) {
      return blockEntity.getRenderBoundingBox();
   }
}
