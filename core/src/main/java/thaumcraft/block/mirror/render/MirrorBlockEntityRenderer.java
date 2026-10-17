package thaumcraft.block.mirror.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;
import thaumcraft.api.blockentity.ThaumcraftBlockEntity;
import thaumcraft.block.mirror.MirrorBlockEntity;
import thaumcraft.block.mirror.MirrorEssenceBlockEntity;

public class MirrorBlockEntityRenderer implements BlockEntityRenderer<ThaumcraftBlockEntity> {
   public static final ResourceLocation TEX_GLASS = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/block/mirror_pane.png");
   public static final ResourceLocation TEX_GLASS_OPEN = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/block/mirror_pane_open.png");
   public static final ResourceLocation TEX_GLASS_TRANS = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/block/mirror_pane_trans.png");
   public static final ResourceLocation TEX_TUNNEL = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/tunnel.png");
   public static final ResourceLocation TEX_PARTICLE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particlefield.png");
   private static final float GLASS_SIZE = 0.45F;
   private static final double BASE_PLANE_Z = -0.43;
   private static final double PORTAL_Z = 0.0015;
   private static final double GLASS_Z = 0.003;

   public MirrorBlockEntityRenderer(Context context) {
   }

   public void render(ThaumcraftBlockEntity tile, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
      boolean linked = false;
      boolean transmitting = false;
      int instability = 0;
      boolean inWorld = tile.hasLevel();
      Direction facing = Direction.SOUTH;
      if (tile.getBlockState() != null && tile.getBlockState().hasProperty(BlockStateProperties.FACING)) {
         facing = (Direction)tile.getBlockState().getValue(BlockStateProperties.FACING);
      }

      ResourceLocation glassTexture;
      if (tile instanceof MirrorBlockEntity mirror) {
         linked = mirror.linked;
         instability = mirror.instability;
         transmitting = linked && instability > 0;
         glassTexture = linked ? (transmitting ? TEX_GLASS_TRANS : TEX_GLASS_OPEN) : TEX_GLASS;
      } else {
         if (!(tile instanceof MirrorEssenceBlockEntity mirrorEssence)) {
            return;
         }

         linked = mirrorEssence.linked;
         instability = mirrorEssence.instability;
         transmitting = linked && instability > 0;
         glassTexture = linked ? (transmitting ? TEX_GLASS_TRANS : TEX_GLASS_OPEN) : TEX_GLASS;
      }

      poseStack.pushPose();
      poseStack.translate(0.5, 0.5, 0.5);
      applyFacingRotation(poseStack, facing);
      poseStack.translate(0.0, 0.0, -0.43);
      if (instability > 0) {
         float strength = Math.min(instability, 10) / 100.0F;
         float t = (float)(System.currentTimeMillis() % 100000L) / 1000.0F;
         float rx = (Mth.sin(t * 17.0F) * 0.6F + Mth.sin(t * 7.0F) * 0.4F) * strength;
         float ry = (Mth.cos(t * 13.0F) * 0.6F + Mth.cos(t * 5.0F) * 0.4F) * strength;
         poseStack.translate(rx, ry, 0.0F);
      }

      if (linked && inWorld) {
         poseStack.pushPose();
         poseStack.translate(0.0, 0.0, 0.0015);
         this.renderPortal1710(poseStack, buffer);
         poseStack.popPose();
      }

      int glassLight = inWorld ? combinedLight : 15728880;
      poseStack.pushPose();
      poseStack.translate(0.0, 0.0, 0.003);
      this.renderGlassOverlay(poseStack, buffer, glassLight, glassTexture, transmitting, linked);
      poseStack.popPose();
      poseStack.popPose();
   }

   private static void applyFacingRotation(PoseStack poseStack, Direction facing) {
      switch (facing) {
         case NORTH:
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            break;
         case SOUTH:
            poseStack.mulPose(Axis.YP.rotationDegrees(0.0F));
            break;
         case EAST:
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            break;
         case WEST:
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            break;
         case UP:
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            break;
         case DOWN:
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
      }
   }

   private void renderGlassOverlay(PoseStack poseStack, MultiBufferSource buffer, int light, ResourceLocation texture, boolean rotating, boolean glowWhenLinked) {
      poseStack.pushPose();
      if (rotating) {
         long time = System.currentTimeMillis();
         float angle = (float)(time % 5000L) / 5000.0F * 360.0F;
         poseStack.mulPose(Axis.ZP.rotationDegrees(-angle));
      }

      RenderType rt = glowWhenLinked ? RenderType.entityTranslucentEmissive(texture) : RenderType.entityTranslucent(texture);
      VertexConsumer builder = buffer.getBuffer(rt);
      Matrix4f mat = poseStack.last().pose();
      addQuad(builder, mat, 0.45F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, glowWhenLinked ? 15728880 : light);
      poseStack.popPose();
   }

   private void renderPortal1710(PoseStack poseStack, MultiBufferSource buffer) {
      VertexConsumer builderTunnel = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEX_TUNNEL));
      VertexConsumer builderParticles = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEX_PARTICLE));
      long time = System.currentTimeMillis();
      Random random = new Random(31100L);
      poseStack.pushPose();
      poseStack.translate(0.0F, 0.0F, 0.002F);

      for (int i = 0; i < 16; i++) {
         poseStack.pushPose();
         float layerIndexInv = 16 - i;
         float scale = 0.0625F;
         float brightness = 1.0F / (layerIndexInv + 1.0F);
         VertexConsumer currentBuilder = builderParticles;
         if (i == 0) {
            currentBuilder = builderTunnel;
            brightness = 0.12F;
            scale = 0.125F;
         }

         if (i == 1) {
            scale = 0.5F;
         }

         poseStack.translate(0.0F, 0.0F, 0.0045F * i);
         float rotation = (i * i * 4321 + i * 9) * 2.0F;
         poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
         float quadSize = 0.3125F;
         float r = 1.0F;
         float g = 1.0F;
         float b = 1.0F;
         if (i > 0) {
            r = random.nextFloat() * 0.5F + 0.1F;
            g = random.nextFloat() * 0.5F + 0.4F;
            b = random.nextFloat() * 0.5F + 0.5F;
         }

         float finalR = r * brightness;
         float finalG = g * brightness;
         float finalB = b * brightness;
         float timeOffset = (float)(time % 700000L) / 250000.0F;
         float alpha = i == 0 ? 0.85F : 0.6F;
         float uvScale = 1.0F / scale;
         float u0 = 0.0F;
         float v1 = timeOffset + uvScale;
         addQuad(currentBuilder, poseStack.last().pose(), quadSize, finalR, finalG, finalB, alpha, u0, timeOffset, uvScale, v1, 15728880);
         poseStack.popPose();
      }

      poseStack.popPose();
   }

   private static void addQuad(
      VertexConsumer builder, Matrix4f mat, float size, float r, float g, float b, float a, float u0, float v0, float u1, float v1, int light
   ) {
      builder.addVertex(mat, -size, -size, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u0, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(light & 65535, light >> 16)
         .setNormal(0.0F, 0.0F, 1.0F);
      builder.addVertex(mat, size, -size, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u1, v0)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(light & 65535, light >> 16)
         .setNormal(0.0F, 0.0F, 1.0F);
      builder.addVertex(mat, size, size, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u1, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(light & 65535, light >> 16)
         .setNormal(0.0F, 0.0F, 1.0F);
      builder.addVertex(mat, -size, size, 0.0F)
         .setColor(r, g, b, a)
         .setUv(u0, v1)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setUv2(light & 65535, light >> 16)
         .setNormal(0.0F, 0.0F, 1.0F);
   }
}
