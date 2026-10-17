package thaumcraft.block.wand_charger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import thaumcraft.Thaumcraft;
import thaumcraft.api.block.ThaumcraftEntityBlock;
import thaumcraft.item.wands.ItemWandCasting;
import thaumcraft.util.UtilsFX;

public class WandChargerRenderer implements BlockEntityRenderer<WandChargerBlockEntity> {
   private static final ResourceLocation WISPY = Thaumcraft.location("textures/misc/wispy.png");
   private static final int BEAM_COLOR = -7667457;

   public WandChargerRenderer(Context context) {
   }

   public void render(WandChargerBlockEntity charger, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      Level level = charger.getLevel();
      if (level != null) {
         ItemStack stack = charger.getItem(0);
         if (!stack.isEmpty() && stack.getItem() instanceof ItemWandCasting) {
            Minecraft minecraft = Minecraft.getInstance();
            Entity cameraEntity = minecraft.getCameraEntity();
            float ticks = (cameraEntity != null ? cameraEntity.tickCount : 0) + partialTick;
            float bob = Mth.sin(ticks % 32767.0F / 16.0F) * 0.05F;
            pose.pushPose();
            pose.translate(0.5, 0.7 + bob, 0.5);
            pose.mulPose(Axis.YP.rotationDegrees(ticks % 360.0F));
            pose.scale(0.5F, 0.5F, 0.5F);
            ItemStack displayStack = stack.copy();
            displayStack.setCount(1);
            int lightAbove = LevelRenderer.getLightColor(level, charger.getBlockPos().above());
            minecraft.getItemRenderer().renderStatic(displayStack, ItemDisplayContext.FIXED, lightAbove, OverlayTexture.NO_OVERLAY, pose, buffer, level, 0);
            pose.popPose();
            if ((Boolean)charger.getBlockState().getValue(ThaumcraftEntityBlock.ACTIVE)) {
               pose.pushPose();
               UtilsFX.drawFloatyLine(
                  pose, buffer, 0.5, 1.65 - bob * 2.0F, 0.5, 0.5, 0.5, 0.5, partialTick, -7667457, WISPY, -0.02F, Math.min(ticks, 10.0F) / 10.0F
               );
               pose.popPose();
            }
         }
      }
   }

   /** The block entity's own box: NeoForge 21.1 asks the renderer, not the block entity. */
   @Override
   public AABB getRenderBoundingBox(WandChargerBlockEntity blockEntity) {
      return blockEntity.getRenderBoundingBox();
   }
}
