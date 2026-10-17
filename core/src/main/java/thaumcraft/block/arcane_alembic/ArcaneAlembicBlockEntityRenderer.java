package thaumcraft.block.arcane_alembic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.block.jar.render.util.JarRenderUtil;

public class ArcaneAlembicBlockEntityRenderer implements BlockEntityRenderer<ArcaneAlembicBlockEntity> {
   private static final ResourceLocation TEX_LABEL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/label.png");
   private static final float LABEL_SCALE = 0.27F;
   private static final float ICON_SCALE = 0.208F;
   private static final float LABEL_Y = 0.468F;
   private static final float LABEL_Z = -0.409F;
   private static final float ICON_FRONT_OFFSET = 8.1E-4F;

   private static final Direction[] HORIZONTALS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

   public ArcaneAlembicBlockEntityRenderer(Context ctx) {
   }

   public void render(
      @NotNull ArcaneAlembicBlockEntity alembic,
      float partialTicks,
      @NotNull PoseStack poseStack,
      @NotNull MultiBufferSource buffers,
      int packedLight,
      int packedOverlay
   ) {
      if (alembic.aspect != null && alembic.amount > 0) {
         renderFluid(alembic, poseStack, buffers);
      }

      Aspect aspectToRender = alembic.aspectFilter != null ? alembic.aspectFilter : (alembic.amount > 0 ? alembic.aspect : null);
      if (aspectToRender != null) {
         for (Direction dir : HORIZONTALS) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.0, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(getLabelRotation(dir)));
            renderLabelWithAspect(poseStack, buffers, aspectToRender, packedLight, packedOverlay);
            poseStack.popPose();
         }
      }
   }

   private static void renderFluid(@NotNull ArcaneAlembicBlockEntity alembic, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers) {
      if (alembic.aspect == null || alembic.amount <= 0) {
         return;
      }
      float fill = net.minecraft.util.Mth.clamp((float)alembic.amount / alembic.maxAmount, 0.0F, 1.0F);
      float halfSize = 0.23F;
      float maxHeight = 0.48F;
      float height = Math.max(0.04F, maxHeight * fill);
      float x0 = 0.5F - halfSize;
      float z0 = 0.5F - halfSize;
      float x1 = 0.5F + halfSize;
      float z1 = 0.5F + halfSize;
      float y0 = 0.14F;
      float y1 = y0 + height;
      JarRenderUtil.renderFluidBox(poseStack, buffers, x0, y0, z0, x1, y1, z1, alembic.aspect.getColor(), 0.85F, 15728880, OverlayTexture.NO_OVERLAY);
   }

   private static void renderLabelWithAspect(PoseStack poseStack, MultiBufferSource buffers, Aspect aspect, int packedLight, int packedOverlay) {
      poseStack.pushPose();
      poseStack.translate(0.0, 0.468F, -0.409F);
      poseStack.scale(0.27F, 0.27F, 0.27F);
      VertexConsumer labelConsumer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEX_LABEL));
      JarRenderUtil.quad(
         poseStack,
         labelConsumer,
         -0.5F,
         -0.5F,
         0.0F,
         0.5F,
         0.5F,
         0.0F,
         0.0F,
         0.0F,
         1.0F,
         1.0F,
         packedLight,
         OverlayTexture.NO_OVERLAY,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         0.0F,
         0.0F,
         1.0F
      );
      renderAspectIcon(poseStack, buffers, aspect, packedOverlay);
      poseStack.popPose();
   }

   private static void renderAspectIcon(PoseStack poseStack, MultiBufferSource buffers, Aspect aspect, int packedOverlay) {
      float half = 0.38518518F;
      float iconZ = -0.003F;
      VertexConsumer iconConsumer = buffers.getBuffer(RenderType.entityTranslucent(aspect.getImage()));
      Pose pose = poseStack.last();
      Matrix4f matrix = pose.pose();
      iconConsumer.addVertex(matrix, -half, -half, iconZ)
         .setColor(1.0F, 1.0F, 1.0F, 0.95F)
         .setUv(0.0F, 1.0F)
         .setOverlay(packedOverlay)
         .setUv2(240, 240)
         .setNormal(0.0F, 0.0F, -1.0F);
      iconConsumer.addVertex(matrix, -half, half, iconZ)
         .setColor(1.0F, 1.0F, 1.0F, 0.95F)
         .setUv(0.0F, 0.0F)
         .setOverlay(packedOverlay)
         .setUv2(240, 240)
         .setNormal(0.0F, 0.0F, -1.0F);
      iconConsumer.addVertex(matrix, half, half, iconZ)
         .setColor(1.0F, 1.0F, 1.0F, 0.95F)
         .setUv(1.0F, 0.0F)
         .setOverlay(packedOverlay)
         .setUv2(240, 240)
         .setNormal(0.0F, 0.0F, -1.0F);
      iconConsumer.addVertex(matrix, half, -half, iconZ)
         .setColor(1.0F, 1.0F, 1.0F, 0.95F)
         .setUv(1.0F, 1.0F)
         .setOverlay(packedOverlay)
         .setUv2(240, 240)
         .setNormal(0.0F, 0.0F, -1.0F);
   }

   private static float getLabelRotation(Direction facing) {
      return switch (facing) {
         case SOUTH -> 180.0F;
         case EAST -> 270.0F;
         case WEST -> 90.0F;
         default -> 0.0F;
      };
   }

   public boolean shouldRenderOffScreen(@NotNull ArcaneAlembicBlockEntity alembic) {
      return false;
   }

   /** The block entity's own box: NeoForge 21.1 asks the renderer, not the block entity. */
   @Override
   public AABB getRenderBoundingBox(ArcaneAlembicBlockEntity blockEntity) {
      return blockEntity.getRenderBoundingBox();
   }
}
