package thaumcraft.block.crucible;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.block.jar.render.util.JarRenderUtil;

public class CrucibleRenderer implements BlockEntityRenderer<CrucibleBlockEntity> {
   private static final ResourceLocation TEX_LABEL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/label.png");
   private static final Direction[] SIDES = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
   private BakedModel model;

   public CrucibleRenderer(Context context) {
   }

   @Override
   public void render(
      @NotNull CrucibleBlockEntity blockEntity,
      float pt,
      @NotNull PoseStack pose,
      @NotNull MultiBufferSource bufferSource,
      int brightnessOverride,
      int overlayTexture
   ) {
      if (blockEntity.tank.getFluidAmount() > 0) {
         if (this.model == null) {
            this.model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(Thaumcraft.location("block/crucible_fluid")));
         }

         float aspectIntensity = Mth.clamp(blockEntity.tagAmount() / 50.0F, 0.0F, 1.0F);
         float r = Mth.lerp(aspectIntensity, 0.28F, 0.78F);
         float g = Mth.lerp(aspectIntensity, 0.55F, 0.10F);
         float b = Mth.lerp(aspectIntensity, 0.88F, 0.90F);

         pose.pushPose();
         pose.scale(1.0F, blockEntity.getFluidHeight(), 1.0F);
         VertexConsumer buffer = bufferSource.getBuffer(Sheets.translucentCullBlockSheet());
         Minecraft.getInstance()
            .getBlockRenderer()
            .getModelRenderer()
            .renderModel(
               pose.last(),
               buffer,
               null,
               this.model,
               r,
               g,
               b,
               brightnessOverride,
               overlayTexture,
               ModelData.EMPTY,
               null
            );
         pose.popPose();
      }

      AspectList aspects = blockEntity.aspects;
      if (aspects != null && aspects.size() > 0) {
         renderSideLabels(aspects, pose, bufferSource, brightnessOverride, overlayTexture);
         renderFloatingAspects(blockEntity, pose, bufferSource, brightnessOverride, overlayTexture);
      }
   }

   private static void renderSideLabels(AspectList aspects, PoseStack pose, MultiBufferSource bufferSource, int light, int overlay) {
      Aspect[] list = aspects.getAspectsSorted();
      if (list == null || list.length == 0) {
         return;
      }

      for (int i = 0; i < SIDES.length; i++) {
         Aspect aspect = list[i % list.length];
         int amt = aspects.getAmount(aspect);
         renderSideLabel(pose, bufferSource, aspect, amt, SIDES[i], light, overlay);
      }
   }

   private static void renderSideLabel(PoseStack pose, MultiBufferSource bufferSource, Aspect aspect, int amount, Direction dir, int light, int overlay) {
      pose.pushPose();
      pose.translate(0.5, 0.0, 0.5);
      switch (dir) {
         case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(180.0F));
         case EAST -> pose.mulPose(Axis.YP.rotationDegrees(270.0F));
         case WEST -> pose.mulPose(Axis.YP.rotationDegrees(90.0F));
         default -> {}
      }
      pose.translate(0.0, 0.46F, -0.505F);
      pose.scale(0.24F, 0.24F, 0.24F);
      VertexConsumer vcLabel = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEX_LABEL));
      JarRenderUtil.quad(
         pose, vcLabel,
         -0.5F, -0.5F, 0.0F,
         0.5F, 0.5F, 0.0F,
         0.0F, 0.0F, 1.0F, 1.0F,
         light, OverlayTexture.NO_OVERLAY,
         1.0F, 1.0F, 1.0F, 1.0F,
         0.0F, 0.0F, 1.0F
      );
      renderAspectQuad(pose, bufferSource, aspect, overlay);

      net.minecraft.client.gui.Font font = Minecraft.getInstance().font;
      pose.pushPose();
      pose.translate(0.0, -0.65F, -0.01F);
      pose.scale(0.045F, -0.045F, 0.045F);
      String text = String.valueOf(amount);
      int w = font.width(text);
      font.drawInBatch(text, -w / 2.0F, 0, 0xFFE080, false, pose.last().pose(), bufferSource, net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 15728880);
      pose.popPose();

      pose.popPose();
   }

   private static void renderAspectQuad(PoseStack pose, MultiBufferSource bufferSource, Aspect aspect, int overlay) {
      float half = 0.385F;
      float iconZ = -0.003F;
      VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucent(aspect.getImage()));
      Matrix4f matrix = pose.last().pose();
      vc.addVertex(matrix, -half, -half, iconZ)
         .setColor(1.0F, 1.0F, 1.0F, 0.95F)
         .setUv(0.0F, 1.0F)
         .setOverlay(overlay)
         .setUv2(240, 240)
         .setNormal(0.0F, 0.0F, -1.0F);
      vc.addVertex(matrix, -half, half, iconZ)
         .setColor(1.0F, 1.0F, 1.0F, 0.95F)
         .setUv(0.0F, 0.0F)
         .setOverlay(overlay)
         .setUv2(240, 240)
         .setNormal(0.0F, 0.0F, -1.0F);
      vc.addVertex(matrix, half, half, iconZ)
         .setColor(1.0F, 1.0F, 1.0F, 0.95F)
         .setUv(1.0F, 0.0F)
         .setOverlay(overlay)
         .setUv2(240, 240)
         .setNormal(0.0F, 0.0F, -1.0F);
      vc.addVertex(matrix, half, -half, iconZ)
         .setColor(1.0F, 1.0F, 1.0F, 0.95F)
         .setUv(1.0F, 1.0F)
         .setOverlay(overlay)
         .setUv2(240, 240)
         .setNormal(0.0F, 0.0F, -1.0F);
   }

   private static void renderFloatingAspects(CrucibleBlockEntity blockEntity, PoseStack pose, MultiBufferSource bufferSource, int light, int overlay) {
      AspectList al = blockEntity.aspects;
      if (al == null || al.size() == 0) {
         return;
      }
      Aspect[] list = al.getAspectsSorted();
      int count = list.length;
      float fluidY = Math.max(0.68F, blockEntity.getFluidHeight() + 0.10F);
      long time = System.currentTimeMillis();
      float baseBob = (float)Math.sin(time / 400.0) * 0.02F;

      for (int i = 0; i < count; i++) {
         Aspect aspect = list[i];
         int amt = al.getAmount(aspect);
         float x;
         float z;
         if (count == 1) {
            x = 0.5F;
            z = 0.5F;
         } else {
            float radius = Math.min(0.24F, 0.14F + count * 0.015F);
            double angle = (2.0 * Math.PI * i / count) + (time % 8000L) / 8000.0 * 2.0 * Math.PI;
            x = 0.5F + (float)Math.cos(angle) * radius;
            z = 0.5F + (float)Math.sin(angle) * radius;
         }
         float y = fluidY + baseBob + (float)Math.sin(time / 300.0 + i) * 0.015F;

         pose.pushPose();
         pose.translate(x, y, z);
         pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
         pose.scale(0.18F, 0.18F, 0.18F);

         renderAspectBillboard(pose, bufferSource, aspect, overlay);

         net.minecraft.client.gui.Font font = Minecraft.getInstance().font;
         pose.pushPose();
         pose.translate(0.0F, -0.65F, 0.01F);
         pose.scale(0.04F, -0.04F, 0.04F);
         String text = String.valueOf(amt);
         int w = font.width(text);
         font.drawInBatch(text, -w / 2.0F, 0, 0xFFFFFF, false, pose.last().pose(), bufferSource, net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 15728880);
         pose.popPose();

         pose.popPose();
      }
   }

   private static void renderAspectBillboard(PoseStack pose, MultiBufferSource bufferSource, Aspect aspect, int overlay) {
      float half = 0.5F;
      VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucent(aspect.getImage()));
      Matrix4f matrix = pose.last().pose();
      vc.addVertex(matrix, -half, -half, 0.0F)
         .setColor(1.0F, 1.0F, 1.0F, 0.95F)
         .setUv(0.0F, 1.0F)
         .setOverlay(overlay)
         .setUv2(240, 240)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(matrix, -half, half, 0.0F)
         .setColor(1.0F, 1.0F, 1.0F, 0.95F)
         .setUv(0.0F, 0.0F)
         .setOverlay(overlay)
         .setUv2(240, 240)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(matrix, half, half, 0.0F)
         .setColor(1.0F, 1.0F, 1.0F, 0.95F)
         .setUv(1.0F, 0.0F)
         .setOverlay(overlay)
         .setUv2(240, 240)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(matrix, half, -half, 0.0F)
         .setColor(1.0F, 1.0F, 1.0F, 0.95F)
         .setUv(1.0F, 1.0F)
         .setOverlay(overlay)
         .setUv2(240, 240)
         .setNormal(0.0F, 1.0F, 0.0F);
   }
}
