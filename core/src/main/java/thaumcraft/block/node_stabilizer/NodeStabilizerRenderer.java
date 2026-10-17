package thaumcraft.block.node_stabilizer;

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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.init.registry.ModBlocks;
import thaumcraft.util.UtilsFX;

@OnlyIn(Dist.CLIENT)
public class NodeStabilizerRenderer implements BlockEntityRenderer<NodeStabilizerBlockEntity> {
   private BakedModel model;
   private BakedModel over;

   public NodeStabilizerRenderer(Context context) {
   }

   public void render(
      @NotNull NodeStabilizerBlockEntity tile, float pt, @NotNull PoseStack pose, @NotNull MultiBufferSource source, int brightnessOverride, int overlayTexture
   ) {
      Minecraft minecraft = Minecraft.getInstance();
      if (this.model == null) {
         ModelManager manager = minecraft.getModelManager();
         this.model = manager.getModel(Thaumcraft.staticModel("node_stabilizer_piston"));
         this.over = manager.getModel(Thaumcraft.staticModel("node_stabilizer_piston_over"));
      }

      int lock = tile.getBlockState().is((Block)ModBlocks.NODE_STABILIZER.get()) ? 1 : 2;
      boolean up = tile.getBlockState().getValue(NodeStabilizerBlock.FACING) == Direction.UP;
      ModelBlockRenderer renderer = minecraft.getBlockRenderer().getModelRenderer();
      VertexConsumer buffer = source.getBuffer(Sheets.translucentCullBlockSheet());
      pose.pushPose();
      if (!up) {
         // Same turn as "x": 180 in the blockstate, so the pistons stay in the flipped body.
         pose.rotateAround(Axis.XP.rotationDegrees(180.0F), 0.5F, 0.5F, 0.5F);
      }

      pose.translate(0.5F, 0.0F, 0.5F);
      pose.mulPose(Axis.XN.rotationDegrees(90.0F));

      for (int i = 0; i < 4; i++) {
         pose.pushPose();
         pose.mulPose(Axis.ZP.rotationDegrees(90 * i));
         pose.mulPose(Axis.XP.rotationDegrees(90.0F));
         pose.translate(-0.33F, 0.37F, -0.5F);
         pose.mulPose(Axis.ZN.rotationDegrees(45.0F));
         pose.translate(0.0F, tile.count / 100.0F, 0.0F);
         renderer.renderModel(pose.last(), buffer, null, this.model, 1.0F, 1.0F, 1.0F, brightnessOverride, overlayTexture, ModelData.EMPTY, null);
         int brightnessOverride0 = 50
            + (int)(170.0F * (tile.count / 37.0F * (Mth.sin((Minecraft.getInstance().cameraEntity.tickCount + i * 5) / 3.0F) * 0.1F + 0.9F)));
         pose.pushPose();
         pose.scale(1.002F, 1.002F, 1.002F);
         pose.translate(-0.001F, -0.001F, -0.001F);
         this.renderOver(pose.last(), buffer, brightnessOverride0, overlayTexture, 1.0F, lock == 2);
         pose.popPose();
         pose.popPose();
      }

      pose.popPose();
      if (tile.count > 0) {
         pose.pushPose();
         float n7 = Mth.sin(Minecraft.getInstance().cameraEntity.tickCount / 8.0F) * 0.1F + 0.5F;
         float bubbleY = up ? 1.5F : -0.5F;
         pose.translate(0.5F, bubbleY, 0.5F);
         pose.scale(1.5F, 1.5F, 1.5F);
         UtilsFX.renderFacingQuad(
            source,
            pose,
            Thaumcraft.location("textures/misc/node_bubble.png"),
            tile.getX() + 0.5,
            tile.getY() + bubbleY,
            tile.getZ() + 0.5,
            0.9F,
            tile.count / 37.0F * n7,
            1,
            0,
            pt,
            lock == 1 ? 16777215 : 16729156
         );
         pose.popPose();
      }
   }

   /** The bubble sits on the node, a block past the stabilizer in the way it faces. */
   @NotNull
   @Override
   public AABB getRenderBoundingBox(@NotNull NodeStabilizerBlockEntity tile) {
      BlockPos pos = tile.getBlockPos();
      return new AABB(pos).minmax(new AABB(pos.relative(tile.getBlockState().getValue(NodeStabilizerBlock.FACING))));
   }

   private void renderOver(Pose pose, VertexConsumer consumer, int brightnessOverride, int overlayTexture, float alpha, boolean advanced) {
      RandomSource randomsource = RandomSource.create();

      for (Direction direction : Direction.values()) {
         randomsource.setSeed(42L);
         this.renderQuadList(
            pose, consumer, this.over.getQuads(null, direction, randomsource, ModelData.EMPTY, null), brightnessOverride, overlayTexture, alpha, advanced
         );
      }

      randomsource.setSeed(42L);
      this.renderQuadList(
         pose, consumer, this.over.getQuads(null, null, randomsource, ModelData.EMPTY, null), brightnessOverride, overlayTexture, alpha, advanced
      );
   }

   private void renderQuadList(
      Pose pose, VertexConsumer consumer, List<BakedQuad> quads, int brightnessOverride, int overlayTexture, float alpha, boolean advanced
   ) {
      float[] floats = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
      int[] brightness = new int[]{brightnessOverride, brightnessOverride, brightnessOverride, brightnessOverride};
      float r = 1.0F;
      float g = advanced ? 0.2F : 1.0F;
      float b = advanced ? 0.2F : 1.0F;

      for (BakedQuad bakedquad : quads) {
         consumer.putBulkData(pose, bakedquad, floats, r, g, b, alpha, brightness, overlayTexture, false);
      }
   }
}
