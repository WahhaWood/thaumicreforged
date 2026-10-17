package thaumcraft.block.encharter;

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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import thaumcraft.item.wands.ItemWandCasting;

@OnlyIn(Dist.CLIENT)
public class EnchanterBlockEntityRenderer implements BlockEntityRenderer<EnchanterBlockEntity> {
   public EnchanterBlockEntityRenderer(Context context) {
   }

   public void render(
      @NotNull EnchanterBlockEntity be, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay
   ) {
      if (be.getLevel() != null) {
         Minecraft mc = Minecraft.getInstance();
         float time = (float)be.getLevel().getGameTime() + partialTick;
         ItemStack wand = be.getItem(1);
         if (!wand.isEmpty() && wand.getItem() instanceof ItemWandCasting) {
            poseStack.pushPose();
            poseStack.translate(0.6, 0.84, 0.44);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-70.0F));
            poseStack.scale(0.5F, 0.5F, 0.5F);
            float bob = (Mth.cos(time / 20.0F) - 1.2F) / 10.0F;
            poseStack.translate(0.0, 0.325, bob);
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.cos(time / 10.0F) * 5.0F));
            poseStack.translate(0.0, -0.325, 0.0);
            mc.getItemRenderer()
               .renderStatic(null, wand, ItemDisplayContext.FIXED, false, poseStack, buffer, be.getLevel(), packedLight, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
         }

         ItemStack tool = be.getItem(0);
         if (!tool.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.6, 0.76, 0.5);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(30.0F));
            poseStack.scale(0.7F, 0.7F, 0.7F);
            mc.getItemRenderer()
               .renderStatic(null, tool, ItemDisplayContext.FIXED, false, poseStack, buffer, be.getLevel(), packedLight, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
         }
      }
   }
}
