package thaumicenergistics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.block.arcane_assembler.AdvancedArcaneAssemblerBlock;
import thaumicenergistics.block.arcane_assembler.ArcaneAssemblerBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArcaneAssemblerBlockEntityRenderer implements BlockEntityRenderer<ArcaneAssemblerBlockEntity> {
   private static final ResourceLocation TEXTURE = ThaumicEnergistics.location("textures/models/arcane.assembler.png");
   private final ArcaneAssemblerModel assemblerModel;

   public ArcaneAssemblerBlockEntityRenderer(Context ctx) {
      this.assemblerModel = new ArcaneAssemblerModel(ctx.bakeLayer(ArcaneAssemblerModel.LAYER_LOCATION));
   }

   public void render(ArcaneAssemblerBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      if (!(be.getBlockState().getBlock() instanceof AdvancedArcaneAssemblerBlock)) {
         int light = be.getLevel() == null ? 15728880 : packedLight;
         poseStack.pushPose();
         poseStack.translate(0.5, 0.5, 0.5);
         poseStack.scale(0.47058824F, 0.47058824F, 0.47058824F);
         VertexConsumer frame = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
         this.assemblerModel.renderFrame(poseStack, frame, light, packedOverlay);
         VertexConsumer glass = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
         this.assemblerModel.renderGlass(poseStack, glass, light, OverlayTexture.NO_OVERLAY);
         poseStack.popPose();
      }
   }
}
