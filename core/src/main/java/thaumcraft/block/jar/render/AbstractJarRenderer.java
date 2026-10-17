package thaumcraft.block.jar.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import thaumcraft.block.jar.render.util.JarRenderUtil;
import thaumcraft.block.jar.standart.JarBlockEntity;

public abstract class AbstractJarRenderer<T extends JarBlockEntity> implements BlockEntityRenderer<T> {
   protected static final ResourceLocation TEX_LABEL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/label.png");
   private static final float LIQUID_EPS = 0.00125F;
   private static final float LABEL_HALF_SIZE = 0.25F;
   private static final float ICON_HALF_SIZE = 0.168F;
   private static final float LABEL_Z = 0.315F;
   private static final float ICON_Z = 0.3142F;

   protected AbstractJarRenderer(Context ctx) {
   }

   public void render(@NotNull T jar, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers, int packedLight, int packedOverlay) {
      if (jar.aspect != null && jar.amount > 0) {
         this.renderLiquid(jar, poseStack, buffers);
      }

      if (jar.aspectFilter != null) {
         poseStack.pushPose();
         poseStack.translate(0.5, 0.01, 0.5);
         poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
         this.renderLabel(jar, poseStack, buffers, packedLight, packedOverlay);
         poseStack.popPose();
      }
   }

   private void renderLiquid(@NotNull T jar, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers) {
      float fill = Mth.clamp((float)jar.amount / jar.maxAmount, 0.0F, 1.0F);
      float halfSize = 0.25F;
      float height = 0.625F * fill;
      float x0 = 0.5F - halfSize + 0.00125F;
      float z0 = 0.5F - halfSize + 0.00125F;
      float x1 = 0.5F + halfSize - 0.00125F;
      float z1 = 0.5F + halfSize - 0.00125F;
      float y0 = 0.0625F;
      float y1 = 0.0625F + height;
      JarRenderUtil.renderFluidBox(poseStack, buffers, x0, y0, z0, x1, y1, z1, jar.aspect.getColor(), 0.9F, 15728880, OverlayTexture.NO_OVERLAY);
   }

   protected void renderLabel(@NotNull T jar, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers, int packedLight, int packedOverlay) {
      Direction facing = jar.getHorizontalFacing();
      poseStack.pushPose();
      switch (facing) {
         case SOUTH:
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            break;
         case EAST:
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            break;
         case WEST:
            poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
      }

      poseStack.pushPose();
      poseStack.translate(0.0, -0.4, 0.315);
      float labelHalf = 0.25F;
      VertexConsumer vcLabel = buffers.getBuffer(RenderType.entityCutoutNoCull(TEX_LABEL));
      JarRenderUtil.quad(
         poseStack,
         vcLabel,
         -labelHalf,
         -labelHalf,
         0.0F,
         labelHalf,
         labelHalf,
         0.0F,
         0.0F,
         0.0F,
         1.0F,
         1.0F,
         packedLight,
         packedOverlay,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         0.0F,
         0.0F,
         1.0F
      );
      if (jar.aspectFilter != null) {
         float iconHalf = 0.168F;
         VertexConsumer vcIcon = buffers.getBuffer(RenderType.entityTranslucent(jar.aspectFilter.getImage()));
         JarRenderUtil.quad(
            poseStack,
            vcIcon,
            -iconHalf,
            -iconHalf,
            0.001F,
            iconHalf,
            iconHalf,
            0.001F,
            0.0F,
            1.0F,
            1.0F,
            0.0F,
            15728880,
            packedOverlay,
            0.1F,
            0.1F,
            0.1F,
            0.8F,
            0.0F,
            0.0F,
            1.0F
         );
      }

      poseStack.popPose();
      poseStack.popPose();
   }

   public boolean shouldRenderOffScreen(@NotNull T jar) {
      return false;
   }
}
