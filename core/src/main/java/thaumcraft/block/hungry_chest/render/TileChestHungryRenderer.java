package thaumcraft.block.hungry_chest.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import thaumcraft.block.hungry_chest.ChestHungryBlockEntity;

public class TileChestHungryRenderer implements BlockEntityRenderer<ChestHungryBlockEntity> {
   public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/chesthungry.png");
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft", "chest_hungry"), "main");
   private final ChestModel model;

   public TileChestHungryRenderer(Context context) {
      this.model = new ChestModel(context.bakeLayer(LAYER_LOCATION));
   }

   public void render(ChestHungryBlockEntity tile, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
      BlockState blockState = tile.hasLevel() ? tile.getBlockState() : tile.getBlockState().getBlock().defaultBlockState();
      float rotation = 0.0F;
      if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
         rotation = ((Direction)blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)).toYRot();
      }

      float lidProgress = Mth.lerp(partialTick, tile.prevLidAngle, tile.lidAngle);
      lidProgress = 1.0F - lidProgress;
      lidProgress = 1.0F - lidProgress * lidProgress * lidProgress;
      poseStack.pushPose();
      poseStack.translate(0.5, 1.2, 0.5);
      poseStack.mulPose(Axis.YP.rotationDegrees(-rotation));
      poseStack.scale(1.0F, -1.0F, -1.0F);
      this.model.setLidAngle(lidProgress);
      VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
      this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, -1);
      poseStack.popPose();
   }
}
