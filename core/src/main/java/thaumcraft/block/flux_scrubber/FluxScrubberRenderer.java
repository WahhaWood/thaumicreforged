package thaumcraft.block.flux_scrubber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;

public class FluxScrubberRenderer implements BlockEntityRenderer<FluxScrubberBlockEntity> {
   private BakedModel model;

   public FluxScrubberRenderer(Context context) {
   }

   public void render(
      @NotNull FluxScrubberBlockEntity blockEntity,
      float pt,
      @NotNull PoseStack pose,
      @NotNull MultiBufferSource bufferSource,
      int brightnessOverride,
      int overlayTexture
   ) {
      Minecraft minecraft = Minecraft.getInstance();
      if (this.model == null) {
         this.model = minecraft.getModelManager().getModel(Thaumcraft.staticModel("flux_scrubber_cap"));
      }

      Direction direction = blockEntity.getFacing();
      pose.pushPose();
      pose.translate(0.5F, 0.5F, 0.5F);
      switch (direction) {
         case DOWN:
            pose.mulPose(Axis.XP.rotationDegrees(180.0F));
            break;
         case NORTH:
            pose.mulPose(Axis.XP.rotationDegrees(270.0F));
            break;
         case SOUTH:
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            break;
         case WEST:
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
            break;
         case EAST:
            pose.mulPose(Axis.XN.rotationDegrees(90.0F));
            pose.mulPose(Axis.ZP.rotationDegrees(270.0F));
      }

      pose.translate(-0.5F, -0.5F, -0.5F);
      pose.translate(0.0F, -(Mth.sin((minecraft.cameraEntity.tickCount + pt + blockEntity.count) / 8.0F) * 0.075F + 0.075F), 0.0F);
      VertexConsumer buffer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());
      minecraft.getBlockRenderer()
         .getModelRenderer()
         .renderModel(pose.last(), buffer, null, this.model, 1.0F, 1.0F, 1.0F, brightnessOverride, overlayTexture, ModelData.EMPTY, null);
      pose.popPose();
   }
}
