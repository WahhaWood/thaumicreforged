package thaumcraft.block.repairer.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import thaumcraft.block.repairer.RepairerBlock;
import thaumcraft.block.repairer.RepairerBlockEntity;
import thaumcraft.util.ThaumcraftRenderTypes;

public class RepairerBlockEntityRenderer implements BlockEntityRenderer<RepairerBlockEntity> {
   private static final ResourceLocation MODEL_TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/block/model/repairer.png");
   private static final ResourceLocation OVERLAY_ON = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/repair.png");
   private static final ResourceLocation OVERLAY_OFF = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/repair_off.png");
   private final RepairerModel model;

   public RepairerBlockEntityRenderer(Context context) {
      this.model = new RepairerModel(context.bakeLayer(RepairerModel.LAYER));
   }

   public void render(RepairerBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      float rotation = switch ((Direction)be.getBlockState().getValue(RepairerBlock.FACING)) {
         case NORTH -> 180.0F;
         case SOUTH -> 0.0F;
         case WEST -> 90.0F;
         case EAST -> 270.0F;
         default -> 0.0F;
      };
      poseStack.pushPose();
      poseStack.translate(0.5, 1.5, 0.5);
      poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
      poseStack.scale(1.0F, -1.0F, -1.0F);
      VertexConsumer solid = buffer.getBuffer(RenderType.entityCutoutNoCull(MODEL_TEX));
      this.model.renderSolid(poseStack, solid, packedLight, OverlayTexture.NO_OVERLAY);
      this.renderFloatingItem(be, partialTick, poseStack, buffer, packedLight);
      VertexConsumer glass = buffer.getBuffer(ThaumcraftRenderTypes.entityTranslucentNoDepthWrite(MODEL_TEX));
      this.model.renderGlass(poseStack, glass, packedLight, OverlayTexture.NO_OVERLAY);
      this.renderOverlay(be, partialTick, poseStack, buffer, packedLight, be.tookLastTick ? OVERLAY_ON : OVERLAY_OFF);
      poseStack.popPose();
   }

   private void renderFloatingItem(RepairerBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      ItemStack item = be.getItem(0);
      if (!item.isEmpty()) {
         float ticks = be.ticksExisted + partialTick;
         poseStack.pushPose();
         poseStack.translate(0.0, 0.72 + Mth.sin(ticks / 10.0F) * 0.06F, 0.0);
         poseStack.mulPose(Axis.YP.rotationDegrees(ticks * 0.75F % 360.0F));
         poseStack.scale(0.5F, 0.5F, 0.5F);
         Minecraft.getInstance()
            .getItemRenderer()
            .renderStatic(item, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, be.getLevel(), 0);
         poseStack.popPose();
      }
   }

   private void renderOverlay(
      RepairerBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, ResourceLocation texture
   ) {
      float ticks = be.ticksExisted + partialTick;
      poseStack.pushPose();
      poseStack.translate(0.0, 0.525, 0.0);
      poseStack.mulPose(Axis.YP.rotationDegrees(ticks * 0.75F % 360.0F));
      VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(texture));
      Pose pose = poseStack.last();
      float size = 0.625F;
      vc.addVertex(pose.pose(), -size, 0.0F, size)
         .setColor(255, 255, 255, 255)
         .setUv(0.0F, 1.0F)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(packedLight, 0)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(pose.pose(), size, 0.0F, size)
         .setColor(255, 255, 255, 255)
         .setUv(1.0F, 1.0F)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(packedLight, 0)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(pose.pose(), size, 0.0F, -size)
         .setColor(255, 255, 255, 255)
         .setUv(1.0F, 0.0F)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(packedLight, 0)
         .setNormal(0.0F, 1.0F, 0.0F);
      vc.addVertex(pose.pose(), -size, 0.0F, -size)
         .setColor(255, 255, 255, 255)
         .setUv(0.0F, 0.0F)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(packedLight, 0)
         .setNormal(0.0F, 1.0F, 0.0F);
      poseStack.popPose();
   }
}
