package thaumcraft.block.tube.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.block.tube.centrifuge.CentrifugeBlockEntity;

public class CentrifugeRenderer implements BlockEntityRenderer<CentrifugeBlockEntity> {
   private BakedModel model;

   public CentrifugeRenderer(Context context) {
   }

   public void render(
      @NotNull CentrifugeBlockEntity blockEntity,
      float pt,
      @NotNull PoseStack pose,
      @NotNull MultiBufferSource source,
      int brightnessOverride,
      int overlayTexture
   ) {
      if (this.model == null) {
         this.model = Minecraft.getInstance().getModelManager().getModel(Thaumcraft.staticModel("alchemical_centrifuge_rotate"));
      }

      VertexConsumer buffer = source.getBuffer(Sheets.cutoutBlockSheet());
      pose.pushPose();
      pose.translate(0.5F, 0.0F, 0.5F);
      pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(pt, blockEntity.previousRotation, blockEntity.rotation)));
      pose.translate(-0.5F, 0.0F, -0.5F);
      Minecraft.getInstance()
         .getBlockRenderer()
         .getModelRenderer()
         .renderModel(pose.last(), buffer, null, this.model, 1.0F, 1.0F, 1.0F, brightnessOverride, overlayTexture, ModelData.EMPTY, null);
      pose.popPose();
   }

   /** The block entity's own box: NeoForge 21.1 asks the renderer, not the block entity. */
   @Override
   public AABB getRenderBoundingBox(CentrifugeBlockEntity blockEntity) {
      return blockEntity.getRenderBoundingBox();
   }
}
