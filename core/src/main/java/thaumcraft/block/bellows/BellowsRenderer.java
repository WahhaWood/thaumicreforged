package thaumcraft.block.bellows;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;

public class BellowsRenderer implements BlockEntityRenderer<BellowsBlockEntity> {
   private BakedModel bag;
   private BakedModel plank;

   public BellowsRenderer(Context context) {
   }

   public void render(
      @NotNull BellowsBlockEntity blockEntity,
      float pt,
      @NotNull PoseStack pose,
      @NotNull MultiBufferSource bufferSource,
      int brightnessOverride,
      int overlayTexture
   ) {
      Minecraft minecraft = Minecraft.getInstance();
      if (this.bag == null) {
         ModelManager manager = minecraft.getModelManager();
         this.bag = manager.getModel(Thaumcraft.staticModel("arcane_bellows_bag"));
         this.plank = manager.getModel(Thaumcraft.staticModel("arcane_bellows_plank"));
      }

      ModelBlockRenderer renderer = minecraft.getBlockRenderer().getModelRenderer();
      VertexConsumer buffer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());
      float offY = blockEntity.inflation * 0.5F;
      pose.pushPose();
      pose.translate(0.5F, 0.5F, 0.5F);
      switch (blockEntity.getFacing()) {
         case UP:
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            break;
         case DOWN:
            pose.mulPose(Axis.XN.rotationDegrees(-90.0F));
      }

      pose.translate(-0.5F, -0.5F, -0.5F);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 771);
      pose.pushPose();
      pose.translate(0.0F, offY * 0.5F, 0.0F);
      pose.scale(1.0F, 1.0F - offY, 1.0F);
      renderer.renderModel(pose.last(), buffer, null, this.bag, 1.0F, 1.0F, 1.0F, brightnessOverride, overlayTexture, ModelData.EMPTY, null);
      pose.popPose();
      pose.pushPose();
      pose.translate(0.0F, 0.875F, 0.0F);
      pose.translate(0.0F, -offY / 2.0F, 0.0F);
      renderer.renderModel(pose.last(), buffer, null, this.plank, 1.0F, 1.0F, 1.0F, brightnessOverride, overlayTexture, ModelData.EMPTY, null);
      pose.popPose();
      pose.pushPose();
      pose.translate(0.0F, offY / 2.0F, 0.0F);
      renderer.renderModel(pose.last(), buffer, null, this.plank, 1.0F, 1.0F, 1.0F, brightnessOverride, overlayTexture, ModelData.EMPTY, null);
      pose.popPose();
      RenderSystem.disableBlend();
      pose.popPose();
   }
}
