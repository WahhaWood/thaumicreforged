package thaumcraft.block.wand_recharger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.util.UtilsFX;

@OnlyIn(Dist.CLIENT)
public class WandPedestalBlockEntityRenderer implements BlockEntityRenderer<WandPedestalBlockEntity> {
   public static final ResourceLocation WISPY = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/wispy.png");

   public WandPedestalBlockEntityRenderer(Context ctx) {
   }

   public void render(WandPedestalBlockEntity te, float pt, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      Level level = te.getLevel();
      if (level != null) {
         ItemStack stack = te.getItem(0);
         if (!stack.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            Entity cam = mc.getCameraEntity();
            float t = (cam != null ? cam.tickCount : 0) + pt;
            float bob = Mth.sin(t % 32767.0F / 16.0F) * 0.05F;
            pose.pushPose();
            pose.translate(0.5, 1.35 + bob, 0.5);
            pose.mulPose(Axis.YP.rotationDegrees(t % 360.0F));
            pose.scale(0.5F, 0.5F, 0.5F);
            ItemStack one = stack.copy();
            one.setCount(1);
            int lightAbove = LevelRenderer.getLightColor(level, te.getBlockPos().above());
            mc.getItemRenderer().renderStatic(one, ItemDisplayContext.FIXED, lightAbove, OverlayTexture.NO_OVERLAY, pose, buffer, level, 0);
            pose.popPose();
            if (te.draining) {
               BlockPos o = te.getBlockPos();
               double x1 = 0.5;
               double y1 = 1.65 - bob * 2.0F;
               double z1 = 0.5;
               double x2 = te.drainX - o.getX() + 0.5;
               double y2 = te.drainY - o.getY() + 0.5;
               double z2 = te.drainZ - o.getZ() + 0.5;
               pose.pushPose();
               UtilsFX.drawFloatyLine(pose, buffer, x1, y1, z1, x2, y2, z2, pt, te.drainColor, WISPY, -0.02F, Math.min(t, 10.0F) / 10.0F);
               pose.popPose();
            }
         }
      }
   }
}
