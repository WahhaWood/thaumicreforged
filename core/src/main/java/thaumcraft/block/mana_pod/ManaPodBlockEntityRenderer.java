package thaumcraft.block.mana_pod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.api.aspects.Aspect;

public class ManaPodBlockEntityRenderer implements BlockEntityRenderer<ManaPodBlockEntity> {
   public static final ResourceLocation POD0_TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/manapod_0.png");
   public static final ResourceLocation POD2_TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/manapod_2.png");
   private final ModelManaPod model;

   public ManaPodBlockEntityRenderer(Context ctx) {
      this.model = new ModelManaPod(ctx.bakeLayer(ModelManaPod.LAYER_LOCATION));
   }

   public void render(ManaPodBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int packedLight, int packedOverlay) {
      Aspect aspect = Aspect.PLANT;
      int age;
      if (be.getLevel() == null) {
         age = 5;
      } else {
         if (!(be.getBlockState().getBlock() instanceof BlockManaPod)) {
            return;
         }

         age = (Integer)be.getBlockState().getValue(BlockManaPod.AGE);
         if (be.aspect != null) {
            aspect = be.aspect;
         }
      }

      if (age > 1) {
         float baseR = 0.14509805F;
         float baseG = 0.6156863F;
         float baseB = 0.45882353F;
         float r = baseR;
         float g = baseG;
         float b = baseB;
         if (be.aspect != null) {
            Color c = new Color(aspect.getColor());
            float ar = c.getRed() / 255.0F;
            float ag = c.getGreen() / 255.0F;
            float ab = c.getBlue() / 255.0F;
            float tintStrength = age <= 2 ? 0.35F : Math.min(1.0F, 0.45F + (age - 2) * 0.18F);
            r = Mth.lerp(tintStrength, baseR, ar);
            g = Mth.lerp(tintStrength, baseG, ag);
            b = Mth.lerp(tintStrength, baseB, ab);
         }

         pose.pushPose();
         pose.translate(0.5, 0.75, 0.5);
         pose.mulPose(Axis.XP.rotationDegrees(180.0F));
         if (age > 2) {
            LocalPlayer player = Minecraft.getInstance().player;
            int ticks = player != null ? player.tickCount : 0;
            float n15 = Mth.sin((ticks + be.hashCode() % 100 + partialTick) / 8.0F) * 0.1F + 0.9F;
            Mth.sin((ticks + be.hashCode() % 100 + partialTick) / 8.0F);
            int n17 = age * 10 + (int)(150.0F * n15);
            int glow = Mth.clamp(n17 / 16, 0, 15);
            int baseBlock = LightTexture.block(packedLight);
            int baseSky = LightTexture.sky(packedLight);
            int pod0Light = LightTexture.pack(Math.max(baseBlock, glow), baseSky);
            pose.pushPose();
            pose.translate(0.0, 0.1, 0.0);
            float s = (float)(0.125 * age * n15);
            pose.scale(s, s, s);
            VertexConsumer vc0 = buffers.getBuffer(RenderType.entityTranslucent(POD0_TEX));
            this.model.renderPod0(pose, vc0, pod0Light, OverlayTexture.NO_OVERLAY, r, g, b, 0.95F);
            pose.popPose();
         }

         pose.scale((float)(0.15 * age), (float)(0.15 * age), (float)(0.15 * age));
         VertexConsumer vc2 = buffers.getBuffer(RenderType.entityTranslucent(POD2_TEX));
         this.model.renderPod2(pose, vc2, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 0.9F);
         pose.popPose();
      }
   }
}
