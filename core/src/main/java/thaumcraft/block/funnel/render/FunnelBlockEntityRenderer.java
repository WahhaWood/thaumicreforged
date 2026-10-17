package thaumcraft.block.funnel.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import thaumcraft.block.funnel.FunnelBlockEntity;

public class FunnelBlockEntityRenderer implements BlockEntityRenderer<FunnelBlockEntity> {
   public FunnelBlockEntityRenderer(Context ignored) {
   }

   public void render(FunnelBlockEntity funnel, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      ItemStack stack = funnel.getItem(0);
      if (!stack.isEmpty()) {
         poseStack.pushPose();
         poseStack.translate(0.5, 0.365, 0.5);
         poseStack.scale(0.8F, 0.8F, 0.8F);
         Minecraft.getInstance()
            .getItemRenderer()
            .renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, funnel.getLevel(), 0);
         poseStack.popPose();
      }
   }
}
