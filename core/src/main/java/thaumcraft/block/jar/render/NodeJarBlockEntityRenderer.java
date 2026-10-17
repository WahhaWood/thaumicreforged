package thaumcraft.block.jar.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import thaumcraft.block.airy.aura_node.AuraNodeRenderer;
import thaumcraft.block.jar.node.NodeJarBlockEntity;

public class NodeJarBlockEntityRenderer implements BlockEntityRenderer<NodeJarBlockEntity> {
   public NodeJarBlockEntityRenderer(Context ctx) {
   }

   public void render(
      @NotNull NodeJarBlockEntity jar, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers, int packedLight, int packedOverlay
   ) {
      Entity camera = Minecraft.getInstance().cameraEntity;
      if (camera != null) {
         poseStack.pushPose();
         poseStack.translate(0.5, 0.01, 0.5);
         float scale = computeNodeAnimateScale(jar);
         if (scale != 1.0F) {
            poseStack.scale(scale, scale, scale);
         }

         poseStack.translate(-0.5, -0.01, -0.5);
         AuraNodeRenderer.renderNode(
            camera, poseStack, buffers, 64.0, true, false, 0.7F, jar.getBlockPos(), jar.getAspects(), jar.getNodeType(), jar.getNodeModifier()
         );
         poseStack.popPose();
      }
   }

   private static float computeNodeAnimateScale(@NotNull NodeJarBlockEntity jar) {
      long now = System.currentTimeMillis();
      long until = jar.animateUntilMs;
      if (until > now) {
         float s = 1.0F + 2.0F * ((float)(until - now) / 1000.0F);
         return Mth.clamp(s, 1.0F, 5.0F);
      } else {
         return 1.0F;
      }
   }

   public boolean shouldRenderOffScreen(NodeJarBlockEntity be) {
      return true;
   }
}
