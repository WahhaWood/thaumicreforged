package thaumicenergistics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import thaumicenergistics.block.gearbox.entity.GearBoxBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GearBoxBlockEntityRenderer implements BlockEntityRenderer<GearBoxBlockEntity> {
   private final GearBoxModel gearboxModel;
   private static final ResourceLocation TEX_IRON = ResourceLocation.fromNamespaceAndPath("thaumicenergistics", "textures/models/gearbox.iron.png");
   private static final ResourceLocation TEX_THAUMIUM = ResourceLocation.fromNamespaceAndPath("thaumicenergistics", "textures/models/gearbox.thaumium.png");

   public GearBoxBlockEntityRenderer(Context ctx) {
      this.gearboxModel = new GearBoxModel(ctx.bakeLayer(GearBoxModel.LAYER_LOCATION));
   }

   public void render(GearBoxBlockEntity gearboxTile, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      int light = gearboxTile.getLevel() == null ? 15728880 : packedLight;
      ResourceLocation tex = gearboxTile.isThaumiumGearbox() ? TEX_THAUMIUM : TEX_IRON;
      VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(tex));
      poseStack.pushPose();
      poseStack.translate(0.5, 0.5, 0.5);
      poseStack.scale(1.2F, 1.2F, 1.2F);
      this.gearboxModel.updateToTileEntity(gearboxTile);
      this.gearboxModel.render(poseStack, vc, light, packedOverlay);
      poseStack.popPose();
   }
}
