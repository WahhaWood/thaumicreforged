package thaumcraft.block.focal_manipulator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.wands.ItemFocusBasic;

@OnlyIn(Dist.CLIENT)
public class FocalManipulatorBlockEntityRenderer implements BlockEntityRenderer<FocalManipulatorBlockEntity> {
   public FocalManipulatorBlockEntityRenderer(Context ctx) {
   }

   public void render(
      @NotNull FocalManipulatorBlockEntity be,
      float partialTick,
      @NotNull PoseStack poseStack,
      @NotNull MultiBufferSource buffer,
      int packedLight,
      int packedOverlay
   ) {
      if (be.getLevel() != null) {
         ItemStack stack = be.getItem(0);
         if (!stack.isEmpty() && stack.getItem() instanceof ItemFocusBasic) {
            float t = (Minecraft.getInstance().getCameraEntity() != null ? Minecraft.getInstance().getCameraEntity().tickCount : 0) + partialTick;
            float bob = Mth.sin(t / 14.0F) * 0.1F + 0.05F;
            poseStack.pushPose();
            poseStack.translate(0.5, 1.2 + bob, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(t % 360.0F));
            poseStack.scale(0.5F, 0.5F, 0.5F);
            Minecraft.getInstance()
               .getItemRenderer()
               .renderStatic(null, stack, ItemDisplayContext.FIXED, false, poseStack, buffer, be.getLevel(), packedLight, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
         }
      }
   }

   /** The block entity's own box: NeoForge 21.1 asks the renderer, not the block entity. */
   @Override
   public AABB getRenderBoundingBox(FocalManipulatorBlockEntity blockEntity) {
      return blockEntity.getRenderBoundingBox();
   }
}
