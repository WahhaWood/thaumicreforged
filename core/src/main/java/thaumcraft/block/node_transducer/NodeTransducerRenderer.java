package thaumcraft.block.node_transducer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;

@OnlyIn(Dist.CLIENT)
public class NodeTransducerRenderer implements BlockEntityRenderer<NodeTransducerBlockEntity> {
   private BakedModel model;
   private BakedModel over;

   public NodeTransducerRenderer(Context context) {
   }

   public void render(
      @NotNull NodeTransducerBlockEntity entity,
      float pt,
      @NotNull PoseStack pose,
      @NotNull MultiBufferSource source,
      int brightnessOverride,
      int overlayTexture
   ) {
      Minecraft minecraft = Minecraft.getInstance();
      if (this.model == null) {
         ModelManager manager = minecraft.getModelManager();
         this.model = manager.getModel(Thaumcraft.staticModel("node_transducer_piston"));
         this.over = manager.getModel(Thaumcraft.staticModel("node_transducer_piston_over"));
      }

      ModelBlockRenderer renderer = minecraft.getBlockRenderer().getModelRenderer();
      VertexConsumer solidBuffer = source.getBuffer(Sheets.solidBlockSheet());
      VertexConsumer translucentBuffer = source.getBuffer(Sheets.translucentCullBlockSheet());
      pose.pushPose();
      pose.translate(0.5F, 0.5F, 0.5F);
      pose.mulPose(Axis.XP.rotationDegrees(90.0F));
      float n5 = Math.min(50, entity.count) / 137.0F;

      for (int i = 0; i < 4; i++) {
         pose.pushPose();
         pose.mulPose(Axis.ZP.rotationDegrees(90 * i));
         pose.mulPose(Axis.XN.rotationDegrees(90.0F));
         pose.translate(0.38F, -0.57F, -0.5F);
         pose.mulPose(Axis.ZP.rotationDegrees(45.0F));
         pose.translate(0.0F, -n5, 0.0F);
         renderer.renderModel(pose.last(), solidBuffer, null, this.model, 1.0F, 1.0F, 1.0F, brightnessOverride, overlayTexture, ModelData.EMPTY, null);
         int brightnessOverride0 = 50 + (int)(170.0F * (n5 * 2.5F * (Mth.sin((Minecraft.getInstance().cameraEntity.tickCount + i * 5) / 3.0F) * 0.1F + 0.9F)));
         pose.pushPose();
         float scaleOffset = 1.002F;
         pose.scale(scaleOffset, scaleOffset, scaleOffset);
         pose.translate(-0.001F, -0.001F, -0.001F);
         this.renderOver(pose.last(), translucentBuffer, brightnessOverride0, overlayTexture, entity.status);
         pose.popPose();
         pose.popPose();
      }

      pose.popPose();
   }

   private void renderOver(Pose pose, VertexConsumer consumer, int brightnessOverride, int overlayTexture, int status) {
      RandomSource randomsource = RandomSource.create();

      for (Direction direction : Direction.values()) {
         randomsource.setSeed(42L);
         this.renderQuadList(
            pose, consumer, this.over.getQuads(null, direction, randomsource, ModelData.EMPTY, null), brightnessOverride, overlayTexture, status
         );
      }

      randomsource.setSeed(42L);
      this.renderQuadList(pose, consumer, this.over.getQuads(null, null, randomsource, ModelData.EMPTY, null), brightnessOverride, overlayTexture, status);
   }

   private void renderQuadList(Pose pose, VertexConsumer consumer, List<BakedQuad> quads, int brightnessOverride, int overlayTexture, int status) {
      float[] floats = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
      int[] brightness = new int[]{brightnessOverride, brightnessOverride, brightnessOverride, brightnessOverride};
      float r = status != 1 && status != 2 ? 0.5F : 1.0F;
      float g = status == 1 ? 0.6F : (status == 2 ? 0.0F : 1.0F);
      float b = status == 1 ? 0.1F : (status == 2 ? 0.3F : 0.5F);

      for (BakedQuad bakedquad : quads) {
         consumer.putBulkData(pose, bakedquad, floats, r, g, b, 1.0F, brightness, overlayTexture, false);
      }
   }
}
