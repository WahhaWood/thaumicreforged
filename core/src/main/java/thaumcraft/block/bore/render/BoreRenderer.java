package thaumcraft.block.bore.render;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.model.CompositeModel.Baked;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.block.bore.BoreBlockEntity;
import thaumcraft.util.UtilsFX;

public class BoreRenderer implements BlockEntityRenderer<BoreBlockEntity> {
   private ImmutableMap<String, BakedModel> children;

   public BoreRenderer(Context context) {
   }

   public void render(
      @NotNull BoreBlockEntity entity, float pt, @NotNull PoseStack pose, @NotNull MultiBufferSource bufferSource, int brightnessOverride, int overlayTexture
   ) {
      Minecraft minecraft = Minecraft.getInstance();
      if (this.children == null) {
         this.children = (ImmutableMap<String, BakedModel>)ObfuscationReflectionHelper.getPrivateValue(
            Baked.class, (Baked)minecraft.getBlockRenderer().getBlockModel(entity.getBlockState()), "children"
         );
      }

      VertexConsumer buffer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());
      pose.pushPose();
      pose.translate(0.5F, 0.5F, 0.5F);
      pose.mulPose(Axis.YP.rotationDegrees(entity.rotX - entity.vRadX + pt * entity.speedX));
      pose.pushPose();
      BlockState state = entity.getBlockState();
      if (state.getValue(BlockStateProperties.VERTICAL_DIRECTION) == Direction.UP) {
         pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
      }

      pose.translate(-0.5F, -0.5F, -0.5F);
      this.renderChildren("base", pose, buffer, state, brightnessOverride, overlayTexture);
      pose.popPose();
      pose.mulPose(Axis.ZP.rotationDegrees(entity.rotZ - entity.vRadZ + pt * entity.speedZ));
      pose.pushPose();
      pose.translate(-0.5F, -0.5F, -0.5F);
      this.renderChildren("nozzle", pose, bufferSource.getBuffer(Sheets.translucentCullBlockSheet()), state, brightnessOverride, overlayTexture);
      pose.popPose();
      pose.pushPose();
      pose.mulPose(Axis.YP.rotationDegrees(entity.topRotation));
      pose.translate(-0.5F, -0.5F, -0.5F);
      this.renderChildren("emit", pose, buffer, state, brightnessOverride, overlayTexture);
      if (entity.hasFocus) {
         this.renderChildren("focus", pose, buffer, state, brightnessOverride, overlayTexture);
      }

      pose.popPose();
      pose.pushPose();
      float anim = minecraft.cameraEntity.tickCount % 45 + pt;
      pose.translate(0.0F, -0.17F, 0.0F);
      pose.mulPose(Axis.XN.rotationDegrees(90.0F));
      pose.mulPose(Axis.ZN.rotationDegrees(anim * 8.0F));
      pose.mulPose(Axis.YP.rotationDegrees(10.0F));
      UtilsFX.renderQuadCenteredFromTextureLevel(
         bufferSource, pose, Thaumcraft.location("textures/misc/vortex.png"), 0.4F, 1.0F, 1.0F, 1.0F, 200, DestFactor.ONE_MINUS_SRC_ALPHA, 1.0F
      );
      pose.popPose();
      pose.pushPose();
      pose.translate(0.0F, -0.21F, 0.0F);
      pose.mulPose(Axis.XN.rotationDegrees(90.0F));
      pose.mulPose(Axis.ZP.rotationDegrees(anim * 8.0F));
      pose.mulPose(Axis.YP.rotationDegrees(10.0F));
      UtilsFX.renderQuadCenteredFromTextureLevel(
         bufferSource, pose, Thaumcraft.location("textures/misc/vortex.png"), 0.3F, 1.0F, 1.0F, 1.0F, 200, DestFactor.ONE_MINUS_SRC_ALPHA, 0.8F
      );
      pose.popPose();
      pose.pushPose();
      pose.translate(0.0F, -0.25F, 0.0F);
      pose.mulPose(Axis.XN.rotationDegrees(90.0F));
      pose.mulPose(Axis.ZN.rotationDegrees(anim * 8.0F));
      pose.mulPose(Axis.YN.rotationDegrees(10.0F));
      UtilsFX.renderQuadCenteredFromTextureLevel(
         bufferSource, pose, Thaumcraft.location("textures/misc/vortex.png"), 0.2F, 1.0F, 1.0F, 1.0F, 200, DestFactor.ONE_MINUS_SRC_ALPHA, 0.8F
      );
      pose.popPose();
      pose.popPose();
   }

   private void renderChildren(String children, PoseStack pose, VertexConsumer buffer, BlockState state, int brightnessOverride, int overlayTexture) {
      Minecraft.getInstance()
         .getBlockRenderer()
         .getModelRenderer()
         .renderModel(
            pose.last(), buffer, state, (BakedModel)this.children.get(children), 1.0F, 1.0F, 1.0F, brightnessOverride, overlayTexture, ModelData.EMPTY, null
         );
   }
}
