package thaumcraft.block.tube.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.block.tube.oneway.TubeOnewayBlockEntity;
import thaumcraft.block.tube.render.model.ModelTubeValve;
import thaumcraft.block.tube.render.util.TubeRenderUtil;

public class TubeOnewayBlockEntityRenderer implements BlockEntityRenderer<TubeOnewayBlockEntity> {
   public static final ResourceLocation VALVE_PNG = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/valve.png");
   private final ModelTubeValve model;

   public TubeOnewayBlockEntityRenderer(Context ctx) {
      this.model = new ModelTubeValve(ctx.bakeLayer(ModelTubeValve.TUBE_VALVE));
   }

   public void render(
      @NotNull TubeOnewayBlockEntity be, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buffers, int packedLight, int packedOverlay
   ) {
      Direction fd = Direction.NORTH;
      Direction f = be.getFacing();
      if (f != null) {
         fd = f;
      }

      if (be.getLevel() == null || ThaumcraftApiHelper.getConnectableTile(be.getLevel(), be.getBlockPos(), fd.getOpposite()) != null) {
         pose.pushPose();
         pose.translate(0.5, 0.5, 0.5);
         TubeRenderUtil.applyFacingOpposite(pose, fd);
         pose.pushPose();
         pose.scale(1.1F, 0.5F, 1.1F);
         pose.translate(0.0, -0.5, 0.0);
         VertexConsumer rod = buffers.getBuffer(RenderType.entityCutout(VALVE_PNG));
         this.model.render(pose, rod, packedLight, packedOverlay, 0.45F, 0.5F, 1.0F, 1.0F);
         pose.translate(0.0, -0.25, 0.0);
         this.model.render(pose, rod, packedLight, packedOverlay, 0.45F, 0.5F, 1.0F, 1.0F);
         pose.translate(0.0, -0.25, 0.0);
         this.model.render(pose, rod, packedLight, packedOverlay, 0.45F, 0.5F, 1.0F, 1.0F);
         pose.popPose();
         pose.popPose();
      }
   }
}
