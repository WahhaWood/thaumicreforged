package thaumcraft.block.arcane_pedestal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PedestalBlockEntityRenderer implements BlockEntityRenderer<ArcanePedestalBlockEntity> {
   public PedestalBlockEntityRenderer(Context ctx) {
   }

   public void render(ArcanePedestalBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
      if (be.getLevel() != null) {
         ItemStack stack = getStack(be);
         if (!stack.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            float t = (mc.getCameraEntity() != null ? mc.getCameraEntity().tickCount : 0) + partialTicks;
            ItemStack renderStack = stack.copy();
            renderStack.setCount(1);
            poseStack.pushPose();
            poseStack.translate(0.5, 1.15 + Mth.sin(t % 32767.0F / 16.0F) * 0.05, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(t % 360.0F));
            if (renderStack.getItem() instanceof BlockItem) {
               poseStack.scale(2.0F, 2.0F, 2.0F);
            }

            int light = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above());
            mc.getItemRenderer()
               .renderStatic(
                  renderStack, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, be.getLevel(), be.getBlockPos().hashCode()
               );

            poseStack.popPose();
         }
      }
   }

   private static ItemStack getStack(ArcanePedestalBlockEntity be) {
      return be.getItem(0);
   }
}
