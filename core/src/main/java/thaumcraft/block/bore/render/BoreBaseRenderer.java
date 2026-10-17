package thaumcraft.block.bore.render;

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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.block.bore.BoreBaseBlockEntity;
import thaumcraft.init.registry.ModBlocks;

public class BoreBaseRenderer implements BlockEntityRenderer<BoreBaseBlockEntity> {
   private static BakedModel connect;

   public BoreBaseRenderer(Context context) {
   }

   public void render(
      @NotNull BoreBaseBlockEntity entity,
      float pt,
      @NotNull PoseStack pose,
      @NotNull MultiBufferSource bufferSource,
      int brightnessOverride,
      int overlayTexture
   ) {
      Level level = entity.getLevel();
      if (level != null) {
         BlockPos blockPos = entity.getBlockPos();
         Direction face = entity.getHorizontalFacing();

         for (Direction value : Direction.values()) {
            if (value != face && !value.getAxis().isVertical()) {
               BlockState state = level.getBlockState(blockPos.relative(value));
               if (state.is((Block)ModBlocks.ARCANE_LAMP.get())
                  || state.is((Block)ModBlocks.ARCANE_LAMP_GROWTH.get())
                  || state.is((Block)ModBlocks.ARCANE_LAMP_FERTILITY.get())) {
                  renderConnect(
                     (BlockState)entity.getBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, value),
                     pose,
                     bufferSource,
                     brightnessOverride,
                     overlayTexture
                  );
               }
            }
         }
      }
   }

   public static void renderConnect(
      BlockState state, @NotNull PoseStack pose, @NotNull MultiBufferSource bufferSource, int brightnessOverride, int overlayTexture
   ) {
      Minecraft minecraft = Minecraft.getInstance();
      if (connect == null) {
         connect = minecraft.getModelManager().getModel(Thaumcraft.staticModel("bore_connect"));
      }

      ModelBlockRenderer renderer = minecraft.getBlockRenderer().getModelRenderer();
      VertexConsumer buffer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());
      pose.pushPose();
      pose.translate(0.5F, 0.5F, 0.5F);
      if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
         pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
         switch ((Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH:
               pose.mulPose(Axis.XP.rotationDegrees(90.0F));
               break;
            case SOUTH:
               pose.mulPose(Axis.XP.rotationDegrees(270.0F));
               break;
            case WEST:
               pose.mulPose(Axis.XP.rotationDegrees(180.0F));
         }
      } else if (state.hasProperty(BlockStateProperties.FACING)) {
         Direction direction = (Direction)state.getValue(BlockStateProperties.FACING);
         if (direction.getAxis().isHorizontal()) {
            pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
         }

         switch ((Direction)state.getValue(BlockStateProperties.FACING)) {
            case NORTH:
               pose.mulPose(Axis.XP.rotationDegrees(90.0F));
               break;
            case SOUTH:
               pose.mulPose(Axis.XP.rotationDegrees(270.0F));
               break;
            case WEST:
            case UP:
               pose.mulPose(Axis.XP.rotationDegrees(180.0F));
         }
      }

      pose.translate(-0.5F, -0.5F, -0.5F);
      renderer.renderModel(pose.last(), buffer, null, connect, 1.0F, 1.0F, 1.0F, brightnessOverride, overlayTexture, ModelData.EMPTY, null);
      pose.popPose();
   }
}
