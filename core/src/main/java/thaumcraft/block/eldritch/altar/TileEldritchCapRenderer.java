package thaumcraft.block.eldritch.altar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import thaumcraft.init.registry.ModItems;

public class TileEldritchCapRenderer implements BlockEntityRenderer<EldritchAltarBlockEntity> {
   public TileEldritchCapRenderer(Context ctx) {
   }

   public void render(EldritchAltarBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      Level level = be.getLevel();
      if (level != null && be.getEyes() > 0) {
         ItemStack eye = createEyeStack();
         poseStack.pushPose();
         poseStack.translate(0.5F, 0.0F, 0.5F);

         for (byte b = 0; b < be.getEyes(); b++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(b * 90.0F));
            poseStack.translate(0.41F, 0.35F, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-18.0F));
            poseStack.scale(0.5F, 0.5F, 0.5F);
            Minecraft.getInstance()
               .getItemRenderer()
               .renderStatic(eye, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, level, (int)(be.getBlockPos().asLong() ^ b));
            poseStack.popPose();
         }

         poseStack.popPose();
      }
   }

   private static ItemStack createEyeStack() {
      return new ItemStack((ItemLike)ModItems.ELDRITCH_EYE.get());
   }
}
