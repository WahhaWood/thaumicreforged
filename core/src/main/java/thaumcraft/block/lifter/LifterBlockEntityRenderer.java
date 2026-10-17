package thaumcraft.block.lifter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class LifterBlockEntityRenderer implements BlockEntityRenderer<LifterBlockEntity> {
   private static final float EPS = 0.001F;
   private static final float INSET = 0.01F;
   private static final float BAND_Y0 = 0.9F;
   private static final float BAND_Y1 = 0.99F;
   private static final int SIDE_RGB = 14488063;
   public static final ResourceLocation GLOW_TEX = ResourceLocation.fromNamespaceAndPath("thaumcraft", "block/animatedglow");

   public LifterBlockEntityRenderer(Context ctx) {
   }

   public void render(LifterBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      Level level = be.getLevel();
      if (level != null) {
         BlockPos pos = be.getBlockPos();
         BlockState state = be.getBlockState();
         TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(GLOW_TEX);
         VertexConsumer vc = buffer.getBuffer(RenderType.translucent());
         int light = be.gettingPower() ? packedLight : LightTexture.pack(15, 15);
         poseStack.pushPose();
         Matrix4f pose = poseStack.last().pose();
         Matrix3f normal = poseStack.last().normal();
         if (Block.shouldRenderFace(state, level, pos, Direction.UP, pos.above())) {
            int rgbTop = 40960;
            emitUpAt(vc, pose, normal, sprite, light, packedOverlay, rgbTop, 0.99F);
         }

         int rgbSide = 14488063;
         if (Block.shouldRenderFace(state, level, pos, Direction.EAST, pos.east())) {
            emitEastAt(vc, pose, normal, sprite, light, packedOverlay, rgbSide, 0.99F);
         }

         if (Block.shouldRenderFace(state, level, pos, Direction.WEST, pos.west())) {
            emitWestAt(vc, pose, normal, sprite, light, packedOverlay, rgbSide, 0.01F);
         }

         if (Block.shouldRenderFace(state, level, pos, Direction.SOUTH, pos.south())) {
            emitSouthAt(vc, pose, normal, sprite, light, packedOverlay, rgbSide, 0.99F);
         }

         if (Block.shouldRenderFace(state, level, pos, Direction.NORTH, pos.north())) {
            emitNorthAt(vc, pose, normal, sprite, light, packedOverlay, rgbSide, 0.01F);
         }

         poseStack.popPose();
      }
   }

   private static void emitUpAt(VertexConsumer vc, Matrix4f pose, Matrix3f normal, TextureAtlasSprite s, int light, int overlay, int rgb, float y) {
      float u0 = s.getU0();
      float u1 = s.getU1();
      float v0 = s.getV0();
      float v1 = s.getV1();
      float r = (rgb >> 16 & 0xFF) / 255.0F;
      float g = (rgb >> 8 & 0xFF) / 255.0F;
      float b = (rgb & 0xFF) / 255.0F;
      v(vc, pose, normal, 0.0F, y, 0.0F, r, g, b, 1.0F, u0, v0, light, overlay, 0.0F, 1.0F, 0.0F);
      v(vc, pose, normal, 0.0F, y, 1.0F, r, g, b, 1.0F, u0, v1, light, overlay, 0.0F, 1.0F, 0.0F);
      v(vc, pose, normal, 1.0F, y, 1.0F, r, g, b, 1.0F, u1, v1, light, overlay, 0.0F, 1.0F, 0.0F);
      v(vc, pose, normal, 1.0F, y, 0.0F, r, g, b, 1.0F, u1, v0, light, overlay, 0.0F, 1.0F, 0.0F);
      v(vc, pose, normal, 1.0F, y, 0.0F, r, g, b, 1.0F, u1, v0, light, overlay, 0.0F, -1.0F, 0.0F);
      v(vc, pose, normal, 1.0F, y, 1.0F, r, g, b, 1.0F, u1, v1, light, overlay, 0.0F, -1.0F, 0.0F);
      v(vc, pose, normal, 0.0F, y, 1.0F, r, g, b, 1.0F, u0, v1, light, overlay, 0.0F, -1.0F, 0.0F);
      v(vc, pose, normal, 0.0F, y, 0.0F, r, g, b, 1.0F, u0, v0, light, overlay, 0.0F, -1.0F, 0.0F);
   }

   private static void emitNorthAt(VertexConsumer vc, Matrix4f pose, Matrix3f normal, TextureAtlasSprite s, int light, int overlay, int rgb, float z) {
      float u0 = s.getU0();
      float u1 = s.getU1();
      float v0 = s.getV0();
      float v1 = s.getV1();
      float r = (rgb >> 16 & 0xFF) / 255.0F;
      float g = (rgb >> 8 & 0xFF) / 255.0F;
      float b = (rgb & 0xFF) / 255.0F;
      v(vc, pose, normal, 1.0F, 0.0F, z, r, g, b, 1.0F, u1, v1, light, overlay, 0.0F, 0.0F, -1.0F);
      v(vc, pose, normal, 1.0F, 1.0F, z, r, g, b, 1.0F, u1, v0, light, overlay, 0.0F, 0.0F, -1.0F);
      v(vc, pose, normal, 0.0F, 1.0F, z, r, g, b, 1.0F, u0, v0, light, overlay, 0.0F, 0.0F, -1.0F);
      v(vc, pose, normal, 0.0F, 0.0F, z, r, g, b, 1.0F, u0, v1, light, overlay, 0.0F, 0.0F, -1.0F);
      v(vc, pose, normal, 0.0F, 0.0F, z, r, g, b, 1.0F, u0, v1, light, overlay, 0.0F, 0.0F, 1.0F);
      v(vc, pose, normal, 0.0F, 1.0F, z, r, g, b, 1.0F, u0, v0, light, overlay, 0.0F, 0.0F, 1.0F);
      v(vc, pose, normal, 1.0F, 1.0F, z, r, g, b, 1.0F, u1, v0, light, overlay, 0.0F, 0.0F, 1.0F);
      v(vc, pose, normal, 1.0F, 0.0F, z, r, g, b, 1.0F, u1, v1, light, overlay, 0.0F, 0.0F, 1.0F);
   }

   private static void emitSouthAt(VertexConsumer vc, Matrix4f pose, Matrix3f normal, TextureAtlasSprite s, int light, int overlay, int rgb, float z) {
      float u0 = s.getU0();
      float u1 = s.getU1();
      float v0 = s.getV0();
      float v1 = s.getV1();
      float r = (rgb >> 16 & 0xFF) / 255.0F;
      float g = (rgb >> 8 & 0xFF) / 255.0F;
      float b = (rgb & 0xFF) / 255.0F;
      v(vc, pose, normal, 0.0F, 0.0F, z, r, g, b, 1.0F, u1, v1, light, overlay, 0.0F, 0.0F, 1.0F);
      v(vc, pose, normal, 0.0F, 1.0F, z, r, g, b, 1.0F, u1, v0, light, overlay, 0.0F, 0.0F, 1.0F);
      v(vc, pose, normal, 1.0F, 1.0F, z, r, g, b, 1.0F, u0, v0, light, overlay, 0.0F, 0.0F, 1.0F);
      v(vc, pose, normal, 1.0F, 0.0F, z, r, g, b, 1.0F, u0, v1, light, overlay, 0.0F, 0.0F, 1.0F);
      v(vc, pose, normal, 1.0F, 0.0F, z, r, g, b, 1.0F, u0, v1, light, overlay, 0.0F, 0.0F, -1.0F);
      v(vc, pose, normal, 1.0F, 1.0F, z, r, g, b, 1.0F, u0, v0, light, overlay, 0.0F, 0.0F, -1.0F);
      v(vc, pose, normal, 0.0F, 1.0F, z, r, g, b, 1.0F, u1, v0, light, overlay, 0.0F, 0.0F, -1.0F);
      v(vc, pose, normal, 0.0F, 0.0F, z, r, g, b, 1.0F, u1, v1, light, overlay, 0.0F, 0.0F, -1.0F);
   }

   private static void emitWestAt(VertexConsumer vc, Matrix4f pose, Matrix3f normal, TextureAtlasSprite s, int light, int overlay, int rgb, float x) {
      float u0 = s.getU0();
      float u1 = s.getU1();
      float v0 = s.getV0();
      float v1 = s.getV1();
      float r = (rgb >> 16 & 0xFF) / 255.0F;
      float g = (rgb >> 8 & 0xFF) / 255.0F;
      float b = (rgb & 0xFF) / 255.0F;
      v(vc, pose, normal, x, 0.0F, 1.0F, r, g, b, 1.0F, u1, v1, light, overlay, -1.0F, 0.0F, 0.0F);
      v(vc, pose, normal, x, 1.0F, 1.0F, r, g, b, 1.0F, u1, v0, light, overlay, -1.0F, 0.0F, 0.0F);
      v(vc, pose, normal, x, 1.0F, 0.0F, r, g, b, 1.0F, u0, v0, light, overlay, -1.0F, 0.0F, 0.0F);
      v(vc, pose, normal, x, 0.0F, 0.0F, r, g, b, 1.0F, u0, v1, light, overlay, -1.0F, 0.0F, 0.0F);
      v(vc, pose, normal, x, 0.0F, 0.0F, r, g, b, 1.0F, u0, v1, light, overlay, 1.0F, 0.0F, 0.0F);
      v(vc, pose, normal, x, 1.0F, 0.0F, r, g, b, 1.0F, u0, v0, light, overlay, 1.0F, 0.0F, 0.0F);
      v(vc, pose, normal, x, 1.0F, 1.0F, r, g, b, 1.0F, u1, v0, light, overlay, 1.0F, 0.0F, 0.0F);
      v(vc, pose, normal, x, 0.0F, 1.0F, r, g, b, 1.0F, u1, v1, light, overlay, 1.0F, 0.0F, 0.0F);
   }

   private static void emitEastAt(VertexConsumer vc, Matrix4f pose, Matrix3f normal, TextureAtlasSprite s, int light, int overlay, int rgb, float x) {
      float u0 = s.getU0();
      float u1 = s.getU1();
      float v0 = s.getV0();
      float v1 = s.getV1();
      float r = (rgb >> 16 & 0xFF) / 255.0F;
      float g = (rgb >> 8 & 0xFF) / 255.0F;
      float b = (rgb & 0xFF) / 255.0F;
      v(vc, pose, normal, x, 0.0F, 0.0F, r, g, b, 1.0F, u1, v1, light, overlay, 1.0F, 0.0F, 0.0F);
      v(vc, pose, normal, x, 1.0F, 0.0F, r, g, b, 1.0F, u1, v0, light, overlay, 1.0F, 0.0F, 0.0F);
      v(vc, pose, normal, x, 1.0F, 1.0F, r, g, b, 1.0F, u0, v0, light, overlay, 1.0F, 0.0F, 0.0F);
      v(vc, pose, normal, x, 0.0F, 1.0F, r, g, b, 1.0F, u0, v1, light, overlay, 1.0F, 0.0F, 0.0F);
      v(vc, pose, normal, x, 0.0F, 1.0F, r, g, b, 1.0F, u0, v1, light, overlay, -1.0F, 0.0F, 0.0F);
      v(vc, pose, normal, x, 1.0F, 1.0F, r, g, b, 1.0F, u0, v0, light, overlay, -1.0F, 0.0F, 0.0F);
      v(vc, pose, normal, x, 1.0F, 0.0F, r, g, b, 1.0F, u1, v0, light, overlay, -1.0F, 0.0F, 0.0F);
      v(vc, pose, normal, x, 0.0F, 0.0F, r, g, b, 1.0F, u1, v1, light, overlay, -1.0F, 0.0F, 0.0F);
   }

   private static void v(
      VertexConsumer vc,
      Matrix4f pose,
      Matrix3f normal,
      float x,
      float y,
      float z,
      float r,
      float g,
      float b,
      float a,
      float u,
      float v,
      int light,
      int overlay,
      float nx,
      float ny,
      float nz
   ) {
      vc.addVertex(pose, x, y, z).setColor(r, g, b, a).setUv(u, v).setOverlay(overlay).setUv2(light & 65535, light >> 16).setNormal(nx, ny, nz);
   }
}
