package thaumcraft.block.arcane_workbench;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.item.wands.ItemWandCasting;

public class ArcaneWorkbenchBlockEntityRenderer implements BlockEntityRenderer<MagicWorkbenchBlockEntity> {
   public ArcaneWorkbenchBlockEntityRenderer(Context ctx) {
   }

   public void render(
      @NotNull MagicWorkbenchBlockEntity be, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buffers, int packedLight, int packedOverlay
   ) {
      if (be.getLevel() != null) {
         Minecraft mc = Minecraft.getInstance();
         mc.getBlockRenderer().renderSingleBlock(be.getBlockState(), pose, buffers, packedLight, packedOverlay);
         ItemStack wand = be.getItem(9);
         if (wand.getItem() instanceof ItemWandCasting) {
            ItemStack renderStack = wand.copy();
            renderStack.setCount(1);
            pose.pushPose();
            pose.translate(0.45, 1.0625, 0.45);
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            pose.mulPose(Axis.ZP.rotationDegrees(20.0F));
            pose.scale(0.7F, 0.7F, 0.7F);
            mc.getItemRenderer()
               .renderStatic(
                  renderStack,
                  ItemDisplayContext.FIXED,
                  LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above()),
                  OverlayTexture.NO_OVERLAY,
                  pose,
                  buffers,
                  be.getLevel(),
                  0
               );
            pose.popPose();
         }
      }
   }
}
