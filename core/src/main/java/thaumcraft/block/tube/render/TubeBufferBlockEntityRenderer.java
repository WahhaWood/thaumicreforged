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
import thaumcraft.block.tube.buffer.TubeBufferBlockEntity;
import thaumcraft.block.tube.render.model.ModelTubeValve;
import thaumcraft.block.tube.render.util.TubeRenderUtil;

public class TubeBufferBlockEntityRenderer implements BlockEntityRenderer<TubeBufferBlockEntity> {
   public static final ResourceLocation VALVE_PNG = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/valve.png");
   private final ModelTubeValve model;

   public TubeBufferBlockEntityRenderer(Context ctx) {
      this.model = new ModelTubeValve(ctx.bakeLayer(ModelTubeValve.TUBE_VALVE));
   }

   public void render(
      @NotNull TubeBufferBlockEntity be, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buffers, int packedLight, int packedOverlay
   ) {
      boolean[] open = be.getOpenSides();
      byte[] choke = be.getChokedSides();
      if (open != null && choke != null) {
         VertexConsumer rod = buffers.getBuffer(RenderType.entityCutout(VALVE_PNG));

         for (Direction dir : Direction.values()) {
            int idx = TubeRenderUtil.idx(dir);
            if (idx >= 0
               && idx < open.length
               && idx < choke.length
               && choke[idx] != 0
               && open[idx]
               && (be.getLevel() == null || ThaumcraftApiHelper.getConnectableTile(be.getLevel(), be.getBlockPos(), dir) != null)) {
               pose.pushPose();
               pose.translate(0.5, 0.5, 0.5);
               TubeRenderUtil.applyFacingOpposite(pose, dir);
               float r = choke[idx] == 2 ? 1.0F : 0.3F;
               float g = 0.3F;
               float b = choke[idx] == 2 ? 0.3F : 1.0F;
               pose.pushPose();
               pose.scale(1.2F, 1.0F, 1.2F);
               pose.translate(0.0, -0.5, 0.0);
               this.model.render(pose, rod, packedLight, packedOverlay, r, g, b, 1.0F);
               pose.popPose();
               pose.popPose();
            }
         }
      }
   }
}
