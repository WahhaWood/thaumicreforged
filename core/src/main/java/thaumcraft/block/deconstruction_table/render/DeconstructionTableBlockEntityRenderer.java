package thaumcraft.block.deconstruction_table.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.block.deconstruction_table.DeconstructionTableBlockEntity;
import thaumcraft.init.registry.ModItems;

public class DeconstructionTableBlockEntityRenderer implements BlockEntityRenderer<DeconstructionTableBlockEntity> {
   private final ItemStack thaumometer = new ItemStack((ItemLike)ModItems.THAUMOMETER.get());

   public DeconstructionTableBlockEntityRenderer(Context ctx) {
   }

   public void render(DeconstructionTableBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int packedLight, int overlay) {
      BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
      if (be.getLevel() == null) {
         blockRenderer.renderSingleBlock(be.getBlockState(), pose, buffers, packedLight, overlay);
      }

      Level level = be.getLevel();
      if (level == null) {
         level = Minecraft.getInstance().level;
      }

      long gt = level != null ? level.getGameTime() : 0L;
      float t = (float)gt + partialTick;
      int lightAbove = level != null ? LevelRenderer.getLightColor(level, be.getBlockPos().above()) : packedLight;
      ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
      ItemStack input = be.getItem(0);
      if (!input.isEmpty()) {
         pose.pushPose();
         pose.translate(0.5, 1.15 + Mth.sin(t / 14.0F) * 0.05F, 0.5);
         pose.mulPose(Axis.YP.rotationDegrees(t % 360.0F));
         pose.scale(0.75F, 0.75F, 0.75F);
         itemRenderer.renderStatic(input.copyWithCount(1), ItemDisplayContext.GROUND, lightAbove, OverlayTexture.NO_OVERLAY, pose, buffers, level, 0);
         pose.popPose();
      }

      pose.pushPose();
      pose.translate(0.5, 1.05, 0.5);
      pose.mulPose(Axis.YP.rotationDegrees(180.0F));
      pose.mulPose(Axis.XP.rotationDegrees(90.0F));
      pose.scale(0.8F, 0.8F, 0.8F);
      itemRenderer.renderStatic(this.thaumometer, ItemDisplayContext.FIXED, lightAbove, OverlayTexture.NO_OVERLAY, pose, buffers, level, 0);
      pose.popPose();
      if (be.aspect != null) {
         renderAspectTag(be.aspect, t, pose, buffers, 15728880);
      }
   }

   private static void renderAspectTag(Aspect aspect, float t, PoseStack pose, MultiBufferSource buffers, int light) {
      ResourceLocation tex = aspect.getImage();
      if (tex != null) {
         pose.pushPose();
         pose.translate(0.5, 1.081, 0.5);
         pose.mulPose(Axis.XP.rotationDegrees(90.0F));
         pose.mulPose(Axis.ZP.rotationDegrees(t % 360.0F));
         pose.scale(0.024F, 0.024F, 0.024F);

         VertexConsumer vc;
         try {
            vc = buffers.getBuffer(RenderType.entityTranslucentEmissive(tex));
         } catch (Throwable var11) {
            vc = buffers.getBuffer(RenderType.entityTranslucent(tex));
         }

         Pose last = pose.last();
         Matrix4f mat = last.pose();
         Matrix3f normal = last.normal();
         int a = 255;
         vc.addVertex(mat, -8.0F, 8.0F, 0.0F)
            .setColor(255, 255, 255, a)
            .setUv(0.0F, 1.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setUv2(light & 65535, light >> 16)
            .setNormal(0.0F, 0.0F, 1.0F);
         vc.addVertex(mat, 8.0F, 8.0F, 0.0F)
            .setColor(255, 255, 255, a)
            .setUv(1.0F, 1.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setUv2(light & 65535, light >> 16)
            .setNormal(0.0F, 0.0F, 1.0F);
         vc.addVertex(mat, 8.0F, -8.0F, 0.0F)
            .setColor(255, 255, 255, a)
            .setUv(1.0F, 0.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setUv2(light & 65535, light >> 16)
            .setNormal(0.0F, 0.0F, 1.0F);
         vc.addVertex(mat, -8.0F, -8.0F, 0.0F)
            .setColor(255, 255, 255, a)
            .setUv(0.0F, 0.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setUv2(light & 65535, light >> 16)
            .setNormal(0.0F, 0.0F, 1.0F);
         pose.popPose();
      }
   }
}
